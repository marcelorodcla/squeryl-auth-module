package net.liftmodules.squerylauth
package model

import net.liftweb.squerylrecord.RecordTypeMode._

class SimpleUserSpec extends WithSessionSpec with SquerylTestKit with TestLiftSession {
  //override val debug = true
  val userPassword = "password"

  def testUser = SimpleUser.createRecord
    .email("test@domain.com")
    .password(userPassword, true)

  "SimpleUser" should {

    "save permissions properly" in {
      inSession {
        inTransaction {
            val printer = APermission("printer")
            val userEntity = APermission("user.users", "read")
            val perms = List(printer, userEntity)
            val user = SimpleUserSchema.users.insertOrUpdate(testUser)
            perms.map(p => Permission.save(Permission.createUserPermission(user.id, p)))
            val userFromDb = SimpleUser.find(user.idField.get)
            userFromDb should be ('defined)
            userFromDb foreach { u =>
              u.authPermissions should equal (perms.toSet)
            }
        }
      }
    }

    "check permissions properly" in {
      inSession {
        inTransaction {
          val adminLogin = APermission("admin", "login")
          val adminAll = APermission("admin")
          adminLogin.implies(adminAll) should equal (true)
          adminLogin.implies(Set(adminAll)) should equal (true)

          val user = testUser
          Permission.save(Permission.createUserPermission(user.id, APermission("admin")))
          SimpleUser.logUserIn(user, false, false)
          SimpleUser.hasPermission(adminLogin) should equal (true)
        }
      }
    }
  }

}

