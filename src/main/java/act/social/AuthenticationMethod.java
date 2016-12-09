package act.social;

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
