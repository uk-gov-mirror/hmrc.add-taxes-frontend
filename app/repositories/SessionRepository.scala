/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package repositories

import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class DatedCacheMap(id: String, data: Map[String, JsValue], lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC))

object DatedCacheMap {
  implicit val dateFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
  implicit val formats: OFormat[DatedCacheMap] = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}

class ReactiveMongoRepository(config: Configuration, mongo: () => DefaultDB)
    extends ReactiveRepository[DatedCacheMap, BSONObjectID](config.get[String]("appName"), mongo, DatedCacheMap.formats) {

  val fieldName = "lastUpdated"
  val createdIndexName = "userAnswersExpiry"
  val expireAfterSeconds = "expireAfterSeconds"
  val timeToLiveInSeconds: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  createIndex(fieldName, createdIndexName, timeToLiveInSeconds)

  private def createIndex(field: String, indexName: String, ttl: Int): Future[Boolean] =
    collection.indexesManager.ensure(Index(
      Seq((field, IndexType.Ascending)),
      Some(indexName),
      options = BSONDocument(expireAfterSeconds -> ttl))) map { result =>
      {
        logger.debug(s"set [$indexName] with value $ttl -> result : $result")
        result
      }
    } recover {
      case e =>
        logger.error("Failed to set TTL index", e)
        false
    }

  def upsert(cm: CacheMap): Future[Boolean] = {
    val selector = BSONDocument("id" -> cm.id)
    val cmDocument = Json.toJson(DatedCacheMap(cm))
    val modifier = BSONDocument("$set" -> cmDocument)

    collection.update(ordered = false).one(selector, modifier, upsert = true).map { lastError =>
      lastError.ok
    }
  }

  def get(id: String): Future[Option[CacheMap]] =
    collection.find(Json.obj("id" -> id), projection = Option.empty[JsObject]).one[CacheMap]
}

@Singleton
class SessionRepository @Inject()(config: Configuration, reactiveMongoComponent: ReactiveMongoComponent) {

  private lazy val sessionRepository = new ReactiveMongoRepository(config, reactiveMongoComponent.mongoConnector.db)

  def apply(): ReactiveMongoRepository = sessionRepository
}
