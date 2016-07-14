var port=8001;
var defaultValue=1.2;
var http = require('http')
, url  = require('url')
, fs   = require('fs');

http.createServer(function (request, response) {
	var currency="";
	try{currency=request.url.split("/")[1];}catch(e){};
	if(!currency)currency="EUR";
	var time2wait=Math.random()*7000;
	//var time2wait=3000;
	setTimeout(function() {
		var currentValue=(Math.random()/5.0)-0.1+defaultValue;
		response.writeHead(200, {'Content-Type': 'text/plain'});
		response.end("the "+currency+" costs "+currentValue+" USD ("+time2wait+"ms)\n");
        }, time2wait);
}).listen(port);

console.log('Server running at http://127.0.0.1:'+port+'/');
