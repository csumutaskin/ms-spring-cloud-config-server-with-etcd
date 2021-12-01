package com.noap.msfrw.etcd.util.watch.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.noap.msfrw.redis.util.RedisConfigurationProperties;
import com.noap.msfrw.redis.util.RedisException;

/**
 * This utility is a distributed lock utility that uses REDIS behind. 
 * <p>
 * The main purpose of this lock is to prevent concurrent refresh events for the same key value change in a
 * cluster of spring cloud configuration server instances. Anyone willing to remove the bottleneck of configuration server by
 * creating a set of spring cloud configuration servers in a cluster can enable this distributed lock by using the property
 * "redis.distributedlockEnabled=true" in application properties. A time-managed distributed lock is auto enabled for other 
 * instances in the same spring cloud configuration server cluster to prevent the refresh signal for all the clients connected 
 * to them. After that certain time passes the lock is auto released by this utility (using the redis.lockWaitTime: and 
 * redis.lockLeaseTime: 5 properties -unit is in seconds-)
 * </p>
 * <p>
 * For single instanced spring cloud configuration servers or if it is not a problem to refresh the clients for 
 * the number of cloud configuration servers for the same key value update, you do not need to use this utility. 
 * Disable the utility by setting redis.distributedlockEnabled to false. 
 * </p>
 * @author UMUT
 *
 */
public class EtcdWatchLock {

  Logger logger = LoggerFactory.getLogger(EtcdWatchLock.class);

  private RedissonClient redisson;
  private RedisConfigurationProperties redisProperties;

  public RedissonClient getRedisson() {
    return redisson;
  }

  public void setRedisson(RedissonClient redisson) {
    this.redisson = redisson;
  }
  
  public EtcdWatchLock(RedisConfigurationProperties redisProperties, Config config) {
	    this.redisProperties = redisProperties;
	    List<String> urls = redisProperties.getUrlsWithRedisPrefix();	    
	    if (urls.size() > 1) {
	      config.useClusterServers().addNodeAddress(urls.toArray(String[]::new));
	    } else if (urls.size() == 1) {
	      config.useSingleServer().setAddress(urls.get(0));
	    } else {
	      throw new RedisException(
	          "At least one connection url should exist as redis.url property in properties/yml file");
	    }
	    redisson = Redisson.create(config);
  }

  public EtcdWatchLock(RedisConfigurationProperties redisProperties) {
	 this(redisProperties, new Config());    
  }
  
  /**
   * Wrap a functionality within a distributed lock so that only the first instance retrieving the lock runs that functionality 
   * in a cluster of servers.
   * @param keyName name of the distributed lock. By default it is formed by concatenating ETCD key with its value.
   * @param insideLockRunnable functionality running inside the distributed lock.
   */
  public void processWithLock(String keyName, InsideLockRunnable insideLockRunnable) {
    if (Boolean.TRUE.equals(redisProperties.getDistributedLockEnabled())) {
      logger.info("Redis distributed lock is requested for the key: {}", keyName);
      RLock lock = redisson.getLock(keyName);
      try {
        if (lock.tryLock(redisProperties.getLockWaitTime(), redisProperties.getLockLeaseTime(),
            TimeUnit.SECONDS)) {
          logger.info("Redis distributed lock retrieved for the key: {}", keyName);
          insideLockRunnable.runInsideLock();
        }
      } catch (InterruptedException e) {
        logger.warn("An Interruption within redis lock utiltiy of current thread ", e);
        Thread.currentThread().interrupt();
      }
      logger.info("Redis distributed lock contolled method execution ended");
    } else {
      logger.info(
          "Redis distributed lock is disabled for current node in the cluster, so this server will run the callback method in all circumstances");
      insideLockRunnable.runInsideLock();
    }
  }
}
