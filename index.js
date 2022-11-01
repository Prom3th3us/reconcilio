const http = require('http');

const hostname = '127.0.0.1';
const port = 3000;

var acc = 0
const server = http.createServer((req, res) => {
  acc = acc + 1
  if (acc % 500 == 0) {
      res.statusCode = 500;
      res.setHeader('Content-Type', 'text/plain');
      res.end('Server Error');
  } else {
      res.statusCode = 200;
      res.setHeader('Content-Type', 'text/plain');
      res.end('Hello World');
  }
});

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});
