package net.liftmodules.squerylauth.model

import net.liftweb.record.{MetaRecord, Record}
import org.squeryl.{Schema, KeyedEntity}
import net.liftweb.record.field.{StringField, LongField}
import net.liftweb.squerylrecord.RecordTypeMode._
import org.squeryl.dsl.CompositeKey2


class PermissionRole extends Record[PermissionRole] with KeyedEntity[CompositeKey2[LongField[PermissionRole], StringField[PermissionRole]]]{

  def id: CompositeKey2[LongField[PermissionRole], StringField[PermissionRole]] = compositeKey(permissionId, roleId)

  override def meta: MetaRecord[PermissionRole] = PermissionRole

  val permissionId = new LongField(this)

  val roleId = new StringField(this, 32)

}

object PermissionRole extends PermissionRole with MetaRecord[PermissionRole]
