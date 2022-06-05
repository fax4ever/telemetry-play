package fax.play;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class JaegerGrpcExporterTracingTest {

   private final JaegerGrpcSpanExporter jaegerGrpcExporter =
         JaegerGrpcSpanExporter.builder()
               .setEndpoint("http://localhost:14250")
               .build();

   private OTelConfig oTelConfig;

   @BeforeEach
   public void before() {
      Resource resource = Resource.create(Attributes.of(
            ResourceAttributes.SERVICE_NAME, "simple-exporter-service"));

      oTelConfig = new OTelConfig(jaegerGrpcExporter, resource);
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
