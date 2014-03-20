package net.liftmodules.squerylauth

import field.PasswordField
import lib.SquerylMetaRecord
import model.{ExtSession, Permission, Role}
import net.liftweb.common._
import net.liftweb.http.{CleanRequestVarOnSessionTransition, RequestVar, SessionVar, LiftResponse, S}
import net.liftweb.util.Helpers
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field.{BooleanField, EmailField, LongField, StringField}
import net.liftweb.util.FieldError
import xml.UnprefixedAttribute
import org.squeryl.Schema

trait AuthUser {
  /*
   * String representing the User ID
   */
  def userIdAsString: String

  /*
   * A list of this user's permissions
   */
  def authPermissions: Set[APermission]

  /*
   * A list of this user's roles
   */
  def authRoles: Set[String]
}

trait AuthUserMeta[UserType <: AuthUser] {
  /*
   * True when the user request var is defined.
   */
  def isLoggedIn: Boolean
  /*
   * User logged in by supplying password. False if auto logged in by ExtSession or LoginToken.
   */
  def isAuthenticated: Boolean
  /*
   * Current user has the given role
   */
  def hasRole(role: String): Boolean
  def lacksRole(role: String): Boolean = !hasRole(role)
  def hasAnyRoles(roles: Seq[String]) = roles exists (r => hasRole(r.trim))

  /*
   * Current user has the given permission
   */
  def hasPermission(permission: APermission): Boolean
  def lacksPermission(permission: APermission): Boolean = !hasPermission(permission)

  def hasRole(user: UserType, role: String): Boolean
  def lacksRole(user: UserType, role: String): Boolean = !hasRole(user, role)
  def hasAnyRoles(user: UserType, roles: Seq[String]) = roles exists (r => hasRole(user, r.trim))

  /*
   * Current user has the given permission
   */
  def hasPermission(user: UserType, permission: APermission): Boolean
  def lacksPermission(user: UserType, permission: APermission): Boolean = !hasPermission(user, permission)

  /*
   * Log the current user out
   */
  def logUserOut(): Unit

  /*
   * Handle a LoginToken. Called from Locs.loginTokenLocParams
   */
  def handleLoginToken(): Box[LiftResponse] = Empty
}

/*
 * Trait that has login related code
 */
trait UserLifeCycle[UserType <: AuthUser] {

  /*
   * Given a String representing the User ID, find the user
   */
  def findByStringId(id: String): Box[UserType]

  // log in/out lifecycle callbacks
  def onLogIn: List[UserType => Unit] = Nil
  def onLogOut: List[Box[UserType] => Unit] = Nil

  // current userId stored in the session.
  private object curUserId extends SessionVar[Box[String]](Empty)
  def currentUserId: Box[String] = curUserId.is

  private object curUserIsAuthenticated extends SessionVar[Boolean](false)

  // Request var that holds the User instance
  private object curUser extends RequestVar[Box[UserType]](currentUserId.flatMap(findByStringId))
  with CleanRequestVarOnSessionTransition {
    override lazy val __nameSalt = Helpers.nextFuncName
  }
  def currentUser: Box[UserType] = curUser.is

  def isLoggedIn: Boolean = currentUserId.isDefined
  def isAuthenticated: Boolean = curUserIsAuthenticated.is

  def hasRole(role: String): Boolean = currentUser.map(u => hasRole(u, role)).openOr(false)

  def hasPermission(permission: APermission): Boolean = currentUser.map(u => hasPermission(u, permission)).openOr(false)

  def hasRole(user: UserType, role: String): Boolean = user.authRoles.exists(_ == role)

  def hasPermission(user: UserType, permission: APermission): Boolean = permission.implies(user.authPermissions)

  def logUserIn(who: UserType, isAuthed: Boolean = false, isRemember: Boolean = false) {
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    curUserId(Full(who.userIdAsString))
    curUserIsAuthenticated(isAuthed)
    curUser(Full(who))
    onLogIn.foreach(_(who))
    if (isRemember) {
      ExtSession.createExtSession(who.userIdAsString)
    }
  }

  def logUserOut() {
    onLogOut.foreach(_(currentUser))
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }
}

/*
 * Squeryl version of AuthUser
 */
trait SquerylAuthUser[T <: SquerylAuthUser[T]] extends Record[T] with KeyedRecord[Long]  with AuthUser  {
  self: T =>

  def userIdAsString: String = idField.is.toString

  val idField: LongField[T]
  val email: EmailField[T]
}

/*
 * Mix this in for a simple user.
 */
trait ProtoAuthUser[T <: ProtoAuthUser[T]] extends SquerylAuthUser[T] {
  self: T =>

  import Helpers._

  val username = new StringField(this, 32) {
    override def displayName = "Username"
    override def setFilter = trim _ :: super.setFilter

   // def valUnique(msg: => String)(value: String): List[FieldError] //ToDo
     /*
    override def validations =
      valUnique("Another user is already using that username, please enter a different one") _ ::
        valMinLen(3, "Username must be at least 3 characters") _ ::
        valMaxLen(32, "Username must be less than 33 characters") _ ::
        super.validations*/

  }

  /*
  * http://www.dominicsayers.com/isemail/
  */
  val email = new EmailField(this, 254) {
    override def displayName = "Email"
    override def setFilter = trim _ :: toLower _ :: super.setFilter

    //ToDo
    /*
    def valUnique(msg: => String)(value: String): List[FieldError] //ToDo

    override def validations =
      valUnique("That email address is already registered with us") _  ::
        valMaxLen(254, "Email must be 254 characters or less") _ ::
        super.validations
        */


  }

  // email address has been verified by clicking on a LoginToken link
  val verified = new BooleanField(this) {
    override def displayName = "Verified"
  }

  val password = new PasswordField(this, 8, 32) {
    override def displayName = "Password"
  }

  //ToDo relations
  val userRoles: List[Role] = Nil
  //object userRoles extends MappedRole(this)

  //  object permissions extends MappedOneToMany(Permission, Permission.userId)
  //  object roles extends StringRefListField(this, Role) {
  //    def permissions: List[Permission] = objs.flatMap(_.permissions.is)
  //    def names: List[String] = objs.map(_.id.is)
  //  }
  //
  //  lazy val authPermissions: Set[Permission] = (permissions.is ::: roles.permissions).toSet
  //  lazy val authRoles: Set[String] = roles.names.toSet

  /**
   * Using a lazy val means the user has to be reloaded if the attached roles or permissions change.
   */
  lazy val authPermissions: Set[APermission] = (Permission.userPermissions(idField.get) ::: userRoles.flatMap(_.permissions)).toSet
  lazy val authRoles: Set[String] = userRoles.map(_.idField.get).toSet

  lazy val fancyEmail = AuthUtil.fancyEmail(username.is, email.is)
}

trait ProtoAuthUserMeta[UserType <: SquerylAuthUser[UserType]]
  extends Record[UserType] with KeyedRecord[Long] with AuthUserMeta[UserType] with UserLifeCycle[UserType] {

  self: UserType =>

}

trait AuthUserSchema[UserType <: SquerylAuthUser[UserType]] extends Schema


