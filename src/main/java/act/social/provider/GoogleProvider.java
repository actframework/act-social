package act.social.provider;

import act.social.SocialId;
import act.social.SocialProfile;
import com.alibaba.fastjson.JSONObject;
import org.osgl.$;
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
        Map<String, String> params = C.map(
                "fields", "emails/value,id,image/url,name(familyName,givenName)",
                authMethod.accessTokenParamName(), user.getToken()
        );
        JSONObject json = readUrlAsJson(url, params, false);
        user.setId(new SocialId(json.getString("id"), this.getId()));
        JSONObject name = json.getJSONObject("name");
        user.setFirstName(name.getString("givenName"));
        user.setLastName(name.getString("familyName"));
        user.setEmail(json.getJSONArray("emails").getJSONObject(0).getString("value"));
        user.setAvatarUrl(json.getJSONObject("image").getString("url"));
    }

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
