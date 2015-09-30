package jp.co.recruit_tech.around.beaconlibrary.scanner;

import android.os.Parcel;
import android.os.Parcelable;

import org.uribeacon.beacon.UriBeacon;

import java.util.Date;

/**
 * Created by Hideaki on 15/02/12.
 */
public class UriBeaconData implements Parcelable {
    private final String uriString;
    private final byte txPowerLevel;
    private final Date scannedAt;

    public UriBeaconData(String uriString, byte txPowerLevel, Date scannedAt) {
        this.uriString = uriString;
        this.txPowerLevel = txPowerLevel;
        this.scannedAt = scannedAt;
    }

    public UriBeaconData(UriBeacon beacon, Date scannedAt) {
        this.uriString = beacon.getUriString();
        this.txPowerLevel = beacon.getTxPowerLevel();
        this.scannedAt = scannedAt;
    }

    public byte getTxPowerLevel() {
        return txPowerLevel;
    }

    public String getUriString() {
        return uriString;
    }

    public Date getScannedAt() {
        return scannedAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uriString);
        dest.writeByte(txPowerLevel);
        dest.writeSerializable(scannedAt);
    }

    public static final Creator<UriBeaconData> CREATOR = new Parcelable.Creator<UriBeaconData>() {
        @Override
        public UriBeaconData createFromParcel(Parcel source) {
            String uriString = source.readString();
            byte txPowerLevel = source.readByte();
            Date scannedAt = (Date)source.readSerializable();
            return new UriBeaconData(uriString, txPowerLevel, scannedAt);
        }

        @Override
        public UriBeaconData[] newArray(int size) {
            return new UriBeaconData[size];
        }
    };
}
