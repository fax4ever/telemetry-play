package fax.play;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class ManualSdkTracingProvider implements AutoCloseable {

   private final OtlpHttpSpanExporter spanExporter;
   private final OpenTelemetrySdk openTelemetrySdk;

   public ManualSdkTracingProvider(String endpoint) {
      GlobalOpenTelemetry.resetForTest();
      Resource resource = Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "http-otlp-manual"));

      spanExporter = OtlpHttpSpanExporter.builder()
            .setEndpoint(endpoint)
            .build();

      SpanProcessor spanProcessor = SimpleSpanProcessor.create(spanExporter);

      SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setResource(Resource.getDefault().merge(resource))
            .build();

      openTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();
   }

   public Tracer provide() {
      return openTelemetrySdk.getTracer("fax.play.tracing", "1.0.0");
   }

   @Override
   public void close() {
      try {
         if (openTelemetrySdk != null) {
            openTelemetrySdk.close();
         }
      } finally {
         if (spanExporter != null) {
            spanExporter.close();
         }
      }
      GlobalOpenTelemetry.resetForTest();
   }
}
