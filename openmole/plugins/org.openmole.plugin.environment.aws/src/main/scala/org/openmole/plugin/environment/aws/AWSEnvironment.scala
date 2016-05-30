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

import java.io.File

import fr.iscpif.gridscale.authentication.PrivateKey
import org.openmole.tool.logger.Logger

import scala.concurrent.duration.Duration

import fr.iscpif.gridscale.aws.{ AWSJobService ⇒ GSAWSJobService, Starcluster }
import fr.iscpif.gridscale.aws.AWSJobService._
import fr.iscpif.gridscale.ssh._
import org.openmole.core.batch.environment.MemoryRequirement
import org.openmole.core.workspace.{ Decrypt, Workspace }
import org.openmole.plugin.environment.ssh.{ ClusterEnvironment, SSHAuthentication, SSHService }

object AWSEnvironment extends Logger {
  def apply(
    region:             String,
    awsUserName:        String,
    awsUserId:          String,
    awsKeypairName:     String,
    awsCredentialsPath: String,
    privateKeyPath:     String,
    clusterSize:        Int              = 1,
    queue:              Option[String]   = None,
    openMOLEMemory:     Option[Int]      = None,
    wallTime:           Option[Duration] = None,
    memory:             Option[Int]      = None,
    threads:            Option[Int]      = None,
    name:               Option[String]   = None
  ) =
    new AWSEnvironment(
      region,
      awsUserName,
      awsUserId,
      awsKeypairName,
      awsCredentialsPath,
      privateKeyPath,
      clusterSize,
      queue,
      openMOLEMemory,
      wallTime,
      memory,
      threads,
      name
    )
}

import AWSEnvironment._

class AWSEnvironment(
    val region:                  String,
    val awsUserName:             String,
    val awsUserId:               String,
    val awsKeypairName:          String,
    val awsCredentialsPath:      String,
    val privateKeyPath:          String,
    val clusterSize:             Int,
    val queue:                   Option[String],
    override val openMOLEMemory: Option[Int],
    val wallTime:                Option[Duration],
    val memory:                  Option[Int],
    override val threads:        Option[Int],
    override val name:           Option[String]
) extends ClusterEnvironment with MemoryRequirement { env ⇒

  // Avoid exposing these in the API
  val port = 22
  val sharedDirectory = Some(Starcluster.SharedHome)
  val storageSharedLocally = false

  type JS = AWSJobService

  def user = Starcluster.MasterUser
  def host = jobService.jobService.starcluster.masterIp
  def credential = sshPrivateKey(PrivateKey(user, new File(jobService.jobService.starcluster.privateKeyPath), ""))

  val config = new GSAWSJobService.Config(region, awsUserName, awsUserId, awsKeypairName, awsCredentialsPath, privateKeyPath, clusterSize)

  @transient lazy val jobService = new AWSJobService with ThisHost {
    override lazy val jobService: GSAWSJobService = createAWSJobService(this, config)
    def queue = env.queue
    def environment = env
    def sharedFS = storage
    def id = url.toString
    def sharedDirectory = jobService.home
    def workDirectory = Some(jobService.home)
  }

  def createAWSJobService(js: AWSJobService, config: GSAWSJobService.Config) = {
    val (keyId, secretKey) = readAWSCredentials(config.awsUserName, config.awsCredentialsPath)
    val starclusterConfig = new Starcluster.Config(
      awsKeyId = keyId,
      awsSecretKey = secretKey,
      awsUserId = config.awsUserId,
      privateKeyPath = config.privateKeyPath,
      instanceType = DefaultInstanceType,
      size = config.clusterSize
    )

    try {
      val gsAWSJobService = new GSAWSJobService with SSHConnectionCache {
        override val credential = createCredential(config.privateKeyPath)
        override lazy val host = createHost(coordinator)
        override val port = js.port
        override val timeout = Workspace.preference(SSHService.timeout)
        override val region = config.region
        override val awsKeypairName = config.awsKeypairName
        override val client = createClient(keyId, secretKey)
        override val starcluster = Starcluster(this, starclusterConfig)
        override lazy val sge = createSGEJobService(starcluster)
      }

      gsAWSJobService.start()

      Log.logger.log(Log.FINE, "AWSJobService started.")

      gsAWSJobService
    }
    catch {
      case ex: Throwable ⇒ {
        Log.logger.log(Log.SEVERE, "The AWSJobService could not be started.", ex)
        throw ex
      }
    }
  }

  override def close() = {
    jobService.jobService.close()
  }
}
