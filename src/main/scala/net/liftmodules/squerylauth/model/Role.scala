package net
package liftmodules
package squerylauth
package model

import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.common.{Empty, Full, Loggable}
import net.liftweb.record.field.StringField
import net.liftweb.http.S
import lib.SquerylMetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util._
import scala.xml.Text

/*
 * Simple record for storing roles. Role name is the PK.
 */

class Role extends Record[Role] with KeyedRecord[String] {
  def meta = Role

  val idField = new StringField(this, 32) {
    val prevValue = get
    override def displayName = S ? "Name"
    override def validations: List[ValidationFunction] =
      valMaxLen(32, S.?("liftmodule-squerylauth.role.name.max.length.msg")) _ ::
      valMinLen(3, S.?("liftmodule-squerylauth.role.name.min.length.msg")) _ ::
      meta.valUnique(S.?("liftmodule-squerylauth.role.name.unique.msg"), prevValue) _ ::
      super.validations
  }

  val category = new StringField(this, 50) {
    override def displayName = S ? "Category"
  }

  lazy val permissions = DbSchema.permissionsToRoles.right(this)

  override def equals(other: Any): Boolean = other match {
    case r: Role => r.idField.get == this.idField.get
    case _ => false
  }

  def userPermissions = permissions.toList.map(Permission.toAPermission)

  def displayName() = S ? ("userClientConnection.role."+idField.get)

  def asHtml = {
    val cls = "label" + (idField.get match {
      case Role.R_TEAM_OWNER => " label-important"
      case Role.R_TEAM_MEMBER => " label-info"
      case _ => ""
    })
    <span class={cls}>{displayName}</span>
  }

}


object Role extends Role with MetaRecord[Role] with SquerylMetaRecord[String, Role] with Loggable {

  val R_SUPERUSER    = "superuser"
  val R_USER         = "user"
  val R_TEAM_OWNER   = "owner"
  val R_TEAM_MEMBER  = "member"
  val R_TEAM_WATCHER = "watcher"

  val CAT_SYSTEM     = "system"
  val CAT_TEAM       = "team"

  lazy val table = DbSchema.roles

  def findOrCreate(roleId: String): Role = find(roleId).openOr(createRecord.idField(roleId))
  def findOrCreateAndSave(roleId: String, category: String, perms: Permission*): Role = {
    find(roleId).openOr {
      logger.info("Create Role %s for category %s".format(roleId, category))
      val r = createRecord.idField(roleId).category(category)
      //r.permissions.appendAll(perms) ToDo
      save(r)
    }
  }

  def allRoles(cat: String) = table.where(_.category === cat).toList

  lazy val TeamOwner   = Role.find(R_TEAM_OWNER).openOrThrowException("No Owner Role found")
  lazy val TeamMember  = Role.find(R_TEAM_MEMBER).openOrThrowException("No Member Role found")
  lazy val TeamWatcher = Role.find(R_TEAM_WATCHER).openOrThrowException("No Watcher Role found")

  def valUnique(msg: => String, prevValue: String)(value: String): List[FieldError] =
    find(value) match {
      case Full(role) if prevValue == role.idField.get => Nil
      case Empty => Nil
      case _ => List(FieldError(idField, Text(msg)))
    }


}