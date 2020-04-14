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

import act.social.SocialId;
import act.social.SocialProfile;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.Map;

public class GoogleProvider extends OAuth2Provider {


    public GoogleProvider() {
        super("google");
    }

    @Override
    protected Map<String, String> exchangeAccessTokenParams(String code, String callback, String payload) {
        Map<String, String> params = super.exchangeAccessTokenParams(code, callback, payload);
        params.put("grant_type", "authorization_code");
        return params;
    }

    @Override
    protected boolean postToAccessTokenUrl() {
        return true;
    }

    @Override
    protected boolean accessTokenInJson() {
        return true;
    }

    @Override
    protected String expiresParamName() {
        return "expires_in";
    }

    @Override
    protected long parseExpires(String s) {
        return $.ms() + Long.parseLong(s) * 1000 - 5 * 1000;
    }

    @Override
    public void fillProfile(SocialProfile user) {
        if (user.isTokenExpired()) {
            updateAccessToken(user);
        }

        String url = config.getProfileUrl();
        Map<String, String> params = C.Map(
                "personFields", "emailAddresses,names,photos",
                authMethod.accessTokenParamName(), user.getToken()
        );
        JSONObject json = readUrlAsJson(url, params, false);
        user.setId(new SocialId(json.getString("id"), this.getId()));
        JSONArray emails = json.getJSONArray("emailAddresses");
        if (!emails.isEmpty()) {
            JSONObject emailObject = emails.getJSONObject(0);
            user.setEmail(emailObject.getString("value"));
        }
        JSONArray names = json.getJSONArray("names");
        if (!names.isEmpty()) {
            JSONObject nameObject = names.getJSONObject(0);
            user.setDisplayName(nameObject.getString("displayName"));
            user.setFirstName(nameObject.getString("givenName"));
            user.setLastName(nameObject.getString("familyName"));
        }
        JSONArray photos = json.getJSONArray("photos");
        if (!photos.isEmpty()) {
            JSONObject photoObject = photos.getJSONObject(0);
            user.setAvatarUrl(photoObject.getString("url"));
        }
        if (isTraceEnabled()) {
            trace("Done GoogleProvider.fillProfile");
        }
    }

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
