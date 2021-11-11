package com.noap.msfrw.etcd.util.watch.lock;

import java.util.concurrent.TimeUnit;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.noap.msfrw.etcd.util.InsideLockRunnable;

// TODO: lock icin keyname + final value dan bir deger olustur
// Lock wait time ve release time parametrik olsun
// Watch lock parametrik olarak disable edilebilmeli butun clusterlar refresh eder bu durumda
// loglari duzelt
// endpoint koy locku tutan cluster icin lock hala uzerinde release eden...
// monitoring icin de rabbitmq dan redise gec.. boylece 3rd party dep. azalir.
public class EtcdWatchLock {

  Logger logger = LoggerFactory.getLogger(EtcdWatchLock.class);

  public RedissonClient connect() {
    Config config = new Config();
    // config.useClusterServers().addNodeAddress("redis://192.168.1.100:6379");
    config.useSingleServer().setAddress("redis://192.168.1.100:6379");
    RedissonClient redisson = Redisson.create(config);
    return redisson;
  }

  public void processWithLock(RedissonClient redisson, String keyName,
      InsideLockRunnable insideLockRunnable) {
    logger.info("8080 Trying lock for key: " + keyName);
    RLock lock = redisson.getLock(keyName); // TODO: "keyName + final value" ile kur lock u
    try {
      if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
        logger.info("8080 Lock retrieved for key: " + keyName);
        insideLockRunnable.runInsideLock();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("8080 Process finished");
  }
}
