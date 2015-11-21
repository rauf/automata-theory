package com.company;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {

        String pathOfFile = "cfg.txt";
        System.out.println("This program takes input from a file named \"cfg.txt\"\nEpsilons are denoted by '$'");

        int indexOfW = 1;
        Map<String, ArrayList<String>> productions;
        Map<String, ArrayList<String>> newProductions;
        Map<Integer, ArrayList<String>> W = new LinkedHashMap<>();


        //regex pattern for '|' , '->' and white spaces.
        Pattern pattern = Pattern.compile("[\\|]|->|\\s");

        //Parsing the file
        productions = parseFile(pattern,pathOfFile);

        //Calculating the W sets
        indexOfW = calculateWSets(productions,W,indexOfW);

        //printing all the W sets
        System.out.println("\nAll the W sets are:");
        printfWsets(W,indexOfW);

        //step3 generating new productions
        newProductions = generateNewProductions(productions,W,indexOfW);

        //Removing $ from productions
        remove$(newProductions);

        //Displaying the new Productions and writing to File
        System.out.println("\nNew Productions are:");
        writeToFile(newProductions);

    }

    public static Map<String,ArrayList<String>> parseFile(Pattern pattern,String pathOfFile) throws IOException {

        BufferedReader input;
        Map<String,ArrayList<String>> productions = new LinkedHashMap<>();
        String str;

        try {
            input = new BufferedReader(new FileReader(pathOfFile));

        while ((str = input.readLine()) != null) {

            if(str.equals(""))
                break;

            ArrayList<String> temp = new ArrayList<>();
            String[] arr = pattern.split(str);

            productions.put(arr[0], temp);

            for (int i = 1; i < arr.length; i++) {
                if (!arr[i].equals(" "))
                    temp.add(arr[i].trim());
            }
            productions.put(arr[0], temp);
        }
        } catch (Exception e) {
            System.out.println("File not found");
            System.exit(1);
        }

        return productions;
    }


    public static int calculateWSets(Map<String,ArrayList<String>> productions,Map<Integer, ArrayList<String>> W, int indexOfW) {

        ArrayList<String> tempList = new ArrayList<>();

        for (Map.Entry<String, ArrayList<String>> entry : productions.entrySet()) {

            for (String s : entry.getValue()) {

                if (s.trim().equals("$")) {
                    tempList.add(entry.getKey());
                }
            }
            W.put(0, tempList);
        }

        //calculating remaining W sets
        while (true) {

            //using linked hast set to avoid duplicates
            LinkedHashSet<String> hashSet = new LinkedHashSet<>();

            //iterating through every entry in original productions
            for (Map.Entry<String, ArrayList<String>> entry : productions.entrySet()) {

                for (String s : entry.getValue()) {

                    for (int i = 0; i < s.length(); i++) {

                        //converting char to String
                        String convertedToString = String.valueOf(s.charAt(i));

                        //since last element of W will be union of all W sets we don not check all other W
                        List<String> latest = W.get(indexOfW - 1);

                        //checking if production is nullable
                        if (latest.contains(convertedToString)) {
                            hashSet.add(entry.getKey());
                        }
                    }
                }
            }

            W.put(indexOfW, new ArrayList<>(hashSet));

            if (W.get(indexOfW).equals(W.get(indexOfW - 1))) {
                break;
            }
            indexOfW++;
        }

        return indexOfW;
    }

    public static Map<String,ArrayList<String>> remove$(Map<String,ArrayList<String>> newProductions) {
        for (Map.Entry<String, ArrayList<String>> entry : newProductions.entrySet()) {

            ArrayList<String> newList = new ArrayList<>();
            for (String s : entry.getValue()) {
                if (!s.equals("$")) {
                    newList.add(s);
                }
            }
            entry.setValue(newList);
        }
        return newProductions;
    }

    public static void writeToFile(Map<String,ArrayList<String>> newProductions) throws IOException {

        BufferedWriter output = null;
        try {
             output = new BufferedWriter(new FileWriter("newCFG.txt"));

            for (Map.Entry<String, ArrayList<String>> entry : newProductions.entrySet()) {

                int i;
                String str1 = "" + entry.getKey() + " -> ";
                System.out.print(str1);
                output.write(str1);

                for (i = 0; i < entry.getValue().size() - 1; i++) {
                    String str2 = entry.getValue().get(i) + " | ";
                    System.out.print(str2);
                    output.write(str2);
                }

                System.out.println(entry.getValue().get(i));
                output.write(entry.getValue().get(i));
                output.newLine();

            }


            System.out.println("\nThe new Productions have also been saved in a file named \"newCFG.txt\"");

        } catch (Exception e) {
            System.out.println("Error Producing Output File");
        } finally {

            if (output != null)
                output.close();
        }
    }

    public static void printfWsets(Map<Integer, ArrayList<String>> W, int indexOfW) {

        for (Map.Entry<Integer, ArrayList<String>> entry : W.entrySet()) {
            System.out.print("W" + entry.getKey() + " = ");
            System.out.println(entry.getValue());
        }
        System.out.println("W" + (indexOfW - 1) + " and W" + indexOfW + " are equal sets");

    }

    public static Map<String, ArrayList<String>> generateNewProductions(Map<String,ArrayList<String>> productions, Map<Integer, ArrayList<String>> W, int indexOfW) {

        Map<String, ArrayList<String>> newProductions = new LinkedHashMap<>(productions);

        for (Map.Entry<String, ArrayList<String>> entry : productions.entrySet()) {

            HashSet<String> hashSet = new HashSet<>();

            for (String s : entry.getValue()) {

                LinkedHashSet<String> set = findCombinations(new LinkedHashSet<>(), W, s, indexOfW, 0);

                if (!entry.getValue().isEmpty())
                    hashSet.addAll(entry.getValue());

                if (!set.isEmpty())
                    hashSet.addAll(set);
            }

            newProductions.get(entry.getKey()).clear();
            newProductions.get(entry.getKey()).addAll(new ArrayList<>(hashSet));
        }
        return newProductions;
    }


    public static LinkedHashSet<String> findCombinations(LinkedHashSet<String> set, Map<Integer, ArrayList<String>> W, String s, int indexOfW, int position) {

        for (int i = position; i < s.length(); i++) {

            String str;
            String convertedToString = String.valueOf(s.charAt(i));
            if (W.get(indexOfW).contains(convertedToString) ) {

                StringBuilder stringBuilder = new StringBuilder(s) ;

                stringBuilder = stringBuilder.deleteCharAt(i);

                //str = string with deleted character
                // s = original string
                str = stringBuilder.toString();

                if (!str.equals(""))
                    set.add(str);

                findCombinations(set, W, str, indexOfW, position);
            }
        }
        return set;
    }
}