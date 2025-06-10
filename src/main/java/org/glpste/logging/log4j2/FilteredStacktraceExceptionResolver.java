package org.glpste.logging.log4j2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolver;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolver;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;

/**
 * Defines a custom resolver to remove stacktrace lines that are irrelevant.
 * See <a href="https://stackoverflow.com/questions/70614495/is-there-a-way-to-override-the-exceptionresolver-of-jsontemplatelayout-of-log4j2/77143208#77143208">Stackoverflow</a>
 *
 * @author Copied from Apache log4j2 and modified by takanuva15
 */
class FilteredStacktraceExceptionResolver implements EventResolver {

    private final TemplateResolver<Throwable> internalResolver;

    FilteredStacktraceExceptionResolver(EventResolverContext context, TemplateResolverConfig resolverConfig) {
        JsonTemplateFieldConfig fieldConfig = JsonTemplateFieldConfig.builder()
                .nameField(resolverConfig.getString(ConfigProperty.NAME.getKey()))
                .messageField(resolverConfig.getString(ConfigProperty.MESSAGE.getKey()))
                .stackField(resolverConfig.getString(ConfigProperty.STACK.getKey()))
                .countField(resolverConfig.getString(ConfigProperty.COUNT.getKey()))
                .allowedPackages(resolverConfig.getList(ConfigProperty.LIST_ALLOW.getKey(), String.class))
                .filteredPackages(resolverConfig.getList(ConfigProperty.LIST_FILTER.getKey(), String.class))
                .build();

        this.internalResolver = new FilteredStacktraceStackTraceJsonResolver(context, fieldConfig);
    }

    @Override
    public void resolve(LogEvent logEvent, JsonWriter jsonWriter) {
        final Throwable exception = logEvent.getThrown();

        if (exception == null) {
            jsonWriter.writeNull();
        } else {
            internalResolver.resolve(exception, jsonWriter);
        }
    }

    @Override
    public boolean isResolvable(final LogEvent logEvent) {
        return logEvent.getThrown() != null;
    }

}
