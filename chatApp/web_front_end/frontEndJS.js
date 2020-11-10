window.onload = function(){

    let chatSocket = new WebSocket("ws://"+location.host);

    let chathead = document.createElement("h1");
    chathead.innerHTML = "MSD Online";
    document.body.appendChild(chathead);
    let userHead = document.createElement("h3");
    userHead.innerHTML = "Enter your username";
    document.body.appendChild(userHead);
    let usernameField = document.createElement("input");
    usernameField.placeholder = "Username";
    document.body.appendChild(usernameField);
    let roomHead = document.createElement("h3");
    roomHead.innerHTML = "Enter your chatroom";
    document.body.appendChild(roomHead);
    let roomNameField = document.createElement("input");
    roomNameField.placeholder = "Chat room name";
    document.body.appendChild(roomNameField);
    let breakLine = document.createElement("br");
    document.body.appendChild(breakLine);
    let enterButton = document.createElement("button");
    enterButton.innerHTML = "Begin Chatting";
    document.body.appendChild(enterButton);

    enterButton.onclick = function(){
        
        
        let username = usernameField.value;
        let roomName = roomNameField.value;
        console.log(username);
        console.log(roomName);
        
        
        document.body.removeChild(userHead);
        document.body.removeChild(usernameField);
        document.body.removeChild(roomHead);
        document.body.removeChild(roomNameField);
        document.body.removeChild(enterButton);

        // Header for username and chatroom information
        let roomHeader = document.createElement("h2");
        roomHeader.innerHTML = "Hi " + username + "! Chatroom: " + roomName;
        document.body.appendChild(roomHeader);

        // Set up my message
        let messageBoard = document.createElement("div");
        messageBoard.id = "messageBoardDiv";
        document.body.appendChild(messageBoard);

        let inputDiv = document.createElement("div");
        inputDiv.id = "inputMessageDiv";
        let messageInput = document.createElement("input");
        messageInput.placeholder = "Type a message...";
        messageInput.id = "messageInputBox";
        inputDiv.appendChild(messageInput);
        let messageSendButton = document.createElement("button");
        messageSendButton.innerHTML = "SEND";
        inputDiv.appendChild(messageSendButton);
        document.body.appendChild(inputDiv);

        if(chatSocket.readyState){
            chatSocket.send("join " + roomName + " " + username);
            console.log("socket ready");

            messageSendButton.onclick = function(){
                let message = messageInput.value;
                let socketSend = username + " " + message;
                chatSocket.send(socketSend);
                messageInput.value = "";
                console.log("Message sent");
            }
            
            chatSocket.onmessage = function(event){
                console.log("Message recieved");
                let messageParse = JSON.parse(event.data);
                let newMessage = document.createElement("div");
                if(messageParse.message == " has joined the chat"){
                    newMessage.classList.add("newUserAdded");
                }else if(messageParse.user == username){
                    newMessage.classList.add("newMessage");
                }else{
                    newMessage.classList.add("otherUserMessage");
                }
                let name = document.createTextNode(messageParse.user);
                let words = document.createTextNode(messageParse.message);
                let lineBreak = document.createElement("br");
                newMessage.appendChild(name);
                if(messageParse.message != " has joined the chat") {
                    newMessage.appendChild(lineBreak);
                }
                newMessage.appendChild(words);
                messageBoard.appendChild(newMessage);
            }            
        }
    }
}