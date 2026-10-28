package ie.bitstep.fractalith.common.api;

import java.util.Arrays;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PropertyContainsCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var attributes = metadata.getAnnotationAttributes(ConditionalOnPropertyContains.class.getName());
        if (attributes == null) {
            return true;
        }

        String prefix = (String) attributes.get("prefix");
        String name = (String) attributes.get("name");
        String expected = (String) attributes.get("value");
        String property = prefix + "." + name;
        String value = context.getEnvironment().getProperty(property);

        if (value == null || value.isBlank()) {
            return false;
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .anyMatch(item -> item.equalsIgnoreCase(expected));
    }
}
