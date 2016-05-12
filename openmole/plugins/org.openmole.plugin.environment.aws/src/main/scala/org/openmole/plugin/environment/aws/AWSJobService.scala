/*
 * Copyright (C) 2016 Adrian Draghici
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

package org.openmole.plugin.environment.aws

import fr.iscpif.gridscale.aws.{ AWSJobService ⇒ GSAWSJobService, AWSJobDescription }
import fr.iscpif.gridscale.ssh.{ SSHConnectionCache, SSHHost }
import org.openmole.core.batch.environment.SerializedJob
import org.openmole.core.batch.jobservice.{ BatchJob, BatchJobId }
import org.openmole.core.workspace.Workspace
import org.openmole.plugin.environment.ssh.{ ClusterJobService, SSHService, SharedStorage }
import org.openmole.tool.logger.Logger

object AWSJobService extends Logger

import org.openmole.plugin.environment.aws.AWSJobService._

trait AWSJobService extends ClusterJobService with SSHHost with SharedStorage { js ⇒

  def environment: AWSEnvironment
  val jobService: GSAWSJobService

  protected def _submit(serializedJob: SerializedJob) = {
    val (remoteScript, result) = buildScript(serializedJob)
    val jobDescription = new AWSJobDescription {
      val executable = "/bin/bash"
      val arguments = remoteScript
      override val queue = environment.queue
      val workDirectory = serializedJob.path
      override val wallTime = environment.wallTime
      override val memory = Some(environment.requiredMemory)
    }

    val jid = js.jobService.submit(jobDescription)
    Log.logger.fine(s"AWS job [${jid.sgeId}], description: \n ${jobDescription.toSGE}")

    new BatchJob with BatchJobId {
      val jobService = js
      val id = jid
      val resultPath = result
    }
  }
}

