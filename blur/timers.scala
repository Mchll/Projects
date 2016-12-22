package Blur

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.scalameter._

object Timers extends Bench.LocalTime{

  val myConfig = config(
    Key.exec.minWarmupRuns -> 10,
    Key.exec.maxWarmupRuns -> 15
  ) withWarmer new Warmer.Default

  def horizontal_parallel_time(radius : Int, threads : Int, image : BufferedImage) : Quantity[Double] ={
    val time = myConfig measure{
      horizontal_parallel(radius, threads, image)
    }
    time
  }

  def vertical_parallel_time(radius : Int, threads : Int, image : BufferedImage) : Quantity[Double] ={
    val time = myConfig measure{
      vertical_parallel(radius, threads, image)
    }
    time
  }
}

object Time {
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
      println(Timers.horizontal_parallel_time(radius, threads, output))
    }
    if(horv == "vertical") {
      println(Timers.vertical_parallel_time(radius, threads, output))
    }
  }
}