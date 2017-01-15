package act.social;

import act.event.ActEvent;
import org.osgl.$;

import java.io.Serializable;

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

    public void fillProfile(SocialProvider provider) {
        provider.fillProfile(this);
    }

    public Fetched createFetchedEvent(String payload) {
        return new Fetched(this, payload);
    }

    public static class Fetched extends ActEvent<SocialProfile> {
        private String payload;
        public Fetched(SocialProfile source, String payload) {
            super(source);
            this.payload = payload;
        }

        public String payload() {
            return payload;
        }
    }

}
