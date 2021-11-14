package com.noap.msfrw;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpringCloudConfigServerVerboseTest {

  @Test
  @DisplayName("Verbose Main Test.")
  void mainVerboseTest() {
    SpringCloudConfigServer.main(new String[] {});
  }
}
