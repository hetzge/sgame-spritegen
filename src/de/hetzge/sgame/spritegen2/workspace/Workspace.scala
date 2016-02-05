package de.hetzge.sgame.spritegen2.workspace

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.collections.FXCollections
import javafx.scene.layout.StackPane
import javafx.scene.layout.HBox
import javafx.scene.control.ToolBar
import javafx.scene.layout.Pane
import javafx.scene.control.Button
import de.hetzge.sgame.spritegen.FxHelper._
import javafx.event.ActionEvent
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.SnapshotParameters
import javafx.application.Platform
import scala.concurrent.Promise
import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import java.io.FileInputStream
import java.io.File
import java.util.ArrayList
import scala.util.Success
import scala.util.Failure
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.impl.Future
import scala.concurrent.Future
import javafx.scene.image.ImageView
import javafx.scene.image.PixelFormat
import java.nio.IntBuffer
import java.nio.Buffer
import javafx.scene.control.ScrollPane
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Rectangle
import javafx.beans.binding.Binding
import javafx.beans.binding.Bindings

object Main extends App {
  Application.launch(classOf[GuiApp], args: _*)
}

class GuiApp extends Application {
  override def start(primaryStage: Stage) {

    primaryStage.setTitle("Sup!")

    val root = new StackPane
    root.getChildren.add(new Workspace().Gui)

    primaryStage.setScene(new Scene(root, 800, 600))
    primaryStage.show()

  }
}

trait SelfProperty {
  val property = new SimpleObjectProperty(this)
}

class Coordinate(private val _x: Int, private val _y: Int) extends SelfProperty {
  val x = new SimpleIntegerProperty(_x)
  val y = new SimpleIntegerProperty(_y)
}
class Rect(private val _start: Coordinate, private val _end: Coordinate) extends SelfProperty {
  val start = _start.property
  val end = _end.property
}

class Workspace {

  val image1 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/b1.png")))
  val image2 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/e2.png")))

  val wImage1 = new WritableImage(Constant.WORKAREA_WIDTH, Constant.WORKAREA_HEIGHT)
  val wImage2 = new WritableImage(Constant.WORKAREA_WIDTH, Constant.WORKAREA_HEIGHT)
  ImageService.imageToImage(image1, wImage1)
  ImageService.imageToImage(image2, wImage2)
  Service.setParts(Vector(wImage1, wImage2), 0)

  object Constant {
    val WORKAREA_WIDTH = 128
    val WORKAREA_HEIGHT = 128
  }

  object Model {
    object Property {
      val primaryColor = new SimpleObjectProperty(Color.RED)
      val pencilShape = new SimpleObjectProperty()
      val selection = new Rect(new Coordinate(0, 0), new Coordinate(0, 0))
      val selectedPart = new SimpleIntegerProperty(0)
      val parts: ObservableList[WritableImage] = FXCollections.observableList(new ArrayList())
      val tool = new SimpleObjectProperty[Tool](Tool.Pencil)
      val scaleFactor = new SimpleIntegerProperty(1)
    }
  }

  object Service {
    def setParts(parts: Seq[WritableImage], selectedPart: Int = 0) = {
      Model.Property.parts.clear()
      Model.Property.parts.addAll(parts)
      Gui.Body.Parts.getChildren().clear()
      Gui.Body.Parts.getChildren().addAll(parts.zipWithIndex.map {
        case (image, index) => new Gui.Body.Part(image, index)
      })
      Model.Property.selectedPart.setValue(selectedPart)
    }
  }

  object Gui extends AnchorPane {
    getChildren().add(Layout)

    object Layout extends VBox {
      getChildren().add(Header)
      getChildren().add(Body)
      getChildren().add(Footer)
      setAnchor(Layout.this)
    }

    object Header extends ToolBar {
      val toolButtons = Tool.activeTools.map(new ToolButton(_))
      getItems().addAll(toolButtons)

      class ToolButton(val tool: Tool) extends Button(tool.name) {
        setOnAction((actionEvent: ActionEvent) => Model.Property.tool.setValue(tool))
        Model.Property.tool.addListener((other: Tool) => setHover(other.equals(other)))
      }
    }
    object Body extends AnchorPane {
      getChildren().add(BodyScrollPane)
      object BodyScrollPane extends ScrollPane {
        setContent(Parts)
      }

      object Parts extends Pane

      class Part(val image: WritableImage, val index: Int) extends Pane {
        val width = image.getWidth()
        val height = image.getHeight()
        val pixelReader = image.getPixelReader()

        for (y <- 0 until height.toInt) {
          for (x <- 0 until width.toInt) {
            getChildren().add(new PixelRectangle(x, y))
          }
        }

        Model.Property.selectedPart.addListener { selectedIndex: Number =>
          setOpacity(if (selectedIndex == index) 0.8d else 0.4d)
        }

        class PixelRectangle(val x: Int, val y: Int) extends Rectangle {
          read()
          widthProperty().bindBidirectional(Model.Property.scaleFactor)
          heightProperty().bindBidirectional(Model.Property.scaleFactor)
          xProperty().bind(Bindings.createDoubleBinding(() => {
            new java.lang.Double(Model.Property.scaleFactor.getValue() * x)
          }, Model.Property.scaleFactor))
          yProperty().bind(Bindings.createDoubleBinding(() => {
            new java.lang.Double(Model.Property.scaleFactor.getValue() * y)
          }, Model.Property.scaleFactor))

          def read() = {
            val color = image.getPixelReader().getColor(x, y)
            setFill(color)
          }

          def write() = {
            val color: Color = getFill().asInstanceOf[Color]
            image.getPixelWriter().setColor(x, y, color)
          }
        }
      }
    }
    object Footer extends Pane
  }

  trait Tool {
    val name: String;

    def mouseUp(coordinate: Coordinate) = {}
    def mouseDown(coordinate: Coordinate) = {}
    def mouseMove(coordinate: Coordinate) = {}
  }

  object Tool {
    val activeTools = Vector(Pencil, Rubber, Pipet, Select, Move)

    object Pencil extends Tool {
      val name = "Pencil"
      override def mouseMove(coordinate: Coordinate) = {}
    }
    object Rubber extends Tool {
      val name = "Rubber"
    }
    object Pipet extends Tool {
      val name = "Pipet"
    }
    object Select extends Tool {
      val name = "Select"
    }
    object Move extends Tool {
      val name = "Move"
    }
  }

}