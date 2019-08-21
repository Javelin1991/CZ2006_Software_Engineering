package seaplus.seefood.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by htetnaing on 25/10/17.
 */

public class User implements Parcelable {


    private String username;
    private String url;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public User(String username, String url) {
        this.username = username;
        this.url = url;
    }

    protected User(Parcel in) {
        username = in.readString();
        url = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(url);
    }
}

