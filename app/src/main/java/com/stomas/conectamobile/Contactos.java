package com.stomas.conectamobile;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contactos extends AppCompatActivity {

    private EditText etSearch;
    private Button btnAddContact;
    private ListView lvContacts;
    private List<String> contactsList;
    private ContactsAdapter contactsAdapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);

        // Inicializar vistas
        etSearch = findViewById(R.id.et_search);
        btnAddContact = findViewById(R.id.btn_add_contact);
        lvContacts = findViewById(R.id.lv_contacts);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://conectmobile-7b74b-default-rtdb.firebaseio.com/")
                .getReference();

        // Inicializar lista de contactos
        contactsList = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(this, contactsList);
        lvContacts.setAdapter(contactsAdapter);

        // Cargar los contactos desde Firebase
        loadContacts();

        // Configurar búsqueda de contactos
        etSearch.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                String query = etSearch.getText().toString().trim();
                searchContacts(query);
                return true;
            }
            return false;
        });

        // Configurar botón para agregar contactos
        btnAddContact.setOnClickListener(v -> {
            String email = etSearch.getText().toString().trim();
            if (!email.isEmpty()) {
                addContact(email);
            } else {
                Toast.makeText(Contactos.this, "Escribe un correo válido", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadContacts() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // Leer los contactos del usuario actual desde Firebase
        mDatabase.child("usuarios").child(userId).child("contacts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        contactsList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String contactUid = snapshot.getKey();
                            fetchContactEmail(contactUid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Contactos.this, "Error al cargar los contactos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchContactEmail(String contactUid) {
        // Obtener el correo electrónico del contacto por su UID
        mDatabase.child("usuarios").child(contactUid).child("email")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String email = dataSnapshot.getValue(String.class);
                            if (email != null) {
                                contactsList.add(email);
                                contactsAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Contactos.this, "Error al obtener datos del contacto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchContacts(String query) {
        // Buscar un usuario por correo
        mDatabase.child("usuarios").orderByChild("email").equalTo(query)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(Contactos.this, "Usuario encontrado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Contactos.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Contactos.this, "Error al buscar usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addContact(String email) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // Buscar el UID del usuario por su correo
        mDatabase.child("usuarios").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // UID del contacto encontrado
                            String contactUid = dataSnapshot.getChildren().iterator().next().getKey();
                            if (contactUid != null) {
                                // Agregar el UID del contacto a la lista del usuario actual
                                mDatabase.child("usuarios").child(userId).child("contacts").child(contactUid).setValue(true)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Contactos.this, "Contacto agregado exitosamente", Toast.LENGTH_SHORT).show();
                                                fetchContactEmail(contactUid);
                                            } else {
                                                Toast.makeText(Contactos.this, "Error al agregar contacto", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Contactos.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Contactos.this, "Error al buscar usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
