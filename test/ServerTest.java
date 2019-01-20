import main.Client.GoClient;
import main.Server.GoServer;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest {
    private GoServer server;
    private GoClient client1;
    private GoClient client2;
    private GoClient client3;
    private GoClient client4;

    public void setUp(){
        server = new GoServer();
        try {
            InetAddress host = InetAddress.getLocalHost();
            client1 = new GoClient("Victor", host, 7171);
            client2 = new GoClient("Anna", host, 7171);
            client3 = new GoClient("Nelli", host, 7171);
            client4 = new GoClient("Jasper", host, 7171);
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
