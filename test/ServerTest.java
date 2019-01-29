import main.Client.GoController;
import main.Server.GoServer;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
        try {
            InetAddress host = InetAddress.getLocalHost();
            client1 = new GoController();
            client2 = new GoController();
            client3 = new GoController();
            client4 = new GoController();
        } catch (UnknownHostException e) {
            e.printStackTrace();
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
