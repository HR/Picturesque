/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.utilities;

public class ArrayUtils {
    /**
     * Returns the index of the first appearance of the value {@code target} in {@code array}.
     *
     * @param arr an array of {@code int} values, possibly empty
     * @param target a primitive {@code int} value
     * @return the least index {@code i} for which {@code array[i] == target}, or {@code -1} if no
     *     such index exists.
     */
    public static int indexOf(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }
}
