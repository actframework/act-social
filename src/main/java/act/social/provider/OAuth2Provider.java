package act.social.provider;

import act.app.ActionContext;
import act.social.AuthenticationMethod;
import act.social.SocialProfile;
import act.social.SocialProvider;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Request;
import org.osgl.$;
import org.osgl.exception.UnexpectedException;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

public abstract class OAuth2Provider extends SocialProvider {


    public OAuth2Provider(String id) {
        super(id, AuthenticationMethod.OAUTH2);
    }

    protected boolean postToAccessTokenUrl() {
        return false;
    }

    protected Map<String, String> authorizationParams() {
        return C.map(
                authMethod.keyParamName(), config.getKey(),
                authMethod.callBackUrlParamName(), callbackUrl(),
                authMethod.scopeParamName(), config.getScope(),
                authMethod.csrfTokenParamName(), createCsrfToken(),
                "response_type", "code"
        );
    }

    protected Map<String, String> exchangeAccessTokenParams(String code) {
        return C.newMap(
                authMethod.keyParamName(), config.getKey(),
                authMethod.secretParamName(), config.getSecret(),
                authMethod.authCodeParamName(), code,
                authMethod.callBackUrlParamName(), callbackUrl(),
                "grant_type", "authorization_code"
        );
    }

    @Override
    public String authUrl() {
        String url = config.getAuthUrl();
        StringBuilder sb = S.builder(url);
        appendParams(sb, authorizationParams());
        return sb.toString();
    }

    protected String expiresParamName() {
        return "expires";
    }

    protected long parseExpires(String s) {
        return $.ms() + Long.parseLong(s) - 5 * 1000;
    }

    protected String accessTokenHeaderName() {
        return null;
    }

    protected String accessTokenHeaderVal(String token) {
        return token;
    }

    protected String accessTokenParamName() {
        return authMethod.accessTokenParamName();
    }

    @Override
    protected Request finishBuildingRequest(Request.Builder builder, Map<String, String> params) {
        tryApplyAccessToken(builder, params);
        return builder.build();
    }

    protected final void tryApplyAccessToken(Request.Builder builder, Map<String, String> params) {
        String accessToken = params.get(accessTokenParamName());
        if (null == accessToken) {
            return;
        }
        String headerName = accessTokenHeaderName();
        if (null != headerName) {
            builder.addHeader(headerName, accessTokenHeaderVal(accessToken));
            params.remove(accessTokenParamName());
        }
    }

    protected boolean accessTokenInJson() {
        return false;
    }

    @Override
    public SocialProfile doAuth(String code) {
        String accessToken = null;
        long expires = -1;
        if (!accessTokenInJson()) {
            String result = readUrlAsString(config.getAccessTokenUrl(), exchangeAccessTokenParams(code), postToAccessTokenUrl());
            String[] pairs = result.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length != 2) {
                    throw new UnexpectedException("Unexpected auth response");
                }
                String k = kv[0];
                if ("access_token".equals(k)) {
                    accessToken = kv[1];
                } else if (expiresParamName().equals(k)) {
                    expires = parseExpires(kv[1]);
                }
            }
        } else {
            JSONObject result = readUrlAsJson(config.getAccessTokenUrl(), exchangeAccessTokenParams(code), postToAccessTokenUrl());
            accessToken = result.getString("access_token");
            expires = parseExpires(result.getString(expiresParamName()));
        }
        SocialProfile profile = new SocialProfile();
        profile.setToken(accessToken);
        profile.setExpiration(expires);
        fillProfile(profile);
        return profile;
    }

    private String createCsrfToken() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
