package com.github.tnielens.intervalops

private[intervalops] trait IntervalInstances extends IntervalInstances1 {
  implicit def tuple2OptBoundsInst[B](
      implicit O: Ordering[B]
    ): OptBoundsInterval[(Option[B], Option[B]), B] = {
    type T2 = (Option[B], Option[B])
    new OptBoundsInterval[T2, B] {
      override def start(i: T2): Option[B] = i._1
      override def end(i: T2): Option[B] = i._2
      override def create(s: Option[B], e: Option[B]): (Option[B], Option[B]) =
        (s, e)
      override implicit val orderableBoundOrdering: Ordering[OptBound[B]] =
        OptBound.optBoundOrdering
    }
  }

  implicit def tuple2OptEndBoundInst[B](
      implicit O: Ordering[B]
    ): OptEndBoundInterval[(B, Option[B]), B] = {
    type T2 = (B, Option[B])
    new OptEndBoundInterval[T2, B] {
      override def start(i: T2): B = i._1
      override def end(i: T2): Option[B] = i._2
      override def create(s: B, e: Option[B]): T2 =
        (s, e)
      override implicit val orderableBoundOrdering: Ordering[OptBound[B]] =
        OptBound.optBoundOrdering
    }
  }
}
