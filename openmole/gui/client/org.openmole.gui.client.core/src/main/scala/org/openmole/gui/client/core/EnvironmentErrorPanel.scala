package org.openmole.gui.client.core

import fr.iscpif.scaladget.stylesheet.{ all ⇒ sheet }
import org.openmole.gui.ext.data._
import fr.iscpif.scaladget.api.{ BootstrapTags ⇒ bs }
import org.openmole.gui.misc.utils.{ stylesheet, Utils }
import org.scalajs.dom.html.TableSection
import org.scalajs.dom.raw.HTMLTableElement
import scalatags.JsDom.{ TypedTag, tags ⇒ tags }
import org.openmole.gui.misc.js.JsRxTags._
import scalatags.JsDom.all._
import sheet._
import bs._
import rx._

/*
 * Copyright (C) 27/07/15 // mathieu.leclaire@openmole.org
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

object EnvironmentErrorPanel {
  def apply = new EnvironmentErrorPanel
}

class EnvironmentErrorPanel {

  val scrollable = scrollableDiv()
  val sortingAndOrdering: Var[ListSortingAndOrdering] = Var(ListSortingAndOrdering(TimeSorting, Descending))
  val entries: Var[TypedTag[TableSection]] = Var(tbody)

  val topTriangle = glyph_triangle_top +++ (fontSize := 10)
  val bottomTriangle = glyph_triangle_bottom +++ (fontSize := 10)

  def exclusiveButton(title: String, action1: () ⇒ Unit, action2: () ⇒ Unit) = exclusiveButtonGroup(emptyMod)(
    ExclusiveButton.twoGlyphSpan(
      topTriangle,
      bottomTriangle,
      action1,
      action2,
      preString = title
    )
  ).div

  def setSorting(sorting: ListSorting, ordering: ListOrdering) = sortingAndOrdering() = ListSortingAndOrdering(sorting, ordering)

  def sort(datedErrors: EnvironmentErrorData, sortingAndOrdering: ListSortingAndOrdering): Seq[(String, Long, String)] = {

    val lines = for {
      errors ← datedErrors.datedErrors
      (error, dates) = (errors._1, errors._2)
      date ← dates
    } yield {
      (error.errorMessage, date, error.level.name)
    }

    val sorted = sortingAndOrdering.fileSorting match {
      case AlphaSorting ⇒ lines.sortBy(_._1)
      case TimeSorting  ⇒ lines.sortBy(_._2)
      case _            ⇒ lines.sortBy(_._3)
    }

    sortingAndOrdering.fileOrdering match {
      case Ascending ⇒ sorted
      case _         ⇒ sorted.reverse
    }
  }

  def setErrors(ers: EnvironmentErrorData) = {
    val stacks = ers.datedErrors.map(_._1).groupBy(_.errorMessage).map { case (k, v) ⇒ k → v.head.stack }
    entries() = tbody(
      for {
        error ← sort(ers, sortingAndOrdering())
      } yield {
        tags.tr(row +++ stylesheet.errorTable)(
          tags.td(colMD(12))(
            tags.a(error._1, cursor := "pointer", onclick := {
              () ⇒
                panels.environmentStackPanel.content() = stacks(error._1).stackTrace
                panels.environmentStackTriggerer.open
            })
          ),
          tags.td(colMD(1))(Utils.longToDate(error._2).split(",").last),
          tags.td(colMD(1))(label(error._3)(label_default))
        )
      }
    )
  }

  val view = {
    val errorTable = tags.table(fontSize := "0.96em", width := "100%")(
      thead(
        tr(row)(
          th(exclusiveButton("Error", () ⇒ setSorting(AlphaSorting, Ascending), () ⇒ setSorting(AlphaSorting, Descending))),
          th(exclusiveButton("Date", () ⇒ setSorting(TimeSorting, Ascending), () ⇒ setSorting(TimeSorting, Descending))),
          th(exclusiveButton("Level", () ⇒ setSorting(LevelSorting, Ascending), () ⇒ setSorting(LevelSorting, Descending)))
        )
      ), Rx {
        entries()
      }
    )

    scrollable.setChild(div(stylesheet.environmentPanelError)(errorTable).render)
    scrollable.sRender
  }

}

