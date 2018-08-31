package caltrack.blub.com.caltrack.Utilities;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import caltrack.blub.com.caltrack.Utilities.Constants;

public class HttpRequests
{
    // HTTP GET request
    public static String sendGet(String urlString)
    {
        try
        {
            String url = urlString;

            URL obj = new URL(url);
            HttpURLConnection con = null;

            con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();

            return response.toString();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return "failed to connect";
    }

    public static String sendPost(String urlString, String query)
    {
        URL url;
        OutputStream outputStream;
        OutputStreamWriter writer;
        StringBuilder buf;
        HttpURLConnection urlConnection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;

        try
        {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            outputStream = urlConnection.getOutputStream();
            writer = new OutputStreamWriter(outputStream);

            writer.write(query);

            writer.flush();
            writer.close();
            outputStream.close();
            Log.e("testlog", query);

            inputStream = urlConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            int b;
            buf = new StringBuilder();
            while ((b = bufferedReader.read()) != -1)
            {
                buf.append((char) b);
            }

            inputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
            urlConnection.disconnect();

            return buf.toString();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return "failed to connect";
    }

    public static int loginRequest(String username, String password)
    {
        HttpURLConnection urlConnection;

        try
        {
            URL url = new URL(Constants.LOGIN);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            String authString = username + ":" + password;
            String authEncBytes = Base64.encodeToString(authString.getBytes(), 1);
            urlConnection.setRequestProperty("Authorization", "Basic " + authEncBytes);

            urlConnection.disconnect();

            return urlConnection.getResponseCode();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return 502;
    }
}
