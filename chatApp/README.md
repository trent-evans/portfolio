# Chat App

## What I did
As part of an school assignment, I built a basic functional chat app that would allow users to join a chat room and interact with the other people that had joined the room as well.  
<p>
The [front end](web_front_end) was built mostly using DOM Manipulation in Javascript.  I also built an extremely simple [Android app](android_app) that would also communicate with the server.  Included in this repo are a couple sreenshots of the [web interface](screenshots/web) as well as complimentary screenshots of the interaction between [the web client and Android app](screenshots/android)</p>

<p>
The back end was a little more interesting and varied.  I built multi-threaded back end servers in three different ways: 1) [manually in Java](my_server) 2) [using Node.js](node_server) 3) using [Spring](spring_server).  It was interesting to learn about and use each of these methods because of the differences inherent in each one as well as discovering how there are different methods for accomplishing the same thing, depending on the level of depth and detail that I want to get into.

## What I would change/add

One of the major things I would like to add at some point would be some kind of a backup of all the messages in a local database (via SQLite or something similar) that would allow me to store all messages sent and return them even after the server had been shut down for a time.
<p>
The other major change I would make is to handle people dropping out of the conversation.  Currently, if someone drops out of the conversation, their WebSocket connection is broken, causing the program to hang when a new message is sent (mostly this was due to my being relatively new at programming and not being sure how to handle it).  Now, I would go in and have a check to see if the WebSocket was still available.  If it was, then the messages could be sent without a problem.  Otherwise, I would have a message sent that says, "Person left the meeting" as well as removing that WebSocket from the list of connections for the ChatRoom.