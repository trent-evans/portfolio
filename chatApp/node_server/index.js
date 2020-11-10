var http = require('http');
var fs = require('fs');
var mime = require('mime-type/with-db');
var path = require('path');
var webSocketServer = require('websocket').server;
let chatRoomStorage = {};

console.log("Running");

let server = http.createServer( function(request,response) {

    let filepath = '.' + request.url;
    if(filepath == './'){
        filepath = './clientFrontEnd.html';
    }
    var extentsionName = String(path.extname(filepath)).toLowerCase();
    var contentType = mime.lookup(extentsionName);

    fs.readFile(filepath, function(error, content){
        if(error){
            fs.readFile('./error404.html', function(content){
                response.writeHead(404,{"Content-Type": contentType});
                response.end(content,'utf-8');
            });
        }else{
            response.writeHead(200,{"Content-Type": contentType});
            response.end(content,'utf-8');
        }
    });
}).listen(8080,function(){
    console.log("Listening on port 8080");
});

wsServer = new webSocketServer({
    httpServer: server,
    autoAcceptConnection: false
});

wsServer.on('request',function(request){
    var connection = request.accept(null,request.origin);
    console.log("Connection accepted");
    let roomname;
    connection.on('message',function(message){
        let splitMessage = message.utf8Data.split(" "); // Pull in the message and parse it
        if(splitMessage[0] == "join"){
            roomname = splitMessage[1];
            let username = splitMessage[2];
            if(roomname in chatRoomStorage){
                let room = chatRoomStorage[roomname];
                room.users.push(connection);
                console.log(username + " joined " + roomname);
                // Send message history to the new person
                for(let x = 0; x < room.messages.length; x++){
                    connection.send(room.messages[x]);
                }
                // Send new join message to everyone
                let jsonJoin = JSON.stringify({user:username,message:" has joined the chat"});
                room.messages.push(jsonJoin); // Add join message to history
                for(let c = 0; c < room.users.length; c++){
                    room.users[c].send(jsonJoin);
                }
            }else{
                let chatRoom = {users:[],messages:[]};
                chatRoom.users.push(connection);
                chatRoomStorage[roomname] = chatRoom;
                console.log(roomname + " created");
                console.log(username + " joined " + roomname);
                let jsonJoin = JSON.stringify({user:username,message:" has joined the chat"});
                chatRoom.messages.push(jsonJoin);
                connection.send(jsonJoin);
            }
        }else{
            let room = chatRoomStorage[roomname];
            let userWhoSentMessage = splitMessage[0];
            let rebuildMessage = "";
            for(let x = 1; x < splitMessage.length; x++){
                rebuildMessage += (" " + splitMessage[x]);
            }
            let jsonString = JSON.stringify({user:userWhoSentMessage,message:rebuildMessage});
            room.messages.push(jsonString);
            for(let x = 0; x < room.users.length; x++){
                room.users[x].send(room.messages[room.messages.length-1]);
            }
        }
    });
});


