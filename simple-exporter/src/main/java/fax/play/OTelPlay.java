package fax.play;

import java.util.Random;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class OTelPlay {

   private final OTelConfig oTelConfig;

   public OTelPlay(OTelConfig oTelConfig) {
      this.oTelConfig = oTelConfig;
   }

   public void play() throws Exception {
      Tracer tracer = oTelConfig.getTracer("fax.play.tracing", "1.0.0");

      Span span = tracer.spanBuilder("parent-span").setSpanKind(SpanKind.CLIENT).startSpan();
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
}
