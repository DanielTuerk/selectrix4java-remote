package net.wbz.selectrix4java.remote;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.wbz.selectrix4java.bus.BusDataReceiver;
import net.wbz.selectrix4java.data.BusData;
import net.wbz.selectrix4java.data.BusDataChannel;

import javax.websocket.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * @author Daniel Tuerk
 */
@ClientEndpoint
public class RemoteBusDataChannel extends BusDataChannel {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Session session;
    private BusDataMessageHandler busDataMessageHandler;

    /**
     * Create an new channel for the given IO streams of the connected device.
     * Default {@link BusDataReceiver} must be set.
     * Additional receivers can be added at runtime. {@see #addBusDataReceiver}
     *
     * @param inputStream  opened {@link InputStream}
     * @param outputStream opened {@link OutputStream}
     * @param receiver     {@link BusDataReceiver} to receive the values of the read operations
     */
    public RemoteBusDataChannel(InputStream inputStream, OutputStream outputStream, BusDataReceiver receiver) {
        super(inputStream, outputStream, receiver);
    }

    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected ... " + session.getId());
        this.session = session;

//            this.session.getBasicRemote().sendText("start");
//            sender();
    }

//    @OnMessage
//    public void onMessage(String message, Session session) {
//        logger.info("Received from " + session.getId() + ": " + message);
//    }

    @Override
    public void start() {
        // websocket open
        // on message
        busDataMessageHandler = new BusDataMessageHandler();
        resume();
    }

    @Override
    public void pause() {
        session.removeMessageHandler(busDataMessageHandler);
    }

    @Override
    public void resume() {
        session.addMessageHandler(busDataMessageHandler);
    }

    @Override
    public void send(BusData busData) {
        // write message
        try {
            session.getBasicRemote().sendObject(busData);
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {
        // write message
        try {
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class BusDataMessageHandler implements MessageHandler.Whole<TransferData> {

        private final ExecutorService executorService;

        public BusDataMessageHandler() {
            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("ReadBlockTask-%d").build();
            executorService = Executors.newCachedThreadPool(namedThreadFactory);
        }

        @Override
        public void onMessage(final TransferData transferData) {
            for (final BusDataReceiver receiver : getReceivers()) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        receiver.received(transferData.getBusNr(), transferData.getBusData());
                    }
                });
            }
        }
    }
}
