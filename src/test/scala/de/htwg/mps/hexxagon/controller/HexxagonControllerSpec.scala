package de.htwg.mps.hexxagon.controller

import de.htwg.mps.hexxagon.util.{GameOver, BoardChanged}
import org.specs2._

import scala.swing.{Publisher, Reactor}

class HexxagonControllerSpec extends mutable.Specification with Publisher with Reactor {

  step {
    // setup
  }

  "Function init" should {
    val controller = HexxagonController("one", "two")

    "start a new game" in {
      controller.init("one", "two")
      controller.p1.name must beEqualTo("one")
      controller.p2.name must beEqualTo("two")
      controller.currPlayerIndex must beEqualTo(0)
      controller.selectedIndices must beEqualTo((0, 0))
    }

  }

  "Function input" should {
    val controller = HexxagonController()

    "do move if input is valid" in {
      controller.input(0, 2)
      controller.selectedIndices must beEqualTo((0, 2))
      controller.input(0, 3)
      controller.selectedIndices must beEqualTo((0, 2))
    }

    "do nothing if move is invalid" in {
      controller.input(4, 4) // empty field
      controller.selectedIndices must beEqualTo((0, 2))
    }

    "do nothing if input is invalid" in {
      controller.input(0, -1)
      controller.selectedIndices must beEqualTo((0, 2))
    }

  }

  "Function isValidIndices" should {
    val controller = HexxagonController()

    "return false when index out of range" in {
      controller.isValidIndices(-1, 0) must beFalse
      controller.isValidIndices(0, -1) must beFalse
      controller.isValidIndices(9, 0) must beFalse
      controller.isValidIndices(0, 9) must beFalse
      controller.isValidIndices(0, 8) must beTrue
      controller.isValidIndices(8, 0) must beTrue
    }
  }

  "Function getFieldsAsList" should {
    val controller = HexxagonController()

    "return the board as list of fields" in {
      val target = "List((0,0,disabled), (0,1,disabled), (0,2,p1), (0,3,empty), (0,4,empty), (0,5,empty), (0,6,p2), (0,7,disabled), (0,8,disabled), (1,0,disabled), (1,1,empty), (1,2,empty), (1,3,empty), (1,4,empty), (1,5,empty), (1,6,empty), (1,7,disabled), (1,8,disabled), (2,0,disabled), (2,1,empty), (2,2,empty), (2,3,empty), (2,4,empty), (2,5,empty), (2,6,empty), (2,7,empty), (2,8,disabled), (3,0,empty), (3,1,empty), (3,2,empty), (3,3,empty), (3,4,disabled), (3,5,empty), (3,6,empty), (3,7,empty), (3,8,disabled), (4,0,p2), (4,1,empty), (4,2,empty), (4,3,disabled), (4,4,empty), (4,5,empty), (4,6,empty), (4,7,empty), (4,8,p1), (5,0,empty), (5,1,empty), (5,2,empty), (5,3,empty), (5,4,disabled), (5,5,empty), (5,6,empty), (5,7,empty), (5,8,disabled), (6,0,disabled), (6,1,empty), (6,2,empty), (6,3,empty), (6,4,empty), (6,5,empty), (6,6,empty), (6,7,empty), (6,8,disabled), (7,0,disabled), (7,1,empty), (7,2,empty), (7,3,empty), (7,4,empty), (7,5,empty), (7,6,empty), (7,7,disabled), (7,8,disabled), (8,0,disabled), (8,1,disabled), (8,2,p1), (8,3,empty), (8,4,empty), (8,5,empty), (8,6,p2), (8,7,disabled), (8,8,disabled))"
      controller.getFieldsAsList.toString must beEqualTo(target)
    }

  }

  "Function gameOver" should {

    "return false at the beginning of a game" in {
      val controller = HexxagonController(HexxagonController.getFieldsFromFile("/map_standard"))
      controller.gameOver must beFalse
    }

    "return true if one player is killed" in {
      val controller = HexxagonController(HexxagonController.getFieldsFromFile("/map_gameover_killed"))
      controller.gameOver must beTrue
    }

    "return true if no empty field is left" in {
      val controller = HexxagonController(HexxagonController.getFieldsFromFile("/map_gameover_full"))
      controller.gameOver must beTrue
    }

    "return true if the active player is surrounded" in {
      val controller = HexxagonController(HexxagonController.getFieldsFromFile("/map_gameover_surrounded"))
      controller.gameOver must beTrue
    }

  }

  "Function publish" should {

    "throw GameOver and BoardChanges events" in {
      var eventCounter = 0
      listenTo(this)
      reactions += {
        case e: BoardChanged => eventCounter += 1
        case e: GameOver => eventCounter += 1
      }
      publish(new BoardChanged)
      publish(new GameOver)
      eventCounter must beEqualTo(2)
    }

  }

  step {
    // tear down
  }

}