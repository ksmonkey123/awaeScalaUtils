package ch.awae.scala.util
package ref

import ch.awae.scala.util.ref.RefType.HARD

private class RebuildingHardRef[+A](private val builder: () => A, value: A) extends HardRef[A](value) {
  if (value == null)
    throw new NullPointerException

  override def map[B](f: A => B) = new RebuildingHardRef(() => f(builder()), f(value))
  override def flatMap[B](f: A => Ref[B, HARD]) = f(value)

  override def toSoft = Ref.soft(builder, value)
  override def toWeak = Ref.weak(builder, value)

  override def stripped(update: Boolean) = Ref.hard(value)
}