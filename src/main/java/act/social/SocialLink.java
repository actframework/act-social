package act.social;

import act.app.ActionContext;
import act.app.conf.AutoConfig;
import act.controller.Controller;
import act.event.EventBus;
import org.osgl.$;
import org.osgl.http.H;
import org.osgl.mvc.annotation.Action;
import org.osgl.mvc.result.Result;
import org.osgl.util.Const;
import org.osgl.util.S;

@Controller("social")
@AutoConfig("social_link")
public class SocialLink extends Controller.Util {

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

    @Action(value = "callback", methods = {H.Method.GET, H.Method.POST})
    public Result authCallback(
            SocialProvider provider,
            String code,
            String state,
            String act_callback,
            String act_payload,
            EventBus eventBus
    ) {
        provider.checkCsrfToken(state);
        SocialProfile profile = provider.doAuth(code, act_callback, act_payload);
        // todo handle exception
        String payload = act_payload;
        eventBus.trigger(profile.createFetchedEvent(payload));
        String originalCallback = act_callback;
        if (S.blank(originalCallback)) {
            originalCallback = LOGIN_REDIRECT.get();
        }
        return redirect(originalCallback);
    }

}
