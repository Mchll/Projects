package Blur

import java.awt.image.BufferedImage

import org.scalatest.FunSuite

class Unit_Tests extends FunSuite{
  test("Merger") {
    val red = 0x11
    val green = 0x22
    val blue = 0x33
    val alpha = 0x44
    val byte = 0x11223344
    assert(Merger(red, green, blue, alpha) == byte)
  }
  test("RGBA") {
    val red = 0x11
    val green = 0x22
    val blue = 0x33
    val alpha = 0x44
    val byte = 0x11223344
    assert(Red(byte) == red)
    assert(Green(byte) == green)
    assert(Blue(byte) == blue)
    assert(Alpha(byte) == alpha)
  }
  test("ByteBlur") {
    val image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0,0,0xffff0000)
    image.setRGB(0,1,0xffff0000)
    image.setRGB(1,0,0xffff0000)
    image.setRGB(1,1,0xffff0000)
    assert(ByteBlur(1, 1, 1, 1, 1, image) == 0xffff0000)
  }
}

