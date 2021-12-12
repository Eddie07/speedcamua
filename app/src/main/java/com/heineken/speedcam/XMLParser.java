package com.heineken.speedcam;

import android.content.Context;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

public class XMLParser {


    private Context mContext;

    public XMLParser(Context context) {
        this.mContext = context;
    }

    public Vector readXML() throws XmlPullParserException, IOException {

        FileInputStream xmlStream = new FileInputStream(mContext.getFilesDir() + "/maps.xml");
        try {

            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(xmlStream);
            NodeList coordList, name;


            coordList = document.getElementsByTagName("coordinates");
            name = document.getElementsByTagName("name");
            Vector<Object> positions = new Vector<>();
            for (int i = 0; i < coordList.getLength(); i++) {
                String[] coordinatePairs = coordList.item(i).getFirstChild().getNodeValue().trim().split(" ");
                String[] names = name.item(i + 2).getFirstChild().getNodeValue().trim().split("  ");


                for (String coord : coordinatePairs) {
                    for (String namess : names) {
                        //Log.e("XML", namess);
                        positions.add(namess);
                        positions.add(Double.parseDouble(coord.split(",")[0]));
                        positions.add(Double.parseDouble(coord.split(",")[1]));
                        positions.size();
                      //  Log.e("XML", String.valueOf(positions.size()));
                      //  Log.e("XML", String.valueOf(Double.parseDouble(coord.split(",")[1])));
                       //   Log.e("XML", String.valueOf(Double.parseDouble(coord.split(",")[0])));
                    }
                }

                // border_fragment.add(positions);
            }
            return positions;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }

    }
}








