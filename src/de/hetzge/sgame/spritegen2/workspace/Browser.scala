package de.hetzge.sgame.spritegen2.workspace

import java.io.File
import java.io.FileInputStream
import java.util.ArrayList
import scala.Vector
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.JavaConversions.mutableSeqAsJavaList
import scala.collection.mutable.MutableList
import de.hetzge.sgame.spritegen.FxHelper
import de.hetzge.sgame.spritegen.FxHelper._
import de.hetzge.sgame.spritegen.component.Repeater
import de.jensd.fx.glyphs.GlyphsDude
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.ToolBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.collections.ListChangeListener
import de.hetzge.sgame.spritegen.component.DragRepeater
import de.hetzge.sgame.spritegen.component.DragGroup
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.awt.Panel
import de.hetzge.sgame.spritegen.component.DragGroup
import javafx.scene.image.WritableImage

object BrowserMain extends App {
  Application.launch(classOf[BrowserGuiApp], args: _*)
}

class BrowserGuiApp extends Application {
  override def start(primaryStage: Stage) {
    primaryStage.setTitle("Browser")

    val root = new StackPane
    val browser = new Browser()
    root.getChildren.add(browser.Gui)
    root.getStylesheets().add(getClass().getResource("style.css").toExternalForm())

    primaryStage.setScene(new Scene(root, 800, 600))
    primaryStage.show()
    
    browser.init()
  }
}

object PartDragGroup extends DragGroup("Part")
object AnimationPartDragGroup extends DragGroup("AnimationPart")
object ImageDragGroup extends DragGroup("Image")

class Animation(name: String = "unnamed", parts: java.util.List[Part] = new ArrayList[Part]()) {
  val nameProperty = new SimpleStringProperty(name)
  val partsProperty = FXCollections.observableList(parts)
}
case class Part(name: String = "unnamed", val images: java.util.List[Image] = new ArrayList[Image]()) {
  val nameProperty = new SimpleStringProperty(name)
  val imagesProperty = FXCollections.observableList(images)
}
case class PartFilter(val query: String)

class Browser {

  def init() = {
    val image1 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/b1.png")))
    val image2 = new Image(new FileInputStream(new File("/home/hetzge/private/werke/animation/test/e2.png")))

    val imageListA = new ArrayList[Image](2)
    imageListA.add(image1)
    imageListA.add(image2)
    
    val imageListB = new ArrayList[Image](1)
    imageListB.add(image2)
    
    Model.Property.partsPool.add(Part("Part A", imageListA))
    Model.Property.partsPool.add(Part("Part B", imageListB))

    Service.filterParts()
  }

  object Model {
    val partsPool = new java.util.ArrayList[Part]()
    val filteredParts = new java.util.ArrayList[Part]()
    val animationPool = new java.util.ArrayList[Animation]()

    object Property {
      val partsPool = FXCollections.observableList(Model.this.partsPool)
      val filteredParts = FXCollections.observableList(Model.this.filteredParts)
      val animationPool = FXCollections.observableList(Model.this.animationPool)

      partsPool.addListener(FilterPartPoolListener)
      object FilterPartPoolListener extends ListChangeListener[Part] {
        override def onChanged(change: ListChangeListener.Change[_ <: Part]) = {
          Service.filterParts(PartFilter(null))
        }
      }
    }
  }

  object Service {
    def filterParts(filter: PartFilter = PartFilter(null)) = {
      val filteredParts = Model.partsPool.filter((part: Part) => {
        filter.query == null || part.name.contains(filter.query)
      })
      Model.Property.filteredParts.clear()
      Model.Property.filteredParts.addAll(filteredParts)
    }

    def newAnimation() = {
      Model.Property.animationPool.add(new Animation())
    }

    def newPart() = {
      Model.Property.partsPool.add(Part())
    }

    def deletePart(part: Part) {
      Model.Property.partsPool.removeAll(part)
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
      getChildren().add(AnimationBrowserMenu)
      getChildren().add(AnimationList)
      FxHelper.setAnchor(AnimationBrowser.this)

      object AnimationList extends Repeater[Animation](Model.Property.animationPool) {
        def cellFactory(animation: Animation) = new AnimationPartListCell(animation)

        class AnimationPartListCell(animation: Animation) extends VBox {
          getChildren().addAll(AnimationCellToolBar, AnimationPartList)

          object AnimationCellToolBar extends ToolBar {
            val editModeProperty = new SimpleBooleanProperty(false)
            getItems().addAll(DeleteButton, EditButton, EditTitlePane)

            object DeleteButton extends Button {
              setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.REMOVE, "16px"))
              setOnAction((actionEvent: ActionEvent) => {
                new Alert(AlertType.CONFIRMATION) {
                  setTitle("Confirm delete")
                  setContentText("Do you really want delete this item ?")

                  if (showAndWait().get() == ButtonType.OK) {
                    Model.Property.animationPool.remove(animation)
                  }
                }
              })
            }

            object EditButton extends Button {
              def setupGraphic(editMode: Boolean) = setGraphic(GlyphsDude.createIcon(if (editMode) FontAwesomeIcon.CLOSE else FontAwesomeIcon.EDIT, "16px"))
              setupGraphic(editModeProperty.get())
              editModeProperty.addListener(new ChangeListener[java.lang.Boolean] {
                override def changed(observable: ObservableValue[_ <: java.lang.Boolean], oldValue: java.lang.Boolean, newValue: java.lang.Boolean) = {
                  setupGraphic(newValue)
                }
              })
              setOnAction((actionEvent: ActionEvent) => {
                editModeProperty.set(!editModeProperty.get())
              })
            }

            object EditTitlePane extends StackPane {
              getChildren().addAll(Title, EditTitle)

              object Title extends Label() {
                textProperty().bind(animation.nameProperty)
                visibleProperty().bind(editModeProperty.not())
              }
              object EditTitle extends TextField {
                textProperty().bindBidirectional(animation.nameProperty)
                visibleProperty().bind(editModeProperty)
              }
            }

          }
          object AnimationPartList extends PartList(animation.partsProperty, AnimationPartDragGroup, Vector(PartDragGroup, AnimationPartDragGroup))
        }
        
        init()
      }

      object AnimationBrowserMenu extends ToolBar {
        getItems().add(NewButton)

        object NewButton extends Button {
          setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PLUS_CIRCLE, "24px"))
          setOnAction((actionEvent: ActionEvent) => {
            Service.newAnimation()
          })
        }
      }
    }

    object PartBrowser extends VBox {
      getChildren().add(PartBrowserMenu)
      getChildren().add(PartBrowserPartList)
      FxHelper.setAnchor(PartBrowser.this)
      setMinHeight(200)

      object PartBrowserMenu extends ToolBar {
        getItems().add(NewButton)
        getItems().addAll(SearchTextField, SearchButton)

        object SearchTextField extends TextField

        object SearchButton extends Button {
          setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.SEARCH, "24px"))
          setOnAction((actionEvent: ActionEvent) => {
            Service.filterParts(PartFilter(SearchTextField.getText()))
          })
        }

        object NewButton extends Button {
          setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PLUS_CIRCLE, "24px"))
          setOnAction((actionEvent: ActionEvent) => {
            Service.newPart()
          })
        }
      }

      object PartBrowserPartList extends PartList(Model.Property.filteredParts, PartDragGroup, Vector(PartDragGroup))
    }

    object PartList {
      val CELL_HEIGHT = 60
    }
    class PartList(sourceParts: ObservableList[Part], val dragGroup: DragGroup, val allowedDragGroups: Vector[DragGroup]) extends DragRepeater[Part](sourceParts, true) {

      def dragCellFactory(part: Part): Node = new PartCell(part)

      class PartCell(val part: Part) extends HBox {
        getChildren().add(DeletePartCellButton)
        getChildren().add(CellLabel)
        getChildren().add(ImageList)
        getChildren().add(AddImageButton)

        object DeletePartCellButton extends Button {
          setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.REMOVE, "24px"))
          setOnAction((actionEvent: ActionEvent) => {
            println("clicked")
            new Alert(AlertType.CONFIRMATION) {
              setTitle("Confirm delete")
              setContentText("Do you really want delete this item ?")

              if (showAndWait().get() == ButtonType.OK) {
                Model.Property.partsPool.remove(part) // TODO
              }
            }
          })
        }

        object CellLabel extends Label {
          setText(part.name)
          setSpacing(10.0d)
          setPadding(new Insets(5.0d))
        }
        
        object AddImageButton extends Button {
          setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PLUS, "24px"))
          setOnAction((actionEvent: ActionEvent) => {
            part.imagesProperty.add( new WritableImage(64, 64))
          })
        }

        object ImageList extends DragRepeater[Image](part.imagesProperty, false) {
          val dragGroup: DragGroup = ImageDragGroup
          val allowedDragGroups: Vector[DragGroup] = Vector(ImageDragGroup)

          def dragCellFactory(image: Image): Node = new ImageCell(image)

          class ImageCell(val image: Image) extends ImageView(image) {
            fitWidthProperty().set(32)
            fitHeightProperty().set(32)
          }
          
          init()
        }
      }
      
      init()
    }

  }

  // TODO extract remove ...
}