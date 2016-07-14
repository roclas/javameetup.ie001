#!/bin/sh
exec scala "$0" "$@"
!#

var a=2
var b=3

var c=a+b

println(s"a is ${a} , b is ${b}, and c(a+b) is ${c} \n")
Thread.sleep(3000)
a+=1
println(s"...lets suppose we change the value of a to ${a}...\n")
Thread.sleep(3000)
println(s"now a is ${a} , b is ${b}, and c(a+b) is ${c} \n")
