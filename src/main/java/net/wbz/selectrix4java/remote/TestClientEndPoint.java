package net.wbz.selectrix4java.remote;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * @author Daniel Tuerk
 */
@ClientEndpoint
public class TestClientEndPoint {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static CountDownLatch latch;
    private Session session;


    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected ... " + session.getId());
        try {
            this.session = session;

            this.session.getBasicRemote().sendText("start");
            sender();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @OnMessage
    public void onMessage(String message, Session session) {
            logger.info("Received from " + session.getId() + ": " + message);
    }

    public void sender() {
      new Thread(new Runnable() {
          @Override
          public void run() {

              BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
              try {
                  String userInput = bufferRead.readLine();
                  session.getAsyncRemote().sendText(userInput);
                  Thread.sleep(100L);
                  sender();
              } catch (IOException e) {
                  e.printStackTrace();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }

          }
      }).start();



    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
        latch.countDown();
    }

    public static void main(String[] args) {
        latch = new CountDownLatch(1);

        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(TestClientEndPoint.class, new URI("ws://localhost:8025/websockets/game"));
            latch.await();

        } catch (DeploymentException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
