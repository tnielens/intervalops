# IntervalOps

An `Interval[I]` typeclass that, given an implementation of `start`, `end` and
an ordering of the bound type, provides default implementation for common
interval-like operations (`contains`, `overlap` , `gap`, etc). Optional bounds
are supported.

Given an `Ordering[B]`, instances are provided for

* `(B, B)`
* `(Option[B], B)`
* `(B, Option[B])`
* `(Option[B], Option[B])`

In the optional bound cases, `None` represents the minimum value in start
position or the maximum value in end position.

## Usage example

Working with time:

```scala
import com.github.tnielens.intervalops.IntervalOps
import org.joda.time.DateTime

implicit val dtOrdering: Ordering[DateTime] = Ordering.fromLessThan(_.isBefore(_))
val t0 = DateTime.now
val t1 = t0.plusMinutes(5)
val t2 = t1.plusMinutes(5)
val t3 = t2.plusMinutes(5)

assert(((t0, t3) overlap(t1, t2)) == (t1, t2))
assert(((t0, t3) diff(t1, t2)) == (Option((t0, t1)), Option((t2, t3))))
```

`Interval` works for any base boundary type given an `Ordering[B]`. Instances
for primitive types (`Ordering[Int]`, `Ordering[Double]`, etc) are provided by
the std library.

```scala
import com.github.tnielens.intervalops.IntervalOps

assert(((0, 10) overlap (5, 15)) == (5, 10))
assert(((0, 10) diff (6, 7)) == (Option((0, 6), Option((7, 10)))
```

## How to create an instance of `Interval[I]` for a custom type

```scala
import com.github.tnielens.intervalops.Interval
import com.github.tnielens.intervalops.IntervalOps

case class CustomInterval[B](start: B, end: B)

// if possible, put the instance definition in the companion object
// of the `CustomInterval` to avoid the import tax
implicit def customIntervalInst[B](implicit ob: Ordering[B]): Interval[CustomInterval[B]] =
  new BoundedInterval[CustomInterval[B], B] {
    override implicit val orderableBoundOrdering: Ordering[B] = ob
    override def start(i: CustomInterval[B]): B = i.start
    override def end(i: CustomInterval[B]): B = i.end
    override def create(s: B, e: B): CustomInterval[B] =
      CustomInterval(start = s, end = e)
}

assert(CustomInterval(1, 6) contains 5)
```

For cases involving optional bounds, see `OptStartBoundInterval`
, `OptEndBoundInterval` or `OptBoundsInterval`.
