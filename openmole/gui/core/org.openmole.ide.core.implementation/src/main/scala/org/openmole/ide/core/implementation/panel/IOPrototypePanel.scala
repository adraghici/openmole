/*
 * Copyright (C) 2011 <mathieu.Mathieu Leclaire at openmole.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmole.ide.core.implementation.panel

import org.openmole.ide.misc.widget._
import org.openmole.ide.core.implementation.dataproxy.{ PrototypeDataProxyUI, Proxies }
import java.awt.{ Color, BorderLayout }
import org.openmole.ide.misc.widget.multirow._
import swing.{ Color, Separator, Label, MyComboBox }
import scala.collection.immutable.HashMap
import org.openmole.ide.core.implementation.execution.ScenesManager
import org.openmole.ide.misc.tools.image.Images._
import org.openmole.ide.misc.widget.multirow.RowWidget.SMALL
import org.openmole.ide.misc.widget.multirow.MultiWidget.CLOSE_IF_EMPTY
import org.openmole.ide.misc.widget.multirow.MultiComboLinkLabelGroovyTextFieldEditor.{ ComboLinkLabelGroovyTextFieldEditorData, ComboLinkLabelGroovyTextFieldEditorPanel }
import org.openmole.ide.misc.widget.multirow.MultiComboLinkLabel.{ ComboLinkLabelData, ComboLinkLabelPanel }
import org.openmole.ide.core.implementation.workflow.ISceneContainer
import org.openmole.core.model.data.Prototype

class IOPrototypePanel(prototypesIn: Seq[PrototypeDataProxyUI] = List.empty,
                       prototypesOut: Seq[PrototypeDataProxyUI] = List.empty,
                       implicitPrototypeIn: Seq[PrototypeDataProxyUI] = List.empty,
                       implicitPrototypeOut: Seq[PrototypeDataProxyUI] = List.empty,
                       inputParameters: Map[PrototypeDataProxyUI, String] = Map.empty) extends PluginPanel("wrap") {

  val protoInEditor = {
    val incomboContent = Proxies.instance.prototypes.map {
      p ⇒
        {
          // lazy val coreOb = p.dataUI.coreObject
          // lazy val test = implicitly[coreOb.type <:< scala.util.Try[Prototype[_]]]
          (p, p.dataUI.coreObject.get, contentAction(p))
        }
    }.toList
    new MultiComboLinkLabelGroovyTextFieldEditor("", incomboContent,
      prototypesIn.map {
        proto ⇒
          new ComboLinkLabelGroovyTextFieldEditorPanel(incomboContent, EYE,
            new ComboLinkLabelGroovyTextFieldEditorData(proto.dataUI.coreObject.get, Some(proto), inputParameters.getOrElse(proto, "")))
      }, EYE, CLOSE_IF_EMPTY, insets = SMALL)
  }

  val protoOutEditor = {
    val outcomboContent = Proxies.instance.prototypes.map {
      p ⇒ (p, contentAction(p))
    }.toList
    new MultiComboLinkLabel("", outcomboContent, prototypesOut.map {
      proto ⇒
        new ComboLinkLabelPanel(outcomboContent, EYE, new ComboLinkLabelData(Some(proto)))
    }, EYE, CLOSE_IF_EMPTY, insets = SMALL)
  }

  var implicitEditorsMapping = new HashMap[PrototypeDataProxyUI, PrototypeGroovyTextFieldEditor]()

  lazy val protoIn = new PluginPanel("wrap") {
    contents += new Label("Inputs") {
      foreground = Color.WHITE
    }
    contents += {
      if (Proxies.instance.prototypes.size > 0) {
        val protoPanel = protoInEditor.panel
        if (!implicitPrototypeIn.isEmpty) {
          protoPanel.contents.insert(0, new PluginPanel("wrap 3, insets -2 25 0 5") {
            implicitPrototypeIn.foreach {
              p ⇒
                contents += new MyComboBox(List(p)) {
                  enabled = false
                }
                implicitEditorsMapping += p -> new PrototypeGroovyTextFieldEditor("Default value", p.dataUI.coreObject.get, inputParameters.getOrElse(p, ""))
                contents += new LinkLabel("", contentAction(p)) {
                  icon = EYE
                  background = new Color(0, 0, 0, 0)
                }
                contents += implicitEditorsMapping(p)
            }
          })
        }
        protoPanel
      }
      else new Label("Please create first Prototypes.")
    }
  }

  lazy val protoOut = new PluginPanel("wrap") {
    contents += new Label("Outputs") {
      foreground = Color.WHITE
    }
    contents += {
      if (Proxies.instance.prototypes.size > 0) {
        val protoPanel = protoOutEditor.panel
        if (!implicitPrototypeOut.isEmpty) {
          protoPanel.contents.insert(0, new PluginPanel("wrap 2, insets -2 25 0 5") {
            implicitPrototypeOut.foreach {
              p ⇒
                contents += new MyComboBox(List(p)) {
                  enabled = false
                }
                contents += new LinkLabel("", contentAction(p)) {
                  icon = EYE
                }
            }
          })
        }
        protoPanel
      }
      else new Label("Please create first Prototypes.")
    }
  }

  contents += new PluginPanel("") {
    peer.setLayout(new BorderLayout)
    peer.add(protoIn.peer, BorderLayout.WEST)
    peer.add((new Separator).peer)
    peer.add(protoOut.peer, BorderLayout.EAST)
  }

  def contentAction(proto: PrototypeDataProxyUI) = new ContentAction(proto.dataUI.toString, proto) {
    override def apply =
      ScenesManager.currentSceneContainer match {
        case Some(x: ISceneContainer) ⇒ x.scene.displayPropertyPanel(proto, 1)
        case None                     ⇒
      }
  }

  def save = {
    val (pInEditorContent, iEditorsMapping, pOutEditorContent) = (protoInEditor.content,
      implicitEditorsMapping.filterNot {
        _._2.editorText.isEmpty
      },
      protoOutEditor.content)

    (pInEditorContent.map { _.content.get }.filterNot { p ⇒ implicitPrototypeIn.contains(p) },
      new HashMap[PrototypeDataProxyUI, String]() ++
      pInEditorContent.map {
        x ⇒ x.content.get -> x.editorValue
      } ++ iEditorsMapping.map {
        case (k, v) ⇒ k -> v.editorText
      }.toMap,
      pOutEditorContent.map {
        _.content.get
      }.filterNot { p ⇒ implicitPrototypeOut.contains(p) })
  }

}