package org.glpste.logging.log4j2;

import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolver;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.layout.template.json.util.Recycler;
import org.apache.logging.log4j.layout.template.json.util.RecyclerFactory;
import org.apache.logging.log4j.layout.template.json.util.TruncatingBufferedPrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Holds the logic on how to delete irrelevant stacktrace lines and formats the stacktrace showing all "Caused by".
 * <p>
 * Originally based on <a href="https://stackoverflow.com/questions/70614495/is-there-a-way-to-override-the-exceptionresolver-of-jsontemplatelayout-of-log4j2/77143208#77143208">Stackoverflow</a>
 * but works totally different now.
 * </p>
 */
class FilteredStacktraceStackTraceJsonResolver implements TemplateResolver<Throwable> {
    private final Recycler<TruncatingBufferedPrintWriter> destWriterRecycler;
    private final List<String> filteredPackages;
    private final List<String> allowedPackages;
    private final JsonTemplateFieldConfig fieldConfig;

    FilteredStacktraceStackTraceJsonResolver(EventResolverContext context, JsonTemplateFieldConfig fieldConfig) {
        final Supplier<TruncatingBufferedPrintWriter> writerSupplier = () -> TruncatingBufferedPrintWriter.ofCapacity(context.getMaxStringByteCount());
        final RecyclerFactory recyclerFactory = context.getRecyclerFactory();

        if (fieldConfig.getAllowedPackages() == null) {
            throw new IllegalArgumentException();
        }

        this.destWriterRecycler = recyclerFactory.create(writerSupplier, TruncatingBufferedPrintWriter::close);
        this.filteredPackages = fieldConfig.getFilteredPackages();
        this.allowedPackages = fieldConfig.getAllowedPackages();
        this.fieldConfig = fieldConfig;
    }

    @Override
    public void resolve(Throwable throwable, JsonWriter jsonWriter) {
        Map<String, Object> jsonAttributes = new HashMap<>();
        List<Cause> allCauses = flattenAndFilterAllCauses(throwable);

        try (TruncatingBufferedPrintWriter stacktraceAsStringWriter = destWriterRecycler.acquire()) {
            int totalLinesFiltered = 0;
            boolean firstCause = true;

            stacktraceAsStringWriter.append(String.format("%s: %s", throwable.getClass().getName(), throwable.getMessage()));

            for (Cause cause : allCauses) {
                if (!firstCause) {
                    // write a "Caused by" including cause message, like Java's native stack exporter.
                    stacktraceAsStringWriter.append(System.lineSeparator());
                    stacktraceAsStringWriter.append(String.format("Caused by %s: %s", cause.throwable.getClass().getName(), cause.throwable.getMessage()));
                }

                int currentFilteredLines = 0;
                firstCause = false;

                for (FilteredStacktraceElement stacktraceElement : cause.filteredStacktraceElements) {
                    if (!stacktraceElement.filtered) {
                        if (currentFilteredLines > 0) {
                            stacktraceAsStringWriter.append(System.lineSeparator());
                            stacktraceAsStringWriter.append(String.format("\t[suppressed %d lines]", currentFilteredLines));
                            currentFilteredLines = 0;
                        }

                        stacktraceAsStringWriter.append(System.lineSeparator());
                        stacktraceAsStringWriter.append(String.format("\tat %s", stacktraceElement.stackTraceElement));
                    } else {
                        currentFilteredLines++;
                        totalLinesFiltered++;
                    }
                }

                // in case the filtered stacktrace elements are at the end
                if (currentFilteredLines > 0) {
                    stacktraceAsStringWriter.append(System.lineSeparator());
                    stacktraceAsStringWriter.append(String.format("\t[suppressed %d lines]", currentFilteredLines));
                }
            }

            jsonAttributes.put(fieldConfig.getStackField(), stacktraceAsStringWriter.toString());
            jsonAttributes.put(fieldConfig.getCountField(), totalLinesFiltered);
        }

        jsonAttributes.put(fieldConfig.getMessageField(), throwable.getMessage());
        jsonAttributes.put(fieldConfig.getNameField(), throwable.getClass().getName());

        jsonWriter.writeObject(jsonAttributes);
    }

    private List<Cause> flattenAndFilterAllCauses(Throwable throwable) {
        List<Cause> allCauses = new ArrayList<>();

        Throwable lastThrowable = null;

        while (throwable != null && lastThrowable != throwable) {
            List<FilteredStacktraceElement> filteredStacktraceElements = applyStacktraceFilter(throwable.getStackTrace());
            allCauses.add(new Cause(throwable, filteredStacktraceElements));

            lastThrowable = throwable;
            throwable = throwable.getCause();
        }

        return allCauses;
    }

    private boolean classIsInFilteredPackage(String className) {
        for (String prefix : allowedPackages) {
            if (className.startsWith(prefix)) {
                return false;
            }
        }

        // if a whitelist is present everything else is blacklisted
        if (! allowedPackages.isEmpty()) {
            return true;
        }

        for (String prefix : filteredPackages) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private List<FilteredStacktraceElement> applyStacktraceFilter(final StackTraceElement[] stacktraceElements) {
        final List<FilteredStacktraceElement> filteredStacktraceElements = new ArrayList<>();

        for (StackTraceElement stacktraceElement : stacktraceElements) {
            boolean stacktraceElementFiltered = classIsInFilteredPackage(stacktraceElement.getClassName());

            filteredStacktraceElements.add(new FilteredStacktraceElement(stacktraceElement, stacktraceElementFiltered));
        }

        return filteredStacktraceElements;
    }

    private static final class Cause {
        private final Throwable throwable;
        private final List<FilteredStacktraceElement> filteredStacktraceElements;

        private Cause(Throwable throwable, List<FilteredStacktraceElement> filteredStacktraceElements) {
            this.throwable = throwable;
            this.filteredStacktraceElements = filteredStacktraceElements;
        }
    }

    private static final class FilteredStacktraceElement {
        private final StackTraceElement stackTraceElement;
        private final boolean filtered;

        private FilteredStacktraceElement(StackTraceElement stackTraceElement, boolean filtered) {
            this.stackTraceElement = stackTraceElement;
            this.filtered = filtered;
        }
    }
}
