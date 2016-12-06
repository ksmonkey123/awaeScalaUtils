package ch.awae.scala.util

import scala.reflect.ClassTag

import ch.awae.scala.util.ref.Ref
import ch.awae.scala.util.ref.RefType
import ch.awae.scala.util.ref.RefType._

/**
 * == Unified Reference Model for Scala ==
 *
 * There are 3 different supported types of references:
 *   - Hard Reference (will not be cleared by GC)
 *   - Soft Reference (will only be cleared when absolutely necessary)
 *   - Weak Reference (can be cleared on any GC run)
 *
 * Normal references can't be recovered once they have been cleared.
 * In addition there are also implementations for all reference types
 * that carry a supplier function that is used internally to rebuild
 * the reference if it has been cleared. This can be very useful for
 * cache implementations.
 *
 * All reference types can be converted freely into all other reference
 * types. For the auto-recovering references these conversions may be
 * done lazily.
 *
 * References can be transformed using the `map` and `flatMap` functions.
 * Auto-recovering references keep track of all mapping functions.
 * Therefore a reference can be recovered even if any or all of its
 * "parents" have been cleared.
 */
package object ref {

  /**
   * Implicit Wrapper providing reference creation functionality to any object
   *
   * @constructor creates a new [[ReferenceDecoration]] instance
   * @tparam A the type of the decorated object
   * @param item the object to decorate
   */
  implicit class ReferenceDecoration[A](private val item: A) extends AnyVal {

    /**
     * @usecase def asRef[T <: RefType[T]]: Ref[A, T]
     *
     * creates a reference of the given type containing the object.
     * The reference type is passed as a type parameter.
     *
     * @tparam T the type of the reference to create
     *
     * @example {{{
     * import ref._
     * import ref.RefType._
     *
     * val x = List(1, 2, 3)
     * val h = x.asRef[HARD]
     * }}}
     */
    def asRef[T <: RefType[T]: ClassTag]: Ref[A, T] = {
      val clazz = implicitly[ClassTag[T]].runtimeClass

      (if (clazz equals classOf[HARD])
        Ref.hard(item)
      else if (clazz equals classOf[SOFT])
        Ref.soft(item)
      else if (clazz equals classOf[WEAK])
        Ref.weak(item)
      else ???).asInstanceOf[Ref[A, T]]
    }

  }
}