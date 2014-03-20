package net.liftmodules.squerylauth

import model.DbSchema
import net.liftweb.util.StringHelpers
import net.liftweb.common._
import net.liftweb.http.{S, Req, LiftSession }
import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.Session
import java.sql.DriverManager
import org.squeryl.adapters.H2Adapter
import net.liftweb.squerylrecord.RecordTypeMode._
import org.scalatest.{WordSpec, BeforeAndAfterAll}

/**
 * Created with IntelliJ IDEA.
 * User: j2
 * Date: 20-03-14
 * Time: 05:51 PM
 * To change this template use File | Settings | File Templates.
 */

trait TestLiftSession {
  def session = new LiftSession("", StringHelpers.randomString(20), Empty)
  def inSession[T](a: => T): T = S.init(Req.nil, session) { a }
}

trait SquerylTestKit extends BeforeAndAfterAll {
  this: WordSpec =>
  Class.forName("org.h2.Driver")

  Logger.setup = Full(net.liftweb.util.LoggingAutoConfigurer())
  Logger.setup.foreach { _.apply() }

  def configureH2() = {
    SquerylRecord.initWithSquerylSession(
      Session.create(
        DriverManager.getConnection("jdbc:h2:mem:dbname;DB_CLOSE_DELAY=-1",
          "sa", ""),
        new H2Adapter)
    )
  }

  def createDb() {
    inTransaction {
      try {
        DbSchema.drop
        DbSchema.create
      } catch {
        case e : Throwable =>
          throw e
      }
    }
  }


  override def beforeAll(configMap: Map[String, Any]) {
    // define the dbs
    //    dbs foreach { case (id, srvr, name) =>
    //      MongoDB.defineDb(id, new Mongo(srvr), name)
    //    }
  }

  override def afterAll(configMap: Map[String, Any]) {
    //if (!debug) {
      // drop the databases
      //      dbs foreach { case (id, _, _) =>
      //        MongoDB.use(id) { db => db.dropDatabase }
      //      }
    //}

    // clear the mongo instances
    //MongoDB.close
    //for (sc <- SquerylRecord.) {
    //  sc.connection.close()
   // }
  }
}

