#!/bin/sh
exec scala "$0" "$@"
!#
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


def get(url: String) = scala.io.Source.fromURL(url).mkString
val listOfFutures=List("EUR","GBP","AUD","CAD","SGD").map(c=>Future(get("http://localhost:8001/"+c)))
listOfFutures.par.map{ a=> { println(Await.result(a, Int.MaxValue seconds)) } }
//listOfFutures.map{ a=> { println(Await.result(a, Int.MaxValue seconds)) } }
//println(listOfFutures.par.map( a=> { Await.result(a, Int.MaxValue seconds) } ) )
//if we put the result in a future, we can continue doing things
