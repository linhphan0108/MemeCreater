package uit.linh.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 *
 * Created by linh on 10/06/2015.
 */
public class JsonReader {
    public static String read(BufferedReader reader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int cp;
        while ((cp = reader.read()) != -1){
            stringBuilder.append((char)cp);
        }
        return stringBuilder.toString();
    }

    public static JSONObject downloadJson(String url) throws IOException, JSONException {
            InputStream in = new URL(url).openStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String jsonText = read(bufferedReader);

            in.close();
            inputStreamReader.close();
            bufferedReader.close();

            return new JSONObject(jsonText);
    }
}
