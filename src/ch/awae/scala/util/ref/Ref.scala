package ch.awae.scala.util
package ref

import java.util.NoSuchElementException

import ch.awae.scala.util.ref.RefType.HARD
import ch.awae.scala.util.ref.RefType.SOFT
import ch.awae.scala.util.ref.RefType.WEAK

trait Ref[+A, T <: RefType[T]] extends (() => Option[A]) {

  def refType: RefType[T]

  // ACCESSORS
  @throws[NoSuchElementException]("if the reference is empty")
  def get: A
  def getOrElse[B >: A](default: => B): B
  def orElse[B >: A](alternative: => Ref[B, T]): Ref[B, T]
  def exists: Boolean

  // MONADIC FUNCTIONS
  def map[B](f: A => B): Ref[B, T]
  def flatMap[B](f: A => Ref[B, T]): Ref[B, T]

  // CONVERSION FUNCTIONS
  def toHard: Ref[A, HARD]
  def toSoft: Ref[A, SOFT]
  def toWeak: Ref[A, WEAK]

  // STRIPPER
  def stripped(update: Boolean = false): Ref[A, T] = this
}

object Ref {

  def hard[A](value: A): Ref[A, HARD] = new HardRef(value)
  def hard[A](f: () => A): Ref[A, HARD] = hard(f, f())
  def hard[A](f: () => A, value: A): Ref[A, HARD] =
    new RebuildingHardRef(f, value)

  def soft[A](value: A): Ref[A, SOFT] = new SoftRef(value)
  def soft[A](f: () => A, force: Boolean = false): Ref[A, SOFT] =
    if (force) new RebuildingSoftRef(f, f())
    else new RebuildingSoftRef(f)
  def soft[A](f: () => A, value: A): Ref[A, SOFT] =
    new RebuildingSoftRef(f, value)

  def weak[A](value: A): Ref[A, WEAK] = new WeakRef(value)
  def weak[A](f: () => A, force: Boolean = false): Ref[A, WEAK] =
    if (force) new RebuildingWeakRef(f, f())
    else new RebuildingWeakRef(f)
  def weak[A](f: () => A, value: A): Ref[A, WEAK] =
    new RebuildingWeakRef(f, value)
}