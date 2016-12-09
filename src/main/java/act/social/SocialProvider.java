package act.social;

import act.Act;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.osgl.inject.annotation.MapKey;
import org.osgl.inject.annotation.TypeOf;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.Codec;
import org.osgl.util.E;
import org.osgl.util.S;
import org.osgl.util.StringValueResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all itendity providers
 *
 * The code is modified from https://github.com/jaliss/securesocial
 */
public abstract class SocialProvider {

    protected final Logger logger = LogManager.get(SocialProvider.class);

    /**
     * The provider ID.
     */
    protected String id;

    /**
     * The configuration for the provider
     */
    protected Config config;

    /**
     * The authentication method used by this provider
     */
    protected AuthenticationMethod authMethod;

    OkHttpClient http = new OkHttpClient();

    /**
     * Creates a new IdentityProvider
     *
     * @param id         The type for this provider
     * @param authMethod The authentication method used by this provider
     */
    public SocialProvider(String id, AuthenticationMethod authMethod) {
        this.id = id;
        this.authMethod = authMethod;
        this.config = Config.load(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    public void checkCsrfToken(String csrfToken) {
        // by default do nothing
    }

    /**
     * Subclasses must implement the authentication logic in this method
     *
     * @param code the auth code
     * @return SocialProfile the authenticated user
     */
    public abstract SocialProfile doAuth(String code);

    protected static StringBuilder appendParam(StringBuilder sb, String key, String val) {
        String sep = sb.toString().contains("?") ? "&" : "?";
        sb.append(sep).append(Codec.encodeUrl(key)).append("=").append(Codec.encodeUrl(val));
        return sb;
    }

    protected static StringBuilder appendParams(StringBuilder sb, Map<String, String> args) {
        String sep = sb.toString().contains("?") ? "&" : "?";
        sb.append(sep).append("zz=0");
        for (Map.Entry<String, String> entry : args.entrySet()) {
            sb.append("&").append(entry.getKey()).append("=").append(Codec.encodeUrl(entry.getValue()));
        }
        return sb;
    }

    /**
     * Once the user is authenticated this method is called to retrieve profile information from the provider.
     *
     * @param user        A SocialProfile
     */
    public abstract void fillProfile(SocialProfile user);

    /**
     * Returns the authentication URL of this provider
     * @return the authentication URL
     */
    public abstract String authUrl();

    protected String callbackUrl() {
        return Act.app().router().fullUrl("~social/callback?provider=%s", getId());
    }

    protected String readUrlAsString(String url, Map<String, String> params, boolean post) {
        Response resp = fetch(url, params, post);
        try {
            return resp.body().string();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    protected JSONObject readUrlAsJson(String url, Map<String, String> params, boolean post) {
        return JSON.parseObject(readUrlAsString(url, params, post));
    }

    protected Request finishBuildingRequest(Request.Builder builder, Map<String, String> params) {
        // subclass to provider further processing
        return builder.build();
    }

    private Response fetch(String url, Map<String, String> params, boolean post) {
        Request.Builder reqBuilder;
        if (post) {
            FormBody.Builder builder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
            reqBuilder = new Request.Builder().url(url).post(builder.build());
        } else {
            url = appendParams(S.builder(url), params).toString();
            reqBuilder = new Request.Builder().url(url).get();
        }
        Request req = finishBuildingRequest(reqBuilder, params);
        try {
            Response resp = http.newCall(req).execute();
            if (resp.isSuccessful()) {
                return resp;
            } else {
                throw E.unexpected("Error reading url: %s", resp.body().string());
            }
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Singleton
    public static class Manager {
        @Inject
        @TypeOf
        @MapKey("id")
        private Map<String, SocialProvider> providers;

        public SocialProvider get(String providerId) {
            return providers.get(providerId);
        }
    }

    @Singleton
    public static class Resolver extends StringValueResolver<SocialProvider> {
        @Inject
        private Manager manager;

        @Override
        public SocialProvider resolve(String s) {
            SocialProvider provider = manager.get(s);
            E.illegalArgumentIf(null == provider, "Unknown provider: %s", s);
            return provider;
        }
    }

}
