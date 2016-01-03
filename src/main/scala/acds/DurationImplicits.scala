package acds

import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 *
 */
object DurationImplicits {

  implicit class IntToDuration(a: Int) {
    def toSecs = {
      Duration.create(a, TimeUnit.SECONDS)
    }
  }

}
