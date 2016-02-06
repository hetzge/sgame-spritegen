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
import de.hetzge.sgame.spritegen.FxHelper
import javafx.scene.control.TextField
import javafx.scene.control.Slider
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.Border
import javafx.scene.Group
import javafx.scene.shape.StrokeType
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.Line
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.input.MouseDragEvent
import de.hetzge.sgame.spritegen2.workspace.Coordinate

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

  val wImage1 = new WritableImage(Constant.WORKAREA_SIZE, Constant.WORKAREA_SIZE)
  val wImage2 = new WritableImage(Constant.WORKAREA_SIZE, Constant.WORKAREA_SIZE)
  ImageService.imageToImage(image1, wImage1)
  ImageService.imageToImage(image2, wImage2)
  Service.setParts(Vector(wImage1, wImage2), 0)

  object Constant {
    val WORKAREA_SIZE = 128
    def scaledWorkareaSize = WORKAREA_SIZE * Model.Property.scaleFactor.getValue()
  }

  object Model {
    object Property {
      val primaryColor = new SimpleObjectProperty(Color.RED)
      val pencilShape = new SimpleObjectProperty()
      val selection = new Rect(new Coordinate(0, 0), new Coordinate(0, 0))
      val selectedPart = new SimpleIntegerProperty(0)
      val parts = FXCollections.observableList(new ArrayList[WritableImage]())
      val hiddenParts = FXCollections.observableList(new ArrayList[Int]())
      val tool = new SimpleObjectProperty[Tool](Tool.Pencil)
      val scaleFactor = new SimpleIntegerProperty(5)
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

    def pixel(value: Double): Int = (value / Model.Property.scaleFactor.getValue()).toInt
    def pixel(x: Double, y: Double): (Int, Int) = (pixel(x), pixel(y))
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
      getItems().add(ScaleSlider)

      class ToolButton(val tool: Tool) extends Button(tool.name) {
        setOnAction((actionEvent: ActionEvent) => Model.Property.tool.setValue(tool))
        Model.Property.tool.addListener((other: Tool) => setHover(other.equals(other)))
      }

      object ScaleSlider extends Slider {
        setMin(1)
        setMax(50)
        valueProperty().bindBidirectional(Model.Property.scaleFactor)
        setShowTickMarks(true)
      }
    }
    object Body extends AnchorPane {
      getChildren().add(BodyScrollPane)
      object BodyScrollPane extends ScrollPane {
        FxHelper.setAnchor(BodyScrollPane.this)
        setContent(Workarea)
      }

      object Workarea extends Pane {
        getChildren().add(Parts)
        getChildren().add(Grid)

        var last: (Int, Int) = null

        setOnMouseDragged((mouseEvent: MouseEvent) => {
          val pixel = Service.pixel(mouseEvent.getX(), mouseEvent.getY())
          if (last == null || pixel != last) {
            Model.Property.tool.getValue().mouseMove(pixel)
            last = pixel
          }
        })

        setOnMouseReleased((mouseEvent: MouseEvent) => {
          val pixel = Service.pixel(mouseEvent.getX(), mouseEvent.getY())
          Model.Property.tool.getValue().mouseUp(pixel)
          last = null
        })

        setOnMousePressed((mouseEvent: MouseEvent) => {
          val pixel = Service.pixel(mouseEvent.getX(), mouseEvent.getY())
          Model.Property.tool.getValue().mouseDown(pixel)
          last = pixel
        })
      }

      trait ReinitInScale {
        def init()

        Model.Property.scaleFactor.addListener((scaleFactor: Number) => {
          init()
        })
      }

      object Grid extends Group {
        (8 until Constant.WORKAREA_SIZE by 8).foreach((size: Int) => {
          getChildren().add(new OrientationRectange(size))
        })

        (8 until Constant.WORKAREA_SIZE by 8).foreach((x: Int) => {
          getChildren().add(new VerticalGridLine(x))
          getChildren().add(new HorizontalGridLine(x))
        })

        class GridLine(index: Int, vertical: Boolean) extends Line with ReinitInScale {
          def calculatePosition = index * Model.Property.scaleFactor.getValue()
          def init() = {
            setStartX(if (vertical) calculatePosition else 0)
            setEndX(if (vertical) calculatePosition else Constant.scaledWorkareaSize)
            setStartY(if (vertical) 0 else calculatePosition)
            setEndY(if (vertical) Constant.scaledWorkareaSize else calculatePosition)
          }
          setStroke(Color.rgb(0, 0, 0, 0.75d))
          getStrokeDashArray().addAll(5d, 10d)
          setStrokeWidth(0.5d)
          init()
        }

        class VerticalGridLine(x: Int) extends GridLine(x, true)
        class HorizontalGridLine(y: Int) extends GridLine(y, false)
      }

      class OrientationRectange(size: Int) extends Rectangle with ReinitInScale {
        def getScaleFactor = Model.Property.scaleFactor.getValue()
        def calculateSize = size * getScaleFactor
        def calculateX = Constant.scaledWorkareaSize / 2 - calculateSize / 2
        def calculateY = Constant.scaledWorkareaSize / 2 - calculateSize / 2
        def init() = {
          setX(calculateX)
          setY(calculateY)
          setWidth(calculateSize)
          setHeight(calculateSize)
        }

        setFill(Color.TRANSPARENT)
        setStroke(Color.rgb(0, 0, 0, 0.5d))
        getStrokeDashArray().addAll(1d, 10d, 2d, 9d)
        setStrokeWidth(0.5d)
        init()
      }

      object Parts extends Pane

      trait ScaleablePixel extends Rectangle {
        val x: Int
        val y: Int

        widthProperty().bindBidirectional(Model.Property.scaleFactor)
        heightProperty().bindBidirectional(Model.Property.scaleFactor)
        xProperty().bind(Bindings.createDoubleBinding(() => {
          new java.lang.Double(Model.Property.scaleFactor.getValue() * x)
        }, Model.Property.scaleFactor))
        yProperty().bind(Bindings.createDoubleBinding(() => {
          new java.lang.Double(Model.Property.scaleFactor.getValue() * y)
        }, Model.Property.scaleFactor))
      }

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

        class PixelRectangle(val x: Int, val y: Int) extends ScaleablePixel {
          read()

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

    def mouseUp(coordinate: (Int, Int)) = {}
    def mouseDown(coordinate: (Int, Int)) = {}
    def mouseMove(coordinate: (Int, Int)) = {}
  }

  object Tool {
    val activeTools = Vector(Pencil, Rubber, Pipet, Select, Move)

    object Pencil extends Tool {
      val name = "Pencil"
      override def mouseMove(coordinate: (Int, Int)) = {
        println("mouse move")
      }

      override def mouseUp(coordinate: (Int, Int)) = {
        println("mouse up")
      }

      override def mouseDown(coordinate: (Int, Int)) = {
        println("mouse down")
      }
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