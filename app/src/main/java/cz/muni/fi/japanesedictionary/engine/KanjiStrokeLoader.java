package cz.muni.fi.japanesedictionary.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import cz.muni.fi.japanesedictionary.Const;
import cz.muni.fi.japanesedictionary.interfaces.KanjiVgCallback;
import cz.muni.fi.japanesedictionary.parser.ParserService;

/**
 * KanjiStrokeLoader
 */
public class KanjiStrokeLoader extends AsyncTask<String, Void, List<SVG>> {
    private static final String LOG_TAG = "KanjiStrokeLoader";
    private Context mContext;
    private KanjiVgCallback mCallback;

    public KanjiStrokeLoader(Context context, KanjiVgCallback callback){
        if( context == null ) {
            throw new IllegalArgumentException("context");
        }
        if( callback == null ) {
            throw new IllegalArgumentException("callback");
        }
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected List<SVG> doInBackground(String... params) {
        if(params == null || params.length != 1 || params[0] == null){
            throw new IllegalArgumentException("wrong aprameter kanji");
        }
        String kanji = params[0];
        String unicode = "0" + Integer.toHexString(kanji.charAt(0) | 0x10000).substring(1);
        SharedPreferences pref = mContext.getSharedPreferences(
                ParserService.DICTIONARY_PREFERENCES, 0);
        String kanjivgFolder = pref.getString(Const.PREF_KANJIVG_PATH, null);
        if(kanjivgFolder == null){
            Log.d(LOG_TAG, "folder null");
            return null;
        }
        Log.d(LOG_TAG, "unicode file: " + unicode);
        File file = new File(kanjivgFolder, unicode + ".svg");
        if(!file.exists()){
            return null;
        }
        try {
            List<SVG> svgList = new ArrayList<>();
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware(false);
            fac.setValidating(false);
            fac.setFeature("http://xml.org/sax/features/namespaces", false);
            fac.setFeature("http://xml.org/sax/features/validation", false);

            DocumentBuilder builder = fac.newDocumentBuilder();
            Document doc = builder.parse(file);
            NodeList pathList = doc.getElementsByTagName("path");
            NodeList textList = doc.getElementsByTagName("text");
            for(int i = pathList.getLength(); i > 0; i--){
                int j = i - 1;
                SVG svg = SVG.getFromString(convertDocumentToString(doc));
                svgList.add(svg);
                if(j < 0){
                    break;
                }

                Node actualPath = pathList.item(j);
                actualPath.getParentNode().removeChild(actualPath);
                if(textList.getLength() > j){
                    Node actualText = textList.item(j);
                    actualText.getParentNode().removeChild(actualText);
                }
            }
            Collections.reverse(svgList);
            return svgList;
        } catch (IOException | SVGParseException | ParserConfigurationException | SAXException | TransformerException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<SVG> svgs) {
        mCallback.kanjiVgLoaded(svgs);
    }

    private static String convertDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }
}
