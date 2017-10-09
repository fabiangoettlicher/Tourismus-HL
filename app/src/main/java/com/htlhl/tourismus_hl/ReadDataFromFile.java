package com.htlhl.tourismus_hl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

class ReadDataFromFile {
    private static final String POI_FILENAME = "PoiFile.srl";
    private static final String ROUTEN_FILENAME = "RoutenFile.srl";
    private static final String ROUTENPOI_FILENAME = "RoutenPoiFile.srl";
    private static final String POI_FILENAME_TEXT = "PoiFileText.srl";
    private static final String ROUTEN_FILENAME_TEXT = "RoutenFileText.srl";
    private static final String ROUTENPOI_FILENAME_TEXT = "RoutenPoiFileText.srl";
    private static final String ROUTEN_FILENAME_KML = "RoutenFileKml.srl";


    static List<DbPoiXmlContainer> getDbPoiXmlContainerList(Context context) {
        List<DbPoiXmlContainer> dbPoiXmlContainerList = null;
        try {
            FileInputStream fis = context.openFileInput(POI_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            dbPoiXmlContainerList = (List<DbPoiXmlContainer>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dbPoiXmlContainerList;
    }

    static List<DbPoiXmlContainer> getDbPoiXmlContainerListText(Context context) {
        List<DbPoiXmlContainer> dbPoiXmlContainerList = null;
        try {
            FileInputStream fis = context.openFileInput(POI_FILENAME_TEXT);
            ObjectInputStream is = new ObjectInputStream(fis);
            dbPoiXmlContainerList = (List<DbPoiXmlContainer>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dbPoiXmlContainerList;
    }

    static List<DbRoutenXmlContainer> getDbRoutenXmlContainerList(Context context) {
        List<DbRoutenXmlContainer> dbRoutenXmlContainerList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTEN_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            dbRoutenXmlContainerList = (List<DbRoutenXmlContainer>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dbRoutenXmlContainerList;
    }

    static List<DbRoutenXmlContainer> getDbRoutenXmlContainerListKml(Context context) {
        List<DbRoutenXmlContainer> dbRoutenXmlContainerList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTEN_FILENAME_KML);
            ObjectInputStream is = new ObjectInputStream(fis);
            dbRoutenXmlContainerList = (List<DbRoutenXmlContainer>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dbRoutenXmlContainerList;
    }

    static List<DbRoutenXmlContainer> getDbRoutenXmlContainerListText(Context context) {
        List<DbRoutenXmlContainer> dbRoutenXmlContainerList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTEN_FILENAME_TEXT);
            ObjectInputStream is = new ObjectInputStream(fis);
            dbRoutenXmlContainerList = (List<DbRoutenXmlContainer>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dbRoutenXmlContainerList;
    }

    static List<DbRoutenPoiXmlContainer> getDbRoutenPoiXmlContainerList(Context context) {
        List<DbRoutenPoiXmlContainer> dbRoutenPoiXmlContainerList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTENPOI_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            dbRoutenPoiXmlContainerList = (List<DbRoutenPoiXmlContainer>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dbRoutenPoiXmlContainerList;
    }

    static List<DbRoutenPoiXmlContainer> getDbRoutenPoiXmlContainerListText(Context context) {
        List<DbRoutenPoiXmlContainer> dbRoutenPoiXmlContainerList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTENPOI_FILENAME_TEXT);
            ObjectInputStream is = new ObjectInputStream(fis);
            dbRoutenPoiXmlContainerList = (List<DbRoutenPoiXmlContainer>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dbRoutenPoiXmlContainerList;
    }

    static Bitmap getBitmap(String fullpath) {
        Bitmap bitmap = null;
        if (fullpath != null) {
            String[] splitfullpath = fullpath.split("/");
            String path = "";
            for (int i = 0; i < splitfullpath.length - 1; i++) {
                if (i != 0) {
                    path = path.concat("/");
                }
                path = path.concat(splitfullpath[i]);
            }
            String name = splitfullpath[splitfullpath.length - 1];
            try {
                File f = new File(path, name);
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    static void getKml(String fullpath, GoogleMap map, Context context) throws IOException {
        InputStream is = null;
        KmlLayer layer = null;
        if (fullpath != null) {
            String[] splitfullpath = fullpath.split("/");
            String path = "";
            for (int i = 0; i < splitfullpath.length - 1; i++) {
                if (i != 0) {
                    path = path.concat("/");
                }
                path = path.concat(splitfullpath[i]);
            }
            String name = splitfullpath[splitfullpath.length - 1];
            try {
                File f = new File(path, name);
                is = new FileInputStream(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (is != null) {
                try {
                    layer = new KmlLayer(map, is, context);
                    layer.addLayerToMap();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
