package net.liftmodules.squerylauth
package model

import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field.{OptionalStringField, OptionalLongField, StringField, LongField}
import net.liftweb.common.Loggable
import lib.SquerylMetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import org.squeryl.annotations.Column

class Permission extends Record[Permission] with KeyedRecord[Long] {
  def meta = Permission

  @Column("id")
  val idField = new LongField(this)

  /**
   * This field is empty for permissions attached to a role
   */
  val roleId = new OptionalStringField(this, 32)

  /**
   * This field is empty for permissions attached to a role
   */
  val userId = new OptionalLongField(this)

  val permission = new StringField(this, 1024)

  //ToDo lazy val users = DbSchema.usersToPermissions.right(this)
  lazy val roles = SquerylAuthSchema.roleToPermissions.right(this)

}

object Permission extends Permission with MetaRecord[Permission] with SquerylMetaRecord[Long, Permission] with Loggable {
  lazy val table = SquerylAuthSchema.permissions
  def createUserPermission(uid: Long, aPerm: APermission) = {
    createRecord.userId(uid).permission(aPerm.toString)
  }

  def removeAllUserPermissions(uid: Long) = {
    table.deleteWhere(_.userId === uid)
  }

  def toAPermission(perm: Permission) = APermission.fromString(perm.permission.get)
  def fromAPermission(aPerm: APermission): Permission = Permission.createRecord.permission(aPerm.toString)

  def userPermissions(uid: Long): List[APermission] = SquerylAuthSchema.permissions.where(_.userId === uid).toList.map(toAPermission)
}
