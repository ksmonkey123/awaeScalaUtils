package ch.awae.scala.util
package ref

import ch.awae.scala.util.ref.RefType.HARD
import java.util.NoSuchElementException

private class HardRef[+A](private val value: A) extends Ref[A, HARD] {
  // cache Option for performance
  private lazy val option = Option(value)

  def refType = HARD
  // ACCESSORS
  def apply() = option
  @throws[NoSuchElementException]("if the reference is empty")
  def get = if (value != null) value else throw new NoSuchElementException
  def getOrElse[B >: A](default: => B) = if (value != null) value else default
  def orElse[B >: A](alternative: => Ref[B, HARD]) = if (value != null) this else alternative
  def exists = value != null

  // MONADIC FUNCTIONS
  def map[B](f: A => B) = if (value != null) new HardRef(f(value)) else this.asInstanceOf[HardRef[B]]
  def flatMap[B](f: A => Ref[B, HARD]) = if (value != null) f(value) else this.asInstanceOf[HardRef[B]]

  // CONVERSION FUNCTIONS
  def toHard: Ref[A, HARD] = this
  def toSoft = Ref.soft(value)
  def toWeak = Ref.weak(value)

}