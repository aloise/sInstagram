package com.yukihirai0505.sInstagram

import com.yukihirai0505.sInstagram.exceptions.OAuthException
import com.yukihirai0505.sInstagram.model.Relationship
import com.yukihirai0505.sInstagram.responses.auth.AccessToken
import com.yukihirai0505.sInstagram.responses.comments.MediaCommentsFeed
import com.yukihirai0505.sInstagram.responses.common.{NoDataResponse, Pagination}
import com.yukihirai0505.sInstagram.responses.likes.LikesFeed
import com.yukihirai0505.sInstagram.responses.locations.{LocationInfo, LocationSearchFeed}
import com.yukihirai0505.sInstagram.responses.media.{MediaFeed, MediaInfoFeed}
import com.yukihirai0505.sInstagram.responses.relationships.RelationshipFeed
import com.yukihirai0505.sInstagram.responses.tags.{TagInfoFeed, TagSearchFeed}
import com.yukihirai0505.sInstagram.responses.users.basicinfo.UserInfo
import com.yukihirai0505.sInstagram.responses.users.feed.UserFeed
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util.Try

/**
  * author Yuki Hirai on 2017/04/14.
  */
class InstagramSpec extends FlatSpec with Matchers {

  private def anInstanceOf[T](implicit tag: reflect.ClassTag[T]) = {
    val clazz = tag.runtimeClass.asInstanceOf[Class[T]]
    new BePropertyMatcher[AnyRef] {
      def apply(left: AnyRef) =
        BePropertyMatchResult(left.getClass.isAssignableFrom(clazz), "an instance of " + clazz.getName)
    }
  }

  private def readAccessTokenFromConfig(): AccessToken = {
    sys.env.get("INSTAGRAM_TEST_ACCESS_TOKEN") match {
      case Some(v) => AccessToken(v)
      case _ =>
        Try {
          val tokenFile = Source.fromURL(getClass.getResource("/token.txt")).mkString
          AccessToken(tokenFile.split("=").toList(1))
        }.getOrElse(throw new Exception(
          "Please provide a valid access_token by making a token.txt in resources.\n" +
            "See token.txt.default for detail."
        ))
    }
  }

  val auth: AccessToken = readAccessTokenFromConfig()
  val instagram = new Instagram(auth)
  val wrongToken = AccessToken("this is a bullshit access token")
  val nonePage = new Pagination(None, None, None, None, None, None)

  var userId: Option[String] = None
  var mediaId: Option[String] = None
  var locationId: Option[String] = None
  var latitude: Option[Double] = None
  var longitude: Option[Double] = None
  var mediaCommentId: Option[String] = None

  "getCurrentUserInfo" should "return a Some[UserInfo]" in {
    val request = Await.result(instagram.getCurrentUserInfo, 10 seconds)
    userId = request.flatMap(_.data.id)
    request should be(anInstanceOf[Some[UserInfo]])
    request.get.data.id.get should be(userId.getOrElse(""))
  }


  "A failed request" should "return a failed promise" in {
    an[Exception] should be thrownBy Await.result(new Instagram(wrongToken).getUserInfo(userId.getOrElse("")), 10 seconds)
  }

  "getRecentMediaFeed" should "return a Some[MediaFeed]" in {
    val request = Await.result(instagram.getRecentMediaFeed(), 10 seconds)
    mediaId = request.flatMap(_.data.lastOption.flatMap(_.id))
    locationId = request.flatMap(_.data.lastOption.flatMap(_.location.flatMap(x => Some(x.id.toString()))))
    request should be(anInstanceOf[Some[MediaFeed]])
  }

  "getUserFollowList" should "return a Some[UserFeed]" in {
    val request = Await.result(instagram.getUserFollowList(userId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[UserFeed]])
  }

  "getUserFollowListNextPage" should "return a Some[UserFeed]" in {
    val request = Await.result(instagram.getUserFollowListNextPage(userId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[UserFeed]])
  }

  "getUserFollowListNextPageByPage" should "return a OAuthException by None pagination" in {
    an[OAuthException] should be thrownBy Await.result(instagram.getUserFollowListNextPageByPage(nonePage), 10 seconds)
  }

  "getUserLikedMediaFeed" should "return a Some[MediaFeed]" in {
    val request = Await.result(instagram.getUserLikedMediaFeed(), 10 seconds)
    request should be(anInstanceOf[Some[MediaFeed]])
  }

  "searchUser" should "return a Some[UserFeed]" in {
    val request = Await.result(instagram.searchUser("i_do_not_like_holidays"), 10 seconds)
    request should be(anInstanceOf[Some[UserFeed]])
  }

  "getLocationInfo" should "return a Some[LocationInfo]" in {
    val request = Await.result(instagram.getLocationInfo(locationId.getOrElse("")), 10 seconds)
    latitude = request.flatMap(_.data.latitude)
    longitude = request.flatMap(_.data.longitude)
    request should be(anInstanceOf[Some[LocationInfo]])
  }

  "getTagInfo" should "return a Some[TagInfoFeed]" in {
    val request = Await.result(instagram.getTagInfo("test"), 10 seconds)
    request should be(anInstanceOf[Some[TagInfoFeed]])
  }

  "searchLocation" should "return a Some[LocationSearchFeed]" in {
    val request = Await.result(instagram.searchLocation(latitude.getOrElse(0), longitude.getOrElse(0)), 10 seconds)
    request should be(anInstanceOf[Some[LocationSearchFeed]])
  }

  "getRecentMediaByLocation" should "return a Some[MediaFeed]" in {
    val request = Await.result(instagram.getRecentMediaByLocation(locationId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[MediaFeed]])
  }

  "setUserLike" should "return a Some[LikesFeed]" in {
    val request = Await.result(instagram.setUserLike(mediaId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[LikesFeed]])
  }

  "getMediaInfo" should "return a Some[MediaInfoFeed]" in {
    val request = Await.result(instagram.getMediaInfo(mediaId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[MediaInfoFeed]])
  }

  "getRecentMediaNextPage" should "return a OAuthException by None pagination" in {
    an[OAuthException] should be thrownBy Await.result(instagram.getRecentMediaNextPage(nonePage), 10 seconds)
  }

  "getUserFeedInfoNextPage" should "return a OAuthException by None pagination" in {
    an[OAuthException] should be thrownBy Await.result(instagram.getUserFeedInfoNextPage(nonePage), 10 seconds)
  }

  "deleteUserLike" should "return a Some[NoDataResponse]" in {
    val request = Await.result(instagram.deleteUserLike(mediaId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[NoDataResponse]])
  }

  "setMediaComments" should "return a Some[NoDataResponse]" in {
    val request = Await.result(instagram.setMediaComments(mediaId.getOrElse(""), "test"), 10 seconds)
    request should be(anInstanceOf[Some[NoDataResponse]])
  }

  "getMediaComments" should "return a Some[MediaCommentsFeed]" in {
    val request = Await.result(instagram.getMediaComments(mediaId.getOrElse("")), 10 seconds)
    mediaCommentId = request.flatMap(_.data.lastOption.flatMap(_.id))
    request should be(anInstanceOf[Some[MediaCommentsFeed]])
  }

  "deleteMediaCommentById" should "return a Some[NoDataResponse]" in {
    val request = Await.result(instagram.deleteMediaCommentById(mediaId.getOrElse(""), mediaCommentId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[NoDataResponse]])
  }

  "getUserFollowedByList" should "return a Some[UserFeed]" in {
    val request = Await.result(instagram.getUserFollowedByList(userId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[UserFeed]])
  }

  "getUserFollowedByListNextPage" should "return a OAuthException by None pagination" in {
    an[OAuthException] should be thrownBy Await.result(instagram.getUserFollowedByListNextPage(nonePage), 10 seconds)
  }

  "getMediaInfoByShortCode" should "return a Some[MediaInfoFeed]" in {
    val request = Await.result(instagram.getMediaInfoByShortCode("BS7WNy9ADi8"), 10 seconds)
    request should be(anInstanceOf[Some[MediaInfoFeed]])
  }

  "searchTags" should "return a Some[TagSearchFeed]" in {
    val request = Await.result(instagram.searchTags("test"), 10 seconds)
    request should be(anInstanceOf[Some[TagSearchFeed]])
  }

  "getRecentMediaFeedTags" should "return a Some[MediaFeed]" in {
    val request = Await.result(instagram.getRecentMediaFeedTags("test"), 10 seconds)
    request should be(anInstanceOf[Some[MediaFeed]])
  }

  "setUserRelationship" should "return a Some[RelationshipFeed]" in {
    val request = Await.result(instagram.setUserRelationship(userId.getOrElse(""), Relationship.FOLLOW), 10 seconds)
    request should be(anInstanceOf[Some[RelationshipFeed]])
  }

  "getUserRequestedBy" should "return a Some[UserFeed]" in {
    val request = Await.result(instagram.getUserRequestedBy, 10 seconds)
    request should be(anInstanceOf[Some[UserFeed]])
  }

  "getUserRelationship" should "return a Some[RelationshipFeed]" in {
    val request = Await.result(instagram.getUserRelationship(userId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[RelationshipFeed]])
  }

  "searchFacebookPlace" should "return a Some[LocationSearchFeed]" in {
    val request = Await.result(instagram.searchFacebookPlace(locationId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[LocationSearchFeed]])
  }

  "searchMedia" should "return a Some[MediaFeed]" in {
    val request = Await.result(instagram.searchMedia(latitude.getOrElse(0), longitude.getOrElse(0)), 10 seconds)
    request should be(anInstanceOf[Some[MediaFeed]])
  }

  "getUserLikes" should "return a Some[LikesFeed]" in {
    val request = Await.result(instagram.getUserLikes(mediaId.getOrElse("")), 10 seconds)
    request should be(anInstanceOf[Some[LikesFeed]])
  }

}
