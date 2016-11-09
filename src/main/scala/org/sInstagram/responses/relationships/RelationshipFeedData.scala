package org.sInstagram.responses.relationships

/**
	* author Yuki Hirai on 2016/11/09.
	*/
case class RelationshipFeedData(
  incomingStatus: Option[String],
  outgoingStatus: Option[String],
  targetUserPrivate: Option[Boolean]
)

import com.github.tototoshi.play.json.JsonNaming
import play.api.libs.json.Json

object RelationshipFeedData {
	implicit val RelationshipFeedDataFormat = JsonNaming.snakecase(Json.format[RelationshipFeedData])
}