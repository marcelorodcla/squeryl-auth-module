package net.liftmodules.squerylauth
package model

import org.squeryl.{Table, Schema}
import net.liftweb.squerylrecord.RecordTypeMode._

object SquerylAuthSchema extends Schema {
  override def name = SquerylAuth.schemaName.vend
  val extSessions: Table[ExtSession] = table("ext_session")
  val permissions: Table[Permission] = table("permission")
  val roles: Table[Role] = table("role")
  val loginTokens: Table[LoginToken] = table("login_token")

  val roleToPermissions = oneToManyRelation(roles, permissions).via((r,p) => p.roleId === Option(r.id))

}
