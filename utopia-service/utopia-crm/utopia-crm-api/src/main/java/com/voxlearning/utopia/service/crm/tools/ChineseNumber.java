package com.voxlearning.utopia.service.crm.tools;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模块： 描述：将汉字数字转换为数字
 */
public class ChineseNumber {
    private ChineseNumber() {
    }

    private static Pattern numRegex = Pattern.compile("[零一二三四五六七八九十百千万][小|中|高|实验|师范|附|外|学|联|分|学|模范|农场]");
    private static Pattern CHN_NUM_PATTERN = Pattern.compile("[一二三四五六七八九][^十百千]?[零一二三四五六七八九]");
    private static Pattern LONG_NUM_PATTERN = Pattern.compile("[(一|二|三|四|五|六|七|八|九)(千|百|十)]+[零一二三四五六七八九]?");


    private static Map<Character, Integer> CHN_UNITS = new HashMap<>();
    private static Map<Integer, Integer> CHN_MULTI_UNITS = new HashMap<>();

    private static Map<Character, Integer> CHN_NUM = new HashMap<>();

    static {
        CHN_UNITS.put('十', 10);
        CHN_UNITS.put('百', 100);
        CHN_UNITS.put('千', 1000);
        CHN_UNITS.put('万', 10000);
        CHN_UNITS.put('亿', 10000000);
        CHN_MULTI_UNITS.put(1, 1);
        CHN_MULTI_UNITS.put(2, 10);
        CHN_MULTI_UNITS.put(3, 100);
        CHN_MULTI_UNITS.put(4, 1000);
        CHN_NUM.put('一', 1);
        CHN_NUM.put('二', 2);
        CHN_NUM.put('三', 3);
        CHN_NUM.put('四', 4);
        CHN_NUM.put('五', 5);
        CHN_NUM.put('六', 6);
        CHN_NUM.put('七', 7);
        CHN_NUM.put('八', 8);
        CHN_NUM.put('九', 9);
        CHN_NUM.put('十', 10);
        CHN_NUM.put('零', 0);
    }

    public static String getReplaceNum(String schoolName) {
        try {
            Matcher numSchool = numRegex.matcher(schoolName);
            if (numSchool.find()) {
                return getNumberString(schoolName);
            } else {
                return schoolName;
            }
        } catch (Exception e) {
            return schoolName;
        }
    }

    private static String getNumberString(String chnNum) {
        Matcher shortMatcher = CHN_NUM_PATTERN.matcher(chnNum);
        Matcher longMatcher = LONG_NUM_PATTERN.matcher(chnNum);
        boolean shortMatched = shortMatcher.find();
        boolean longMatched = longMatcher.find();

        if (longMatched && !shortMatched) {
            Integer replaceNum = 0;
            longMatcher.reset(chnNum);

            int startIndex;
            int lastIndex = 0;
            String subStr;

            while (longMatcher.find()) {
                String subNumber = longMatcher.group();
                startIndex = chnNum.indexOf(subNumber, lastIndex);
                if (startIndex == -1)
                    continue;
                if (startIndex + 1 <= chnNum.length()) {
                    int idx = chnNum.length() < startIndex + subNumber.length() + 1 ? chnNum.length()
                            : startIndex + subNumber.length() + 1;

                    subStr = chnNum.substring(lastIndex, idx);
                    Matcher isRealNum = numRegex.matcher(subStr);
                    lastIndex = startIndex + subNumber.length();
                    if (!isRealNum.find())
                        continue;
                }

                for (int i = 0; i < subNumber.length(); ) {
                    Integer num;
                    Integer multi;
                    char key;
                    char unit;

                    key = subNumber.charAt(i);
                    if (key == '百' || key == '千' || key == '万') {// 必须是 五百 这样形式的出现，不允许为百二十 这样的出现
                        i++;
                        continue;
                    }
                    if (i + 1 < subNumber.length()) {
                        unit = subNumber.charAt(i + 1);
                        if (CHN_NUM.containsKey(key) && CHN_UNITS.containsKey(unit)) {
                            num = CHN_NUM.get(key);
                            multi = CHN_UNITS.get(unit);
                            replaceNum += num * multi;
                        } else if (CHN_NUM.containsKey(key) && CHN_NUM.containsKey(unit)) {
                            num = CHN_NUM.get(key);
                            multi = CHN_NUM.get(unit);
                            replaceNum += num + multi;
                        }
                    } else {
                        num = CHN_NUM.get(key);
                        replaceNum += num;
                    }

                    if (key == '零') {
                        i += 1;
                        continue;
                    }
                    i += 2;
                }
                chnNum = chnNum.replace(subNumber, replaceNum.toString());
                replaceNum = 0;
            }
            return chnNum;
        } else {
            shortMatcher.reset(chnNum);
            Integer num = 0;
            while (shortMatcher.find()) {
                String subNumber = shortMatcher.group();
                if (subNumber.length() == 1) {
                    num += CHN_NUM.get(subNumber.charAt(0));
                } else if (subNumber.length() > 1) {
                    for (int i = 0; i < subNumber.length(); i++) {
                        if (CHN_NUM.get(subNumber.charAt(i)) == null) {
                            continue;
                        }
                        num += BigInteger.valueOf(CHN_NUM.get(subNumber.charAt(i)))
                                .multiply(BigInteger.valueOf(CHN_MULTI_UNITS.get(subNumber.length() - i))).intValue();
                    }
                }
                chnNum = chnNum.replace(subNumber, num.toString());
                num = 0;
            }
            return chnNum;
        }
    }

    public static void main(String[] args) {
        //System.out.println(getReplaceNum("齐齐哈尔市三六测试二四五中学校"));
        System.out.println(getReplaceNum("齐齐哈尔市三千六中测试一百二十中测试四十五小测试学校"));

    }

}
