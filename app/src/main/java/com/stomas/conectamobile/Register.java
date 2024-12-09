package com.stomas.conectamobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas, FirebaseAuth y Firebase Realtime Database
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        btnRegister = findViewById(R.id.btn_register_user);

        mAuth = FirebaseAuth.getInstance();

        // Crear referencia directa a la base de datos usando la URL personalizada
        mDatabase = FirebaseDatabase.getInstance("https://conectmobile-7b74b-default-rtdb.firebaseio.com/")
                .getReference();

        // Configurar el evento para el botón de registro
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                if (password.length() >= 6) {
                    registerUser(email, password);
                } else {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String email, String password) {
        // Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Usuario registrado correctamente en Authentication
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Guardar información del usuario en Firebase Realtime Database
                            saveUserToDatabase(user.getUid(), email);
                        }
                    } else {
                        // Manejar errores de autenticación
                        String errorMessage = getFirebaseErrorMessage(task.getException());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String email) {
        // Crear objeto de datos del usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", "Nombre de Usuario"); // Puedes reemplazar con un campo de nombre si existe

        // Guardar en la base de datos
        mDatabase.child("usuarios").child(userId).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Usuario registrado correctamente.", Toast.LENGTH_SHORT).show();
                        finish(); // Finalizar la actividad
                    } else {
                        Toast.makeText(this, "Error al guardar en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para manejar errores de Firebase Authentication
    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "Error desconocido";

        String message = exception.getMessage();

        if (message != null) {
            if (message.contains("The email address is badly formatted")) {
                return "El correo tiene un formato inválido.";
            } else if (message.contains("Password should be at least 6 characters")) {
                return "La contraseña debe tener al menos 6 caracteres.";
            } else if (message.contains("The email address is already in use")) {
                return "El correo ya está registrado.";
            }
        }
        return "Error al registrar. Intenta de nuevo.";
    }
}
