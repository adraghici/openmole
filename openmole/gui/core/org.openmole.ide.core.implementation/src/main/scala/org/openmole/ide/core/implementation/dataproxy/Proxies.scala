/*
 * Copyright (C) 2011 Mathieu Mathieu Leclaire <mathieu.Mathieu Leclaire at openmole.org>
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

package org.openmole.ide.core.implementation.dataproxy

import org.openmole.ide.misc.tools.util._
import org.openmole.misc.tools.obj.ClassUtils._
import concurrent.stm._
import org.openmole.ide.core.implementation.builder.Builder
import org.openmole.ide.core.implementation.registry.PrototypeKey
import org.openmole.misc.eventdispatcher.EventDispatcher
import org.openmole.ide.core.implementation.panel.ConceptMenu

object Proxies {
  var instance = new Proxies
}

class Proxies {

  private val _proxies = TMap[ID.Type, DataProxyUI]()

  def dataUIs = atomic { implicit ctx ⇒
    _proxies.values
  }

  def tasks = _tasks.values.toList
  def prototypes = _prototypes.values.toList
  def samplings = _samplings.values.toList
  def environments = _environments.values.toList
  def hooks = _hooks.values.toList
  def sources = _sources.values.toList

  private def castProxies[T](implicit m: Manifest[T]): Map[ID.Type, T] = _proxies.single.flatMap {
    case (k, v) ⇒
      v match {
        case x if m.runtimeClass.isAssignableFrom(x.getClass) ⇒ Some(k -> x.asInstanceOf[T])
        case _ ⇒ None
      }
  }.toMap

  private def _tasks = castProxies[TaskDataProxyUI]
  private def _prototypes = castProxies[PrototypeDataProxyUI]
  private def _samplings = castProxies[SamplingCompositionDataProxyUI]
  private def _environments = castProxies[EnvironmentDataProxyUI]
  private def _hooks = castProxies[HookDataProxyUI]
  private def _sources = castProxies[SourceDataProxyUI]

  def task(id: ID.Type) = _tasks.get(id)
  def prototype(id: ID.Type) = _prototypes.get(id)
  def sampling(id: ID.Type) = _samplings.get(id)
  def environment(id: ID.Type) = _environments.get(id)
  def hook(id: ID.Type) = _hooks.get(id)
  def source(id: ID.Type) = _sources.get(id)

  def prototype(p: PrototypeKey) =
    _prototypes.map { case (_, v) ⇒ PrototypeKey(v) -> v }.get(p)

  def prototypeOrElseCreate(k: PrototypeKey) = atomic { implicit ctx ⇒
    prototype(k).getOrElse {
      val p = PrototypeKey.build(k)
      this += p
      p
    }
  }

  def +=(p: DataProxyUI) = {
    _proxies.single += p.id -> p
    EventDispatcher.trigger(this, new ProxyCreatedEvent)
  }

  def -=(p: DataProxyUI) = {
    _proxies.single -= p.id
    EventDispatcher.trigger(this, new ProxyDeletedEvent)
  }

  def contains(p: DataProxyUI) = _proxies.single.contains(p.id)

  def classPrototypes(prototypeClass: Class[_]): List[PrototypeDataProxyUI] =
    classPrototypes(prototypeClass, prototypes.toList)

  def classPrototypes(prototypeClass: Class[_],
                      protoList: List[PrototypeDataProxyUI]): List[PrototypeDataProxyUI] = protoList.filter {
    p ⇒ assignable(prototypeClass, p.dataUI.coreObject.get.`type`.runtimeClass)
  }

  def getOrGenerateSamplingComposition(p: SamplingCompositionDataProxyUI) =
    if (contains(p)) p
    else Builder.samplingCompositionUI(true)

  def clearAll: Unit = atomic { implicit actx ⇒
    ConceptMenu.clearAllItems
    _proxies.clear
    EventDispatcher.trigger(this, new ProxyDeletedEvent)
  }
}

