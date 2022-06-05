package fax.play;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;

public class JaegerGrpcExporterTracingTest {

   private final JaegerGrpcSpanExporter jaegerGrpcExporter =
         JaegerGrpcSpanExporter.builder()
               .setEndpoint("http://localhost:14250")
               .build();

   private OTelConfig oTelConfig;

   @BeforeEach
   public void before() {
      oTelConfig = new OTelConfig(jaegerGrpcExporter);
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
