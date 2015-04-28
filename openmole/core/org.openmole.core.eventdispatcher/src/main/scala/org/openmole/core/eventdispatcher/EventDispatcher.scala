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

package org.openmole.core.eventdispatcher

import scala.collection.mutable.WeakHashMap

object EventDispatcher {

  private lazy val listenerMap = new WeakHashMap[Any, Listner[Any]]

  def listen[T](obj: T)(listener: Listner[T]) =
    listenerMap.put(obj, listener.asInstanceOf[Listner[Any]])

  def trigger[T](obj: T, event: Event[T]) = {
    for {
      l ← listenerMap.get(obj)
    } l.asInstanceOf[Listner[T]].lift(event)
  }

}