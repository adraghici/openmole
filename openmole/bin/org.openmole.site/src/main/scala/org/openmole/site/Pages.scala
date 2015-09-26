
/*
 * Copyright (C) 2014 Romain Reuillon
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.site

import org.openmole.site.market._

import scalatags.Text.all
import scalatags.Text.all._
import com.github.rjeschke._

import scalatags.generic.Attr

object Pages {

  def decorate(p: Page): Frag =
    p match {
      case p: DocumentationPage ⇒ DocumentationPages.decorate(p)
      case _                    ⇒ decorate(p.content)
    }

  def decorate(p: Frag): Frag =
    div(`class` := "container")(
      div(`class` := "header pull-center")(
        div(`class` := "title")(a(img(id := "logo", src := Resource.logo.file), href := index.file)),
        ul(id := "sections", `class` := "nav nav-pills")(
          li(a("Getting Started", `class` := "amenu", id := "section", href := gettingStarted.file)),
          li(a("Documentation", `class` := "amenu", id := "section", href := DocumentationPages.root.file)),
          li(a("Who are we?", `class` := "amenu", id := "section", href := whoAreWe.file))
        )
      ),
      div(`class` := "row")(p)
    )

  def index = Page("index",
    Seq(scalatags.Text.tags2.title("OpenMOLE: scientific workflow, distributed computing, parameter tuning")),
    Index())
  def gettingStarted = Page("getting_started",
    Seq(scalatags.Text.tags2.title("Getting started with OpenMOLE - introductory tutorial")),
    GettingStarted())
  def whoAreWe = Page("who_are_we",
    Seq(scalatags.Text.tags2.title("Developers, reference publications, contact information - OpenMOLE")),
    WhoAreWe())
  def communications = Page("communications",
    Seq(scalatags.Text.tags2.title("Related papers, conference slides, videos, OpenMOLE in the news")),
    Communications())

  def all: Seq[Page] = DocumentationPages.allPages ++ Seq(index, gettingStarted, whoAreWe, communications)

}

object Page {
  def apply(_name: String, _header: Seq[Frag], _content: Frag) =
    new Page {
      override def name: String = _name
      override def header: Seq[Frag] = _header
      override def content: all.Frag = _content
    }
}

trait Page {
  def header: Seq[Frag]
  def content: Frag
  def name: String

  def location = name
  def file = location + ".html"
}

case class Parent[T](parent: Option[T])

abstract class DocumentationPage(implicit p: Parent[DocumentationPage] = Parent(None)) extends Page {

  def header = Seq(scalatags.Text.tags2.title("Generic title TODO change"))

  def parent = p.parent
  implicit def thisIsParent = Parent[DocumentationPage](Some(this))

  def children: Seq[DocumentationPage]

  def apply() = content

  override def location: String =
    parent match {
      case None    ⇒ name
      case Some(p) ⇒ p.location + "_" + name
    }

  def allPages: Seq[DocumentationPage] = {
    def pages(p: DocumentationPage): List[DocumentationPage] =
      p.children.toList ::: p.children.flatMap(_.allPages).toList
    this :: pages(this)
  }

  override def equals(o: scala.Any): Boolean =
    o match {
      case p2: DocumentationPage ⇒ this.location == p2.location
      case _                     ⇒ false
    }

  override def hashCode(): Int = location.hashCode()
}

object DocumentationPages { index ⇒

  var marketEntries: Seq[GeneratedMarketEntry] = Seq()

  def tag(p: DocumentationPage): String = p.name + p.parent.map(pa ⇒ "-" + tag(pa)).getOrElse("")

  def decorate(p: DocumentationPage): Frag =
    Pages.decorate(
      Seq(
        div(id := "documentation-content", `class` := "row")(
          div(`class` := "col-sm-3")(documentationMenu(root, p)),
          div(`class` := "col-sm-9", id := "documentation-text")(div(p.content, if (p != root) bottomLinks(p) else ""))
        )
      )
    )

  def documentationMenu(root: DocumentationPage, currentPage: DocumentationPage): Frag = {
    def menuEntry(p: DocumentationPage) = {
      def current = p == currentPage
      def idLabel = "documentation-menu-entry" + (if (current) "-current" else "")
      a(p.name, href := p.file)
    }

    def parents(p: DocumentationPage): List[DocumentationPage] =
      p.parent match {
        case None         ⇒ Nil
        case Some(parent) ⇒ parent :: parents(parent)
      }

    val currentPageParents = parents(currentPage).toSet

    def pageLine(p: DocumentationPage): Frag = {

      def contracted = li(menuEntry(p))
      def expanded =
        li(
          menuEntry(p),
          div(id := tag(p) + "-menu", ul(id := "documentation-menu-ul")(p.children.map(pageLine)))
        )

      if (p.children.isEmpty) contracted
      else if (p == currentPage) expanded
      else if (currentPageParents.contains(p)) expanded
      else contracted
    }

    div(id := "documentation-menu")(
      root.children.map(pageLine)
    )
  }

  def bottomLinks(p: DocumentationPage) = {
    def previous(p: DocumentationPage): Option[DocumentationPage] =
      p.parent match {
        case None ⇒ None
        case Some(parent) ⇒
          parent.children.indexOf(p) match {
            case x if (x - 1) < 0 ⇒ None
            case x                ⇒ Some(parent.children(x - 1))
          }
      }

    def next(p: DocumentationPage): Option[DocumentationPage] =
      p.parent match {
        case None ⇒ None
        case Some(parent) ⇒
          parent.children.indexOf(p) match {
            case x if (x + 1 >= parent.children.size) || (x == -1) ⇒ None
            case x ⇒ Some(parent.children(x + 1))
          }
      }

    def up(p: DocumentationPage): Option[DocumentationPage] = p.parent

    table(id := "documentation-bottom-links")(
      Seq("previous" -> previous(p), "up" -> up(p), "next" -> next(p)).map {
        case (t, None)       ⇒ td(id := "documentation-bottom-link-unavailable")(t)
        case (item, Some(p)) ⇒ td(id := "documentation-bottom-link")(a(item, href := p.file))
      }
    )
  }

  def allPages = root.allPages

  def root = new DocumentationPage {
    def name = "Documentation"
    def content = documentation.Documentation()
    def children = Seq(application, language, tutorial, market, faq, development)

    def application = new DocumentationPage {
      def name = "Application"
      def children = Seq()
      def content = documentation.Application()
    }

    def language =
      new DocumentationPage {
        def name = "Language"
        def children = Seq(task, sampling, transition, hook, environment, source, method)
        def content = documentation.Language()

        def task = new DocumentationPage {
          def name = "Tasks"
          def children = Seq(scala, systemExec, netLogo, mole)
          def content = documentation.language.Task()

          def scala = new DocumentationPage {
            def name = "Scala"
            def children = Seq()
            def content = documentation.language.task.Scala()
          }

          def systemExec = new DocumentationPage {
            def name = "SystemExec"
            def children = Seq()
            def content = documentation.language.task.SystemExec()
          }

          def netLogo = new DocumentationPage {
            def name = "NetLogo"
            def children = Seq()
            def content = documentation.language.task.NetLogo()
          }

          def mole = new DocumentationPage {
            def name = "Mole"
            def children = Seq()
            def content = documentation.language.task.MoleTask()
          }
        }

        def sampling = new DocumentationPage {
          def name = "Samplings"
          def children = Seq()
          def content = documentation.language.Sampling()
        }

        def transition = new DocumentationPage {
          def name = "Transitions"
          def children = Seq()
          def content = documentation.language.Transition()
        }

        def hook = new DocumentationPage {
          def name = "Hooks"
          def children = Seq()
          def content = documentation.language.Hook()
        }

        def environment = new DocumentationPage {
          def name = "Environments"
          def children = Seq(multithread, ssh, egi, cluster, desktopGrid)
          def content = documentation.language.Environment()

          def multithread = new DocumentationPage {
            def name = "Multi-threads"
            def children = Seq()
            def content = documentation.language.environment.Multithread()
          }

          def ssh = new DocumentationPage {
            def name = "SSH"
            def children = Seq()
            def content = documentation.language.environment.SSH()
          }

          def egi = new DocumentationPage {
            def name = "EGI"
            def children = Seq()
            def content = documentation.language.environment.EGI()
          }

          def cluster = new DocumentationPage {
            def name = "Clusters"
            def children = Seq()
            def content = documentation.language.environment.Cluster()
          }

          def desktopGrid = new DocumentationPage {
            def name = "Desktop Grid"
            def children = Seq()
            def content = documentation.language.environment.DesktopGrid()
          }

        }

        def source = new DocumentationPage {
          def name = "Sources"
          def children = Seq()
          def content = documentation.language.Source()
        }

        def method = new DocumentationPage {
          def name = "Exploration Methods"
          def children = Seq()
          def content = documentation.language.Method()
        }
      }

    def tutorial = new DocumentationPage {
      def name = "Tutorials"
      def children = Seq(helloWorld, headlessNetLogo, netLogoGA, capsule)
      def content = documentation.language.Tutorial()

      def helloWorld = new DocumentationPage {
        def name = "Hello World"
        def children = Seq()
        def content = Pages.gettingStarted.content
      }

      def headlessNetLogo = new DocumentationPage {
        def name = "NetLogo Headless"
        def children = Seq()
        def content = documentation.language.tutorial.HeadlessNetLogo()
      }

      def netLogoGA = new DocumentationPage {
        def name = "GA with NetLogo"
        def children = Seq()
        def content = documentation.language.tutorial.NetLogoGA()
      }

      def capsule = new DocumentationPage {
        def name = "Capsule"
        def children = Seq()
        def content = documentation.language.tutorial.Capsule()
      }
    }

    def market = new DocumentationPage {
      def children: Seq[DocumentationPage] = pages
      def name: String = "Market Place"
      def content: all.Frag = documentation.Market()

      def themes: Seq[Market.Tag] =
        marketEntries.flatMap(_.entry.tags).distinct.sortBy(_.label.toLowerCase)

      def allEntries =
        new DocumentationPage {
          def children: Seq[DocumentationPage] = Seq()
          def name: String = "All"
          def content: all.Frag = tagContent("All", marketEntries)
        }

      def pages = allEntries :: (themes map documentationPage).toList

      def documentationPage(t: Market.Tag) =
        new DocumentationPage {
          def children: Seq[DocumentationPage] = Seq()
          def name: String = t.label
          def content: all.Frag =
            tagContent(t.label, marketEntries.filter(_.entry.tags.contains(t)))
        }

      def tagContent(label: String, entries: Seq[GeneratedMarketEntry]) =
        Seq(
          h1(label),
          ul(
            entries.sortBy(_.entry.name.toLowerCase).map {
              de ⇒ li(entryContent(de))
            }: _*
          )
        )

      def entryContent(deployedMarketEntry: GeneratedMarketEntry) = {
        Seq(
          a(deployedMarketEntry.entry.name, href := deployedMarketEntry.archive),
          p(
            div(id := "market-entry")(
              Seq(
                deployedMarketEntry.readme.map {
                  rm ⇒ RawFrag(txtmark.Processor.process(rm))
                }.getOrElse(p("No README.md available yet."))
              ) ++ deployedMarketEntry.viewURL.map {
                  u ⇒ a("source repository", href := u)
                }: _*
            )
          )
        )
      }

    }

    def faq = new DocumentationPage {
      def name = "FAQ"
      def children = Seq()
      def content = documentation.FAQ()
    }

    def development = new DocumentationPage {
      def name = "Development"
      def children = Seq(compilation, plugin, branching, webserver)
      def content = documentation.Development()

      def compilation = new DocumentationPage {
        def name = "Compilation"
        def children = Seq()
        def content = documentation.development.Compilation()
      }

      def plugin = new DocumentationPage {
        def name = "Plugins"
        def children = Seq()
        def content = documentation.development.Plugin()
      }

      def branching = new DocumentationPage {
        def name = "Branching model"
        def children = Seq()
        def content = documentation.development.Branching()
      }

      def webserver = new DocumentationPage {
        def name = "Web Server"
        def children = Seq()
        def content = documentation.development.WebServer()
      }
    }
  }
}
