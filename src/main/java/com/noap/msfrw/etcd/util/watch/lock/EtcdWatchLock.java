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

// TODO: loglari duzelt
// TODO: endpoint koy locku tutan cluster icin lock hala uzerinde release eden
public class EtcdWatchLock {

  Logger logger = LoggerFactory.getLogger(EtcdWatchLock.class);

  private Config config;
  private RedissonClient redisson;
  private RedisConfigurationProperties redisProperties;

  public RedissonClient getRedisson() {
    return redisson;
  }

  public void setRedisson(RedissonClient redisson) {
    this.redisson = redisson;
  }

  public EtcdWatchLock(RedisConfigurationProperties redisProperties) {
    this.redisProperties = redisProperties;
    List<String> urls = redisProperties.getUrlsWithRedisPrefix();
    config = new Config();
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
