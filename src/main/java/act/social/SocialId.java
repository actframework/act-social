package act.social;

/**
 * A class to uniquely identify users. This combines the id the user has on
 * an external service (eg: twitter, facebook) with the provider type.
 *
 * The code is modified from https://github.com/jaliss/securesocial
 */
public class SocialId implements java.io.Serializable {
    /**
     * The id the user has in a external service.
     */
    private String userId;

    /**
     * The provider this user belongs to.
     */
    private String provider;

    public SocialId(String userId, String provider) {
        this.userId = userId;
        this.provider = provider;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

}
