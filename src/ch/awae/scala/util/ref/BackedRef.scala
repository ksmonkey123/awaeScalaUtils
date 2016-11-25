package ch.awae.scala.util
package ref

import scala.annotation.unchecked.uncheckedVariance
import ch.awae.scala.util.ref.RefType.SOFT
import java.lang.ref.Reference

private abstract class BackedRef[+A, T <: RefType[T]] extends Ref[A, T] {

  protected def ref: Reference[A @uncheckedVariance]

  // ACCESSORS
  def apply() = Option(ref.get)
  @throws[NoSuchElementException]("if the reference is empty")
  def get = apply().get
  def getOrElse[B >: A](default: => B) = apply().getOrElse(default)
  def orElse[B >: A](alternative: => Ref[B, T]) = if (exists) this else alternative
  def exists = ref.get != null

  // MONADIC FUNCTIONS
  def flatMap[B](f: A => Ref[B, T]) = apply().map(f).getOrElse(this.asInstanceOf[Ref[B, T]])

}