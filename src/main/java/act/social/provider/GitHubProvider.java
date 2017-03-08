package act.social.provider;

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
