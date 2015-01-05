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

package org.openmole.plugin.sampling

import java.io.File

import org.openmole.core.model.data.Prototype
import org.openmole.misc.macros.Keyword._

package object csv {

  lazy val columns = add[{ def addColumn(proto: Prototype[_]) }]
  lazy val fileColumns = add[{
    def addFileColumn(name: String, dir: File, proto: Prototype[File])
    def addFileColumn(dir: File, proto: Prototype[File])
  }]
  lazy val separator = set[{ def setSeparator(s: Option[Char]) }]

}