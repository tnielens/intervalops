package com.github.tnielens.intervalops
import Ordering.Implicits._

trait IntervalCommonImpl[I] extends Interval[I] {
  // orderable repr used for comparison
  type OrderableBound
  implicit val orderableBoundOrdering: Ordering[OrderableBound]

  def startToOrderable(s: StartBound): OrderableBound
  def orderableToStart(c: OrderableBound): StartBound
  def endToOrderable(e: EndBound): OrderableBound
  def orderableToEnd(c: OrderableBound): EndBound
  def baseToOrderable(c: BaseBound): OrderableBound
  def orderableToBase(c: OrderableBound): BaseBound

  override def contains(i: I, b: BaseBound): Boolean =
    start(i).toCommon <= b.toCommon && b.toCommon < end(i).toCommon

  override def containsI(i0: I, i1: I): Boolean =
    start(i0).toCommon <= start(i1).toCommon && end(i1).toCommon <= end(i0).toCommon

  override def overlap(i0: I, i1: I): Option[I] = {
    val cbo = orderableBoundOrdering
    if (start(i0).toCommon < end(i1).toCommon &&
      start(i1).toCommon < end(i0).toCommon)
      Option(
        create(
          cbo.max(start(i0).toCommon, start(i1).toCommon).toStart,
          cbo.min(end(i0).toCommon, end(i1).toCommon).toEnd
        )
      )
    else None
  }

  override def adjacency(i0: I, i1: I): Option[BaseBound] =
    if (end(i0).toCommon equiv start(i1).toCommon) Option(end(i0).toCommon.toAdjacent)
    else if (end(i1).toCommon equiv start(i0).toCommon) Option(end(i1).toCommon.toAdjacent)
    else None

  override def diff(i0: I, i1: I): (Option[I], Option[I]) = {
    val firstPart: Option[I] =
      if (start(i0).toCommon < start(i1).toCommon)
        Option(create(start(i0), start(i1).toCommon.toEnd))
      else None
    val secondPart =
      if (end(i1).toCommon < end(i0).toCommon)
        Option(create(end(i1).toCommon.toStart, end(i0)))
      else None
    (firstPart, secondPart)
  }

  override def gap(i0: I, i1: I): Option[I] =
    if (end(i0).toCommon < start(i0).toCommon)
      Option(create(end(i0).toCommon.toStart, start(i1).toCommon.toEnd))
    else if (end(i1).toCommon < start(i0).toCommon)
      Option(create(end(i1).toCommon.toStart, start(i0).toCommon.toEnd))
    else None

  implicit class StartBoundOps(private val s: StartBound) {
    def toCommon: OrderableBound = startToOrderable(s)
  }
  implicit class EndBoundOps(private val e: EndBound) {
    def toCommon: OrderableBound = endToOrderable(e)
  }
  implicit class BaseBoundOps(private val b: BaseBound) {
    def toCommon: OrderableBound = baseToOrderable(b)
  }
  implicit class CommonBoundOps(private val c: OrderableBound) {
    def toStart: StartBound = orderableToStart(c)
    def toEnd: EndBound = orderableToEnd(c)
    def toAdjacent: BaseBound = orderableToBase(c)
  }
}
