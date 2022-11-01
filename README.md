

# Reconcilio

In this repository we will benchmark N projects.
 - one will use shardcake 
 - the others will not (NodeJS, Akka, plain Scala)

To run a benchmark:

### NodeJS server
`node index.js`
`sbt 'AB/runMain MainApp'`

Should give you an output like:
```
Maximum Requests per Second: 498
Average Requests per Second: 381.42857142857144
Average Errors per Second: 1.75
Average latency: 381.42857142857144
```

TODO 
- Add metric per node
- Expose Prometheus compatible API for metrics
