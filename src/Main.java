import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {

    private static HashMap<String, ArrayList<Integer>> countryMap;
    private static ArrayList<String> nonReporters;

    public static void main(String[] args) throws FileNotFoundException {

        countryMap = new HashMap<>();
        nonReporters = new ArrayList<>();

        Scanner maleData = new Scanner(new File("databases/maleData.csv"));
        maleData.useDelimiter(",");

        //date to compare before and after is 1989

        while (maleData.hasNext()) {
            String country = "";
            ArrayList<Integer> tempList = new ArrayList<>();
            for (int i = 0; i < 52; i++) {
                String thisLine = maleData.next();
                if (thisLine.isEmpty()) {
                    tempList.add(-1);
                    continue;
                }
                if (i == 0) {
                    country = thisLine;
                } else {
                    tempList.add(stringToInt(thisLine));
                }
            }
            countryMap.put(country, tempList);
        }

        maleData.close();

        recordData();

        filterReporters();

        //nonReporters.forEach(a -> System.out.println(a));
        countryMap.forEach((a, b) -> System.out.println(a + " " + b));
    }

    public static int stringToInt(String number) {
        int newNum = -1;
        try {
            newNum = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            throw new RuntimeException("stringToInt failed to convert to int " + e);
        }
        return newNum;
    }

    public static void recordData() {
        ArrayList<Integer> newList = new ArrayList<>();
        countryMap.forEach((a, b) -> countryMap.replace(a, b, newList(b)));
    }

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
                    double average = sum / localCount;
                    newList.add((int) average);
                }

                count = 0;
                templist.clear();
                flag = false;
            }

            templist.add(integer);
            if (integer != -1)
                flag = true;

            count++;
        }

        return newList;
    }

    private static void filterReporters() {
        for (Map.Entry<String, ArrayList<Integer>> entrySet : countryMap.entrySet()) {
            if (checkFirstFour(entrySet.getValue())) {
                nonReporters.add(entrySet.getKey());
            }
        }

        for (String country : nonReporters) {
            countryMap.remove(country);
        }


    }

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


    private static void findSignificantChange() {

    }

}