import scala.io.StdIn.readLine
import java.io.{BufferedReader, FileReader}

object CheckingLog {

  def parse_str(str: BufferedReader): List[String] = {
    var line = str.readLine
    var log_output = List[String]()
    while (line != null) {
      log_output = line :: log_output
      line = str.readLine
    }
    log_output.reverse
  }

  def queue_size(num: Int, strs: List[String]): Boolean = {
    if (strs.isEmpty) {
      true
    }
    else {
      if (strs.head.contains("in queue")) {
        val tmp = strs.head.split(' ')(5)
        if (tmp.toInt > num) {
          println("Oh, sorry, there is an ERROR in code! Clients on chairs > chairs")
          return false
        }
        if (tmp.toInt < 0) {
          println("Oh, sorry, there is an ERROR in code! Clients on chairs < 0")
          return false
        }
        queue_size(num, strs.tail)
      }
      else {
        queue_size(num, strs.tail)
      }
    }
  }

  def done_work(num: Int, strs: List[String], max: Int): Boolean = {
    if (strs.isEmpty) {
      (num == 0)
    }
    else {
      if (strs.head.contains("goes")) {
        if (strs.head.contains("to master")) {
          if (num + 1 > max) {
            println("Oh, sorry, there is an ERROR in code! Finished works > started works")
            return false
          }
          done_work(num + 1, strs.tail, max)
        }
        else {
          if (num - 1 < 0) {
            println("Oh, sorry, there is an ERROR in code! Finished works < started works")
            return false
          }
          done_work(num - 1, strs.tail, max)
        }
      }
      else {
        done_work(num, strs.tail, max)
      }
    }
  }

  def m_for_c(strs: List[String]): Boolean = {
    def clients_indexes (lines: List[String], names: List[String]): List[String] = {
      if (lines.isEmpty) {
        names
      }
      else {
        if (lines.head.contains("to master")) {
          val name = lines.head.split(' ')(1)
          clients_indexes(lines.tail, name :: names)
        }
        else {
          clients_indexes(lines.tail, names)
        }
      }
    }
    def for_ones(lis: List[String]): Boolean = {
      if (lis.isEmpty) {
        true
      }
      else {
        if (lis.tail.contains(lis.head)) {
          false
        }
        else {
          for_ones(lis.tail)
        }
      }
    }
    val indexes = clients_indexes(strs, List())
    for_ones(indexes)
  }

  def main(args: Array[String]) = {
    val log_output = parse_str(new BufferedReader(new FileReader("log_output.txt")))
    val chairs = (log_output(0).split(' '))(3).toInt
    val masters = (log_output(1).split(' '))(3).toInt
    val clients = (log_output(2).split(' '))(3).toInt

    val drop_log_output = log_output.drop(4 + masters)
    if (done_work(0, drop_log_output, masters)) {
      println("All masters are OK!")
      if (queue_size(chairs, drop_log_output)) {
        println("All clients in queue are OK!")
        if (m_for_c(drop_log_output)) {
          println("All errors in log_output are OK! (there are no errors here, yeah)")
        }
        else {
          println("Oh, sorry, there is an ERROR in code! Too many masters for one client")
        }
      }
    }
    else {
      println("Oh, sorry, there is an ERROR in code! Some masters haven't finished haircut")
    }

    println()
    println("That's all")
  }
}