package fax.play;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;

public class JaegerGrpcExporterTracingTest {

   private OTelConfig oTelConfig;

   @BeforeEach
   public void before() {
      ManagedChannel jaegerChannel = ManagedChannelBuilder.forAddress("localhost", 14250).usePlaintext().build();
      JaegerGrpcSpanExporter jaegerGrpcExporter =
            JaegerGrpcSpanExporter.builder()
                  .setChannel(jaegerChannel)
                  .build();

      Resource resource = Resource.create(Attributes.of(
            AttributeKey.stringKey("service.name"), "simple-exporter-service"));

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
