package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GoServer {
    private static final String USAGE
            = "usage: " + GoServer.class.getName() + " <port>";
    private int port;

    /**The list handlerThreads saves all connected clients.
     * The GameThreads map stores all running games.**/
    private List<ClientHandler> handlerThreads;
    private Map<GameHandler, Integer> gameThreads;

    /**
     * Starts a server application
     * @param args Expects a port
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println(USAGE);
            System.exit(0);
        }
        GoServer server = new GoServer(Integer.parseInt(args[0]));
        server.run();
    }

    private GoServer(int port) {
        this.port = port;
        this.handlerThreads = new ArrayList<ClientHandler>();
        this.gameThreads = new HashMap<GameHandler, Integer>();
        //try to use port. Return error and allow new port number to be entered if port already in use. Define own exceptions!!
    }

    private void run() throws IOException {
        ServerSocket serverSock = new ServerSocket(this.port);

        while (true) {
            Socket sockP1;
            Socket sockP2;
            Integer gameID;

            try {
                sockP1 = serverSock.accept();
                System.out.println("Client 1 is connected : " + sockP1);
                ClientHandler newClient1 = new ClientHandler(this, sockP1);
                this.addHandler(newClient1);
                newClient1.start();

                gameID = createGameID();
                GameHandler newGame = new GameHandler(this, newClient1, gameID);
                addGame(newGame, gameID);
                newGame.start();

                sockP2 = serverSock.accept();
                System.out.println("Client 2 is connected : " + sockP2);
                ClientHandler newClient2 = new ClientHandler(this, sockP2);
                this.addHandler(newClient2);
                newClient2.start();
                newGame.addPlayerTwo(newClient2);


            } catch (IOException ie) {
                System.out.println("Connection lost. " + ie.getMessage());
            }
        }
    }

    /**
     *
     */
    private synchronized Integer createGameID() {
        Random r = new Random();
        Integer gameID;
        do {
            gameID = 100 + r.nextInt(900);
        } while (gameThreads.containsValue(gameID));
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
        this.gameThreads.put(handler, gameID);
    }

    synchronized void removeGame(GameHandler handler) {
        this.gameThreads.remove(handler);
    }
}
