package com.liceu.tickets;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


class ServerError extends Exception {
};


interface LiceuServer {
    public void init(String u, String p);

    public String readJson(String url) throws ServerError;

    public void doPost(String url, Map<String, String> vars) throws ServerError;
}


class LiceuServerImp implements LiceuServer {
    static String TAG = "LiceuServer";
    String username = null;
    String password = null;
    DefaultHttpClient client = null;
    Date lastDate = null;

    // Inicialitza Server amb usuari i password
    public void init(String u, String p) {
        username = u;
        password = p;
        client = null;
    }

    // Fa un get i retorna un json en format raw
    public String readJson(String url) throws ServerError {
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = executeRequest(httpGet);
            return getString(response.getEntity());

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new ServerError();
    }

    // Fa un post. Amolla una excepció si hi ha problemes
    public void doPost(String url, Map<String, String> vars) throws ServerError {
        try {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            for (Map.Entry<String, String> e : vars.entrySet())
                nvps.add(new BasicNameValuePair((String) e.getKey(), (String) e.getValue()));

            HttpPost httpost = new HttpPost(url);
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            HttpResponse response = executeRequest(httpost);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ServerError();
    }

    // Retorna el httpclient. Si no està establerta la connexió, l'estableix
    private DefaultHttpClient getHttpClient() throws ServerError {
        // Primer comprovem si ja tenim el client en caché
        // També si ha passat manco de mitja hora des de la darrera vegada.
        if (client != null) {
            Date now = new Date();
            Long dif = now.getTime() - lastDate.getTime();
            Log.v(TAG, "Client object exists. Lastdate=" + String.valueOf(dif));
            if (dif < 1800000) {
                Log.v(TAG, "Client is valid. Returning from cache");
                lastDate = now;
                return client;
            }
        }

        // La idea és la següent:
        //
        // Primer fem un get a https://apps.esliceu.com/auth/login
        // Això ens serveix per emmagazemar les cookies. N'hi ha dues: sessionid i csrftoken
        // Després, hem de fer un post a https://apps.esliceu.com/auth/login, transmitint també les cookies anteriors
        // Dins aquest post hem d'especificar les variables "username", "password" i "csrfmiddlewaretoken"
        // Aquesta darrera variable té el mateix valor que el nom de la cookie csrftoken

        Log.v(TAG, "Beginning authorization...");

        DefaultHttpClient myclient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet("https://apps.esliceu.com/auth/login/");
        try {
            HttpResponse response = myclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            response.getEntity().consumeContent();

            if (statusLine.getStatusCode() != 200)
                throw new ServerError();

            Cookie ck = getCookie(myclient, "csrftoken");
            if (ck == null) throw new ServerError();
            String token = ck.getValue();
            Log.v(TAG, "csr: " + token);
            ck = getCookie(myclient, "sessionid");
            if (ck == null) throw new ServerError();
            String sid = ck.getValue();
            Log.v(TAG, "sessionid: " + sid);

            HttpPost httpost = new HttpPost("https://apps.esliceu.com/auth/login/");
            httpost.addHeader("Referer", "https://apps.esliceu.com/auth/login/");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("username", username));
            nvps.add(new BasicNameValuePair("password", password));
            nvps.add(new BasicNameValuePair("csrfmiddlewaretoken", token));
            httpost.setEntity(new UrlEncodedFormEntity(nvps));
            response = myclient.execute(httpost);
            response.getEntity().consumeContent();
            Log.v(TAG, "Status: " + String.valueOf(response.getStatusLine().getStatusCode()));

            if (response.getStatusLine().getStatusCode() == 200) {
                // Comprovem que la sessionid ha canviat
                // Si no ha canviat és que no ha anat bé l'autorització
                Cookie ck2 = getCookie(myclient, "sessionid");
                if (ck2 == null) throw new ServerError();
                if (ck2.getValue().equals(sid)) throw new ServerError();

                Log.v(TAG, "AUTHORIZED!");
                client = myclient;
                lastDate = new Date();
                return myclient;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ServerError();
    }

    // Obté cookie a partir d'un nom del client http especificat
    private Cookie getCookie(DefaultHttpClient cl, String name) {
        List<Cookie> cookies = cl.getCookieStore().getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie c = cookies.get(i);
            if (c.getName().equals(name)) return c;
        }
        return null;
    }

    // Fa el get o el post, i torna el HttpResponse si tot va bé.
    // Si no, amolla excepció ServerError
    private HttpResponse executeRequest(HttpRequestBase request) throws IOException, ServerError {
        DefaultHttpClient myclient = getHttpClient();
        HttpResponse response = myclient.execute(request);
        int code = response.getStatusLine().getStatusCode();
        if (code == 200)
            return response;
        else
            throw new ServerError();
    }

    private String getString(HttpEntity e) throws ClientProtocolException, IOException {
        StringBuilder builder = new StringBuilder();
        InputStream content = e.getContent();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

}
