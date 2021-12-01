package com.noap.msfrw.etcd.util;

/**
 * Enumerator for each part of key that may define which application and environment the key belongs to.
 * Think of these parts of the key as prefixes of the actual key.
 * @author UMUT
 *
 */
public enum KeyPrefix {
  APPLICATION("application"), PROFILE("profile"), LABEL("label");

  private String keyToken;

  private KeyPrefix(String keyToken) {
    this.keyToken = keyToken;
  }

  public String getKeyToken() {
    return keyToken;
  }
}
