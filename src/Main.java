import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {

    //HashMap containing data read from .csv files
    private static HashMap<String, ArrayList<Integer>> countryMap;

    //list countries who report no data before 1989
    private static ArrayList<String> nonReporters;

    //Map of countries who show significant improvement (-5% from lowest reported 5-year mean prior to 1989)
    //after 1989 with accompanying 5-year averages
    private static HashMap<String, ArrayList<Integer>> significantImprovement;

    //list of countries who show significant unimprovement (+5% from lowest reported 5-year mean prior to 1989)
    //after 1989 with acompanying 5-year averages
    private static HashMap<String, ArrayList<Integer>> significantUnimprovement;

    //list of countries who showed no significant change in data after 1989 ratification
    private static ArrayList<String> noSignificantChange;

    public static void main(String[] args) throws IOException {

        //initialize lists and datasets
        countryMap = new HashMap<>();
        nonReporters = new ArrayList<>();
        significantUnimprovement = new HashMap<>();
        significantImprovement = new HashMap<>();
        noSignificantChange = new ArrayList<>();

        //reads the dataset
        Scanner userChoice = new Scanner(System.in);
        Boolean flag = false;
        String pathname = "";
        String whichBase = "";

        //allows the user to choose which dataset to analyze
        //NOTE: currently this implementation only supports the male database
        while (!flag) {
            System.out.print("Enter 'm' for male database analytics and and 'f' for female database analytics: ");
            whichBase = userChoice.nextLine();
            //reads data from maleData.csv into countryMap
            if (whichBase.equals("m")) {
                pathname = "databases/maleData.csv";
                flag = true;
            }
            else if (whichBase.equals("f")) {
                pathname = "databases/femaleData.csv";
                flag = true;
            }

        }

        Scanner maleData = new Scanner(new File(pathname));
        maleData.useDelimiter(","); //separates values in dataset by comma

        //Reads the .cvs file into the local dataset (countryMap)
        while (maleData.hasNext()) { //loops through the data
            String country = "";
            ArrayList<Integer> tempList = new ArrayList<>();
            for (int i = 0; i < 52; i++) { //loops through the dataset for each country
                String thisLine = maleData.next();
                if (thisLine.isEmpty()) { //fills -1 in map when no data is reported
                    tempList.add(-1);
                    continue;
                }
                if (i == 0) {
                    country = thisLine;
                } else {
                    tempList.add(stringToInt(thisLine));
                }
            }
            countryMap.put(country, tempList); //adds each country and its reported data into countryMap
        }

        //closes the file after data has been read
        maleData.close();

        //calls helper method to replace data in countryMap with 5-year averages
        recordData();

        //removes countries that do not report data before 1989 from countryMap and
        //adds them to the nonReporters list;
        filterReporters();

        //splits countryMap into 3 separate datasets, denoting countries that have showed significant improvement
        //since 1989, who have significantly worsened since 1989, and who have shown no major change.
        findSignificantChange();

        //TESTERS
        //nonReporters.forEach(a -> System.out.println(a));
        //countryMap.forEach((a, b) -> System.out.println(a + " " + b));


        //OUTPUT
        //the following print statements display the results to console - they can be commented out
        //and do not affect the output files
        System.out.println("Database: " + pathname);
        System.out.println();

        System.out.println("Number of non-reporters: " + nonReporters.size());
        System.out.println();

        System.out.println("Countries Who Have Shown Improvement: " + significantImprovement.size() + " Countries");
//        System.out.println();
//        significantImprovement.forEach((a, b) -> System.out.println(a + " " + b));
        System.out.println();

        //OUTPUT FILES
        //Creates the output (results) .csv files
        Path filename = Path.of("");
        if (whichBase.equals("m"))
            filename = Path.of("Output/improvement-male.csv");
        else
            filename = Path.of("Output/imrpovement-female.csv");

        //Writes to significantImprovement output .csv
        String signiciantText = "";
        for (Map.Entry<String, ArrayList<Integer>> element : significantImprovement.entrySet()) {
            signiciantText += mapToString(element.getKey(), element.getValue());
        }
        Files.writeString(filename, signiciantText);

        if (whichBase.equals("m"))
            filename = Path.of("Output/unimprovement-male.csv");
        else
            filename = Path.of("Output/unimrpovement-female.csv");

        //Writes to significant unimprovement output .csv
        signiciantText = "";
        for (Map.Entry<String, ArrayList<Integer>> element : significantUnimprovement.entrySet()) {
            signiciantText += mapToString(element.getKey(), element.getValue());
        }
        Files.writeString(filename, signiciantText);

       System.out.println("Significant Unimprovement: " + significantUnimprovement.size() + " Countries");
////        System.out.println();
////        significantUnimprovement.forEach((a,b) -> System.out.println(a + " " + b));
//        System.out.println();
//

        //Creates output .txt list of countries that showed no significant change
        if (whichBase.equals("m"))
            filename = Path.of("Output/noSignificantChange-male.txt");
        else
            filename = Path.of("Output/noSignificantChange-female.txt");

        signiciantText = noSignificantChange.toString();

        Files.writeString(filename, signiciantText);

        System.out.println("Number of countries that showed no significant change: " + noSignificantChange.size());
////        System.out.println("List:");
////        noSignificantChange.forEach(System.out::println);
//        System.out.println();
    }

    //This helper method converts a single line of the countryMap dataset into a String
    //and returns that value
    private static String mapToString(String country, ArrayList<Integer> data) {
        String newString = country + ",";
        for (int i = 0; i < data.size(); i++) {
            newString += data.get(i) + ",";
        }
        newString += "\n";
        return newString;
    }

    //This helper method converts numbers from String objects to Integer objects
    public static int stringToInt(String number) {
        int newNum = -1;
        try {
            newNum = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            throw new RuntimeException("stringToInt failed to convert to int " + e);
        }
        return newNum;
    }

    //Helper method which creates 5-year averages for the dataset and replaces
    //the original data in countryMap with those averages
    public static void recordData() {
        countryMap.forEach((a, b) -> countryMap.replace(a, b, newList(b)));
    }

    //recordData() helper method which creates the 5-year averages for each individual country
    //in this method, a single's countries data is passed as a param, and the new average list is returned
    private static ArrayList<Integer> newList(ArrayList<Integer> oldList) {
        ArrayList<Integer> newList = new ArrayList<>(); //create new return list

        int count = 0;
        ArrayList<Integer> templist = new ArrayList<>();
        boolean flag = false; //flag to check if non -1 value passed

        for (Integer integer : oldList) { //iterate through oldList

            if (count == 5) {

                if (!flag) //all 5 elements in subset = -1
                    newList.add(-1);
                else { //some elements were valid elements
                    double sum = 0;
                    double localCount = 0;
                    for (int num : templist) {
                        if (num != -1) {
                            sum += num;
                            localCount++;
                        }
                    }
                    double average = sum / localCount; //calculate the average of this 5-year span
                    newList.add((int) average); //adds the average to the new dataList
                }

                //reset fields for next 5-year period
                count = 0;
                templist.clear();
                flag = false;
            }

            templist.add(integer);
            if (integer != -1)
                flag = true;

            count++;
        }

        return newList; //return resulting list
    }

    //This helper method removes all countries who did not report sufficient data
    //from country map and adds them to a new list: nonReporters
    private static void filterReporters() {
        for (Map.Entry<String, ArrayList<Integer>> entrySet : countryMap.entrySet()) {
            if (checkFirstFour(entrySet.getValue())) { //calls helper method
                nonReporters.add(entrySet.getKey()); //adds non-reporters to nonReporters list
            }
        }

        for (String country : nonReporters) { //removes all non-reporters from countryMap
            countryMap.remove(country);
        }


    }

    //filterReporters() helper method, returns true if a country reported no data before 1989
    private static boolean checkFirstFour(ArrayList<Integer> list) {
        boolean flag = true;
        for (int i = 0; i < 4; i++) {
            if (list.get(i) != -1) {
                flag = false;
                break;
            }
        }
        return flag;
    }


    //This method iterates through countryMap and determines if a country has shown significant change in their
    //data since 1989. Countries are then sorted into one of three datasets accordingly.
    private static void findSignificantChange() {
        //iterates through every item in countryMap;
        for (Map.Entry<String, ArrayList<Integer>> entrySet : countryMap.entrySet()) {
            ArrayList<Integer> dataSet = entrySet.getValue(); //assigns each country's data to a local variable

            int min = -1;
            int max = -1; //temporary assingment of min to negative value
            final double percentDiff = 0.05; //final int - denotes the percent change we are looking for from the min
            //(5%)
            double significantGap; //initializes variable which will be used to store to quantity of 5% from the min
            // for each country

            for (int i = 0; i < dataSet.size(); i++) {
                if (dataSet.get(i) == -1)
                    continue;
                else if (i == 0) {//assigns the min to the first element in the data
                    max = dataSet.get(i);
                    min = dataSet.get(i);
                }
                else if (i < 4) { //assigns min to the lowest of the first 4 values (values before 1989)
                    if (dataSet.get(i) > max)
                        max = dataSet.get(i);
                    if (dataSet.get(i) < min)
                        min = dataSet.get(i);
                } else {
                    significantGap = percentDiff * max; //calculates the difference from the min we are looking for

                    if (dataSet.get(i) <= min - significantGap) { //a country has made significant improvement
                        significantImprovement.put(entrySet.getKey(), dataSet); //adds improved countries to dataset
                    } else if (dataSet.get(i) >= max + significantGap) {//a country has significantly worsened since
                        significantUnimprovement.put(entrySet.getKey(), dataSet);
                    } else {}//no significant change detected
                }
            }
        }

        //adds all countries who were not determined to have significant change to noSignificantChange list
        for (Map.Entry<String, ArrayList<Integer>> entrySet : countryMap.entrySet()) {
            if (!significantImprovement.containsKey(entrySet.getKey()) &&
            !significantUnimprovement.containsKey(entrySet.getKey()))
                noSignificantChange.add(entrySet.getKey());
        }

    }
}