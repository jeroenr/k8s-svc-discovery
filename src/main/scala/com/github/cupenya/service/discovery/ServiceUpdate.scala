package com.github.cupenya.service.discovery

import scala.concurrent.Future
import spray.json._

sealed trait PermissionModel

case class Permission(
  id: String,
  name: String,
  description: Option[String]
) extends PermissionModel

object PermissionModel extends DefaultJsonProtocol {
  implicit val PermissionFormat = jsonFormat3(Permission)
}

sealed trait UpdateType

object UpdateType {

  case object Addition extends UpdateType

  case object Mutation extends UpdateType

  case object Deletion extends UpdateType

}

trait DiscoverableAddress {
  def address: String
}

trait ServiceUpdate extends DiscoverableAddress {
  def updateType: UpdateType
  def resource: String
  def secured: Boolean
  def port: Int
  def namespace: String
  def permissions: List[Permission]
}

trait ServiceDiscoverySource[T <: ServiceUpdate] extends Logging {
  def name: String
  def healthCheck: Future[_]
  def listServices: Future[List[T]]
}
