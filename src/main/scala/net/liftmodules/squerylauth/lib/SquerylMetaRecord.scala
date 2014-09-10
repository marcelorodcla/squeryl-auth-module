package net.liftmodules.squerylauth
package lib

import net.liftweb.record.{Record, MetaRecord}
import org.squeryl.{Table, Schema}
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.common._
import net.liftweb.squerylrecord.KeyedRecord

/**
 * Created with IntelliJ IDEA.
 * User: j2
 * Date: 20-03-14
 * Time: 01:21 PM
 * To change this template use File | Settings | File Templates.
 */
trait SquerylMetaRecord[T, BaseRecord <: Record[BaseRecord] with KeyedRecord[T]] {
  self: BaseRecord =>

  def table: Table[BaseRecord]

  def find(id: T): Box[BaseRecord] = inTransaction(table.lookup(id))

  def findAll: List[BaseRecord] = inTransaction(table.toList)

  def save(inst: BaseRecord) = inTransaction(table.insertOrUpdate(inst))

  def delete_!(inst: BaseRecord) = inTransaction(table.delete(inst.id))

}
