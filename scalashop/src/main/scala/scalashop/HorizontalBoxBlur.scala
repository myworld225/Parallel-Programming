package scalashop

import org.scalameter._
import common._

object HorizontalBoxBlurRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val radius = 3
    val width = 1920
    val height = 1080
    val src = new Img(width, height)
    val dst = new Img(width, height)
    val seqtime = standardConfig measure {
      HorizontalBoxBlur.blur(src, dst, 0, height, radius)
    }
    println(s"sequential blur time: $seqtime ms")

    val numTasks = 32
    val partime = standardConfig measure {
      HorizontalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $partime ms")
    println(s"speedup: ${seqtime / partime}")
  }
}


/** A simple, trivially parallelizable computation. */
object HorizontalBoxBlur {

  /** Blurs the rows of the source image `src` into the destination image `dst`,
   *  starting with `from` and ending with `end` (non-inclusive).
   *
   *  Within each row, `blur` traverses the pixels by going from left to right.
   */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit = {
  // TODO implement this method using the `boxBlurKernel` method
    var i = 0
    var j = from
    while(j < end){
      while(i < src.width){
        dst.update(i,j,boxBlurKernel(src,i,j,radius))
        i += 1
      }
      i = 0
      j += 1
    }


  }

  /** Blurs the rows of the source image in parallel using `numTasks` tasks.
   *
   *  Parallelization is done by stripping the source image `src` into
   *  `numTasks` separate strips, where each strip is composed of some number of
   *  rows.
   */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
  // TODO implement using the `task` construct and the `blur` method

    /**
      * Img.width를 numTasks로 나눈 몫으로 구간을 나누고 마지막원소를 구간의 마지막 값 즉 Img.width로 바꿔준다
      * 그 후 zip 연산으로 튜플 리스트 생성.
      */
    val q = src.height / numTasks + 1
    var slices = ((0 to src.height) by q).toList

    if (slices.last != src.height)
      slices = slices :+ src.height

    val tuples = slices zip slices.tail
    val tasks = tuples flatMap( x => List(task(blur(src,dst,x._1,x._2,radius))))

    tasks foreach(x => x.join())
  }


  /***
    * version 2
    */
//  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
//    val min = src.height / numTasks + 1
//    val sliding = min match {
//      case 1 =>
//        (0 until src.height).map(x => List(x, x + 1))
//      case _ =>
//        (0 to src.height).sliding(min, min - 1).toList
//    }
//    val tasks = sliding.map(y => task(blur(src, dst, y.head, y.last, radius)))
//    tasks.foreach(_.join)
//  }
}