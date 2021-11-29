package com.noap.msfrw.etcd.repository;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bus.event.PathDestinationFactory;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import com.noap.msfrw.etcd.util.EtcdConnector;

/**
 * A custom EnvironmentRepository for that uses an ETCD Cluster as the data resource.
 * 
 * @author Umut
 **/
public class EtcdEnvironmentRepository
    implements EnvironmentRepository, ApplicationEventPublisherAware {

  private static final Logger logger = LoggerFactory.getLogger(EtcdEnvironmentRepository.class);
  private String busId;
  private ApplicationEventPublisher applicationEventPublisher;
  private EtcdConnector connector;

  public EtcdEnvironmentRepository(EtcdConnector connector, String busId) {
    this.busId = busId;
    this.connector = connector;
    connector.connect(null, null, null, null);
    logger.info("Starting listening to the ETCD cluster...");
    connector.startListening(this);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }
  
  public ApplicationEventPublisher getApplicationEventPublisher() {
	  return applicationEventPublisher;
  }

  @Override
  public Environment findOne(String application, String profile, String label) {
    Environment environment = new Environment(application, profile);
    environment.add(new PropertySource("mapPropertySource",
        connector.getAllKeyValues(application, profile, label)));
    return environment;
  }

  // Below code taken from spring cloud monitor project...
  public void publishEventByPath(String... registeredClientAppLicationPaths) {
    if (registeredClientAppLicationPaths != null && this.applicationEventPublisher != null) {
      Set<String> services = new LinkedHashSet<>();
      services.addAll(Arrays.asList(registeredClientAppLicationPaths));
      for (String service : services) {
        logger.info("Refresh will be occurred for: {}", service);
        this.applicationEventPublisher.publishEvent(new RefreshRemoteApplicationEvent(this,
            this.busId, new PathDestinationFactory().getDestination(service)));
      }
    }
  }
}
