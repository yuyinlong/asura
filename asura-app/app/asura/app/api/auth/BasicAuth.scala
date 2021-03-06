package asura.app.api.auth

import java.nio.charset.StandardCharsets
import java.util.Base64

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import asura.core.auth.AuthorizeAndValidate
import asura.core.es.model.Authorization

import scala.concurrent.Future

object BasicAuth extends AuthorizeAndValidate {

  override val `type`: String = "Basic Auth"
  override val description: String =
    """## Basic Auth
      |> Add a header with the key `Authorization` and the value a string encoded by base64.
      |
      |### Example
      |
      |If the data is as below:
      |
      |```json
      |{
      |    "username" : "a",
      |    "password": "b"
      |}
      |```
      |
      |A header `Authorization: Basic YTpi` will be added. `YTpi` is generated by call `Base64.encode("a:b")`.
      |
    """.stripMargin


  override val template: String =
    """{
      |    "username" : "",
      |    "password": ""
      |}
    """.stripMargin

  override def authorize(request: HttpRequest, auth: Authorization): Future[HttpRequest] = {
    val username = auth.data.get("username")
    val password = auth.data.get("password")
    val bytes = Base64.getEncoder.encode(s"${username.get}:${password.get}".getBytes(StandardCharsets.UTF_8))
    val value = new String(bytes, StandardCharsets.UTF_8)
    Future.successful(request.withHeaders(request.headers :+ RawHeader("Authorization", s"Basic ${value}")))
  }

  override def validate(auth: Authorization): (Boolean, String) = {
    val username = auth.data.get("username")
    val password = auth.data.get("password")
    if (username.isEmpty || password.isEmpty) {
      (false, "username and password can't be empty")
    } else {
      (true, null)
    }
  }
}
