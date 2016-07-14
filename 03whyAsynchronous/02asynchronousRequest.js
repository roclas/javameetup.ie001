#!/usr/bin/env node

var http = require('http');

callback = function(r) {
  var s= ''
  r.on('data',function(c){s+=c;});
  r.on('end',function(){console.log(s);});
}

for(c in ["EUR","GBP","AUD","CAD","SGD"]){
	var req = http.request({ host: 'localhost', path: '/GBP', port: '8001'}, callback);
	console.log("request sent for currency "+c);
	req.end();
}
