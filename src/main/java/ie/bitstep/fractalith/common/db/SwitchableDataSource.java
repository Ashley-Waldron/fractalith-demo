package ie.bitstep.fractalith.common.db;

import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.jdbc.datasource.AbstractDataSource;

/**
 * A DataSource whose backing pool can be replaced at runtime without failing callers.
 *
 * <p>{@link #freeze} makes new {@code getConnection()} calls wait and returns once every
 * connection already handed out has been closed. {@link #swapAndResume} then replaces the pool
 * and releases the waiting callers. If nobody completes the switch, the freeze lifts itself on
 * the original pool after a timeout so a failed migration cannot cause an outage.
 */
public class SwitchableDataSource extends AbstractDataSource implements AutoCloseable {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();
    private final Duration maxWait;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "datasource-switch");
        thread.setDaemon(true);
        return thread;
    });

    private HikariDataSource delegate;
    private boolean frozen;
    private int borrowed;
    private ScheduledFuture<?> autoResume;

    public SwitchableDataSource(HikariDataSource initial, Duration maxWait) {
        this.delegate = initial;
        this.maxWait = maxWait;
    }

    @Override
    public Connection getConnection() throws SQLException {
        HikariDataSource target = acquireSlot();
        try {
            return track(target.getConnection());
        } catch (SQLException | RuntimeException e) {
            releaseSlot();
            throw e;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        HikariDataSource target = acquireSlot();
        try {
            return track(target.getConnection(username, password));
        } catch (SQLException | RuntimeException e) {
            releaseSlot();
            throw e;
        }
    }

    public void freeze(Duration drainTimeout, Duration autoResumeAfter) {
        lock.lock();
        try {
            if (frozen) {
                throw new IllegalStateException("Datasource is already frozen");
            }
            frozen = true;
            long nanos = drainTimeout.toNanos();
            while (borrowed > 0) {
                if (nanos <= 0) {
                    frozen = false;
                    changed.signalAll();
                    throw new IllegalStateException("In-flight connections did not drain within " + drainTimeout);
                }
                nanos = changed.awaitNanos(nanos);
            }
            autoResume = scheduler.schedule(this::resume, autoResumeAfter.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            frozen = false;
            changed.signalAll();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while draining connections", e);
        } finally {
            lock.unlock();
        }
    }

    public void swapAndResume(HikariDataSource next) {
        HikariDataSource previous;
        lock.lock();
        try {
            if (!frozen) {
                throw new IllegalStateException("Datasource must be frozen before it can be switched");
            }
            previous = delegate;
            delegate = next;
            frozen = false;
            cancelAutoResume();
            changed.signalAll();
        } finally {
            lock.unlock();
        }
        previous.close();
    }

    public void resume() {
        lock.lock();
        try {
            frozen = false;
            cancelAutoResume();
            changed.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isFrozen() {
        lock.lock();
        try {
            return frozen;
        } finally {
            lock.unlock();
        }
    }

    public String currentUrl() {
        lock.lock();
        try {
            return delegate.getJdbcUrl();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
        lock.lock();
        try {
            delegate.close();
        } finally {
            lock.unlock();
        }
    }

    private HikariDataSource acquireSlot() throws SQLException {
        lock.lock();
        try {
            long nanos = maxWait.toNanos();
            while (frozen) {
                if (nanos <= 0) {
                    throw new SQLTransientConnectionException("Datasource stayed frozen for longer than " + maxWait);
                }
                nanos = changed.awaitNanos(nanos);
            }
            borrowed++;
            return delegate;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLTransientConnectionException("Interrupted while waiting for the datasource", e);
        } finally {
            lock.unlock();
        }
    }

    private void releaseSlot() {
        lock.lock();
        try {
            borrowed--;
            changed.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void cancelAutoResume() {
        if (autoResume != null) {
            autoResume.cancel(false);
            autoResume = null;
        }
    }

    private Connection track(Connection connection) {
        AtomicBoolean released = new AtomicBoolean();
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class},
                (proxy, method, args) -> {
                    try {
                        return method.invoke(connection, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    } finally {
                        if (method.getName().equals("close") && released.compareAndSet(false, true)) {
                            releaseSlot();
                        }
                    }
                });
    }
}
