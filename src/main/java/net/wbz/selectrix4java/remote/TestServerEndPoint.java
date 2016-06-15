package net.wbz.selectrix4java.remote;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Daniel Tuerk
 */
@ServerEndpoint(value = "/game")
public class TestServerEndPoint {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("Connected ... " + this.session.getId());

        foo();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("Received from client ("+session.getId()+"): " + message);
        switch (message) {
            case "quit":
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Game ended"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
//        return "server: " +message;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }

    public void foo() {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("serial-io-executor-%d").build();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    session.getBasicRemote().sendText("test "+ new Date().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0L, 2000L, TimeUnit.MILLISECONDS);
    }
}
