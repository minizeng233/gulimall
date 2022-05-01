//package test;
//
//import java.util.Map;
//
///**
// * @author mini_zeng
// * @create 2022-04-23 10:43
// */
//// https://www.csdn.net/tags/MtjakgysMjA1NTItYmxvZwO0O0OO0O0O.html
///*
//    输入任意一种物质，要求输出其每种元素的数量。
//    H2O
//    H2SO4
//    比如 输入CaCO3，其组成分别为Ca：1，C：1，O：3，输出Ca1C1O3
//    输入Fe2(SO4)3，其组成分别为Fe：2，S：3，O：12，输出Fe2S3O12
//   （注意：元素名称首字母大写，剩余字母都小写；
//   括号括起来表示括号中的结构作为整体出现多少次）
//    **/
//class Solution {
//    public String countOfAtoms(String formula) {
//        String resStr = "";
//        Map<String, Integer> hashMap = null;//用于统计各个原子出现的次数（已经按照字典升序排序）
//        myFunc(formula, 0, hashMap);//从起始开始处理formula
//        //最后将得到的结果进行合并成字符串
//        for (Integer item : hashMap) {
//            resStr += item.first;
//            if (item.second > 1) {//只有当这个元素出现多次时才需要加上倍数，比如"H2O"中的O只出现了一次，所以不需要倍数
//                resStr += to_String(item.second);
//            }
//        }
//        return resStr;
//    }
//    //从formula.charAt(nowIndex)开始处理剩余的化学式，返回处理后剩余的起始下标
//    int myFunc(String formula, int nowIndex, Map<String, Integer> hashMap) {
//        int formulaSize = formula.length();
//        if (nowIndex >= formulaSize || formula.charAt(nowIndex) == ')') {
//            return nowIndex + 1;
//        }
//        while (nowIndex < formulaSize) {
//            if (formula.charAt(nowIndex) == '(') {
//                //第一种情况：遇到了左括号，递归处理括号内部的
//                Map<String, Integer> tempMap;//统计括号内部各个原子出现的次数
//                nowIndex = myFunc(formula, nowIndex + 1, tempMap);//返回的是当前左括号匹配的右括号后面的第一个字符位置
//                //计算右括号后的倍率，如"Mg(OH)2"中“(OH)”后的数字2
//                int cnt = 0;
//                while (nowIndex < formulaSize && formula.charAt(nowIndex) >= '0' && formula.charAt(nowIndex) <= '9') {
//                    cnt = cnt * 10 + formula.charAt(nowIndex++) - '0';
//                }
//                //如果出现倍率，则需要先放大，否则默认是1不处理
//                if (cnt > 0) {
//                    for (auto &item : tempMap) {
//                        item.second *= cnt;
//                    }
//                }
//                //将括号中各个原子的个数合并到当前hashMap中
//                for (auto &item : tempMap) {
//                    hashMap[item.first] += item.second;
//                }
//            }
//            else if (formula.charAt(nowIndex) == ')') {
//                //第二种情况：遇到了右括号，则返回下一个下标比如"Mg(OH)2"，应该返回'2'的下标
//                return nowIndex + 1;
//            }
//            else {
//                //第三种情况：遇到普通原子
//                //先读取原子的名称（所有原子的第一个字母为大写，剩余字母都是小写。），也就是说以大写字母分割原子名称
//                String name = String(1, formula.charAt(nowIndex++));
//                while (nowIndex < formulaSize && formula.charAt(nowIndex) >= 'a' && formula.charAt(nowIndex) <= 'z') {
//                    name += formula.charAt(nowIndex++);
//                }
//                //然后读取该原子的倍率
//                int cnt = 0;
//                while (nowIndex < formulaSize && formula.charAt(nowIndex) >= '0' && formula.charAt(nowIndex) <= '9') {
//                    cnt = cnt * 10 + formula.charAt(nowIndex++) - '0';
//                }
//                if (cnt > 0) {//倍数超过了1需要放大
//                    hashMap[name] += (cnt);
//                }
//                else {//否则默认是1
//                    hashMap[name] += 1;
//                }
//            }
//        }
//        return nowIndex;
//    }
//};