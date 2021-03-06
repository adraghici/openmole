/*
 * Copyright (C) 19/12/12 Romain Reuillon
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

package org.openmole.plugin.domain.modifier

import org.openmole.core.workflow.domain._
import org.openmole.core.workflow.data._
import org.openmole.core.workflow.tools.FromContext

import scalaz._
import Scalaz._

object SortDomain {

  implicit def isFinite[D, T] = new Finite[SortDomain[D, T], T] with DomainInputs[SortDomain[D, T]] {
    override def computeValues(domain: SortDomain[D, T]) = domain.computeValues()
    override def inputs(domain: SortDomain[D, T]): PrototypeSet = domain.inputs
  }

  def apply[D[_], T: scala.Ordering](domain: D[T])(implicit finite: Finite[D[T], T], domainInputs: DomainInputs[D[T]]) =
    new SortDomain[D[T], T](domain)

}

class SortDomain[D, T: scala.Ordering](val domain: D)(implicit finite: Finite[D, T], domainInputs: DomainInputs[D]) {
  def inputs = domainInputs.inputs(domain)
  def computeValues() =
    for {
      f ← finite.computeValues(domain)
    } yield f.toList.sorted
}

