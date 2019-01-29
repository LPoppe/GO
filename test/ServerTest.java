import main.Client.GoController;
import main.Server.GoServer;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**Intended to be used to test the communication system between server and clients.*/
public class ServerTest {
    private GoServer server;
    private GoController client1;
    private GoController client2;
    private GoController client3;
    private GoController client4;

    @Before
    public void setUp(){
        server = new GoServer();
        client1 = new GoController();
        client2 = new GoController();
        client3 = new GoController();
        client4 = new GoController();


        String data = "\n\n\n\n\nA";
        InputStream stdin = System.in;
        try {
            System.setIn(new ByteArrayInputStream(data.getBytes()));
            Scanner scanner = new Scanner(System.in);
            System.out.println(scanner.nextLine());
        } finally {
            System.setIn(stdin);
        }
    }

    @Test
    public void serverTest() {

    }

    @Test
    public void testClientOneConnection() {

    }

    @Test
    public void testClientTwoConnection() {

    }

    @Test
    public void testConfig() {

    }

    @Test
    public void testBroadcast() {
        //handshake
    }

    @Test
    public void testExit() {

    }

    @Test
    public void testMultipleGames() {

    }
}
