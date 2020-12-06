package com.github.tnielens.intervalops

import OptBound.Convs

trait OptStartBoundInterval[I, B] extends IntervalCommonImpl[I] {
  override type StartBound = Option[B]
  override type EndBound = B
  override type OrderableBound = OptBound[B]
  override type BaseBound = B
  override def startToOrderable(s: Option[B]): OptBound[B] = s.startToOptBound
  override def endToOrderable(e: B): OptBound[B] = OptBound.Bounded(e)
  override def baseToOrderable(b: B): OptBound[B] = OptBound.Bounded(b)
  override def orderableToStart(c: OptBound[B]): Option[B] = c.toOptStart
  override def orderableToEnd(c: OptBound[B]): B = c.toEnd
  override def orderableToBase(c: OptBound[B]): B = c.toBase
}
