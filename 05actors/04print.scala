#!/bin/sh
exec scala "$0" "$@"
!#

println("\n\n================\nCounting down ...\n\n")

def print4lines(n:Int,max:Int)={
	val m=max-n
	var filling="="*50
	if(n % 2 == 0)filling=filling.replaceAll("=","_")
	println(s"\n$filling\n")
	println(s"${("\r" + " " * m)+n}")
	println(s"\n$filling")
}

def actualize(x:Int,max:Int):Unit={
	x match{
	case 0=> 
		print4lines(0,max)
	case n:Int=>
		print4lines(n,max)
		Thread.sleep(500)
		print("\u001b[6A\r\u001b[J")
		actualize(n-1,max)
	}
}
actualize(50,50)

