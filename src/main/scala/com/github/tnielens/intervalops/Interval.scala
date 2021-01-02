package com.github.tnielens.intervalops
import com.github.tnielens.intervalops.OptBound.{Bound, OptEndBound, OptStartBound}
import Ordering.Implicits._

/**
 * Interval operations type class. Usual inclusive-start, exclusive-end semantic.
 */
trait Interval[I] {

  type BaseBound
  type StartBound >: Bound[BaseBound] <: OptStartBound[BaseBound]
  type EndBound >: Bound[BaseBound] <: OptEndBound[BaseBound]

  // these methods implementation must be provided by the typeclass instances
  def start(i: I): StartBound
  def end(i: I): EndBound
  def create(s: StartBound, e: EndBound): I

  implicit val ordS: Ordering[StartBound]
  implicit val ordE: Ordering[EndBound]
  implicit val ordB: Ordering[BaseBound]

  // the following methods implementation is based on the precedent ones
  def withStart(i: I, s: StartBound): I = create(s, end(i))

  def withEnd(i: I, e: EndBound): I = create(start(i), e)

  def contains(i: I, b: BaseBound): Boolean = start(i) <= Bound(b) && (Bound(b): EndBound) < end(i)

  def containsI(i1: I, i2: I): Boolean = {
    ordS.lteq(start(i1), start(i2)) && ordE.lt(end(i2), end(i1))
  }

  def overlap(i1: I, i2: I): Option[I] = {
    val b1 = ordS.max(start(i1), start(i2))
    val b2 = ordE.min(end(i1), end(i2))
    val b1BeforeB2 = (b1, b2) match {
      case (b1: Bound[BaseBound], b2: Bound[BaseBound]) => b1 < b2
      case _ => true
    }
    if (b1BeforeB2) Some(create(b1, b2)) else None
  }

  /**
    * Adjacent intervals don't overlap. Example:
    * {{{
    *   ((0, 2) adjacency (2, 4)) == Option(2)
    *   ((0, 2) adjacency (1, 3)) == None
    * }}}
    * @return `Some[BaseBound]` if intervals abut, else `None`
    */
  def adjacency(i1: I, i2: I): Option[BaseBound] = {
    def isAdjacent(e: EndBound, s: StartBound): Option[BaseBound] =
      (e, s) match {
        case (Bound(e), Bound(s)) if ordB.equiv(e, s) => Option(e)
        case _ => None
      }
    isAdjacent(end(i1), start(i2)).orElse(isAdjacent(end(i2), start(i1)))
  }

  def diff(i1: I, i2: I): (Option[I], Option[I]) = overlap(i1, i2) match {
    case Some(_) =>
      val firstPart = (start(i1), start(i2)) match {
        case (s1, s2: Bound[BaseBound]) if ordS.lt(s1, s2) => Option(create(s1, s2))
        case _ => None
      }
      val secondPart =
        (end(i1), end(i2)) match {
          case (e1, e2: Bound[BaseBound]) if ordE.lt(e2, e1) => Option(create(e2, e1))
          case _ => None
        }
      (firstPart, secondPart)
    case None =>
      (None, None)
  }

  def gap(i1: I, i2: I): Option[I] = {
    def hasGap(e: EndBound, s: StartBound): Option[I] = (e, s) match {
      case (Bound(e), Bound(s)) if ordB.lt(e, s) =>
        Option(create(Bound(e), Bound(s)))
      case _ => None
    }
    hasGap(end(i1), start(i2)).orElse(hasGap(end(i2), start(i1)))
  }
}

object Interval extends LowPrioIntervalInstances {
  type Aux[I, B, S, E] = Interval[I] { type BaseBound = B; type StartBound = S; type EndBound = E }

  implicit class Ops[I, B, S, E](private val v: I)(implicit val I: Interval.Aux[I, B, S, E]) {
    def start: S = I.start(v)
    def withStart(s: S) = I.withStart(v, s)
    def end: E = I.end(v)
    def withEnd(e: E) = I.withEnd(v, e)

    def contains(other: B): Boolean = I.contains(v, other)
    def containsI(other: I): Boolean = I.containsI(v, other)
    def overlap(other: I): Option[I] = I.overlap(v, other)
    def diff(other: I): (Option[I], Option[I]) = I.diff(v, other)
    def adjacency(other: I): Option[B] = I.adjacency(v, other)
    def gap(other: I): Option[I] = I.gap(v, other)
  }

  def instance[I, B, S >: Bound[B] <: OptStartBound[B], E >: Bound[B] <: OptEndBound[B]](
      start0: I => S,
      end0: I => E,
      create0: (S, E) => I
    )(
      implicit ob: Ordering[B],
      os: Ordering[S],
      oe: Ordering[E]
    ) = new Interval[I] {
    override type StartBound = S
    override type EndBound = E
    override type BaseBound = B
    override def start(i: I): S = start0(i)
    override def end(i: I): EndBound = end0(i)
    override def create(s: S, e: E): I = create0(s, e)
    override implicit val ordB: Ordering[B] = ob
    override implicit val ordS: Ordering[S] = os
    override implicit val ordE: Ordering[E] = oe
  }

  implicit def optStartBoundTupleInterval[T: Ordering] =
    instance[(Option[T], T), T, OptStartBound[T], Bound[T]](
      t => OptStartBound.fromOpt(t._1),
      t => Bound(t._2),
      (b1, b2) => (b1.toOpt, b2.v)
    )

  implicit def optEndBoundTupleInterval[T: Ordering] =
    instance[(T, Option[T]), T, Bound[T], OptEndBound[T]](
      t => Bound(t._1),
      t => OptEndBound.fromOpt(t._2),
      (b1, b2) => (b1.v, b2.toOpt)
    )

  implicit def optBoundsTupleInterval[T: Ordering] =
    instance[(Option[T], Option[T]), T, OptStartBound[T], OptEndBound[T]](
      t => OptStartBound.fromOpt(t._1),
      t => OptEndBound.fromOpt(t._2),
      (b1, b2) => (b1.toOpt, b2.toOpt)
    )
}

trait LowPrioIntervalInstances {
  implicit def tupleInterval[T: Ordering] =
    Interval.instance[(T, T), T, Bound[T], Bound[T]](
      t => Bound(t._1),
      t => Bound(t._2),
      (b1, b2) => (b1.v, b2.v)
    )
}
