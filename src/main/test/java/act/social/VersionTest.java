package act.social;

import org.junit.Test;
import osgl.ut.TestBase;

public class VersionTest extends TestBase {

    @Test
    public void versionShallContainsSocialLink() {
        yes(SocialLink.VERSION.toString().contains("social-link"));
    }

}
