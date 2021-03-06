import sbt._
import sbt.Keys._

import root._


object Root extends Defaults(OSGi) {
  implicit val dir = file(".")
  lazy val all = Project(id = "root", base = dir) aggregate (subProjects: _*)

  override def settings = super.settings ++ Seq(
    //make openmole repo the resolver of last resort
    resolvers += DefaultMavenRepository,
    resolvers += "openmole-public" at "https://maven.openmole.org/public",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases")
  )

}
