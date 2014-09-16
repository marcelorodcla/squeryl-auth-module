package net.liftmodules.squerylauth
package model

import field._

import java.util.{Locale, UUID}

import org.joda.time.DateTime

import net.liftweb._
import common._
import http.{Req, S}
import http.provider.HTTPCookie
import record.field.LongField
import record.{MetaRecord, Record}
import squerylrecord.KeyedRecord
import util.{Helpers, LoanWrapper}
import lib.SquerylMetaRecord
import org.squeryl.annotations.Column

class ExtSession extends Record[ExtSession] with KeyedRecord[UUID] {
  def meta = ExtSession

  @Column("id")
  val idField = new UUIDField(this)

  val userId = new LongField(this, 0)
  val expires = new ExpiresField(this, SquerylAuth.extSessionExpires.vend)
}

object ExtSession extends ExtSession with MetaRecord[ExtSession] with SquerylMetaRecord[UUID, ExtSession] with Loggable {

  lazy val table = DbSchema.extSessions
  private lazy val whenExpires = SquerylAuth.extSessionExpires.vend
  private lazy val cookieName = SquerylAuth.extSessionCookieName.vend
  private lazy val cookiePath = SquerylAuth.extSessionCookiePath.vend
  private lazy val cookieDomain = SquerylAuth.extSessionCookieDomain.vend

  // create an extSession
  def createExtSession(uid: Long) {
    deleteExtCookie() // make sure existing cookie is removed
    val inst = save(createRecord.userId(uid))
    val cookie = new HTTPCookie(cookieName, Full(inst.idField.get.toString), cookieDomain, Full(cookiePath), Full(whenExpires.toPeriod.toStandardSeconds.getSeconds), Empty, Empty)
    S.addCookie(cookie)
  }

  def createExtSession(uid: String) {
    Helpers.asLong(uid) match {
      case Full(id) => createExtSession(id) //ToDo verify user
      case _ => ()
    }
  }

  // delete the ext cookie
  def deleteExtCookie() {
    for (cook <- S.findCookie(cookieName)) {
      // need to set a new cookie with expires now.
      val cookie = new HTTPCookie(cookieName, Empty, cookieDomain, Full(cookiePath), Full(0), Empty, Empty)
      S.addCookie(cookie)
      logger.debug("deleteCookie called")
      for {
        cv <- cook.value
        uuid <- Helpers.tryo(UUID.fromString(cv))
        extSess <- find(uuid)
      } {
        delete_!(extSess)
        logger.debug("ExtSession Record deleted")
      }
    }
  }

  def handleExtSession: Box[ExtSession] = {
    val extSess = for {
      cookie <- S.findCookie(cookieName) // empty means we should ignore it
      cookieValue <- cookie.value ?~ "Cookie value is empty"
      uuid <- Helpers.tryo(UUID.fromString(cookieValue)) ?~ "Invalid UUID"
      es <- find(uuid) ?~ "ExtSession not found: %s".format(uuid.toString)
    } yield es

    extSess match {
      case Failure(msg, _, _) => deleteExtCookie(); extSess // cookie is not valid, delete it
      case Full(es) if (es.expires.isExpired) => // if it's expired, delete it and the cookie
        deleteExtCookie()
        Failure("Extended session has expired")
      case _ => extSess
    }
  }
}
