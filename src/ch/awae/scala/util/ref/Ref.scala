package ch.awae.scala.util
package ref

import java.util.NoSuchElementException

import ch.awae.scala.util.ref.RefType.HARD
import ch.awae.scala.util.ref.RefType.SOFT
import ch.awae.scala.util.ref.RefType.WEAK

/**
 * Common base trait for all reference types. References are
 * created by the companion object [[Ref$ Ref]]. All references are read-only,
 * however immutability of the referenced object can obviously
 * not be ensured.
 *
 * @tparam A the data type of the referenced object (covariant)
 * @tparam T the [[RefType]] of the reference
 *
 * @define RECOVER For auto-recovering references this recreates the reference
 * if it does not exist.
 */
trait Ref[+A, T <: RefType[T]] extends (() => Option[A]) {

  /**
   * the companion object to the reference type of this reference
   * @return the companion object to the reference type of this reference
   */
  def refType: RefType[T]

  /**
   * retrieves the referenced object if it exists.
   *
   * @note $RECOVER
   *
   * @return a `Some` with the referenced object or `None` iff
   * the object does not exist and cannot be recreated.
   */
  override def apply(): Option[A]

  // ACCESSORS 

  /**
   * retrieves the referenced object. If it does not exist an exception is thrown.
   *
   * @note $RECOVER
   *
   * @return the referenced object if it exists
   * @throws NoSuchElementException if the reference is empty
   */
  @throws[NoSuchElementException]("if the reference is empty")
  def get: A

  /**
   * retrieves the referenced object or returns a default value if the referenced
   * does not exist.
   *
   * @note $RECOVER Therefore the `default` value is ignored.
   *
   * @tparam B the type of the default value
   * @param default the value to be used if the referenced object does not exist
   * @return the referenced object or the `default` value iff the referenced object
   * does not exist
   */
  def getOrElse[B >: A](default: => B): B

  /**
   * returns this reference or an `alternative` iff this reference references an
   * object that does not exist.
   *
   * @note for auto-recovering references this ''always'' returns the reference
   * itself. The `alternative` is ignored.
   *
   * @tparam B the object type of the `alternative` reference
   * @param alternative an alternative reference to be returned if this references
   * an object that does not exist
   */
  def orElse[B >: A](alternative: => Ref[B, T]): Ref[B, T]

  /**
   * Indicates if the referenced object exists.
   *
   * For auto-recovering references this indicates the actual state of the
   * reference. Therefore if this returns `false` the next method that can
   * recreate references ''will'' recreate the reference.
   *
   * @return `true` if the referenced object exists, `false` otherwise.
   */
  def exists: Boolean

  // MONADIC FUNCTIONS
  /**
   * transforms the reference by applying a given function to the referenced
   * object if it exists. The result from that function application is returned
   * inside of a new reference.
   *
   * @note for auto-recovering references this does not recreate the referenced
   * object and uses lazy object creation in the resulting reference object.
   *
   * @tparam B the type of the resulting reference
   * @param f the transformation function to be applied
   * @return a new reference containing the result of the application of the
   * function `f` or this reference if it references an object that does not
   * exist.
   */
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