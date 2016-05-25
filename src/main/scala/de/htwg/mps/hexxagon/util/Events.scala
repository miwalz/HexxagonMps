package de.htwg.mps.hexxagon.util

import scala.swing.event.Event

case class BoardChanged() extends Event

case class GameOver() extends Event