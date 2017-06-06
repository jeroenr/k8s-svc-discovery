package com.github.cupenya.service.discovery

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

object Config {
  private val rootConfig = ConfigFactory.load()

  object `service-discovery` {
    private val config = rootConfig.getConfig("service-discovery")

    object kubernetes {
      private val k8sConfig = config.getConfig("kubernetes")
      val host = k8sConfig.getString("host")
      val port = k8sConfig.getInt("port")
      val token = k8sConfig.getString("token")
      val namespaces = k8sConfig.getStringList("namespaces").toList
    }

    object polling {
      private val reconnectConfig = config.getConfig("polling")
      val interval = reconnectConfig.getDuration("interval", TimeUnit.SECONDS)
    }
  }
}
