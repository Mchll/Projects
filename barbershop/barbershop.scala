import scala.io.StdIn.readLine
import java.io.{File, PrintWriter}

class MyQueue (chairs: Int) {
  val maxi = chairs
  var engaged = 0
  var my_queue = List[Int]()
  def armchair (index: Int) = {
    this.synchronized {
      if (engaged <= maxi - 1) {
        engaged = engaged + 1
        my_queue = my_queue :+ index
        Barbershop.log_output.write("Arrival of client: " + index + "\r\n")
        Barbershop.log_output.write("Number of clients in queue: " + engaged + "\r\n")
      }
      else {
        Barbershop.log_output.write("Arrival of client: " + index + ". Oh, sorry, there is no place for you here!" + "\r\n")
      }
    }
  }
  def GoToMaster (): Int = {
    var client = 0
    this.synchronized {
      if (engaged > 0) {
        engaged = engaged - 1
        client = my_queue.head
        my_queue = my_queue.tail
      }
    }
    client
  }
}

class Client (bench: MyQueue, num: Int) extends Thread {
  val my_queue = bench
  val index = num
  override def run() = {
    my_queue.armchair(index)
  }
}

class Master (time: Int, bench: MyQueue, num: Int) extends Thread {
  val index = num
  val my_queue = bench
  override def run() = {
    val client = my_queue.GoToMaster()
    if (client != 0) {
      Barbershop.log_output.write("Client: " + client + " goes to master: " + index  + "\r\n")
      Thread.sleep (time * 1000)
      Barbershop.log_output.write("Client " + client + " goes away from master: " + index + "\r\n")
      if (my_queue.engaged != -1) {
        Barbershop.log_output.write("Number of clients in queue: " + my_queue.engaged + "\r\n")
      }
      run()
    }
    else {
      Barbershop.log_output.write("Master: " + index + " is ready to start now" + "\r\n")
      var clients = my_queue.engaged
      while (clients == 0) {
        Thread.sleep (77)
        clients = my_queue.engaged
      }
      if (clients != -1) {
        run()
      }
    }
  }
}

object Barbershop {
  val log_output = new PrintWriter(new File("log_output.txt"))
  def main(args: Array[String]) = {

    println("Here you can write a number of chairs for clients that you want: ")
    val chairs = scala.io.StdIn.readInt()
    log_output.write("Number of chairs: " + chairs + "\r\n")

    println("Here you can write a number of masters that you want: ")
    val masters = scala.io.StdIn.readInt()
    log_output.write("Number of masters: " + masters + "\r\n")

    println("Here you can write a number of clients that you want: ")
    val clients = scala.io.StdIn.readInt()
    log_output.write("Number of clients: " + clients + "\r\n")

    val random = scala.util.Random
    val my_queue = new MyQueue (chairs)
    val arr_master = new Array[Master](masters + 1)
    for ( i <- 1 until (masters + 1)) {
      val time = (Math.abs(random.nextInt) % 6) + 5
      arr_master(i) = new Master(time, my_queue, i)
      log_output.write("There is a master: " + i + ". Time needed to do haircut: " + time + "\r\n")
    }
    var number = 1
    for ( i <- 1 until (masters + 1)) {
      arr_master(i).start
    }
    log_output.write("New day is new life! Barbershop is open now!" + "\r\n")
    Thread.sleep(777)
    while (number != (clients + 1)) {
      new Client(my_queue, number).start
      number = number + 1
      val time = (Math.abs(random.nextInt) % 15) + 1
      Thread.sleep(time * 77)
    }
    my_queue.engaged = -1
    for ( i <- 1 until (masters + 1)) {
      arr_master(i).join
    }
    log_output.write("It was a good time! Barbershop is going to be closed for today." + "\r\n")
    println("That's all")
    log_output.close
  }
}

