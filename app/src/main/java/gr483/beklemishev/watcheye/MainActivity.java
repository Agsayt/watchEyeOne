package gr483.beklemishev.watcheye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private String destinationAddress = "node00.ddns.net";
    private int destinationPort = 2040;

    ListView loadLV;
    ArrayList<NetworkSettings> lstNetwork = new ArrayList<NetworkSettings>();
    ArrayAdapter<NetworkSettings> adpNetwork;
    eyeCanvas mainCanvas;
    TextView fps;
    EditText channel;
    ImageView streamImage;

    Client client;
    public static Thread mainThread;
    private Timer timer;

    public static Bitmap inputStream2Bitmap(InputStream is) {
        return BitmapFactory.decodeStream(is);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        streamImage = findViewById(R.id.streamImage);
        fps = findViewById(R.id.framesPerSecond);
        channel = findViewById(R.id.etChannelNumber);
        channel.setText("3");


        mainCanvas = findViewById(R.id.eyeCanvas);
        client = new Client(mainCanvas, destinationAddress, destinationPort, Integer.parseInt(channel.getText().toString()));
        mainThread = new Thread(client);


        startFPSCounting();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); // иначе вываливается в NetworkOnMainThreadException
        StrictMode.setThreadPolicy(policy);


        mainThread.start();
    }

    public void onChangeChannel(View v)
    {
        timer.cancel();
        try {
            client.stopTranslation();
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.changeChannel(Integer.valueOf(channel.getText().toString()));

        client.startTranslation();
        mainThread = new Thread(client);
        mainThread.start();

        startFPSCounting();
    }

    private void startFPSCounting() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int buffer = client.frames;
                        fps.setText("rate: " + String.valueOf(buffer) + " fps");
                        client.frames = 0;
                    }
                });

            }
        }, 0, 1000);
    }

    public void onStopTranslationClick(View v){
        if (client.getCurrentState()){
            timer.cancel();
            fps.setText("rate: " + 0 + " fps");
            try {
                client.stopTranslation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            Toast.makeText(this, "Вы уже отписались от потока", Toast.LENGTH_SHORT).show();

    }

    public void onResumeTranslationClick(View v){
        if (!client.getCurrentState()){
            client.startTranslation();
            mainThread = new Thread(client);
            mainThread.start();
            startFPSCounting();
        } else
            Toast.makeText(this, "Поток уже получается", Toast.LENGTH_SHORT).show();


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.Settings: {
                final View customLayout = getLayoutInflater().inflate(R.layout.dialog_settings, null);
                AlertDialog.Builder bld = new AlertDialog.Builder(this);
                bld.setTitle("Настройки подключения!");
                bld.setView(customLayout);

                Dialog dlg = bld.create();

                EditText address = customLayout.findViewById(R.id.etDestinationAddress);
                EditText port = customLayout.findViewById(R.id.etDestinationPort);

                Button accept = customLayout.findViewById(R.id.bAcceptSettings);
                Button cancel = customLayout.findViewById(R.id.bCancelSettings);
                Button save = customLayout.findViewById(R.id.bSaveNetworkSettingsDialog);
                Button load = customLayout.findViewById(R.id.bLoadNetworkSettingsDialog);

                address.setText(destinationAddress);
                port.setText(String.valueOf(destinationPort));

                dlg.show();

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OnNetworkSettingsSave(view, "save", address.getText().toString(), port.getText().toString());
                    }
                });
                load.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OnNetworkSettingsLoad(view, "load", address, port);
                    }
                });

                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        destinationAddress = String.valueOf(address.getText());
                        destinationPort = Integer.valueOf(String.valueOf(port.getText()));
                    }

                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dlg.cancel();
                    }
                });
            }
//            case R.id.Save: {
//                final View customLayout = getLayoutInflater().inflate(R.layout.dialog_imagesave, null);
//                AlertDialog.Builder bld = new AlertDialog.Builder(this);
//                bld.setTitle("Настройки подключения!");
//                bld.setView(customLayout);
//
//                LinearLayout preview = customLayout.findViewById(R.id.previewGridSave);
//                EditText imageName = customLayout.findViewById(R.id.etImageNameToSave);
//
//                Button save = customLayout.findViewById(R.id.bSaveImage);
//                Button cancel = customLayout.findViewById(R.id.bCancelImageSave);
//
//                Dialog dlg = bld.create();
//                dlg.show();
//
//                GridLayout prevGrid = new GridLayout(getApplicationContext());
//                prevGrid.setRowCount(gridHeight);
//                prevGrid.setColumnCount(gridWidth);
//                List<Button> prevButtonsList = new ArrayList<>();
//                FillGrid(gridWidth, gridHeight, prevGrid, prevButtonsList);
//                for (int i = 0; i < prevButtonsList.size(); i++) {
//                    prevButtonsList.get(i).setEnabled(false);
//                    prevButtonsList.get(i).setTag(addedButtons.get(i).getTag());
//                    prevButtonsList.get(i).setText(addedButtons.get(i).getText());
//                    ColorDrawable viewColor = (ColorDrawable) addedButtons.get(i).getBackground();
//                    int colorId = viewColor.getColor();
//                    prevButtonsList.get(i).setBackgroundColor(colorId);
//                }
//                preview.addView(prevGrid);
//
//                save.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        int[] buffer = new int[prevGrid.getColumnCount() * prevGrid.getRowCount()];
//                        for (int i = 0; i < prevButtonsList.size(); i++) {
//                            ColorDrawable viewColor = (ColorDrawable) prevButtonsList.get(i).getBackground();
//                            int colorId = viewColor.getColor();
//                            buffer[i] = colorId;
//                        }
//                        int nid = StaticDb.database.getMaxIdForSavedImages() + 1;
//                        StaticDb.database.addImage(nid, buffer, imageName.getText().toString());
//
//                        Toast.makeText(getApplicationContext(), "Изображение успешно сохранено!", Toast.LENGTH_LONG).show();
//                        dlg.cancel();
//                    }
//                });
//
//                cancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dlg.cancel();
//                    }
//                });
//            }
//            break;
//            case R.id.Load: {
//                Intent i = new Intent(this, StateActivity.class);
//                startActivityForResult(i, 1);
//            }
//            break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void OnNetworkSettingsLoad(View view, String load, EditText address, EditText port) {
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_loadnetworksettings, null);
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Загрузка настроек подключения!");
        bld.setView(customLayout);

        Dialog dlg = bld.create();
        dlg.show();

        loadLV = customLayout.findViewById(R.id.lvNetworkSettings);
        adpNetwork = new ArrayAdapter<NetworkSettings>(this, android.R.layout.simple_list_item_1, lstNetwork);

        loadLV.setAdapter(adpNetwork);

        loadLV.setOnItemClickListener((parent, _view, position, id) -> {
            NetworkSettings n = adpNetwork.getItem(position);
            address.setText(n.Address);
            port.setText(String.valueOf(n.Port));
            dlg.cancel();
        });

        lstNetwork.clear();
        StaticDB.database.getAllNetworkSettings(lstNetwork);
        adpNetwork.notifyDataSetChanged();
    }
    private void OnNetworkSettingsSave(View view, String mode, String _address, String _port) {

        View customLayout = getLayoutInflater().inflate(R.layout.dialog_savenetworksettings, null);
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Сохранение настроек подключения!");
        bld.setView(customLayout);

        Dialog dlg = bld.create();

        EditText title = customLayout.findViewById(R.id.etSaveName);
        EditText address = customLayout.findViewById(R.id.etAddressToSave);
        EditText port = customLayout.findViewById(R.id.etPortToSave);

        Button save = customLayout.findViewById(R.id.bSaveNetworkSettings);
        Button cancel = customLayout.findViewById(R.id.bCancelNetworkSettingsDialog);

        address.setText(_address);
        port.setText(_port);

        dlg.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nid = StaticDB.database.getMaxIDForNetworkSettings() + 1;
                StaticDB.database.addNetworkSettings(nid, title.getText().toString(), address.getText().toString(), Integer.valueOf(port.getText().toString()));
                Toast.makeText(getApplicationContext(), "Запись добавлена!", Toast.LENGTH_LONG).show();
                dlg.cancel();
            }
        });
    }

}