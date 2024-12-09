package com.stomas.conectamobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSendMessage;

    private FirebaseAuth mAuth;
    private MqttHelper mqttHelper;
    private MessagesAdapter messagesAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicializar vistas
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        // Inicializar Firebase y MQTT
        mAuth = FirebaseAuth.getInstance();
        mqttHelper = new MqttHelper();
        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messageList);
        rvMessages.setAdapter(messagesAdapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        // Suscribirse al tópico de chat
        String currentUserId = mAuth.getCurrentUser().getUid();
        String chatTopic = currentUserId + "/usuario2/chat";  // Reemplazar con el ID del otro usuario
        mqttHelper.subscribeToTopic(chatTopic);

        // Cargar los mensajes de Firebase (opcional) si también los guardas en Firebase

        // Enviar mensaje
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Obtener el ID del usuario actual y el ID del usuario con el que está chateando
            String currentUserId = mAuth.getCurrentUser().getUid();
            String chatTopic = currentUserId + "/usuario2/chat";  // Reemplazar con el ID del otro usuario

            // Publicar el mensaje en el tópico MQTT
            mqttHelper.publishMessage(chatTopic, messageText);

            // Agregar el mensaje a la lista de mensajes y actualizar la UI
            Message message = new Message(messageText, currentUserId);
            messageList.add(message);
            messagesAdapter.notifyDataSetChanged();

            // Limpiar el campo de texto
            etMessage.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttHelper.disconnect();  // Desconectar cuando se cierre la actividad
    }
}