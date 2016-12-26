object Turtle {

  val into_radian = Math.PI / 180
  var threads = 1

  def OneThread(num: Array[Int], ang: Array[Int]): (Int, Int) = {
    var a = 0
    var b = 0
    var angle = 0
    for (i <- 0 until num.length) {
      angle = (angle + ang(i)) % 360
      a = a + (num(i) * Math.cos(angle * into_radian)).toInt
      b = b + (num(i) * Math.sin(angle * into_radian)).toInt
    }
    (a, b)
  }

    def DefineOperation(p : (Double, Double, Double), q : (Double, Double, Double)) : (Double, Double, Double) = {
      val (a, realAlpha, alpha) = p
      val (b, realBeta,  beta)  = q
      val sqr = Math.sqrt(a * a + b * b + 2 * a * b * Math.cos((realBeta + alpha - realAlpha) * into_radian))
      if (sqr == 0) {
        (sqr, realAlpha, (alpha + beta) % 360)
      }
      else {
        (sqr,
          (realAlpha + Math.asin(b * Math.sin((realBeta + alpha - realAlpha) * into_radian) / sqr) / into_radian) % 360,
          (alpha + beta) % 360)
      }
    }

    def Collect(space : Array[(Double, Double, Double)], n : Int) : Array[(Double, Double, Double)] = {
      val arr : Array[(Double, Double, Double)] = space
      val num = n / threads
      def ThreadWork(l : Int, r : Int) : Unit = {

        for (i <- l + 1 to r) {
          arr(i) = DefineOperation(arr(i - 1), arr(i))
        }
      }
      def Threads(l : Int, r : Int) : Unit = {
        if (r - l + 1 == num) {
          ThreadWork(l, r)
        }
        else {
          val new_thread = new Thread() {
            override def run() : Unit = {
              Threads((r - l) / 2 + 1 + l, r)
            }
          }
          new_thread.start()
          Threads(l, (r - l - 1) / 2 + l)
          new_thread.join()
          arr(r) = DefineOperation(arr((r - 1 - l) / 2 + l), arr(r))
        }
      }
      Threads(0, n - 1)
      arr
    }

    def main(args: Array[String]): Unit = {
      var n = 1
      println("Here you can write a number of threads that you want: ")
      threads = scala.io.StdIn.readInt()
      println("Here you can write a number of commands that you want: ")
      n = scala.io.StdIn.readInt()
      var need = 0
      if (n % threads != 0) {
        need = threads - n % threads
      }
      println("Here you can write commands that you want: ")
      var space : Array[(Double, Double, Double)] = new Array[(Double, Double, Double)](n + need)
      for (i <- 0 until n) {
        val angle = scala.io.StdIn.readInt().toDouble
        val dist = scala.io.StdIn.readInt().toDouble
        space(i) = (dist, angle, angle)
      }
      for (i <- n until n + need) {
        space(i) = (0.0, 0.0, 0.0)
      }
      n += need
      space = Collect(space, n)
      println(space.last._1 * Math.cos(space.last._2 * into_radian), space.last._1 * Math.sin(space.last._2 * into_radian))
    }
  }
