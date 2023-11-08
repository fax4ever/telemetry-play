package fax.play;

import java.util.Random;

import org.junit.jupiter.api.Test;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class SmokeTest {

   private final Random random = new Random(739);

   @Test
   public void manual() {
      try (ManualSdkTracingProvider tracingProvider = new ManualSdkTracingProvider("http://localhost:4318/v1/traces")) {
         parentSpan(tracingProvider.provide());
      }
   }

   @Test
   public void auto() {
      try (AutoSdkTracingProvider tracingProvider = new AutoSdkTracingProvider("http://localhost:4318")) {
         parentSpan(tracingProvider.provide());
      }
   }

   private void parentSpan(Tracer tracer) {
      Span span = tracer.spanBuilder("parent-span").setSpanKind(SpanKind.CLIENT).startSpan();
      try (Scope scope = span.makeCurrent()) {
         Thread.sleep(300);
         span.addEvent("init");
         childrenSpan(tracer);
         span.addEvent("end");
         Thread.sleep(300);
         span.setStatus(StatusCode.OK);
      } catch (Throwable throwable) {
         span.setStatus(StatusCode.ERROR, "Error during execution of my span operation!");
         span.recordException(throwable);
      } finally {
         span.end();
      }
   }

   private void childrenSpan(Tracer tracer) throws InterruptedException {
      Span childSpan1 = tracer.spanBuilder("child-1").startSpan();
      try (Scope scope = childSpan1.makeCurrent()) {
         childSpan1.setAttribute("foo", random.nextInt());
         Thread.sleep(500);
         childSpan1.setStatus(StatusCode.OK);
      } finally {
         childSpan1.end();
      }

      Span childSpan2 = tracer.spanBuilder("child-2").startSpan();
      try (Scope scope = childSpan2.makeCurrent()) {
         childSpan2.setAttribute("bar", random.nextInt());
         Thread.sleep(500);
         childSpan2.setStatus(StatusCode.OK);
      } finally {
         childSpan2.end();
      }
   }
}
