package act.social;

import act.app.ActionContext;
import act.app.conf.AutoConfig;
import act.controller.Controller;
import act.event.EventBus;
import org.osgl.$;
import org.osgl.bootstrap.Version;
import org.osgl.http.H;
import org.osgl.mvc.annotation.Action;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.result.Result;
import org.osgl.util.Const;
import org.osgl.util.S;

@Controller("social")
@AutoConfig("social_link")
public class SocialLink extends Controller.Util {

    public static final Version VERSION = Version.of(SocialLink.class);

    public static final Const<String> LOGIN_REDIRECT = $.constant();

    /**
     * Post to this endpoint to trigger the social authenticate process
     */
    @Action(value = "start", methods = {H.Method.GET, H.Method.POST})
    public Result startSocialLink(
            SocialProvider provider,
            String callback,
            String payload,
            ActionContext context
    ) {
        if (null == callback) {
            callback = context.req().referrer();
        }
        return redirect(provider.authUrl(callback, payload));
    }

    /**
     * Returns the auth URL so in certain case the pure front end app (e.g. ionic) can fetch it
     * and initiate redirect from the client side (to manipulate headers like User-Agent)
     */
    @GetAction("auth_link")
    public String socialRedirectLink(SocialProvider provider, String callback, String payload, ActionContext context) {
        if (null == callback) {
            callback = context.req().referrer();
        }
        return provider.authUrl(callback, payload);
    }

    @Action(value = "callback", methods = {H.Method.GET, H.Method.POST})
    public Result authCallback(
            SocialProvider provider,
            String code,
            String state,
            String act_callback,
            String act_payload,
            EventBus eventBus
    ) {
        try {
            provider.checkCsrfToken(state);
            SocialProfile profile = provider.doAuth(code, act_callback, act_payload);
            // todo handle exception
            String payload = act_payload;
            eventBus.trigger(profile.createFetchedEvent(payload));
        } catch (Result r) {
            return r;
        } catch (RuntimeException e) {
            eventBus.trigger(new SocialLinkFailed());
        }
        String originalCallback = act_callback;
        if (S.blank(originalCallback)) {
            originalCallback = LOGIN_REDIRECT.get();
        }
        return redirect(originalCallback);
    }

}
