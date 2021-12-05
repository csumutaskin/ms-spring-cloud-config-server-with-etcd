package com.noap.msfrw.etcd.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.noap.msfrw.etcd.util.EtcdConnector;

@Controller
@RequestMapping("/etcd")
public class Etcd3Controller {

  private static final Logger logger = LoggerFactory.getLogger(Etcd3Controller.class);
  private EtcdConnector etcdConnector;

  public Etcd3Controller(EtcdConnector etcdConnector) {
    this.etcdConnector = etcdConnector;
  }

  /**
   * This end point is added because 3rd party ETCD browser application is throwing an error when
   * adding a new key to the store. Use this end point for the demo.
   * 
   * @param key key to add.
   * @param value value to add.
   * @return true if key value is added with success.
   */
  @ResponseBody
  @GetMapping(path = "/add/{key}/{value}")
  public Boolean addKeyValue(@PathVariable String key, @PathVariable String value) {
    logger.info("A key: {} value: {} pair is being added to ETCD store", key, value);
    return etcdConnector.addKeyValue(key, value);
  }
}
