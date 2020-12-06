package com.github.tnielens.intervalops

trait Interval[I] {

  type BaseBound
  // `StartBound` and `EndBound` are `BaseBound` or `Option[BaseBound]`
  type StartBound
  type EndBound

  // these methods' implementation must be provided by the typeclass instances
  def start(i: I): StartBound
  def end(i: I): EndBound
  def create(s: StartBound, e: EndBound): I

  // The following methods implementation is based on the precedent methods
  // or provided by the subtraits BoundedInterval, OptStartBoundInterval, etc

  def withStart(i: I, s: StartBound): I = create(s, end(i))

  def withEnd(i: I, e: EndBound): I = create(start(i), e)

  def contains(i: I, b: BaseBound): Boolean

  def containsI(i0: I, i1: I): Boolean

  def overlap(i0: I, i1: I): Option[I]

  /**
    * Adjacent intervals don't overlap. Example:
    * {{{
    *   ((0, 2) adjacency (2, 4)) == Option(2)
    *   ((0, 2) adjacency (1, 3)) == None
    * }}}
    * @return `Some[BaseBound]` if intervals abut, else `None`
    */
  def adjacency(i0: I, i1: I): Option[BaseBound]

  def diff(i0: I, i1: I): (Option[I], Option[I])

  def gap(i0: I, i1: I): Option[I]
}

object Interval extends IntervalInstances {
  type Aux[I, B, S, E] = Interval[I] {
    type BaseBound = B; type StartBound = S; type EndBound = E
  }

  def apply[I](
      implicit inst: Interval[I]
    ): Interval.Aux[I, inst.BaseBound, inst.StartBound, inst.EndBound] =
    inst

  implicit class Ops[I, B, S, E](private val v: I)(implicit val inst: Interval.Aux[I, B, S, E]) {
    def start: S = inst.start(v)
    def withStart(s: S) = inst.withStart(v, s)
    def end: E = inst.end(v)
    def withEnd(e: E) = inst.withEnd(v, e)

    def contains(other: B): Boolean = inst.contains(v, other)
    def containsI(other: I): Boolean = inst.containsI(v, other)
    def overlap(other: I): Option[I] = inst.overlap(v, other)
    def diff(other: I): (Option[I], Option[I]) = inst.diff(v, other)
    def adjacency(other: I): Option[B] = inst.adjacency(v, other)
    def gap(other: I): Option[I] = inst.gap(v, other)
  }
}
