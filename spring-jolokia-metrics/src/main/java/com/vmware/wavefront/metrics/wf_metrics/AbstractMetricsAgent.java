package com.vmware.wavefront.metrics.wf_metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMetricsAgent extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMetricsAgent.class);
    protected JSONParser parser = new JSONParser();

    protected String getAPI(String baseUrl, String token, String apiURI) throws Exception {
        // first, create URL
        URL myUrl = new URL(baseUrl + apiURI);
        HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (token != null)
            conn.setRequestProperty("Authorization", "Bearer " + token);

        StringBuffer result = new StringBuffer();

        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String inputLine;

        while ((inputLine = br.readLine()) != null) {
            result.append(inputLine);
        }

        logger.debug("[GET]" + result.toString());

        br.close();
        conn.disconnect();
        return result.toString();
    }

    // post messages - have data payload as string.
    protected String postAPI(String baseUrl, String apiURI, String token, String json) throws Exception {
        // first, create URL
        URL myUrl = new URL(baseUrl + apiURI);
        HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");

        if (token != null)
            conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        conn.setDoInput(true);

        // post message
        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);

        logger.debug("json data for POST: " + apiURI + " : " + json);

        bw.write(json);
        bw.flush();

        StringBuffer result = new StringBuffer();
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String inputLine;

        while ((inputLine = br.readLine()) != null) {
            result.append(inputLine);
        }

        br.close();
        conn.disconnect();

        logger.debug("[POST]" + result.toString());

        return result.toString();
    }

    // post messages - have data payload as string.
    protected String putAPI(String baseUrl, String apiURI, String token, String json) throws Exception {
        // first, create URL
        URL myUrl = new URL(baseUrl + apiURI);
        HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");

        if (token != null)
            conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        conn.setDoInput(true);

        // post message
        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);

        logger.debug("json data for PUT: " + apiURI + " : " + json);

        bw.write(json);
        bw.flush();

        StringBuffer result = new StringBuffer();
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String inputLine;

        while ((inputLine = br.readLine()) != null) {
            result.append(inputLine);
        }

        br.close();
        conn.disconnect();

        logger.debug("[PUT]" + result.toString());

        return result.toString();
    }

    protected String deleteAPI(String baseUrl, String apiURI, String token) throws Exception {
        // first, create URL
        URL myUrl = new URL(baseUrl + apiURI);
        HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/json");

        if (token != null)
            conn.setRequestProperty("Authorization", "Bearer " + token);

        StringBuffer result = new StringBuffer();

        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String inputLine;

        while ((inputLine = br.readLine()) != null) {
            result.append(inputLine);
        }

        br.close();
        conn.disconnect();

        logger.debug("[DELETE]" + result.toString());

        return result.toString();
    }
}
