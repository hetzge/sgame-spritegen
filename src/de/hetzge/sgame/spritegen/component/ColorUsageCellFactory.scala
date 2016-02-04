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
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import de.hetzge.sgame.spritegen.FxHelper._
import javafx.event.ActionEvent
import de.hetzge.sgame.spritegen.Main

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
        setContextMenu(_ContextMenu)

        object _ContextMenu extends ContextMenu {
          getItems().add(EditMenuItem)

          object EditMenuItem extends MenuItem("Edit") {
            setOnAction((actionEvent: ActionEvent) => {
              val optional = new EditColorUsageDialog(colorUsage).showAndWait()
              optional.ifPresent((newColorUsage: ColorUsage) => {
                Main.service.replaceColorUsage(newColorUsage, colorUsage)
              });
            });
          }
        }
      }
    }
  }
}