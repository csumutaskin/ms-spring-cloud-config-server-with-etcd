package com.noap.msfrw.redis.util;

/**
 * Redis Utility exception.
 * 
 * @author UMUT
 *
 */
public class RedisException extends RuntimeException {

  private static final long serialVersionUID = 1087935699174870005L;

  public RedisException(String errorMessage, Throwable thr) {
    super(errorMessage, thr);
  }

  public RedisException(String errorMessage) {
    super(errorMessage);
  }

  public RedisException(Throwable thr) {
    super(thr);
  }
}
