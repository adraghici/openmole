/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openmole.ide.plugin.environment.glite

import org.openmole.plugin.environment.glite._
import org.openmole.plugin.environment.glite.MyProxy
import org.openmole.core.batch.environment.BatchEnvironment
import org.openmole.misc.exception.UserBadDataError
import org.openmole.misc.workspace.Workspace
import scala.collection.JavaConversions._
import org.openmole.ide.core.implementation.data.EnvironmentDataUI

class GliteEnvironmentDataUI(val name: String = "",
                             val vo: String = "",
                             val voms: String = "",
                             val bdii: String = Workspace.preference(GliteEnvironment.DefaultBDII),
                             val proxy: Boolean = false,
                             val proxyTime: Option[String] = None,
                             val proxyHost: Option[String] = None,
                             val proxyPort: Option[Int] = None,
                             val fqan: Option[String] = None,
                             val openMOLEMemory: Option[Int] = Some(Workspace.preference(BatchEnvironment.MemorySizeForRuntime).toInt),
                             val memory: Option[Int] = None,
                             val cpuTime: Option[String] = None,
                             val wallTime: Option[String] = None,
                             val cpuNumber: Option[Int] = None,
                             val jobType: Option[String] = None,
                             val smpGranularity: Option[Int] = None,
                             val architecture: Boolean = false,
                             val threads: Option[Int] = None) extends EnvironmentDataUI {

  def coreObject = util.Try {

    if (vo == "") throw new UserBadDataError(s"The VO in glite environment $name is not properly set")
    if (voms == "") throw new UserBadDataError(s"The VOMs in glite environment $name is not properly set")
    if (bdii == "") throw new UserBadDataError(s"The BDII in glite environment $name is not properly set")
    val myProxy = {
      if (proxy && proxyTime.isDefined && proxyHost.isDefined) Some(new MyProxy(proxyTime.get, proxyHost.get, proxyPort))
      else None
    }
    try {
      GliteEnvironment(
        vo,
        vomsURL = Some(voms),
        bdii = Some(bdii),
        fqan = fqan,
        openMOLEMemory = openMOLEMemory,
        memory = memory,
        cpuTime = cpuTime,
        wallTime = wallTime,
        cpuNumber = cpuNumber,
        jobType = jobType,
        smpGranularity = smpGranularity,
        myProxy = myProxy,
        architecture = if (architecture) Some("x86_64") else None,
        threads = threads)
    }
    catch {
      case e: Throwable ⇒ throw new UserBadDataError(e, "An error occurred when initializing the glite environment " + name + ". Please check your certificate settings in the Preferences menu.")
    }
  }

  def coreClass = classOf[GliteEnvironment]

  override def imagePath = "img/glite.png"

  override def fatImagePath = "img/glite_fat.png"

  def buildPanelUI = new GliteEnvironmentPanelUI(this)
}
