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
 * @version 1.1
 * @author Andreas Wälchli (andreas.waelchli@me.com)
 *
 * @tparam A the data type of the referenced object (covariant)
 * @tparam T the [[RefType]] of the reference
 *
 * @define RECOVER For auto-recovering references this recreates the reference
 * if it does not exist.
 * @define CONVERSION If this reference is already of the requested reference
 * type this reference is directly returned. If this reference is auto-recovering
 * the recovery function is retained in the conversion result.
 */
trait Ref[+A, T <: RefType[T]] extends (() => Option[A]) {

  /**
   * the companion object to the reference type of this reference
   *
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
   * @note this method cannot be used for existence checks on the
   * referenced object since any further operation is not atomic and therefore
   * GC may clear the reference in the meantime. If existence of the object is
   * relevant use the following implementations using [[Ref.apply apply]]:
   * {{{
   * ref.apply().foreach(x => /* code here */)
   * ref().foreach(x => /* code here */)
   * }}}
   *
   * @return `true` if the referenced object exists, `false` otherwise.
   */
  def exists: Boolean

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

  /**
   * applies a given function to the referenced object and returns the result
   * of that operation.
   *
   * If this reference references an object that does not exist, this reference
   * is returned.
   *
   * @note $RECOVER
   *
   * @tparam B the type of the resulting reference
   * @param f the function to be applied
   * @return the result of applying `f` to the referenced object or `this` if
   *   			 the referenced object does not exist
   */
  def flatMap[B](f: A => Ref[B, T]): Ref[B, T]

  /**
   * converts this into a [[RefType.HARD HARD]] reference.
   *
   * If this is an auto-recovering reference and the referenced object currently
   * does not exist this recovers the object in both this reference and the
   * returned reference.
   *
   * @note $CONVERSION
   *
   * @return a hard reference generated from this reference
   */
  def toHard: Ref[A, HARD]

  /**
   * converts this into a [[RefType.SOFT SOFT]] reference.
   *
   * @note $CONVERSION
   *
   * @return a soft reference generated from this reference
   */
  def toSoft: Ref[A, SOFT]

  /**
   * converts this into a [[RefType.WEAK WEAK]] reference.
   *
   * @note $CONVERSION
   *
   * @return a weak reference generated from this reference
   */
  def toWeak: Ref[A, WEAK]

  /**
   * converts this into a reference that is stripped of all
   * secondary data.
   *
   * Here special rules apply to different reference types:
   *   - if `this` is a normal reference, `this` is directly returned
   *   - if `this` is an auto-recovering reference, the reference
   *   		is recovered iff `update==true` and the object referenced by
   *      this reference does not exist. The referenced object
   *      is packed into a normal reference of the same type.
   *
   * @note forcing a reference update by setting `update` to `true`
   * does not guarantee that the object referenced by the resulting
   * reference does actually exist. It is entirely possible for GC
   * to clear the newly generated object at any time - even before
   * the reference is actually returned.
   *
   * @param update indicates whether or not the reference should be updated
   * before stripping it of the secondary data.
   *
   * @return a new basic reference of the same type referencing the same
   * object (if it exists) as this reference.
   */
  def stripped(update: Boolean = false): Ref[A, T] = this

  /**
   * @usecase def foreach(f: A => Unit): Unit
   * 
   * applies a given function to the referenced object if it exists.
   * 
   * @note $RECOVER
   * 
   * @param f the function to apply
   */
  def foreach[U](f: A => U): Unit = apply() foreach f
}

/**
 * Factory object for creating [[Ref! Ref]] instances
 * @define TPAR the type of the referenced object
 */
object Ref {

  /**
   * creates a hard reference for a given value
   *
   * @tparam A $TPAR
   * @param value the object to create the reference for
   * @return a hard reference for the given `value`
   */
  def hard[A](value: A): Ref[A, HARD] = new HardRef(value)

  /**
   * creates an auto-recovering hard reference for a given supplier function.
   *
   * The function is evaluated and the return value is referenced. The
   * reference also contains the function itself for recovery if it is
   * ever converted into a soft or weak reference.
   *
   * @tparam A $TPAR
   * @param f the supplier function
   * @return an auto-recovering hard reference holding the function `f`
   * and its return value
   */
  def hard[A](f: () => A): Ref[A, HARD] = hard(f(), f)

  /**
   * creates an auto-recovering hard reference for a given value and supplier
   * function.
   *
   * The supplier function should return the same value as the one provided, this
   * is however not checked since the function is not evaluated.
   *
   * @note consider using [[hard[A](f:()=>A):* hard[A](f: () => A)]] instead to
   * calculate the value from the function directly
   *
   * @tparam A $TPAR
   * @param f the supplier function
   * @param value the value
   * @return an auto-recovering hard reference holding the value `value`
   * and the recovery function `f`
   */
  def hard[A](value: A, f: () => A): Ref[A, HARD] =
    new RebuildingHardRef(f, value)

  /**
   * creates a soft reference for a given value
   *
   * @tparam A $TPAR
   * @param value the object to create the reference for
   * @return a soft reference for the given `value`
   */
  def soft[A](value: A): Ref[A, SOFT] = new SoftRef(value)

  /**
   * creates an auto-recovering soft reference for a given supplier function.
   *
   * If the parameter `force` is `true` the function is evaluated and the
   * return value is referenced. The reference also contains the function
   * itself for recovery. If `force` is `false` an empty reference just holding
   * the recovery function is created
   *
   * @note in most cases it is not necessary to evaluate the function when creating
   * the reference as the value will automatically be created when it is first requested.
   * In some cases where i.e. the generation is expensive forcing initialisation may
   * provide some performance benefits.
   *
   * @tparam A $TPAR
   * @param f the supplier function
   * @param force indicates whether or not the function should be evaluated when
   * creating the reference. By default this is `false`
   * @return an auto-recovering soft reference holding the function `f`
   * and optionally its return value
   */
  def soft[A](f: () => A, force: Boolean = false): Ref[A, SOFT] =
    if (force) new RebuildingSoftRef(f, f())
    else new RebuildingSoftRef(f)

  /**
   * creates an auto-recovering soft reference for a given value and supplier
   * function.
   *
   * The supplier function should return the same value as the one provided, this
   * is however not checked since the function is not evaluated.
   *
   * @note consider using [[soft[A](f:()=>A,force:Boolean):* soft[A](f: () => A, force:Boolean)]] instead to
   * calculate the value from the function directly: {{{
   * Ref.soft(f, true)
   * }}}
   *
   * @tparam A $TPAR
   * @param f the supplier function
   * @param value the value
   * @return an auto-recovering soft reference holding the value `value`
   * and the recovery function `f`
   */
  def soft[A](value: A, f: () => A): Ref[A, SOFT] =
    new RebuildingSoftRef(f, value)

  /**
   * creates a weak reference for a given value
   *
   * @tparam A $TPAR
   * @param value the object to create the reference for
   * @return a weak reference for the given `value`
   */
  def weak[A](value: A): Ref[A, WEAK] = new WeakRef(value)

  /**
   * creates an auto-recovering weak reference for a given supplier function.
   *
   * If the parameter `force` is `true` the function is evaluated and the
   * return value is referenced. The reference also contains the function
   * itself for recovery. If `force` is `false` an empty reference just holding
   * the recovery function is created
   *
   * @note in most cases it is not necessary to evaluate the function when creating
   * the reference as the value will automatically be created when it is first requested.
   * In some cases where i.e. the generation is expensive forcing initialisation may
   * provide some performance benefits.
   *
   * @tparam A $TPAR
   * @param f the supplier function
   * @param force indicates whether or not the function should be evaluated when
   * creating the reference. By default this is `false`
   * @return an auto-recovering weak reference holding the function `f`
   * and optionally its return value
   */
  def weak[A](f: () => A, force: Boolean = false): Ref[A, WEAK] =
    if (force) new RebuildingWeakRef(f, f())
    else new RebuildingWeakRef(f)

  /**
   * creates an auto-recovering soft reference for a given value and supplier
   * function.
   *
   * The supplier function should return the same value as the one provided, this
   * is however not checked since the function is not evaluated.
   *
   * @note consider using [[weak[A](f:()=>A,force:Boolean):* weak[A](f: () => A, force:Boolean)]] instead to
   * calculate the value from the function directly: {{{
   * Ref.weak(f, true)
   * }}}
   *
   * @tparam A $TPAR
   * @param f the supplier function
   * @param value the value
   * @return an auto-recovering soft reference holding the value `value`
   * and the recovery function `f`
   */
  def weak[A](value: A, f: () => A): Ref[A, WEAK] =
    new RebuildingWeakRef(f, value)
}