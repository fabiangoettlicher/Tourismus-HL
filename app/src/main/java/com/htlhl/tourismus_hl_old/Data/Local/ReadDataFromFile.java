package com.htlhl.tourismus_hl_old.Data.Local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.kml.KmlLayer;
import com.htlhl.tourismus_hl_old.Data.Model.PointOfInterest;
import com.htlhl.tourismus_hl_old.Data.Model.Routen;
import com.htlhl.tourismus_hl_old.Data.Model.RoutenPointOfInterestLinking;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class ReadDataFromFile {
    private static final String POI_FILENAME = "PoiFile.srl";
    private static final String ROUTEN_FILENAME = "RoutenFile.srl";
    private static final String ROUTENPOI_FILENAME = "RoutenPoiFile.srl";
    private static final String POI_FILENAME_TEXT = "PoiFileText.srl";
    private static final String ROUTEN_FILENAME_TEXT = "RoutenFileText.srl";
    private static final String ROUTENPOI_FILENAME_TEXT = "RoutenPoiFileText.srl";
    private static final String ROUTEN_FILENAME_KML = "RoutenFileKml.srl";


    public static List<PointOfInterest> getDbPoiXmlContainerList(Context context) {
        List<PointOfInterest> PointOfInterestList = null;
        try {
            FileInputStream fis = context.openFileInput(POI_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            PointOfInterestList = (List<PointOfInterest>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return PointOfInterestList;
    }

    public static List<PointOfInterest> getDbPoiXmlContainerListText(Context context) {
        List<PointOfInterest> PointOfInterestList = null;
        try {
            FileInputStream fis = context.openFileInput(POI_FILENAME_TEXT);
            ObjectInputStream is = new ObjectInputStream(fis);
            PointOfInterestList = (List<PointOfInterest>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return PointOfInterestList;
    }

    public static List<Routen> getDbRoutenXmlContainerList(Context context) {
        List<Routen> routenList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTEN_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            routenList = (List<Routen>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return routenList;
    }

    public static List<Routen> getDbRoutenXmlContainerListKml(Context context) {
        List<Routen> routenList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTEN_FILENAME_KML);
            ObjectInputStream is = new ObjectInputStream(fis);
            routenList = (List<Routen>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return routenList;
    }

    public static List<Routen> getDbRoutenXmlContainerListText(Context context) {
        List<Routen> routenList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTEN_FILENAME_TEXT);
            ObjectInputStream is = new ObjectInputStream(fis);
            routenList = (List<Routen>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return routenList;
    }

    public static List<RoutenPointOfInterestLinking> getDbRoutenPoiXmlContainerList(Context context) {
        List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTENPOI_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            routenPointOfInterestLinkingList = (List<RoutenPointOfInterestLinking>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return routenPointOfInterestLinkingList;
    }

    public static List<RoutenPointOfInterestLinking> getDbRoutenPoiXmlContainerListText(Context context) {
        List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList = null;
        try {
            FileInputStream fis = context.openFileInput(ROUTENPOI_FILENAME_TEXT);
            ObjectInputStream is = new ObjectInputStream(fis);
            routenPointOfInterestLinkingList = (List<RoutenPointOfInterestLinking>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return routenPointOfInterestLinkingList;
    }

    public static Bitmap getBitmap(String fullpath) {
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

    public static void getKml(String fullpath, GoogleMap map, Context context) throws IOException {
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
