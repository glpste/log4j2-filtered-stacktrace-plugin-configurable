package org.glpste.logging.log4j2;

import static org.glpste.logging.log4j2.FilteredStacktraceExceptionResolver.PROPERTY_LIST_ALLOW;
import static org.glpste.logging.log4j2.JsonTemplateFieldConfig.FIELD_DEFAULT_STACK;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.layout.template.json.util.QueueingRecyclerFactory;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.LinkedList;

@ExtendWith(MockitoExtension.class)
class FilteredStacktraceStackTraceJsonResolverWhitelistUnitTest {
    private FilteredStacktraceExceptionResolver filteredStacktraceExceptionResolver;

    private JsonWriter jsonWriter;

    @Mock
    private EventResolverContext mockedEventResolverContext;
    @Mock
    private TemplateResolverConfig mockedConfig;

    @BeforeEach
    void setUp() {
        Mockito.when(mockedEventResolverContext.getRecyclerFactory()).thenReturn(new QueueingRecyclerFactory(LinkedList::new));
        Mockito.when(mockedEventResolverContext.getMaxStringByteCount()).thenReturn(60000);

        // sets the package whitelist
        Mockito.when(mockedConfig.getList(PROPERTY_LIST_ALLOW, String.class)).thenReturn(Collections.singletonList("com.hlag.logging.log4j2"));

        jsonWriter = JsonWriter.newBuilder().setMaxStringLength(60000).setTruncatedStringSuffix("...").build();

        filteredStacktraceExceptionResolver = new FilteredStacktraceExceptionResolver(mockedEventResolverContext, mockedConfig);
    }

    @Test
    void shouldHaveWhitelistedPackagesOnly_whenResolve_givenThrowable() {
        LogEvent givenLogEvent = TestLogEvent.builder().thrown(wrappedThrowable()).build();

        filteredStacktraceExceptionResolver.resolve(givenLogEvent, jsonWriter);
        String actualStringOutput = jsonWriter.getStringBuilder().toString();

        JSONObject actualLogOutput = new JSONObject(actualStringOutput);

        Assertions.assertThat(actualLogOutput.get(FIELD_DEFAULT_STACK).toString().split(System.lineSeparator()))
                .filteredOn(line -> line.startsWith("\tat "))
                .allSatisfy(line -> Assertions.assertThat(line).startsWith("\tat com.hlag.logging.log4j2."));
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
