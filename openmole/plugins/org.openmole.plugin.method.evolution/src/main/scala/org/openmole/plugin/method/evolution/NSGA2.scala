/*
 * Copyright (C) 2014 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
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

package org.openmole.plugin.method.evolution

import fr.iscpif.mgo._
import fr.iscpif.mgo.algorithm._
import org.openmole.core.workflow.data.PrototypeType

import scala.util.Random

object NSGA2 {

  def apply(
    mu: Int,
    inputs: Inputs,
    objectives: Objectives,
    epsilons: Option[Seq[Double]] = None) = {

    val (_mu, _inputs, _objectives) = (mu, inputs, objectives)

    new NSGAII with GAAlgorithm {
      val stateType = PrototypeType[STATE]
      val populationType = PrototypeType[Pop]
      val individualType = PrototypeType[Ind]
      val gType = PrototypeType[G]

      val objectives = _objectives
      val inputs = _inputs

      override implicit def fitness: Fitness[Seq[Double]] = Fitness(_.phenotype)
      override def mu: Int = _mu
    }
  }

}

