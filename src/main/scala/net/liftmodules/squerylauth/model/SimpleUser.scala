package net.liftmodules.squerylauth
package model

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.common.Full
import net.liftweb.record.MetaRecord
import net.liftweb.record.field.LongField
import lib.SquerylMetaRecord
import net.liftweb.util.Helpers
import org.squeryl.{Table, Schema}
import net.liftweb.squerylrecord.RecordTypeMode._
import org.squeryl.annotations.Column

class SimpleUser extends ProtoAuthUser[SimpleUser] {
  @Column("id")
  val idField = new LongField(this)

  def meta = SimpleUser

  def findAllByUsername(username: String): List[SimpleUser] = meta.findAllByUsername(username)

  def findAllByEmail(email: String): List[SimpleUser] = meta.findAllByEmail(email)

}

object SimpleUser extends SimpleUser with MetaRecord[SimpleUser] with ProtoAuthUserMeta[SimpleUser] with Loggable {

  def findByStringId(id: String): Box[SimpleUser] = Helpers.asLong(id).flatMap(find(_))

  def find(id: Long): Box[SimpleUser] = SimpleUserSchema.users.lookup(id)

  override def onLogOut: List[Box[SimpleUser] => Unit] = List(
    x => logger.debug("User.onLogOut called."),
    boxedUser => boxedUser.foreach { u =>
      ExtSession.deleteExtCookie()
    }
  )

  /*
   * SquerylAuth vars
   */
  private lazy val siteName = SquerylAuth.siteName.vend
  private lazy val sysUsername = SquerylAuth.systemUsername.vend
  private lazy val indexUrl = SquerylAuth.indexUrl.vend
  private lazy val registerUrl = SquerylAuth.registerUrl.vend
  private lazy val loginTokenAfterUrl = SquerylAuth.loginTokenAfterUrl.vend

  /*
   * LoginToken
   */
  override def handleLoginToken: Box[LiftResponse] = {
    val resp = S.param("token").flatMap(LoginToken.findByStringId) match {
      case Full(at) if (at.expires.isExpired) => {
        LoginToken.delete_!(at)
        RedirectWithState(indexUrl, RedirectState(() => { S.error(S ? "liftmodule-squerylauth.simpleUser.handleLoginToken.expiredToken") }))
      }
      case Full(at) => find(at.userId.is).map(user => {
        if (user.validate.length == 0) {
          user.verified(true)
          SimpleUserSchema.users.insertOrUpdate(user)
          logUserIn(user)
          LoginToken.delete_!(at)
          RedirectResponse(loginTokenAfterUrl)
        }
        else {
          LoginToken.delete_!(at)
          regUser(user)
          RedirectWithState(registerUrl, RedirectState(() => { S.notice(S ? "liftmodule-squerylauth.simpleUser.handleLoginToken.completeRegistration") }))
        }
      }).openOr(RedirectWithState(indexUrl, RedirectState(() => { S.error( S ? "liftmodule-squerylauth.simpleUser.handleLoginToken.userNotFound") })))
      case _ => RedirectWithState(indexUrl, RedirectState(() => { S.warning(S ? "liftmodule-squerylauth.simpleUser.handleLoginToken.noToken") }))
    }

    Full(resp)
  }

  // send an email to the user with a link for logging in
  def sendLoginToken(user: SimpleUser): Unit = {
    import net.liftweb.util.Mailer._

    val token = LoginToken.createForUserId(user.idField.is)

    val msgTxt = S ? ( "liftmodule-squerylauth.simpleUser.sendLoginToken.msg", siteName, token.url, sysUsername)

    sendMail(
      From(SquerylAuth.systemFancyEmail),
      Subject(S ? ("liftmodule-squerylauth.simpleUser.sendLoginToken.subject",siteName)),
      To(user.fancyEmail),
      PlainMailBodyType(msgTxt)
    )
  }

  /*
  * Test for active ExtSession.
  */
  def testForExtSession: Box[Req] => Unit = {
    ignoredReq => {
      logger.debug("ExtSession currentUserId: "+currentUserId.toString)
      if (currentUserId.isEmpty) {
        ExtSession.handleExtSession match {
          case Full(es) => find(es.userId.is).foreach { user => logUserIn(user, false) }
          case Failure(msg, _, _) => logger.warn("Error logging user in with ExtSession: %s".format(msg))
          case Empty => logger.warn("Unknown error logging user in with ExtSession: Empty")
        }
      }
    }
  }

  override def findAllByUsername(username: String): List[SimpleUser] = SimpleUserSchema.users.where(_.username === username).toList

  override def findAllByEmail(email: String): List[SimpleUser] = SimpleUserSchema.users.where(_.email === email).toList

  object regUser extends SessionVar[SimpleUser](currentUser openOr meta.createRecord)
}

object SimpleUserSchema extends AuthUserSchema[SimpleUser] {
  val users: Table[SimpleUser] = table("users")

}
