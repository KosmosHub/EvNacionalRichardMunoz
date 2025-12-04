package com.example.conectamobile.utils;

import android.content.Context;
import android.util.Log;

// IMPORTS DE LA LIBRERÍA NUEVA
import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

// IMPORTS DE PAHO CLÁSICO (Interfaces)
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHandler {

    private MqttAndroidClient client;
    private static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    private static final String TAG = "MqttHandler";

    public void connect(Context context, String clientId) {
        // CORRECCIÓN DEL CONSTRUCTOR:
        // Pasamos los 7 parámetros obligatorios: Context, URI, ID, Ack, Persistence(null), Trace(false), Buffer(8192)
        client = new MqttAndroidClient(context, BROKER_URL, clientId, Ack.AUTO_ACK, null, false, 8192);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);

        // CORRECCIÓN DEL CATCH: Eliminamos el try-catch aquí
        client.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "✅ Conexión MQTT Exitosa");
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

            // CORRECCIÓN DEL CATCH: Eliminamos el try-catch aquí
            client.publish(topic, mqttMessage);
            Log.d(TAG, "📤 Mensaje MQTT enviado: " + topic);
        }
    }

    public void subscribe(String topic) {
        if (client != null && client.isConnected()) {
            // CORRECCIÓN DEL CATCH: Eliminamos el try-catch aquí
            client.subscribe(topic, 1);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "📩 Recibido MQTT: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });
        }
    }

    public void disconnect() {
        if (client != null && client.isConnected()) {
            // CORRECCIÓN DEL CATCH: Eliminamos el try-catch aquí
            client.disconnect();
        }
    }
}