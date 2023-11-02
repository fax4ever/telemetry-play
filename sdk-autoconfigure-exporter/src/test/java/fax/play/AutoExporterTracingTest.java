package fax.play;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AutoExporterTracingTest {

   private OTelConfig oTelConfig;

   @BeforeEach
   public void before() {
      oTelConfig = new OTelConfig();
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
