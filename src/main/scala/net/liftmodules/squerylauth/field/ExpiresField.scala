package net.liftmodules.squerylauth
package field

import java.util.Date

import org.joda.time.{ReadablePeriod, DateTime}

import net.liftweb._
import common._
import record.field.DateTimeField
import record.Record

class ExpiresField[OwnerType <: Record[OwnerType]](rec: OwnerType) extends DateTimeField(rec) {

  def this(rec: OwnerType, period: ReadablePeriod) = {
    this(rec)
    setFromAny(periodToExpiresDate(period))
  }

  def periodToExpiresDate(period: ReadablePeriod): Date = ((new DateTime).plus(period.toPeriod)).toDate

  def apply(in: ReadablePeriod): OwnerType = apply(Full(((new DateTime).plus(in.toPeriod)).toGregorianCalendar))

  def isExpired: Boolean = (new DateTime).getMillis >= (new DateTime(value)).getMillis
}
