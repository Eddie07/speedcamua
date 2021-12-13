package com.heineken.speedcam;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
    import android.util.Log;

import java.util.Vector;


public class myDbAdapter {
        myDbHelper myhelper;
      public myDbAdapter(Context context)
      {
           myhelper = new myDbHelper(context);
       }

        private long insertData(String name, Float x, Float y)
        {
            //Log.d ("SQL", "Error");
            SQLiteDatabase dbb = myhelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(myDbHelper.NAME, name);
            contentValues.put(myDbHelper.X, x);
            contentValues.put(myDbHelper.Y, y);
            long id = dbb.insert(myDbHelper.TABLE_NAME, null , contentValues);
            return id;
        }

        public Vector getData()
        {
            SQLiteDatabase db = myhelper.getWritableDatabase();
            String[] columns = {myDbHelper.UID,myDbHelper.NAME,myDbHelper.X,myDbHelper.Y};
            Cursor cursor =db.query(myDbHelper.TABLE_NAME,columns,null,null,null,null,null);
            Vector<Object> buffer= new Vector();

            while (cursor.moveToNext())
            {
                int cid =cursor.getInt(cursor.getColumnIndex(myDbHelper.UID));
                buffer.add (cursor.getString(cursor.getColumnIndex(myDbHelper.NAME)));
                buffer.add (cursor.getFloat(cursor.getColumnIndex(myDbHelper.X)));
                buffer.add (cursor.getFloat(cursor.getColumnIndex(myDbHelper.Y)));
                //buffer.add(cid+ "   " + name + "   " + x + " " + y + " \n");
            }
            db.close();
            return buffer;
        }

        private int delete(String uname)
        {
            SQLiteDatabase db = myhelper.getWritableDatabase();
            String[] whereArgs ={uname};

            int count =db.delete(myDbHelper.TABLE_NAME ,myDbHelper.NAME+" = ?",whereArgs);
            return  count;
        }
    private void deleteDB ()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();

        db.execSQL(myDbHelper.DROP_TABLE);
        db.execSQL(myDbHelper.CREATE_TABLE);
    }



        public int updateName(String oldName , String newName)
        {
            SQLiteDatabase db = myhelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(myDbHelper.NAME,newName);
            String[] whereArgs= {oldName};
            int count =db.update(myDbHelper.TABLE_NAME,contentValues, myDbHelper.NAME+" = ?",whereArgs );
            return count;
        }

        static class myDbHelper extends SQLiteOpenHelper
        {
            private static final String DATABASE_NAME = "CamBaseUA.db";    // Database Name
            private static final String TABLE_NAME = "myTable";   // Table Name
            private static final int DATABASE_Version = 1;   // Database Version
            private static final String UID="_id";     // Column I (Primary Key)
            private static final String NAME = "Description";    //Column II
            private static final String X= "X";    // Column III
            private static final String Y= "Y";    // Column III
            private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                    " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+NAME+" VARCHAR(255) ,"+X+" float,"+Y+" float);";
            private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
            private Context context;

            public myDbHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_Version);
                this.context=context;
            }

            public void onCreate(SQLiteDatabase db) {

                try {
                    db.execSQL(CREATE_TABLE);
                } catch (Exception e) {
                    Log.d ("SQL", "Error");
                }
            }


            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                try {
                    Log.d ("SQL", "OnUpgrade");
                    db.execSQL(DROP_TABLE);
                    onCreate(db);
                }catch (Exception e) {
                    Log.d ("SQL", "Error");
                }
            }
        }
    public void addCamera( String name, Float x, Float y)
    {
        long id=insertData(name,x,y);
       // getData();
        if(id<=0)
        {   Log.d ("SQL", "Error");

        } else
        {
            Log.d ("SQL", "OK");
        }
       // Log.d ("SQL",getData());
    }
    public void deleteDBase()
        {
        deleteDB();

        }

    }

