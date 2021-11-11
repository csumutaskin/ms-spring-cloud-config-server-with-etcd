package com.noap.msfrw.etcd.repository;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.StringUtils;
import com.noap.msfrw.etcd.util.EtcdConnector;

/***
 * A custom EnvironmentRepository for ETCD Cluster as data resource.**
 * 
 * @author Umut
 **/
// @Component
public class EtcdEnvironmentRepository
    implements EnvironmentRepository, ApplicationEventPublisherAware {

  private static Log log = LogFactory.getLog(EtcdEnvironmentRepository.class);

  private String busId;
  private ApplicationEventPublisher applicationEventPublisher;
  private EtcdConnector connector;

  public EtcdEnvironmentRepository(@Value("${spring.cloud.bus.id:application}") String busId) {
    this.busId = busId;

    connector = new EtcdConnector("http://192.168.1.100:2379", "http://192.168.1.100:2381",
        "http://192.168.1.100:2383");
    connector.connect(null, null, null, null);
    log.info("Starting listening to the etcd cluster...");
    connector.startListening(this);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public Environment findOne(String application, String profile, String label) {

    Environment environment = new Environment(application, profile);
    environment.add(new PropertySource("mapPropertySource", connector.getAllKeyValues()));
    return environment;
  }

  // for local project: publishEventByPath("sample") // taken from spring cloud monitor
  @SuppressWarnings("deprecation")
  public Set<String> publishEventByPath(String... registeredClientAppLicationPaths) {

    if (registeredClientAppLicationPaths != null) {
      Set<String> services = new LinkedHashSet<>();
      for (String path : registeredClientAppLicationPaths) {
        services.addAll(guessServiceName(path));
      }
      if (this.applicationEventPublisher != null) {
        for (String service : services) {
          log.info("Refresh for: " + service);
          this.applicationEventPublisher
              .publishEvent(new RefreshRemoteApplicationEvent(this, this.busId, service));
        }
        return services;
      }
    }
    return Collections.emptySet();
  }

  // taken from spring cloud monitor
  private Set<String> guessServiceName(String path) {
    Set<String> services = new LinkedHashSet<>();
    if (path != null) {
      String stem =
          StringUtils.stripFilenameExtension(StringUtils.getFilename(StringUtils.cleanPath(path)));
      // TODO: correlate with service registry
      int index = stem.indexOf("-");
      while (index >= 0) {
        String name = stem.substring(0, index);
        String profile = stem.substring(index + 1);
        if ("application".equals(name)) {
          services.add("*:" + profile);
        } else if (!name.startsWith("application")) {
          services.add(name + ":" + profile);
        }
        index = stem.indexOf("-", index + 1);
      }
      String name = stem;
      if ("application".equals(name)) {
        services.add("*");
      } else if (!name.startsWith("application")) {
        services.add(name);
      }
    }
    return services;
  }
}
