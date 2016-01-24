package de.hetzge.sgame.spritegen.component

import javafx.scene.control.ListView
import javafx.util.Callback
import javafx.scene.control.ListCell
import de.hetzge.sgame.spritegen.ColorUsage
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.control.Label

object ColorUsageCellFactory extends Callback[ListView[ColorUsage], ListCell[ColorUsage]] {
  override def call(p: ListView[ColorUsage]): ListCell[ColorUsage] = {
    new ListCell[ColorUsage]() {
      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      override def updateItem(colorUsage: ColorUsage, empty: Boolean) = {
        super.updateItem(colorUsage, empty)
        val color = if (empty) Color.WHITE else colorUsage.color
        val text = if (empty) "" else colorUsage.name
        val pane = new HBox
        pane.setSpacing(10d)
        pane.getChildren().add(new Rectangle(15, 15, color))
        pane.getChildren().add(new Label(text))
        setGraphic(pane)
      }
    }
  }
}