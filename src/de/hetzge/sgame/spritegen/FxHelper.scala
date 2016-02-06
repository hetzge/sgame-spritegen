package de.hetzge.sgame.spritegen

import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import javafx.event.ActionEvent
import javafx.util.Callback
import java.util.function.Consumer
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import scala.collection.convert.wrapAsJava
import javafx.scene.layout.AnchorPane
import javafx.scene.Node
import java.util.concurrent.Callable
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseDragEvent

object FxHelper {

  implicit def mouseEvent2EventHandler(event: (MouseEvent) => _) = new EventHandler[MouseEvent] {
    override def handle(mouseEvent: MouseEvent): Unit = event(mouseEvent)
  }

  implicit def actionEvent2EventHandler(event: (ActionEvent) => _) = new EventHandler[ActionEvent] {
    override def handle(actionEvent: ActionEvent): Unit = event(actionEvent)
  }

  implicit def dragEvent2EventHandler(event: (DragEvent) => _) = new EventHandler[DragEvent] {
    override def handle(dragEvent: DragEvent): Unit = event(dragEvent)
  }

  implicit def mouseDragEvent2MouseDragEventHandler(event: (MouseDragEvent) => _) = new EventHandler[MouseDragEvent] {
    override def handle(dragEvent: MouseDragEvent): Unit = event(dragEvent)
  }

  implicit def callback2callback[IN, OUT](callback: (IN) => OUT) = new Callback[IN, OUT]() {
    override def call(in: IN): OUT = callback(in)
  }

  implicit def consumer2consumer[IN](consumer: (IN) => _) = new Consumer[IN] {
    override def accept(in: IN) = consumer(in)
  }

  implicit def changeListener2changeListener[T, U <: T, S <: ObservableValue[_ <: T]](f: (T) => Unit): ChangeListener[_ >: T] = new ChangeListener[T] {
    override def changed(o: ObservableValue[_ <: T], oldValue: T, newValue: T) = f(o.getValue())
  }

  implicit def changeListener2ChangeListener[T](f: (T) => Any) = new ChangeListener[T] {
    override def changed(o: ObservableValue[_ <: T], oldValue: T, newValue: T) = f(o.getValue())
  }

  implicit def callable2callable[T](callable: () => T) = new Callable[T] {
    override def call(): T = { callable() }
  }

  implicit def seq2List[T](seq: Seq[T]): java.util.List[T] = wrapAsJava.seqAsJavaList(seq)

  implicit def runnable2Runnable(runnable: () => Any) = new Runnable() {
    override def run() { runnable() }
  }

  def setAnchor(node: Node) = {
    AnchorPane.setTopAnchor(node, 0.0d)
    AnchorPane.setLeftAnchor(node, 0.0d)
    AnchorPane.setRightAnchor(node, 0.0d)
    AnchorPane.setBottomAnchor(node, 0.0d)
  }

}