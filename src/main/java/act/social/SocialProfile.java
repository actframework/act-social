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

import act.event.ActEvent;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.S;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing a conected user and its authentication details.
 *
 * Modified from https://github.com/jaliss/securesocial
 */
public class SocialProfile implements Serializable {

    public static final String EVENT_FETCHED = "~social_profile_fetched~";

    /**
     * The user id
     */
    private SocialId id;

    /**
     * The user full name.
     */
    private String displayName;

    /**
     * The user's email
     */
    private String email;

    private String firstName;

    private String lastName;

    /**
     * A URL pointing to an avatar
     */
    private String avatarUrl;

    /**
     * The method that was used to authenticate the user.
     */
    private AuthenticationMethod authMethod;

    /**
     * The service info required to make calls to the API for OAUTH1 users
     * (available when authMethod is OAUTH1 or OPENID_OAUTH_HYBRID)
     *
     * Note: this value does not need to be persisted by UserService since it is set automatically
     * in the SecureSocial Controller for each request that needs it.
     */
    transient private Config serviceConfig;

    /**
     * The token (available when authMethod is OAUTH1, OAUTH2 or OPENID_OAUTH_HYBRID)
     */
    private String token;

    /**
     * the milliseconds since 1970-01=01 when the token is expired
     */
    private long expiration;

    /**
     * The secret (available when authMethod is OAUTH1 or OPENID_OAUTH_HYBRID)
     */
    private String secret;

    private Map<String, Object> attributes = new HashMap<>();

    public SocialProfile() {}

    public SocialProfile(String userId, String provider) {
        this.id = new SocialId(userId, provider);
    }

    public SocialId getId() {
        return id;
    }

    public void setId(SocialId id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        S.Buffer buf = S.newBuffer();
        if (S.notBlank(firstName)) {
            buf.a(firstName);
            if (S.notBlank(lastName)) {
                buf.a(" ").append(lastName);
            }
        } else if (S.notBlank(lastName)) {
            buf.append(lastName);
        }
        return buf.toString();
    }

    public void setFullName(String fullName) {
        if (fullName.contains(" ")) {
            setFirstName(S.beforeLast(fullName, " "));
            setLastName(S.afterLast(fullName, " "));
        } else {
            setLastName(fullName);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public AuthenticationMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthenticationMethod authMethod) {
        this.authMethod = authMethod;
    }

    public Config getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(Config serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public boolean isTokenExpired() {
        return -1 != expiration && expiration < $.ms();
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public void putAll(Map<String, Object> data) {
        attributes.putAll(data);
    }

    public void put(String key, Object val) {
        attributes.put(key, val);
    }

    public Map<String, Object> getAttributes() {
        return C.newMap(attributes);
    }

    public <T> T get(String key) {
        return (T) attributes.get(key);
    }

    public void fillProfile(SocialProvider provider) {
        provider.fillProfile(this);
    }

    public Fetched createFetchedEvent(String payload, String provider) {
        return new Fetched(this, payload, provider);
    }

    public static class Fetched extends ActEvent<SocialProfile> {
        private String payload;
        private String provider;
        public Fetched(SocialProfile source, String payload,String provider) {
            super(source);
            this.payload = payload;
            this.provider = provider;
        }

        public String provider() {
            return provider;
        }

        public String payload() {
            return payload;
        }
    }

}
