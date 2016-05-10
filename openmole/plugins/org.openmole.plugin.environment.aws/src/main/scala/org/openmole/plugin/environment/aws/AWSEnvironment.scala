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

import org.openmole.core.batch.environment.MemoryRequirement
import org.openmole.core.workspace.Decrypt
import org.openmole.plugin.environment.ssh.{ SSHAuthentication, ClusterEnvironment }

import scala.concurrent.duration.Duration

object AWSEnvironment {
  def apply(
    user:                 String,
    host:                 String,
    port:                 Int              = 22,
    queue:                Option[String]   = None,
    openMOLEMemory:       Option[Int]      = None,
    wallTime:             Option[Duration] = None,
    memory:               Option[Int]      = None,
    sharedDirectory:      Option[String]   = None,
    workDirectory:        Option[String]   = None,
    threads:              Option[Int]      = None,
    storageSharedLocally: Boolean          = false,
    name:                 Option[String]   = None
  )(implicit decrypt: Decrypt) =
    new AWSEnvironment(
      user = user,
      host = host,
      port = port,
      queue = queue,
      openMOLEMemory = openMOLEMemory,
      wallTime = wallTime,
      memory = memory,
      sharedDirectory = sharedDirectory,
      workDirectory = workDirectory,
      threads = threads,
      storageSharedLocally = storageSharedLocally,
      name = name
    )(SSHAuthentication.find(user, host, port).apply)
}

class AWSEnvironment(
    val user:                    String,
    val host:                    String,
    override val port:           Int,
    val queue:                   Option[String],
    override val openMOLEMemory: Option[Int],
    val wallTime:                Option[Duration],
    val memory:                  Option[Int],
    val sharedDirectory:         Option[String],
    val workDirectory:           Option[String],
    override val threads:        Option[Int],
    val storageSharedLocally:    Boolean,
    override val name:           Option[String]
)(val credential: fr.iscpif.gridscale.ssh.SSHAuthentication) extends ClusterEnvironment with MemoryRequirement { env ⇒

  type JS = AWSJobService

  @transient lazy val jobService = new AWSJobService with ThisHost {
    def queue = env.queue
    def environment = env
    def sharedFS = storage
    def id = url.toString
    def workDirectory = env.workDirectory
  }
}
