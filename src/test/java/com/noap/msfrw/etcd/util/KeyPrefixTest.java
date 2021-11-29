package com.noap.msfrw.etcd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeyPrefixTest {
	
	@Test
	@DisplayName("Check Enum's GetKeyToken Method")
	void whenGetKeyTokenRequested_thenReturnKeyToken() {
		KeyPrefix key = KeyPrefix.APPLICATION;
		assertEquals("application", key.getKeyToken());
	}
}
