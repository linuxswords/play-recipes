package plugins

import play.api._
import play.api.libs.ws.WS
import java.net.{URLEncoder, URL}
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.util.Collections
import scala.collection.mutable
import scala.io.Source
import play.api.libs.ws.Response
import play.api.libs.json.JsString
import scala.Some
import play.api.libs.json.JsObject
import plugins.CouchDBPlugin.{Server, Authentication, DBAccess}

/**
 * @author knm
 */
class CouchDBPlugin(app: Application) extends Plugin {
  val log = Logger(classOf[CouchDBPlugin])

  def validateDesignDoc(doc: JsValue, cfg: Configuration): Boolean = false

  def resolveResource(resource: String): String = {
    log.debug(s"Loading resource '$resource'")
    Source.fromInputStream(getClass.getResourceAsStream(resource)).buffered.mkString
  }

  def updateDesign(dba: DBAccess, cfg: Configuration, rev: Option[String]): Future[DBAccess] = {
    val docid = "_design/" + cfg.getString("name").get
    log.info(s"Updating design $docid")
    val obj: mutable.Buffer[(String, JsValue)] = mutable.Buffer("_id" -> JsString(docid))
    rev.foreach {
      r =>
        obj += ("_rev" -> JsString(r))
    }
    obj += ("language" -> JsString(cfg.getString("language").getOrElse("javascript")))

    val views = for {
      view <- cfg.getConfigList("views").get
      vname <- view.getString("name")
    } yield {
      val map = view.getString("map").get
      val reduce = view.getString("reduce")
      if (reduce.isDefined) {
        (vname, Json.obj(
          "map" -> resolveResource(map),
          "reduce" -> resolveResource(reduce.get)
        ))
      } else {
        (vname, Json.obj(
          "map" -> resolveResource(map)
        ))
      }
    }

    obj += ("views" -> JsObject(views.toSeq))
    dba.doc(docid, JsObject(obj)).map(design => dba)
  }

  private def updateDesigns(fdb: Future[DBAccess]): Future[DBAccess] = fdb flatMap {
    db =>
      val designs = db.cfg.getConfigList("designs").getOrElse(Collections.emptyList[Configuration]())
      designs.foldLeft(future(db)) {
        case (chain, cfg) =>
          chain.flatMap {
            cdb =>
              cdb.doc("_design/" + cfg.getString("name").get).flatMap {
                case Right(value) => if (!validateDesignDoc(value, cfg)) {
                  updateDesign(cdb, cfg, Some((value \ "_rev").as[String]))
                } else {
                  future(cdb)
                }
                case _ => updateDesign(cdb, cfg, None)
              }
          }
      }
  }

  override def onStart() {
    log.info("Starting couchdb plugin")
    for ((_, dba) <- db) {
      log.info(s"Checking db ${dba.dbName} for existence")
      Await.result(updateDesigns(dba.createIfNotExists), 60.seconds)
    }
  }

  lazy val server = {
    val serverSettings = app.configuration.getConfig("couchdb.server").get
    val auth = serverSettings.getConfig("auth").map {
      c =>
        Authentication(c.getString("user").get, c.getString("password").get)
    }
    val serverUrl: String = serverSettings.getString("url").get
    log.info(s"Initializing server at $serverUrl")
    Server(serverUrl, auth)
  }

  lazy val db = {
    val dbs = for {
      dbcfg <- app.configuration.getConfigList("couchdb.db").get
      dbname = dbcfg.getString("name").get
    } yield {
      log.info(s"Opening database ${dbname}")
      (dbname, DBAccess(dbname, dbcfg, server))
    }
    dbs.toMap
  }
}


object CouchDBPlugin {

  // TODO: make this more robust
  def db(name: String) = Play.maybeApplication.get.plugin(classOf[CouchDBPlugin]).get.db.get(name).get

  private def urienc(str: String): String = {
    URLEncoder.encode(str, "UTF-8")
  }

  case class ServerError(message: String, method: String, path: String, response: Response) extends Throwable(s"$message: $method => $path = ${response.status}: ${response.statusText}")

  case class Authentication(user: String, password: String)

  case class Server(url: String, auth: Option[Authentication] = None) {
    private lazy val pUrl = new URL(url)

    private def encodeParams(params: Seq[(String, String)]): String = {
      val (_, encodedParams) = params.foldLeft(("?", "")) {
        case ((seq, queryStr), element) =>
          ("&", queryStr + seq + urienc(element._1) + "=" + urienc(element._2))
      }
      encodedParams
    }

    def request(path: String, params: (String, String)*) = {
      WS.url(new URL(pUrl.getProtocol, pUrl.getHost, pUrl.getPort, s"${pUrl.getPath}/$path${encodeParams(params)}").toString)
        .withHeaders(("Accept", "application/json"))
    }
  }


  case class DBAccess(dbName: String, cfg: Configuration, conn: Server) {

    val log = Logger(classOf[DBAccess])

    def create: Future[JsValue] = {
      conn.request(dbName).put("").map {
        r =>
          if (r.status != 201) {
            throw ServerError("Error creating database", "PUT", dbName, r)
          }
          r.json
      }
    }

    def exists: Future[Boolean] = {
      conn.request(dbName).head().map {
        r =>
          r.status == 200
      }
    }

    def createIfNotExists: Future[DBAccess] = {
      exists.flatMap {
        dbExists =>
          if (!dbExists) {
            log.info(s"Creating non-existing database ${dbName}")
            create.map((_) => this)
          } else {
            future(this)
          }
      }
    }

    def doc(id: String): Future[Either[Throwable, JsValue]] = {
      val path = docPath(id)
      conn.request(path).get().map {
        r =>
          if (r.status != 200) {
            Left(ServerError("Document not found", "GET", path, r))
          } else {
            Right(r.json)
          }
      }
    }
    def viewPath(name: String, view: String) = s"$dbName/_design/$name/_view/$view"

    def view(name: String, view: String, key: Option[JsValue]=None,
             startKey: Option[JsValue]=None,
             endKey: Option[JsValue]=None,
             params: List[(String, String)] = Nil) = {
      val path = viewPath(name, view)
      log.debug(s"requesting view with $path and key $key")
      val keys = List(("key", key), ("startKey", startKey), ("endKey", endKey)).flatMap { case(key,value) =>
        value.map { v =>
          (key, Json.stringify(v))
        }
      }
      conn.request(path, (keys ++ params):_* ).get.map{
        r =>
          if (r.status > 299) {
            throw ServerError("Error saving document ", "PUT", path, r)
          }
          r.json
      }

    }

    def docPath(id: String) = s"$dbName/$id"

    def doc(id: String, content: JsValue) = {
      val path = docPath(id)
      log.debug(s"Storing doc $path with content: ${content}")
      conn.request(path).put(content).map {
        r =>
          if (r.status > 299) {
            throw ServerError("Error saving document ", "PUT", path, r)
          }
          r.json
      }
    }

    def forceUpdate(id: String, content: JsValue): Future[JsValue] = {
      doc(id).flatMap{ _ match{
        case Left(t) => throw new RuntimeException("Error saving document", t)
        case Right(jsVal) =>
          val rev = JsObject(Seq(("_rev" , jsVal \ "_rev")))
          val mergedObject = content.as[JsObject].deepMerge(rev)
          doc(id, mergedObject)
      }
      }
    }


    def update(id: String, rev: String, content: JsValue) = {
      val path = docPath(id)

      val revObject = JsObject(Seq("_rev" -> JsString(rev)))
      val idObject = JsObject(Seq("_id" -> JsString(id)))

      val contentWithRevisionAndId =
        content.as[JsObject].deepMerge(idObject).deepMerge(revObject)
      log.debug(s"Storing doc $path with content: ${contentWithRevisionAndId}")
      conn.request(path).put(contentWithRevisionAndId).map {
        r =>
          if (r.status > 299) {
            throw ServerError("Error saving document ", "PUT", path, r)
          }
          r.json
      }
    }
  }

}

