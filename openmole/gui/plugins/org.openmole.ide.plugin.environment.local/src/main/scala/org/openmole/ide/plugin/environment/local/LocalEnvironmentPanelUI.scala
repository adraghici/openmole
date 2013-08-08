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

package org.openmole.ide.plugin.environment.local

import java.util.Locale
import java.util.ResourceBundle
import org.openmole.ide.misc.widget.Help
import org.openmole.ide.misc.widget.Helper
import org.openmole.ide.misc.widget.PluginPanel
import org.openmole.ide.misc.widget.URL
import scala.swing.TextField
import org.openmole.ide.core.implementation.panelsettings.EnvironmentPanelUI

class LocalEnvironmentPanelUI(pud: LocalEnvironmentDataUI)(implicit val i18n: ResourceBundle = ResourceBundle.getBundle("help", new Locale("en", "EN"))) extends PluginPanel("wrap 2") with EnvironmentPanelUI {

  val nbThreadTextField = new TextField(6)
  val components = List(("Number of threads", new PluginPanel("") {
    contents += nbThreadTextField
  }))

  nbThreadTextField.text = pud.nbThread.toString

  override lazy val help = new Helper(List(new URL(i18n.getString("permalinkText"), i18n.getString("permalink"))))

  add(nbThreadTextField, new Help(i18n.getString("thread"), i18n.getString("threadEx")))

  override def saveContent(name: String) = new LocalEnvironmentDataUI(name,
    nbThreadTextField.text.toInt)
}
