package gr483.beklemishev.watcheye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Client implements Runnable{

    String destinationAddress = null;
    Integer destinationPort = null;
    InetAddress addr = null;
    int frames = 0;
    private int channel;
    private InputStream inputStream;

    eyeCanvas mainCanvas;
    Socket socket;
    Bitmap image = null;

    private boolean onRun = true;

    byte[] subscribe;
    private Timer timer;

    public Client(eyeCanvas canvas, String host, int port, int channelNum){
        mainCanvas = canvas;
        destinationAddress = host;
        destinationPort = port;
        channel = channelNum;
    }

    public InputStream getInputStream(){
        return inputStream;
    }

    public boolean getCurrentState()
    {
        return onRun;
    }

    public void changeChannel(int channelNum)
    {
        channel = channelNum;
    }

    public void stopTranslation() throws IOException {
        if (onRun){
            onRun = false;
            OutputStream out = socket.getOutputStream();
            InputStream input = socket.getInputStream();

            subscribe = new byte[5];
            subscribe[0] = 5;
            subscribe[1] = 0;
            subscribe[2] = 3;
            subscribe[3] = (byte) channel;
            subscribe[4] = 0;
            out.write(subscribe);
        }

    }

    public void startTranslation()
    {
        if (!onRun){
            onRun = true;
        }

    }


    int read_u8(byte[] arr, int ofs){
        int x = arr[ofs];
        if (x < 0) x += 256;
        return x;
    }

    int read_u16le(byte[] arr, int ofs){
        int a = read_u8(arr, ofs + 0);
        int b = read_u8(arr, ofs + 1);
        return b * 256 + a;
    }

    @Override
    public void run() {
        try {
            addr = InetAddress.getByName(destinationAddress);

            socket = new Socket(addr, destinationPort);

            OutputStream out = socket.getOutputStream();
            InputStream input = socket.getInputStream();

            subscribe = new byte[5];
            subscribe[0] = 5;
            subscribe[1] = 0;
            subscribe[2] = 3;
            subscribe[3] = (byte) channel;
            subscribe[4] = 1;
            out.write(subscribe);

            int ofs = 0;
            byte[] buffer = new byte[32*1024];

            while (onRun){
                int len = input.available();
                if (len > 0){
                    input.read(buffer, ofs, len);
                    ofs += len;

                    if (ofs >= 2){
                        int num = read_u16le(buffer, 0);
                        if (ofs >= num){
                            int typ = buffer[2];

                            switch (typ){
                                case 1:
                                    subscribe = new byte[3];
                                    subscribe[0] = 3;
                                    subscribe[1] = 0;
                                    subscribe[2] = 1;
                                    out.write(subscribe);
                                    break;
                                case 2:
                                    subscribe = new byte[3];
                                    subscribe[0] = 3;
                                    subscribe[1] = 0;
                                    subscribe[2] = 2;
                                    out.write(subscribe);

                                    Log.i("Received", buffer.toString());

                                    int id = buffer[3];
                                    int w = read_u16le(buffer, 4);
                                    int h = read_u16le(buffer, 6);


                                    mainCanvas.update(w,h,buffer,8);
                                    frames++;

                                    image = BitmapFactory.decodeStream(input);

                                    mainCanvas.invalidate();
                                    break;
                            }

                            int remaining = ofs - num;
                            for (int i = 0; i < remaining; i++){
                                buffer[i] = buffer[i + num];
                            }
                            ofs = remaining;
                        }
                    }
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
