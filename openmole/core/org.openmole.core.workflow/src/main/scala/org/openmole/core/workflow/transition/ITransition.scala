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

package org.openmole.core.workflow.transition

import org.openmole.core.exception.InternalProcessingError
import org.openmole.core.workflow.mole._
import org.openmole.core.workflow.data._
import org.openmole.core.workflow.tools.ContextAggregator._
import org.openmole.core.workflow.tools._
import org.openmole.core.workflow.validation.TypeUtil._

import scala.util.Random

trait ITransition {

  /**
   *
   * Get the starting capsule of this transition.
   *
   * @return the starting capsule of this transition
   */
  def start: Capsule

  /**
   *
   * Get the ending capsule of this transition.
   *
   * @return the ending capsule of this transition
   */
  def end: Slot

  /**
   *
   * Get the condition under which this transition is performed.
   *
   * @return the condition under which this transition is performed
   */
  //def condition: Condition

  /**
   *
   * Get the filter of the variables which are filtred by this transition.
   *
   * @return filter on the names of the variables which are filtred by this transition
   */
  def filter: BlockList[String]

  /**
   * Get the unfiltred user output data of the starting capsule going through
   * this transition
   *
   * @return the unfiltred output data of the staring capsule
   */
  def data(mole: Mole, sources: Sources, hooks: Hooks): PrototypeSet =
    start.outputs(mole, sources, hooks).filterNot(d ⇒ filter(d.name))

  /**
   *
   * Perform the transition and submit the jobs for the following capsules in the mole.
   *
   * @param from      context generated by the previous job
   * @param ticket    ticket of the previous job
   * @param subMole   current submole
   */
  def perform(from: Context, ticket: Ticket, subMole: SubMoleExecution)(implicit rng: RandomProvider)

  private def nextTaskReady(ticket: Ticket, subMole: SubMoleExecution): Boolean = {
    val registry = subMole.transitionRegistry
    val mole = subMole.moleExecution.mole
    mole.inputTransitions(end).forall(registry.isRegistred(_, ticket))
  }

  protected def submitNextJobsIfReady(context: Iterable[Variable[_]], ticket: Ticket, subMole: SubMoleExecution) = {
    val moleExecution = subMole.moleExecution
    val registry = subMole.transitionRegistry
    val mole = subMole.moleExecution.mole

    registry.register(this, ticket, context)
    if (nextTaskReady(ticket, subMole)) {
      val dataChannelVariables = mole.inputDataChannels(end).toList.flatMap { _.consums(ticket, moleExecution) }

      def removeVariables(t: ITransition) = registry.remove(t, ticket).getOrElse(throw new InternalProcessingError("BUG context should be registered")).toIterable

      val transitionsVariables: Iterable[Variable[_]] =
        mole.inputTransitions(end).toList.flatMap {
          t ⇒ removeVariables(t)
        }

      val combinasion = (dataChannelVariables ++ transitionsVariables)

      val newTicket =
        if (mole.slots(end.capsule).size <= 1) ticket
        else moleExecution.nextTicket(ticket.parent.getOrElse(throw new InternalProcessingError("BUG should never reach root ticket")))

      val toArrayManifests =
        validTypes(mole, moleExecution.sources, moleExecution.hooks)(end).filter(_.toArray).map(ct ⇒ ct.name -> ct.`type`).toMap[String, PrototypeType[_]]

      val newContext = aggregate(end.capsule.inputs(mole, moleExecution.sources, moleExecution.hooks), toArrayManifests, combinasion.map(ticket.content -> _))

      subMole.submit(end.capsule, newContext, newTicket)
    }
  }

  protected def filtered(context: Context) = context.filterNot { case (n, _) ⇒ filter(n) }

}
