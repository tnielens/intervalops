package com.github.tnielens.intervalops

import Ordering.Implicits._

/**
  * Represents an optional bound.
  * Option[Bound] can't be used as is, we must encode whether `None` means the min or max value.
  */
sealed trait OptBound[+B] {
  import OptBound._
  def toOpt: Option[B] = this match {
    case Bounded(v) => Some(v)
    case Min | Max => None
  }

  def toOptStart: Option[B] = this match {
    case Bounded(b) => Some(b)
    case OptBound.Min => None
    case OptBound.Max => throw new AssertionError("the start bound can't be the max value")
  }

  def toStart: B = toOptStart match {
    case Some(v) => v
    case None => throw new AssertionError("the start must be bounded")
  }

  def toOptEnd: Option[B] = this match {
    case Bounded(b) => Some(b)
    case OptBound.Min => throw new AssertionError("the end bound can't be the min value")
    case OptBound.Max => None
  }

  def toEnd: B = toOptEnd match {
    case Some(v) => v
    case None => throw new AssertionError("the end must be bounded")
  }

  def toBase: B = this match {
    case Bounded(b) => b
    case OptBound.Min | OptBound.Max =>
      throw new AssertionError("the base bound can't encode the min or max value")
  }
}

object OptBound {
  final case class Bounded[B](b: B) extends OptBound[B]
  object Min extends OptBound[Nothing]
  object Max extends OptBound[Nothing]

  implicit def optBoundOrdering[B: Ordering]: Ordering[OptBound[B]] =
    Ordering.fromLessThan((ob1, ob2) =>
      (ob1, ob2) match {
        case (Min, Min) => false
        case (Min, Bounded(_) | Max) => true
        case (Bounded(b1), Bounded(b2)) => b1 < b2
        case (Bounded(_), Min) => false
        case (Bounded(_), Max) => true
        case (Max, Min | Bounded(_) | Max) => false
      }
    )

  implicit class Convs[B](private val v: Option[B]) extends AnyVal {
    def startToOptBound: OptBound[B] =
      v.fold[OptBound[B]](OptBound.Min)(OptBound.Bounded.apply)
    def endToOptBound: OptBound[B] =
      v.fold[OptBound[B]](OptBound.Max)(OptBound.Bounded.apply)
  }
}
