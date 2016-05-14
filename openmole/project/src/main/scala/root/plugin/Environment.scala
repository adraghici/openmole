package plugin

import root.Libraries
import sbt._
import Keys._
import org.openmole.buildsystem.OMKeys._
import root._

object Environment extends PluginDefaults {
  implicit val artifactPrefix = Some("org.openmole.plugin.environment")

  lazy val oar = OsgiProject("oar", imports = Seq("*")) dependsOn (Core.exception, Core.workspace, Core.batch, gridscale, ssh) settings
    (libraryDependencies += Libraries.gridscaleOAR)

  lazy val desktopgrid = OsgiProject("desktopgrid", imports = Seq("*")) dependsOn (Core.workflow, Core.workspace, Core.tools,
    Core.batch, Core.serializer, Tool.sftpserver, gridscale) //settings (bundleType += "daemon")

  lazy val egi = OsgiProject("egi", imports = Seq("!org.apache.http.*", "!fr.iscpif.gridscale.libraries.srmstub", "!fr.iscpif.gridscale.libraries.lbstub", "!fr.iscpif.gridscale.libraries.wmsstub", "*")) dependsOn (Core.workflow, Core.exception, Core.updater, Core.batch,
    Core.workspace, Core.fileService, gridscale) settings (
      libraryDependencies ++= Seq(Libraries.gridscaleGlite, Libraries.gridscaleHTTP, Libraries.scalaLang)
    )

  lazy val gridscale = OsgiProject("gridscale", imports = Seq("*")) dependsOn (Core.workflow, Core.workspace, Core.tools, Core.workflow,
    Core.batch, Core.exception)

  lazy val pbs = OsgiProject("pbs", imports = Seq("*")) dependsOn (Core.exception, Core.workspace, Core.batch, gridscale, ssh) settings
    (libraryDependencies += Libraries.gridscalePBS)

  lazy val sge = OsgiProject("sge", imports = Seq("*")) dependsOn (Core.exception, Core.workspace, Core.batch, gridscale, ssh) settings
    (libraryDependencies += Libraries.gridscaleSGE)

  lazy val aws = OsgiProject("aws", imports = Seq("!org.apache.jclouds", "!org.apache.jclouds.*", "!org.jclouds", "!org.jclouds.*", "*")) dependsOn (Core.exception, Core.workspace, Core.batch, gridscale, ssh) settings (
    libraryDependencies += Libraries.gridscaleAWS,
    libraryDependencies += "org.apache.jclouds" % "jclouds-all" % "1.9.2",
    libraryDependencies += "org.apache.jclouds.driver" % "jclouds-sshj" % "1.9.2",
    libraryDependencies += "org.apache.jclouds.driver" % "jclouds-log4j" % "1.9.2"
  )

  lazy val condor = OsgiProject("condor", imports = Seq("*")) dependsOn (Core.exception, Core.workspace, Core.batch, gridscale, ssh) settings
    (libraryDependencies += Libraries.gridscaleCondor)

  lazy val slurm = OsgiProject("slurm", imports = Seq("*")) dependsOn (Core.exception, Core.workspace, Core.batch, gridscale, ssh) settings
    (libraryDependencies += Libraries.gridscaleSLURM)

  lazy val ssh = OsgiProject("ssh", imports = Seq("*")) dependsOn (Core.exception, Core.workspace, Core.event, Core.batch, gridscale) settings
    (libraryDependencies += Libraries.gridscaleSSH)

}
