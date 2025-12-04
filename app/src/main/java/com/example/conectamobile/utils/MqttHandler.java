package com.example.conectamobile.utils;

import android.content.Context;
import android.util.Log;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHandler {

    private MqttAndroidClient client;
    private static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    private static final String TAG = "MqttHandler";

    public void connect(Context context, String clientId) {
        client = new MqttAndroidClient(context, BROKER_URL, clientId, Ack.AUTO_ACK, null, false, 8192);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);

        client.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "✅ Conexión MQTT Exitosa");
                // NUEVO: Nos suscribimos a nuestro propio canal para recibir mensajes del profe
                subscribe("conectamobile/chat/" + clientId);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e(TAG, "❌ Falló conexión MQTT: " + exception.getMessage());
            }
        });
    }

    public void publish(String topic, String message) {
        if (client != null && client.isConnected()) {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());
            mqttMessage.setQos(1);

            client.publish(topic, mqttMessage);
            Log.d(TAG, "📤 Enviado a: " + topic);
        }
    }

    public void subscribe(String topic) {
        if (client != null && client.isConnected()) {
            client.subscribe(topic, 1);
            Log.d(TAG, "🎧 Suscrito esperando mensajes en: " + topic);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // ESTO ES LO QUE EL PROFE QUIERE VER
                    Log.d(TAG, "📩 MENSAJE RECIBIDO DESDE AFUERA: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });
        }
    }

    public void disconnect() {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }
}