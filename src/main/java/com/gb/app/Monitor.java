/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */

package com.gb.app;

import java.io.IOException;
import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 */
public class Monitor implements MqttCallback {

    /**
     * The main entry point of the sample.
     */
    public static void main(String[] args) {

        String topic = "#";
        if (args.length > 0) {
            topic = args[0];
        }
        int qos = 0;
        String broker = "test.mosquitto.org";
//		String broker 		= "localhost";
        int port = 1883;
        String clientId = MqttClient.generateClientId();
        String protocol = "tcp://";
//		String protocol = "ssl://";
        String url = protocol + broker + ":" + port;

        System.out.println("clientId: " + clientId);

        try {
            Monitor sampleClient = new Monitor(url, clientId, true, false);
            sampleClient.subscribe(topic, qos);
        } catch (MqttException me) {
            // Display full details of any exception that occurs
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
        System.exit(0);
    }

    // Private instance variables
    private MqttClient client;
    private String brokerUrl;
    private boolean quietMode;
    private MqttConnectOptions conOpt;
    private boolean clean;

    /**
     * Constructs an instance of the sample client wrapper
     *
     * @param brokerUrl    the url of the server to connect to
     * @param clientId     the client id to connect with
     * @param cleanSession clear state at end of connection or not (durable or non-durable subscriptions)
     * @param quietMode    whether debug should be printed to standard out
     * @throws MqttException
     */
    public Monitor(String brokerUrl, String clientId, boolean cleanSession, boolean quietMode) throws MqttException {
        this.brokerUrl = brokerUrl;
        this.quietMode = quietMode;
        this.clean = cleanSession;

        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(clean);

        client = new MqttClient(this.brokerUrl, clientId, dataStore);
        client.setCallback(this);
    }


    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     *
     * @param topicName to subscribe to (can be wild carded)
     * @param qos       the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {


        client.connect(conOpt);
        log("Connected to " + brokerUrl + " with client ID " + client.getClientId());

        log("Subscribing to topic \"" + topicName + "\" qos " + qos);
        client.subscribe(topicName, qos);

        // Continue waiting for messages until the Enter is pressed
        log("Press <Enter> to exit");
        try {
            System.in.read();
        } catch (IOException e) {
            //If we can't read we'll just exit
        }
        client.disconnect();
        client.close();

        log("Disconnected");
    }

    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does nothing
     *
     * @param message the message to log
     */
    private void log(String message) {
        if (!quietMode) {
            System.out.println(message);
        }
    }

    /****************************************************************/
    /* Methods to implement the MqttCallback interface              */
    /****************************************************************/

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
        log("Connection to " + brokerUrl + " lost!" + cause);
        System.exit(1);
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    public void messageArrived(String topic, MqttMessage message) throws MqttException {

        String time = new Timestamp(System.currentTimeMillis()).toString();
        System.out.println("Time:\t" + time +
                "  Topic:\t" + topic +
                "  Message:\t" + new String(message.getPayload()) +
                "  QoS:\t" + message.getQos());
    }

    /****************************************************************/
    /* End of MqttCallback methods                                  */
    /****************************************************************/

}



