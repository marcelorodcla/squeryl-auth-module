package net.liftmodules.squerylauth
package model

import net.liftweb.squerylrecord.RecordTypeMode._

class SimpleUserSpec extends WithSessionSpec with SquerylTestKit {
  //override val debug = true
  val userPassword = "password"

  def testUser = SimpleUser.createRecord
    .email("test@domain.com")
    .password(userPassword, true)

  "SimpleUser" should {

    "save permissions properly" in {
      val printer = APermission("printer")
      val userEntity = APermission("user.users", "read")
      val perms = List(printer, userEntity)
      val user = SimpleUser.save(testUser)
        //.permissions(perms)
        //.save
      val userFromDb = SimpleUser.find(user.idField.get)
      userFromDb should be ('defined)
      userFromDb foreach { u =>
        u.authPermissions should equal (perms)
      }
    }

    "check permissions properly" in {
      val adminLogin = APermission("admin", "login")
      val adminAll = APermission("admin")
      adminLogin.implies(adminAll) should equal (true)
      adminLogin.implies(Set(adminAll)) should equal (true)

      val user = SimpleUser.createRecord//.permissions(List(Permission("admin")))
      SimpleUser.logUserIn(user, false, false)
      SimpleUser.hasPermission(adminLogin) should equal (true)
    }
  }


}

