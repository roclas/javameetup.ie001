#!/bin/sh
exec scala "$0" "$@" 
!#

import scala.concurrent.{ ExecutionContext, ExecutionContext$, Future, Promise, Await }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps


val p = Promise[Int]()
println("We created a promise (a promise that your order will be ready in the FUTURE)") 

p.future onSuccess {
  case x => println(s"yeah!!! we received a $x, this means that, your food is ready")
}

//p.success(1)
p completeWith Future { Thread sleep 2000; 1 } //food being cooked

println("waiting...")

Await.result(p.future,2 seconds)
