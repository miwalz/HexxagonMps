package de.htwg.mps.hexxagon.model

import de.htwg.mps.hexxagon.model.Filling.Filling

object Filling extends Enumeration {
  type Filling = Value
  val disabled, empty, p1, p2, duplicate, jump = Value
}

class Field(val filling: Filling)