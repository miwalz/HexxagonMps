package de.htwg.mps.hexxagon

import de.htwg.mps.hexxagon.controller.HexxagonController
import de.htwg.mps.hexxagon.view.gui.Gui
import de.htwg.mps.hexxagon.view.tui.Tui

object Hexxagon {

  val controller = HexxagonController()

  def main(args: Array[String]) {

    new Thread(new Runnable {
      override def run() = {
        new Tui(controller).init()
      }
    }).start()

    new Thread(new Runnable {
      override def run() = {
        new Gui(controller).main(Array())
      }
    }).start()

  }

}