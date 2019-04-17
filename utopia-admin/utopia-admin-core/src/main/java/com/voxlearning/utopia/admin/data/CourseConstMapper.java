package com.voxlearning.utopia.admin.data;

import java.util.Arrays;
import java.util.List;

/**
 * @author xuerui.zhang
 * @since 2018/9/12 下午8:06
 */
public class CourseConstMapper {

    public final static String OSS_HOST = "oss_pmc_host";

    public final static List<Integer> ENV_LEVEL = Arrays.asList(10, 20, 30, 40, 50);
    public final static List<Integer> NOTICE_TYPE = Arrays.asList(1, 2);
    public final static List<Integer> CLAZZ_TYPE = Arrays.asList(1, 2);
    public final static List<Integer> SKIP_TYPE = Arrays.asList(1, 2, 3);
    public final static List<Integer> KID_GRADE = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
    public final static List<Integer> JOIN_WAY = Arrays.asList(0, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    public final static List<Integer> CODE_TYPE = Arrays.asList(1, 2);
    public final static List<Integer> EBOOK_TYPE = Arrays.asList(0, 1, 2);
    public final static List<Integer> SPU_TYPE = Arrays.asList(0, 1);

    public final static List<String> REVIEW_TYPE = Arrays.asList("true", "false");

    public final static String CHAPTER_REDIRECT = "redirect: /opmanager/studytogether/chapter/chindex.vpage";
    public final static String CLAZZ_REDIRECT = "redirect: /opmanager/studytogether/clazzfestival/index.vpage";
    public final static String WEEKLY_REDIRECT = "redirect: /opmanager/studytogether/weeklyreward/wrindex.vpage";
    public final static String NOTICE_REDIRECT = "redirect: /opmanager/studytogether/notice/index.vpage";
    public final static String SKIP_REDIRECT = "redirect: /opmanager/studytogether/skip/index.vpage";
    public final static String SHARE_REDIRECT = "redirect: /opmanager/studytogether/share/index.vpage";
    public final static String SKU_REDIRECT = "redirect: /opmanager/studytogether/sku/index.vpage";
    public final static String CSCR_REDIRECT = "redirect: /opmanager/studytogether/coinreward/index.vpage";
    public final static String SUBJECT_REDIRECT = "redirect: /opmanager/studytogether/subject/index.vpage";
    public final static String SPU_REDIRECT = "redirect: /opmanager/studytogether/spu/index.vpage";
    public final static String SERIES_REDIRECT = "redirect: /opmanager/studytogether/series/index.vpage";

}
