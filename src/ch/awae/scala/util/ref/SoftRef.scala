package ch.awae.scala.util
package ref

import java.lang.ref.SoftReference
import scala.annotation.unchecked.uncheckedVariance
import ch.awae.scala.util.ref.RefType.SOFT

private class SoftRef[+A] private (protected val ref: SoftReference[A @uncheckedVariance]) extends BackedRef[A, SOFT] {

  def this(value: A) = this(new SoftReference(value))

  def refType = SOFT

  // MONADIC FUNCTIONS
  def map[B](f: A => B) = apply().map(v => new SoftRef(f(v))).getOrElse(this.asInstanceOf[SoftRef[B]])

  // CONVERSION FUNCTIONS
  def toHard = Ref.hard(ref.get)
  def toSoft: Ref[A, SOFT] = this
  def toWeak = Ref.weak(ref.get)

}