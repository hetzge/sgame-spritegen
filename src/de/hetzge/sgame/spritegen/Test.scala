package de.hetzge.sgame.spritegen

import javafx.collections.FXCollections
import scala.collection.JavaConversions._
import java.util.function.Predicate
import scala.collection.mutable.ArrayBuffer
import java.util.Arrays
import java.util.ArrayList

object Test extends App {

  def x = Math.random()

  def v() = Math.random()

  println(v)
  println(v)

  println(x)
  println(x)

  class A {
    object X {
      var a = 0
    }
  }

  val a = new A

  a.X.a = 10

  val b = new A

  println(b.X.a)
  println(a.X.a)

  val list = new ArrayList[String]()
  list.add("a")
  list.add("b")

  val collection = FXCollections.observableList(list)
  val filtered = collection.filtered(new Predicate[String]() {
    override def test(value: String): Boolean = {
      return value.startsWith("a")
    }
  });

  for (element <- filtered) {
    println(element)
  }

  println("----")

  filtered.setPredicate(new Predicate[String]() {
    override def test(value: String): Boolean = {
      return value.startsWith("b")
    }
  })

  for (element <- filtered) {
    println(element)
  }

  println("----")
  collection.add("ab")

  for (element <- filtered) {
    println(element)
  }
}