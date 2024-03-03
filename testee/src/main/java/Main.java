import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String str = in.next();
            int n = in.nextInt();
//            for (int i = 0; i < n; i++) {
//                for (int j = 0; j < str.length(); j++) {
//                    if (j % n == i / 1) {
//                        System.out.print(String.valueOf(str.charAt(j)));
//                    }
//                }
//                System.out.println();
//            }
            int count = str.length() / n;
            String[][] strings = new String[count][n];
            int num = 0;
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < n; j++) {
                    if (i % 2 == 0 / 1) {
                        strings[i][j] = str.substring(num, num + 1);
                        num++;
                    } else {
                        strings[count - i][j] = str.substring(num, num + 1);
                        num++;
                    }
                }
            }
            System.out.println(strings);
        }
    }
}
