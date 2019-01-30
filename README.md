# GO
The GO game consists of a server and two clients. The module provides two classes:
- GoServer: A server that hosts GO games for two people.
- GoController: A client that allows a user to connect and play GO on this, or another server using the same protocol.

To set up playing the GO, please use the GO-1.0-SNAPSHOT.jar. Go to the directory it is in, and start the server from the terminal using: 
- java -cp GO-1.0-SNAPSHOT.jar Server.GoServer
  
Likewise, a client can be started using:
- java -cp GO-1.0-SNAPSHOT.jar Client.GoController

You can also clone the repository, and run the two runnable classes provided in the GO module.

To set up the server, run the GoServer class. Follow the instructions in the console. To set up a client, first ensure the server port number has been selected. Run the GoController class, and select the server. If you are the first to connect, it will ask for input about the settings for the game you want to play. 


