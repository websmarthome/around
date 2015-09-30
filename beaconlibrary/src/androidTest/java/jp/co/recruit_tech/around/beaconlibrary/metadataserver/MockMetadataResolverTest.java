package jp.co.recruit_tech.around.beaconlibrary.metadataserver;

import android.test.AndroidTestCase;

import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;

/**
 * Created by Hideaki on 15/01/30.
 */
public class MockMetadataResolverTest extends AndroidTestCase {
    public MockMetadataResolverTest() {
    }

    public void testMock() {
        MockMetadataResolver resolver = MockMetadataResolver.getInstance(getContext());
        resolver.setMetadataResolverCallback(new MetadataResolver.RequestCallback() {
            @Override
            public void onUrlMetadataReceived(String requestUrl, UrlMetadata urlMetadata) {
                assertEquals("http://google.co.jp/", requestUrl);
                assertEquals("http://google.co.jp/", urlMetadata.getUrl());
                assertEquals("Google JP", urlMetadata.getTitle());
                assertEquals("検索エンジン", urlMetadata.getDescription());
            }

            @Override
            public void onUrlMetadataIconReceived(UrlMetadata urlMetadata) {

            }
        });
        resolver.requestUrlMetadata("http://google.co.jp/");
        resolver.setAsyncMode(false);
    }
}