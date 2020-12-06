package com.github.tnielens.intervalops

import OptBound.Convs

trait OptEndBoundInterval[I, B] extends IntervalCommonImpl[I] {
  override type StartBound = B
  override type EndBound = Option[B]
  override type OrderableBound = OptBound[B]
  override type BaseBound = B
  override def startToOrderable(s: B): OptBound[B] = OptBound.Bounded(s)
  override def endToOrderable(e: Option[B]): OptBound[B] = e.endToOptBound
  override def baseToOrderable(b: B): OptBound[B] = OptBound.Bounded(b)
  override def orderableToStart(c: OptBound[B]): B = c.toStart
  override def orderableToEnd(c: OptBound[B]): Option[B] = c.toOptEnd
  override def orderableToBase(c: OptBound[B]): B = c.toBase
}
