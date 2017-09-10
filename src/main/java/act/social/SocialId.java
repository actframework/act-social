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

    private SocialId() {
        // for ORM usage
    }

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
