/*
 * Copyright (C) 2011 <mathieu.leclaire at openmole.org>
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.ide.core.model.workflow

import org.apache.commons.collections15.bidimap.DualHashBidiMap
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

trait IMoleSceneManager {
  def name: Option[String]
  
  def name_=(n:Option[String])
  
  def capsules:DualHashBidiMap[String, ICapsuleUI]
  
  def startingCapsule: Option[ICapsuleUI]
  
  def capsuleConnections:  HashMap[ICapsuleUI, HashSet[ITransitionUI]]
}
