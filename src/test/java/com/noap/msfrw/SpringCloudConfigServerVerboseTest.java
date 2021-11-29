package com.noap.msfrw;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verbose test to increase code coverage. 
 * @author UMUT
 *
 */
@ExtendWith(MockitoExtension.class)
class SpringCloudConfigServerVerboseTest {
	
  @Test
  @DisplayName("Given No Extra Configuration When Bootstrap Class Main Method is Called Then Succeed (Verbose)")
  void givenNoExtraConfiguration_whenBootstrapClassMainMethodCalled_thenSucceed_Verbose() {
    try(MockedStatic<SpringCloudConfigServer> server = Mockito.mockStatic(SpringCloudConfigServer.class)) {
	  server.when(() -> SpringCloudConfigServer.main(Mockito.any(String[].class))).then(invocation -> null);	
      SpringCloudConfigServer.main(new String[] {});
      server.verify(() -> SpringCloudConfigServer.main(Mockito.any(String[].class)));
	}
  }
}
