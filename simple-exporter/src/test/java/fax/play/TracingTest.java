package fax.play;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class TracingTest {

   private final InMemorySpanExporter inMemoryExporter = InMemorySpanExporter.create();

   private SdkTracerProvider tracerProvider;
   private OpenTelemetry openTelemetry;

   @BeforeEach
   public void before() {
      // we usually use a batch processor:
//      BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(inMemoryExporter)
//            .setMaxQueueSize(1000)
//            .build();

      SpanProcessor spanProcessor = SimpleSpanProcessor.create(inMemoryExporter);
      tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .build();

      openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();
   }

   @AfterEach
   public void afterEach() {
      tracerProvider.shutdown();
   }

   @Test
   public void test() throws Exception {
      Tracer tracer = openTelemetry.getTracer("fax.play.tracing", "1.0.0");
      assertThat(tracer).isNotNull();

      Span span = tracer.spanBuilder("parent-span").startSpan();
      try (Scope scope = span.makeCurrent()) {
         Thread.sleep(300);
         span.addEvent("init");
         execute(tracer);
         span.addEvent("end");
         Thread.sleep(300);
         span.setStatus(StatusCode.OK);
      } catch (Throwable throwable) {
         span.setStatus(StatusCode.ERROR, "Error during execution of my span operation!");
         span.recordException(throwable);
         throw throwable;
      } finally {
         span.end();
      }

      List<SpanData> spanData = inMemoryExporter.getFinishedSpanItems();
      assertThat(spanData).hasSize(3);

      checkChild1SpanData(spanData.get(0));
      checkChild2SpanData(spanData.get(1));
      checkParentSpanData(spanData.get(2));
   }

   private void execute(Tracer tracer) throws InterruptedException {
      Random random = new Random();

      Span childSpan1 = tracer.spanBuilder("child-1").startSpan();
      try(Scope scope = childSpan1.makeCurrent()) {
         childSpan1.setAttribute("foo", random.nextInt());
         Thread.sleep(500);
         childSpan1.setStatus(StatusCode.OK);
      } finally {
         childSpan1.end();
      }

      Span childSpan2 = tracer.spanBuilder("child-2").startSpan();
      try(Scope scope = childSpan2.makeCurrent()) {
         childSpan2.setAttribute("bar", random.nextInt());
         Thread.sleep(500);
         childSpan2.setStatus(StatusCode.OK);
      } finally {
         childSpan2.end();
      }
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
