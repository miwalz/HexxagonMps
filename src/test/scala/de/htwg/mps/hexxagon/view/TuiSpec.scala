package de.htwg.mps.hexxagon.view

import de.htwg.mps.hexxagon.controller.HexxagonController
import de.htwg.mps.hexxagon.model.Filling
import de.htwg.mps.hexxagon.view.tui.Tui
import org.specs2._

class TuiSpec extends mutable.Specification {

  step {
    // setup
  }

  val controller = HexxagonController()
  val tui = new Tui(controller)

  "Function processInput" should {

    "do a move if input is valid" in {
      controller.init("one", "two")
      tui.processInput("02")
      tui.processInput("03")
      controller.board.getField(0, 3).filling must beEqualTo(Filling.p1)
    }

  }

  step {
    // tear down
  }

}