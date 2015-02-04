/*
 * Copyright (C) 2015 Romain Reuillon
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

package org.openmole.plugin.method.modelfamily.ga

import fr.iscpif.mgo._

import scala.util.Random

trait ModelFamilyMutation <: DynamicGAMutation with ModelFamilyGenome {

  def changeModel = 0.1

  override def mutate(genome: G, population: Population[G, P, F], archive: A)(implicit rng: Random): G = {
    def mutated = super.mutate(genome, population, archive)
    if (rng.nextDouble < changeModel) modelId.set(mutated, rng.nextInt(models)) else mutated
  }

}