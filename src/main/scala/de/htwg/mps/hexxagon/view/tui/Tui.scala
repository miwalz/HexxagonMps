package de.htwg.mps.hexxagon.view.tui

import de.htwg.mps.hexxagon.controller.HexxagonController
import de.htwg.mps.hexxagon.util.{GameOver, BoardChanged}

import scala.swing.Reactor
import scala.io.StdIn.readLine

class Tui(controller: HexxagonController) extends Reactor {

  // listen to controller for changes
  listenTo(controller)
  reactions += {
    case e: BoardChanged => printBoard()
    case e: GameOver => printGameOver()
  }

  // inital draw
  printBoard()

  def init() = {
    while (processInput(readLine())) {}
  }

  def processInput(input: String) = {
    println("Input:")
    var continue = true
    input match {
      case "exit" => continue = false
      case "new" => controller.init(controller.p1.name, controller.p2.name)
      case _ =>
        input.toList.filter(c => c != ' ').map(c => c.toString.toInt) match {
          case x :: y :: Nil => controller.input(x, y)
          case _ => println("Invalid input.")
        }
    }
    continue
  }

  def inputNames = {
    println("\nName Player 1:")
    val p1Name = readLine()
    println("Name Player 2:")
    val p2Name = readLine()
    (p1Name, p2Name)
  }

  def printBoard() = {
    println("\n" + controller.p1.name + " " + controller.p1.score + " - " + controller.p2.score + " " + controller.p2.name)
    print("Turn: ")
    println(if (controller.currPlayerIndex == 0) controller.p1.name else controller.p2.name)
    println("\n" + controller.board + "\n\n")
  }

  def printGameOver() = {
    def winnerResult = controller.p1.score compare controller.p2.score
    def winnerMsg = winnerResult match {
      case 0 => "The game ended in a draw"
      case 1 => controller.p1.name
      case -1 => controller.p2.name
    }
    println("=====================")
    println(" GAME OVER")
    print(" ")
    if (winnerResult != 0) print("Winner: ")
    println(winnerMsg)
    println("=====================")
  }

}