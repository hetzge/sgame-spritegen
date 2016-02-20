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
import javafx.scene.layout.HBox
import javafx.scene.layout.BorderPane

abstract class Repeater[T](val items: ObservableList[T], val vertical: Boolean = true) extends BorderPane  {

  def cellFactory(t: T): Node
  
  def init() = layoutPane.refresh()
  
  val layoutPane = if (vertical) {
    new VBox with Layout
  } else {
    new HBox with Layout
  }
  setCenter(layoutPane)

  trait Layout extends Pane {
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
}


