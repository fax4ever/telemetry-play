package fax.play;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;

public class InMemoryExporterTracingTest {

   private final InMemorySpanExporter inMemoryExporter = InMemorySpanExporter.create();
   private OTelConfig oTelConfig;

   @BeforeEach
   public void before() {
      oTelConfig = new OTelConfig(inMemoryExporter);
   }

   @AfterEach
   public void afterEach() {
      oTelConfig.shutdown();
   }

   @Test
   public void test() throws Exception {
      new OTelPlay(oTelConfig).play();

      List<SpanData> spanData = inMemoryExporter.getFinishedSpanItems();
      assertThat(spanData).hasSize(3);

      checkChild1SpanData(spanData.get(0));
      checkChild2SpanData(spanData.get(1));
      checkParentSpanData(spanData.get(2));
   }

   private void checkChild1SpanData(SpanData spanData) {
      assertThat(spanData.getName()).isEqualTo("child-1");
      assertThat(spanData.getAttributes().get(AttributeKey.longKey("foo"))).isNotNull();
      assertThat(spanData.getStatus()).isEqualTo(StatusData.ok());

      long startEpochNanos = spanData.getStartEpochNanos();
      long endEpochNanos = spanData.getEndEpochNanos();
      assertThat(endEpochNanos).isGreaterThan(startEpochNanos);

      long durationNanos = endEpochNanos - startEpochNanos;
      long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
      System.out.println("Child span 1 duration: " + durationMillis);
   }

   private void checkChild2SpanData(SpanData spanData) {
      assertThat(spanData.getName()).isEqualTo("child-2");
      assertThat(spanData.getAttributes().get(AttributeKey.longKey("bar"))).isNotNull();
      assertThat(spanData.getStatus()).isEqualTo(StatusData.ok());

      long startEpochNanos = spanData.getStartEpochNanos();
      long endEpochNanos = spanData.getEndEpochNanos();
      assertThat(endEpochNanos).isGreaterThan(startEpochNanos);

      long durationNanos = endEpochNanos - startEpochNanos;
      long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
      System.out.println("Child span 2 duration: " + durationMillis);
   }

   private void checkParentSpanData(SpanData spanData) {
      assertThat(spanData.getName()).isEqualTo("parent-span");
      assertThat(spanData.getStatus()).isEqualTo(StatusData.ok());

      long startEpochNanos = spanData.getStartEpochNanos();
      long endEpochNanos = spanData.getEndEpochNanos();
      assertThat(endEpochNanos).isGreaterThan(startEpochNanos);

      long durationNanos = endEpochNanos - startEpochNanos;
      long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
      System.out.println("Parent span duration: " + durationMillis);

      List<EventData> events = spanData.getEvents();
      assertThat(events.size()).isEqualTo(2);

      EventData init = events.get(0);
      assertThat(init.getName()).isEqualTo("init");
      long initEpochNanos = init.getEpochNanos();

      EventData end = events.get(1);
      assertThat(end.getName()).isEqualTo("end");
      long endEventEpochNanos = end.getEpochNanos();

      assertThat(endEventEpochNanos).isGreaterThan(initEpochNanos);

      durationNanos = endEventEpochNanos - initEpochNanos;
      durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
      System.out.println("Between init/end events duration: " + durationMillis);
   }
}
