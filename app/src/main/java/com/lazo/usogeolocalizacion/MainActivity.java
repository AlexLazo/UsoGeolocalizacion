package com.lazo.usogeolocalizacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btnObtenerUbicacion, btnWA, btnMapa;
    TextView tvLatitud, tvDireccion, tvLongitud;
    public static final int CODIGO_UBICACION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnObtenerUbicacion = findViewById(R.id.btnUbicacion);
        btnWA = findViewById(R.id.btnWA);
        tvLatitud = findViewById(R.id.tvLatitud);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvLongitud = findViewById(R.id.tvLongitud);
        btnMapa = findViewById(R.id.btnMapa);

        btnObtenerUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerUbicacion();
            }
        });
        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapa = new Intent(MainActivity.this, Mapa.class);
                mapa.putExtra("Longitud", Double.parseDouble(tvLongitud.getText().toString()));
                mapa.putExtra("Latitud", Double.parseDouble(tvLatitud.getText().toString()));
                startActivity(mapa);
            }
        });

        btnWA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnviarUbicacion();
            }
        });

    }

    private void EnviarUbicacion() {
        Intent intentWhatsApp = new Intent(Intent.ACTION_SEND);
        intentWhatsApp.setType("text/plain");
        intentWhatsApp.setPackage("com.whatsapp");
        String latitud = tvLatitud.getText().toString();
        String longitud = tvLongitud.getText().toString();
        String url = "https://maps.google.com/?q="+latitud+","+longitud+"";
        intentWhatsApp.putExtra(Intent.EXTRA_TEXT, "Hola!, te adjunto mi ubicación: "+url);
        startActivity(intentWhatsApp);
    }

    public void obtenerUbicacion() {
        verificarPermisosUbicacion();
    }

    public void verificarPermisosUbicacion() {
        if(
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_UBICACION
            );
        }else {
            iniciarUbicacion();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        if (requestCode== CODIGO_UBICACION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarUbicacion();
                return;
            }
        }
    }

    public void iniciarUbicacion() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion localizacion = new Localizacion();
        localizacion.setMainActivity(this);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        if(
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_UBICACION
            );
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, localizacion);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, localizacion);
        Toast.makeText(this, "Localizacion Iniciada", Toast.LENGTH_SHORT).show();
        tvLatitud.setText(null);
        tvLongitud.setText(null);
        tvDireccion.setText(null);
        }
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loccation) {
            tvLatitud.setText(String.valueOf(loccation.getLatitude()));
            tvLongitud.setText(String.valueOf(loccation.getLongitude()));
            this.mainActivity.obtenerDireccion(loccation);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("Status GPS", "GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.i("Status GPS", "GPS Activado");
        }

    }


    public void obtenerDireccion(Location location) {
        if (location.getLatitude() != 0 && location.getLongitude() != 0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                );

                if (!list.isEmpty()) {
                    Address direccion  = list.get(0);
                    tvDireccion.setText(direccion.getAddressLine(0));
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
