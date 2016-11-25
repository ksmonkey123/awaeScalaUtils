package ch.awae.scala.util
package ref

sealed trait RefType[T <: RefType[T]] { self: T => }

object RefType {
  sealed trait HARD extends RefType[HARD]
  sealed trait SOFT extends RefType[SOFT]
  sealed trait WEAK extends RefType[WEAK]

  object HARD extends HARD
  object SOFT extends SOFT
  object WEAK extends WEAK
}