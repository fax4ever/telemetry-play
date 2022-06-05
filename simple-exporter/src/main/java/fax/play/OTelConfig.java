package fax.play;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class OTelConfig {

   private final SdkTracerProvider tracerProvider;
   private final OpenTelemetry openTelemetry;

   public OTelConfig(SpanExporter spanExporter) {
      // we usually use a batch processor:
//      BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(inMemoryExporter)
//            .setMaxQueueSize(1000)
//            .build();

      SpanProcessor spanProcessor = SimpleSpanProcessor.create(spanExporter);
      tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .build();

      openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();
   }

   public void shutdown() {
      tracerProvider.shutdown();
   }

   public Tracer getTracer(String instrumentationScopeName, String instrumentationScopeVersion) {
      return openTelemetry.getTracer(instrumentationScopeName, instrumentationScopeVersion);
   }
}
