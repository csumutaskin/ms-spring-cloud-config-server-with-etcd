package com.noap.msfrw.etcd.util;

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "etcd")
public class EtcdConfigurationProperties {

  private static final String HTTP_PREFIX = "http://";
  private static final String HTTPS_PREFIX = "https://";
  private List<String> urls = new ArrayList<>();
  private List<KeyPrefix> keyPrefixOrder = new ArrayList<>();
  private Boolean httpsEnabled = false;

  public List<String> getUrls() {
    return urls;
  }

  public List<String> getUrlsWithHttpPrefix() {

    String prefix;
    if (Boolean.TRUE.equals(httpsEnabled)) {
      prefix = HTTPS_PREFIX;
    } else {
      prefix = HTTP_PREFIX;
    }
    return urls.stream().map(u -> prefix + u).collect(toList());
  }

  public void setUrls(List<String> urls) {
    this.urls = urls;
  }

  public Boolean getHttpsEnabled() {
    return httpsEnabled;
  }

  public void setHttpsEnabled(Boolean httpsEnabled) {
    this.httpsEnabled = httpsEnabled;
  }

  public List<KeyPrefix> getKeyPrefixOrder() {
    return keyPrefixOrder;
  }

  public void setKeyPrefixOrder(List<KeyPrefix> keyPrefixOrder) {
    this.keyPrefixOrder = keyPrefixOrder;
  }
}
