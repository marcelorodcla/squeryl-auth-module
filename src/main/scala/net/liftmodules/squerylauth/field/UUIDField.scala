package net.liftmodules.squerylauth.field

import java.util.UUID

import scala.xml.NodeSeq

import net.liftweb.common.{Box, Empty, Failure, Full}
import net.liftweb.http.js.JE.{JsNull, JsRaw}
import net.liftweb.http.S
import net.liftweb.record.{Record, Field, FieldHelpers, MandatoryTypedField}
import net.liftweb.util.Helpers._
import net.liftweb.squerylrecord.SquerylRecordField
import net.liftweb.json.{JNothing => _, JNull => _, JString => _, JField => _, JObject => _, render => _, JValue => _, _}
import JsonAST._

class UUIDField[OwnerType <: Record[OwnerType]](rec: OwnerType)
  extends Field[UUID, OwnerType]
  with MandatoryTypedField[UUID] with SquerylRecordField
{

  def owner = rec

  def defaultValue = UUID.randomUUID

  /**
   * Should return the class of the field's value in the database.
   */
  def classOfPersistentField: Class[_] = classOf[String]

  def setFromAny(in: Any): Box[UUID] = in match {
    case uid: UUID => setBox(Full(uid))
    case Some(uid: UUID) => setBox(Full(uid))
    case Full(uid: UUID) => setBox(Full(uid))
    case (uid: UUID) :: _ => setBox(Full(uid))
    case s: String => setFromString(s)
    case Some(s: String) => setFromString(s)
    case Full(s: String) => setFromString(s)
    case null|None|Empty => setBox(defaultValueBox)
    case f: Failure => setBox(f)
    case o => setFromString(o.toString)
  }

  def setFromJValue(jvalue: JValue): Box[UUID] = jvalue match {
    case JNothing|JNull if optional_? => setBox(Empty)
    case JObject(JField("$uuid", JString(s)) :: Nil) => setFromString(s)
    case other => setBox(FieldHelpers.expectedA("JObject", other))
  }

  def setFromString(in: String): Box[UUID] = tryo(UUID.fromString(in)) match {
    case Full(uid: UUID) => setBox(Full(uid))
    case f: Failure => setBox(f)
    case other => setBox(Failure("Invalid UUID string: "+in))
  }

  private def elem =
    S.fmapFunc(S.SFuncHolder(this.setFromAny(_))){funcName =>
        <input type="text"
               name={funcName}
               value={valueBox.map(v => v.toString) openOr ""}
               tabindex={tabIndex toString}/>
    }

  def toForm =
    uniqueFieldId match {
      case Full(id) => Full(elem % ("id" -> id))
      case _ => Full(elem)
    }

  def asJs = asJValue match {
    case JNothing => JsNull
    case jv => JsRaw(Printer.compact(render(jv)))
  }

  def asJValue: JValue = valueBox.map(v => JField("$uuid", JString(v.toString))) openOr (JNothing: JValue)

}

