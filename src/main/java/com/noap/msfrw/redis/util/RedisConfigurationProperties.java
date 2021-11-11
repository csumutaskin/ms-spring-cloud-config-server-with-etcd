package com.noap.msfrw.redis.util;

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Property holder POJO for Redis Configuration.
 * 
 * @author umuta
 *
 */
@Component
@ConfigurationProperties(prefix = "redis")
public class RedisConfigurationProperties {

  private static final String REDIS_CONNECTION_PREFIX = "redis://";
  private List<String> urls = new ArrayList<>();
  private Boolean distributedlockEnabled = true; // default
  private Long lockWaitTime = 4L; // default
  private Long lockLeaseTime = 10L; // default

  public List<String> getUrls() {
    return urls;
  }

  public void setUrls(List<String> urls) {
    this.urls = urls;
  }

  public Boolean getDistributedlockEnabled() {
    return distributedlockEnabled;
  }

  public void setDistributedlockEnabled(Boolean distributedlockEnabled) {
    this.distributedlockEnabled = distributedlockEnabled;
  }

  public Long getLockWaitTime() {
    return lockWaitTime;
  }

  public void setLockWaitTime(Long lockWaitTime) {
    this.lockWaitTime = lockWaitTime;
  }

  public Long getLockLeaseTime() {
    return lockLeaseTime;
  }

  public void setLockLeaseTime(Long lockLeaseTime) {
    this.lockLeaseTime = lockLeaseTime;
  }

  public List<String> getUrlsWithRedisPrefix() {
    return urls.stream().map(u -> REDIS_CONNECTION_PREFIX + u).collect(toList());
  }
}
