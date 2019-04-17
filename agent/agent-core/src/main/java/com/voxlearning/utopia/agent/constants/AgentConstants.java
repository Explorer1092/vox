package com.voxlearning.utopia.agent.constants;

/**
 * @author Jia HuanYin
 * @since 2015/7/30
 */
public class AgentConstants {

    public static final String TEAM_EMAIL = "song.wang@17zuoye.com;deliang.che@17zuoye.com;xianlong.zhang@17zuoye.com;ziqi.feng@17zuoye.com;mingyuan.xia@17zuoye.com;dongshuang.zhao@17zuoye.com;";

    public static final String API_BAD_REQUEST = "400";
    public static final String API_SERVER_ERROR = "500";
    public static final String API_NEED_LOGIN = "900";

    public static final Integer MODE_ONLINE = 1;
    public static final Integer MODE_OFFLINE = 2;

    public static final Integer ONLINE_INDICATOR_MONTH = 2;   // online 本月
    public static final Integer ONLINE_INDICATOR_DAY = 1;     // online 昨日
    public static final Integer ONLINE_INDICATOR_SUM = 4;     // online 累计
    public static final Integer ONLINE_INDICATOR_TERM = 3;    // online 学期
    //培训中心-广告位
    public static final String REQ_AD_POSITION = "ad_position";
    public static final String REQ_SYS = "sys";
    public static final String REQ_APP_NATIVE_VERSION = "ver";
    public static final String RES_RESULT_AD_IMG = "ad_img";
    public static final String RES_RESULT_AD_URL = "ad_url";
    public static final String RES_RESULT_AD_INFO = "ad_info";


    public static final Integer OFFLINE_INDICATOR_MONTH = 2;   // offline 本月
    public static final Integer OFFLINE_INDICATOR_DAY = 1;     // offline 昨日
    public static final Integer OFFLINE_INDICATOR_SUM = 4;     // offline 累计
    public static final Integer OFFLINE_INDICATOR_TERM = 3;    // offline 学期

    public static final Integer INDICATOR_TYPE_GROUP = 1;    // 指标类型   部门
    public static final Integer INDICATOR_TYPE_USER = 2;     // 指标类型   USER
    public static final Integer INDICATOR_TYPE_UNALLOCATED = 3;     // 指标类型  未分配

    public static final Integer XTEST_DAY = 1;     //  昨日
    public static final Integer XTEST_MONTH = 2;   //  本月
    public static final String XTEST_DATA_ALL = "all";   // 各个维度汇总排重后指标数据
//    public static final String XTEST_DATA_PROVINCE = "province";   // 省级
    public static final String XTEST_DATA_CITY = "city";     // 市级
    public static final String XTEST_DATA_COUNTY = "county";     // 区级
    public static final String XTEST_DATA_SCHOOL = "school";    //  校级

    public static final Integer TEST_TYPE_X_TEST = 0; //x测
    public static final Integer TEST_TYPE_EXAME = 2; //统考
    public static final Integer TEST_TYPE_ACTIVITY = 1;//活动


    public static final String AGENT_INNER_TOPIC = "utopia.agent.queue.inner";
}
