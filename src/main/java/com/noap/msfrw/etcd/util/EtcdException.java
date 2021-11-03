package com.noap.msfrw.etcd.util;
/**
 * ETCD Utility exception.
 * 
 * @author UMUT
 *
 */
public class EtcdException extends RuntimeException {

	private static final long serialVersionUID = 4931351520040875374L;

	public EtcdException(String errorMessage, Throwable thr) {
	        super(errorMessage, thr);
	}
	
	public EtcdException(String errorMessage) {
		super(errorMessage);
	}
	
	public EtcdException(Throwable thr) {
		super(thr);
	}
}
