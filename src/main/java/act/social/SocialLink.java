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

import act.app.ActionContext;
import act.app.conf.AutoConfig;
import act.controller.Controller;
import act.event.EventBus;
import act.util.LogSupport;
import act.view.ActForbidden;
import act.view.ActUnauthorized;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.osgl.$;
import org.osgl.http.H;
import org.osgl.mvc.annotation.Action;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.result.Result;
import org.osgl.util.*;
import osgl.version.Version;

import static act.controller.Controller.Util.redirect;

@Controller("social")
@AutoConfig("social_link")
public class SocialLink extends LogSupport {

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
        return redirect(socialRedirectLink(provider, callback, payload, context));
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
        String redirectUrl = provider.authUrl(callback, payload);
        if (isDebugEnabled()) {
            debug("secure: %s\n\tprovider: %s\n\tcallback: %s\n\tpayload: %s\n\tredirect to: %s",
                    context.req().secure(), provider, callback, payload, redirectUrl);
        }
        return redirectUrl;
    }

    @Action(value = "callback", methods = {H.Method.GET, H.Method.POST})
    public Result authCallback(
            SocialProvider provider,
            String code,
            String state,
            EventBus eventBus
    ) {
        if (isDebugEnabled()) {
            debug("callback >>>");
        }
        String act_callback = null;
        String act_payload = null;
        if (S.notBlank(state)) {
            byte[] jsonStr = Codec.decodeUrlSafeBase64(state);
            JSONObject json = JSON.parseObject(jsonStr, JSONObject.class);
            act_callback = json.getString("act_callback");
            act_payload = json.getString("act_payload");
        }
        try {
            if (isDebugEnabled()) {
                debug("checking CSRF token...");
            }
            provider.checkCsrfToken(state);
            SocialProfile profile = provider.doAuth(code, act_callback, act_payload);
            if (isDebugEnabled()) {
                debug("profile fetched: %s", profile);
                debug("triggering event");
            }
            // todo handle exception
            eventBus.trigger(profile.createFetchedEvent(act_payload, provider.getId()));
            if (isDebugEnabled()) {
                debug("Fetch event triggered");
            }
        } catch (Result r) {
            return r;
        } catch (RuntimeException e) {
            eventBus.trigger(new SocialLinkFailed(e));
            throw ActForbidden.create(e);
        }
        String originalCallback = act_callback;
        if (S.blank(originalCallback)) {
            originalCallback = LOGIN_REDIRECT.get();
        }
        return redirect(originalCallback);
    }

}
