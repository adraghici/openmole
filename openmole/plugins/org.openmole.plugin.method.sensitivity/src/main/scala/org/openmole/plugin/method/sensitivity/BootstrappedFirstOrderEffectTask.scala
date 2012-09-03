/*
 * Copyright (C) 2012 Romain Reuillon
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

package org.openmole.plugin.method.sensitivity

import org.openmole.core.implementation.data._
import org.openmole.core.implementation.task._
import org.openmole.core.model.data._
import SaltelliSampling._
import SensitivityTask._
import org.openmole.core.model.task._

object BootstrappedFirstOrderEffectTask {

  def apply(
    name: String,
    modelInputs: Iterable[Prototype[Double]],
    modelOutputs: Iterable[Prototype[Double]],
    bootstrap: Int)(implicit plugins: PluginSet) = new BootstrappedFirstOrderEffectTaskBuilder(name, SaltelliSampling.matrixName, modelInputs, modelOutputs, bootstrap)

  def apply(
    name: String,
    matrixName: Prototype[String],
    modelInputs: Iterable[Prototype[Double]],
    modelOutputs: Iterable[Prototype[Double]],
    bootstrap: Int)(implicit plugins: PluginSet) = new BootstrappedFirstOrderEffectTaskBuilder(name, matrixName, modelInputs, modelOutputs, bootstrap)

  class BootstrappedFirstOrderEffectTaskBuilder(
      val name: String,
      val matrixName: Prototype[String],
      val modelInputs: Iterable[Prototype[Double]],
      val modelOutputs: Iterable[Prototype[Double]],
      val bootstrap: Int)(implicit plugins: PluginSet) extends BootstrappedSensitivityTask.Builder { builder ⇒

    def toTask = new BootstrappedFirstOrderEffectTask(name, matrixName, modelInputs, modelOutputs) {
      val inputs: DataSet = builder.inputs
      val outputs: DataSet = builder.outputs
      val parameters = builder.parameters
      val bootstrap = builder.bootstrap
    }

  }

}

abstract sealed class BootstrappedFirstOrderEffectTask(
  val name: String,
  val matrixName: Prototype[String],
  val modelInputs: Iterable[Prototype[Double]],
  val modelOutputs: Iterable[Prototype[Double]])(implicit val plugins: PluginSet) extends BootstrappedSensitivityTask with FirstOrderEffect
