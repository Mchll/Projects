import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage


package object Blur {

  def Red(byte : Int) : Int = {
    val mask = 0x000000FF
    byte >>> 24 & mask
  }

  def Green(byte : Int) : Int = {
    val mask = 0x000000FF
    byte >>> 16 & mask
  }

  def Blue(byte : Int) : Int = {
    val mask = 0x000000FF
    byte >>> 8 & mask
  }

  def Alpha(byte : Int) : Int = {
    val mask = 0x000000FF
    byte & mask
  }

  def Merger(red : Int, green : Int, blue : Int, alpha : Int): Int = {
    (red << 24) | (green << 16) | (blue << 8) | alpha
  }

  def ByteBlur(x : Int, y : Int, radius : Int, width : Int, height : Int, arr : BufferedImage) : Int ={
    var r_sum = 0
    var g_sum = 0
    var b_sum  = 0
    var a_sum = 0
    var r = 0
    var g = 0
    var b = 0
    var a = 0
    var num = 0
    for (i <- (x - radius) until (x + radius))
      for (j <- (y - radius) until (y + radius)){
        if ((i >= 0) && (i < width) && (j >= 0) && (j < height)) {
          r = Red(arr.getRGB(i, j))
          g = Green(arr.getRGB(i, j))
          b = Blue(arr.getRGB(i, j))
          a = Alpha(arr.getRGB(i, j))
          r_sum = r_sum + r
          g_sum = g_sum + g
          b_sum = b_sum + b
          a_sum = a_sum + a
          num = num + 1
        }
      }
    r_sum = r_sum / num
    g_sum = g_sum / num
    b_sum = b_sum / num
    a_sum = a_sum / num

    Merger(r_sum, g_sum, b_sum, a_sum)
  }

  def horizontal_parallel(radius: Int, threads: Int, input_img: BufferedImage): BufferedImage = {
    val width = input_img.getWidth
    val height = input_img.getHeight
    var period = height / threads
    if (threads * period != height) {
      period = period + 1
    }
    val output_img = input_img
    val tasks_list = Range(0, height) by period
    val tasks = tasks_list.map(k => {
      new Thread {
        override def run(): Unit = {
          if ((k + period) > height) {
            for (i <- 0 until width; j <- k until height) {
              output_img.setRGB(i, j, ByteBlur(i, j, radius, width, height, input_img))
            }
          }
          else {
            for (i <- 0 until width; j <- k until (k + period)) {
              output_img.setRGB(i, j, ByteBlur(i, j, radius, width, height, input_img))
            }
          }
        }
      }
    })
    tasks.foreach(k => k.start)
    tasks.foreach(k => k.join)
    output_img
  }

  def vertical_parallel(radius: Int, threads: Int, input_img: BufferedImage): BufferedImage = {
    val width = input_img.getWidth
    val height = input_img.getHeight
    var period = width / threads
    if (threads * period != width) {
      period = period + 1
    }
    val output_img = input_img
    val tasks_list = Range(0, width) by period
    val tasks = tasks_list.map(k => {
      new Thread {
        override def run(): Unit = {
          if ((k + period) > width) {
            for (i <- 0 until height; j <- k until width) {
              output_img.setRGB(j, i, ByteBlur(j, i, radius, width, height, input_img))
            }
          }
          else {
            for (i <- 0 until height; j <- k until (k + period)) {
              output_img.setRGB(j, i, ByteBlur(j, i, radius, width, height, input_img))
            }
          }
        }
      }
    })
    tasks.foreach(k => k.start)
    tasks.foreach(k => k.join)
    output_img
  }

  def main(args: Array[String]): Unit = {
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

    ImageIO.write(output, "jpg", new File("output.jpg"))
  }

}
