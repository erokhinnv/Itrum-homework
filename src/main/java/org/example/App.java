package org.example;

import java.util.HashMap;
import java.util.Map;

public class App
{
    public static <T> Map<T, Integer> frequencyCheck(T[] arr) {
        Map<T, Integer> result = new HashMap<>(arr.length);
        for (T iterator : arr) {
            result.put(iterator, result.getOrDefault(iterator, 0) + 1);
        }
        return result;
    }

    public static void main( String[] args ) {
        Integer[] arr = new Integer[6];
        arr[0] = 1;
        arr[1] = 2;
        arr[2] = 3;
        arr[3] = 1;
        arr[4] = 2;
        arr[5] = 1;
        Map<Integer, Integer> result;
        result = frequencyCheck(arr);
        for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}
