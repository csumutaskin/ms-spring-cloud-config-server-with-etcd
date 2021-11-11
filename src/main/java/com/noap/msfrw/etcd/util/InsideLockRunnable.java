package com.noap.msfrw.etcd.util;

/**
 * A runnable like functional interface that runs a method with no input arguments.
 * 
 * @author umuta
 *
 */
@FunctionalInterface
public interface InsideLockRunnable {
  void runInsideLock();
}
