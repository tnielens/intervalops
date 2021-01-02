# IntervalOps

An `Interval[I]` typeclass that, given an implementation of the methods `start`
, `end` and an ordering of the base bound type, provides default implementation
for common interval-like operations (`contains`, `overlap` , `gap`, etc).
Optional bounds are supported.

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
import com.github.tnielens.intervalops.Interval.Ops
import java.time.Instant

implicit val instanctOrdering: Ordering[Instant] = Ordering.fromLessThan(_.isBefore(_))
val Seq(t0, t1, t2, t3) = Seq.iterate(Instant.now(), 4)(_.plusSeconds(5))

assert(((t0, t3) overlap (t1, t2)) == (t1, t2))
assert(((t0, t3) diff (t1, t2)) == (Option((t0, t1)), Option((t2, t3))))
```

`Interval` works for any base boundary type `B` given an `Ordering[B]`.
Instances for primitive types (`Ordering[Int]`, `Ordering[Double]`, etc) are
provided by the std library.

```scala
import com.github.tnielens.intervalops.Interval.Ops

assert(((0, 10) overlap (5, 15)) == (5, 10))
assert(((0, 10) diff (6, 7)) == (Option((0, 6), Option((7, 10))))
```

Working with optional bounds:

```scala
import com.github.tnielens.intervalops.Interval.Ops

assert(((0, Option(10)) overlap (8, None)) == (8, Option(10)))
assert(((0, None: Option[Int]) diff (6, Some(7))) == (Option((0, Some(6)), Option((7, None)))))
```

Mind that `Ordering[T]` is not contravariant, so you have to upcast `Some`
and `None` first operand values to `Option` for `Interval.Ops` to work properly.

## How to create an instance of `Interval[I]` for a custom type

```scala
import com.github.tnielens.intervalops.Interval
import com.github.tnielens.intervalops.Interval.Ops

case class CustomInterval[B](start: B, end: B)

// if possible, put the instance definition in the companion object
// of the `CustomInterval` to avoid the import tax
implicit def customIntervalInst[B](implicit ob: Ordering[B]) =
  Interval.instance[CustomInterval[B], B, Bound[B], Bound[B]](
    c => Bound(c.start),
    c => Bound(c.end),
    (b1, b2) => CustomInterval(b1.v, b2.v)
  )

assert(CustomInterval(1, 6) contains 5)
```

For cases involving optional bounds, see tuples instances in the companion
object `Interval`.
