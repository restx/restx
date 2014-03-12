package restx.json.testing.twitter;

import org.junit.Test;

import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 12/3/14
 * Time: 22:07
 */
public class TwitterSearchTest {
    @Test
    public void should_parse_json() throws Exception {
        TwitterSearch search = TwitterSearchParser.parse(
                new InputStreamReader(TwitterBench.class.getResourceAsStream("/twitter-search.json"), "UTF-8"));

        assertThat(search.getCompleted_in()).isEqualTo(0.01338);
        assertThat(search.getResults()).hasSize(15);
        assertThat(search.getResults().get(0).toString())
                .isEqualTo(
                        "{id=1125687077; " +
                                "Text=@stroughtonsmith You need to add a &quot;Favourites&quot; tab " +
                                "to TC/iPhone. Like what TwitterFon did. I can't WAIT for your Twitter App!! :) Any ETA?;" +
                                "from_user_id =855523;" +
                                "to_user_id =815309;" +
                                "from_user =Shaun_R;" +
                                "to_user =stroughtonsmith;" +
                                "iso_language_code =en;" +
                                "profile_image_url =http://s3.amazonaws.com/twitter_production/profile_images/68778135/Safari_Dude_normal.jpg;" +
                               "created_at =Sat, 17 Jan 2009 06:14:13 +0000}"); 
                        // compare toString as an esay way to test all properties at once

    }
}
