package org.glpste.logging.log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.layout.template.json.util.QueueingRecyclerFactory;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilteredStacktraceExceptionResolverUnitTest {
    private static final List<String> PACKAGE_TO_REMOVE_FROM_STACKTRACE = Arrays.asList("org.junit");
    private FilteredStacktraceExceptionResolver filteredStacktraceExceptionResolver;

    @Mock
    private EventResolverContext mockedEventResolverContext;
    @Mock
    private TemplateResolverConfig mockedConfig;

    private JsonWriter jsonWriter;

    @BeforeEach
    void setUp() {
        // lenient() needed to build the filteredStacktraceExceptionResolver
        Mockito.lenient().when(mockedEventResolverContext.getRecyclerFactory()).thenReturn(new QueueingRecyclerFactory(LinkedList::new));
        Mockito.lenient().when(mockedEventResolverContext.getMaxStringByteCount()).thenReturn(10000);

        Mockito.when(mockedConfig.getList(ConfigProperty.LIST_FILTER.getKey(), String.class)).thenReturn(PACKAGE_TO_REMOVE_FROM_STACKTRACE);
        Mockito.when(mockedConfig.getList(ConfigProperty.LIST_ALLOW.getKey(), String.class)).thenReturn(new ArrayList<>());

        jsonWriter = JsonWriter.newBuilder().setMaxStringLength(10000).setTruncatedStringSuffix("...").build();

        filteredStacktraceExceptionResolver = new FilteredStacktraceExceptionResolver(mockedEventResolverContext, mockedConfig);
    }

    @Test
    void shouldBeAbleToCreateInstance_whenConstructor_givenAdditionalPackagesIsNull() {
        Mockito.when(mockedConfig.getList(ConfigProperty.LIST_FILTER.getKey(), String.class)).thenReturn(null);

        NullPointerException e = Assertions.catchThrowableOfType(() -> new FilteredStacktraceExceptionResolver(mockedEventResolverContext, mockedConfig), NullPointerException.class);
        Assertions.assertThat(e).isNull();
    }

    @Test
    void shouldBeAbleToCreateInstance_whenConstructor_givenWhitelistIsNull() {
        Mockito.when(mockedConfig.getList(ConfigProperty.LIST_ALLOW.getKey(), String.class)).thenReturn(null);

        NullPointerException e = Assertions.catchThrowableOfType(() -> new FilteredStacktraceExceptionResolver(mockedEventResolverContext, mockedConfig), NullPointerException.class);
        Assertions.assertThat(e).isNull();
    }

    @Test
    void shouldReturnTrue_whenIsResolve_givenExceptionInLogEvent() {
        LogEvent givenLogEvent = TestLogEvent.builder().thrown(new RuntimeException()).build();

        Assertions.assertThat(filteredStacktraceExceptionResolver.isResolvable(givenLogEvent)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenIsResolve_givenNoExceptionInLogEvent() {
        LogEvent givenLogEvent = TestLogEvent.builder().build();

        Assertions.assertThat(filteredStacktraceExceptionResolver.isResolvable(givenLogEvent)).isFalse();
    }

    @Test
    void shouldResolveToNull_whenResolve_givenNoExceptionInLogEvent() {
        LogEvent givenLogEvent = TestLogEvent.builder().build();

        filteredStacktraceExceptionResolver.resolve(givenLogEvent, jsonWriter);
        String actualOutput = jsonWriter.getStringBuilder().toString();

        Assertions.assertThat(actualOutput).isEqualTo("null");
    }

    @Test
    void shouldResolveToAJsonObject_whenResolve_givenExceptionInLogEvent() {
        LogEvent givenLogEvent = TestLogEvent.builder().thrown(createExceptionWithStacktrace()).build();

        filteredStacktraceExceptionResolver.resolve(givenLogEvent, jsonWriter);
        String actualStringOutput = jsonWriter.getStringBuilder().toString();

        JSONException actualException = Assertions.catchThrowableOfType(() -> new JSONObject(actualStringOutput), JSONException.class);

        Assertions.assertThat(actualException).isNull();
    }

    @Test
    void shouldRemoveSomePackagesFromStacktrace_whenResolve_givenStacktrace() {
        LogEvent givenLogEvent = TestLogEvent.builder().thrown(createExceptionWithStacktrace()).build();

        filteredStacktraceExceptionResolver.resolve(givenLogEvent, jsonWriter);
        String actualStringOutput = jsonWriter.getStringBuilder().toString();

        Assertions.assertThat(actualStringOutput).doesNotContain(PACKAGE_TO_REMOVE_FROM_STACKTRACE.get(0));
    }

    private Exception createExceptionWithStacktrace() {
        try {
            int a = 0 / 0;
        } catch (Exception e) {
            return e;
        }

        // never reached
        return new RuntimeException();
    }
}
