package act.social.provider;

/*-
 * #%L
 * ACT Social Link
 * %%
 * Copyright (C) 2016 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.social.AuthenticationMethod;
import act.social.SocialProfile;
import act.social.SocialProvider;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Request;
import org.osgl.$;
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

    protected Map<String, String> authorizationParams(String callback, String payload) {
        return C.Map(
                authMethod.keyParamName(), config.getKey(),
                authMethod.callBackUrlParamName(), callbackUrl(callback, payload),
                authMethod.scopeParamName(), config.getScope(),
                authMethod.csrfTokenParamName(), createCsrfToken(),
                "response_type", "code"
        );
    }

    protected Map<String, String> exchangeAccessTokenParams(String code, String act_callback, String act_payload) {
        return C.newMap(
                authMethod.keyParamName(), config.getKey(),
                authMethod.secretParamName(), config.getSecret(),
                authMethod.authCodeParamName(), code,
                authMethod.callBackUrlParamName(), callbackUrl(act_callback, act_payload),
                "grant_type", "authorization_code"
        );
    }

    @Override
    public String authUrl(String callback, String payload) {
        String url = config.getAuthUrl();
        StringBuilder sb = S.builder(url);
        appendParams(sb, authorizationParams(callback, payload));
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
    public SocialProfile doAuth(String code, String act_callback, String act_payload) {
        String accessToken = null;
        long expires = -1;
        if (!accessTokenInJson()) {
            String result = readUrlAsString(config.getAccessTokenUrl(), exchangeAccessTokenParams(code, act_callback, act_payload), postToAccessTokenUrl());
            String[] pairs = result.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length != 2) {
                    // github response might have scope=&... meaning there
                    // is no scope. let's just ignore it
                    continue;
                }
                String k = kv[0];
                if ("access_token".equals(k)) {
                    accessToken = kv[1];
                } else if (expiresParamName().equals(k)) {
                    expires = parseExpires(kv[1]);
                }
            }
        } else {
            JSONObject result = readUrlAsJson(config.getAccessTokenUrl(), exchangeAccessTokenParams(code, act_callback, act_payload), postToAccessTokenUrl());
            accessToken = result.getString("access_token");
            expires = parseExpires(result.getString(expiresParamName()));
        }
        SocialProfile profile = new SocialProfile();
        profile.setToken(accessToken);
        profile.setExpiration(expires);
        profile.setServiceConfig(config);
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
