package de.hetzge.sgame.spritegen.component

import javafx.scene.Node
import javafx.scene.input.DragEvent
import de.hetzge.sgame.spritegen.FxHelper._
import javafx.scene.input.TransferMode
import javafx.beans.Observable
import javafx.collections.ObservableList
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.layout.HBox
import javafx.scene.control.ToolBar
import javafx.scene.input.MouseEvent
import javafx.scene.input.ClipboardContent
import javafx.collections.transformation.FilteredList

object DragAction extends Enumeration {
  val PLACE_BEFORE, PLACE_AFTER, REPLACE, ADD = Value
}

case class DragGroup(val name: String)

trait ItemHolder[T] {
  val item: T
}

trait DragSource[T] extends Node with ItemHolder[T] {
  val dragGroup: DragGroup

  setOnDragDetected((mouseEvent: MouseEvent) => {
    println("start drag")

    val dragBoard = DragSource.this.startDragAndDrop(TransferMode.ANY: _*)
    val content = new ClipboardContent()
    content.putString("")
    dragBoard.setContent(content);

    mouseEvent.consume()
  })
}

trait DragGoal[T] extends Node with ItemHolder[T] {
  val item: T
  val allowedDragGroups: Vector[DragGroup]
  val action: DragAction.Value

  def onDragDropped(source: DragSource[T], goal: DragGoal[T], action: DragAction.Value)

  DragGoal.this.setOnDragOver((dragEvent: DragEvent) => {
    println("drag over")

    dragEvent.getGestureSource() match {
      case dragSource: DragSource[T] => {
        if (allowedDragGroups.isEmpty || allowedDragGroups.contains(dragSource.dragGroup)) {
          dragEvent.acceptTransferModes(TransferMode.ANY: _*)
          dragEvent.consume()
        }
      }
      case _ => throw new IllegalStateException()
    }

  })

  DragGoal.this.setOnDragDropped((dragEvent: DragEvent) => {
    println("drag dropped")

    dragEvent.getGestureSource() match {
      case dragSource: DragSource[T] => onDragDropped(dragSource, DragGoal.this, DragGoal.this.action)
      case _ => throw new IllegalStateException()
    }

    dragEvent.setDropCompleted(true)
    dragEvent.consume()
  })

}

trait DragList[T] extends DragGoal[T] {
  val sourceItems: ObservableList[T]

  def onDragDropped(source: DragSource[T], goal: DragGoal[T], action: DragAction.Value) = add(goal.item, action, source.item)

  def add(where: T, action: DragAction.Value, part: T): Unit = {

    if (action == DragAction.ADD) {
      sourceItems.add(part)
      return
    }

    if (where.equals(part)) {
      return
    }
    if (sourceItems.contains(part)) {
      sourceItems.remove(part)
    }

    val whereIndex = sourceItems.indexOf(where)

    val offset = action match {
      case DragAction.PLACE_AFTER => 1
      case DragAction.PLACE_BEFORE => 0
      case DragAction.REPLACE => 0
      case _ => throw new IllegalStateException()
    }

    val index = whereIndex + offset;

    if (action == DragAction.REPLACE) {
      sourceItems.set(index, part)
      println("replace")
    } else {
      if (index >= sourceItems.size()) {
        sourceItems.add(part)
      } else if (index <= 0) {
        sourceItems.add(0, part)
      } else {
        sourceItems.add(index, part)
      }
    }

  }
}

abstract class DragRepeater[T >: Null](val sourceItems: ObservableList[T], vertical: Boolean = true) extends Repeater[T](sourceItems.filtered((item: T) => true), vertical) with DragList[T] with DragGoal[T] {
  val dragGroup: DragGroup
  val allowedDragGroups: Vector[DragGroup]

  def dragCellFactory(t: T): Node

  val item: T = null
  val action: DragAction.Value = DragAction.ADD

  trait DragDecorator extends DragSource[T] with DragGoal[T] {
    val dragGroup: DragGroup = DragRepeater.this.dragGroup
    val allowedDragGroups: Vector[DragGroup] = DragRepeater.this.allowedDragGroups
    val action: DragAction.Value = DragAction.REPLACE

    def onDragDropped(source: DragSource[T], goal: DragGoal[T], action: DragAction.Value) = DragRepeater.this.add(goal.item, action, source.item)
  }

  def cellFactory(itemParameter: T): Node = {
    val layout = if (vertical) {
      new VBox() with DragDecorator {
        val item: T = itemParameter
      }
    } else {
      new HBox() with DragDecorator {
        val item: T = itemParameter
      }
    }

    trait DragAround extends ToolBar with DragGoal[T] {
      val allowedDragGroups: Vector[DragGroup] = DragRepeater.this.allowedDragGroups

      def onDragDropped(source: DragSource[T], goal: DragGoal[T], action: DragAction.Value) = DragRepeater.this.add(goal.item, action, source.item)
    }

    object BeforeDrop extends DragAround {
      val item: T = itemParameter
      val action = DragAction.PLACE_BEFORE
    }
    object AfterDrop extends DragAround {
      val item: T = itemParameter
      val action = DragAction.PLACE_AFTER
    }

    layout.getChildren().add(BeforeDrop)
    layout.getChildren().add(dragCellFactory(itemParameter))
    layout.getChildren().add(AfterDrop)
    layout
  }

}

