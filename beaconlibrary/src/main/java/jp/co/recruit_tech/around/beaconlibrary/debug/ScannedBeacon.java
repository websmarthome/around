package jp.co.recruit_tech.around.beaconlibrary.debug;

import org.uribeacon.beacon.UriBeacon;

import java.util.Date;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/16.
 */
public class ScannedBeacon {
    private UriBeacon beacon;
    private Date timestamp;

    public ScannedBeacon(UriBeacon beacon, Date timestamp) {
        this.beacon = beacon;
        this.timestamp = timestamp;
    }

    public UriBeacon getBeacon() {
        return beacon;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
