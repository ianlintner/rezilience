package nl.vroste.rezilience

import nl.vroste.rezilience.Policy.CircuitBreakerOpen
import zio.test.Assertion._
import zio.test.TestAspect.nonFlaky
import zio.test._
import zio._

object RetrySpec extends ZIOSpecDefault {
  override def spec = suite("Retry")(
    test("widen should not retry unmatched errors") {
      ZIO.scoped {
        for {
          retry     <- Retry
                         .make(Retry.Schedules.exponentialBackoff(1.second, 2.seconds))
                         .map(_.widen(Policy.unwrap[Throwable]))
          tries     <- Ref.make(0)
          failure    = ZIO.fail(CircuitBreakerOpen)
          _         <- retry((tries.update(_ + 1) *> failure).unit).either
          triesMade <- tries.get
        } yield assert(triesMade)(equalTo(1))
      }
    }
  ) @@ nonFlaky
}
