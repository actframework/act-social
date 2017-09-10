package act.social;

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

import act.Act;
import org.osgl.$;
import org.osgl.exception.UnexpectedIOException;
import org.osgl.util.C;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Config {

    public static final List<String> BUILT_IN_PROVIDERS = C.list(
            "google", "facebook", "twitter", "yahoo",
            "linkedin", "foursquare"
    );

    private String provider;
    private String scope;
    private String authorizationUrl;
    private String requestTokenUrl;
    private String accessTokenUrl;
    private String profileUrl;
    /**
     * The consumerKey or clientId
     */
    private String key;
    private String secret;
    private String accessType;

    // used by ORM or JSON mappers
    private Config() {}

    private Config(String provider) {
        this.provider = provider;
    }

    public Config(Config copy) {
        provider = copy.provider;
        scope = copy.scope;
        authorizationUrl = copy.authorizationUrl;
        requestTokenUrl = copy.requestTokenUrl;
        accessTokenUrl = copy.accessTokenUrl;
        profileUrl = copy.profileUrl;
        key = copy.key;
        secret = copy.secret;
        accessType = copy.accessType;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAuthUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getRequestTokenUrl() {
        return requestTokenUrl;
    }

    public void setRequestTokenUrl(String requestTokenUrl) {
        this.requestTokenUrl = requestTokenUrl;
    }

    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }

    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    @Override
    public String toString() {
        return "Config{" +
                "provider='" + provider + '\'' +
                ", scope='" + scope + '\'' +
                ", authorizationUrl='" + authorizationUrl + '\'' +
                ", requestTokenUrl='" + requestTokenUrl + '\'' +
                ", accessTokenUrl='" + accessTokenUrl + '\'' +
                ", key='" + key + '\'' +
                ", secret='" + secret + '\'' +
                ", accessType='" + accessType + '\'' +
                '}';
    }

    private void populateFrom(Map properties) {
        for (Field field : Config.class.getDeclaredFields()) {
            String name = field.getName();
            String key = S.builder("social_link.").append(provider).append(".").append(name).toString();
            String val = S.string(properties.get(key));
            if (S.notBlank(val)) {
                $.setField(name, this, val);
            }
        }
    }

    private static class Default {

        private static Properties properties;

        static {
            try {
                properties = IO.loadProperties(Config.class.getResourceAsStream("/social_link.properties"));
            } catch (NullPointerException e) {
                throw new UnexpectedIOException("Error loading default social link properties");
            }
        }

        private static Map<String, Config> defaults = new HashMap<>();

        static Config get(String providerId) {
            Config config = defaults.get(providerId);
            if (null == config) {
                config = createFromProperties(providerId);
                defaults.put(providerId, config);
            }
            return new Config(config);
        }

        private static Config createFromProperties(String providerId) {
            Config config = new Config(providerId);
            config.populateFrom(properties);
            config.populateFrom(Act.appConfig().rawConfiguration());
            return config;
        }
    }

    public static Config load(String provider) {
        return Default.get(provider);
    }

    public static void main(String[] args) {
        for (String providerId : BUILT_IN_PROVIDERS) {
            System.out.println(Default.get(providerId));
        }
    }

}
