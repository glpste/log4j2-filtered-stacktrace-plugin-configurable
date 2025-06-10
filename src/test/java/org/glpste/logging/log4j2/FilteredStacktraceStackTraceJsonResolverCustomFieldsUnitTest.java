package org.glpste.logging.log4j2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.layout.template.json.util.QueueingRecyclerFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FilteredStacktraceStackTraceJsonResolverCustomFieldsUnitTest {

    private JsonWriter jsonWriter;

    @Mock
    private EventResolverContext mockedEventResolverContext;
    @Mock
    private TemplateResolverConfig mockedConfig;

    @BeforeEach
    void setUp() {
        when(mockedEventResolverContext.getRecyclerFactory()).thenReturn(new QueueingRecyclerFactory(LinkedList::new));
        when(mockedEventResolverContext.getMaxStringByteCount()).thenReturn(60000);

        jsonWriter = JsonWriter.newBuilder().setMaxStringLength(60000).setTruncatedStringSuffix("...").build();
    }

    @Test
    void testCustomFields() {
        // GIVEN all custom fields are configured
        String nameField = UUID.randomUUID().toString();
        String messageField = UUID.randomUUID().toString();
        String stackField = UUID.randomUUID().toString();
        String countField = UUID.randomUUID().toString();

        TemplateResolverConfig config = givenConfig(Collections.singletonList("org.glpste.logging.log4j2"), Collections.emptyList());
        when(config.getString(ConfigProperty.NAME.getKey())).thenReturn(nameField);
        when(config.getString(ConfigProperty.MESSAGE.getKey())).thenReturn(messageField);
        when(config.getString(ConfigProperty.STACK.getKey())).thenReturn(stackField);
        when(config.getString(ConfigProperty.COUNT.getKey())).thenReturn(countField);

        // WHEN resolving an event with an exception
        FilteredStacktraceExceptionResolver filteredStacktraceExceptionResolver = new FilteredStacktraceExceptionResolver(mockedEventResolverContext, config);
        LogEvent logEvent = TestLogEvent.builder()
                .thrown(wrappedThrowable())
                .build();

        filteredStacktraceExceptionResolver.resolve(logEvent, jsonWriter);
        String actualStringOutput = jsonWriter.getStringBuilder().toString();

        // THEN all custom fields are present in the output
        JSONObject actualLogOutput = new JSONObject(actualStringOutput);
        assertThat(actualLogOutput.get(nameField)).isEqualTo("java.lang.RuntimeException");
        assertThat(actualLogOutput.get(messageField)).isEqualTo("java.lang.ArithmeticException: / by zero");
        assertThat(actualLogOutput.get(countField)).isEqualTo(5);

        assertThat(actualLogOutput.get(stackField).toString().split(System.lineSeparator()))
                .filteredOn(line -> !line.startsWith("\t[suppressed"))
                .filteredOn(line -> !line.startsWith("Caused by"))
                .filteredOn(line -> !line.startsWith("java.lang.RuntimeException"))
                .allMatch(line -> line.startsWith("\tat "));
    }

    private TemplateResolverConfig givenConfig(List<String> filterList, List<String> allowList) {
        TemplateResolverConfig config = Mockito.mock(TemplateResolverConfig.class);
        when(config.getList(ConfigProperty.LIST_FILTER.getKey(), String.class)).thenReturn(filterList);
        when(config.getList(ConfigProperty.LIST_ALLOW.getKey(), String.class)).thenReturn(allowList);
        return config;
    }

    private Throwable wrappedThrowable() {
        return new RuntimeException(createExceptionWithStacktrace());
    }

    private Exception createExceptionWithStacktrace() {
        try {
            throw new ArithmeticException("/ by zero");
        } catch (Exception e) {
            return e;
        }
    }
}
