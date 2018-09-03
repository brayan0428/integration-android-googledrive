package brayan0428.androidgoogledrive.com.android_googledrive;

import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient apiClient;
    FloatingActionButton crearCarpeta;

    EditText nombreCarpeta,nombreArchivo,textoArchivo;
    Button nuevaCarpeta,cancelarCarpeta,nuevoArchivo,cancelarArchivo,crearArchivo,crearArchivoRaiz;

    AlertDialog alert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crearCarpeta = findViewById(R.id.crearCarpeta);
        crearArchivo = findViewById(R.id.crearArchivo);
        crearArchivoRaiz = findViewById(R.id.crearArchivoRaiz);

        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        crearCarpeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.agregar_carpeta_modal,null);
                builder.setView(view);
                builder.setTitle("Crear Carpeta");
                nombreCarpeta = view.findViewById(R.id.nombreCarpeta);
                nuevaCarpeta = view.findViewById(R.id.guardarCarpeta);
                cancelarCarpeta = view.findViewById(R.id.cancelarCarpeta);

                nuevaCarpeta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(nombreCarpeta.getText().length() == 0){
                            mostrarMensaje("Debe ingresar el nombre de la carpeta");
                            return;
                        }
                        crearCarpeta(nombreCarpeta.getText().toString().trim());
                        alert.dismiss();
                    }
                });

                cancelarCarpeta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
                builder.create();
                alert = builder.show();
            }
        });

        crearArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearArchivo();
            }
        });

        crearArchivoRaiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.agregar_archivo_modal,null);
                builder.setView(view);
                builder.setTitle("Crear Archivo");
                nombreArchivo = view.findViewById(R.id.nombreArchivo);
                textoArchivo = view.findViewById(R.id.tituloArchivo);
                nuevoArchivo = view.findViewById(R.id.guardarArchivo);
                cancelarArchivo = view.findViewById(R.id.cancelarArchivo);

                nuevoArchivo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(nombreArchivo.getText().length() == 0){
                            mostrarMensaje("Debe ingresar el nombre del archivo");
                            return;
                        }
                        if(textoArchivo.getText().length() == 0){
                            mostrarMensaje("Debe ingresar el texto del archivo");
                            return;
                        }
                        crearArchivoRaiz(nombreArchivo.getText().toString().trim(),textoArchivo.getText().toString().trim());
                        alert.dismiss();
                    }
                });

                cancelarArchivo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
                builder.create();
                alert = builder.show();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Error de conexión: " + connectionResult,Toast.LENGTH_SHORT).show();
    }

    private void mostrarMensaje(String mensaje){
        Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
    }

    private void crearCarpeta(String folderName){
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folderName)
                .build();
        DriveFolder driveFolder = Drive.DriveApi.getRootFolder(apiClient);
        driveFolder.createFolder(apiClient,changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
                if(driveFolderResult.getStatus().isSuccess()){
                    mostrarMensaje("Carpeta creada exitosamente");
                }else{
                    mostrarMensaje(("Error al crear carpeta"));
                }
            }
        });
        driveFolder.listChildren(apiClient);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void crearArchivo(){
            Drive.DriveApi.newDriveContents(apiClient)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setMimeType("text/plain")
                                    .build();
                            escribirArchivoTexto(driveContentsResult.getDriveContents(),"Esto es un texto de prueba");
                            IntentSender intentSender = Drive.DriveApi
                                    .newCreateFileActivityBuilder()
                                    .setInitialMetadata(changeSet)
                                    .setInitialDriveContents(driveContentsResult.getDriveContents())
                                    .build(apiClient);
                            try{
                                startIntentSenderForResult(intentSender,0,null,0,0,0);
                            }catch (Exception e){
                                mostrarMensaje(e.getMessage());
                            }
                        }
                    });
    }

    private void crearArchivoRaiz(final String titulo, final String texto){
        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        if(driveContentsResult.getStatus().isSuccess()){
                            escribirArchivoTexto(driveContentsResult.getDriveContents(),texto);
                            MetadataChangeSet changeSet =
                                    new MetadataChangeSet.Builder()
                                    .setTitle(titulo)
                                    .setMimeType("text/plain")
                                    .build();
                            DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);
                            folder.createFile(apiClient,changeSet,driveContentsResult.getDriveContents())
                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                            if(driveFileResult.getStatus().isSuccess()){
                                                mostrarMensaje("Fichero creado exitosamente");
                                            }else{
                                                mostrarMensaje("Error al crear el fichero");
                                            }
                                        }
                                    });
                        }else{
                            mostrarMensaje("Error al crear drivecontents");
                        }
                    }
                });
    }
    private void escribirArchivoTexto(DriveContents driveContents,String textoArchivo){
        OutputStream outputStream = driveContents.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);
        try{
            writer.write(textoArchivo);
            writer.close();
            mostrarMensaje("Fichero creado");
        }catch (Exception e){
            mostrarMensaje("Error al crear el fichero");
        }
    }
}
