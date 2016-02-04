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
class Rectangle(private val _start: Coordinate, private val _end: Coordinate) extends SelfProperty {
  val start = _start.property
  val end = _end.property
}

class Workspace {

  // TODO Promises aus Service raus
  // TODO Image Konvertierung

  val image1 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/b1.png")))
  val image2 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/b2.png")))

  val wImage1 = new WritableImage(image1.getWidth().toInt, image1.getHeight().toInt)

  Service.imageToCanvas(image1, new Canvas(image1.getWidth().toInt, image1.getHeight().toInt)).onComplete {
    case Success(canvas) => Service.canvasToImage(canvas, new WritableImage(canvas.getWidth().toInt, canvas.getHeight().toInt)).onComplete {
      case Success(image) => Platform.runLater { () =>
        Service.setParts(Vector(image), 0)
      }
      case Failure(ex) => println("bad2")
    }
    case Failure(ex) => println("bad")
  }

  object Constant {
    val WORKAREA_WIDTH = 128
    val WORKAREA_HEIGHT = 128
  }

  object Model {
    object Property {
      val primaryColor = new SimpleObjectProperty(Color.RED)
      val pencilShape = new SimpleObjectProperty()
      val selection = new Rectangle(new Coordinate(0, 0), new Coordinate(0, 0))
      val selectedPart = new SimpleIntegerProperty(0)
      val parts: ObservableList[WritableImage] = FXCollections.observableList(new ArrayList())
      val tool = new SimpleObjectProperty[Tool](Tool.Pencil)
    }
  }

  object Service {
    def setParts(parts: Seq[WritableImage], selectedPart: Int = 0) = {
      Model.Property.parts.clear()
      Model.Property.parts.addAll(parts)
      Gui.Body.Parts.getChildren().clear()
      Gui.Body.Parts.getChildren().addAll(parts.zipWithIndex.map {
        case (image, index) =>
          val canvas = new Gui.Body.Part(index)
          imageToCanvas(image, canvas)
          canvas
      })
      Model.Property.selectedPart.setValue(selectedPart)
    }

    def imageToCanvas(image: Image, canvas: Canvas): Future[Canvas] = {
      val promise = Promise[Canvas]
      Platform.runLater { () =>
        clearCanvas(canvas)
        val (x, y) = (getCenteredX(image, canvas), getCenteredY(image, canvas))
        canvas.getGraphicsContext2D().drawImage(image, x, y)
        promise.success(canvas)
      }
      promise.future
    }

    def canvasToImage(canvas: Canvas, image: WritableImage): Future[WritableImage] = {
      val promise = Promise[WritableImage]
      Platform.runLater { () =>
        canvas.snapshot(new SnapshotParameters(), image)
        promise.success(image)
      }
      promise.future
    }

    private def getCenteredX(image: Image, canvas: Canvas) = canvas.getWidth() / 2 - image.getWidth() / 2
    private def getCenteredX(canvas: Canvas, image: Image) = image.getWidth() / 2 - canvas.getWidth() / 2
    private def getCenteredY(image: Image, canvas: Canvas) = canvas.getHeight() / 2 - image.getHeight() / 2
    private def getCenteredY(canvas: Canvas, image: Image) = image.getHeight() / 2 - canvas.getHeight() / 2

    private def clearCanvas(canvas: Canvas) = {
      canvas.getGraphicsContext2D().clearRect(0d, 0d, canvas.getWidth(), canvas.getHeight())
    }
  }

  object Gui extends AnchorPane {
    getChildren().add(Layout)

    object Layout extends VBox {
      getChildren().add(Header)
      getChildren().add(Body)
      getChildren().add(Footer)
      AnchorPane.setTopAnchor(Layout.this, 0.0d)
      AnchorPane.setLeftAnchor(Layout.this, 0.0d)
      AnchorPane.setRightAnchor(Layout.this, 0.0d)
      AnchorPane.setBottomAnchor(Layout.this, 0.0d)
    }

    object Header extends ToolBar {
      val toolButtons = Tool.activeTools.map(new ToolButton(_))
      getItems().addAll(toolButtons)

      class ToolButton(val tool: Tool) extends Button(tool.name) {
        setOnAction((actionEvent: ActionEvent) => Model.Property.tool.setValue(tool))
        Model.Property.tool.addListener((other: Tool) => setHover(other.equals(other)))
      }
    }
    object Body extends Pane {
      getChildren().add(Parts)

      object Parts extends Pane
      class Part(val index: Int) extends Canvas {
        setWidth(Constant.WORKAREA_WIDTH)
        setHeight(Constant.WORKAREA_HEIGHT)
        Model.Property.selectedPart.addListener { selectedIndex: Number =>
          setOpacity(if (selectedIndex == index) 0.8d else 0.4d)
        }
      }
    }
    object Footer extends Pane {

    }
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