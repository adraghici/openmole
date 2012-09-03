/*
 * Copyright (C) 2010 Romain Reuillon
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.core.implementation.task

import org.openmole.core.implementation.data._
import org.openmole.core.model.data._
import org.openmole.core.model.sampling._
import org.openmole.core.model.task._
import org.openmole.misc.exception._

import scala.collection.immutable.TreeMap
import scala.collection.mutable.ArrayBuffer

object ExplorationTask {
  type SampledValues = Iterable[Iterable[Variable[_]]]

  def apply(name: String, sampling: ISampling)(implicit plugins: PluginSet) = {
    new TaskBuilder { builder ⇒

      addInput(sampling.inputs)
      addOutput(sampling.prototypes.map { p ⇒ Data(p, DataModeMask.explore).toArray })

      def toTask =
        new ExplorationTask(name, sampling) {
          val inputs = builder.inputs + sampling.inputs
          val outputs = builder.outputs
          val parameters = builder.parameters
        }

    }
  }

}

sealed abstract class ExplorationTask(val name: String, val sampling: ISampling)(implicit val plugins: PluginSet) extends Task with IExplorationTask {

  //If input prototype as the same name as the output it is erased
  override protected def process(context: Context) = {
    val sampled = sampling.build(context).toIterable

    val variablesValues = TreeMap.empty[Prototype[_], ArrayBuffer[Any]] ++ sampling.prototypes.map { p ⇒ p -> new ArrayBuffer[Any](sampled.size) }

    for (sample ← sampled; v ← sample) variablesValues.get(v.prototype) match {
      case Some(b) ⇒ b += v.value
      case None ⇒
    }

    context ++ variablesValues.map {
      case (k, v) ⇒
        try Variable(k.toArray.asInstanceOf[Prototype[Array[_]]],
          v.toArray(k.`type`.asInstanceOf[Manifest[Any]]))
        catch {
          case e: ArrayStoreException ⇒ throw new UserBadDataError("Cannot fill factor values in " + k.toArray + ", values " + v)
        }
    }
  }

}
