#!/bin/sh
exec scala "$0" "$@"
!#
import scala.concurrent._
import ExecutionContext.Implicits.global

val listOfFutures=List("EUR","GBP","AUD","CAD","SGD").
	map(c=>Future( scala.io.Source.fromURL("http://localhost:8001/"+c).mkString))

listOfFutures.foreach{c=> println(s"the future $c is working in the shadow" ) }

listOfFutures.map(x=>x.onComplete(r=>println(r)))

//####Futures work in the background and the execution of our program
//####will finish if we don't wait for them
println("waiting for 7 seconds");Thread sleep 7000
