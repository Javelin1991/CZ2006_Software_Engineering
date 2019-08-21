package seaplus.seefood.model;


/**
 * Created by Lock on 1/11/2017.
 */

public class Carpark {

    String category, weekdays_rate1, weekdays_rate2, sunday_public_holiday_rate, carpark, saturday_rate;
    double latitude, longitude;

    public Carpark(String category, String weekdays_rate1, String weekdays_rate2, String sunday_public_holiday_rate, String carpark, String saturday_rate, double latitude, double longitude) {
        this.category = category;
        this.weekdays_rate1 = weekdays_rate1;
        this.weekdays_rate2 = weekdays_rate2;
        this.sunday_public_holiday_rate = sunday_public_holiday_rate;
        this.carpark = carpark;
        this.saturday_rate = saturday_rate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getWeekdays_rate1() {
        return weekdays_rate1;
    }

    public void setWeekdays_rate1(String weekdays_rate1) {
        this.weekdays_rate1 = weekdays_rate1;
    }

    public String getWeekdays_rate2() {
        return weekdays_rate2;
    }

    public void setWeekdays_rate2(String weekdays_rate2) {
        this.weekdays_rate2 = weekdays_rate2;
    }

    public String getSunday_public_holiday_rate() {
        return sunday_public_holiday_rate;
    }

    public void setSunday_public_holiday_rate(String sunday_public_holiday_rate) {
        this.sunday_public_holiday_rate = sunday_public_holiday_rate;
    }

    public String getCarpark() {
        return carpark;
    }

    public void setCarpark(String carpark) {
        this.carpark = carpark;
    }

    public String getSaturday_rate() {
        return saturday_rate;
    }

    public void setSaturday_rate(String saturday_rate) {
        this.saturday_rate = saturday_rate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
