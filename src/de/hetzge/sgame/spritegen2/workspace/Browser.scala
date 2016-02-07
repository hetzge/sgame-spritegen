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

case class Animation(val name: String, val parts: java.util.ArrayList[Part]) {
  val partsProperty = FXCollections.observableList(parts)
}
case class Part(val name: String, val images: MutableList[Image])
case class PartFilter(val query: String)

class Browser {

  val image1 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/b1.png")))
  val image2 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/e2.png")))

  Model.Property.partsPool.add(Part("Part A", MutableList(image1)))
  Model.Property.partsPool.add(Part("Part B", MutableList(image2)))

  val partsList = new java.util.ArrayList[Part]()
  partsList.add(Part("Part A", MutableList(image1)))
  partsList.add(Part("Part B", MutableList(image2)))

  Model.Property.animationPool.add(Animation("Animation 1", partsList))

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
                  object CellGraphic extends TitledPane(animation.name, CellGraphicBody)
                  object CellGraphicBody extends Pane {

                    getChildren().add(new Label("test"))
                  }
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
      getChildren().add(PartList)
      FxHelper.setAnchor(PartBrowser.this)

      object PartList extends ListView[Part] {
        setItems(Model.Property.filteredParts)
        setCellFactory(PartCellFactory)

        object PartCellFactory extends Callback[ListView[Part], ListCell[Part]] {
          override def call(p: ListView[Part]): ListCell[Part] = {
            object Cell extends ListCell[Part] {
              override def updateItem(part: Part, empty: Boolean) = {
                super.updateItem(part, empty)
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (part != null) {
                  object CellGraphic extends HBox {
                    val firstImage = part.images.get(0)
                    getChildren().add(firstImage match {
                      case Some(image) => new ImageView(image)
                      case None => new Pane()
                    })
                    getChildren().add(CellLabel)

                    object CellLabel extends Label(part.name) {
                      setSpacing(10.0d)
                      setTranslateY(5.0d)
                    }
                  }
                  setGraphic(CellGraphic)
                }
              }
            }
            Cell
          }
        }
      }
    }
  }
}