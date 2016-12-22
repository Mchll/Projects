
package Blur

import java.io.File
import javax.imageio.ImageIO
import swing.{Panel, MainFrame, SimpleSwingApplication}
import java.awt.{Color, Graphics2D, Dimension}
import java.awt.image.BufferedImage


class DrawPicture(image : BufferedImage) extends Panel {

  override def paintComponent(g: Graphics2D) {
    val width = image.getWidth()
    val height = image.getHeight()
    val dx = g.getClipBounds.width.toFloat / width
    val dy = g.getClipBounds.height.toFloat / height

    g.drawRenderedImage(image, java.awt.geom.AffineTransform.getScaleInstance(dx, dy))
  }
}

object out extends SimpleSwingApplication {

  println("Here you can write a name of the file that you want to blur: ")
  val name = scala.io.StdIn.readLine()
  val image = ImageIO.read(new File(name))
  val width = image.getWidth
  val height = image.getHeight

  println("Here you can write a radius that you want: ")
  val radius = scala.io.StdIn.readInt()

  println("Here you can write a number of threads that you want: ")
  val threads = scala.io.StdIn.readInt()

  println("Here you can write a horizontal or vertical: ")
  val horv = scala.io.StdIn.readLine()

  var output : BufferedImage = image
  if(horv == "horizontal") {
    horizontal_parallel(radius, threads, output)
  }
  if(horv == "vertical") {
    vertical_parallel(radius, threads, output)
  }

  def top = new MainFrame {
    contents = new DrawPicture(output) {
      preferredSize = new Dimension(width, height)
    }
  }
}

