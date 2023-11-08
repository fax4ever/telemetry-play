package fax.play;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.resources.Resource;

public class OtlpHttpExporterTracingTest {
   private OTelConfig oTelConfig;

   @BeforeEach
   public void before() {
      OtlpHttpSpanExporter exporter = OtlpHttpSpanExporter.builder()
            .setEndpoint("http://localhost:4318/v1/traces")
            .build();

      Resource resource = Resource.create(Attributes.of(
            AttributeKey.stringKey("service.name"), "simple-exporter-service"));

      oTelConfig = new OTelConfig(exporter, resource);
   }

   @AfterEach
   public void afterEach() {
      oTelConfig.shutdown();
   }

   @Test
   public void test() throws Exception {
      new OTelPlay(oTelConfig).play();
   }
}
