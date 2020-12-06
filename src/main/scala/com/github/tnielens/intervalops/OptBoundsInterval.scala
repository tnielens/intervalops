package com.github.tnielens.intervalops

import OptBound.Convs

trait OptBoundsInterval[I, B] extends IntervalCommonImpl[I] {
  override type StartBound = Option[B]
  override type EndBound = Option[B]
  override type OrderableBound = OptBound[B]
  override type BaseBound = B
  override def startToOrderable(s: Option[B]): OptBound[B] = s.startToOptBound
  override def endToOrderable(e: Option[B]): OptBound[B] = e.endToOptBound
  override def baseToOrderable(b: B): OptBound[B] = OptBound.Bounded(b)
  override def orderableToStart(c: OptBound[B]): Option[B] = c.toOptStart
  override def orderableToEnd(c: OptBound[B]): Option[B] = c.toOptEnd
  override def orderableToBase(c: OptBound[B]): B = c.toBase
}
