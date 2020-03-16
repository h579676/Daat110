package no.hvl.dat110.iotsystem;

import no.hvl.dat110.broker.ClientSession;
import no.hvl.dat110.client.Client;
import no.hvl.dat110.common.TODO;
import no.hvl.dat110.messagetransport.MessagingClient;

public class TemperatureDevice {

	private static final int COUNT = 10;

	public static void main(String[] args) {

		// simulated / virtual temperature sensor
		TemperatureSensor sn = new TemperatureSensor();

		// TODO - start

		// create a client object and use it to

		// - connect to the broker
		// - publish the temperature(s)
		// - disconnect from the broker

		Client t = new Client("tempraturSensor", Common.BROKERHOST,  Common.BROKERPORT );

		t.connect();

		for (int i = 0; i< COUNT; i++){
			String temp = Integer.toString(sn.read());
			t.publish("temperature", temp);
		}
		t.disconnect();

		// TODO - end

		System.out.println("Temperature device stopping ... ");


	}
}