const kafka = require('kafka-node'),
    Consumer = kafka.Consumer,
    client = new kafka.KafkaClient({kafkaHost: 'localhost:9092'}),
    consumer = new Consumer(client, [ { topic: 'allFiles', partition: 0 } ], { autoCommit: false });

const WebSocketServer = require('websocket').server;
const http = require('http');


const server = http.createServer(function(request, response) {
    console.log(' Request recieved : ' + request.url);
    response.writeHead(404);
    response.end();
});

server.listen(1234, function() {
    console.log('Listening on port : 1234');
});

const webSocketServer = new WebSocketServer({
    httpServer: server,
    autoAcceptConnections: false
});

function iSOriginAllowed(origin) {
    return true;
}

webSocketServer.on('request', function(request) {
    if (!iSOriginAllowed(request.origin)) {
        request.reject();
        console.log(' Connection from : ' + request.origin + ' rejected.');
        return;
    }

    const trackedFiles = [];

    const connection = request.accept('echo-protocol', request.origin);
    console.log(' Connection accepted : ' + request.origin);
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            console.log('Received Message: ' + message.utf8Data);
        }
        const [request, filename] = message.utf8Data.split(" ");
        if (request === "Track") {
            trackedFiles.push(filename)
        }
    });
    consumer.on('message', function (message) {
        console.log(message);
        const [filename, progress] = message.value.split(":")
        if (trackedFiles.includes(filename)) {
            connection.sendUTF(message.value);
            if (progress === "indexed") {
                trackedFiles.splice(trackedFiles.indexOf(filename), 1);
            }
        }
    });
    connection.on('close', function(reasonCode, description) {
        console.log('Connection ' + connection.remoteAddress + ' disconnected.');
    });
});