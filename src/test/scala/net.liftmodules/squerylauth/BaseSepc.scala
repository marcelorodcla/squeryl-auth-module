package net.liftmodules.squerylauth

import org.scalatest.WordSpec

import net.liftweb._
import common._
import http._
import org.scalatest.matchers.ShouldMatchers
import util._
import Helpers._

trait BaseSpec extends WordSpec with ShouldMatchers

trait WithSessionSpec extends BaseSpec {
  def session = new LiftSession("", randomString(20), Empty)

  override def withFixture(test: NoArgTest) = {
    S.initIfUninitted(session) { test() }
  }
}