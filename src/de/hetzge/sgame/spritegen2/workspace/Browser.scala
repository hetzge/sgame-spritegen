package de.hetzge.sgame.spritegen2.workspace

import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.layout.StackPane
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.scene.image.Image
import scala.collection.mutable.MutableList
import scala.collection.mutable.Queue
import javafx.scene.layout.Pane
import collection.JavaConversions._
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import de.hetzge.sgame.spritegen.FxHelper._
import javafx.scene.control.ListCell
import javafx.util.Callback
import javafx.scene.layout.HBox
import javafx.scene.image.ImageView
import javafx.scene.control.Label
import java.io.FileInputStream
import java.io.File
import de.hetzge.sgame.spritegen.FxHelper
import sun.launcher.LauncherHelper.FXHelper
import sun.launcher.LauncherHelper.FXHelper
import javafx.scene.control.ContentDisplay
import javafx.geometry.Pos
import javafx.scene.control.Accordion
import javafx.scene.control.TitledPane
import javafx.scene.input.MouseEvent
import javafx.scene.control.ToolBar
import javafx.scene.input.TransferMode
import javafx.scene.input.DragEvent
import javafx.scene.Cursor
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.Node
import javafx.collections.ObservableList

object BrowserMain extends App {
  Application.launch(classOf[BrowserGuiApp], args: _*)
}

class BrowserGuiApp extends Application {
  override def start(primaryStage: Stage) {
    primaryStage.setTitle("Browser")

    val root = new StackPane
    root.getChildren.add(new Browser().Gui)

    primaryStage.setScene(new Scene(root, 800, 600))
    primaryStage.show()
  }
}

object DragAction extends Enumeration {
  val PLACE_BEFORE, PLACE_AFTER, REPLACE = Value
}

case class Animation(val name: String, val parts: java.util.ArrayList[Part]) {
  val partsProperty = FXCollections.observableList(parts)
}
case class Part(val name: String, val images: MutableList[Image])
case class PartFilter(val query: String)

class Browser {

  val image1 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/b1.png")))
  val image2 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/e2.png")))

  Model.Property.partsPool.add(Part("Part A", MutableList(image1, image2)))
  Model.Property.partsPool.add(Part("Part B", MutableList(image2)))

  val partsList = new java.util.ArrayList[Part]()
  partsList.add(Part("Part A", MutableList(image1, image2)))
  partsList.add(Part("Part B", MutableList(image2)))

  Model.Property.animationPool.add(Animation("Animation 1", partsList))
  Model.Property.animationPool.add(Animation("Animation 2", partsList))
  Model.Property.animationPool.add(Animation("Animation 3", partsList))
  Model.Property.animationPool.add(Animation("Animation 4", partsList))

  object Model {
    val partsPool = new java.util.ArrayList[Part]()
    val filteredParts = new java.util.ArrayList[Part]()
    val animationPool = new java.util.ArrayList[Animation]()

    object Property {
      val partsPool = FXCollections.observableList(Model.this.partsPool)
      val filteredParts = FXCollections.observableList(Model.this.filteredParts)
      val animationPool = FXCollections.observableList(Model.this.animationPool)
    }
  }

  object Service {
    def filterParts(filter: PartFilter) = {
      val filteredParts = Model.partsPool.filter((part: Part) => {
        filter.query == null || part.name.contains(filter.query)
      })
      Model.Property.filteredParts.clear()
      Model.Property.filteredParts.addAll(filteredParts)
    }
  }

  object Gui extends AnchorPane {
    getChildren().add(Layout)

    object Layout extends VBox {
      getChildren().add(Header)
      getChildren().add(Body)
      getChildren().add(Footer)
      FxHelper.setAnchor(Layout.this)
    }

    object Header extends AnchorPane
    object Body extends AnchorPane {
      getChildren().add(AnimationBrowser)
    }
    object Footer extends AnchorPane {
      getChildren().add(PartBrowser)
    }

    object AnimationBrowser extends VBox {
      FxHelper.setAnchor(AnimationBrowser.this)
      getChildren().add(AnimationList)

      object AnimationList extends ListView[Animation] {
        setItems(Model.Property.animationPool)
        setCellFactory(AnimationCellFactory)

        object AnimationCellFactory extends Callback[ListView[Animation], ListCell[Animation]] {
          override def call(p: ListView[Animation]): ListCell[Animation] = {
            object Cell extends ListCell[Animation] {
              override def updateItem(animation: Animation, empty: Boolean) = {
                super.updateItem(animation, empty)
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY)
                if (animation != null) {
                  object CellGraphic extends TitledPane(animation.name, AnimationPartList)
                  object AnimationPartList extends PartList(animation.partsProperty)
                  setGraphic(CellGraphic)
                }
              }
            }
            Cell
          }
        }
      }

      class AnimationBuilder(val animation: Animation) extends Pane {

      }
    }

    object PartBrowser extends VBox {
      Service.filterParts(PartFilter(null))
      getChildren().add(new PartList(Model.Property.filteredParts))
      FxHelper.setAnchor(PartBrowser.this)
    }

    class PartList(val parts: ObservableList[Part]) extends ListView[Part] with PartCellHolder {
      setItems(parts)
      setCellFactory(PartCellFactory)

      object PartCellFactory extends Callback[ListView[Part], ListCell[Part]] {
        override def call(p: ListView[Part]): ListCell[Part] = {
          object Cell extends ListCell[Part] {
            override def updateItem(part: Part, empty: Boolean) = {
              super.updateItem(part, empty)
              setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
              if (part != null) {
                setGraphic(new PartCell(part, true))
              }
            }
          }
          Cell
        }
      }

      class PartCell(val part: Part, val dragGoal: Boolean = true) extends VBox with PartHolder {
        getChildren().add(BeforeDrop)
        getChildren().add(Content)
        getChildren().add(AfterDrop)

        trait DragGoal extends Node {
          val action: DragAction.Value

          DragGoal.this.setOnDragOver((dragEvent: DragEvent) => {
            println("drag over")

            dragEvent.acceptTransferModes(TransferMode.ANY: _*)
            dragEvent.consume()
          })

          DragGoal.this.setOnDragDropped((dragEvent: DragEvent) => {

            dragEvent.getGestureSource() match {
              case partHolder: PartHolder => add(part, action, partHolder.part)
              case _ => throw new IllegalStateException()
            }

            dragEvent.setDropCompleted(true)
            dragEvent.consume()
          })

        }

        object BeforeDrop extends ToolBar with DragGoal {
          val action = DragAction.PLACE_BEFORE
          setVisible(dragGoal)
        }
        object AfterDrop extends ToolBar with DragGoal {
          val action = DragAction.PLACE_AFTER
          setVisible(dragGoal)
        }

        object Content extends HBox with PartHolder with DragGoal {
          val action = DragAction.REPLACE
          val part = PartCell.this.part
          val imageCells = part.images.map(new ImageCell(_))
          getChildren().add(CellLabel)
          getChildren().addAll(imageCells)

          object CellLabel extends Label {
            setText(part.name)
            setSpacing(10.0d)
            setTranslateY(5.0d)
          }

          Content.this.setOnDragDetected((mouseEvent: MouseEvent) => {
            println("start drag")
            val dragBoard = Content.this.startDragAndDrop(TransferMode.ANY: _*)
            val content = new ClipboardContent()
            content.putString("")
            dragBoard.setContent(content);
            mouseEvent.consume()
          })
        }
      }
    }

    trait PartHolder {
      val part: Part
    }

    trait PartCellHolder {
      val parts: ObservableList[Part]

      def add(where: Part, action: DragAction.Value, part: Part): Unit = {
        val index = parts.indexOf(where)
        add(index, action, part)
      }

      def add(where: Int, action: DragAction.Value, part: Part): Unit = {
        val offset = action match {
          case DragAction.PLACE_AFTER => 1
          case DragAction.PLACE_BEFORE => 0
          case DragAction.REPLACE => 0
          case _ => throw new IllegalStateException()
        }

        val index = if (where + offset < 0) {
          0
        } else if (where + offset >= parts.size()) {
          parts.size() - 1
        } else {
          where + offset
        }

        println(where, index, part.name, action)

        if (action == DragAction.REPLACE) {
          parts.set(index, part)
        } else {
          parts.add(index, part)
        }
      }
    }

    class AnimationCell extends HBox {

    }

    class ImageCell(val image: Image) extends ImageView(image)

  }
}