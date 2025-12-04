ConectaMobile

ConectaMobile es una aplicación de mensajería instantánea para Android que combina la robustez de Firebase con la eficiencia del protocolo MQTT para garantizar una comunicación en tiempo real segura y confiable.

Este proyecto fue desarrollado como parte de la evaluación nacional, cumpliendo con estándares modernos de desarrollo en Java y Material Design.

Características Principales

1. Autenticación Robusta (Firebase Auth)

Registro y Login: Creación de cuentas seguras con correo y contraseña.

Google Sign-In: Acceso rápido mediante cuentas de Google (SHA-1 configurado).

Persistencia: La sesión se mantiene activa hasta que el usuario decide salir.

2. Chat Híbrido en Tiempo Real

Implementamos una Arquitectura Dual única:

Persistencia (Firebase): Todos los mensajes se guardan en Realtime Database para mantener el historial.

Señalización (MQTT): Cada mensaje se envía simultáneamente a través del protocolo MQTT usando un Broker público (hivemq) para cumplir con estándares IoT y requisitos académicos.

Librería Moderna: Se utiliza el fork hannesa2 de Paho MQTT para evitar bloqueos en Android 12+.

3. Gestión de Contactos y Perfil

Lista de Contactos: Visualización de usuarios registrados en la plataforma.

Buscador Inteligente: Filtrado en tiempo real por nombre o correo.

Perfil de Usuario:

Edición de nombre.

Foto de Perfil (Base64): Sistema optimizado que convierte imágenes de la galería a Base64 para guardarlas directamente en la base de datos, eliminando la dependencia de servidores de almacenamiento externos.

4. Interfaz Intuitiva (UI/UX)

Diseño limpio inspirado en aplicaciones de mensajería líderes.

Navegación: BottomNavigationView con fragmentos (Contactos, Chats, Perfil).

Feedback Visual: Burbujas de chat diferenciadas, fotos circulares y estados de carga.

Tecnologías Utilizadas

Componente

Tecnología / Librería

Lenguaje

Java (Android SDK)

Backend

Firebase (Auth + Realtime Database)

Protocolo IoT

MQTT (v3.1.1)

Cliente MQTT

com.github.hannesa2:paho.mqtt.android (Android 12+ Safe)

Imágenes

Glide 4.16.0

Autenticación

Google Play Services Auth

Diseño

Material Design Components, ConstraintLayout

Configuración del Proyecto

Requisitos Previos

Android Studio Iguana o superior.

JDK 11 o superior.

Dispositivo Android o Emulador con Google Play Services.

Instalación

Clonar el repositorio.

Abrir en Android Studio.

Sincronizar Gradle (Sync Project with Gradle Files).

Importante: Asegurarse de que el archivo google-services.json esté en la carpeta app/.

Guía de Pruebas

1. Probar Chat y MQTT

Para verificar que el protocolo MQTT está funcionando "bajo el capó":

Abre la pestaña Logcat en Android Studio.

Filtra por la etiqueta: MqttHandler.

Envía un mensaje en la app.

Debes ver el log:  Conexión MQTT Exitosa y  Mensaje MQTT enviado....

2. Probar Recepción Externa (Opcional)

Para demostrar la conectividad con el mundo exterior:

Usa un cliente MQTT (como MyMQTT en el celular o MQTT Explorer en PC).

Conéctate a: tcp://broker.hivemq.com:1883.

Publica un mensaje en el tópico: conectamobile/chat/TU_UID_DE_FIREBASE.

Mira el Logcat de Android Studio, aparecerá:  MENSAJE RECIBIDO DESDE AFUERA....

3. Probar Foto de Perfil

Ve a la pestaña Perfil.

Toca "Cambiar Foto".

Selecciona una imagen de la galería.

La imagen se comprimirá y se guardará como texto en la base de datos, actualizándose instantáneamente en toda la app.

📂 Estructura del Proyecto

com.example.conectamobile
├── activities
│   ├── LoginActivity.java      # Entrada y Auth
│   ├── RegisterActivity.java   # Registro de usuarios
│   ├── MainActivity.java       # Contenedor de Tabs
│   └── ChatActivity.java       # Lógica del chat (Firebase + MQTT)
├── adapters
│   ├── ContactsAdapter.java    # Lista de usuarios y último mensaje
│   └── MessagesAdapter.java    # Burbujas de chat
├── fragments
│   ├── ContactsFragment.java   # Buscador y lista
│   ├── ChatsFragment.java      # Historial de conversaciones
│   └── ProfileFragment.java    # Gestión de cuenta y foto
├── models
│   ├── User.java
│   └── Message.java
└── utils
└── MqttHandler.java        # Cliente MQTT Paho encapsulado


Desarrollado por: Richard Muñoz
