package jp.co.recruit_tech.around.beaconlibrary.service;

import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;

interface IBeaconLibraryMainService {
    /**
     * 現在のMetadataSetを得る
     */
    UrlMetadataSet getCurrentUrlMetadataSet();
    /**
     * MetadataSetをクリアする。
     * DB上の情報もクリアする。
     */
    void clearUrlMetadataSet();
}
