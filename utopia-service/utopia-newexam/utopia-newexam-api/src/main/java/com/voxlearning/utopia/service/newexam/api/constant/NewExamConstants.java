package com.voxlearning.utopia.service.newexam.api.constant;

import com.voxlearning.alps.calendar.DateUtils;

import java.util.Date;

/**
 * @Description: 考试常量
 * @author: Mr_VanGogh
 * @date: 2019/3/26 下午2:58
 */
public class NewExamConstants {

    //单元测试 按照时间过滤
    public static final Date UNIT_TEST_DUE_DATE = DateUtils.stringToDate("2019-03-20 00:00:00");

    // 单元测
    public static final String UNIT_TEST_PAPER_IDS_TEST_URL = "http://10.7.7.41:9528/unit_test/v1/paper_pids";
    public static final String UNIT_TEST_PAPER_IDS_STAGING_URL = "http://10.7.7.41:9530/unit_test/v1/paper_pids";
    public static final String UNIT_TEST_PAPER_IDS_PRODUCT_URL = "http://yqc.17zuoye.net/unit_test/v1/paper_pids";

}
