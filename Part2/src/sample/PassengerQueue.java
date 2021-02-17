package sample;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PassengerQueue implements Serializable {

    private List <Passenger>queueArray =new ArrayList<>();
    private List <Passenger>boardedPassengers =new ArrayList<>();
    private int maxLength=0;
    private int maxStay =0;
    private int minStay =100;

    public  void setMaxStay(int maxStay){
        this.maxStay=maxStay;
    }   //maxStay and minStay is only set when loading.

    public  void setMinStay(int minStay){ this.minStay=minStay; }

    public void setQueueArray(List<Passenger> queueArray) {
        this.queueArray = queueArray;
    }

    public void add(Passenger next){
        queueArray.add(next);
    }

    public boolean isEmpty(){
        return queueArray.size() == 0;
    }    //checking whether queue is empty

    public int getLength(){
        return queueArray.size();
    }

    public int getMaxStay(){
        for (Passenger person : boardedPassengers) {
            int personTime = person.getSecondsInQueue();
            maxStay = Math.max(personTime, maxStay);
        }
        return maxStay;
    }

    public int getMinStay(){
        for (Passenger person : boardedPassengers) {
            int personTime = person.getSecondsInQueue();
            minStay = Math.min(personTime, minStay);
        }
        return minStay;
    }

    public double getAverageTime(){
        double totalWaitingTime=0;
        for (Passenger person : boardedPassengers) {
            totalWaitingTime=person.getSecondsInQueue()+totalWaitingTime;
        }
        return Math.round((totalWaitingTime/boardedPassengers.size() )* 100.00) / 100.00;
    }

    public void remove(){   //used to remove passengers from queue when boarding
        boardedPassengers.add(queueArray.get(0));
        queueArray.remove(0);

    }
    public Passenger removeParticularPassenger(String name, String seat){   //specifically delete a passenger in the queue

        for(Passenger item :queueArray){
            if(item.getName().equals(name)&& item.getSeat().equals(seat)){
                queueArray.remove(item);
                System.out.println("Passenger removed from queue");
                System.out.println("name: "+item.getName()+"   seat: "+item.getSeat());
                return item;
            }
        }
        System.out.println("Passenger not found");
        return null;
    }

    public List<Passenger> getQueueArray(){
        return queueArray;
    }

    public void setMaxLength(int length){
        this.maxLength=Math.max(maxLength,length);
    }

    public void setInitialMaxLength(int length){
        this.maxLength=length;
    }   //used when loading data

    public int getMaxLength(){
        return maxLength;
    }

    public List<Passenger> getBoardedPassengers(){
        return boardedPassengers;
    }

    public void setBoardedPassengers(List<Passenger>boardedPassengers){
        this.boardedPassengers=boardedPassengers;
    }

}

