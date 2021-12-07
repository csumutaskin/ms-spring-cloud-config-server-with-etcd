package com.noap.msfrw.etcd.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.noap.msfrw.etcd.util.EtcdConnector;

/**
 * A controller that controls end points to alter the ETCD cluster this spring cloud config server is connecting to.
 * This controller actually is not the main aim of this project. But the 3rd party tool that is used for altering ETCD
 * is lacking the capability to add new keys to the ETCD store. This is the reason that this class is added here.
 * It can be omitted to focus on the real aim of the project.
 * 
 * @author Umut
 **/
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
