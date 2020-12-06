package com.github.tnielens.intervalops

private[intervalops] trait IntervalInstances1 {
  implicit def tuple2Inst[B](implicit O: Ordering[B]) =
    new BoundedInterval[(B, B), B] {
      override def start(i: (B, B)): B = i._1
      override def end(i: (B, B)): B = i._2
      override def create(s: B, e: B): (B, B) = (s, e)
      override implicit val orderableBoundOrdering: Ordering[B] = O
    }
}
