/*
 * Copyright (C) 20/02/13 Romain Reuillon
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

package org.openmole.core.implementation.mole

import org.openmole.core.implementation.tools.InputOutputCheck
import org.openmole.core.model.mole._
import org.openmole.core.model.data.Context

trait Hook extends IHook with InputOutputCheck {
  protected def process(context: Context, executionContext: ExecutionContext): Context
  def perform(context: Context, executionContext: ExecutionContext) = perform(context, process(_, executionContext))
}