package act.social.provider;

import act.social.SocialId;
import act.social.SocialProfile;
import com.alibaba.fastjson.JSONObject;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Map;

public class LinkedInProvider extends OAuth2Provider {


    public LinkedInProvider() {
        super("linkedin");
    }

    protected String expiresParamName() {
        return "expires_in";
    }

    @Override
    protected long parseExpires(String s) {
        return $.ms() + Long.parseLong(s) * 1000 - 5 * 1000;
    }

    @Override
    protected boolean accessTokenInJson() {
        return true;
    }

    @Override
    protected String accessTokenHeaderName() {
        return "Authorization";
    }

    @Override
    protected String accessTokenHeaderVal(String token) {
        return S.builder("Bearer ").append(token).toString();
    }

    @Override
    public void fillProfile(SocialProfile user) {
        if (user.isTokenExpired()) {
            updateAccessToken(user);
        }

        String url = config.getProfileUrl();
        Map<String, String> params = C.newMap(
                "format", "json",
                authMethod.accessTokenParamName(), user.getToken()
        );
        JSONObject json = readUrlAsJson(url, params, false);
        user.setId(new SocialId(json.getString("id"), this.getId()));
        user.setFirstName(json.getString("firstName"));
        user.setLastName(json.getString("lastName"));
        user.setEmail(json.getString("emailAddress"));
        user.setAvatarUrl(json.getString("pictureUrl"));
    }

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
