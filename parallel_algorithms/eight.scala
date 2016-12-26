object Linear_equations {

  var a : Array[Int] = Array.emptyIntArray
  var b : Array[Int] = Array.emptyIntArray
  var threads = 1
  var length = 1
  var pairs : Array[(Int, Int)] = Array.empty

  def DefineOperation(first : (Int, Int), second : (Int, Int)) : (Int, Int) = {
    (first._1 * second._1, second._1 * first._2 + second._2)
  }

  def OneThread(p : Array[(Int, Int)], len : Int) : Int = {
    var x = p(0)._2
    for (i <- 1 to len) {
      x = p(i)._1 * x + p(i)._2
    }
    x
  }

  def PowerOfTwo (x : Int) : Int = {
    var f = 1
    var current = 0
    while (f < x) {
      f *= 2
      current += 1
    }
    current
  }

  def GetIntFromStr (str : String, num : Int) : Array[Int] = {
    val len = str.length
    val size = scala.math.pow (2, PowerOfTwo(len)).toInt
    val arr: Array[Int] = new Array[Int](size)
    for (i <- 0 until len) {
      arr(i) = str.charAt(i).asDigit
    }
    for (i <- len until size) {
      arr(i) = num
    }
    arr
  }

  def SetOneSize(arr : Array[Int], size : Int, num : Int) : Array[Int] = {
    val res : Array[Int] = new Array[Int](size)
    for (i <- arr.indices) {
      res(i) = arr(i)
    }
    val len = arr.length
    for (i <- len until size) {
      res(i) = num
    }
    res
  }

  def Collect() : Array[(Int, Int)] = {

    val arr : Array[(Int, Int)] = pairs
    val num = length / threads

    def ThreadWork(l : Int, r : Int) : Unit = {
      var current = 2
      val maxi = r - l + 1
      while (current <= maxi) {
        var i = l + current - 1
        while (i <= r) {
          arr(i) = DefineOperation(arr(i - current / 2), arr(i))
          i += current
        }
        current *= 2
      }
    }

    def Threads(l : Int, r : Int) : Unit = {
      if (r - l + 1 == num) {
        ThreadWork(l, r)
      }
      else {
        println(l, r)
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

    Threads(0, length - 1)
    arr
  }

  def Distribute() : Array[(Int, Int)] = {
    val arr : Array[(Int, Int)] = pairs
    val numPerThr = length / threads
    def ThreadWork(l : Int, r : Int) : Unit = {
      var current = r - l + 1
      while (current >= 2) {
        var i = current - 1 + l
        while (i <= r) {
          val tmp = arr(i)
          arr(i) = DefineOperation(arr(i), arr(i - current / 2))
          arr(i - current / 2) = tmp
          i += current
        }
        current /= 2
      }
    }
    def Threads(l : Int, r : Int) : Unit = {
      if (r - l + 1 == numPerThr) {
        ThreadWork(l, r)
      }
      else {
        println(l, r)
        val new_thread = new Thread() {
          override def run() : Unit = {
            Threads((r - l) / 2 + 1 + l, r)
          }
        }
        val tmp = arr(r)
        arr(r) = DefineOperation(arr(r), arr((r - l - 1) / 2 + l))
        arr((r - l - 1) / 2 + l) = tmp
        new_thread.start()
        Threads(l, (r - l - 1) / 2 + l)
        new_thread.join()
      }
    }
    Threads(0, length - 1)
    arr
  }

  def main(args: Array[String]): Unit = {
    println("Here you can write an arrays: ")
    a = GetIntFromStr(scala.io.StdIn.readLine(), 1)
    b = GetIntFromStr(scala.io.StdIn.readLine(), 0)
    println("Here you can write a number of threads that you want: ")
    threads = scala.io.StdIn.readInt()
    println("Here you can write an index that you want: ")
    val index : Int = scala.io.StdIn.readInt()

    val num = Math.max(a.length, b.length) - 1
    if (a.length > b.length) {
      b = SetOneSize(b, a.length, 0)
    } else {
      if (a.length != b.length) {
        a = SetOneSize(a, b.length, 1)
      }
    }

    length = a.length
    pairs = a zip b
    var lastPair = (1,0)
    if (index == num) {
      lastPair = pairs(num)
    }
    pairs = Collect()
    pairs(pairs.length - 1) = (1,0)
    pairs = Distribute()
    if (index == num) {
      print(DefineOperation(pairs(index), lastPair)._2)
    }
    else {
      print(pairs(index + 1)._2)
    }
  }
}