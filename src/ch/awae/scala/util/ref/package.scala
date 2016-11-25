package ch.awae.scala.util

import scala.reflect.ClassTag

import ch.awae.scala.util.ref.Ref
import ch.awae.scala.util.ref.RefType
import ch.awae.scala.util.ref.RefType._

package object ref {

  implicit class ReferenceDecoration[A](val item: A) extends AnyVal {

    /**
     * creates a reference of the given type containing the object.
     * 
     * {{{
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