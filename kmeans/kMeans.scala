import scala.collection.GenSeq
import scala.annotation.tailrec
import scala.util.Random
import org.scalameter._

object KMeans {

  type Point = (Double, Double)
  def dist(x1 : Point, x2 : Point) : Double = {
    Math.sqrt(Math.pow(x1._1 - x2._1, 2) + Math.pow(x1._2 - x2._2, 2))
  }

  def generatePoints(k : Int, num : Int) : Seq[Point] = {
    val rand = Random
    for {
      i <- 0 until k
    } yield (rand.nextDouble() + rand.nextInt(num), rand.nextDouble() + rand.nextInt(num))
  }

  def initializeMeans(k : Int, points : Seq[Point]) : Seq[Point] = {
    Random.shuffle(points).take(k)
  }

  def findClosest(p : Point, means : GenSeq[Point]) : Point = {
    means.minBy(dist(p, _))
  }

  def classify(points : GenSeq[Point], means : GenSeq[Point]) : GenSeq[(Point, GenSeq[Point])] = {
    val tmp = (-1.0, -1.0)
    val ans = means.map(c => (c, points.map(
      p => if (findClosest(p, means) == c) p
      else tmp
    ).filter(_ != tmp)
      )
    )
    ans
  }

  def findAverage(oldMean : Point, points : GenSeq[Point]) : Point = {
    if (points.isEmpty) {
      oldMean
    }
    else {
      val p = points.unzip
      (p._1.sum / points.length, p._2.sum / points.length)
    }
  }

  def update(classified : GenSeq[(Point, GenSeq[Point])]) : GenSeq[Point] = {
    classified.map(pair => findAverage(pair._1, pair._2))
  }

  def converged(eta : Double)(oldMeans : GenSeq[Point], newMeans : GenSeq[Point]) : Boolean = {
    oldMeans.zip(newMeans).forall({
      case (old_mean, new_mean) => dist(old_mean, new_mean) < eta
    })
  }

  @tailrec
  final def kMeans(points : GenSeq[Point], means : GenSeq[Point], eta : Double) : GenSeq[Point] ={
    val centres = update(classify(points, means))
    if (converged(eta)(means, centres))
      centres
    else
      kMeans(points, centres, eta)
  }
}

object TimeMeasurement {
  type Point = (Double, Double)
  def main(args: Array[String]): Unit = {

    println("Here you can write a number of points that you want: ")
    val points_number = scala.io.StdIn.readInt()
    println("Here you can write range that you want: ")
    val range = scala.io.StdIn.readInt()
    println("Here you can write a number of clusters that you want: ")
    val clusters = scala.io.StdIn.readInt()
    println("Here you can write a eta that you want: ")
    val eta = scala.io.StdIn.readDouble()

    val arr_points = KMeans.generatePoints(points_number, range)
    val first_clusters = KMeans.initializeMeans(clusters, arr_points)
    val timers = config(
      Key.exec.minWarmupRuns -> 10,
      Key.exec.maxWarmupRuns -> 50
    ) withWarmer new Warmer.Default

    def SeqPoints(points: GenSeq[Point], means: GenSeq[Point]): Quantity[Double] = {
      val time = timers measure {
        KMeans.kMeans(points, means, eta)
      }
      time
    }
    def ParSeqPoints(points: GenSeq[Point], means: GenSeq[Point]): Quantity[Double] = {
      val time = timers measure {
        KMeans.kMeans(points.par, means.par, eta)
      }
      time
    }
    print("Ohhhh! Seq time is ")
    println(SeqPoints(arr_points, first_clusters))
    print("Oh! ParSeq time is ")
    println(ParSeqPoints(arr_points, first_clusters))
  }
}