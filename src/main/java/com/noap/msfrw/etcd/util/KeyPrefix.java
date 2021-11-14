package com.noap.msfrw.etcd.util;

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
