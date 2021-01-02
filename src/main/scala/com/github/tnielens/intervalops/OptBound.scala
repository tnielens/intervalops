package com.github.tnielens.intervalops
import Ordering.Implicits._

sealed trait OptBound[+T] {
  import OptBound._
  def toOpt: Option[T] = this match {
    case Min | Max => None
    case Bound(v) => Some(v)
  }
}

object OptBound {
  sealed trait OptStartBound[+T] extends OptBound[T]
  object OptStartBound {
    implicit def ordering[T: Ordering]: Ordering[OptStartBound[T]] = Ordering.fromLessThan {
      case (Min, Min) => false
      case (Min, _) => true
      case (_, Min) => false
      case (Bound(b1), Bound(b2)) => b1 < b2
    }
    def fromOpt[T](o: Option[T]): OptStartBound[T] = o match {
      case Some(v) => Bound(v)
      case None => Min
    }
  }

  sealed trait OptEndBound[+T] extends OptBound[T]
  object OptEndBound {
    implicit def ordering[T: Ordering]: Ordering[OptEndBound[T]] = Ordering.fromLessThan {
      case (Max, Max) => false
      case (Max, _) => false
      case (_, Max) => true
      case (Bound(b1), Bound(b2)) => b1 < b2
    }
    def fromOpt[T](o: Option[T]): OptEndBound[T] = o match {
      case Some(v) => Bound(v)
      case None => Max
    }
  }

  final case class Bound[T](v: T) extends OptStartBound[T] with OptEndBound[T]
  object Bound {
    implicit def ordering[T: Ordering]: Ordering[Bound[T]] = Ordering.by(_.v)
  }

  object Min extends OptStartBound[Nothing]
  object Max extends OptEndBound[Nothing]
}