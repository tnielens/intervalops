package com.github.tnielens.intervalops

/**
  * Bounded version of Interval[I].
  * The same type is used for `StartBound`, `EndBound` and `BaseBound`.
  */
trait BoundedInterval[I, B] extends IntervalCommonImpl[I] {
  override type BaseBound = B
  override type StartBound = B
  override type EndBound = B
  override type OrderableBound = B
  override def startToOrderable(b: B): B = b
  override def endToOrderable(b: B): B = b
  override def baseToOrderable(b: B): B = b
  override def orderableToStart(b: B): B = b
  override def orderableToEnd(b: B): B = b
  override def orderableToBase(b: B): B = b
}
