package net.liftmodules.squerylauth
package model

import org.squeryl.{Table, Schema}
import net.liftweb.squerylrecord.RecordTypeMode._

/**
 * Created with IntelliJ IDEA.
 * User: j2
 * Date: 20-03-14
 * Time: 02:46 PM
 * To change this template use File | Settings | File Templates.
 */
object DbSchema extends Schema {
  override def name = SquerylAuth.schemaName.vend
  val extSessions: Table[ExtSession] = table("ext_session")
  val permissions: Table[Permission] = table("permission")
  val roles: Table[Role] = table("role")
  val loginTokens: Table[LoginToken] = table("login_token")

  val roleToPermissions = oneToManyRelation(roles, permissions).via((r,p) => p.roleId === Option(r.id))

}
