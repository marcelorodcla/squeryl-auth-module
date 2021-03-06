package net.liftmodules.squerylauth

import model.{SimpleUserSchema, SimpleUser, SquerylAuthSchema}
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
  def liftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
  def inSession[T](a: => T): T = S.init(Req.nil, liftSession) { a }
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
        SquerylAuthSchema.drop
        SquerylAuthSchema.create
        SquerylAuthSchema.printDdl
        SimpleUserSchema.drop
        SimpleUserSchema.create
        SimpleUserSchema.printDdl
      } catch {
        case e : Throwable =>
          throw e
      }
    }
  }


  override def beforeAll() = {
    configureH2()
    createDb()
  }

  override def afterAll() = {
  }
}

