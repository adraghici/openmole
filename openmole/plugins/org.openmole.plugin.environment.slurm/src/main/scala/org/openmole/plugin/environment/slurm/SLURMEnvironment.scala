/*
 * Copyright (C) 2012 Romain Reuillon
 * Copyright (C) 2014 Jonathan Passerat-Palmbach
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

package org.openmole.plugin.environment.slurm

import fr.iscpif.gridscale.ssh.SSHStorage
import fr.iscpif.gridscale.ssh.SSHHost
import java.net.URI
import org.openmole.core.batch.control.LimitedAccess
import org.openmole.core.batch.environment._
import org.openmole.core.workspace._
import org.openmole.plugin.environment.gridscale._
import org.openmole.plugin.environment.ssh._

import scala.concurrent.duration.Duration

object SLURMEnvironment {

  def apply(
    user:                 String,
    host:                 String,
    port:                 Int              = 22,
    queue:                Option[String]   = None,
    openMOLEMemory:       Option[Int]      = None,
    wallTime:             Option[Duration] = None,
    memory:               Option[Int]      = None,
    qos:                  Option[String]   = None,
    gres:                 List[Gres]       = List(),
    constraints:          List[String]     = List(),
    nodes:                Option[Int]      = None,
    coresByNode:          Option[Int]      = None,
    sharedDirectory:      Option[String]   = None,
    workDirectory:        Option[String]   = None,
    threads:              Option[Int]      = None,
    storageSharedLocally: Boolean          = false,
    name:                 Option[String]   = None
  )(implicit decrypt: Decrypt) =
    new SLURMEnvironment(
      user = user,
      host = host,
      port = port,
      queue = queue,
      openMOLEMemory = openMOLEMemory,
      wallTime = wallTime,
      memory = memory,
      qos = qos,
      gres = gres,
      constraints = constraints,
      nodes = nodes,
      coresByNode = coresByNode,
      sharedDirectory = sharedDirectory,
      workDirectory = workDirectory,
      threads = threads,
      storageSharedLocally = storageSharedLocally,
      name = name
    )(SSHAuthentication.find(user, host, port).apply)
}

import SLURMEnvironment._

class SLURMEnvironment(
    val user:                    String,
    val host:                    String,
    override val port:           Int,
    val queue:                   Option[String],
    override val openMOLEMemory: Option[Int],
    val wallTime:                Option[Duration],
    val memory:                  Option[Int],
    val qos:                     Option[String],
    val gres:                    List[Gres],
    val constraints:             List[String],
    val nodes:                   Option[Int],
    val coresByNode:             Option[Int],
    val sharedDirectory:         Option[String],
    val workDirectory:           Option[String],
    override val threads:        Option[Int],
    val storageSharedLocally:    Boolean,
    override val name:           Option[String]
)(val credential: fr.iscpif.gridscale.ssh.SSHAuthentication) extends ClusterEnvironment with MemoryRequirement { env ⇒

  type JS = SLURMJobService

  @transient lazy val jobService = new SLURMJobService with ThisHost {
    def queue = env.queue
    val environment = env
    def sharedFS = storage
    def workDirectory = env.workDirectory
  }

  override def close() = ???
}
