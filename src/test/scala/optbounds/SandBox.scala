package optbounds

import com.github.tnielens.intervalops.{BoundedInterval, Interval}

import java.time.LocalDateTime

object SandBox extends App {
  import Interval.Ops

  type OptBounds = (Option[Int], Option[Int])
  println(((Some(5), None): OptBounds) overlap ((Option(7), Option(134))))
  println(((Some(5), None): OptBounds) diff ((Option(7), Option(134))))

  type OptEndBound = (Int, Option[Int])
  println(((5, None): OptEndBound) overlap ((7, Option(134))))
  println(((5, None): OptEndBound) diff ((7, Option(134))))

  implicit val dtOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_.isBefore(_))
  val now = LocalDateTime.now
  println(now)
  println((now, now.plusMinutes(20)) overlap (now.plusMinutes(10), now.plusMinutes(30)))

  case class CustomInterval[B](start: B, end: B)

  implicit def customIntervalInst[B](implicit ob: Ordering[B]) =
    new BoundedInterval[CustomInterval[B], B] {
      override implicit val orderableBoundOrdering: Ordering[B] = ob
      override def start(i: CustomInterval[B]): B = i.start
      override def end(i: CustomInterval[B]): B = i.end
      override def create(s: B, e: B): CustomInterval[B] =
        CustomInterval(start = s, end = e)
    }

  assert(CustomInterval(1, 6) contains 5)
}
