package org.glpste.logging.log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.layout.template.json.util.QueueingRecyclerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FilteredStacktraceStackTraceJsonResolverCustomFieldsUnitTest {

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

        // this ensures that the internal mechanism of adding packages is working
        Mockito.when(mockedConfig.getList(ConfigProperty.LIST_FILTER.getKey(), String.class)).thenReturn(Collections.singletonList("org.glpste.logging.log4j2"));
        Mockito.when(mockedConfig.getList(ConfigProperty.LIST_ALLOW.getKey(), String.class)).thenReturn(new ArrayList<>());

        jsonWriter = JsonWriter.newBuilder().setMaxStringLength(60000).setTruncatedStringSuffix("...").build();

        filteredStacktraceExceptionResolver = new FilteredStacktraceExceptionResolver(mockedEventResolverContext, mockedConfig);
    }
}
