package net.liftmodules.squerylauth
package model

import field.ExpiresField

import org.joda.time.Hours

import net.liftweb._
import common._
import http._
import record.field.LongField
import record.{MetaRecord, Record, MandatoryTypedField}
import squerylrecord.KeyedRecord
import lib.SquerylMetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import util.Helpers
import org.squeryl.annotations.Column

/**
 * This is a token for automatically logging a user in
 */
class LoginToken extends Record[LoginToken] with KeyedRecord[Long] {
  def meta = LoginToken

  @Column("id")
  val idField = new LongField(this)
  val userId = new LongField(this)
  val expires = new ExpiresField(this, SquerylAuth.loginTokenExpires.vend)

  def url: String = meta.url(this)
}

object LoginToken extends LoginToken with MetaRecord[LoginToken] with SquerylMetaRecord[Long, LoginToken] {

  private lazy val loginTokenUrl = SquerylAuth.loginTokenUrl.vend
  lazy val table = DbSchema.loginTokens

  def url(inst: LoginToken): String = "%s%s?token=%s".format(S.hostAndPath, loginTokenUrl, inst.id.toString)

  def createForUserId(uid: Long): LoginToken = {
    save(createRecord.userId(uid))
  }

  def deleteAllByUserId(uid: Long) {
    table.deleteWhere(_.userId === uid)
  }

  def findByStringId(in: String): Box[LoginToken] = Helpers.asLong(in).flatMap(find(_))
}
