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
    public static final String KEY_ORIGINAL_CALLBACK = "~social_callback~";

    /**
     * Post to this endpoint to trigger the social authenticate process
     */
    @Action(value = "start", methods = {H.Method.GET, H.Method.POST})
    public Result startSocialLink(
            SocialProvider provider,
            String callback,
            ActionContext context
    ) {
        if (null == callback) {
            callback = context.req().referrer();
        }
        context.flash().put(KEY_ORIGINAL_CALLBACK, callback);
        return redirect(provider.authUrl());
    }

    @Action(value = "callback", methods = {H.Method.GET, H.Method.POST})
    public Result authCallback(
            SocialProvider provider,
            String code,
            String state,
            ActionContext context,
            EventBus eventBus
    ) {
        provider.checkCsrfToken(state);
        context.flash().keep(KEY_ORIGINAL_CALLBACK);
        SocialProfile profile = provider.doAuth(code);
        // todo handle exception
        eventBus.trigger(profile.createFetchedEvent());
        String originalCallback = context.flash().get(KEY_ORIGINAL_CALLBACK);
        if (S.blank(originalCallback)) {
            originalCallback = LOGIN_REDIRECT.get();
        }
        return redirect(originalCallback);
    }

}
