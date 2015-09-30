package jp.co.recruit_tech.around.beaconlibrary.metadataserver;

import android.content.Context;

/**
 * Created by Hideaki on 15/01/29.
 */
public class MetadataResolverFactory {

    private static MetadataResolverFactory instance = null;

    private boolean isMock;

    private MetadataResolverFactory() {
        isMock = true;
    }

    public static MetadataResolverFactory getInstance() {
        if (instance == null) {
            instance = new MetadataResolverFactory();
        }
        return instance;
    }

    public MetadataResolver getMetadataResolver(Context appContext) {
        if (isMock) {
            return MockMetadataResolver.getInstance(appContext);
        } else {
            return RealMetadataResolver.getInstance(appContext);
        }
    }

    public boolean getMockStatus() {
        return isMock;
    }
    public void setMockStatus(boolean isMock) {
        this.isMock = isMock;
    }
}
