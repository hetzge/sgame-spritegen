package de.hetzge.sgame.spritegen.component

import de.hetzge.sgame.spritegen.FxHelper._
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.layout.VBox
import javafx.scene.Node
import scala.collection.JavaConversions._
import javafx.scene.layout.AnchorPane
import de.hetzge.sgame.spritegen.FxHelper
import javafx.scene.control.ScrollPane
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Border

abstract class Repeater[T](val items: ObservableList[T]) extends ScrollPane {
  setContent(List)
  setFitToHeight(true)
  setFitToWidth(true)

  object List extends VBox {
    refresh()

    def refresh() = {
      val nodes = items.map((item: T) => new AnchorPane() {
        val node = cellFactory(item)
        getChildren().add(node)
        FxHelper.setAnchor(node)
      })
      getChildren().clear()
      getChildren().addAll(nodes)
    }

    items.addListener(RepeaterListChangeListener)
    object RepeaterListChangeListener extends ListChangeListener[T] {
      override def onChanged(change: ListChangeListener.Change[_ <: T]) = {
        println("change")
        refresh()
      }
    }
  }

  def cellFactory(t: T): Node

}