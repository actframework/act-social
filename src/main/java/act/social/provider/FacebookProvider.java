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
import com.alibaba.fastjson.JSONObject;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Map;

public class FacebookProvider extends OAuth2Provider {


    public FacebookProvider() {
        super("facebook");
    }

    @Override
    public void fillProfile(SocialProfile user) {
        if (user.isTokenExpired()) {
            updateAccessToken(user);
        }

        String url = config.getProfileUrl();
        Map<String, String> params = C.newMap(
                "fields", "name,picture,email,first_name,last_name",
                authMethod.accessTokenParamName(), user.getToken()
        );
        JSONObject json = readUrlAsJson(url, params,false);
        user.setId(new SocialId(json.getString("id"), this.getId()));
        user.setFirstName(json.getString("first_name"));
        user.setLastName(json.getString("last_name"));
        user.setEmail(json.getString("email"));

        Object pic = json.get("picture");
        if (pic instanceof JSONObject) {
            user.setAvatarUrl(((JSONObject) pic).getJSONObject("data").getString("url"));
        } else if (null != pic) {
            user.setAvatarUrl(S.string(pic));
        }

    }

    @Override
    protected boolean accessTokenInJson() {
        return true;
    }

    @Override
    protected String expiresParamName() {
        return "expires_in";
    }

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
