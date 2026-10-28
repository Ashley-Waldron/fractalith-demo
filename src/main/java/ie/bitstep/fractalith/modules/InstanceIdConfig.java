package ie.bitstep.fractalith.modules;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
class InstanceIdConfig {

    @Bean
    FilterRegistrationBean<OncePerRequestFilter> instanceIdHeaderFilter(@Value("${instance.id:local}") String instanceId) {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                response.setHeader("X-Instance-Id", instanceId);
                chain.doFilter(request, response);
            }
        };
        return new FilterRegistrationBean<>(filter);
    }
}
