package optbounds
import com.github.tnielens.intervalops.Interval
import com.github.tnielens.intervalops.Interval.Ops
import com.github.tnielens.intervalops.OptBound.Bound
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class IntervalSpec extends AnyFreeSpec with Matchers {
  case class CustomInterval[B](start: B, end: B)
  implicit def customIntervalInst[B](implicit ob: Ordering[B]) =
    Interval.instance[CustomInterval[B], B, Bound[B], Bound[B]](
      c => Bound(c.start),
      c => Bound(c.end),
      (b1, b2) => CustomInterval(b1.v, b2.v)
    )

  "overlap should return the overlap in `Some` if the intervals overlap" in {
    ((1, 3) overlap (2, 5)) shouldBe Option((2, 3))
    ((1, Option(3)) overlap (2, None)) shouldBe Option((2, Some(3)))
    ((None: Option[Int], 3) overlap (Some(2), 5)) shouldBe Option(Some(2), 3)
    ((None: Option[Int], Option(3)) overlap (Option(2), None)) shouldBe Option(Some(2), Option(3))
    CustomInterval(1, 3) overlap CustomInterval(2, 5) shouldBe Option(CustomInterval(2, 3))
  }

  "overlap should return `None` if intervals don't overlap" in {
    ((1, 2) overlap (3, 5)) shouldBe None
    ((1, Option(2)) overlap (3, None)) shouldBe None
    ((None: Option[Int], 2) overlap (Some(3), 5)) shouldBe None
    ((None: Option[Int], Option(2)) overlap (Option(3), None)) shouldBe None
    CustomInterval(1, 2) overlap CustomInterval(3, 5) shouldBe None
  }

  "diff should return the start diff if o2 covers the end bound of o1 but not the start one" in {
    ((1, 3) diff (2, 5)) shouldBe (Some((1, 2)), None)
    ((1, Option(3)) diff (2, None)) shouldBe (Some((1, Some(2))), None)
    ((None: Option[Int], 3) diff (Some(2), 5)) shouldBe (Some((None, 2)), None)
    ((None: Option[Int], Option(3)) diff (Option(2), None)) shouldBe (Some(None, Some(2)), None)
    CustomInterval(1, 3) diff CustomInterval(2, 5) shouldBe (Some(CustomInterval(1, 2)), None)
  }

  "diff should return the end diff if o2 covers the start bound of o1 but not the end one" in {
    ((2, 5) diff (1, 3)) shouldBe (None, Some((3, 5)))
    ((2, None: Option[Int]) diff (1, Some(3))) shouldBe (None, Some((3, None)))
    ((Option(2), 5) diff (None, 3)) shouldBe (None, Some((Some(3), 5)))
    ((Option(2), Option(5)) diff (None, Some(3))) shouldBe (None, Some(Some(3), Some(5)))
    CustomInterval(2, 5) diff CustomInterval(1, 3) shouldBe (None, Some(CustomInterval(3, 5)))
  }

  "diff should return the two parts of the diff if o1 covers fully o2 with margins on both sides" in {
    ((1, 5) diff (2, 3)) shouldBe (Some((1, 2)), Some((3, 5)))
    ((1, None: Option[Int]) diff (2, Some(3))) shouldBe (Some((1, Some(2))), Some((3, None)))
    ((None: Option[Int], 5) diff (Some(2), 3)) shouldBe (Some((None, 2)), Some((Some(3), 5)))
    ((None: Option[Int], Option(5)) diff (Some(2), Some(3))) shouldBe
      (Some((None, Some(2))), Some(Some(3), Some(5)))
    CustomInterval(1, 5) diff CustomInterval(2, 3) shouldBe (Some(CustomInterval(1, 2)), Some(
      CustomInterval(3, 5)
    ))
  }

  "diff should return no parts if the intervals don't overlap" in {
    ((1, 2) diff (3, 5)) shouldBe (None, None)
    ((1, Option(2)) diff (3, None)) shouldBe (None, None)
    ((None: Option[Int], 2) diff (Some(3), 5)) shouldBe (None, None)
    ((None: Option[Int], Option(2)) diff (Some(3), Some(5))) shouldBe
      (None, None)
    CustomInterval(1, 2) diff CustomInterval(3, 5) shouldBe (None, None)
  }

  "contains should return true if the interval contains the bound (start inclusive, end exclusive)" in {
    assert((1, 5) contains 2)
    assert((1, 5) contains 1)
    assert(!((1, 5) contains 5))
    assert((1, None: Option[Int]) contains 2)
    assert((None: Option[Int], 5) contains 2)
    assert((None: Option[Int], Option(5)) contains 2)
    assert(CustomInterval(1, 5) contains 2)
  }

  "contains should return false if the interval does not containt the bound (start inclusive, end exclusive)" in {
    assert(!((1, 5) contains 6))
    assert(!((1, Option(5)) contains 6))
    assert(!((None: Option[Int], 5) contains 6))
    assert(!((None: Option[Int], Option(5)) contains 6))
    assert(!(CustomInterval(1, 5) contains 6))
  }

  "containsI should return true if the interval contains the bound (start inclusive, end exclusive)" in {
    assert((1, 5) containsI (2, 3))
    assert((1, None: Option[Int]) containsI (2, Some(3)))
    assert((None: Option[Int], 5) containsI (Some(2), 3))
    assert((None: Option[Int], Option(5)) containsI (Some(2), Some(3)))
    assert(CustomInterval(1, 5) containsI CustomInterval(2, 3))
  }

  "containsII should return false if the interval does not containt the bound (start inclusive, end exclusive)" in {
    assert(!((1, 5) containsI (4, 6)))
    assert(!((1, Option(5)) containsI (4, Some(6))))
    assert(!((None: Option[Int], 5) containsI (Some(4), 6)))
    assert(!((None: Option[Int], Option(5)) containsI (Some(4), Some(6))))
    assert(!(CustomInterval(1, 5) containsI CustomInterval(4, 6)))
  }

  "adjacency should return the adjacent bound if two intervals abut" in {
    ((1, 2) adjacency (2, 5)) shouldBe Option(2)
    ((2, 5) adjacency (1, 2)) shouldBe Option(2)
    ((1, Option(2)) adjacency (2, None)) shouldBe Option(2)
    ((None: Option[Int], 2) adjacency (Some(2), 5)) shouldBe Option(2)
    ((None: Option[Int], Option(2)) adjacency (Some(2), Some(5))) shouldBe Option(2)
    CustomInterval(1, 2) adjacency CustomInterval(2, 5) shouldBe Option(2)
  }

  "adjacency should return None if the intervals don't abut nor overlap" in {
    ((1, 2) adjacency (3, 5)) shouldBe None
    ((1, Option(2)) adjacency (3, None)) shouldBe None
    ((None: Option[Int], 2) adjacency (Some(3), 5)) shouldBe None
    ((None: Option[Int], Option(2)) adjacency (Some(3), Some(5))) shouldBe None
    CustomInterval(1, 2) adjacency CustomInterval(3, 5) shouldBe None
  }

  "adjacency should return None if the intervals overlap" in {
    ((1, 4) adjacency (3, 5)) shouldBe None
    ((1, Option(4)) adjacency (3, None)) shouldBe None
    ((None: Option[Int], 4) adjacency (Some(3), 5)) shouldBe None
    ((None: Option[Int], Option(4)) adjacency (Some(3), Some(5))) shouldBe None
    CustomInterval(1, 4) adjacency CustomInterval(3, 5) shouldBe None
  }

  "gap should return the gap between intervals if they don't abut nor overlap" in {
    ((1, 2) gap (3, 5)) shouldBe Some((2, 3))
    ((3, 5) gap (1, 2)) shouldBe Some((2, 3))
    ((1, Option(2)) gap (3, None)) shouldBe Some((2, Some(3)))
    ((None: Option[Int], 2) gap (Some(3), 5)) shouldBe Some((Some(2), 3))
    ((None: Option[Int], Option(2)) gap (Some(3), Some(5))) shouldBe Some((Some(2), Some(3)))
    CustomInterval(1, 2) gap CustomInterval(3, 5) shouldBe Some(CustomInterval(2, 3))
  }

  "gap should return None if the intervals abut or overlap" in {
    ((1, 4) gap (3, 5)) shouldBe None
    ((3, 5) gap (1, 4)) shouldBe None
    ((1, 3) gap (3, 5)) shouldBe None
    ((3, 5) gap (1, 3)) shouldBe None
    ((1, Option(4)) gap (3, None)) shouldBe None
    ((None: Option[Int], 4) gap (Some(3), 5)) shouldBe None
    ((None: Option[Int], Option(4)) gap (Some(3), Some(5))) shouldBe None
    CustomInterval(1, 4) gap CustomInterval(3, 5) shouldBe None
  }

  "Interval[I] works fine with Instant" in {
    implicit val io: Ordering[Instant] = Ordering.fromLessThan(_.isBefore(_))
    val Seq(t1, t2, t3, t4) = Seq.iterate(Instant.now(), 4)(_.plusSeconds(5))
    (t1, t3) overlap (t2, t4) shouldBe Some((t2, t3))
    (t1, t3) diff (t2, t4) shouldBe (Some((t1, t2)), None)
    (t1, t2) adjacency (t2, t4) shouldBe Some(t2)
  }
}
