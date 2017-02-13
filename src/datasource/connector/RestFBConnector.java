package connector;
import com.restfb.*;

import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.types.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RestFBConnector class provides events link to a word form coordinates
 */
public class RestFBConnector {
    public static String pageAccessToken = "EAAQHHCKJVHkBAMEJqUUQLZBEEsOTWFqltdwKZBpV0IWtcBRuE60xuUueyGKAntaQc1UmgrjJXqRO8KET0YeI3jMJbdXrJhwaU5KvybBCjq9BgSSD1itRuXeqt8sge1BRQG3cKLkM5I040rknz7fMec7XyXoOK2VC78kILQQAZDZD";

    /**
     *
     * @param word a Sting which is the event to find
     * @param limit an int which the result limit
     * @param latitude a long
     * @param longitude a long
     * @return a list of events form coordiantes
     */
    public List<Event> SearchWordFromGeolocation(String word, int limit, double latitude, double longitude) {
        if (word != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
            Connection<Event> publicSearch = facebookClient.fetchConnection("search", Event.class,
                    Parameter.with("q", word),
                    Parameter.with("type", "event"),
                    Parameter.with("limit", limit),
                    Parameter.with("place&center", latitude + "," + longitude));
            System.out.println(publicSearch.getData().size());
            return publicSearch.getData().stream().collect(Collectors.toList());
        }
        return null;
    }

    public List<BatchResponse> SearchForTermUsingGeolocation(String word, int limit, double latitude, double longitude) {
        if (word != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
            BatchRequest.BatchRequestBuilder requestBuilder = new BatchRequest.BatchRequestBuilder("search?q=&type=event");
            requestBuilder.method("GET").parameters(Parameter.with("place&center", latitude + "," + longitude));
            List<BatchResponse> responses = facebookClient.executeBatch(requestBuilder.build());
            System.out.println(responses.size());
            return responses;
        }
        return null;
    }

    public static void main (String [] args){

        RestFBConnector rs = new RestFBConnector();
        List<Event> list = rs.SearchWordFromGeolocation("conference", 100, 48.8781161065379, 2.22459235221894);
        List<BatchResponse> br = rs.SearchForTermUsingGeolocation("conference", 100, 48.8781161065379, 2.22459235221894);
        /*for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != null) {
                    System.out.println(list.get(i)+"\n++++++++++++++++++++++");
        }*/


       /* String pageAccessToken = "EAAQHHCKJVHkBAMEJqUUQLZBEEsOTWFqltdwKZBpV0IWtcBRuE60xuUueyGKAntaQc1UmgrjJXqRO8KET0YeI3jMJbdXrJhwaU5KvybBCjq9BgSSD1itRuXeqt8sge1BRQG3cKLkM5I040rknz7fMec7XyXoOK2VC78kILQQAZDZD";
        FacebookClient facebookClient = new DefaultFacebookClient(pageAccessToken,Version.LATEST);
        //User user = facebookClient.fetchObject("me", User.class);
        Connection<Event> eventList = facebookClient.fetchConnection("search", Event.class,
                Parameter.with("q", "eau"), Parameter.with("type", "event"));
        BatchRequest.BatchRequestBuilder requestBuilder = new BatchRequest.BatchRequestBuilder("search?q=conference&type=event");
          requestBuilder.method("GET").parameters(Parameter.with("limit", 25));
          List<BatchResponse> batch = facebookClient.executeBatch(requestBuilder.build());
          batch.

        System.out.println(eventList.getData().size());
        eventList.getData().stream().forEach( e -> System.out.println(e+"\n++++++++++++++++"));*/
    }
}
