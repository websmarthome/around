package jp.co.recruit_tech.around.beaconlibrary;

/**
 * Created by kusakabe on 15/02/24.
 */
public class Consts {
    private Consts() {}

    /**
     * DBから取得したMetadataの有効期限。
     * 期限切れの場合、ResolveServerから取得する。
     */
    public static final int METADATA_AVAILABLE_PERIOD = 60 * 60 * 1000; // 1s = 1000

    /**
     * AggregateMetadataTaskが直近の周辺Metadataとして保持する時間間隔。
     */
    public static final int AGGREGATE_METADATA_PERIOD = 60 * 1000;

    /**
     * 1度スキャンされたBeaconDataを無視し続ける期間
     */
    public static final int IGNORE_SCANNED_BEACON_PERIOD_MILLIS = 60 * 1000;

}
