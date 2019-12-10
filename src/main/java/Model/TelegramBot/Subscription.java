package Model.TelegramBot;

import java.io.Serializable;
import java.sql.Timestamp;

public class Subscription implements Serializable{

    private String code;
    private int hours;
    private boolean isReusable;
    private Timestamp tillDate;
    private long userId;

    public Subscription(String code, int hours, long userId) {
        this.code = code;
        this.hours = hours;
        this.isReusable = false;
        this.tillDate = new Timestamp(System.currentTimeMillis() + (1000L*60*60*hours));
        this.userId = userId;
    }

    public Subscription(String code, int hours) {
        this.code = code;
        this.hours = hours;
        this.isReusable = false;
        this.tillDate = new Timestamp(System.currentTimeMillis() + (1000L*60*60*hours));
    }

    public Subscription(String code, Timestamp tillDate, long userId) {
        this.code = code;
        this.tillDate = tillDate;
        this.isReusable = true;
        this.userId = userId;
    }

    public Subscription(String code, Timestamp tillDate) {
        this.code = code;
        this.tillDate = tillDate;
        this.isReusable = true;
    }

    public Subscription() {
        code = null;
        hours = 0;
    }

    public String getCode() {
        return code;
    }

    public int getHours() {
        return hours;
    }

    public boolean isReusable() {
        return isReusable;
    }

    public Timestamp getTillDate() {
        return tillDate;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "code='" + code + '\'' +
                ", hours=" + hours +
                ", isReusable=" + isReusable +
                ", tillDate=" + tillDate +
                '}';
    }
}
