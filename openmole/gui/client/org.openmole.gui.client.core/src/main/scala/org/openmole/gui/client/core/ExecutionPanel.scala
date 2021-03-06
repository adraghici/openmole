package org.openmole.gui.client.core

/*
 * Copyright (C) 17/05/15 // mathieu.leclaire@openmole.org
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

import java.util.concurrent.atomic.AtomicBoolean

import fr.iscpif.scaladget.api.BootstrapTags.ScrollableTextArea.BottomScroll
import fr.iscpif.scaladget.api.{ BootstrapTags ⇒ bs }
import org.openmole.gui.misc.utils.{ stylesheet ⇒ omsheet, Utils }
import org.openmole.gui.shared.Api
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }
import scalatags.JsDom.all._
import org.openmole.gui.misc.js.{ _ }
import org.openmole.gui.misc.js.Expander._
import scalatags.JsDom._
import org.openmole.gui.misc.js.JsRxTags._
import scala.scalajs.js.timers._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import fr.iscpif.scaladget.stylesheet.{ all ⇒ sheet }
import sheet._
import autowire._
import org.openmole.gui.ext.data.{ Error ⇒ ExecError }
import org.openmole.gui.ext.data._
import bs._
import rx._
import concurrent.duration._

class ExecutionPanel extends ModalPanel {
  lazy val modalID = "executionsPanelID"

  case class PanelInfo(
    executionInfos: Seq[(ExecutionId, ExecutionInfo)],
    outputsInfos:   Seq[RunningOutputData]
  )

  val execInfo = Var(PanelInfo(Seq(), Seq()))
  val staticInfo: Var[Map[ExecutionId, StaticExecutionInfo]] = Var(Map())
  var envError: Var[Map[EnvironmentId, EnvironmentErrorData]] = Var(Map())
  val expander = new Expander

  val updating = new AtomicBoolean(false)

  def updateExecutionInfo: Unit = {
    def delay = {
      updating.set(false)
      setTimeout(5000) {
        if (isVisible) updateExecutionInfo
      }
    }

    if (updating.compareAndSet(false, true)) {
      OMPost[Api].allStates(outputHistory.value.toInt).call().andThen {
        case Success((executionInfos, runningOutputData)) ⇒
          execInfo() = PanelInfo(executionInfos, runningOutputData)
          doScrolls
          delay
        case Failure(_) ⇒ delay
      }
    }
  }

  def updateStaticInfos = OMPost[Api].staticInfos.call().foreach { s ⇒
    staticInfo() = s.toMap
    setTimeout(0) {
      updateExecutionInfo
    }
  }

  def onOpen() = {
    updateStaticInfos
  }

  def onClose() = {}

  def doScrolls = {
    Seq(outputTextAreas(), scriptTextAreas(), errorTextAreas()).map {
      _.values.foreach {
        _.doScroll
      }
    }
    envErrorPanels().values.foreach {
      _.scrollable.doScroll
    }
  }

  case class ExecutionDetails(
    ratio:     String,
    running:   Long,
    error:     Option[ExecError]     = None,
    envStates: Seq[EnvironmentState] = Seq(),
    outputs:   String                = ""
  )

  val scriptTextAreas: Var[Map[ExecutionId, ScrollableText]] = Var(Map())
  val errorTextAreas: Var[Map[ExecutionId, ScrollableText]] = Var(Map())
  val outputTextAreas: Var[Map[ExecutionId, ScrollableText]] = Var(Map())
  val envErrorPanels: Var[Map[EnvironmentId, EnvironmentErrorPanel]] = Var(Map())

  def staticPanel[T, I <: ID](id: I, panelMap: Var[Map[I, T]], builder: () ⇒ T, appender: T ⇒ Unit = (t: T) ⇒ {}): T = {
    if (panelMap().isDefinedAt(id)) {
      val t = panelMap()(id)
      appender(t)
      t
    }
    else {
      val toBeAdded = builder()
      panelMap() = panelMap() + (id → toBeAdded)
      toBeAdded
    }
  }

  val envLevel: Var[ErrorStateLevel] = Var(ErrorLevel())

  val outputHistory = bs.labeledInput("# outputs", "500", "# outputs", labelStyle = color := "#000")
  val envErrorHistory = bs.labeledInput("# envirnoment errors", "500", "# envirnoment errors", labelStyle = color := "#000")

  def ratio(completed: Long, running: Long, ready: Long) = s"${completed} / ${completed + running + ready}"

  val envErrorVisible: Var[Seq[EnvironmentId]] = Var(Seq())

  def glyphAndText(mod: ModifierSeq, text: String) = tags.span(
    tags.span(mod),
    s" $text"
  )

  lazy val executionTable = {
    val scriptID: VisibleID = "script"
    val envID: VisibleID = "env"
    val errorID: VisibleID = "error"
    val outputStreamID: VisibleID = "outputStream"
    tags.table(sheet.table)(
      thead,
      Rx {
        tbody({
          for {
            (id, executionInfo) ← execInfo().executionInfos.sortBy { case (execId, _) ⇒ staticInfo()(execId).startDate }.reverse
          } yield {

            val duration: Duration = (executionInfo.duration milliseconds)
            val h = (duration).toHours
            val m = ((duration) - (h hours)).toMinutes
            val s = (duration - (h hours) - (m minutes)).toSeconds

            val durationString = s"""${h.formatted("%d")}:${m.formatted("%02d")}:${s.formatted("%02d")}"""

            val completed = executionInfo.completed

            val details = executionInfo match {
              case f: Failed   ⇒ ExecutionDetails("0", 0, Some(f.error))
              case f: Finished ⇒ ExecutionDetails(ratio(f.completed, f.running, f.ready), f.running, envStates = f.environmentStates)
              case r: Running  ⇒ ExecutionDetails(ratio(r.completed, r.running, r.ready), r.running, envStates = r.environmentStates)
              case c: Canceled ⇒ ExecutionDetails("0", 0)
              case r: Ready    ⇒ ExecutionDetails("0", 0)
            }

            val scriptLink = expander.getLink(staticInfo()(id).path.name, id.id, scriptID)
            val envLink = expander.getGlyph(glyph_stats, "Env", id.id, envID)
            val stateLink = executionInfo match {
              case f: Failed ⇒ expander.getLink(executionInfo.state, id.id, errorID).render
              case _         ⇒ tags.span(executionInfo.state).render
            }
            val outputLink = expander.getGlyph(glyph_list, "", id.id, outputStreamID, () ⇒ doScrolls)

            lazy val hiddenMap: Map[VisibleID, Modifier] = Map(
              scriptID → staticPanel(id, scriptTextAreas,
                () ⇒ scrollableText(staticInfo()(id).script)).view,
              envID → {
                details.envStates.map { e ⇒
                  tags.table(sheet.table)(
                    thead,
                    tbody(
                      Seq(
                        tr(row)(
                          td(colMD(3))(tags.span(e.taskName)),
                          td(colMD(2))(glyphAndText(glyph_upload, s" ${e.networkActivity.uploadingFiles} ${displaySize(e.networkActivity.uploadedSize, e.networkActivity.readableUploadedSize)}")),
                          td(colMD(2))(glyphAndText(glyph_download, s" ${e.networkActivity.downloadingFiles} ${displaySize(e.networkActivity.downloadedSize, e.networkActivity.readableDownloadedSize)}")),
                          td(colMD(2))(glyphAndText(glyph_road +++ sheet.paddingBottom(7), e.submitted.toString)),
                          td(colMD(1))(glyphAndText(glyph_flash +++ sheet.paddingBottom(7), e.running.toString)),
                          td(colMD(1))(glyphAndText(glyph_flag +++ sheet.paddingBottom(7), e.done.toString)),
                          td(colMD(1))(glyphAndText(glyph_fire +++ sheet.paddingBottom(7), e.failed.toString)),
                          td(colMD(3))(tags.span(omsheet.color("#3086b5") +++ ((envErrorVisible().contains(e.envId)), ms(" executionVisible"), emptyMod))(
                            sheet.pointer, onclick := { () ⇒
                            if (envErrorVisible().contains(e.envId)) envErrorVisible() = envErrorVisible().filterNot {
                              _ == e.envId
                            }
                            else envErrorVisible() = envErrorVisible() :+ e.envId
                          }
                          )("details"))
                        ),
                        tr(row)(
                          {
                            td(colMD(12) +++ (!envErrorVisible().contains(e.envId), omsheet.displayOff, emptyMod))(
                              colspan := 12,
                              bs.buttonGroup(omsheet.centerElement)(
                                bs.button("Update", () ⇒ updateEnvErrors(e.envId, false)),
                                bs.button("Reset", () ⇒ updateEnvErrors(e.envId, true))
                              ),
                              staticPanel(e.envId, envErrorPanels,
                                () ⇒ new EnvironmentErrorPanel,
                                (ep: EnvironmentErrorPanel) ⇒
                                  ep.setErrors(envError().getOrElse(e.envId, EnvironmentErrorData.empty))).view
                            )
                          }
                        )
                      )
                    )
                  )
                }
              },
              errorID →
                div(
                  omsheet.monospace,
                  staticPanel(
                  id,
                  errorTextAreas,
                  () ⇒ scrollableText(),
                  (sT: ScrollableText) ⇒ sT.setContent(new String(details.error.map {
                    _.stackTrace
                  }.getOrElse("")))
                ).view
                ),
              outputStreamID → staticPanel(
                id,
                outputTextAreas,
                () ⇒ scrollableText("", BottomScroll),
                (sT: ScrollableText) ⇒ sT.setContent(
                  execInfo().outputsInfos.filter {
                    _.id == id
                  }.map {
                    _.output
                  }.mkString("\n")
                )
              ).view
            )

            Seq(
              tr(row +++ omsheet.executionTable, colspan := 12)(
                td(colMD(2))(visibleClass(id.id, scriptID))(scriptLink),
                td(colMD(2))(div(Utils.longToDate(staticInfo()(id).startDate))),
                td(colMD(2))(glyphAndText(glyph_flash, details.running.toString)),
                td(colMD(2))(glyphAndText(glyph_flag, details.ratio.toString)),
                td(colMD(1))(div(durationString)),
                td(colMD(1))(ms(executionInfo.state + "State"))(stateLink),
                td(colMD(1))(visibleClass(id.id, envID))(envLink),
                td(colMD(1))(visibleClass(id.id, outputStreamID))(outputLink),
                td(colMD(1))(tags.span(glyph_remove +++ ms("removeExecution"), onclick := { () ⇒
                  OMPost[Api].cancelExecution(id).call().foreach { r ⇒
                    updateExecutionInfo
                  }
                })),
                td(colMD(1))(tags.span(glyph_trash +++ ms("removeExecution"), onclick := { () ⇒
                  OMPost[Api].removeExecution(id).call().foreach { r ⇒
                    updateExecutionInfo
                  }
                }))
              ),
              tr(row)(
                expander.getVisible(id.id) match {
                  case Some(v: VisibleID) ⇒ td(colspan := 12)(hiddenMap(v))
                  case _                  ⇒ div()
                }
              )
            )
          }
        })
      }
    ).render
  }

  def updateEnvErrors(environmentId: EnvironmentId, reset: Boolean) = {
    OMPost[Api].runningErrorEnvironmentData(environmentId, envErrorHistory.value.toInt, reset).call().foreach { err ⇒
      envError() = envError() + (environmentId → err)
    }
  }

  def displaySize(size: Long, readable: String) =
    if (size == 0L) ""
    else s"($readable)"

  def visibleClass(expandID: ExpandID, visibleID: VisibleID): ModifierSeq = (expander.isVisible(expandID, visibleID), omsheet.executionVisible, pointer)

  val settingsButton = tags.span(
    btn_default +++ glyph_settings +++ omsheet.settingsButton
  )(tags.span(caret))

  val settingsDiv = tags.div(width := 200)(
    outputHistory.render,
    envErrorHistory.render
  )

  val dialog = bs.modalDialog(
    modalID,
    headerDialog(
      div(height := 55)(
        b("Executions"),
        div(omsheet.executionHeader)(
          settingsButton
        ).popup(
          settingsDiv,
          onclose = () ⇒ {},
          popupStyle = whitePopupWithBorder
        )
      )
    ),
    bodyDialog(ms("executionTable"))(
      executionTable
    ),
    footerDialog(
      closeButton
    )
  )

}