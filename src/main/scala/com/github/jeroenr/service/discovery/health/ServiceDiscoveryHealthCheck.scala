package com.github.jeroenr.service.discovery.health

import akka.actor.ActorRef
import akka.http.scaladsl.model.DateTime
import akka.pattern._
import akka.util.Timeout
import com.github.jeroenr.service.discovery.Logging
import com.github.jeroenr.service.discovery.ServiceDiscoveryAgent

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

class ServiceDiscoveryHealthCheck(serviceDiscoveryAgent: ActorRef) extends HealthCheck with Logging {

  import ServiceDiscoveryHealthCheck._

  implicit val timeout: Timeout = 10 seconds

  override def name: String = "ServiceDiscoveryService"

  override def runCheck()(implicit ec: ExecutionContext): Future[HealthCheckResult] = {
    (serviceDiscoveryAgent ? ServiceDiscoveryAgent.HealthCheck).mapTo[ServiceDiscoveryStatus].map {
      case OK => healthCheckResult(HealthCheckStatus.Ok)
      case NOK => healthCheckResult(HealthCheckStatus.Critical)
    }
  }

  private def healthCheckResult(healthCheckStatus: HealthCheckStatus) =
    HealthCheckResult(name, healthCheckStatus, DateTime.now.clicks)
}

object ServiceDiscoveryHealthCheck {

  sealed trait ServiceDiscoveryStatus

  case object OK extends ServiceDiscoveryStatus

  case object NOK extends ServiceDiscoveryStatus

}
