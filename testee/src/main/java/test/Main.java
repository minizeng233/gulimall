package test;

/**
 * @author mini_zeng
 * @create 2022-04-23 10:17
 */
// 功能描述: 表达式是否匹配成功, $表示长度大于0的数字串，*表示任意长的字符串
// 输入参数:@pRule, 以'\0'结束的字符串，表示规则;
//         @pStr, 以'\0'结束的待匹配的字符串;
// 返回值: true:匹配成功; false: 匹配失败
public class Main {
    public static boolean isRegularMatching(String rule, String str) {
        int lRule = rule.length();
        int lStr = str.length();
        int iRule = 0;
        int iStr = 0;
        while (iRule < lRule && iStr < lStr) {
            switch (rule.charAt(iRule)) {
                case '*': {
                    iRule += 1;
                    if (iRule >= lRule) {
                        return true;
                    } else {
                        for (int i = iStr; i < lStr; i++) {
                            if (str.substring(i) == rule.substring(iRule)) {
                                return true;
                            }
                        }
                    }
                    break;
                }
                case '$': {
                    if ( iStr >= lStr) {
                        return false;
                    }
                    while ((iStr < lStr) && (str.charAt(iStr) >= '0') && (str.charAt(iStr) <= '9')) {
                        iStr += 1;
                    }
                    iRule += 1;
                    break;
                }
                default: {
                    if (rule.charAt(iRule) != str.charAt(iStr)) {
                        return false;
                    }
                    iRule += 1;
                    iStr += 1;
                    break;
                }
            }
        }
        if (iRule < lRule && iStr >= lStr) {
            if (rule.charAt(iRule) == '*') {
                return true;
            }
        } else {
            return  iStr >= lStr;
        }
        return false;
    }

    public static void main(String[] args) {
        boolean regularMatching = isRegularMatching("*.txt", "a.txt");
        System.out.println(regularMatching);
    }
}
