package act.social.provider;

import act.social.SocialId;
import act.social.SocialProfile;
import com.alibaba.fastjson.JSONObject;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Map;

public class GoogleProvider extends OAuth2Provider {


    public GoogleProvider() {
        super("google");
    }

    @Override
    protected Map<String, String> exchangeAccessTokenParams(String code) {
        Map<String, String> params = super.exchangeAccessTokenParams(code);
        params.put("grant_type", "authorization_code");
        return params;
    }

    @Override
    public void fillProfile(SocialProfile user) {
        if (user.isTokenExpired()) {
            updateAccessToken(user);
        }

        String url = config.getProfileUrl();
        Map<String, String> params = C.map(
                "fields", "emails%2Fvalue%2Cid%2Cimage%2Furl%2Cname(familyName%2CgivenName)",
                authMethod.accessTokenParamName(), user.getToken()
        );
        JSONObject json = readUrlAsJson(url, params, false);
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

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
