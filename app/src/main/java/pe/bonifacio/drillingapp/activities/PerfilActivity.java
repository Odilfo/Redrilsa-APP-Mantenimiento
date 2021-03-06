package pe.bonifacio.drillingapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pe.bonifacio.drillingapp.R;
import pe.bonifacio.drillingapp.api.WebService;
import pe.bonifacio.drillingapp.api.WebServiceApi;
import pe.bonifacio.drillingapp.models.Usuario;
import pe.bonifacio.drillingapp.shared_pref.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etDni;
    private EditText etCargo;
    private EditText etPassword;
    private ImageView ivUsuario;
    private Button tvDelete;
    private Button tvLogOut;
    private Button tvProyectos;
    private Button btUpdate;
    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        setUpUsuario();
        setUpView();
    }
    private void setUpUsuario(){
        usuario= SharedPrefManager.getInstance(getApplicationContext()).getUsuario();

    }
    public void setUpView(){

        etName=findViewById(R.id.etName);
        etName.setText(usuario.getNombre());
        etEmail=findViewById(R.id.etEmail);
        etEmail.setText(usuario.getEmail());
        etDni=findViewById(R.id.etDni);
        etDni.setText(usuario.getDni());
        etCargo=findViewById(R.id.etCargo);
        etCargo.setText(usuario.getCargo());
        etPassword=findViewById(R.id.etPassword);
        etPassword.setText(usuario.getPassword());

        btUpdate=findViewById(R.id.btUpdate);
        tvDelete=findViewById(R.id.tvDelete);
        tvLogOut=findViewById(R.id.tvLogOut);


        //Proyectos
        tvProyectos=findViewById(R.id.tvProyectos);
        tvProyectos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProyectoActivity.class));
                Toast.makeText(PerfilActivity.this, "Registro de Proyecto", Toast.LENGTH_SHORT).show();
            }
        });

        //Cerrar Sesión
        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        //Actualizar Usuario
        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUsuario();
            }
        });
        //Eliminar Usuario
        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteById();
            }
        });

    }
    //Cerrar Sesión
    public void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
        builder.setTitle("¿Deseas cerrar sesión?");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNeutralButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefManager.getInstance(getApplicationContext()).logOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
    }
    //Actualizar USUARIO
    public void updateUsuario(){
        String email = etEmail.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String dni=etDni.getText().toString().trim().toUpperCase();
        String cargo=etCargo.getText().toString().trim().toUpperCase();
        String password=etPassword.getText().toString().trim();

        if(name.isEmpty()){
            etName.setError(getResources().getString(R.string.name_error));
            etName.requestFocus();
            return;
        }

        if(email.isEmpty()){
            etEmail.setError(getResources().getString(R.string.email_error));
            etEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError(getResources().getString(R.string.email_doesnt_match));
            etEmail.requestFocus();
            return;
        }
        if(dni.isEmpty()){
            etDni.setError("Ingrese correcto tu dni");
            etDni.requestFocus();
            return;
        }
        if(dni.length()<8){
            etDni.setError("Dni no debe ser menor a 8 digitos");
            etDni.requestFocus();
            return;
        }
        if(cargo.isEmpty()){
            etCargo.setError("ingrese cargo");
            etCargo.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingrese correctamente");
            etPassword.requestFocus();
            return;
        }
        if (password.length()<6){
            etPassword.setError(getResources().getString(R.string.password_error_less_than));
            etPassword.requestFocus();
            return;
        }
        usuario.setNombre(name);
        usuario.setEmail(email);
        usuario.setCargo(cargo);
        usuario.setDni(dni);
        usuario.setPassword(password);

        AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
        builder.setTitle("¿Deseas actualizar Usuario?");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNeutralButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Call<Usuario> call = WebService
                        .getInstance()
                        .createService(WebServiceApi.class)
                        .update(usuario);
                call.enqueue(new Callback<Usuario>() {
                    @Override
                    public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                        if (response.code() == 200) {
                            Log.d("TAG1", "Usuario actualizado correctamente");
                            SharedPrefManager.getInstance(getApplicationContext())
                                    .saveUsuario(response.body());
                            Toast.makeText(PerfilActivity.this, "Usuario Actualizado", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                        } else if (response.code() == 400) {
                            Log.d("TAG1", "Usuario no existe");
                            Toast.makeText(PerfilActivity.this, "Usuario no existe", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("TAG1", "Error indeterminado");
                        }
                    }
                    @Override
                    public void onFailure(Call<Usuario> call, Throwable t) {

                    }
                });
            }
        });
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    //Eliminar USUARIO
    public void deleteById() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
        builder.setTitle("¿Estas seguro de eliminar usuario?");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNeutralButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Call<Void>call=WebService
                        .getInstance()
                        .createService()
                        .deleteById(usuario.getId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(response.code() == 200){
                            Log.d("TAG1", "Usuario eliminado correctamente");
                            Toast.makeText(PerfilActivity.this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                            logout();
                        }else{
                            Log.d("TAG1", "Error no definido");
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });
            }
        });
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

}
