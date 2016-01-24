package de.hetzge.sgame.spritegen.component

import de.hetzge.sgame.spritegen.ColorUsage
import javafx.scene.control.Dialog
import javafx.scene.layout.GridPane
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import javafx.scene.control.ColorPicker
import javafx.geometry.Pos
import javafx.scene.paint.Color

import de.hetzge.sgame.spritegen.FxHelper._

class EditColorUsageDialog(colorUsage: ColorUsage = ColorUsage("unnamed", Color.RED)) extends Dialog[ColorUsage] {
  setTitle("Add color usage")
  getDialogPane().setContent(Form)
  setResultConverter(Form.resultConverter)
  val buttonType = new ButtonType("Ok")
  getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL)

  object Form extends GridPane {
    setAlignment(Pos.TOP_LEFT)
    setHgap(10)
    setVgap(10)
    setPadding(new Insets(25, 25, 25, 25))
    add(new Label("Name"), 0, 0)
    add(NameTextField, 1, 0)
    add(new Label("Color"), 0, 1)
    add(ColorChooser, 1, 1)

    val resultConverter = (buttonType: ButtonType) => {
      if (buttonType == EditColorUsageDialog.this.buttonType) {
        val nameTextFieldText = NameTextField.getText()
        val name = if (nameTextFieldText.isEmpty()) "unnamed" else nameTextFieldText
        new ColorUsage(name, ColorChooser.getValue())
      } else {
        null
      }
    }

    object NameTextField extends TextField {
      setText(colorUsage.name)
    }

    object ColorChooser extends ColorPicker {
      setValue(colorUsage.color)
    }
  }
}