package com.junting.gulimall.auth;

import java.util.Arrays;

/**
 * @author mini_zeng
 * @create 2022-02-16 21:41
 */


public class Practice {
    private static void bubbleSort(int[] arr) {
        int temp;//定义一个临时变量
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j + 1] < arr[j]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    public static void main(String[] args) {
        int arr[] = new int[]{1,6,2,2,5};
        Practice.bubbleSort(arr);
        System.out.println(Arrays.toString(arr));
    }

    private static void quickSort(int[] arr, int leftIndex, int rightIndex){
        if (leftIndex >= rightIndex){
            return;
        }

        int left = leftIndex;
        int right = rightIndex;
        //待排序的第一个元素作为基准值
        int key = arr[left];

        //从左右两边交替扫描，直到left = right
        while (left < right){
            while (right > left && arr[right] >= key) {
                //从右往左扫描，找到第一个比基准值小的元素
                right--;
            }
            //找到这种元素将arr[right]放入arr[left]中
            arr[left] = arr[right];

            while (right > left && arr[left] <= key){
                //从左往右扫描，找到第一个比基准值大的元素
                left++;
            }
            //找到这种元素将arr[left]放入arr[right]中
            arr[right] = arr[left];
        }
        //基准值归位
        arr[left] = key;


    }
}
