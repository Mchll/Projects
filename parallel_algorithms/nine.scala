object Correct_parentheses {

  var parentheses : Array[(Int, Int)] = Array.empty
  var threads = 1
  var length = 1

  def OneThread(p: Array[Char]): Boolean = {
    var current = 0
    var ans = true
    for (i <- 0 until p.length) {
      if (p(i) == '(') {
        current = current + 1
      }
      else {
        if (current > 0) {
          current = current - 1
        }
        else {
          ans = false
        }
      }
    }
    if (current != 0) {
      ans = false
    }
    ans
  }
  
  def DefineOperation(first : (Int, Int), second : (Int, Int)) : (Int, Int) = {
    val tmp = Math.min(first._1, second._2)
    (first._1 + second._1 - tmp, first._2 + second._2 - tmp)
  }

  def GetArray() : Array[(Int, Int)] = {
    val str = scala.io.StdIn.readLine()
    val arr : Array[(Int, Int)] = new Array[(Int, Int)](str.length)
    def Threads(l : Int, r : Int) : Unit = {
      for (i <- l to r) {
        if (str(i) == '(')
          arr(i) = (1,0)
        else arr(i) = (0,1)
      }
    }
    val num = str.length / threads
    val tmp = num until str.length by num
    val tasks = tmp.map(x => {
      new Thread {
        override def run() : Unit = {
          Threads(x, x + num - 1)
        }
      }
    }
    )
    tasks.foreach(x => x.start())
    Threads(0, num - 1)
    tasks.foreach(x => x.join())
    arr
  }

  def Collect() : Array[(Int, Int)] = {
    val arr : Array[(Int, Int)] = parentheses
    val num = length / threads
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
    Threads(0, length - 1)
    arr
  }

  def main(args: Array[String]): Unit = {
    println("Here you can write a sequence: ")
    parentheses = GetArray()
    length = parentheses.length
    parentheses = Collect()

    if (parentheses(length - 1) == (0,0))
      println("This is CORRECT sequence")
    else println("Ohh, sorry, it's WRONG sequence")
  }
}

