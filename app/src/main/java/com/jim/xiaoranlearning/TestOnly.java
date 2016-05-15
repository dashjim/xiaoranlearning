package com.jim.xiaoranlearning;

/**
 */
public class TestOnly {
    public static void main(String[] arg){
        String target = "I am here\r\n 123";
        System.out.println(target);
        String[] split = target.split("\r\n");
        String longest = split[0];
        for (String aSplit : split) {
            longest = longest.length() > aSplit.length() ? longest : aSplit;
            System.out.println("longest ->" + longest);
        }
    }
}
