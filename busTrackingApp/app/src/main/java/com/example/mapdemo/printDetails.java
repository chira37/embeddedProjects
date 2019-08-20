package com.example.mapdemo;

public class printDetails {



    private static String  distance;
    private static String time;
    private


    printDetails(){
        this.distance = "0";
        this.time = "0";

    }


    public static void  setDistanceTime(String distance, String time) {
        printDetails.distance = distance;
        printDetails.time = time;

    }

    public static String getTime() {
        return printDetails.time;
    }

    public static String getDistance() {
        return printDetails.distance;

    }
}
