/*
 * Copyright 2018 Julien Ponge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jponge.temperature.gateway

import io.reactivex.rxkotlin.subscribeBy
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.servicediscovery.ServiceDiscovery
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint
import org.slf4j.LoggerFactory

class DevModeDiscoveryVerticle : AbstractVerticle() {

  private val logger = LoggerFactory.getLogger(DevModeDiscoveryVerticle::class.java)

  override fun start() {
    logger.info("Service discovery for development purposes, manually injecting references")
    val discovery = ServiceDiscovery.create(vertx)

    val host = "localhost"
    val path = "/api/temperature"
    val meta = json {
      obj("app" to "temperature-service")
    }

    val s1 = HttpEndpoint.createRecord("temp1", host, 6000, path, meta)
    val s2 = HttpEndpoint.createRecord("temp2", host, 6001, path, meta)
    val s3 = HttpEndpoint.createRecord("temp3", host, 6002, path, meta)

    discovery
      .rxPublish(s1)
      .flatMap { discovery.rxPublish(s2) }
      .flatMap { discovery.rxPublish(s3) }
      .subscribeBy(
        onSuccess = {
          logger.info("All 3 local services have been published")
        },
        onError = {
          logger.error("Error while publishing a service", it)
        })
  }
}
