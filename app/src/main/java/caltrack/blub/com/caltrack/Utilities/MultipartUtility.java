package caltrack.blub.com.caltrack.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import caltrack.blub.com.caltrack.DashboardActivity;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 *
 * @author www.codejava.net
 * @author Cloudinary
 */
public class MultipartUtility {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    /**
     * This constructor initializes a new HTTP POST request with content type is
     * set to multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public MultipartUtility(String requestURL, String charset, String boundary) throws IOException {
        this.charset = charset;
        this.boundary = boundary;

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }

    /**
     * Adds a form field to the request
     *
     * @param name
     *            field name
     * @param value
     *            field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned status
     *         OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String execute() throws IOException {
        StringBuilder buf;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;

        writer.append("--" + boundary).append(LINE_FEED);
        writer.close();

        inputStream = httpConn.getInputStream();


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
        return buf.toString();
    }
}