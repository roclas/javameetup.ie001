#!/bin/sh
exec scala -deprecation "$0" "$@"
!#

import java.io._
import scala.io._ 
import akka.actor.{Actor, Props, ActorSystem, ActorRef}
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.Duration
import java.net.{InetAddress, ServerSocket, Socket}
import akka.routing.RoundRobinPool
import akka.pattern.pipe
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Stop
import akka.actor.SupervisorStrategy.Restart

class ServerActor(address: String, port: Int, processorsAmount: Int, shutdown: Promise[Unit]) extends Actor {

  val requestProcessors = context.actorOf(
    RoundRobinPool(processorsAmount)
      .props(Props(new RequestActor(context.self))))

  val serverSocket: ServerSocket = new ServerSocket(port, 1, InetAddress.getByName(address))
  println(s"server listening on port ${serverSocket.getLocalPort}...")

  handleNextRequest

  override def receive: Receive = {
    case request: Socket =>
      println (s"connecting to ${request}")
      requestProcessors forward request
      handleNextRequest
    case ServerActor.Stop =>
      println("stopping ServerActor")
      shutdown.success(())
  }

  override def unhandled(message: Any): Unit = {
    println(s"${sender.path} is requesting something weird to ServerActor")
    super.unhandled(message)
  }

  // Get request and send it to myself
  def handleNextRequest = {
    //In order to execute callbacks and operations, Futures need something called an ExecutionContext
    //which is very similar to a java.util.concurrent.Executor
    import context.dispatcher
    Future(serverSocket.accept()) pipeTo self
  }

  /*
  override val supervisorStrategy = OneForOneStrategy() {
    case throwable: Throwable  => {
      println("ServerActor - supervision strategy - exception in child actor")
      Restart
    }
  }
  */


}

class RequestActor(server: ActorRef) extends Actor {
  override def receive: Receive = {
    case s: Socket =>
    	val out = new PrintStream(s.getOutputStream())
    	var exit=false
        val in = new BufferedSource(s.getInputStream()).getLines()
    	while (!exit) {
        	val read=in.next()
        	out.println(read)
		read match{
		  case ServerActor.Stop=>server!ServerActor.Stop;exit=true
		  case ServerActor.Quit=> exit=true
		  case _=> 
		}
    	}
    	out.flush()
    	s.close()
  }
}



object ServerActor {
  val Stop = "stop"
  val Quit= "quit"
}



val address = "127.0.0.1"
val port = 9999
val processorsAmount = 5

val system = ActorSystem("myActorsSystem")
val shutdown = Promise[Unit]()

val myServerActor = system.actorOf(Props(
    new ServerActor(address, port, processorsAmount, shutdown)))

import scala.concurrent.ExecutionContext.Implicits.global
shutdown.future.map{_ =>
    system.shutdown()
    system.awaitTermination(Duration.Inf)
    System.exit(0)
}



