package de.htwg.mps.hexxagon.view.gui

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

import de.htwg.mps.hexxagon.controller.HexxagonController
import de.htwg.mps.hexxagon.model.Filling.Filling
import de.htwg.mps.hexxagon.model.{Board, Filling}
import de.htwg.mps.hexxagon.util.BoardChanged

import scala.swing.Reactor
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Menu, MenuBar, MenuItem}
import scalafx.scene.layout.{BorderPane, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Polygon

class Gui(controller: HexxagonController) extends JFXApp with Reactor {

  val WindowWidth = 520
  val WindowHeight = 620
  val InsetsSize = 20
  val Width = 30
  val ScaleX = 5

  lazy val menu = new Menu("Hexxagon") {
    items = List(
      new MenuItem("Restart") {
        onAction = (e: ActionEvent) => controller.init(controller.p1.name, controller.p2.name)
      },
      new MenuItem("Exit") {
        onAction = (e: ActionEvent) => sys.exit(0)
      }
    )
  }

  lazy val menuBar = new MenuBar {
    useSystemMenuBar = true
    minWidth = WindowWidth
    menus.add(menu)
  }

  lazy val gridPane = new Pane {
    padding = Insets(InsetsSize)
    children = polygonGrid(controller.board, Width, ScaleX)
  }

  // listen to controller for changes
  listenTo(controller)
  reactions += {
    case e: BoardChanged => draw()
  }

  // main window stage
  stage = new PrimaryStage {
    title = "Hexxagon"
    scene = new Scene(WindowWidth, WindowHeight) {
      fill = Color.Black
      content = new BorderPane {
        top = menuBar
        bottom = gridPane
      }
    }
  }

  // initial draw
  draw()

  def draw() = gridPane.children = polygonGrid(controller.board, Width, ScaleX)

  def colorFromFilling(filling: Filling) = {
    filling match {
      case Filling.disabled => Color.Transparent
      case Filling.empty => Color.White
      case Filling.p1 => Color.Blue
      case Filling.p2 => Color.Red
      case Filling.jump => Color.Yellow
      case Filling.duplicate => Color.Green
    }
  }

  def polygonGrid(board: Board, width: Int, scale: Int) = for (col <- board.fields.indices; row <- board.fields.indices) yield {
    val sY = 50 + (row * 2 * (width + 1)) + (if (col % 2 == 0) 0 else width)
    val sX = 50 + (col * 2 * (width - scale + 1))
    val pol = Polygon(
      sX - width - scale, sY,
      sX - width / 2, sY + width,
      sX + width / 2, sY + width,
      sX + width + scale, sY,
      sX + width / 2, sY - width,
      sX - width / 2, sY - width
    )
    pol.setFill(colorFromFilling(controller.board.getField(col, row).filling))
    pol.onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(e: MouseEvent) = {
        controller.input(col, row)
      }
    }
    pol
  }
}