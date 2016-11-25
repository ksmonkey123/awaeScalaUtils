package ch.awae.scala.util
package ref

import java.lang.ref.WeakReference

import scala.annotation.unchecked.uncheckedVariance

import ch.awae.scala.util.ref.RefType.WEAK

private class WeakRef[+A](protected val ref: WeakReference[A @uncheckedVariance]) extends BackedRef[A, WEAK] {
  def this(value: A) = this(new WeakReference(value))

  def refType = WEAK

  // MONADIC FUNCTIONS
  def map[B](f: A => B) = apply().map(v => new WeakRef(f(v))).getOrElse(this.asInstanceOf[WeakRef[B]])

  // CONVERSION FUNCTIONS
  def toHard = Ref.hard(ref.get)
  def toSoft = Ref.soft(ref.get)
  def toWeak: Ref[A, WEAK] = this

}