package org.keycloak.quickstart.appjee;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class IdpTokenUtil {

    private static String IDB_BROKER_URL = "http://localhost:8180/auth/realms/idbrealm/broker/idp-sso-provider/token";
    private static String IDP_TOKEN_URL = "http://localhost:8380/auth/realms/idprealm/protocol/openid-connect/token";
    private static String IDB_CLIENT_SECRET = System.getProperty("idb.client.secret");

    public static String getAccessToken(String idbAccessToken, HttpSession httpSession) throws Exception {
        String accessToken = (String) httpSession.getAttribute("access_token");
        if (accessToken == null) {
            String tokens = httpGet(IDB_BROKER_URL, idbAccessToken);
            accessToken = setTokensInHttpSession(tokens, httpSession);
        }
        else if (isAccessTokenExpired(accessToken)) {
            String refreshToken = (String) httpSession.getAttribute("refresh_token");
            String tokens = httpPost(IDP_TOKEN_URL, refreshToken);
            accessToken = setTokensInHttpSession(tokens, httpSession);
        }
        return accessToken;
    }

    private static boolean isAccessTokenExpired(String accessToken) throws Exception {
        String decodedAccessToken = new String(Base64.decodeBase64(accessToken.split("\\.")[1]));
        long exp = Long.parseLong(jsonGetInt(decodedAccessToken, "exp"));
        long currentSecs = new Date().getTime() / 1000;
        return (exp - 10) < currentSecs;
    }

    private static String setTokensInHttpSession(String tokens, HttpSession httpSession) {
        String accessToken = jsonGetString(tokens, "access_token");
        httpSession.setAttribute("access_token", accessToken);
        String refreshToken = jsonGetString(tokens, "refresh_token");
        httpSession.setAttribute("refresh_token", refreshToken);
        String idToken = jsonGetString(tokens, "id_token");
        httpSession.setAttribute("id_token", idToken);
        return accessToken;
    }

    // TODO: Avoid using regex
    private static String jsonGetString(String json, String key) {
        String value = json.replaceFirst(".*\"" + key + "\"\\s*:\\s*\"([^\"]+?)\".*", "$1");
        return value;
    }

    // TODO: Avoid using regex
    private static String jsonGetInt(String json, String key) {
        String value = json.replaceFirst(".*\"" + key + "\"\\s*:\\s*(\\d+).*", "$1");
        return value;
    }

    // TODO: Verify the token signature
    private static String httpGet(String url, String accessToken) throws Exception {
        String jsonBody = null;
        CloseableHttpClient client = null;
        try {
            HttpGet get = new HttpGet(url);
            get.addHeader("Authorization", "Bearer " + accessToken);

            System.out.println("Executing: " + get.getRequestLine());
            client = HttpClients.createDefault();
            HttpResponse response = client.execute(get);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new Exception(status.getStatusCode() + ":" + status.getReasonPhrase());
            }
            jsonBody = EntityUtils.toString(response.getEntity());
        }
        finally {
            if (client != null)
                client.close();
        }
        return jsonBody;
    }

    // TODO: Verify the token signature
    private static String httpPost(String url, String refreshToken) throws Exception {
        String jsonBody = null;
        CloseableHttpClient client = null;
        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("grant_type", "refresh_token"));
            form.add(new BasicNameValuePair("refresh_token", refreshToken));
            form.add(new BasicNameValuePair("client_id", "idb-sso-broker"));
            form.add(new BasicNameValuePair("client_secret", IDB_CLIENT_SECRET));
            post.setEntity(new UrlEncodedFormEntity(form, Consts.UTF_8));

            System.out.println("Executing: " + post.getRequestLine());
            client = HttpClients.createDefault();
            HttpResponse response = client.execute(post);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new Exception(status.getStatusCode() + ":" + status.getReasonPhrase());
            }
            jsonBody = EntityUtils.toString(response.getEntity());
        }
        finally {
            if (client != null)
                client.close();
        }
        return jsonBody;
    }
}
