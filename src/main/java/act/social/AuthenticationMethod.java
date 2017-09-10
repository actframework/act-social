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
 * Authentication methods used by the Identity Providers
 *
 * The code is copied from https://github.com/jaliss/securesocial
 */
public enum AuthenticationMethod {
    OAUTH2 () {
        @Override
        public String secretParamName() {
            return "client_secret";
        }

        @Override
        public String keyParamName() {
            return "client_id";
        }

        @Override
        public String callBackUrlParamName() {
            return "redirect_uri";
        }

        @Override
        public String scopeParamName() {
            return "scope";
        }

        @Override
        public String csrfTokenParamName() {
            return "state";
        }

        @Override
        public String authCodeParamName() {
            return "code";
        }

        @Override
        public String accessTokenParamName() {
            return "access_token";
        }
    };

    public abstract String keyParamName();

    public abstract String secretParamName();

    public abstract String callBackUrlParamName();

    public abstract String scopeParamName();

    public abstract String csrfTokenParamName();

    public abstract String authCodeParamName();

    public abstract String accessTokenParamName();

}
