#!/bin/sh
exec scala "$0" "$@"
!#
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


def get(url: String) = scala.io.Source.fromURL(url).mkString
val listOfFutures=List("EUR","GBP","AUD","CAD","SGD").map(c=>Future(get("http://localhost:8001/"+c)))
val futureOfList=Future.sequence(listOfFutures)
val result=Await.result( futureOfList, 8 seconds) //this is a syncronous/blocking operation
println(result)
