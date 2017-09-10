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

public class GitHubProvider extends OAuth2Provider {


    public GitHubProvider() {
        super("github");
    }

    @Override
    protected String accessTokenParamName() {
        return super.accessTokenParamName();
    }

    @Override
    public void fillProfile(SocialProfile user) {
        if (user.isTokenExpired()) {
            updateAccessToken(user);
        }

        String url = config.getProfileUrl();
        Map<String, String> params = C.map(authMethod.accessTokenParamName(), user.getToken());
        JSONObject json = readUrlAsJson(url, params,false);
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (null == val) {
                continue;
            }
            String sVal = S.string(val);
            if ("id".equals(key)) {
                user.setId(new SocialId(sVal, this.getId()));
            } else if ("login".equals(key)) {
                user.setDisplayName(sVal);
            } else if ("name".equals(key)) {
                user.setFullName(sVal);
            } else if ("email".equals(key)) {
                user.setEmail(sVal);
            } else if ("avatar_url".equals(key)) {
                user.setAvatarUrl(sVal);
            } else {
                user.put(key, val);
            }
        }
    }

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
