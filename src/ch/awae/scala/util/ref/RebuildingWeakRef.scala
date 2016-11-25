package ch.awae.scala.util
package ref

import java.lang.ref.WeakReference

import scala.annotation.unchecked.uncheckedVariance

import ch.awae.scala.util.ref.RefType.WEAK

/**
 * SoftRef that is able to reconstruct its value if
 * it has been collected.
 *
 * The value is rebuilt only when the methods `get` or `flatMap` are called
 * or when converting it into a hard reference.
 *
 * @param builder the function that should be used to rebuild a lost value
 */
private class RebuildingWeakRef[+A] private (private val builder: () => A, private var ref: WeakReference[A @uncheckedVariance]) extends Ref[A, WEAK] {

  // CONSTRUCTORS
  def this(builder: () => A) = this(builder, new WeakReference(null.asInstanceOf[A]))
  def this(builder: () => A, value: A) = this(builder, new WeakReference(value))

  def refType = WEAK

  // ACCESSORS
  def apply = Option(get)
  def get = ref.get match {
    case null =>
      val value = builder()
      ref = new WeakReference(value)
      value
    case value => value
  }
  def exists = ref.get != null
  def getOrElse[B >: A](default: => B) = get
  def orElse[B >: A](alternative: => Ref[B, WEAK]) = this

  // MONADICS
  def map[B](f: A => B) = {
    val b = () => f(builder())
    ref.get match {
      case null => new RebuildingWeakRef(b)
      case x => new RebuildingWeakRef(b, f(x))
    }
  }
  def flatMap[B](f: A => Ref[B, WEAK]) = f(get)

  // CONVERSION FUNCTION
  def toHard = Ref.hard(builder, get)
  def toSoft = ref.get match {
    case null => Ref.soft(builder, false)
    case x => Ref.soft(builder, x)
  }
  def toWeak = this

  override def stripped(update: Boolean) = Ref.weak(if (update) get else ref.get)

}