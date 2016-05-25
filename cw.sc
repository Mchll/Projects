abstract class paragraph_element {
  def length: Int
}

case class Word(word: String) extends paragraph_element {
  override def toString: String =
    "Word(" + this.word + ")"
  def length = word.length
}

case class Space(width: Int) extends paragraph_element {
  override def toString: String =
    "Space(" + this.width.toString + ")"
  def length = width
}

abstract class Alignment

case class Left() extends Alignment
case class Right() extends Alignment
case class Middle() extends Alignment
case class Fill() extends Alignment

object control_work {

  type Line = List[paragraph_element]
  type Paragraph = List[Line]

  def error(lis: List[String], width: Int): Boolean = {
    if(lis.isEmpty) {
      false
    }
    else if(lis.head.length > width) {
      true
    }
    else {
      error(lis.tail, width)
    }
  }

  def len(lis: List[paragraph_element], ans: Int): Int = {
    if(lis.isEmpty) {
      ans
    }
    else {
      len(lis.tail, ans + lis.head.length)
    }
  }



  def alignment(lis: Paragraph, width: Int, al: Alignment, ans: Paragraph): Paragraph = {
    def al_str(lis: Line, width: Int, al_type: Alignment): Line = {
      if (width != 0)
        al_type match {
          case Fill() =>
            if (lis.tail.isEmpty) {
              lis.head :: new Space(width) :: Nil
            } else {
              lis.head :: new Space(width) :: lis.tail.tail
            }
          case Middle() =>
            val border = width / 2
            if (border != 0) new Space(width - border) :: lis ::: List(new Space(border))
            else new Space(width - border) :: lis
          case Left() =>
            lis :+ new Space(width)
          case Right() =>
            new Space(width) :: lis
        }
      else {
        lis
      }
    }
    if (lis.isEmpty)
      ans
    else {
      val nstr = al_str(lis.head, width - len(lis.head, 0), al)
      alignment(lis.tail, width, al, nstr :: ans)
    }
  }

  def transform(text: List[String], width: Int): Paragraph = {
    def Spaces(words: List[String], count: Int): Line = {
      if (count == 1)
        new Word(words.head) :: Space(1) :: Nil
      else
        new Word(words.head) :: Nil
    }

    if(error(text, width)) {
      val length = text.map(_.length).max
      transform(text, length)
    }

    def make_str(lis: List[String], f_width: Int, s_width: Int,  q: Int, str: Line, hlp: Paragraph): Paragraph = {
      if (lis.isEmpty)
        str.reverse :: hlp
      else if (lis.head.length <= s_width)
        make_str(lis.tail, width, s_width - lis.head.length - 1, 1, str.:::(Spaces(lis.head :: Nil, q)), hlp)
      else make_str(lis, width, width, 0, Nil, str.reverse :: hlp)
    }
    make_str(text, width, width, 0, Nil, Nil)
  }



/*  val ex = transform(List("qqq", "www", "eee"), 11)
  println(alignment(ex, 11, Right(), Nil))*/

  /*def main(lis: List[String]) : Unit = {
    val text = lis
    val required_width = 12
    val ans = transform(List("qqqqqq", "ww", "eeeee", "r"), required_width)
    println(alignment(ans, required_width, Middle(), Nil))
  }*/
}
