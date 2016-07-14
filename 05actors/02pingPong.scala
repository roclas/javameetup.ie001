#!/bin/sh
exec scala -deprecation "$0" "$@"
!#

import akka.actor.{Actor, Props, ActorSystem, ActorRef}

class PingActor() extends Actor {
  override def receive: Receive = {
    case "pong"=> 
	println (s"$self> received pong, sending ping")
	Thread sleep 500
	pong ! "ping"
    case _ => println(self+">???")
  }
}

class PongActor() extends PingActor {
    override def receive:Receive = masterReceive orElse super.receive
    def masterReceive: Receive = {
	case "ping"=> 
		println (s"$self> received ping, sending pong")
		Thread sleep 3000
		system.actorSelection("/user/pong") ! "pong"
		//system.actorSelection("akka://myActorsSystem/user/pong") ! "pong"
    }
}

val system= ActorSystem("myActorsSystem")
val ping= system.actorOf(Props(new PingActor()),name="ping")
val pong= system.actorOf(Props(new PongActor()),name="pong")

ping ! "hello"
pong ! "hola"
pong ! "ping"

