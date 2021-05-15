function webSocketInvoke() {
    if ("WebSocket" in window) {
        console.log("WebSocket is supported by your Browser!");
        const ws = new WebSocket("ws://localhost:1234/","echo-protocol");

            ws.onopen = function() {
                console.log("Connection created");
                const file = Math.random() < 0.5 ? 'filename.mp3' : 'filename.pdf';
                console.log("Need " + file + " progress info");
                ws.send("Track " + file);
            };

        ws.onmessage = function (evt) {
            const received_msg = evt.data;
            console.log(received_msg );
            ws.send("Successfully received " + received_msg);
        };

        ws.onclose = function() {
            console.log("Connection closed");
        };
    } else {
        alert("WebSocket NOT supported by your Browser!");
    }
}

webSocketInvoke();