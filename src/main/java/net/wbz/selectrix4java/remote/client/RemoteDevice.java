package net.wbz.selectrix4java.remote.client;

import net.wbz.selectrix4java.bus.BusDataDispatcher;
import net.wbz.selectrix4java.data.BusDataChannel;
import net.wbz.selectrix4java.device.AbstractDevice;
import net.wbz.selectrix4java.device.DeviceAccessException;
import net.wbz.selectrix4java.remote.server.RemoteBusDataChannel;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Daniel Tuerk
 */
public class RemoteDevice extends AbstractDevice {
    private final URI uri;
    private Session session;

    public RemoteDevice(URI uri) {
        this.uri = uri;
    }

    @Override
    protected BusDataChannel doConnect(BusDataDispatcher busDataDispatcher) throws DeviceAccessException {

        ClientManager client = ClientManager.createClient();
        try {
            session = client.connectToServer(TestClientEndPoint.class, uri);

        } catch (DeploymentException e) {
            throw new DeviceAccessException("can't connect", e);
        }
        return new RemoteBusDataChannel(null, null, busDataDispatcher);
    }

    @Override
    public void doDisconnect() throws DeviceAccessException {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Disconnect"));
        } catch (IOException e) {
            throw new DeviceAccessException("can't disconnect", e);
        }
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public static void main(String[] args) {
        try {
            new RemoteDevice(new URI("ws://localhost:8025/websockets/test")).connect();
        } catch (URISyntaxException | DeviceAccessException e) {
            e.printStackTrace();
        }
    }
}
