package com.noap.msfrw.etcd.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.core.Ordered;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

public class EtcdEnvironmentRepository implements EnvironmentRepository, Ordered {

  @Value("${spring.cloud.config.server.etcdrepo.order:0}")
  private int order = Ordered.LOWEST_PRECEDENCE;

  @Override
  public Environment findOne(String application, String profile, String label) {
    Environment environment = new Environment(application, profile);

    Map<String, String> properties = new HashMap();
    try {
      properties = createEtcdMap();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    environment.add(new PropertySource("mapPropertySource", properties));
    return environment;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  public int getOrder() {
    return order;
  }

  private Map<String, String> createEtcdMap() throws InterruptedException, ExecutionException {
    final Map<String, String> properties = new HashMap<String, String>();

    Client client = Client.builder().endpoints("http://127.0.0.1:2379").build();

    ByteSequence key = ByteSequence.from("\0".getBytes());

    GetOption option = GetOption.newBuilder().withSortField(GetOption.SortTarget.KEY)
        .withSortOrder(GetOption.SortOrder.DESCEND).withRange(key).build();

    CompletableFuture<GetResponse> futureResponse = client.getKVClient().get(key, option);

    GetResponse response = futureResponse.get();

    if (response.getKvs().isEmpty()) {
      return null;
    }

    Map<String, String> keyValueMap = new HashMap<>();
    for (KeyValue kv : response.getKvs()) {
      keyValueMap.put(kv.getKey().toString(), kv.getValue().toString());
    }

    return keyValueMap;
  }
}
