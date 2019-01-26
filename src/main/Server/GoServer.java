package main.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GoServer {

    /**The list handlerThreads saves all connected clients.
     * The GameThreads map stores all running games.**/
    private List<ClientHandler> handlerThreads;
    private Map<GameHandler, Integer> games;
    private ServerSocket serverSock;

    /**Starts a server application.
     */
    public static void main(String[] args) throws IOException {
        GoServer server = new GoServer();
        server.run();
    }

    public GoServer() {
        this.handlerThreads = new ArrayList<ClientHandler>();
        this.games = new HashMap<GameHandler, Integer>();
        //try to use port. Return error and allow new port number to be entered if port already in use. Define own exceptions!!
    }

    /**
     * Listens for new clients trying to connect on its port.
     * Two clients are accepted to connect to one new GameHandler thread
     * and individual ClientHandler threads. These handle further communication.
     */
    private void run() {
        setPort();

        while (true) {
            Socket sockP1;
            Socket sockP2;
            int gameID;
        //TODO: If client1 disconnects before client2 enters, the next client will be client 2.
            // TODO: probably fixed if I add a waiting list for clients and call 'try start game' method
            // TODO: each time a new client arrives.
            try {
                sockP1 = serverSock.accept();
                printOnServer("Client 1 is connected : " + sockP1);

                gameID = createGameID();
                GameHandler newGame = new GameHandler(this, gameID);
                addGame(newGame, gameID);

                ClientHandler newClient1 = new ClientHandler(this, newGame, sockP1, true);
                this.addHandler(newClient1);
                newGame.addPlayer1(newClient1);
                newClient1.start();

                sockP2 = serverSock.accept();
                printOnServer("Client 2 is connected : " + sockP2);
                ClientHandler newClient2 = new ClientHandler(this, newGame, sockP2, false);
                this.addHandler(newClient2);
                newGame.addPlayer2(newClient2);
                newClient2.start();


            } catch (IOException ie) {
                printOnServer("A game connection has been lost. " + ie.getMessage());
            }
        }
    }

    /**Asks the user for the port to be used. Sets the server's port number if available.
     * Port number cannot be a system port.
     */
    //TODO setPort can get stuck in an infinite loop, but I'm not sure what caused it.
    private void setPort() {
        printOnServer("Please provide a port number above 1023: ");
        Scanner readPort = new Scanner(System.in);
        int input = 0;
        do {
            if (readPort.hasNextInt()) {
                input = readPort.nextInt();
            }
        } while (!startServerSocket(input));
        System.out.println("Port number accepted.");
        readPort.close();
    }

    /**Checks the availability of a given port number.
     * @param portNr Port number as specified by the user.
     * @return true if port is free, otherwise false.
     */
    private boolean startServerSocket(int portNr) {
        boolean result;
        if (portNr < 1024) {
            printOnServer("System port chosen. Please provide a number above 1023: ");
            result = false;
        } else {
            try {
                this.serverSock = new ServerSocket(portNr, 50, InetAddress.getByName("0.0.0.0"));
                result = true;
            } catch (IOException e) {
                printOnServer("Port already in use. Please provide a different number: ");
                result = false;
            }
        }
        return result;
    }

    /** Generates a unique ID number for a Go game between 100 and 999.
     */
    private synchronized int createGameID() {
        Random r = new Random();
        int gameID;
        do {
            gameID = 100 + r.nextInt(900);
        } while (games.containsValue(gameID));
        return gameID;
    }

    /**
     * Adds a ClientHandler to the list of ClientHandlers.
     * @param handler ClientHandler added to list
     */
    private synchronized void addHandler(ClientHandler handler) {
        if (!this.handlerThreads.contains(handler)) {
            this.handlerThreads.add(handler);
        }
    }

    /**
     * Removes a ClientHandler from the list of ClientHandlers.
     * @param handler ClientHandler removed from list
     */
    synchronized void removeHandler(ClientHandler handler) {
        this.handlerThreads.remove(handler);
    }

    private synchronized void addGame(GameHandler handler, int gameID) {
        this.games.put(handler, gameID);
    }

    synchronized void removeGame(GameHandler handler) {
        this.games.remove(handler);
    }

    synchronized void printOnServer(String message) {
        System.out.println(message);
    }
}
