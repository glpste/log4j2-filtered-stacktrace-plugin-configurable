package com.hlag.logging.log4j2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolver;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolver;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines a custom resolver to remove stacktrace lines that are irrelevant.
 * See <a href="https://stackoverflow.com/questions/70614495/is-there-a-way-to-override-the-exceptionresolver-of-jsontemplatelayout-of-log4j2/77143208#77143208">Stackoverflow</a>
 *
 * @author Copied from Apache log4j2 and modified by takanuva15
 */
class FilteredStacktraceExceptionResolver implements EventResolver {

    public static final String PROPERTY_LIST_ALLOW = "allowedPackages";
    public static final String PROPERTY_LIST_FILTER = "filteredPackages";
    public static final String PROPERTY_NAME_FIELD = "nameField";
    public static final String PROPERTY_MESSAGE_FIELD = "messageField";
    public static final String PROPERTY_STACK_FIELD = "stackField";
    public static final String PROPERTY_COUNT_FIELD = "countField";

    private final TemplateResolver<Throwable> internalResolver;

    FilteredStacktraceExceptionResolver(EventResolverContext context, TemplateResolverConfig resolverConfig) {
        JsonTemplateFieldConfig fieldConfig = new JsonTemplateFieldConfig.Builder()
                .nameField(resolverConfig.getString(PROPERTY_NAME_FIELD))
                .messageField(resolverConfig.getString(PROPERTY_MESSAGE_FIELD))
                .stackField(resolverConfig.getString(PROPERTY_STACK_FIELD))
                .countField(resolverConfig.getString(PROPERTY_COUNT_FIELD))
                .allowedPackages(resolverConfig.getList(PROPERTY_LIST_ALLOW, String.class))
                .filteredPackages(resolverConfig.getList(PROPERTY_LIST_FILTER, String.class))
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
