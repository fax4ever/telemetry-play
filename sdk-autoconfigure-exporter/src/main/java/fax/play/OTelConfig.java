package fax.play;

import java.util.Collections;
import java.util.HashMap;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class OTelConfig {

   private final OpenTelemetry openTelemetry;

   public OTelConfig() {
      HashMap<String, String> properties = new HashMap<>();
      properties.put("otel.traces.exporter", "otlp");
      properties.put("otel.metrics.exporter", "none");
      properties.put("otel.service.name", "fan");
      properties.put("otel.exporter.otlp.endpoint", "http://localhost:4317");

      ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 4317).usePlaintext().build();
      OtlpGrpcSpanExporter otlpGrpcExporter =
            OtlpGrpcSpanExporter.builder()
                  .setChannel(managedChannel)
                  .build();

      openTelemetry = AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(() -> properties)
            .addSpanExporterCustomizer((spanExporter, configProperties) -> otlpGrpcExporter)
            .build()
            .getOpenTelemetrySdk();
   }

   public Tracer getTracer(String instrumentationScopeName, String instrumentationScopeVersion) {
      return openTelemetry.getTracer(instrumentationScopeName, instrumentationScopeVersion);
   }

   public void shutdown() {
      GlobalOpenTelemetry.resetForTest();
   }
}
