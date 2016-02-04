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

  object Model {
    object Property {
      val primaryColor = new SimpleObjectProperty(Color.RED)
      val pencilShape = new SimpleObjectProperty()
      val selection = new Rectangle(new Coordinate(0, 0), new Coordinate(0, 0))
      val selectedPart = new SimpleIntegerProperty(0)
      val parts: ObservableList[GraphicsContext] = FXCollections.emptyObservableList()
      val tool = new SimpleObjectProperty[Tool](Tool.Pencil)
    }
  }

  object Service {
    def setParts(graphicContexts: Seq[GraphicsContext], selectedPart: Int) = {
      Model.Property.parts.clear()
      Model.Property.parts.addAll(graphicContexts)
      Model.Property.selectedPart.setValue(selectedPart)
      Gui.Body.Parts.set(graphicContexts)
    }
    
    def imageToCanvas(image: Image, canvas: Canvas) = {
      clearCanvas(canvas)
      val (x, y) = (getCenteredX(image, canvas), getCenteredY(image, canvas))
      canvas.getGraphicsContext2D().drawImage(image, x, y)
    }
    
    def canvasToImage(canvas: Canvas, image: Image) = {
      val (x, y) = (getCenteredX(canvas, image), getCenteredY(canvas, image))
      canvas.getGraphicsContext2D().drawImage(image, x, y)
    }
    
    def getCenteredX(image: Image, canvas: Canvas) = canvas.getWidth() / 2 - image.getWidth() / 2
    def getCenteredX(canvas: Canvas, image: Image) = image.getWidth() / 2 - canvas.getWidth() / 2 
    def getCenteredY(image: Image, canvas: Canvas) = canvas.getHeight() / 2 - image.getHeight() / 2
    def getCenteredY(canvas: Canvas, image: Image) = image.getHeight() / 2 - canvas.getHeight() / 2
    
    def clearCanvas(canvas: Canvas) = {
      canvas.getGraphicsContext2D().clearRect(0d, 0d, canvas.getWidth(), canvas.getHeight())
    }
  }

  object Gui {
    object Layout extends HBox {
      getChildren().add(Header)
      getChildren().add(Body)
      getChildren().add(Footer)
    }

    object Header extends ToolBar {
      val toolButtons = Tool.activeTools.map(new ToolButton(_))
      getChildren().addAll(toolButtons)

      class ToolButton(val tool: Tool) extends Button(tool.name) {
        setOnAction((actionEvent: ActionEvent) => Model.Property.tool.setValue(tool))
        Model.Property.tool.addListener((other: Tool) => setHover(other.equals(other)))
      }
    }
    object Body extends Pane {
      getChildren().add(Parts)
      
      object Parts extends Pane {
        def set(graphicContexts: Seq[GraphicsContext]) = {
          getChildren().clear()
          getChildren().addAll(graphicContexts.map(new Part(_)))
        }

        class Part(val graphicsContext: GraphicsContext) extends Canvas {
          
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