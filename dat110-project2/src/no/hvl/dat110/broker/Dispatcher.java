package no.hvl.dat110.broker;

import java.util.Iterator;
import java.util.Set;
import java.util.Collection;

import no.hvl.dat110.common.Logger;
import no.hvl.dat110.common.Stopable;
import no.hvl.dat110.messages.*;
import no.hvl.dat110.messagetransport.Connection;
import no.hvl.dat110.messagetransport.MessagingClient;

public class Dispatcher extends Stopable {

    private Storage storage;

    public Dispatcher(Storage storage) {
        super("Dispatcher");
        this.storage = storage;

    }

    @Override
    public void doProcess() {

        Collection<ClientSession> clients = storage.getSessions();

        Logger.lg(".");
        for (ClientSession client : clients) {

            Message msg = null;

            if (client.hasData()) {
                msg = client.receive();
            }

            // a message was received
            if (msg != null) {
                dispatch(client, msg);
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void dispatch(ClientSession client, Message msg) {

        MessageType type = msg.getType();

        // invoke the appropriate handler method
        switch (type) {

            case DISCONNECT:
                onDisconnect((DisconnectMsg) msg);
                break;

            case CREATETOPIC:
                onCreateTopic((CreateTopicMsg) msg);
                break;

            case DELETETOPIC:
                onDeleteTopic((DeleteTopicMsg) msg);
                break;

            case SUBSCRIBE:
                onSubscribe((SubscribeMsg) msg);
                break;

            case UNSUBSCRIBE:
                onUnsubscribe((UnsubscribeMsg) msg);
                break;

            case PUBLISH:
                onPublish((PublishMsg) msg);
                break;

            default:
                Logger.log("broker dispatch - unhandled message type");
                break;

        }
    }

    // called from Broker after having established the underlying connection
    public void onConnect(ConnectMsg msg, Connection connection) {

        String user = msg.getUser();

        Logger.log("onConnect:" + msg.toString());

        if(storage.hasBufferedMessages(user)){
            Set<Message> m = storage.getBufferedMessages(user);
            ClientSession session = storage.getSession(user);
            for(Iterator<Message> i = m.iterator(); i.hasNext();){
                    session.send(i.next());
                    storage.removeBufferedMessage(user, i.next());
            }
        }

        storage.addClientSession(user, connection);

    }

    // called by dispatch upon receiving a disconnect message
    public void onDisconnect(DisconnectMsg msg) {

        String user = msg.getUser();

        Logger.log("onDisconnect:" + msg.toString());

        storage.removeClientSession(user);

    }

    public void onCreateTopic(CreateTopicMsg msg) {

        Logger.log("onCreateTopic:" + msg.toString());

        // create the topic in the broker storage
        // the topic is contained in the create topic message
        String topic = msg.getTopic();
        storage.createTopic(topic);
    }

    public void onDeleteTopic(DeleteTopicMsg msg) {

        Logger.log("onDeleteTopic:" + msg.toString());

        // delete the topic from the broker storage
        // the topic is contained in the delete topic message

        String topic = msg.getTopic();
        storage.deleteTopic(topic);

    }

    public void onSubscribe(SubscribeMsg msg) {

        Logger.log("onSubscribe:" + msg.toString());

        // subscribe user to the topic
        // user and topic is contained in the subscribe message

        String user = msg.getUser();
        String topic = msg.getTopic();
        storage.addSubscriber(user, topic);

    }

    public void onUnsubscribe(UnsubscribeMsg msg) {

        Logger.log("onUnsubscribe:" + msg.toString());

        // unsubscribe user to the topic
        // user and topic is contained in the unsubscribe message
        String user = msg.getUser();
        String topic = msg.getTopic();
        storage.removeSubscriber(user, topic);

    }

    public void onPublish(PublishMsg msg) {

        Logger.log("onPublish:" + msg.toString());

        // publish the message to clients subscribed to the topic
        // topic and message is contained in the subscribe message
        // messages must be sent used the corresponding client session objects

        Set<String> subs = storage.getSubscribers(msg.getTopic());
        ClientSession session = null;

        for (String s : subs) {
            session = storage.getSession(s);
            if (session != null) {
                session.send(msg);
            } if(session.noConnection(session)){
               String u = session.getUser();
               storage.addBufferedMessage(u, msg);
            }


        }
    }
}