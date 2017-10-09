package com.htlhl.tourismus_hl;


import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DbRoutenXmlParser {

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
        List routen = new ArrayList();
        parser.require(XmlPullParser.START_TAG, ns, "NewDataSet");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("routen")) {
                routen.add(readRouten(parser));
            } else {
                skip(parser);
            }
        }
        return routen;
    }

    // Parses the contents of an entry. If it encounters a poiName, poiLng, or poiLat tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private DbRoutenXmlContainer readRouten(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "routen");
        String routenName_=null, routenKml_=null, routenLink_=null, routenBild1_=null, routenBild2_=null, routenBild3_=null;
        String routenInfo1DE_=null, routenInfo2DE_=null, routenInfo3DE_=null, routenInfo4DE_=null, routenInfo5DE_=null, routenInfo6DE_=null, routenInfo7DE_=null;
        String routenInfo1EN_=null, routenInfo2EN_=null, routenInfo3EN_=null, routenInfo4EN_=null, routenInfo5EN_=null, routenInfo6EN_=null, routenInfo7EN_=null;
        String routenInfo1CZ_=null, routenInfo2CZ_=null, routenInfo3CZ_=null, routenInfo4CZ_=null, routenInfo5CZ_=null, routenInfo6CZ_=null, routenInfo7CZ_=null;
        int routenID_=-1, routenKatID_=-1;


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "Name_":
                    routenName_ = readContent(parser, name);
                    break;
                case "Kml_":
                    routenKml_ = readContent(parser, name);
                    break;
                case "Link_":
                    routenLink_ = readContent(parser, name);
                    break;
                case "Bild1_":
                    routenBild1_ = readContent(parser, name);
                    break;
                case "Bild2_":
                    routenBild2_ = readContent(parser, name);
                    break;
                case "Bild3_":
                    routenBild3_ = readContent(parser, name);
                    break;
                case "Info1DE_":
                    routenInfo1DE_ = readContent(parser, name);
                    break;
                case "Info2DE_":
                    routenInfo2DE_ = readContent(parser, name);
                    break;
                case "Info3DE_":
                    routenInfo3DE_ = readContent(parser, name);
                    break;
                case "Info4DE_":
                    routenInfo4DE_ = readContent(parser, name);
                    break;
                case "Info5DE_":
                    routenInfo5DE_ = readContent(parser, name);
                    break;
                case "Info6DE_":
                    routenInfo6DE_ = readContent(parser, name);
                    break;
                case "Info7DE_":
                    routenInfo7DE_ = readContent(parser, name);
                    break;
                case "Info1EN_":
                    routenInfo1EN_ = readContent(parser, name);
                    break;
                case "Info2EN_":
                    routenInfo2EN_ = readContent(parser, name);
                    break;
                case "Info3EN_":
                    routenInfo3EN_ = readContent(parser, name);
                    break;
                case "Info4EN_":
                    routenInfo4EN_ = readContent(parser, name);
                    break;
                case "Info5EN_":
                    routenInfo5EN_ = readContent(parser, name);
                    break;
                case "Info6EN_":
                    routenInfo6EN_ = readContent(parser, name);
                    break;
                case "Info7EN_":
                    routenInfo7EN_ = readContent(parser, name);
                    break;
                case "Info1CZ_":
                    routenInfo1CZ_ = readContent(parser, name);
                    break;
                case "Info2CZ_":
                    routenInfo2CZ_ = readContent(parser, name);
                    break;
                case "Info3CZ_":
                    routenInfo3CZ_ = readContent(parser, name);
                    break;
                case "Info4CZ_":
                    routenInfo4CZ_ = readContent(parser, name);
                    break;
                case "Info5CZ_":
                    routenInfo5CZ_ = readContent(parser, name);
                    break;
                case "Info6CZ_":
                    routenInfo6CZ_ = readContent(parser, name);
                    break;
                case "Info7CZ_":
                    routenInfo7CZ_ = readContent(parser, name);
                    break;
                case "ID_":
                    routenID_ = Integer.parseInt(readContent(parser, name));
                    break;
                case "KatID_":
                    routenKatID_ = Integer.parseInt(readContent(parser, name));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new DbRoutenXmlContainer(routenName_, routenKml_, routenLink_, routenBild1_, routenBild2_, routenBild3_,
                routenInfo1DE_, routenInfo2DE_, routenInfo3DE_, routenInfo4DE_, routenInfo5DE_, routenInfo6DE_, routenInfo7DE_,
                routenInfo1EN_, routenInfo2EN_, routenInfo3EN_, routenInfo4EN_, routenInfo5EN_, routenInfo6EN_, routenInfo7EN_,
                routenInfo1CZ_, routenInfo2CZ_, routenInfo3CZ_, routenInfo4CZ_, routenInfo5CZ_, routenInfo6CZ_, routenInfo7CZ_,
                routenID_, routenKatID_);
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
