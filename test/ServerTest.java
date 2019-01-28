import main.Client.GoClient;
import main.Client.GoController;
import main.Server.GoServer;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest {
    private GoServer server;
    private GoController client1;
    private GoController client2;
    private GoController client3;
    private GoController client4;

    public void setUp(){
        server = new GoServer();
        try {
            InetAddress host = InetAddress.getLocalHost();
            client1 = new GoController(host, 7171);
            client2 = new GoController(host, 7171);
            client3 = new GoController(host, 7171);
            client4 = new GoController(host, 7171);
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
