package com.htlhl.tourismus_hl.Data.XmlParser;

import android.util.Xml;

import com.htlhl.tourismus_hl.Data.Model.PointOfInterest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class DbPoiXmlParser {

    // We don't use namespaces
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readNewDataSet(parser);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private List readNewDataSet(XmlPullParser parser) throws XmlPullParserException, IOException {
        List pois = new ArrayList();
        parser.require(XmlPullParser.START_TAG, ns, "NewDataSet");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("poi")) {
                pois.add(readPoi(parser));
            } else {
                skip(parser);
            }
        }
        return pois;
    }

    // Parses the contents of an entry. If it encounters a poiName, poiLng, or poiLat tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private PointOfInterest readPoi(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "poi");
        String poiName_ = null, poiLat_ = null, poiLng_ = null, poiTextDE_ = null, poiTextEN_ = null, poiTextCZ_ = null, poiStat_ = null;
        String poiOffenDE_ = null, poiOffenEN_ = null, poiOffenCZ_ = null, poiAudioDE_ = null, poiAudioEN_ = null, poiAudioCZ_ = null;
        String poiLogo1_ = null, poiLogo2_ = null, poiLogo3_ = null, poiLogo4_ = null, poiLogo5_ = null, poiLogo6_ = null, poiBild_ = null;
        String poiKontakt1_ = null, poiKontakt2_ = null, poiKontakt3_ = null, poiKontakt4_ = null, poiKontakt5_ = null;
        int poiID_ = -1, poiKatID_ = -1;


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "Name_":
                    poiName_ = readContent(parser, name);
                    break;
                case "Lat_":
                    poiLat_ = readContent(parser, name);
                    break;
                case "Lng_":
                    poiLng_ = readContent(parser, name);
                    break;
                case "TextDE_":
                    poiTextDE_ = readContent(parser, name);
                    break;
                case "TextEN_":
                    poiTextEN_ = readContent(parser, name);
                    break;
                case "TextCZ_":
                    poiTextCZ_ = readContent(parser, name);
                    break;
                case "OffenDE_":
                    poiOffenDE_ = readContent(parser, name);
                    break;
                case "OffenEN_":
                    poiOffenEN_ = readContent(parser, name);
                    break;
                case "OffenCZ_":
                    poiOffenCZ_ = readContent(parser, name);
                    break;
                case "Logo1_":
                    poiLogo1_ = readContent(parser, name);
                    break;
                case "Logo2_":
                    poiLogo2_ = readContent(parser, name);
                    break;
                case "Logo3_":
                    poiLogo3_ = readContent(parser, name);
                    break;
                case "Logo4_":
                    poiLogo4_ = readContent(parser, name);
                    break;
                case "Logo5_":
                    poiLogo5_ = readContent(parser, name);
                    break;
                case "Logo6_":
                    poiLogo6_ = readContent(parser, name);
                    break;
                case "Bild_":
                    poiBild_ = readContent(parser, name);
                    break;
                case "AudioDE_":
                    poiAudioDE_ = readContent(parser, name);
                    break;
                case "AudioEN_":
                    poiAudioEN_ = readContent(parser, name);
                    break;
                case "AudioCZ_":
                    poiAudioCZ_ = readContent(parser, name);
                    break;
                case "Stat_":
                    poiStat_ = readContent(parser, name);
                    break;
                case "Kontakt1_":
                    poiKontakt1_ = readContent(parser, name);
                    break;
                case "Kontakt2_":
                    poiKontakt2_ = readContent(parser, name);
                    break;
                case "Kontakt3_":
                    poiKontakt3_ = readContent(parser, name);
                    break;
                case "Kontakt4_":
                    poiKontakt4_ = readContent(parser, name);
                    break;
                case "Kontakt5_":
                    poiKontakt5_ = readContent(parser, name);
                    break;
                case "ID_":
                    poiID_ = Integer.parseInt(readContent(parser, name));
                    break;
                case "KatID_":
                    poiKatID_ = Integer.parseInt(readContent(parser, name));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new PointOfInterest(poiName_, poiLat_, poiLng_, poiTextDE_, poiTextEN_, poiTextCZ_, poiStat_,
                poiLogo1_, poiLogo2_, poiLogo3_, poiLogo4_, poiLogo5_, poiLogo6_, poiBild_,
                poiKontakt1_, poiKontakt2_, poiKontakt3_, poiKontakt4_, poiKontakt5_,
                poiOffenDE_, poiOffenEN_, poiOffenCZ_, poiAudioDE_, poiAudioEN_, poiAudioCZ_, poiKatID_, poiID_);
    }

    // Processes title tags in the feed.
    private String readContent(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return title;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}

