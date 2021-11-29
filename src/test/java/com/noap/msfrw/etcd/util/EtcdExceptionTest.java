package com.noap.msfrw.etcd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EtcdExceptionTest {
	
	@Test
	@DisplayName("Thrown Exception Message contains a particular String")
	void whenEtcdExceptionInitialized_thenThrownExceptionHasSameTypeAndMessage_VerboseTest() {
		String exceptionMessage = "sampleMessage";
		
		EtcdException ee = new EtcdException(exceptionMessage);
		Exception exception = assertThrows(EtcdException.class,	() -> { throw ee; });
		assertEquals(exception.getMessage(), exceptionMessage);		
	}
	
	@Test
	@DisplayName("Thrown Exception Message Contains the Identified Root Cause")
	void whenEtcdExceptionInitialized_thenThrownExceptionHasSameRootCause_VerboseTest() {
		Throwable identifiedRunTime = new RuntimeException();		
		EtcdException ee = new EtcdException(identifiedRunTime);
		Exception exception = assertThrows(EtcdException.class,	() -> { throw ee; });
		assertEquals(exception.getCause(), identifiedRunTime);		
	}
}
