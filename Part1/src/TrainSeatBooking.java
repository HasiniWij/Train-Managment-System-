package sample;
import com.mongodb.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.lang.Integer.parseInt;

public class TrainSeatBooking extends Application {
    private static HashMap<String,HashMap<String, List<Integer>>>customersOfAllDates= new HashMap<>();
    Stage windows;
    @Override
    public void start(Stage primaryStage) {
        windows = primaryStage;
        menu();
    }
    public static void main(String[] args) {
        Application.launch();
    }

    private void ordered() {    //specific trip is taken and loaded to the hashMap customers
        HashMap<String, List<Integer>> customers = new HashMap<>();
        System.out.println("Enter the date you want to see the bookings of");
        String date=getDate();
        if (customersOfAllDates.containsKey(date)){
            customers=customersOfAllDates.get(date);
        }
        else{
            System.out.println("No booking made yet");
        }//bubble sort algorithm is used to sort names
        List<String> names = new ArrayList<>(customers.keySet());
        String temp;
        for (int i=0; i<names.size()-1;i++){
            for(int j=0;j<names.size()-1-i;j++) {
                if(names.get(j).compareTo(names.get(j+1))>0){
                    temp=names.get(j);
                    names.set(j,names.get(j+1));
                    names.set(j+1,temp);
                }
            }
        }
        for (String item:names){
            System.out.print("\n"+item+"-");
            numToLetter(customers.get(item));
        }
        menu();
    }

   private void loadData() {//all the previous customer information will load to the hashMap regardless of tme limit
       MongoClient mongoClient=new MongoClient("localhost",27017);
       DB db=mongoClient.getDB("train-Booking");
       DBCollection coll=db.getCollection("AC compartment");
       DBObject dbo = coll.findOne();
       Object details = dbo.get("data");
       customersOfAllDates= (HashMap<String, HashMap<String, List<Integer>>>) details;
       System.out.println("customer details loaded");
       menu();
   }
   private void storeData() {//the entire hashMap is stores as a value for one key
       MongoClient mongoClient=new MongoClient("localhost",27017);
       DB db=mongoClient.getDB("train-Booking");
       DBCollection coll=db.getCollection("AC compartment");
       BasicDBObject doc1=new BasicDBObject();
       coll.remove(doc1);
       doc1.append("data",customersOfAllDates);
       coll.insert(doc1);
       System.out.println("Stored to the database");
       menu();
   }
   private String getDate() {//called in all options except S,L,Q to get the specific trip.
       Scanner sc = new Scanner(System.in);
       Date checkedDate;
       Date today;
       Calendar cal = Calendar.getInstance();
       cal.set(Calendar.HOUR_OF_DAY, 7); //at 7.30 am the last train of each day leave the station
       cal.set(Calendar.MINUTE, 20); // therefore after 7.30 train seats of that particular date wont be shown
       cal.set(Calendar.SECOND, 0);
       cal.set(Calendar.MILLISECOND, 0);
       today = cal.getTime();
       cal.add(Calendar.DAY_OF_MONTH, 10);
       System.out.print("Enter the date(dd/mm/yyyy)");
       String date = sc.nextLine();

       while (true) {
           while (true) {
               try {
                   checkedDate = new SimpleDateFormat("dd/MM/yyyy").parse(date);
                   break;
               }
               catch (ParseException e) {
                   System.out.print("Invalid, Enter the date(dd/mm/yyyy)");
                   date = sc.nextLine();
               }//customer details can be be shown or taken only 10 days in advance.
           }//customer details of past dates wont be shown or taken.
           if (checkedDate.before(today) || checkedDate.after(cal.getTime())) {
               System.out.print("can only book and view 10 days in advance,Re-enter the date(dd/mm/yyyy)");
               date = sc.nextLine();
           }
           else {break;}
       }//only 2 train numbers 1001 & 1002
        System.out.println("Train numbers:\n 1001(Colombo to Badulla)\n 1002(Badulla to Colombo Fort)");
        System.out.print("Enter the train number");
        String trainNumber = sc.nextLine();
        while ((!trainNumber.equals("1001")) && (!trainNumber.equals("1002"))){
            System.out.print("Invalid train number, Enter the train number");
            trainNumber = sc.nextLine();
        }
        return date + " " + trainNumber;
    }

   private void findSeatByCustomer() {//only check the seats of the perticuar trip the customer enters
      System.out.println("Enter the date that you want to find the seat of");
      String date=getDate();
      while (!customersOfAllDates.containsKey(date)){
          System.out.println("No booking made on this date");
          date=getDate();
      }
      HashMap<String, List<Integer>> customers=customersOfAllDates.get(date);
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name ");
        String name = (sc.nextLine()).toLowerCase().trim();
        if (customers.containsKey(name)) {numToLetter(customers.get(name));}
        else {System.out.println("There is no booking under this name");}
        menu();
    }

   private void deleteBooking() {//one seat can be removed at a time
        System.out.println("Enter the date that you booked the seat");
        String date=getDate();
        while (!customersOfAllDates.containsKey(date)){
            System.out.println("No booking made on this date");
            date=getDate();
        }
        HashMap<String, List<Integer>> customers=customersOfAllDates.get(date);
        int rowInNumbers = 0;
        String[] letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
        Scanner input = new Scanner(System.in);
        System.out.println("Enter your name: ");
        try {
            String name = input.nextLine().toLowerCase().trim();
            if (customers.containsKey(name)) {
                System.out.println("Enter the seat you don't want: ");
                String seat = input.nextLine().toUpperCase();
                String row = seat.substring(0, 1);
                for (String item : letter) {
                    if (item.equals(row)) {
                        rowInNumbers = (Arrays.binarySearch(letter, item) + 1);
                        break;
                    }
                }
                int colInNumber = parseInt(seat.substring(1));
                List<Integer> seatsTheCustomerBooked = customers.get(name);

                for (int i = 0; i < seatsTheCustomerBooked.size(); i = i + 2) {
                    if (rowInNumbers == seatsTheCustomerBooked.get(i) && colInNumber == seatsTheCustomerBooked.get(i + 1)) {
                        seatsTheCustomerBooked.remove(i);
                        seatsTheCustomerBooked.remove(i);
                        customers.replace(name, seatsTheCustomerBooked);
                        System.out.println(seat+" seat is removed ");
                        if(customers.get(name).isEmpty()){
                            customers.remove(name);}
                        break;
                    }
                    else if(i==seatsTheCustomerBooked.size()-2){System.out.println("You haven't booked this Seat");}
                }
            }
            else {System.out.println("There is no booking under this name");}
        }
        catch (Exception e) {System.out.println("invalid seat number entered");}
        customersOfAllDates.put(date,customers);
        menu();
    }

   private void emptySeats() {//showing only empty seats of a given trip in GUI
        HashMap<String, List<Integer>> customers = new HashMap<>();
        String date=getDate();
        if (customersOfAllDates.containsKey(date)){
            customers=customersOfAllDates.get(date);
        }
        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, 800, 700);
        windows.setTitle("A/C compartment seat arrangement ");
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        windows.setScene(scene);
        scene.setRoot(root);
        windows.show();
        common(root,"E",customers);
   }

  private void viewAllSeats() {//showing only empty seats of a given trip in GUI
      HashMap<String, List<Integer>> customers = new HashMap<>();
      String date=getDate();
      if (customersOfAllDates.containsKey(date)){
          customers=customersOfAllDates.get(date);
      }
        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, 800, 700);
        windows.setTitle("A/C compartment seat arrangement ");
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        windows.setScene(scene);
        windows.show();
        common(root,"V",customers);
  }
  private List<Integer> common(AnchorPane root,String method,HashMap <String, List<Integer>> customers){
        Rectangle rect;                                             //contain common elements of V,A,E
        List<Integer> bookedSeats = new ArrayList<>();
        rect = new Rectangle(6, 6, 400, 680);
        rect.getStyleClass().add("border");

        Label lbl1 = new Label("A/C Compartment");
        lbl1.setStyle("-fx-font-size: 40px;");
        lbl1.setLayoutY(50);
        lbl1.setLayoutX(450);
        root.getChildren().addAll(rect,lbl1);

        rect = new Rectangle(460, 180, 200, 180);
        rect.getStyleClass().add("border");
        root.getChildren().add(rect);

        rect = new Rectangle(490, 200, 30, 30);
        rect.setStyle("-fx-fill:red;");
        root.getChildren().add(rect);

        rect = new Rectangle(490, 250, 30, 30);
        rect.setStyle("-fx-fill: #8cb9ed;");
        root.getChildren().add(rect);

        rect = new Rectangle(240, 610, 110, 60);
        rect.getStyleClass().add("seat");
        root.getChildren().add(rect);

        Label toilet = new Label("Toilet");
        toilet.setLayoutY(620);
        toilet.setLayoutX(250);
        rect.getStyleClass().add("toilet");

        Label occupied = new Label("Occupied ");
        occupied.setLayoutY(210);
        occupied.setLayoutX(530);

        Label available = new Label("Available");
        available.setLayoutY(260);
        available.setLayoutX(530);

        Button menuButton = new Button("Back to menu");
        menuButton.setLayoutX(580);
        menuButton.setLayoutY(510);

        root.getChildren().addAll(menuButton,available,toilet,occupied);

        for (List<Integer> item : customers.values()){
            for (Object number : item){
                bookedSeats.add((Integer) number);
            }
        }
        menuButton.setOnAction(event -> {
            windows.close();
            menu();
        });
        List<Integer> clickedSeats = new ArrayList<>();
        String[] letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
        int rows = 11, columns = 4, horizontal = 40, vertical = 40;
        double xWidth = 1.5;

        for (int j = 1; j < columns + 1; ++j) {
            int count = 0;
            for (int i = 1; i < rows + 1; ++i) {
                ToggleButton btn = new ToggleButton();
                btn.setText(letter[count] + j);
                root.getChildren().add(btn);

                btn.setLayoutX(horizontal * j * xWidth);
                btn.setLayoutY(vertical * i * 1.4);
                btn.setStyle("-fx-background-color:#8cb9ed");
                if (method.equals("V")){
                    btn.setDisable(true);
                    btn.setStyle("-fx-opacity: 1.0;-fx-background-color:#8cb9ed;");
                }
                for (int value=0;value<bookedSeats.size() ;value=value+2){
                    int colValue= bookedSeats.get(value);
                    int rowValue=bookedSeats.get(value+1);
                    if (i==colValue && j==rowValue ){
                        if (method.equals("E")){root.getChildren().remove(btn);}
                        btn.setStyle( "-fx-background-color:red;-fx-opacity: 1.0;");
                        btn.setDisable(true);
                        break;
                    }
                }
                int rowOfClickedSeat = i;
                int colOfClickedSeat = j;

                btn.setOnAction(event -> {
                    if (btn.isSelected()) {
                        btn.setStyle("-fx-background-color:#34eb9b");
                        clickedSeats.add(rowOfClickedSeat);
                        clickedSeats.add(colOfClickedSeat);
                    }
                    else {
                        btn.setStyle("-fx-background-color:#8cb9ed");
                        clickedSeats.remove(new Integer(rowOfClickedSeat));
                        clickedSeats.remove(new Integer(colOfClickedSeat));
                    }
                });
                count++;
            }
            if (j == 2) {
                xWidth = 2;
                rows = 10;
            } else if (j == 3) {
                xWidth = 1.85;
            }
        }
        return clickedSeats;
    }
  private void addCustomerToSeat() {//each customer can book multiple seats for a given trip
        HashMap<String, List<Integer>> customers = new HashMap<>();
        System.out.println("Enter the date you want to book");
        String date=getDate();
        if (customersOfAllDates.containsKey(date)){
           customers=customersOfAllDates.get(date);
        }

        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, 800, 700);
        windows.setTitle("A/C compartment seat arrangement ");
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        windows.setScene(scene);
        scene.setRoot(root);
        windows.show();

        TextField name = new TextField();
        name.setLayoutY(470);
        name.setLayoutX(550);

        name.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\sa-zA-Z*")) {
                name.setText(newValue.replaceAll("[^\\sa-zA-Z]", ""));
            }
        });

        Label nameLabel = new Label("Name");
        nameLabel.setLayoutY(470);
        nameLabel.setLayoutX(480);

        Button bookButton = new Button("Book");
        bookButton.setLayoutX(530);
        bookButton.setLayoutY(510);

        List<Integer> clickedSeats ;
        clickedSeats=common(root,"A",customers);

        Rectangle rect = new Rectangle(490, 300, 30, 30);
        rect.setStyle("-fx-fill:#34eb9b;");

        Label selected = new Label("selected seat ");
        selected.setLayoutY(300);
        selected.setLayoutX(530);
        root.getChildren().addAll(name,nameLabel,bookButton,rect,selected);

        HashMap<String, List<Integer>> finalCustomers = customers;
        bookButton.setOnAction(event -> {
            Alert a = new Alert(Alert.AlertType.WARNING);
            if (name.getText().isEmpty()) {
                a.setContentText("you need to enter your name");
                a.show();
            }
            else if (clickedSeats.isEmpty()){
                a.setContentText("you need to select seats");
                a.show();
            }
            else {
                String status="";
                for (String key: finalCustomers.keySet()){
                    if(name.getText().equals(key)){
                        a.setContentText("There is already a booking under this name");
                        a.show();
                        status="contains name";
                        break;
                    }
                }
                if(!status.equals("contains name")) {
                    String simpleName = name.getText().toLowerCase();
                    finalCustomers.put(simpleName, clickedSeats);
                    customersOfAllDates.put(date,finalCustomers);
                    name.setText("");
                    windows.close();
                    menu();
                }
            }
        });
    }
  private void numToLetter(List<Integer> seatList) {//as buttons for seats are created by iteration the seat number used for
        String[] letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};// processing is the number of row and column
        for (int i = 0; i < seatList.size(); i = i + 2) {//converting into the format shown to customer.
            String Letter = letter[seatList.get(i) - 1];    //B 1=2 1
            System.out.print(Letter + seatList.get(i + 1)+" ");
        }
    }
  private void menu(){
        Scanner sc = new Scanner(System.in);
        System.out.println("\nEnter \"A\" to add a customer");
        System.out.println("Enter \"V\" to view all seats");
        System.out.println("Enter \"E\" to view empty seats");
        System.out.println("Enter \"D\" to delete a booked seat");
        System.out.println("Enter \"F\" to find a customer name");
        System.out.println("Enter \"O\" to view seats alphabetically ");
        System.out.println("Enter \"S\" to store program data to file ");
        System.out.println("Enter \"L\" to load booking details ");
        System.out.println("Enter \"Q\" to quit");
        String option = sc.next();
        switch (option) {
            case "A":
            case "a":
                addCustomerToSeat();
                break ;

            case "v":
            case "V":
                viewAllSeats();
                break;
            case "E":
            case "e":
                emptySeats();
                break;
            case "D":
            case "d":
                deleteBooking();
                break;
            case "f":
            case "F":
                findSeatByCustomer();
                break;
            case "S":
            case "s":
                storeData();
                break;
            case "L":
            case "l":
               loadData();
                break;
            case "O":
            case "o":
                ordered();
                break;
            case "q":
            case "Q":
                Platform.exit();
                break;
            default:
                System.out.println("Invalid input re-enter ");
                menu();
        }
  }
}










