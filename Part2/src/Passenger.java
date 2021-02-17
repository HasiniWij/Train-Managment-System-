package sample;

import java.io.Serializable;

public class Passenger implements Serializable {
    private String name;
    private String seat;
    private int secondsInQueue;

    public Passenger(String name ,String seat){ //when creating passenger objects a name and a seat number essential
        super();
        this.name=name;
        this.seat=seat;
    }

    public String getName(){
        return name;
    }   //full name

    public int getSecondsInQueue(){
        return secondsInQueue;
    }

    public void setSecondsInQueue(int sec) {
        this.secondsInQueue = sec;
    }

    public String getSeat(){
        return seat;
    }
}
