package ch.awae.scala.util
package ref

import java.lang.ref.SoftReference

import scala.annotation.unchecked.uncheckedVariance

import ch.awae.scala.util.ref.RefType.SOFT

/**
 * SoftRef that is able to reconstruct its value if
 * it has been collected.
 *
 * The value is rebuilt only when the methods `get` or `flatMap` are called
 * or when converting it into a hard reference.
 *
 *
 * @param builder the function that should be used to rebuild a lost value
 */
private class RebuildingSoftRef[+A] private (private val builder: () => A, private var ref: SoftReference[A @uncheckedVariance]) extends Ref[A, SOFT] {

  // CONSTRUCTORS
  def this(builder: () => A) = this(builder, new SoftReference(null.asInstanceOf[A]))
  def this(builder: () => A, value: A) = this(builder, new SoftReference(value))

  def refType = SOFT

  // ACCESSORS
  def apply = Option(get)
  def get = ref.get match {
    case null =>
      val value = builder()
      ref = new SoftReference(value)
      value
    case value => value
  }
  def exists = ref.get != null
  def getOrElse[B >: A](default: => B) = get
  def orElse[B >: A](alternative: => Ref[B, SOFT]) = this

  // MONADICS
  def map[B](f: A => B) = {
    val b = () => f(builder())
    ref.get match {
      case null => new RebuildingSoftRef(b)
      case x => new RebuildingSoftRef(b, f(x))
    }
  }
  def flatMap[B](f: A => Ref[B, SOFT]) = f(get)

  // CONVERSION FUNCTION
  def toHard = Ref.hard(get, builder)
  def toSoft: Ref[A, SOFT] = this
  def toWeak = ref.get match {
    case null => Ref.weak(builder, false)
    case x => Ref.weak(x, builder)
  }

  override def stripped(update: Boolean) = Ref.soft(if (update) get else ref.get)

}