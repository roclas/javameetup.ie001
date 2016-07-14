#!/bin/sh
exec scala "$0" "$@" 
!#

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


def get(url: String) = {
	println(s"making http request for $url")
	scala.io.Source.fromURL(url).mkString
}


val future1=Future(get("http://localhost:8001/GBP"))
val future2=Future(get("http://localhost:8001/CAD"))
val future3 = future1.flatMap( x => future2.map( y => x+y ) )

println("waiting for the result...")

//future3.onSuccess{case x=>println(x)}
println(Await.result( future3, 20 seconds))
