package fax.play;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;

public class OtlpGrpcExporterTracingTest {
   private OTelConfig oTelConfig;

   @BeforeEach
   public void before() {
      ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 4317).usePlaintext().build();

      OtlpGrpcSpanExporter otlpGrpcExporter =
            OtlpGrpcSpanExporter.builder()
                  .setChannel(managedChannel)
                  .build();

      Resource resource = Resource.create(Attributes.of(
            AttributeKey.stringKey("service.name"), "simple-exporter-service"));

      oTelConfig = new OTelConfig(otlpGrpcExporter, resource);
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
