package jp.co.recruit_tech.around.beaconlibrary.metadata;

import android.test.AndroidTestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Hideaki on 15/03/10.
 */
public class UrlMetadataSetTest extends AndroidTestCase {
    public void testSortByScannedAt() {
        UrlMetadataSet metadataSet = new UrlMetadataSet();
        UrlMetadata metadata;
        JSONObject json;
        Date now = new Date();

        metadataSet.sortByScannedAt();
        assertEquals(0, metadataSet.size());

        try {
            json = new JSONObject();
            json.put("url", "http://example.com/1");
            json.put("title", "title01");
            json.put("description", "desc01");

            metadata = new UrlMetadata();
            metadata.updateFromResolverJSON(json);
            metadata.setLastScannedAt(now);
            metadataSet.upsert(metadata);

            json = new JSONObject();
            json.put("url", "http://example.com/2");
            json.put("title", "title02");
            json.put("description", "desc02");

            metadata = new UrlMetadata();
            metadata.updateFromResolverJSON(json);
            metadata.setLastScannedAt(new Date(now.getTime() + 10));
            metadataSet.upsert(metadata);

            json = new JSONObject();
            json.put("url", "http://example.com/3");
            json.put("title", "title03");
            json.put("description", "desc03");

            metadata = new UrlMetadata();
            metadata.updateFromResolverJSON(json);
            metadata.setLastScannedAt(new Date(now.getTime() + 20));
            metadataSet.upsert(metadata);

            metadataSet.sortByScannedAt();

            assertEquals("title03", metadataSet.get(0).getTitle());
            assertEquals("title02", metadataSet.get(1).getTitle());
            assertEquals("title01", metadataSet.get(2).getTitle());
        } catch (JSONException ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }
}
