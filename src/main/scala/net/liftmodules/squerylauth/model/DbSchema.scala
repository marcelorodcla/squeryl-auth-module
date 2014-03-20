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
  lazy val permissions: Table[Permission] = table("permission")
  lazy val roles: Table[Role] = table("role")
  lazy val loginTokens: Table[LoginToken] = table("login_token")
  lazy val extSessions: Table[ExtSession] = table("ext_sesssion")


  val permissionsToRoles = manyToManyRelation(permissions, roles, "permission_role").via[PermissionRole]((p,r,pr) =>
    (pr.roleId === r.id , pr.permissionId === p.id))
}
