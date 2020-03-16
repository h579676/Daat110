package no.hvl.dat110.broker;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import no.hvl.dat110.client.Client;
import no.hvl.dat110.common.Logger;
import no.hvl.dat110.messages.Message;
import no.hvl.dat110.messagetransport.Connection;

public class Storage {

	// data structure for managing subscriptions
	// maps from a topic to set of subscribed users
	protected ConcurrentHashMap<String, Set<String>> subscriptions;

	// data structure for managing currently connected clients
	// maps from user to corresponding client session object

	protected ConcurrentHashMap<String, ClientSession> clients;

	// hashMap to save buffered messages
	// maps a user to a set of messages sent while the user was
	// unconnected, corresponding to the topics a users ClientSession is subscribed to

	protected ConcurrentHashMap<String, Set<Message>> bufferedMessages;

	public Storage() {
		subscriptions = new ConcurrentHashMap<String, Set<String>>();
		clients = new ConcurrentHashMap<String, ClientSession>();
		bufferedMessages = new ConcurrentHashMap<String, Set<Message>>();
	}

	public Collection<ClientSession> getSessions() {
		return clients.values();
	}

	public Set<String> getTopics() {

		return subscriptions.keySet();

	}

	// get the session object for a given user
	// session object can be used to send a message to the user

	public ClientSession getSession(String user) {

		ClientSession session = clients.get(user);

		return session;
	}

	public Set<String> getSubscribers(String topic) {

		return (subscriptions.get(topic));

	}

	public void addClientSession(String user, Connection connection) {

		ClientSession session = new ClientSession(user, connection);
		clients.put(user, session);

	}

	public void removeClientSession(String user) {

		ClientSession session = clients.get(user);
		clients.remove(user, session);

	}

	public void addBufferedMessage(String user, Message message){

			Set<Message> s = ConcurrentHashMap.newKeySet();
			s.add(message);
			bufferedMessages.put(user, s);
	}

	public Set<Message> getBufferedMessages(String user){

		Set<Message> m = bufferedMessages.get(user);
		return m;

	}

	public boolean hasBufferedMessages(String user){

		return(bufferedMessages.containsKey(user));
	}

	public void removeBufferedMessage(String user, Message message){

		bufferedMessages.remove(user, message);
	}

	public void createTopic(String topic) {

		Set<String> s = ConcurrentHashMap.newKeySet();
		subscriptions.put(topic, s);
	}

	public void deleteTopic(String topic) {

		subscriptions.remove(topic);

	}

	public void addSubscriber(String user, String topic) {
		if (subscriptions.containsKey(topic)) {
			Set<String> s = getSubscribers(topic);
			s.add(user);
			subscriptions.replace(topic, s);
		}

	}

	public void removeSubscriber(String user, String topic) {
		if (subscriptions.containsKey(topic)) {
			Set<String> s = getSubscribers(topic);
			if (s.contains(user)) {
				s.remove(user);
			}
			subscriptions.replace(topic, s);
		}

	}

}