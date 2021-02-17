package sample;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import com.mongodb.*;
import java.io.*;
import java.util.*;

public class TrainStation extends Application {

    Stage windows;
    private static ArrayList<Passenger> waitingRoom=new ArrayList<>();
    private static String trip;
    private PassengerQueue queue = new PassengerQueue();


    @Override
    public void start(Stage primaryStage) {
        windows = primaryStage;
        menu();
    }
    public static void main(String[] args) {    //loads data of all dates stored in database without filter at the start of the program.
        HashMap<String,String>customers= new HashMap<>();
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("train-Booking");
        DBCollection coll = db.getCollection("AC compartment");
        DBObject dbo = coll.findOne();
        Object details = dbo.get("data");
        HashMap<String, HashMap<String, List<Integer>>> customersOfAllDates = (HashMap<String, HashMap<String, List<Integer>>>) details;
        HashMap<String, List<Integer>> listName ;
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the date(dd/mm/yyyy): ");
        String date = sc.nextLine();

        loop:
        while(true){
            System.out.println("Train numbers:\n 1001(Colombo to Badulla)\n 1002(Badulla to Colombo Fort)");
            System.out.print("Enter the train number: ");
            String trainNumber = sc.nextLine();
            while ((!trainNumber.equals("1001")) && (!trainNumber.equals("1002"))){
                System.out.print("Invalid train number, Enter the train number");
                trainNumber = sc.nextLine();
            }
            trip= date + " " + trainNumber;

            for(String key: customersOfAllDates.keySet()){
                if(key.equals(trip)){
                    listName = customersOfAllDates.get(key);
                    break loop;
                }
            }

            System.out.print("Invalid Trip, Enter the date(dd/mm/yyyy): ");
            date = sc.nextLine();
        }
        for(String key: listName.keySet()){
            String[] letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
            for (int i = 0; i < listName.get(key).size(); i = i + 2) {
                String Letter = letter[ listName.get(key).get(i) - 1];
                customers.put(Letter+listName.get(key).get(i + 1),key);
            }
        }

        for (String key : customers.keySet()){
            Passenger person=new Passenger(customers.get(key),key);
            waitingRoom.add(person);
        }
        Application.launch();
    }

    private void stimulationReport() {
        queue.setMaxLength(queue.getLength());  //queue length is compared to previous queues  every time the stimulation is run.
        List<Passenger> queueOfPassengers= queue.getQueueArray();
        while(!queue.isEmpty()){
            Random randomNumber=new Random();
            int firstDice=randomNumber.nextInt(6)+1;
            int secondDice=randomNumber.nextInt(6)+1;
            int thirdDice=randomNumber.nextInt(6)+1;
            int delay=firstDice+secondDice+thirdDice;

            for (Passenger person: queueOfPassengers){
                person.setSecondsInQueue(person.getSecondsInQueue()+delay);
            }
            queue.remove();
        }

        AnchorPane root = new AnchorPane();
        windows.setTitle("Queue");
        windows.setScene(new Scene(root, 1400, 700));
        windows.show();

        Label titleLabel = new Label("A/C Compartment Train Queue Report");
        titleLabel.setStyle("-fx-font-size: 35px;");
        titleLabel.setLayoutY(10);
        titleLabel.setLayoutX(200);

        Button menuButton = new Button("Back to menu");
        menuButton.setLayoutX(1100);
        menuButton.setLayoutY(530);
        root.getChildren().addAll(menuButton,titleLabel);

        menuButton.setOnAction(event -> {
            windows.close();
            menu();
        });

        int x=40;
        int y=80;
        int count=1;
        for(Passenger person : queue.getBoardedPassengers()){
            Label nameLabel = new Label();
            nameLabel.setText("Seat: "+person.getSeat()+"\nName: "+person.getName()+"\nWaiting Time: "+person.getSecondsInQueue()+" seconds");
            nameLabel.setLayoutX(x);
            nameLabel.setLayoutY(y);
            root.getChildren().add(nameLabel);
            count++;
            y=y+75;
            if (count==9){x=250;y=80;}
            else if (count==17){x=450;y=80;}
            else if (count==25){x=650;y=80;}
            else if (count==33){x=850;y=80;}
            else if (count==41){x=1050;y=80;}
        }

        Label averageTimeLabel = new Label("The average waiting time of a passenger: "+ queue.getAverageTime());
        averageTimeLabel.setLayoutX(1060);
        averageTimeLabel.setLayoutY(390);


        Label maxTimeLabel = new Label("The maximum waiting time: "+ queue.getMaxStay());
        maxTimeLabel.setLayoutX(1060);
        maxTimeLabel.setLayoutY(420);

        Label minTimeLabel = new Label("The minimum waiting time: "+ queue.getMinStay());
        minTimeLabel.setLayoutX(1060);
        minTimeLabel.setLayoutY(450);

        Label maxLengthLabel = new Label("Maximum length the queue attained: "+ queue.getMaxLength());
        maxLengthLabel.setLayoutX(1060);
        maxLengthLabel.setLayoutY(480);

        root.getChildren().addAll(averageTimeLabel,maxLengthLabel,maxTimeLabel,minTimeLabel);

        File f = new File("C:\\Users\\User\\cw2\\src\\sample\\report.txt");
        FileWriter fw =  null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter(f, true);
            pw = new PrintWriter(fw, true);
            pw.println(trip);
            pw.println("The average waiting time of a passenger: "+ queue.getAverageTime());
            pw.println("The maximum waiting time: "+ queue.getMaxStay());
            pw.println("The minimum waiting time: "+ queue.getMinStay());
            pw.println("Maximum length the queue attained: "+ queue.getMaxLength());
            for(Passenger person : queue.getBoardedPassengers()){
                pw.println("Seat: "+person.getSeat()+"\nName: "+person.getName()+"\nWaiting Time: "+person.getSecondsInQueue()+" seconds\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                fw.close();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private  void loadData()  {

        ArrayList<HashMap> dataOfAllDays;
        File file = new File("C:\\Users\\User\\cw2\\src\\sample\\ObjectFile.txt");
        HashMap <String,ArrayList<Passenger>>waitingRoomData ;
        HashMap<String,PassengerQueue> queueData;
        if (file.length() != 0 && file.exists()){   //checks whether file exists  or is empty
                dataOfAllDays=fileLoad();
                waitingRoomData = dataOfAllDays.get(0);
                queueData= dataOfAllDays.get(1);


            Scanner sc = new Scanner(System.in);
            System.out.print("Enter the date(dd/mm/yyyy): ");
            String date = sc.nextLine();

            loop:
            while(true){

                System.out.println("Train numbers:\n 1001(Colombo to Badulla)\n 1002(Badulla to Colombo Fort)");
                System.out.print("Enter the train number: ");
                String trainNumber = sc.nextLine();
                while ((!trainNumber.equals("1001")) && (!trainNumber.equals("1002"))){
                    System.out.print("Invalid train number, Enter the train number");
                    trainNumber = sc.nextLine();
                }
                trip= date + " " + trainNumber;

                for(String key: waitingRoomData.keySet()){ //sets all values of the queue as loaded
                    if(key.equals(trip)){
                        waitingRoom=waitingRoomData.get(trip);
                        queue.setBoardedPassengers(queueData.get(trip).getBoardedPassengers());
                        queue.setQueueArray(queueData.get(trip).getQueueArray());
                        queue.setInitialMaxLength(queueData.get(trip).getMaxLength());
                        queue.setMaxStay(queueData.get(trip).getMaxStay());
                        queue.setMinStay(queueData.get(trip).getMinStay());
                        break loop;
                    }
                }

                System.out.print("Invalid Trip, Enter the date(dd/mm/yyyy)");
                date= sc.nextLine();
            }
        }
        else{
            System.out.println("No data stored");
        }
        menu();
    }

    private void storeData() {
        ArrayList<HashMap> dataOfAllDays;
        File file = new File("C:\\Users\\User\\cw2\\src\\sample\\ObjectFile.txt");
        HashMap <String,ArrayList<Passenger>>waitingRoomData =  new HashMap<>();
        HashMap<String,PassengerQueue> queueData= new HashMap<>();
        if (file.length() != 0 && file.exists()){   //if file exists or and has data, data is loaded 1st as when writing to file again all previous data is erased.
            dataOfAllDays=fileLoad();
            waitingRoomData = dataOfAllDays.get(0);
            queueData= dataOfAllDays.get(1);

        }

        waitingRoomData.put(trip,waitingRoom);
        queueData.put(trip,queue);


        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(waitingRoomData);

            oos.writeObject(queueData);
           System.out.println("stored to the file");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                assert oos != null;
                fos.close();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        menu();

    }

    private void deletePassenger() {    //names are stored and used in lower case and seat in upper case.
        Scanner sc=new Scanner(System.in);

        System.out.printf("Enter name: ");
        String name=sc.nextLine().toLowerCase();

        System.out.printf("Enter seat: ");
        String seat=sc.nextLine().toUpperCase();

        Passenger personRemoved=queue.removeParticularPassenger(name,seat);
        if(personRemoved != null){
            waitingRoom.add(personRemoved);
        }
        menu();
    }

    private void viewQueue() {
        AnchorPane root = new AnchorPane();
        windows.setTitle("Queue");
        windows.setScene(new Scene(root, 1100, 700));
        windows.show();

        Label titleLabel = new Label("A/C Compartment");
        titleLabel.setStyle("-fx-font-size: 40px;");
        titleLabel.setLayoutY(50);
        titleLabel.setLayoutX(750);

        Rectangle waitingRectangle = new Rectangle(790, 200, 30, 30);
        waitingRectangle.setStyle("-fx-fill:#8cb9ed;");

        Rectangle queueRectangle = new Rectangle(790, 250, 30, 30);
        queueRectangle.setStyle("-fx-fill: #dec7f0;");

        Rectangle boardRectangle = new Rectangle(790, 300, 30, 30);
        boardRectangle.setStyle("-fx-fill: #f7ecbe;");

        Rectangle notBookedRectangle = new Rectangle(790, 350, 30, 30);
        notBookedRectangle.setStyle("-fx-fill: grey;");

        Label waitingLabel = new Label("Passengers in the waiting room ");
        waitingLabel.setLayoutY(210);
        waitingLabel.setLayoutX(830);

        Label queueLabel = new Label("Passengers in the Queue");
        queueLabel.setLayoutY(260);
        queueLabel.setLayoutX(830);

        Label boardedLabel = new Label("passengers boarded to the train");
        boardedLabel.setLayoutY(300);
        boardedLabel.setLayoutX(830);

        Label notBookedLabel = new Label("Not Booked seats");
        notBookedLabel.setLayoutY(360);
        notBookedLabel.setLayoutX(830);   Button menuButton = new Button("Back to menu");
        menuButton.setLayoutX(880);
        menuButton.setLayoutY(410);

        menuButton.setOnAction(event -> {
            windows.close();
            menu();
        });

        root.getChildren().addAll(waitingRectangle,queueRectangle,titleLabel,queueLabel,waitingLabel,boardedLabel,boardRectangle,notBookedRectangle,notBookedLabel,menuButton);
        String[] letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
        int rows = 11, columns = 4, horizontal = 40, vertical = 40;
        double xWidth = 3.5;

        for (int j = 1; j < columns + 1; ++j) {
            int count = 0;

            for (int i = 1; i < rows + 1; ++i) {
                Label nameLabel = new Label();
                String status="not booked";
                root.getChildren().add(nameLabel);

                nameLabel.setLayoutX(horizontal * j * xWidth);
                nameLabel.setLayoutY(vertical * i * 1.4);

                for(Passenger item : waitingRoom){
                    if((letter[count] + j).equals(item.getSeat())){
                        nameLabel.setStyle("-fx-background-color:#8cb9ed;-fx-min-width: 130px;-fx-min-height: 35px;");
                        nameLabel.setText(letter[count] + j+"\n"+ "Empty");
                        status="waiting";
                        break;
                    }
                }
                if (!status.equals("waiting")){
                    for (Passenger item: queue.getQueueArray()) {
                        if ((letter[count] + j).equals(item.getSeat())) {
                            nameLabel.setStyle("-fx-background-color:#dec7f0;-fx-min-width: 130px;-fx-min-height: 35px;");
                            nameLabel.setText(letter[count] + j + "\n" + item.getName());
                            status = "queue";
                            break;
                        }
                    }
                }
                if(status.equals("not booked")) {
                    for (Passenger item : queue.getBoardedPassengers()) {
                        if ((letter[count] + j).equals(item.getSeat())) {
                            nameLabel.setStyle("-fx-background-color:#f7ecbe;-fx-min-width: 130px;-fx-min-height: 35px;");
                            nameLabel.setText(letter[count] + j + "\n" + item.getName());
                            status = "boarded";
                            break;
                        }
                    }
                }
                if(status.equals("not booked")){
                    nameLabel.setStyle("-fx-background-color:grey;-fx-min-width: 130px;-fx-min-height: 35px;");
                    nameLabel.setText(letter[count] + j + "\n"+"Not booked");
                }
                count++;
            }
        }
    }

    private void addPassenger() {

        AnchorPane root = new AnchorPane();
        windows.setTitle("Queue");
        windows.setScene(new Scene(root, 1200, 700));
        windows.show();

        Label titleLabel = new Label("A/C Compartment Train Queue");
        titleLabel.setStyle("-fx-font-size: 30px;");
        titleLabel.setLayoutY(10);
        titleLabel.setLayoutX(300);

        Label waitingLabel = new Label("Waiting Room");
        waitingLabel.setStyle("-fx-font-size: 20px;");
        waitingLabel.setLayoutY(60);
        waitingLabel.setLayoutX(840);

        Label queueLabel = new Label("Passenger Queue");
        queueLabel.setStyle("-fx-font-size: 20px;");
        queueLabel.setLayoutY(60);
        queueLabel.setLayoutX(100);

        Button menuButton = new Button("Back to menu");
        menuButton.setLayoutX(50);
        menuButton.setLayoutY(20);
        root.getChildren().addAll(menuButton,titleLabel,waitingLabel,queueLabel);


        Random rand = new Random();
        int random = rand.nextInt(6) + 1;
        if (waitingRoom.size()==0){ //checkng wheather waiting room is empty
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setContentText("No passengers in the waiting room");
            a.show();
        }

        else{   //if the random number is bigger than the number of passengers in the waiting room,all remaining passengers are added to queue.
            if (random>waitingRoom.size()){
                random=waitingRoom.size();
            }
            for(int i=0; i<random;i++){
                queue.add(waitingRoom.remove(0));
            }
        }


        int x=20;
        int y=100;
        List<Passenger> queueOfPassengers= queue.getQueueArray();
        int count=1;
        for (Passenger item: queueOfPassengers){
            Label queuePassenger=new Label(item.getSeat()+" - "+item.getName());
            queuePassenger.setStyle("-fx-background-color:#dec7f0;-fx-min-width: 180px;-fx-min-height: 35px;");
            queuePassenger.setLayoutY(y);
            queuePassenger.setLayoutX(x);
            root.getChildren().add(queuePassenger);
            y=y+55;
            count++;
            if (count==12){x=220;y=100;}
            else if (count==23){x=430;y=100;}
            else if (count==34){x=630;y=100;}
        }
        x=1000;
        y=100;
        count=1;
        for (Passenger item: waitingRoom){
            Label queuePassenger=new Label(item.getSeat()+" -"+item.getName());
            queuePassenger.setStyle("-fx-background-color:#8cb9ed;-fx-min-width: 180px;-fx-min-height: 35px;");
            queuePassenger.setLayoutY(y);
            queuePassenger.setLayoutX(x);
            root.getChildren().add(queuePassenger);
            y=y+55;
            count++;
            if (count==12){x=800;y=100;}
            if (count==23){x=600;y=100;}
            else if (count==34){x=400;y=100;}

        }
        menuButton.setOnAction(event -> {
            windows.close();
            menu();
        });


    }
    private void menu() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\nEnter \"A\" to add passenger to train queue");
        System.out.println("Enter \"V\" to view the train queue");
        System.out.println("Enter \"D\" to delete passenger from train queue");
        System.out.println("Enter \"S\" to store train queue data ");
        System.out.println("Enter \"L\" to load train queue data ");
        System.out.println("Enter \"R\" to Run the stimulation and produce the report ");
        System.out.println("Enter \"Q\" to quit");
        String option = sc.next();
        switch (option) {
            case "A":
            case "a":
                addPassenger();
                break ;

            case "v":
            case "V":
                viewQueue();
                break;
            case "D":
            case "d":
                deletePassenger();
                break;
            case "S":
            case "s":
                storeData();
                break;
            case "L":
            case "l":
                loadData();
                break;
            case "R":
            case "r":
                stimulationReport();
                break;
            case "q":
            case "Q":
                break;
            default:
                System.out.println("Invalid input re-enter ");
                menu();
        }

    }
    public ArrayList fileLoad(){    //called when both storing and loading to file.
        ArrayList <HashMap> dataOfAllDates =new ArrayList<>();
        File file = new File("C:\\Users\\User\\cw2\\src\\sample\\ObjectFile.txt");
        FileInputStream f ;
        ObjectInputStream s = null;
        HashMap<String,Object> waitingRoomData = null;
        HashMap<String,Object> boardedPassengerData=null;
        try {
            f = new FileInputStream(file);
            s = new ObjectInputStream(f);
            waitingRoomData = (HashMap<String,Object>)s.readObject();
            boardedPassengerData=(HashMap<String,Object>)s.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataOfAllDates.add(waitingRoomData);
        dataOfAllDates.add(boardedPassengerData);
        return dataOfAllDates;
    }
}


