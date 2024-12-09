package com.stomas.conectamobile;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {

    private static final String TAG = "MqttHelper";
    private MqttClient mqttClient;
    private MqttConnectOptions mqttConnectOptions;

    public MqttHelper() {
        try {
            mqttClient = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId(), null);
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttClient.connect(mqttConnectOptions);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Message received: " + message.toString());
                    // Aquí puedes procesar el mensaje recibido (como actualizar la interfaz de usuario)
                }

                @Override
                public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
                    Log.d(TAG, "Message delivered: " + token.getMessageId());
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String topic) {
        try {
            mqttClient.subscribe(topic);
            Log.d(TAG, "Subscribed to topic: " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());
            mqttClient.publish(topic, mqttMessage);
            Log.d(TAG, "Message published: " + message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
            Log.d(TAG, "Disconnected from MQTT broker");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
