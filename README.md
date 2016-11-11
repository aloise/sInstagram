# A Scala library for the Instagram API.

## Installation

submitting maven now

## Documentation

### Examples

```scala
import com.yukihirai0505.{Instagram, Authentication}
import com.yukihirai0505.http.Response
import com.yukihirai0505.model.ResponseType
import com.yukihirai0505.model.Scope
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

val clientId = "client-id"
val clientSecret = "client-secret"
val callbackUrl = "callback-URI"
val authentication: Authentication = new Authentication
val scopes: Seq[Scope] = Seq(Scope.BASIC) // other: Scope.FOLLOWER_LIST, Scope.PUBLIC_CONTENT, Scope.COMMENTS, Scope.LIKES, Scope.RELATIONSHIPS

// Server-Side login
// Step 1: Get a URL to call. This URL will return the CODE to use in step 2
val codeUrl = authentication.authURL(clientId, callbackUrl, ResponseType.CODE, scopes)

// Step 2: Use the code to get an AccessToken
val accessTokenFuture = Authentication.requestToken(clientId, clientSecret, redirectURI, code = "the-code-from-step-1")
val accessToken = accessTokenFuture onComplete {
  case Success(Response(Some(token: AccessToken), _, _, _)) => token
  case Failure(t) => println("An error has occured: " + t.getMessage)
}

// Making an authenticated call
val instagram: Instagram = new Instagram("an-access-token")
// The library is asynchronous by default and returns a promise.
val future = instagram.getUserRecentMedia()
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
future onComplete {
  case Success(Response(body, statusCode, headers)) => {
    println(body.getOrElse())
  } // do stuff
  case Failure(t) => println("An error has occured: " + t.getMessage)
}
```

Please look at this file to see all available methods:

https://github.com/yukihirai0505/sInstagram/blob/master/src/main/scala/com/yukihirai0505/instsagram/InstagramClient.scala

## References

inspired by following source code

- https://github.com/sachin-handiekar/jInstagram
- https://github.com/Rydgel/scalagram
