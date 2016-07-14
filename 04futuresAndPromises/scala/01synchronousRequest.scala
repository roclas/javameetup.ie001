#!/bin/sh
exec scala "$0" "$@"
!#

List("EUR","GBP","AUD","CAD","SGD").foreach{ c=>
	println("Hello! " + scala.io.Source.fromURL("http://localhost:8001/"+c).mkString )
}
