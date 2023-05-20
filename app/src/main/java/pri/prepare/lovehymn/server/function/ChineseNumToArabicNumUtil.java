package pri.prepare.lovehymn.server.function;

import pri.prepare.lovehymn.server.entity.Logger;

public class ChineseNumToArabicNumUtil {
    static final char[] cnArr = new char[]{'一', '二', '三', '四', '五', '六', '七', '八', '九'};
    static final char[] chArr = new char[]{'十', '百', '千', '万', '亿'};
    static final String allChineseNum = "零○一二三四五六七八九十百千万亿";

    /**
     * 将汉字中的数字转换为阿拉伯数字
     */
    public static int chineseNumToArabicNum(String chineseNum) {
        int result = 0;
        int temp = 1;//存放一个单位的数字如：十万
        int count = 0;//判断是否有chArr
        for (int i = 0; i < chineseNum.length(); i++) {
            boolean b = true;//判断是否是chArr
            char c = chineseNum.charAt(i);
            for (int j = 0; j < cnArr.length; j++) {//非单位，即数字
                if (c == cnArr[j]) {
                    if (0 != count) {//添加下一个单位之前，先把上一个单位值添加到结果中
                        result += temp;
                        count = 0;
                    }
                    // 下标+1，就是对应的值
                    temp = j + 1;
                    b = false;
                    break;
                }
            }
            if (b) {//单位{'十','百','千','万','亿'}
                for (int j = 0; j < chArr.length; j++) {
                    if (c == chArr[j]) {
                        switch (j) {
                            case 0:
                                temp *= 10;
                                break;
                            case 1:
                                temp *= 100;
                                break;
                            case 2:
                                temp *= 1000;
                                break;
                            case 3:
                                temp *= 10000;
                                break;
                            case 4:
                                temp *= 100000000;
                                break;
                            default:
                                break;
                        }
                        count++;
                    }
                }
            }
            if (i == chineseNum.length() - 1) {//遍历到最后一个字符
                result += temp;
            }
        }
        if (chineseNum.length() == 2 && result < 10) {
            int shi = -1;
            for (int i = 0; i < cnArr.length; i++)
                if (cnArr[i] == chineseNum.charAt(0)) {
                    shi = i;
                    break;
                }
            shi += 1;
            int ge = -1;
            for (int i = 0; i < cnArr.length; i++)
                if (cnArr[i] == chineseNum.charAt(1)) {
                    ge = i;
                    break;
                }
            ge += 1;
            result = 10 * shi + ge;
        }
        if (chineseNum.length() == 3 && result < 10) {
            int bai = -1;
            for (int i = 0; i < cnArr.length; i++)
                if (cnArr[i] == chineseNum.charAt(0)) {
                    bai = i;
                    break;
                }
            bai += 1;
            int shi = -1;
            for (int i = 0; i < cnArr.length; i++)
                if (cnArr[i] == chineseNum.charAt(1)) {
                    shi = i;
                    break;
                }
            shi += 1;
            int ge = -1;
            for (int i = 0; i < cnArr.length; i++)
                if (cnArr[i] == chineseNum.charAt(2)) {
                    ge = i;
                    break;
                }
            ge += 1;
            result = 100 * bai + 10 * shi + ge;
        }
        if (result == 0)
            Logger.info("c " + chineseNum + " a " + result);
        return result;
    }

    /**
     * 判断传入的字符串是否全是汉字数字
     */
    public static boolean isChineseNum(String chineseStr) {
        char[] ch = chineseStr.toCharArray();
        for (char c : ch) {
            if (!allChineseNum.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

}
