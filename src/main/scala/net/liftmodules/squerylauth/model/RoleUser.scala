package net.liftmodules.squerylauth.model

import net.liftweb.record.{MetaRecord, Record}
import org.squeryl.{Schema, KeyedEntity}
import net.liftweb.record.field.{StringField, LongField}
import net.liftweb.squerylrecord.RecordTypeMode._
import org.squeryl.dsl.CompositeKey2


class RoleUser extends Record[RoleUser] with KeyedEntity[CompositeKey2[StringField[RoleUser], LongField[RoleUser]]]{

  def id: CompositeKey2[StringField[RoleUser], LongField[RoleUser]] = compositeKey(roleId, userId)

  override def meta: MetaRecord[RoleUser] = RoleUser

  val userId = new LongField(this)

  val roleId = new StringField(this, 32)

}

object RoleUser extends RoleUser with MetaRecord[RoleUser]
