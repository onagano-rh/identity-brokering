package org.keycloak.quickstart.appjee;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class IdpTokenUtil {

    private static String BROKER_URL = "http://localhost:8180/auth/realms/idbrealm/broker/idp-sso-provider/token";

    // IdB access token -> Stored IdP tokens
    // TODO: Store in HttpSession
    private static ConcurrentMap<String, String> tokenMap = new ConcurrentHashMap();

    public static String getAccessToken(String idbAccessToken) throws Exception {
        //System.out.println("idbAccessToken: " + idbAccessToken);
        String idpTokens = tokenMap.computeIfAbsent(idbAccessToken, (k) -> {
                String jsonBody = null;
                try {
                    jsonBody = httpGet(BROKER_URL, k);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return jsonBody;
            });
        //System.out.println("idpTokens: " + idpTokens);
        String idpAccessToken = extractAccessToken(idpTokens);
        //System.out.println("idpAccessToken: " + idpAccessToken);
        return idpAccessToken;
    }

    // TODO: Call this when logout
    public static void invalidateTokens(String idbAccessToken) {
        if (idbAccessToken != null)
            tokenMap.remove(idbAccessToken);
    }

    private static String extractAccessToken(String idpTokens) {
        // TODO: Avoid using regex
        String accessToken = idpTokens.replaceFirst("\\{.*\"access_token\"\\s*:\\s*\"([^\"]+)\".*\\}", "$1");
        return accessToken;
    }

    private static String httpGet(String url, String accessToken) throws Exception {
        String jsonBody = null;
        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            HttpGet get = new HttpGet(url);
            get.addHeader("Authorization", "Bearer " + accessToken);
            HttpResponse response = client.execute(get);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new Exception(status.getStatusCode() + ":" + status.getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                for (String s = reader.readLine(); s != null; s = reader.readLine())
                    sb.append(s);
                jsonBody = sb.toString();
            }
            finally {
                is.close();
            }
        }
        finally {
            if (client != null)
                client.close();
        }
        return jsonBody;
    }
}
