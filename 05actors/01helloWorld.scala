#!/bin/sh
exec scala -deprecation "$0" "$@"
!#

import akka.pattern.ask
import akka.util.Timeout
import akka.actor.{Actor, Props, ActorSystem, ActorRef,PoisonPill}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
implicit val timeout = Timeout(10 seconds)

class HelloActor() extends Actor {
  override def receive: Receive = {
    case "hello"=> println (s"< answer to hello: how are you?")
    case n:Int=> Thread sleep n*1000;println (s"\n\n< answer to $n:\n"+s"  hello\n"*n)
    case x => println(s"< answer to $x: ???")
  }
}

val system= ActorSystem("myActorsSystem")
val myactor= system.actorOf(Props(new HelloActor()))

myactor ! "hello"; println("> sent hello")
myactor ! "hola"; println("> sent hola")
myactor ! 5; println("> sent 5")
myactor ! 4; println("> sent 4")//this will be enqueued in the mailbox
val ended= myactor ? 7; println("> sent 7") //this will be enqueued in the mailbox and will return a future
//myactor ? PoisonPill //this would kill the actor

ended onComplete{case result=>
	println("finishing")
	system.shutdown()
	system.awaitTermination(Duration.Inf)
	System.exit(0)
}
