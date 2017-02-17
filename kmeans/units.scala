import org.scalatest.FunSuite

class unit_tests extends FunSuite {

  type Point = (Double, Double)

  test("findClosest") {
    assert(KMeans.findClosest((0,0), Seq[Point]((1,0), (2,0), (3,0))) == (1,0))
  }
  test("classify") {
    val my_points = Seq[Point]((1,0), (2,1), (3,2), (4,2))
    val my_means = Seq[Point]((1,1), (3.5,2))
    assert(KMeans.classify(my_points, my_means) == Seq(((1,1), Seq((1,0), (2,1))), ((3.5,2), Seq((3,2), (4,2)))))
  }
  test("findAverage") {
    val my_points = Seq[Point]((5,5), (5,0), (-1,1))
    val my_means = (6,8)
    assert(KMeans.findAverage(my_means, my_points) == (3,2))
  }
}
