package de.htwg.mps.hexxagon.controller

import _root_.de.htwg.mps.hexxagon.model.Filling.Filling
import _root_.de.htwg.mps.hexxagon.model.{Board, Field, Filling, Player}
import de.htwg.mps.hexxagon.util.{BoardChanged, GameOver}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.swing.Publisher

object HexxagonController {

  def apply() =
    new HexxagonController(getInitialFields, "Player1", "Player2")

  def apply(namePlayer1: String, namePlayer2: String) =
    new HexxagonController(getInitialFields, namePlayer1, namePlayer2)

  def apply(fieldMap: Array[Array[Field]]) =
    new HexxagonController(fieldMap, "Player1", "Player2")

  def getInitialFields = getFieldsFromFile("/map_standard")

  def getFieldsFromFile(path: String) = {
    val lines = Source.fromURL(getClass.getResource(path)).getLines()
    val rowBuffer = new ArrayBuffer[Array[Field]]
    lines.foreach(line => {
      val colBuffer = new ArrayBuffer[Field]
      line.toArray.foreach {
        case 'X' => colBuffer += new Field(Filling.disabled)
        case 'O' => colBuffer += new Field(Filling.empty)
        case '1' => colBuffer += new Field(Filling.p1)
        case '2' => colBuffer += new Field(Filling.p2)
      }
      rowBuffer += colBuffer.toArray
    })
    rowBuffer.toArray
  }

}

class HexxagonController(initialFieldMap: Array[Array[Field]], namePlayer1: String, namePlayer2: String)
  extends Publisher {

  var board = new Board(initialFieldMap map (_.clone()))
  var p1 = new Player(0, namePlayer1, 0)
  var p2 = new Player(1, namePlayer2, 0)
  var currPlayerIndex = 0
  var selectedIndices = (0, 0)

  setScore()

  def init(newNamePlayer1: String, newNamePlayer2: String) = {
    board = new Board(initialFieldMap map (_.clone()))
    p1 = new Player(0, newNamePlayer1, 0)
    p2 = new Player(1, newNamePlayer2, 0)
    currPlayerIndex = 0
    selectedIndices = (0, 0)
    setScore()
    publish(new BoardChanged)
  }

  def input(x: Int, y: Int) = {
    val currPlayerFilling = getCurrPlayerFilling
    if (isValidIndices(x, y)) {
      board.getField(x, y).filling match {
        case `currPlayerFilling` =>
          selectedIndices = (x, y)
          setPossibilities(x, y)
        case Filling.duplicate | Filling.jump => move(selectedIndices._1, selectedIndices._2, x, y)
        case _ => println("Invalid input.")
      }
    }
  }

  def setPossibilities(x: Int, y: Int) = {
    cleanUp()

    val duplFuture = Future(getDuplicateIndices(x, y))
    val jumpFuture = Future(getJumpIndices(x, y))
    for {
      dupl <- duplFuture
      jump <- jumpFuture
    } {
      setFields(dupl, Filling.duplicate)
      setFields(jump, Filling.jump)
    }

    publish(new BoardChanged)
  }

  def move(fromX: Int, fromY: Int, toX: Int, toY: Int) = {
    if (board.getField(toX, toY).filling == Filling.jump) setField((fromX, fromY), Filling.empty)
    setField((toX, toY), getCurrPlayerFilling)
    val enemyNeighbours = getNeighbourIndices(toX, toY) filter (f =>
      board.getField(f._1, f._2).filling == getEnemyFilling)
    enemyNeighbours foreach (f => setField((f._1, f._2), getCurrPlayerFilling))
    setScore()
    cleanUp()
    currPlayerIndex = if (currPlayerIndex == 0) 1 else 0
    publish(new BoardChanged)
    if (gameOver) publish(new GameOver)
  }

  def gameOver = {
    def enemyKilled = p1.score == 0 || p2.score == 0
    def enemySurrounded = {
      val filtered = getCoordsByFilling(getCurrPlayerFilling) filter { i =>
        val movePossibilities = getDuplicateIndices(i._1, i._2) ::: getJumpIndices(i._1, i._2)
        movePossibilities.nonEmpty
      }
      filtered.isEmpty
    }
    def noEmptyFieldsLeft = !board.fields.flatten.exists(_.filling == Filling.empty)
    enemyKilled || noEmptyFieldsLeft || enemySurrounded
  }

  def cleanUp() = {
    val targetIndices = getCoordsByFilling(Filling.jump) ::: getCoordsByFilling(Filling.duplicate)
    setFields(targetIndices, Filling.empty)
  }

  def setScore() = {
    def count(f: Filling) = board.fields.flatten.count(_.filling == f)
    p1 = new Player(p1.index, p1.name, count(Filling.p1))
    p2 = new Player(p2.index, p2.name, count(Filling.p2))
  }

  def setField(indices: (Int, Int), f: Filling) = {
    board.fields(indices._2)(indices._1) = new Field(f)
  }

  def setFields(l: List[(Int, Int)], f: Filling) = {
    l foreach (i => setField((i._1, i._2), f))
  }

  def getFieldsAsList = {
    val size = board.fields.length - 1
    val list = for {
      i <- 0 to size
      j <- 0 to size
    } yield (i, j, board.getField(i, j).filling.toString)
    list.toList
  }

  def getJumpIndices(x: Int, y: Int) = {
    val staticIndices = (x - 2, y - 1) ::(x - 2, y) ::(x - 2, y + 1) ::(x, y - 2) ::(x, y + 2) ::(x + 2, y - 1) ::(x + 2, y) ::(x + 2, y + 1) :: Nil
    val varIndices = if (x % 2 == 0) {
      (x - 1, y - 2) ::(x - 1, y + 1) ::(x + 1, y - 2) ::(x + 1, y + 1) :: Nil
    } else {
      (x - 1, y - 1) ::(x - 1, y + 2) ::(x + 1, y - 1) ::(x + 1, y + 2) :: Nil
    }
    val allIndices = staticIndices ::: varIndices
    allIndices filter (f => isValidIndices(f._1, f._2) && (board.getField(f._1, f._2).filling == Filling.empty))
  }

  def getDuplicateIndices(x: Int, y: Int) = getNeighbourIndices(x, y) filter (f =>
    board.getField(f._1, f._2).filling == Filling.empty)

  def getNeighbourIndices(x: Int, y: Int) = {
    val staticIndices = (x - 1, y) ::(x, y - 1) ::(x, y + 1) ::(x + 1, y) :: Nil
    val varIndices = if (x % 2 == 0) {
      (x - 1, y - 1) ::(x + 1, y - 1) :: Nil
    } else {
      (x - 1, y + 1) ::(x + 1, y + 1) :: Nil
    }
    val allIndices = staticIndices ::: varIndices
    allIndices filter (f => isValidIndices(f._1, f._2))
  }

  def getEnemyFilling = if (currPlayerIndex == 0) Filling.p2 else Filling.p1

  def getCurrPlayerFilling = if (currPlayerIndex == 0) Filling.p1 else Filling.p2

  def isValidIndices(x: Int, y: Int) = {
    def isValidIndex(i: Int) = i >= 0 && i < board.fields.length
    isValidIndex(x) && isValidIndex(y)
  }

  def getCoordsByFilling(filling: Filling) = {
    val indices = for (row <- board.fields.indices; col <- board.fields.indices) yield (col, row)
    val res = indices filter (i => board.getField(i._1, i._2).filling == filling)
    res.toList
  }

}