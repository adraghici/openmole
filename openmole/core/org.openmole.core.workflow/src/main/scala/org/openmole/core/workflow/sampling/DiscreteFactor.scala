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

package org.openmole.core.workflow.sampling

import org.openmole.core.workflow.data._
import org.openmole.core.workflow.domain._
import org.openmole.core.workflow.tools.FromContext
import scalaz.Scalaz._

object DiscreteFactor {

  def apply[D, T](f: Factor[D, T])(implicit discrete: Discrete[D, T], domainInputs: DomainInputs[D] = DomainInputs.empty) =
    new DiscreteFactor[D, T] {
      val prototype = f.prototype
      val domain = f.domain

      override def inputs = domainInputs.inputs(domain)
      override def prototypes = List(prototype)
      override def apply(): FromContext[Iterator[collection.Iterable[Variable[T]]]] =
        discrete.iterator(f.domain).map(_.map { v ⇒ List(Variable(prototype, v)) })
    }

}

trait DiscreteFactor[D, T] extends Factor[D, T] with Sampling
