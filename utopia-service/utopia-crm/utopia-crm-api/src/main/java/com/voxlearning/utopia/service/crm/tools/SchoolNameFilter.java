package com.voxlearning.utopia.service.crm.tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchoolNameFilter {

    private static Set<String> chineseSpecialCharSet = new HashSet<>();//汉字特殊字符
    private static Set<String> provinceSet = new HashSet<>();//省
    private static Set<String> cityCountySet = new HashSet<>();//城市,区县

    private static void initSet() {
        try {
            InputStream in = SchoolNameFilter.class.getResourceAsStream("/filterinfo/chinesechar.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                chineseSpecialCharSet.add(line);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("");
        }
    }

    private static void initCityInfoSet() {
        try {
            InputStream in = SchoolNameFilter.class.getResourceAsStream("/filterinfo/provincecitycounty.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] line_arry = line.split("\t");
                if (line_arry.length != 6) {
                    System.out.println("line not 6 fild is:" + line);
                    continue;
                }
                if (!line_arry[0].equals("") && !line_arry[0].equals(" ") && !line_arry[2].equals("")
                        && !line_arry[2].equals(" ") && !line_arry[4].equals("") && !line_arry[4].equals(" ")) {
                    provinceSet.add(line_arry[0]);

                    String ct = getRuledCityName(line_arry[2]);

                    String county = line_arry[4];
                    if (county.contains("区") && county.length() > 2) {
                        county = county.substring(0, county.indexOf("区")).trim();
                    } else if (county.contains("县") && county.length() > 2) {
                        county = county.substring(0, county.indexOf("县")).trim();
                    } else if (county.contains("市") && county.length() > 2) {
                        county = county.substring(0, county.indexOf("市")).trim();

                    }

                    if (county.contains("　")) {
                        county = county.substring(0, county.indexOf("　"));
                    }
                    if (county.length() > 1)
                        cityCountySet.add(ct + "," + county);
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("");
        }
    }

    //初始化信息
    static {
        initSet();
        initCityInfoSet();
    }

    private static String filterByBracket(String schoolName) {
        Set<String> bracketSet = new HashSet<>();
        bracketSet.add("()");
        bracketSet.add("[]");
        bracketSet.add("（）");
        bracketSet.add("【】");

        Pattern pattern = Pattern.compile("\\(.*\\)");
        for (String tempBrackets : bracketSet) {
            String headBracket = tempBrackets.substring(0, 1);
            String tailBracket = tempBrackets.substring(1);
            if (schoolName.contains(headBracket)) {
                pattern = Pattern.compile("\\" + headBracket + ".*\\" + tailBracket);
                if (schoolName.endsWith(tailBracket)) {
                    pattern = Pattern.compile("\\" + headBracket + "|\\" + tailBracket);
                }
            }
        }
        Matcher matcher = pattern.matcher(schoolName);
        return matcher.replaceAll("");
    }

    private static String getRuledCityName(String city) {
        String shortCityName;
        if (city.endsWith("市") || city.endsWith("盟")) {
            shortCityName = city.substring(0, city.length() - 1);
        } else if (city.endsWith("地区")) {
            shortCityName = city.substring(0, city.length() - 2);
        } else if (city.endsWith("自治区")) {
            shortCityName = city.substring(0, city.length() - 3);
        } else {
            shortCityName = city;
        }
        return shortCityName;
    }

    private static String filterArtherCharacter(String schoolName) {
        String returnValue = "";
        char[] answer_char = schoolName.toCharArray();
        for (char one : answer_char) {
            String one_string = "" + one;
            if (chineseSpecialCharSet.contains(one_string)) {
                continue;
            }
            if ((one >= '0' && one <= '9') || isChineseChar(one)) {
                returnValue = returnValue + one_string;
            }
        }
        return returnValue;
    }

    private static String replaceSpecifiedChineseNum(String schoolName) {
        return ChineseNumber.getReplaceNum(schoolName);
    }

    private static String formatSchoolNameTerm(String schoolName, String type) {
        if (schoolName.contains("体校")) {
            schoolName = schoolName.replace("体校", "体育学校");
        }
        if (schoolName.contains("体育运动")) {
            schoolName = schoolName.replace("体育运动", "体育学校");
        }
        if (schoolName.contains("外国语")) {
            schoolName = schoolName.replace("外国语", "外语");
        }
        if (schoolName.contains("双语")) {
            schoolName = schoolName.replace("双语", "");
        }
        if (schoolName.contains("中学校")) {
            schoolName = schoolName.replace("中学校", "中学");
        }
        if (schoolName.contains("联中")) {
            schoolName = schoolName.replace("联中", "中学");
        }
        if (schoolName.contains("综合高中")) {
            schoolName = schoolName.replace("综合高中", "高中");
        }
        if (schoolName.contains("实验高中")) {
            schoolName = schoolName.replace("实验高中", "高中");
        }
        if (schoolName.contains("附中")) {
            schoolName = schoolName.replace("附中", "高中");
        }
        if (schoolName.contains("完中")) {
            schoolName = schoolName.replace("完中", "中学");
        }
        if (schoolName.contains("实验中学")) {
            schoolName = schoolName.replace("实验中学", "高中");
        }
        if (schoolName.contains("附属中学")) {
            schoolName = schoolName.replace("附属中学", "高中");
        }

        if (schoolName.contains("中学")) {
            if (type.equals("高中"))
                schoolName = schoolName.replace("中学", "高中");
            if (type.equals("初中"))
                schoolName = schoolName.replace("中学", "初中");
        }
        if (schoolName.contains("学校")) {
            if (type.equals("高中"))
                schoolName = schoolName.replace("学校", "高中");
            if (type.equals("初中"))
                schoolName = schoolName.replace("学校", "初中");
        }

        if (schoolName.contains("联小")) {
            schoolName = schoolName.replace("联小", "小学");
        }
        if (schoolName.contains("小学校")) {
            schoolName = schoolName.replace("小学校", "小学");
        }

        if (schoolName.contains("附小")) {
            schoolName = schoolName.replace("附小", "小学");
        }
        if (schoolName.contains("完小")) {
            schoolName = schoolName.replace("完小", "小学");
        }
        if (schoolName.contains("中心小学")) {
            schoolName = schoolName.replace("中心小学", "小学");
        }

        if (schoolName.contains("初中部")) {
            schoolName = schoolName.replace("初中部", "初中");
        }
        if (schoolName.contains("高中部")) {
            schoolName = schoolName.replace("高中部", "高中");
        }
        if (schoolName.contains("小学部")) {
            schoolName = schoolName.replace("小学部 ", "小学");
        }

        if (schoolName.contains("高级中学")) {
            schoolName = schoolName.replace("高级中学", "高中");
        }
        if (schoolName.contains("初级中学")) {
            schoolName = schoolName.replace("初级中学", "初中");
        }

        // need delete name prefix
        if (schoolName.contains("第")) {
            schoolName = schoolName.replace("第", "");
        }
        if (schoolName.contains("乡")) {
            schoolName = schoolName.replace("乡", "");
        }
        if (schoolName.contains("镇")) {
            schoolName = schoolName.replace("镇", "");
        }
        if (schoolName.contains("街道")) {
            schoolName = schoolName.replace("街道", "");
        }
        if (schoolName.contains("村")) {
            schoolName = schoolName.replace("村", "");
        }
        if (schoolName.contains("私立")) {
            schoolName = schoolName.replace("私立", "");
        }
        if (schoolName.contains("民办")) {
            schoolName = schoolName.replace("民办", "");
        }
        if (schoolName.contains("学院")) {
            schoolName = schoolName.replace("学院", "");
        }
        if (schoolName.contains("开发区")) {
            schoolName = schoolName.replace("开发区", "");
        }
        if (schoolName.contains("经开区")) {
            schoolName = schoolName.replace("经开区", "");
        }
        if (schoolName.contains("分局")) {
            schoolName = schoolName.replace("分局", "");
        }
        if (schoolName.contains("分部")) {
            schoolName = schoolName.replace("分部", "");
        }
        if (schoolName.contains("经济技术")) {
            schoolName = schoolName.replace("经济技术", "");
        }

        Pattern p = Pattern.compile("[\\d]+中");
        boolean matched = p.matcher(schoolName).find();
        if (schoolName.endsWith("中") && matched) {
            if (type.equals("初中"))
                schoolName = schoolName.replace("中", "初中");
            if (type.equals("高中"))
                schoolName = schoolName.replace("中", "高中");
        }

        if (schoolName.endsWith("高")) {
            schoolName = schoolName.replace("高", "高中");
        }

        if (schoolName.endsWith("小")) {
            schoolName = schoolName.replace("小", "小学");
        }

        return schoolName;
    }

    private static String filterProvinceCity(String schoolName, String city) {
        String returnValue_tmp = schoolName;

        String provinceSuffix;
        for (String province : provinceSet) {
            provinceSuffix = province + "省";
            boolean hasSuffix = returnValue_tmp.contains(provinceSuffix);
            boolean hasShort = returnValue_tmp.contains(province);

            while (hasSuffix || hasShort) {
                String returnValue = "";
                String[] schoolNameArray;
                if (hasSuffix) {
                    schoolNameArray = returnValue_tmp.split(provinceSuffix);
                } else {
                    schoolNameArray = returnValue_tmp.split(province);
                }
                for (String tempSchoolName : schoolNameArray) {
                    returnValue = returnValue + tempSchoolName;
                }

                boolean onlyKey = onlyContainKeyWord(returnValue);
                if (!onlyKey)
                    returnValue_tmp = returnValue;
                else
                    break;
                hasSuffix = returnValue.contains(provinceSuffix);
                hasShort = returnValue.contains(province);
            }
        }

        String longCity;
        String shortCity;
        String key;
        if (city.endsWith("市")) {
            longCity = city;
            key = "市";
            shortCity = city.substring(0, city.length() - 1);
        } else if (city.endsWith("盟")) {
            longCity = city;
            key = "盟";
            shortCity = city.substring(0, city.length() - 1);
        } else if (city.endsWith("地区")) {
            longCity = city;
            key = "地区";
            shortCity = city.substring(0, city.length() - 2);
        } else if (city.endsWith("自治区")) {
            longCity = city;
            key = "自治区";
            shortCity = city.substring(0, city.length() - 3);
        } else {
            longCity = city + "市";
            key = "市";
            shortCity = city;
        }

        if (city.length() > 1) {
            while (returnValue_tmp.contains(longCity) || returnValue_tmp.contains(shortCity)) {
                String returnValue = "";
                if (returnValue_tmp.contains(longCity)) {
                    String[] schoolNameArray = returnValue_tmp.split(longCity);
                    for (String aSchoolNameArray : schoolNameArray) {
                        returnValue = returnValue + aSchoolNameArray;
                    }
                    boolean onlyKey = onlyContainKeyWord(returnValue);
                    if (!onlyKey)
                        returnValue_tmp = returnValue;
                    else {
                        returnValue_tmp = returnValue.replace(key, ""); //修改2 returnValue_tmp = returnValue_tmp.replace(key, "");

                        break;
                    }
                }

                returnValue = "";
                if (returnValue_tmp.contains(shortCity)) {
                    String[] schoolNameArray = returnValue_tmp.split(shortCity);
                    for (String aSchoolNameArray : schoolNameArray) {
                        returnValue = returnValue + aSchoolNameArray;
                    }
                    boolean onlyKey = onlyContainKeyWord(returnValue);
                    if (!onlyKey)
                        returnValue_tmp = returnValue;
                    else {
                        returnValue_tmp = returnValue; //修改2 之前没有这步
                        break;
                    }
                }
            }
        }
        return returnValue_tmp;
    }

    private static String filterCounty(String schoolName, String city) {
        String returnValue;
        String returnValue_tmp = schoolName;
        boolean hadProcess = false;
        String ct = getRuledCityName(city);
        String county;
        for (String one : cityCountySet) {
            if (one.contains(ct)) {
                county = one.split(",")[1];
            } else {
                continue;
            }

            String tempXian = county + "县";
            String tempQu = county + "区";
            String tempShi = county + "市";
            while (returnValue_tmp.contains(county)) {
                returnValue = "";
                boolean hasXian = returnValue_tmp.contains(tempXian);
                boolean hasQu = returnValue_tmp.contains(tempQu);
                boolean hasShi = returnValue_tmp.contains(tempShi);
                boolean onlyHasOne = false; //returnValue_tmp.contains(county);
                if (hasXian || hasQu || hasShi || onlyHasOne) {
                    String[] schoolNameArray;
                    if (hasXian) {
                        schoolNameArray = returnValue_tmp.split(tempXian);
                    } else if (hasQu) {
                        schoolNameArray = returnValue_tmp.split(tempQu);
                    } else if (hasShi) {
                        schoolNameArray = returnValue_tmp.split(tempShi);
                    } else {
                        schoolNameArray = returnValue_tmp.split(county);
                    }

                    for (String aSchoolNameArray : schoolNameArray) {
                        returnValue = returnValue + aSchoolNameArray;
                    }
                    boolean onlyKey = onlyContainKeyWord(returnValue);
                    if (!onlyKey)
                        returnValue_tmp = returnValue;
                    else {
                        hadProcess = true;
                        break;
                    }
                }
                hadProcess = true;
                break;
            }

            if (hadProcess)
                break;
        }
        return returnValue_tmp;
    }

    private static boolean isChineseChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    private static boolean onlyContainKeyWord(String schoolName) {
        if (schoolName.contains("中心小学")) {
            schoolName = schoolName.replace("中心小学", "");
        }
        if (schoolName.contains("实验小学")) {
            schoolName = schoolName.replace("实验小学", "");
        }
        if (schoolName.contains("体育学校")) {
            schoolName = schoolName.replace("体育学校", "");
        }
        if (schoolName.contains("联办中学")) {
            schoolName = schoolName.replace("联办中学", "");
        }
        if (schoolName.contains("附属中学")) {
            schoolName = schoolName.replace("附属中学", "");
        }
        if (schoolName.contains("完全中学")) {
            schoolName = schoolName.replace("完全中学", "");
        }
        if (schoolName.contains("外语高中")) {
            schoolName = schoolName.replace("外语高中", "");
        }
        if (schoolName.contains("联办小学")) {
            schoolName = schoolName.replace("联办小学", "");
        }
        if (schoolName.contains("附属小学")) {
            schoolName = schoolName.replace("附属小学", "");
        }
        if (schoolName.contains("完全小学")) {
            schoolName = schoolName.replace("完全小学", "");
        }
        if (schoolName.contains("小学")) {
            schoolName = schoolName.replace("小学", "");
        }
        if (schoolName.contains("联办")) {
            schoolName = schoolName.replace("联办", "");
        }
        if (schoolName.contains("外语")) {
            schoolName = schoolName.replace("外语", "");
        }
        if (schoolName.contains("中学")) {
            schoolName = schoolName.replace("中学", "");
        }
        if (schoolName.contains("分校")) {
            schoolName = schoolName.replace("分校", "");
        }
        if (schoolName.contains("高")) {
            schoolName = schoolName.replace("高", "");
        }
        if (schoolName.contains("小")) {
            schoolName = schoolName.replace("小", "");
        }
        if (schoolName.contains("第")) {
            schoolName = schoolName.replace("第", "");
        }
        if (schoolName.contains("高")) {
            schoolName = schoolName.replace("高", "");
        }
        if (schoolName.contains("中")) {
            schoolName = schoolName.replace("中", "");
        }
        if (schoolName.contains("小")) {
            schoolName = schoolName.replace("小", "");
        }
        if (schoolName.contains("省")) {
            schoolName = schoolName.replace("省", "");
        }
        if (schoolName.contains("市")) {
            schoolName = schoolName.replace("市", "");
        }
        if (schoolName.contains("区")) {
            schoolName = schoolName.replace("区", "");
        }
        if (schoolName.contains("县")) {
            schoolName = schoolName.replace("县", "");
        }
        if (schoolName.length() < 1)
            return true;
        Pattern p = Pattern.compile("^[\\d]");
        return p.matcher(schoolName).find();
    }

    protected static String getTempResult(String schoolName, String city, String type) {
        String returnValue;
        //修改1 替代filtKuohao
        returnValue = filterByBracket(schoolName);//去掉括号内的字符(包括括号),以括号结束的字符保留字符内的描述
        returnValue = filterArtherCharacter(returnValue); //去掉特殊字符
        returnValue = replaceSpecifiedChineseNum(returnValue); //汉字转换为数值
        returnValue = formatSchoolNameTerm(returnValue, type); //替换掉特定术语
        returnValue = filterProvinceCity(returnValue, city);
        //returnValue = filterCounty(returnValue, city); // 区名称的替换在现在的需求中没有必要而且会引起bug

        return returnValue;
    }

}
