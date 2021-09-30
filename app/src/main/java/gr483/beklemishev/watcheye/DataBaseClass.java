package gr483.beklemishev.watcheye;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DataBaseClass extends SQLiteOpenHelper {

    public DataBaseClass(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE NetworkSettings (id INT, title TXT, address TXT, port INT);";
        db.execSQL(sql);
    }

    public int getMaxIDForNetworkSettings(){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT Max(id) FROM NetworkSettings";
        Cursor cur = db.rawQuery(sql, null);

        if (cur.moveToFirst()){
            int maxid = cur.getInt(0);
            cur.close();
            return maxid;
        }

        cur.close();
        return 0;
    }

    public void addNetworkSettings (int id, String title, String address, int port)
    {
        String sid = String.valueOf(id);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO NetworkSettings VALUES ("+sid+", '"+title+"', '"+address+"', "+port+");";
        db.execSQL(sql);
    }

    public void getAllNetworkSettings(ArrayList<NetworkSettings> lst)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, Title, Address, Port FROM NetworkSettings;";
        Cursor cur = db.rawQuery(sql,null);
        if(cur.moveToFirst()){
            do {
                NetworkSettings n = new NetworkSettings();
                n.id = cur.getInt(0);
                n.Title = cur.getString(1);
                n.Address = cur.getString(2);
                n.Port = cur.getInt(3);
                lst.add(n);
            } while (cur.moveToNext());
        }
        cur.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
