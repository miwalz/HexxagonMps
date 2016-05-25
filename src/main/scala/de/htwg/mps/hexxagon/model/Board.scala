package de.htwg.mps.hexxagon.model

class Board(val fields: Array[Array[Field]]) {

  def getField(x: Int, y: Int) = {
    fields(y)(x)
  }

  override def toString = {
    val strBuilder = new StringBuilder
    strBuilder ++= "    0 1 2 3 4 5 6 7 8\n---------------------\n"
    for (row <- fields.indices) {
      strBuilder ++= row + " |"
      for (col <- fields(row).indices) {
        fields(row)(col).filling match {
          case Filling.disabled => strBuilder ++= " X"
          case Filling.empty => strBuilder ++= " O"
          case Filling.p1 => strBuilder ++= " 1"
          case Filling.p2 => strBuilder ++= " 2"
          case Filling.jump => strBuilder ++= " J"
          case Filling.duplicate => strBuilder ++= " C"
        }
      }
      strBuilder ++= "\n"
    }
    strBuilder.toString()
  }
}