package de.hetzge.sgame.spritegen

object Test extends App {
  
  def x = Math.random()
  
  def v() = Math.random()
  
  println(v)
  println(v)
  
  println(x)
  println(x)
  
  class A{
    object X{
      var a = 0
    }
  }
  
  val a = new A
  
  a.X.a = 10
  
  val b = new A
  
  println(b.X.a)
  println(a.X.a)
  
  
}