package fax.play;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class OTelConfig {

   private final OpenTelemetry openTelemetry;

   public OTelConfig() {
      openTelemetry = AutoConfiguredOpenTelemetrySdk.builder()
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
