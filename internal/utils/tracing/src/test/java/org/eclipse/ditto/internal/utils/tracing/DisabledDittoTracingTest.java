/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.internal.utils.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.headers.DittoHeadersSettable;
import org.eclipse.ditto.base.model.headers.WithDittoHeaders;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.internal.utils.tracing.config.DefaultTracingConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.typesafe.config.ConfigFactory;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.RequestContext;
import kamon.Kamon;
import kamon.context.Context;
import kamon.trace.Identifier;
import kamon.trace.Span;
import kamon.trace.Trace;


/**
 * Tests {@link org.eclipse.ditto.internal.utils.tracing.DittoTracing} in disabled state.
 */
public class DisabledDittoTracingTest {

    private static final Identifier SPAN_ID = Kamon.identifierScheme().spanIdFactory().generate();
    private static final Identifier TRACE_ID = Kamon.identifierScheme().traceIdFactory().generate();
    private static final String EXPECTED_TRACEPARENT =
            "00-0000000000000000" + TRACE_ID.string() + "-" + SPAN_ID.string() + "-00";
    private Context context;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DittoTracing.initialize(DefaultTracingConfig.of(ConfigFactory.parseMap(
                Map.of("tracing.enabled", "false"))));
    }

    @Before
    public void setUp() throws Exception {
        final Trace trace = mock(Trace.class);
        final Span span = Mockito.mock(Span.class);
        when(span.trace()).thenReturn(trace);
        when(span.id()).thenReturn(SPAN_ID);
        when(trace.id()).thenReturn(TRACE_ID);

        context = Context.of(Span.Key(), span);
    }

    @Test
    public void propagateContextToCommand() {
        final Command<?> command = mock(Command.class);
        final Command<?> sameCommand = DittoTracing.propagateContext(context, command);
        assertThat(sameCommand).isSameAs(command);
        verifyNoInteractions(command);
    }

    @Test
    public void propagateContextToSignal() {
        final Signal<?> signal = mock(Signal.class);
        final Signal<?> sameSignal = DittoTracing.propagateContext(context, signal);
        assertThat(sameSignal).isSameAs(signal);
        verifyNoInteractions(signal);
    }

    @Test
    public void propagateContextToDittoHeadersSettable() {
        final DittoHeadersSettable<?> dittoHeadersSettable = mock(DittoHeadersSettable.class);
        final DittoHeadersSettable<?> sameSettable = DittoTracing.propagateContext(context, dittoHeadersSettable);
        assertThat(sameSettable).isSameAs(dittoHeadersSettable);
        verifyNoInteractions(dittoHeadersSettable);
    }

    @Test
    public void propagateContextToDittoHeaders() {
        final DittoHeaders empty = DittoHeaders.empty();
        final DittoHeaders dittoHeaders = DittoTracing.propagateContext(context, empty);
        assertThat(dittoHeaders).isSameAs(empty);
    }

    @Test
    public void propagateContextWithCustomSetter() {
        final List<String> emptyList = Collections.emptyList();
        final List<String> result = DittoTracing.propagateContext(context, emptyList, (l, e) -> {
            final List<String> theList = new ArrayList<>(l);
            theList.add(e.getKey() + "=" + e.getValue());
            return theList;
        });
        assertThat(result).isSameAs(emptyList);
    }

    @Test
    public void propagateContextToNewMap() {
        final Map<String, String> map = DittoTracing.propagateContext(context);
        assertThat(map).isEmpty();
    }

    @Test
    public void propagateContextToExistingMap() {
        final Map<String, String> existing = new HashMap<>(Map.of("eclipse", "ditto"));
        final Map<String, String> map = DittoTracing.propagateContext(context, existing);
        assertThat(map).isSameAs(existing);
    }


    @Test
    public void extractTraceContextFromHttpRequest() {
        final HttpRequest httpRequest = mock(HttpRequest.class);
        when(httpRequest.getHeaders()).thenReturn(List.of(HttpHeader.parse("traceparent", EXPECTED_TRACEPARENT)));
        final Context extracted = DittoTracing.extractTraceContext(httpRequest);
        assertThat(extracted.isEmpty()).isTrue();
    }

    @Test
    public void extractTraceContextFromRequestContext() {
        final RequestContext requestContext = mock(RequestContext.class);
        final HttpRequest httpRequest = mock(HttpRequest.class);
        when(httpRequest.getHeaders()).thenReturn(List.of(HttpHeader.parse("traceparent", EXPECTED_TRACEPARENT)));
        when(requestContext.getRequest()).thenReturn(httpRequest);
        final Context extracted = DittoTracing.extractTraceContext(requestContext);
        assertThat(extracted.isEmpty()).isTrue();
    }

    @Test
    public void extractTraceContextFromWithDittoHeaders() {
        final WithDittoHeaders withDittoHeaders = mock(WithDittoHeaders.class);
        when(withDittoHeaders.getDittoHeaders()).thenReturn(
                DittoHeaders.newBuilder().traceparent(EXPECTED_TRACEPARENT).build());
        final Context extracted = DittoTracing.extractTraceContext(withDittoHeaders);
        assertThat(extracted.isEmpty()).isTrue();
    }

    @Test
    public void extractTraceContextFromMap() {
        final Context extracted = DittoTracing.extractTraceContext(Map.of("traceparent", EXPECTED_TRACEPARENT));
        assertThat(extracted.isEmpty()).isTrue();
    }
}
