package fax.play;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class AutoSdkTracingProvider implements AutoCloseable {

   private final OpenTelemetrySdk openTelemetrySdk;

   public AutoSdkTracingProvider(String endpoint) {
      GlobalOpenTelemetry.resetForTest();

      HashMap<String, String> properties = new HashMap<>();
      properties.put("otel.exporter.otlp.protocol", "http/protobuf");
      properties.put("otel.traces.exporter", "otlp");
      properties.put("otel.metrics.exporter", "none");
      properties.put("otel.service.name", "http-otlp-auto");
      properties.put("otel.exporter.otlp.endpoint", endpoint);

      openTelemetrySdk = AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(() -> properties)
            .build()
            .getOpenTelemetrySdk();
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
         GlobalOpenTelemetry.resetForTest();
      }
   }

}
