package com.noap.msfrw.redis.util;

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Property holder POJO for Redis Configuration.
 * <p>
 * Property Declerations:
 * <ul>
 * <li><b>lockWaitTime:</b> time in Seconds, that is needed for the distributed lock to wait for the
 * lock, if it is owned by another thread</li>
 * <li><b>lockLeasetTime:</b> time in Seconds, that is needed for the distributed lock to release
 * the lock automatically, if it is owned by that thread</li>
 * <li><b>distributedlockEnabled:</b> if true, distributed lock utility is in action so that if
 * there are more than one nodes of this server instance in a cluster, they can race for a solution,
 * the losing threads may skip to repeat the same action, currently being used by the <b><i>ETCD
 * watcher utility's callback action when a change occurs in the ETCD property store itself</i></b>,
 * only one thread is publishing event to refresh the configuration clients to refresh their
 * scope</li>
 * </ul>
 * </p>
 * <p>
 * The main distributed lock usage in this server is that: ETCD watcher is always listening the ETCD
 * servers to check if a property change has occurred or not, in case of any property key value pair
 * update, the watcher triggers a callback function that publishes the same "property changed event"
 * just like the monitor end point of this spring cloud config server does. If there are more than
 * one instance of server running in a cluster (to get rid of the single point of failure problem)
 * this means each server instance has its own watcher and each watcher will be triggered by the
 * same key value change in the ETCD server. This auto-releasing-itself distributed lock mechanism
 * works by blocking the losing threads to wait for a less amount of the then, the winning thread's
 * lock release time just to distract losing threads doing the same update over again and again.
 * Winning thread keeps the lock for a certain amount of time (regulate it from properties file in
 * redis.lockLeaseTime) and then releases it automatically so that other losing threads have already
 * given up doing the same update.
 * </p>
 * <p>
 * Please regulate your leaseTime and WaitTime so that both of them are not very long durations and
 * lease Time is slightly more than the wait time so that losing threads give up already to do the
 * same update when the winning releases the lock. wait:1 to lease:3 seconds or 2 to 4 seconds may
 * be a good starting point. And it does not mean the end of all, if one of the losing threads gets
 * the same lock for the same operation, it means for that ETCD update cycle 2 same refreshment will
 * occur in the clients.
 * </p>
 * 
 * @author umuta
 *
 */
@Component
@ConfigurationProperties(prefix = "redis")
public class RedisConfigurationProperties {

  private static final String REDIS_CONNECTION_PREFIX = "redis://";
  private List<String> urls = new ArrayList<>();
  private Boolean distributedLockEnabled = false; // default
  private Long lockWaitTime = 4L; // default
  private Long lockLeaseTime = 10L; // default

  public List<String> getUrls() {
    return urls;
  }

  public void setUrls(List<String> urls) {
    this.urls = urls;
  }

  public Boolean getDistributedLockEnabled() {
    return distributedLockEnabled;
  }

  public void setDistributedLockEnabled(Boolean distributedLockEnabled) {
    this.distributedLockEnabled = distributedLockEnabled;
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
