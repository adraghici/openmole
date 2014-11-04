package org.openmole.gui.server.factory

/*
 * Copyright (C) 24/09/14 // mathieu.leclaire@openmole.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.openmole.core.model.task.PluginSet
import scala.collection.JavaConversions._
import scala.collection.mutable
import org.openmole.gui.ext.data._

import scala.util.{ Failure, Try }

object ServerFactories {
  lazy private val instance = new ServerFactories

  def coreObject(data: Data): Try[Any] = instance.factories.synchronized {
    instance.factories.get(data.getClass()) match {
      case Some(f: Factory) ⇒ f.coreObject(PluginSet.empty) //FIXME AND TAKE THE PLUGINS
      case _                ⇒ Failure(new Throwable("The data " + data.name + " cannot be recontructed on the server."))
    }
  }

  def add(dataClass: Class[_], factory: Factory) = instance.factories.synchronized {
    println("Add server " + dataClass)
    instance.factories += dataClass -> factory
  }

  def remove(dataClass: Class[_]) = instance.factories.synchronized {
    instance.factories -= dataClass
  }
}

class ServerFactories {
  val factories = new mutable.WeakHashMap[Class[_], Factory]
}

trait Factory {
  def data: Data
  def coreObject(implicit plugins: PluginSet): Try[Any]
}
