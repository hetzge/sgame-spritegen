package de.hetzge.sgame.spritegen

import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import javafx.event.ActionEvent
import javafx.util.Callback
import java.util.function.Consumer

object FxHelper {

  implicit def mouseEvent2EventHandler(event: (MouseEvent) => Unit) = new EventHandler[MouseEvent] {
    override def handle(mouseEvent: MouseEvent): Unit = event(mouseEvent)
  }

  implicit def actionEvent2EventHandler(event: (ActionEvent) => Unit) = new EventHandler[ActionEvent] {
    override def handle(actionEvent: ActionEvent): Unit = event(actionEvent)
  }

  implicit def callback2callback[IN, OUT](callback: (IN) => OUT) = new Callback[IN, OUT]() {
    override def call(in: IN): OUT = callback(in)
  }

  implicit def consumer2consumer[IN](consumer: (IN) => Unit) = new Consumer[IN] {
    override def accept(in: IN) = consumer(in)
  }
  
  def result(value:Any): Unit = {}

}