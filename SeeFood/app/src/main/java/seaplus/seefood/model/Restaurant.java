package seaplus.seefood.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by NTU user on 30/9/2017.
 */

public class Restaurant implements Parcelable, Comparable<Restaurant>{

    private String placeID;
    private String image;
    private String name;
    private double rating;
    private String price;
    private String vicinity;

    public Restaurant(String placeID, String image, String name, double rating, String price, String vicinity) {
        this.placeID = placeID;
        this.image = image;
        this.name = name;
        this.rating = rating;
        this.price = price;
        this.vicinity = vicinity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(placeID);
        out.writeString(image);
        out.writeString(name);
        out.writeDouble(rating);
        out.writeString(price);
        out.writeString(vicinity);
    }

    private Restaurant(Parcel in){
        this.placeID = in.readString();
        this.image = in.readString();
        this.name = in.readString();
        this.rating = in.readDouble();
        this.price = in.readString();
        this.vicinity = in.readString();
    }

    public static final Parcelable.Creator<Restaurant> CREATOR = new Parcelable.Creator<Restaurant>() {
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    public static Comparator<Restaurant> RestaruantNameComparator = new Comparator<Restaurant>() {
        public int compare(Restaurant rest1, Restaurant rest2) {

            String restName1 = rest1.getName().toUpperCase();
            String restName2 = rest2.getName().toUpperCase();

            //ascending order
            return restName1.compareTo(restName2);

            //descending order
            //return restName2.compareTo(restName1);
        }
    };

    public static Comparator<Restaurant> RestaruantPriceComparator = new Comparator<Restaurant>() {
        public int compare(Restaurant rest1, Restaurant rest2) {

            String restPrice1 = rest1.getPrice().toUpperCase();
            String restPrice2 = rest2.getPrice().toUpperCase();

            //ascending order
            return restPrice1.compareTo(restPrice2);

            //descending order
            //return restPrice2.compareTo(restPrice1);
        }
    };

    @Override
    public int compareTo(Restaurant compareRestaurant) {

        double compareRating = ((Restaurant) compareRestaurant).getRating();


        if(this.rating < compareRating)
            return -1;
        else if(compareRating < this.rating)
            return 1;
        return 0;
    }
}
