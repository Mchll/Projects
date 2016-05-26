package GIFReader

import java.util.{MissingResourceException, IllegalFormatException}
import jdk.nashorn.internal.runtime.BitVector
import scodec._
import bits._
import scodec.codecs._
import java.nio.file.{Files, Paths}
import java.io._
import scodec.bits.ByteOrdering.LittleEndian
import scala.NoSuchElementException
import swing.{Panel, MainFrame, SimpleSwingApplication}
import java.awt.{Color, Graphics2D, Dimension}
import java.awt.image.BufferedImage

object Timer {
  def apply(interval: Int, repeats: Boolean = true)(op: => Unit) {
    val timeOut = new javax.swing.AbstractAction() {
      def actionPerformed(e : java.awt.event.ActionEvent) = op
    }
    val t = new javax.swing.Timer(interval, timeOut)
    t.setRepeats(repeats)
    t.start()
  }
}

class DataPanel(data: Array[Array[Color]]) extends Panel {
  override def paintComponent(g: Graphics2D) {
    val width = data.length
    val height = data.map(_.length).max
    val dx = g.getClipBounds.width.toFloat  / width
    val dy = g.getClipBounds.height.toFloat / height
    for {
      x <- 0 until data.length
      y <- 0 until data(x).length
      x1 = (x * dx).toInt
      y1 = (y * dy).toInt
      x2 = ((x + 1) * dx).toInt
      y2 = ((y + 1) * dy).toInt
    } {
      data(x)(y) match {
        case c: Color => g.setColor(c)
        case _ => g.setColor(Color.WHITE)
      }
      g.fillRect(x1, y1, x2 - x1, y2 - y1)
    }
  }
}

object Draw extends SimpleSwingApplication {

  case class animation (disposal_method: Int, UI_flag: Boolean, TC_flag: Boolean, delay: Int, transparent_color: Int)

  case class image (LP: Int, TP: Int, width: Int, height: Int, LCT_flag: Boolean, interlace_flag: Boolean,
                    sort_flag: Boolean, reserved_bits: Int, LCT_size: Int)

  case class gif_file (header: gif_header, global_color_table: Array[Int], image_descriptors: List[image],
                       images: List[Array[Int]], animations: List[animation])

  case class gif_header (width: Int, height: Int, GCT_flag: Boolean, color_resolution: Int, SF_flag: Boolean,
                         GCT_size: Int, background_color: Int, ratio: Int)

  def color(col: Int): Color = {
    new Color(col / 1000000, (col / 1000) % 1000, col % 1000)
  }

  def LZW(miniroot: Int, input: BitVector, CT: List[Int]): Array[Int] = {
    def list_of_arrays(lis: List[Int], arr: List[Array[Int]]): List[Array[Int]] = {
      if (lis.isEmpty) {
        arr.reverse
      }
      else {
        list_of_arrays(lis.tail, Array(lis.head) :: arr)
      }
    }
    
    def iteration(size: Int, prefix: Array[Int], in: BitVector, sheet: List[Array[Int]], out: Array[Int],
                  last_elem: Int): Array[Int] = {
      val element = in.take(size).reverseBitOrder.toInt(false, LittleEndian)
      if (element == last_elem) {
        out
      }
      else if (element < sheet.length) {
        val new_out = out ++ sheet(element)
        val new_sheet = sheet ::: List(prefix :+ sheet(element)(0))
        iteration(1 + (Math.log(new_sheet.length) / Math.log(2)).toInt, sheet(element), in.drop(size),
          new_sheet, new_out, last_elem)
      }
      else {
        val new_sheet = sheet ::: List(prefix :+ prefix(0))
        val new_out = out ++ (prefix :+ prefix(0))
        iteration(1 + (Math.log(new_sheet.length) / Math.log(2)).toInt, prefix :+ prefix(0), in.drop(size),
          new_sheet, new_out, last_elem)
      }
    }
    val first_elem = input.drop(miniroot).take(miniroot).reverseBitOrder.toInt(false, LittleEndian)
    val clear_elem = input.take(miniroot).reverseBitOrder.toInt(false, LittleEndian)
    iteration(miniroot, Array(CT(first_elem)), input.drop(2 * miniroot), list_of_arrays(CT, Nil),
      Array(CT(first_elem)), clear_elem + 1)
  }

  def decoder(path: String): gif_file = {
    val header_Codec = (uint16L :: uint16L :: bool :: uintL(3) :: bool :: uintL(3) :: uint8L :: uint8L).as[gif_header]
    val animation_Codec = (uintL(3) :: bool :: bool :: uint16L :: uint8L).as[animation]
    val image_Codec = (uint16L :: uint16L :: uint16L :: uint16L :: bool :: bool :: bool :: uint2L :: uintL(3)).as[image]

    def dropper(vector: BitVector): BitVector = {
      val size = vector.take(8).toInt(false, LittleEndian)
      val new_vector = vector.drop(8)
      if (size == 0) {
        new_vector
      }
      else {
        dropper(new_vector.drop(8 * size))
      }
    }

    def get_image(vec: BitVector, acc: BitVector): BitVector = {
      def reverse_bits(in_bl: BitVector, out_bl: BitVector): BitVector = {
        if (in_bl.isEmpty) {
          out_bl
        }
        else {
          reverse_bits(in_bl.drop(8), out_bl ++ in_bl.take(8).reverseBitOrder)
        }
      }
      val size = vec.take(8).toInt(false, LittleEndian)
      var vector = vec.drop(8)
      if (size == 0) {
        acc
      }
      else {
        val block = vector.take(size * 8)
        get_image(vector.drop(size * 8), acc ++ reverse_bits(block, BitVector(Nil)))
      }
    }

    def create_color_table(sheet: Array[Int], size: Int, vector: BitVector): Array[Int] = {
      if (size == 0) {
        sheet
      }
      else {
        val red = vector.take(8).toInt(false, LittleEndian)
        val green = vector.drop(8).take(8).toInt(false, LittleEndian)
        val blue = vector.drop(16).take(8).toInt(false, LittleEndian)
        val new_color = red * 1000000 + green * 1000 + blue
        create_color_table(sheet :+ new_color, size - 1, vector.drop(24))
      }
    }

    def descriptor_cases(vector: BitVector, giffile: gif_file): gif_file = {
      val extension = vector.take(8).toInt(false, LittleEndian)
      if (extension == 44) {
        val new_image_descriptor = image_Codec.decode(vector.drop(8)).require.value
        var new_LCT: Array[Int] = Array[Int]()
        var new_vector = vector.drop(8).drop(9 * 8)
        if (new_image_descriptor.LCT_flag) {
          new_LCT = create_color_table(new_LCT, Math.pow(2, new_image_descriptor.LCT_size + 1).toInt, vector)
          new_vector = new_vector.drop(Math.pow(2, new_image_descriptor.LCT_size + 1).toInt * 3 * 8)
        }
        val root = new_vector.take(8).toInt(false, LittleEndian)
        new_vector = new_vector.drop(8)
        var image_vector = get_image(new_vector, BitVector(Nil))
        var CT: Array[Int] = Array[Int]()
        if (new_image_descriptor.LCT_flag) {
          CT = new_LCT
        }
        else {
          CT = giffile.global_color_table
        }
        val new_image = LZW(root + 1, image_vector, CT.toList ::: List(1000000000, 2000000000))
        val new_gif = new gif_file(giffile.header, giffile.global_color_table, new_image_descriptor ::
          giffile.image_descriptors, new_image :: giffile.images,
          giffile.animations)
        new_vector = dropper(new_vector)
        descriptor_cases(new_vector, new_gif)
      }
      else if (extension == 59) {
        val gif_out = new gif_file(giffile.header, giffile.global_color_table, giffile.image_descriptors.reverse,
          giffile.images.reverse, giffile.animations.reverse)
        gif_out
      }
      else if (extension == 33) {
        val lbl = vector.drop(8).take(8).toInt(false, LittleEndian)
        if (lbl == 249) {
          val new_animation = animation_Codec.decode(vector.drop(27)).require.value
          val new_gif = new gif_file(giffile.header, giffile.global_color_table, giffile.image_descriptors,
            giffile.images, new_animation :: giffile.animations)
          descriptor_cases(vector.drop(16).drop(6 * 8), new_gif)
        }
        else if (lbl == 254) {
          val new_vector = dropper(vector.drop(16))
          descriptor_cases(new_vector, giffile)
        }
        else if (lbl == 1) {
          val new_vector = dropper(vector.drop(16).drop(13 * 8))
          descriptor_cases(new_vector, giffile)
        }
        else if (lbl == 255) {
          val new_vector = dropper(vector.drop(16))
          descriptor_cases(new_vector, giffile)
        }
        else {
          throw new NoSuchElementException()
        }
      }
      else {
        throw new NoSuchElementException()
      }
    }
    
    val byte_Array = Files.readAllBytes(Paths.get(path))
    var bit_Vector = (BitVector(byte_Array)).drop(48)
    val decoded_header = header_Codec.decode(bit_Vector).require.value
    bit_Vector = bit_Vector.drop(7 * 8)
    var GCT: Array[Int] = Array[Int](0)
    if (decoded_header.GCT_flag) {
      GCT = create_color_table(Array[Int](), Math.pow(2, decoded_header.GCT_size + 1).toInt, bit_Vector)
      bit_Vector = bit_Vector.drop(Math.pow(2, decoded_header.GCT_size + 1).toInt * 3 * 8)
    }
    val file = new gif_file(decoded_header, GCT, Nil, Nil, Nil)
    descriptor_cases(bit_Vector, file)
  }

  def get_frames(im: List[Array[Int]], dsc: List[image], sheet: List[Array[Array[Color]]]): List[Array[Array[Color]]] = {

    def frame(back: Array[Array[Color]], ims: Array[Int], wdth: Int, hght: Int, desc: image): Array[Array[Color]] = {
      val new_frame = back
      var i = 0
      for {
        x <- 0 until wdth
        y <- 0 until hght
      } {
        if (x >= desc.LP && y>= desc.TP) {
          new_frame(x)(y) = color(ims(i))
          i = i + 1
        }
      }
      new_frame
    }

    if(im.isEmpty) {
      sheet
    }
    else {
      get_frames(im.tail, dsc.tail, sheet :+ frame(Array.fill(gif.header.width,
        gif.header.height)(color(gif.global_color_table(gif.header.background_color))), im.head,
        gif.header.width, gif.header.height, dsc.head))
    }
  }

  val gif: gif_file = decoder("mygif.gif")
  val scale = 30
  val frames = get_frames(gif.images, gif.image_descriptors, Nil).toArray
  val module = frames.length
  val back = Array.fill(gif.header.width, gif.header.height)(color(gif.global_color_table(gif.header.background_color)))
  var number = 0

  def top = new MainFrame {

    def del() = {
      var disp = gif.animations(number % module).disposal_method
      if (disp == 2) {
        contents = new DataPanel(back) {
          preferredSize = new Dimension(gif.header.width * scale, gif.header.height * scale)
        }
      }
      else if (disp == 3) {
        contents = new DataPanel(frames((number - 1) % module)) {
          preferredSize = new Dimension(gif.header.width * scale, gif.header.height * scale)
        }
      }
      number = number + 1
      contents = new DataPanel(frames((number - 1) % module)) {
        preferredSize = new Dimension(gif.header.width * scale, gif.header.height * scale)
      }
    }

    Timer(gif.animations(number % module).delay) {del()}
  }
  
}
