package object BidIntegersSum {

  var a : Array[Int] = Array.emptyIntArray
  var b : Array[Int] = Array.emptyIntArray
  var c : Array[Char] = Array.emptyCharArray
  var threads = 1
  var length = 1

  def OneThread(a: Array[Int], b: Array[Int]): Array[Int] = {
    val size = math.max(a.length, b.length) + 1
    val sum = new Array[Int](size)
    var c = 0
    for (i <- 0 until size - 1) {
      sum(i) = (a(i) + b(i) + c) % 10
      c = (a(i) + b(i) + c) / 10
    }
    sum
  }

  def DefineOperation(x : Char, y : Char) : Char = {
    if (y == 'M') x
    else y
  }

  def PowerOfTwo (x : Int) : Int = {
    var f = 1
    var currentrent = 0
    while (f < x) {
      f *= 2
      currentrent += 1
    }
    currentrent
  }

  def SetOneSize(arr : Array[Int], toSize : Int) : Array[Int] = {
    val arr : Array[Int] = new Array[Int](toSize)
    for (i <- arr.indices) {
      arr(i) = arr(i)
    }
    val len = arr.length
    for (i <- len until toSize) {
      arr(i) = 0
    }
    arr
  }

  def GetIntFromStr (str : String) : Array[Int] = {
    val len = str.length
    val size = scala.math.pow (2, PowerOfTwo(len)).toInt
    val arr: Array[Int] = new Array[Int](size)
    val reverse_string = str.reverse
    for (i <- 0 until len) {
      arr(i) = reverse_string.charAt(i).asDigit
    }
    for (i <- len until size) {
      arr(i) = 0
    }
    arr
  }

  def MakeC() : Array[Char] = {
    val arr : Array[Char] = new Array[Char](length)
    def Threads(l: Int, r : Int) : Unit = {
      for (i <- l until r) {
        val sum = a(i) + b(i)
        if (sum > 9) arr(i) = 'C'
        else if (sum == 9) arr(i) = 'M'
        else arr(i) = 'N'
      }
    }
    val num = length / threads
    val for_thread = num until length by num
    val tmp = for_thread.map(x => {
        new Thread {
          override def run() : Unit = {
            Threads(x, x + num)
          }
        }
      }
      )
    tmp.foreach(x => x.start())
    Threads(0, num)
    tmp.foreach(x => x.join())
    arr
  }

  def Collect() : Array[Char] = {
    val arr : Array[Char] = c
    val num = length / threads
    def ThreadWork(l : Int, r : Int) : Unit = {
      var current = 2
      val need = r - l + 1
      while (current <= need) {
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

  def distributePhase() : Array[Char] = {
    val arr : Array[Char] = c
    val num = length / threads
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
      if (r - l + 1 == num) {
        ThreadWork(l, r)
      }
      else {
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
    println("Here you can write a first number: ")
    val aa = scala.io.StdIn.readLine()
    a = GetIntFromStr(aa)
    println("Here you can write a second number: ")
    val bb = scala.io.StdIn.readLine()
    b = GetIntFromStr(bb)
    println("Here you can write a number of threads that you want: ")
    threads = scala.io.StdIn.readInt()
    val first_length = Math.max(aa.length, bb.length)
    if (a.length > b.length) {
      b = SetOneSize(b, a.length)
    } else {
      if (a.length != b.length) {
        a = SetOneSize(a, b.length)
      }
    }
    length = a.length
    c = MakeC()
    c = Collect()
    c(length - 1) = 'M'
    c = distributePhase()
    val arr = new Array[Int](length + 1)
    arr(length) = 0
    for (i <- 0 until length) {
      arr(i) = a(i) + b(i)
      if (c(i) == 'C') {
        arr(i) += 1
      }
      arr(i) %= 10
    }
    if (c(first_length - 1) == 'C') {
      arr(first_length) = 1
    }
    var state = 0
    for (i <- first_length to 0 by -1) {
      state match {
        case 0 => if (arr(i) != 0) {
          state = 1
          print(arr(i))
        }
        case 1 => print(arr(i))
      }
    }
  }
}