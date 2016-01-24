package de.hetzge.sgame.spritegen

import java.util.ArrayList
import scala.collection.mutable.ArrayBuffer
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.control.ToolBar
import javafx.scene.control.ChoiceBox
import javafx.collections.ObservableList
import javafx.collections.FXCollections
import javafx.scene.layout.VBox
import javafx.scene.control.Button
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.control.ComboBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Background
import javafx.scene.input.MouseEvent
import javafx.scene.layout.TilePane
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.Cursor
import javafx.util.Callback
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.HBox
import javafx.scene.input.MouseButton
import javafx.scene.input.TransferMode
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.File
import java.io.FileInputStream
import javafx.scene.control.TextField
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.geometry.Pos
import javafx.geometry.Insets
import javafx.scene.control.ColorPicker
import javafx.scene.control.Dialog
import javafx.scene.control.ButtonType
import FxHelper._
import javafx.scene.control.Accordion
import javafx.scene.control.TitledPane
import de.hetzge.sgame.spritegen.component.EditColorUsageDialog
import de.hetzge.sgame.spritegen.component.ColorUsageCellFactory

case class ColorUsage(val name: String, color: Color)
object ColorUsage {
  val NONE = ColorUsage("none", Color.color(0d, 0d, 0d, 0.5d));
}

case class TemplateUsage(val name: String)
object TemplateUsage {
  val HEAD = TemplateUsage("head")
  val BODY = TemplateUsage("body")
  val FEED = TemplateUsage("feed")
  val ITEM = TemplateUsage("item")
  val HEAD_DECORATION = TemplateUsage("headDecoration")
  val BODY_DECORATION = TemplateUsage("bodyDecoration")
}

case class Orientation(val name: String)
object Orientation {
  val NORTH = Orientation("north")
  val SOUTH = Orientation("south")
  val SIDE = Orientation("west")
}

case class AnimationUsage(val name: String)
object AnimationUsage {
  val IDLE = AnimationUsage("idle")
  val WALK = AnimationUsage("walk")
}

case class ColorRelation(from: Color, to: Color) {
  val r: Double = from.getRed() - to.getRed()
  val g: Double = from.getGreen() - to.getGreen()
  val b: Double = from.getBlue() - to.getBlue()
}
case class Pixel(val color: ColorUsage, val relation: ColorRelation)
case class Layer(val pixels: IndexedSeq[Pixel], val width: Int, val height: Int)
case class Animation(val templates: Vector[Layer])

object Main extends App {
  val model = new Model
  val service = new Service

  //  project.colors.put(ColorUsage.COLOR_A, Color.GREEN)
  //  project.colors.put(ColorUsage.COLOR_B, Color.GREEN)
  //  project.colors.put(ColorUsage.COLOR_C, Color.GREEN)
  //  project.colors.put(ColorUsage.BORDER_COLOR, Color.BLACK)
  //  project.colors.put(ColorUsage.FACE_COLOR, Color.CYAN)

  Application.launch(classOf[GuiApp], args: _*)
}

class GuiApp extends Application {
  override def start(primaryStage: Stage) {

    primaryStage.setTitle("Sup!")

    val root = new StackPane
    root.getChildren.add(new gui.Gui)

    primaryStage.setScene(new Scene(root, 800, 600))
    primaryStage.show()

  }
}

class Model {
  val colorUsages = new ArrayList[ColorUsage]()
  val observableColorUsages = FXCollections.observableList(colorUsages)

  val layers = new ArrayList[Layer]()
  val observableLayers = FXCollections.observableList(layers)
}

class Service {

}

package gui {

  class Gui extends BorderPane {
    setTop(ToolMenu)
    setLeft(BrowserMenu)
    setCenter(new Editor(new File("/home/hetzge/private/werke/animation/test/b2.png")))
  }

  object ToolMenu extends ToolBar {

  }

  object BrowserMenu extends Accordion {
    getPanes().add(ColorUsagePane)

    object ColorUsagePane extends TitledPane("Color usage", ColorUsageBrowser)

    object ColorUsageBrowser extends VBox {
      getChildren().add(ColorUsageToolBar)
      getChildren().add(ColorUsageList)

      object ColorUsageList extends ListView[ColorUsage] {
        setItems(Main.model.observableColorUsages)
        getSelectionModel().selectFirst()
        setCellFactory(ColorUsageCellFactory)
      }

      object ColorUsageToolBar extends ToolBar {
        getItems().add(AddColorUsageButton)
        getItems().add(RemoveColorUsageButton)

        object AddColorUsageButton extends Button {
          setText("+")
          setOnAction((actionEvent: ActionEvent) => {
            val optional = new EditColorUsageDialog().showAndWait()
            optional.ifPresent((colorUsage: ColorUsage) => result(Main.model.observableColorUsages.add(colorUsage)))
          })
        }

        object RemoveColorUsageButton extends Button {
          setText("-")
          disableProperty().bind(ColorUsageList.getSelectionModel().selectedItemProperty().isNull())
          setOnAction((actionEvent: ActionEvent) => {

            object ConfirmAlert extends Alert(AlertType.CONFIRMATION) {
              setTitle("Are you sure ?")
              setHeaderText("Do you really want delete the current selected color usage ?")
            }

            val buttonTypeOptional = ConfirmAlert.showAndWait()
            if (buttonTypeOptional.get() == ButtonType.OK) {
              val selectedIndex = ColorUsageList.getSelectionModel().getSelectedIndex()
              result(Main.model.observableColorUsages.remove(selectedIndex))
            }
          })
        }
      }
    }

    object BrowseLayers extends ListView

  }

  class Editor(val imageFile: File) extends VBox {
    val tempImage = new Image(new FileInputStream(imageFile))
    val width = tempImage.getWidth().intValue()
    val height = tempImage.getHeight().intValue()
    val image = new Image(new FileInputStream(imageFile), width * Tile.TILE_SIZE, height * Tile.TILE_SIZE, false, false)

    getChildren().add(Menu)
    getChildren().add(Workspace)

    def export(): Layer = {
      val referenceColors = scala.collection.mutable.HashMap.empty[ColorUsage, Color]
      val pixels = TileMap.tiles.map { tile =>
        val color = tile.color
        val colorUsage = tile.colorUsage
        val referenceColor = referenceColors.getOrElseUpdate(tile.colorUsage, color)
        val colorRelation = ColorRelation(color, referenceColor)
        new Pixel(colorUsage, colorRelation)
      }
      new Layer(pixels, width, height)
    }

    object Menu extends ToolBar {
      getItems().add(SelectColorUsage)
      getItems().add(NameTextField)
      getItems().add(SaveButton)

      object SelectColorUsage extends ComboBox[ColorUsage] {
        setItems(Main.model.observableColorUsages)
        getSelectionModel().selectFirst()
        setCellFactory(ColorUsageCellFactory)
        setButtonCell(ColorUsageCellFactory.call(null))
      }

      object NameTextField extends TextField
      object SaveButton extends Button
    }

    object Workspace extends Pane {
      getChildren().add(Background)
      getChildren().add(TileMap)
    }

    object Background extends ImageView {
      setImage(image)
    }

    object TileMap extends GridPane {
      setOpacity(0.6d)

      val tiles = (0 until height).map { y =>
        (0 until width).map { x =>
          val tile = new Tile(x, y)
          GridPane.setConstraints(tile, x, y)
          getChildren().add(tile)
          tile
        }
      }.flatten

      class Tile(val x: Int, val y: Int) extends StackPane {
        var colorUsage: ColorUsage = ColorUsage.NONE
        var color: Color = image.getPixelReader().getColor(x, y)
        getChildren().add(BackgroundRectangle)

        def setColorUsage(colorUsage: ColorUsage) = {
          this.colorUsage = colorUsage
          setup()
        }

        def setup() = {
          BackgroundRectangle.setFill(colorUsage.color)
        }

        object BackgroundRectangle extends Rectangle {
          setX(x * Tile.TILE_SIZE)
          setY(y * Tile.TILE_SIZE)
          setWidth(Tile.TILE_SIZE)
          setHeight(Tile.TILE_SIZE)
          setCursor(Cursor.CROSSHAIR)

          val onDragDetected = (mouseEvent: MouseEvent) => startFullDrag()
          setOnDragDetected(onDragDetected)

          val onClick = (mouseEvent: MouseEvent) => setColorUsage(Editor.this.Menu.SelectColorUsage.getValue())
          setOnMouseDragOver(onClick)
          setOnMouseClicked(onClick)
        }
      }
    }
    object Tile {
      val TILE_SIZE = 16
    }

  }

}
