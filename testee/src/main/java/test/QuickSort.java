package test;

/**
 * @author mini_zeng
 * @create 2022-04-21 22:26
 */

public class QuickSort {
    /**
        *@Description
        *@author mini_zeng
        *@Date 2022/4/21
        *@Param arr     需要排序的数组
                left    最左侧数据
                right   最右侧数据
        *@return void
        **/
    public static void quickSort(int[] arr,int left,int right){
        if (left > right){
            return ;
        }
        int i,j,pivot,temp;
        // 最左侧从i开始
        i = left;
        // 最右侧从j开始
        j = right;
        // 指定左侧第一个为支点
        pivot = arr[left];

        // 将小于pivot的数字都放在pivot的左边，将大于pivot的数字都放在pivot的右边
        while (i < j){
            // 从右侧开始，查找小于pivot的值
            while (arr[j] >= pivot && i < j){
                j--;
            }
            // 从左侧开始，查找大于pivot的值
            while (arr[i] <= pivot && i < j){
                i++;
            }
            // 交换位置
            if (i < j){
                temp = arr[j];
                arr[j] = arr[i];
                arr[i] = temp;
            }
        }
        // 当i >= j时，交换pivot与arr【i】
        arr[left] = arr[i];
        arr[i] = pivot;

        // 分区完成，两侧递归，同时重复操作进行分区，每次至少有一个数字排到了最终位置
        quickSort(arr,left,i - 1);
        quickSort(arr,i + 1,right);
    }

//    public static void main(String[] args) {
//        int[] arr = {10,7,2,4,7,62,3,4,2,1,8,9,19};
//        quickSort(arr,0,arr.length - 1);
//        for (int i = 0; i < arr.length; i++) {
//            System.out.println(arr[i]);
//        }
//    }
}
