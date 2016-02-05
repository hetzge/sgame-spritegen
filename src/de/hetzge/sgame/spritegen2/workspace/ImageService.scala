package de.hetzge.sgame.spritegen2.workspace

import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.SnapshotParameters
import javafx.scene.image.PixelFormat

object ImageService {
  def imageToCanvas(image: Image, canvas: Canvas) = {
    clearCanvas(canvas)
    val (x, y) = (getCenteredX(image, canvas), getCenteredY(image, canvas))
    canvas.getGraphicsContext2D().drawImage(image, x, y)
  }

  def canvasToImage(canvas: Canvas, image: WritableImage) = {
    canvas.snapshot(new SnapshotParameters(), image)
  }

  def imageToImage(image: Image, writableImage: WritableImage) = {
    val pixelReader = image.getPixelReader()
    val pixelWriter = writableImage.getPixelWriter()

    val x = getCenteredX(writableImage, image).intValue()
    val y = getCenteredY(writableImage, image).intValue()
    val width = image.getWidth().intValue()
    val height = image.getHeight().intValue()
    val pixelFormat = PixelFormat.getIntArgbInstance()
    val buffer = new Array[Int](width * height)
    val offset = 0
    val scanline = width

    pixelReader.getPixels(0, 0, width, height, pixelFormat, buffer, offset, scanline)
    pixelWriter.setPixels(x, y, width, height, pixelFormat, buffer, offset, scanline)
  }

  private def getCenteredX(one: Image, two: Image) = center(one.getWidth(), two.getWidth())
  private def getCenteredX(image: Image, canvas: Canvas) = center(canvas.getWidth(), image.getWidth())
  private def getCenteredX(canvas: Canvas, image: Image) = center(image.getWidth(), canvas.getWidth())
  private def getCenteredY(one: Image, two: Image) = center(one.getHeight(), two.getHeight())
  private def getCenteredY(image: Image, canvas: Canvas) = center(canvas.getHeight(), image.getHeight())
  private def getCenteredY(canvas: Canvas, image: Image) = center(image.getHeight(), canvas.getHeight())
  private def center(sizeA: Double, sizeB: Double) = sizeA / 2 - sizeB / 2;

  private def clearCanvas(canvas: Canvas) = {
    canvas.getGraphicsContext2D().clearRect(0d, 0d, canvas.getWidth(), canvas.getHeight())
  }
}