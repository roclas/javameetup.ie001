#!/bin/sh
exec scala "$0" "$@"
!#

import java.io._
import scala.io._ 
import akka.actor.{Actor, Props, ActorSystem, ActorRef}
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.Duration
import java.net.{InetAddress, ServerSocket, Socket}
import akka.routing.RoundRobinPool
import akka.pattern.pipe
import scala.util.parsing.combinator._


class RequestActor(server: ActorRef) extends Actor with RegexParsers{
  override val skipWhitespace = false
  def messageCommand=(currencyValueAssignment | currency) <~ end 
  def currencyValueAssignment: Parser[(String,Float)] = currency ~ """\s+""".r ~floatValue ^^ {case c~b~v =>(c,v)}
  def currency: Parser[String] = """[A-Z]{3}""".r ^^ { _.toString }
  def floatValue: Parser[Float] = """([0-9]*[.])?[0-9]+""".r ^^ { _.toFloat}
  def end= """\s*$""".r ^^ { _.toString }
  override def receive: Receive = {
    case s: Socket =>
    	val out = new PrintStream(s.getOutputStream())
    	var exit=false
        val in = new BufferedSource(s.getInputStream()).getLines()
    	while (!exit) {
        	val read=in.next()
        	out.println(read)
		parse(messageCommand,read) match{
			case Success(matched,_)=>{
				matched match{
				  case c:String=>
					if(euroValues.getOrElse(c,0.toFloat)>0){
					  out.println(s"showing $c ..")
					  ServerActor.CurrencyCode=c
					  deleteTable
  					  printTable(ServerActor.CurrencyCode)
					}else out.println(s"sorry but the currency $c doesn't exist!")
				  case (c:String,v:Float)=>
					if(euroValues.getOrElse(c,0.toFloat)>0){
					 out.println(s"changing $c to $v ..")
					 euroValues(c)=v
					 deleteTable
  					 printTable(ServerActor.CurrencyCode)
					}else out.println(s"sorry but the currency $c doesn't exist!")
				  case _=> out.println( s"$matched UNIMPLEMENTED OPTION" )
				}
			}
			case Failure(msg,_) =>{
        		  if(read==ServerActor.Quit)exit=true
        		  else if(read==ServerActor.Stop){
				exit=true
				server!ServerActor.Stop
			  }
			  else out.println(s"I don't understand ur compex grammar; what does $read mean? ($msg)")
			}
			case Error(msg,_) => out.println("ERROR: " + msg)
		}
    	}
    	out.flush()
    	s.close()
  }
}

class ServerActor(address: String, port: Int, processorsAmount: Int, shutdown: Promise[Unit]) extends Actor {

  val requestProcessors = context.actorOf(
    RoundRobinPool(processorsAmount)
      .props(Props(new RequestActor(context.self))))

  val serverSocket: ServerSocket = new ServerSocket(port, 1, InetAddress.getByName(address))
  println(s"server listening on port ${serverSocket.getLocalPort}...")
  printTable(ServerActor.CurrencyCode)

  handleNextRequest

  override def receive: Receive = {
    case request: Socket =>
      requestProcessors forward request
      handleNextRequest
    case ServerActor.Stop => shutdown.success(())
  }

  def handleNextRequest = {
    import context.dispatcher
    Future(serverSocket.accept()) pipeTo self
  }

}

object ServerActor {
  val Stop = "stop"
  val Quit= "quit"
  var CurrencyCode= "GBP"
}



import scala.xml.XML
def get(url: String) = scala.io.Source.fromURL(url).mkString
val raw=(XML.loadFile("eurofxref-daily.xml") \\ "Cube").flatMap(x=>List(x.attribute("currency"),x.attribute("rate"))).flatten
val euroValuesOriginal=raw.grouped(2).toList.map{case (a::b)=>(a.text,b.head.text.toFloat)}.toMap
val euroValues=collection.mutable.Map(euroValuesOriginal.toSeq: _*)
val currencies=euroValues.map(_._1).toList
val currencyMatrix=currencies.combinations(2).toList.map(_.permutations.toList).flatten.groupBy(_.head).map{
        case(k,l)=>{
                (k,l.map(_.tail).flatten.map{x=>
                        (x,() => euroValues(k)/euroValues(x))
                }.toMap)
        }
}

def printTable(c:String)=print(currencyMatrix(c).map(x=>(s"${c}-${x._1}",x._2())).mkString("\n"))
def deleteTable=print("\u001b[29A\r\u001b[J")


val address = "127.0.0.1"
val port = 9999
val processorsAmount = 5
val system = ActorSystem("myActorsSystem")
val shutdown = Promise[Unit]()


val httpServerActor = system.actorOf(Props(
    new ServerActor(address, port, processorsAmount, shutdown)))


import scala.concurrent.ExecutionContext.Implicits.global
shutdown.future.map{_ =>
    system.shutdown()
    system.awaitTermination(Duration.Inf)
    System.exit(0)
}




