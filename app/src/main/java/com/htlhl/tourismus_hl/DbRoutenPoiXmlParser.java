package com.htlhl.tourismus_hl;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DbRoutenPoiXmlParser {
    // We don't use namespaces
    private static final String ns = null;

    List parse(InputStream in) throws XmlPullParserException, IOException {
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
        List routenpois = new ArrayList();
        parser.require(XmlPullParser.START_TAG, ns, "NewDataSet");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("routenpoi")) {
                routenpois.add(readRoutenPoi(parser));
            } else {
                skip(parser);
            }
        }
        return routenpois;
    }

    // Parses the contents of an entry. If it encounters a poiName, poiLng, or poiLat tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private DbRoutenPoiXmlContainer readRoutenPoi(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "routenpoi");
        int routenpoiID_=-1, routenpoiIDrouten_=-1, routenpoiIDpoi_=-1;


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "ID_":
                    routenpoiID_ = Integer.parseInt(readContent(parser, name));
                    break;
                case "IDrouten_":
                    routenpoiIDrouten_ = Integer.parseInt(readContent(parser, name));
                    break;
                case "IDpoi_":
                    routenpoiIDpoi_ = Integer.parseInt(readContent(parser, name));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new DbRoutenPoiXmlContainer(routenpoiID_, routenpoiIDrouten_, routenpoiIDpoi_);
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
