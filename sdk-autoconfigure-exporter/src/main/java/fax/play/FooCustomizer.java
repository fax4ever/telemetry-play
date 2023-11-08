package fax.play;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

public class FooCustomizer implements AutoConfigurationCustomizerProvider {

   private final ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 4317).usePlaintext().build();

   @Override
   public void customize(AutoConfigurationCustomizer autoConfiguration) {
      autoConfiguration.addSpanExporterCustomizer(
            (spanExporter, configProperties) -> {
               if (spanExporter instanceof OtlpGrpcSpanExporter) {
                  spanExporter.shutdown().join(10, TimeUnit.SECONDS);
                  return ((OtlpGrpcSpanExporter) spanExporter).toBuilder().setChannel(managedChannel).build();
               }

               return spanExporter;
            });
   }

}
