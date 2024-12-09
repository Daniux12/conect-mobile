package com.stomas.conectamobile;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
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

public class Login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas y Firebase
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://conectmobile-7b74b-default-rtdb.firebaseio.com/")
                .getReference();

        // Configurar el botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    // Iniciar sesión
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Inicio de sesión exitoso
                                    Toast.makeText(Login.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                                    // Redirigir a la actividad de Contactos
                                    Intent intent = new Intent(Login.this, Contactos.class);
                                    startActivity(intent);
                                    finish(); // Opcional: Cierra esta actividad para evitar volver atrás
                                } else {
                                    // Error al iniciar sesión
                                    String errorMessage = getFirebaseErrorMessage(task.getException());
                                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(Login.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Configurar el botón de registro
        btnRegister.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));
    }

    private void iniciarSesion(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Usuario autenticado correctamente
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            verificarUsuarioEnBaseDeDatos(user.getUid(), email);
                        }
                    } else {
                        // Manejo de errores
                        String errorMessage = getFirebaseErrorMessage(task.getException());
                        Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void verificarUsuarioEnBaseDeDatos(String userId, String email) {
        mDatabase.child("usuarios").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Usuario ya registrado en la base de datos
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        redirigirAMainActivity();
                    } else {
                        // Usuario no registrado en la base de datos, agregarlo
                        registrarUsuarioEnBaseDeDatos(userId, email);
                    }
                });
    }

    private void registrarUsuarioEnBaseDeDatos(String userId, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", "Nuevo Usuario"); // Puedes pedir el nombre del usuario en otra pantalla

        mDatabase.child("usuarios").child(userId).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Usuario guardado en la base de datos", Toast.LENGTH_SHORT).show();
                        redirigirAMainActivity();
                    } else {
                        Toast.makeText(this, "Error al guardar los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirigirAMainActivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "Error desconocido";

        String message = exception.getMessage();

        if (message != null) {
            if (message.contains("The email address is badly formatted")) {
                return "El correo tiene un formato inválido.";
            } else if (message.contains("The password is invalid")) {
                return "La contraseña es incorrecta.";
            } else if (message.contains("There is no user record corresponding to this identifier")) {
                return "No se encuentra un usuario con este correo.";
            }
        }
        return "Error al iniciar sesión. Intenta de nuevo.";
    }
}
