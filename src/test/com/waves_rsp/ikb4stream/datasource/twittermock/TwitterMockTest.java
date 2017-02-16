package com.waves_rsp.ikb4stream.datasource.twittermock;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TwitterMockTest {

    private static final String TWEETS = "{ \"_id\" : { \"$oid\" : \"583629450310e118d02283f5\" }, \"created_at\" : \"Wed Nov 23 23:41:56 +0000 2016\", \"id\" : 8.0157151671866163E17, \"id_str\" : \"801571516718661632\", \"text\" : \"O zeezeilers" +
            "? O Coren Sails? O Rostock Germany?!\", \"source\" : \"<a href=\\\"http://twitter.com\\\" rel=\\\"nofollow\\\">Twitter Web Client</a>\", \"truncated\" : false, \"in_reply_to_status_id\" : null, \"in_repl" +
            "y_to_status_id_str\" : null, \"in_reply_to_user_id\" : null, \"in_reply_to_user_id_str\" : null, \"in_reply_to_screen_name\" : null, \"user\" : { \"id\" : 7.7935356826028851E17, \"id_str\" : \"779353" +
            "568260288512\", \"name\" : \"Anna Coren Taxi 19dP\", \"screen_name\" : \"SouthernCrossFr\", \"location\" : \"Paris, France\", \"url\" : \"https://www.youtube.com/watch?v=hW2vyytxCGM\", \"description\" : \"" +
            "https://www.youtube.com/watch?v=2bosouX_d8Y\", \"protected\" : false, \"verified\" : false, \"followers_count\" : 12, \"friends_count\" : 0, \"listed_count\" : 18, \"favourites_count\" : 1, \"statuse" +
            "s_count\" : 4, \"created_at\" : \"Fri Sep 23 16:15:45 +0000 2016\", \"utc_offset\" : 3600, \"time_zone\" : \"Paris\", \"geo_enabled\" : true, \"lang\" : \"fr\", \"contributors_enabled\" : false, \"is_trans" +
            "lator\" : false, \"profile_background_color\" : \"000000\", \"profile_background_image_url\" : \"http://abs.twimg.com/images/themes/theme1/bg.png\", \"profile_background_image_url_https\" : \"https" +
            "://abs.twimg.com/images/themes/theme1/bg.png\", \"profile_background_tile\" : false, \"profile_link_color\" : \"1B95E0\", \"profile_sidebar_border_color\" : \"000000\", \"profile_sidebar_fill_color" +
            "\" : \"000000\", \"profile_text_color\" : \"000000\", \"profile_use_background_image\" : false, \"profile_image_url\" : \"http://pbs.twimg.com/profile_images/801554065729736710/xAURzsfl_normal.jpg\"" +
            ", \"profile_image_url_https\" : \"https://pbs.twimg.com/profile_images/801554065729736710/xAURzsfl_normal.jpg\", \"profile_banner_url\" : \"https://pbs.twimg.com/profile_banners/77935356826028" +
            "8512/1479940388\", \"default_profile\" : false, \"default_profile_image\" : false, \"following\" : null, \"follow_request_sent\" : null, \"notifications\" : null }, \"geo\" : null, \"coordinates\" : n" +
            "ull, \"place\" : { \"id\" : \"09f6a7707f18e0b1\", \"url\" : \"https://api.twitter.com/1.1/geo/id/09f6a7707f18e0b1.json\", \"place_type\" : \"city\", \"name\" : \"Paris\", \"full_name\" : \"Paris, France\", \"" +
            "country_code\" : \"FR\", \"country\" : \"France\", \"bounding_box\" : { \"type\" : \"Polygon\", \"coordinates\" : [ [ [ 2.224101, 48.815521 ], [ 2.224101, 48.902146 ], [ 2.469905, 48.902146 ], [ 2.469" +
            "905, 48.815521 ] ] ] }, \"attributes\" : {  } }, \"contributors\" : null, \"is_quote_status\" : false, \"retweet_count\" : 0, \"favorite_count\" : 0, \"entities\" : { \"hashtags\" : [  ], \"urls\" : [ " +
            "], \"user_mentions\" : [  ], \"symbols\" : [  ] }, \"favorited\" : false, \"retweeted\" : false, \"filter_level\" : \"low\", \"lang\" : \"en\", \"timestamp_ms\" : \"1479944516721\" }";

    @Test
    public void withNoErrorTest(){
        InputStream iS = new ByteArrayInputStream(TWEETS.getBytes(StandardCharsets.UTF_8));
        TwitterMock tm = new TwitterMock(iS);
        tm.load(System.out::println);
    }

    @Test (expected = NullPointerException.class)
    public void nullInputStreamTest() {
        TwitterMock tm = new TwitterMock(null);
        tm.load(dataProducer -> {
            //Nothing
        });
    }

    @Test
    public void badInputStreamTest() {
        String s = "HelloWorld!";
        InputStream iS = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        TwitterMock tm = new TwitterMock(iS);
        Thread t = new Thread(() -> {
            tm.load(dataProducer -> {

            });
        });

        t.start();

        try{
            Thread.sleep(100);
        } catch (InterruptedException e) {
            //Do nothing
        }finally {
            t.interrupt();
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullDataProducerTest() {
        InputStream iS = new ByteArrayInputStream(TWEETS.getBytes(StandardCharsets.UTF_8));
        TwitterMock tm = new TwitterMock(iS);
        tm.load(null);
    }

    @Test
    public void testWithoutError() {
        InputStream iS = new ByteArrayInputStream(TWEETS.getBytes(StandardCharsets.UTF_8));
        TwitterMock tm = new TwitterMock(iS);
        Thread t = new Thread(() -> {
            tm.load(dataProducer -> {
                //Do nothing
            });
        });

        t.start();

        try {
            Thread.sleep(100);
        }catch (InterruptedException err) {
            //Do nothing
        }finally {
            t.interrupt();
            Thread.currentThread().interrupt();
        }
    }
}
