package seaplus.seefood.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Vin on 25/10/2017.
 */

public class CLocation implements Parcelable {

    private double longitude;
    private double latitude;

    public CLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    protected CLocation(Parcel in) {
        longitude = in.readDouble();
        latitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CLocation> CREATOR = new Creator<CLocation>() {
        @Override
        public CLocation createFromParcel(Parcel in) {
            return new CLocation(in);
        }

        @Override
        public CLocation[] newArray(int size) {
            return new CLocation[size];
        }
    };

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
