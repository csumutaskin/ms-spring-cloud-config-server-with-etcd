package com.noap.msfrw.etcd.util.watch.lock;

/**
 * A runnable like functional interface that runs a method with no input arguments.
 * Main purpose of this functional interface is to allow the distributed lock utility to wrap the functionality of a method.
 * See how an instance of this functional interface is being used in this project.
 *  
 * @author umuta
 *
 */
@FunctionalInterface
public interface InsideLockRunnable {
  void runInsideLock();
}
