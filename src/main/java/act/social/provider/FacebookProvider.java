package act.social.provider;

import act.app.ActionContext;
import act.social.AuthenticationMethod;
import act.social.SocialId;
import act.social.SocialProfile;
import act.social.SocialProvider;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.osgl.$;
import org.osgl.exception.UnexpectedException;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.io.IOException;
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

    private void updateAccessToken(SocialProfile user) {
        throw E.tbd();
    }
}
