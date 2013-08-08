/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openmole.ide.plugin.task.netlogo

import org.openmole.core.model.data._
import org.openmole.core.model.task._
import org.openmole.ide.core.implementation.data.TaskDataUI
import org.openmole.plugin.task.netlogo5.NetLogo5Task
import scala.io.Source
import java.io.File
import org.openmole.ide.core.implementation.dataproxy.PrototypeDataProxyUI

case class NetLogo5TaskDataUI(
    name: String = "",
    workspaceEmbedded: Boolean = false,
    nlogoPath: String = "",
    lauchingCommands: String = "",
    prototypeMappingInput: List[(PrototypeDataProxyUI, String)] = List(),
    prototypeMappingOutput: List[(String, PrototypeDataProxyUI)] = List(),
    resources: List[String] = List()) extends TaskDataUI {

  override def cloneWithoutPrototype(proxy: PrototypeDataProxyUI) =
    this.copy(prototypeMappingInput = prototypeMappingInput.filterNot(_._1 == proxy),
      prototypeMappingOutput = prototypeMappingOutput.filterNot(_._2 == proxy))

  def coreObject(plugins: PluginSet) = util.Try {
    val builder = NetLogo5Task(
      name,
      new File(nlogoPath),
      Source.fromString(lauchingCommands).getLines.toIterable,
      workspaceEmbedded)(plugins)
    initialise(builder)
    resources.foreach { r ⇒ builder addResource (new File(r)) }
    prototypeMappingInput.foreach { case (p, n) ⇒ builder addNetLogoInput (p.dataUI.coreObject.get, n) }
    prototypeMappingOutput.foreach { case (n, p) ⇒ builder addNetLogoOutput (n, p.dataUI.coreObject.get) }
    builder.toTask
  }

  def coreClass = classOf[NetLogo5Task]

  override def imagePath = "img/netlogo5.png"

  def fatImagePath = "img/netlogo5_fat.png"

  def buildPanelUI = new NetLogo5TaskPanelUI(this)
}
