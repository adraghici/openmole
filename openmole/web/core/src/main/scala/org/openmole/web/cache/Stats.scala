package org.openmole.web.cache

/**
 * Created by mhammons on 4/23/14.
 */
case class Stats(ready: Int, running: Int, completed: Int, failed: Int, cancelled: Int, status: Status) {
  val self = this

  object lens {
    trait JobStatusLens {
      protected def binding: (Int) => Stats
      protected def stat: Int
      def ++ = binding(stat + 1)
      def -- = binding(stat - 1)
    }

    case class JSLens(stat: Int, binding: (Int) => Stats) extends JobStatusLens

    val ready = JSLens(self.ready, Stats(_, self.running, self.completed, self.failed, self.cancelled, self.status))
    val running = JSLens(self.running, Stats(self.ready, _, self.completed, self.failed, self.cancelled, self.status))
    val completed = JSLens(self.completed, Stats(self.ready, self.running, _, self.failed, self.cancelled, self.status))
    val failed = JSLens(self.failed, Stats(self.ready, self.running, self.completed, _, self.cancelled, self.status))
    val cancelled = JSLens(self.cancelled, Stats(self.ready, self.running, self.completed, self.failed, _, self.status))

    object status {
      def set(s: Status) = Stats(self.ready, self.running, self.completed, self.failed, self.cancelled, s)
    }
  }

}


trait Status

object Status {
  val statuses = List(Running, Finished, Stopped)

  object Running extends Status {
    override def toString = "Running"
  }

  object Finished extends Status {
    override def toString = "Finished"
  }

  object Stopped extends Status {
    override def toString = "Stopped"
  }
}

object ex {
  lazy val empty = Map("Ready" -> 0,
    "Running" -> 0,
    "Completed" -> 0,
    "Failed" -> 0,
    "Cancelled" -> 0)
}
