package com.voxlearning.utopia.service.newhomework.api.constant;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.*;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType.Normal;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType.TermReview;
import static com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType.*;

/**
 * @author guoqiang.li
 * @since 2016/10/28
 */
public class NewHomeworkConstants {
    public static final long DEFAULT_DURATION_MILLISECONDS = 10000L; // 作业单题完成默认用时，10秒
    public static final long MAX_DURATION_MILLISECONDS = 150000L; // 作业单题完成最大用时，150秒
    public static final long LEVEL_READINGS_MAX_DURATION_MILLISECONDS = 1080000L; // 单个绘本完成最大用时,1080s
    public static final long DUBBING_MAX_DURATION_MILLISECONDS = 720000L; // 单个配音完成最大用时，720s
    public static final long OCR_MENTAL_MAX_DURATION_MILLISECONDS = 600000L; // 纸质口算最大完成用时，600s
    public static final long MAX_EFFECTIVE_MILLISECONDS = 31536000000L; // 作业最大有效时长，1年
    public static final int ASSIGN_VACATION_HOMEWORK_INTEGRAL_REWARD = 1000; //布置假期作业老师奖励学豆
    public static final int FINISH_VACATION_HOMEWORK_INTEGRAL_REWARD = 5; //完成假期作业学生奖励学豆
    public static final int FINISH_VACATION_HOMEWORK_ENERGY_REWARD = 2; //完成假期作业学生奖励能量
    public static final int FINISH_VACATION_HOMEWORK_CREDIT_REWARD = 2; //完成假期作业学生奖励学分
    public static final int ASSIGN_HOMEWORK_MAX_DURATION_MINUTES = 25; // 布置作业最大时长(超过会弹窗提示，但依然能布置)

    private static final Date VH_START_DATE_EARLIEST = DateUtils.stringToDate("2019-01-01 00:00:00"); //假期作业最早开始时间  1.1
    private static final Date VH_START_DATE_EARLIEST_STAGING = DateUtils.stringToDate("2018-11-20 00:00:00"); // staging最早开始时间
    private static final Date VH_START_DATE_EARLIEST_TEST = DateUtils.stringToDate("2018-11-20 00:00:00"); // test最早开始时间
    public static final Date VH_START_DATE_DEFAULT = DateUtils.stringToDate("2019-01-07 08:00:00"); // 默认开始时间：1.7
    public static final Date VH_START_DATE_LATEST = DateUtils.stringToDate("2019-02-10 23:59:59"); // 最晚开始时间
    public static final Date VH_END_DATE_EARLIEST = DateUtils.stringToDate("2019-02-11 23:59:59"); // 最早截止时间
    public static final Date VH_END_DATE_DEFAULT = DateUtils.stringToDate("2019-03-01 23:59:59"); // 默认截止时间
    public static final Date VH_END_DATE_LATEST = DateUtils.stringToDate("2019-03-17 23:59:59"); // 最晚截止时间
    public static final Date VH_OFFLINE_DATE = DateUtils.stringToDate("2019-03-31 23:59:59");  // 下线时间
    public static final Date VH_PARENT_HOMEWORK_DYNAMIC_SHOW_END_TIME = DateUtils.stringToDate("2017-02-12 23:59:59");
    public static final Date VH_OPEN_FIFTEEN_LIMIT_DATE = DateUtils.stringToDate("2019-02-03 00:00:00"); // 开放前15份假期作业时间

    public static final Date STUDENT_ALLOW_SEARCH_HOMEWORK_START_TIME = DateUtils.stringToDate("2018-06-01 00:00:00"); // 学生作业历史里允许查询的最早的作业
    public static final Date ALLOW_SHOW_DUBBING_VIDEO_START_TIME = DateUtils.stringToDate("2018-08-10 00:00:00"); // 这个时间之前合成的配音都清掉节省cdn存储空间开支
    public static final long SPECIAL_PRACTICE_TYPE_ENGLISH_READING = 67L;

    private static final Date WQ_START_DATE_TEST = DateUtils.stringToDate("2017-04-06 00:00:00"); // test，错题订正报告允许显示的起始时间
    private static final Date WQ_START_DATE_STAGING = DateUtils.stringToDate("2017-04-10 00:00:00"); // staging，错题订正报告允许显示的起始时间
    private static final Date WQ_START_DATE = DateUtils.stringToDate("2017-04-13 00:00:00"); // 线上，错题订正报告允许显示的起始时间

    public static final Date REMIND_CORRECTION_START_DATE = DateUtils.stringToDate("2018-10-17 00:00:00"); // 推荐巩固按钮显示的起始时间

    public static final Date DRAGON_BOAT_FESTIVAL_DATE_EARLIEST = DateUtils.stringToDate("2017-05-27 23:59:59"); //端午小长假作业任务最早结束时间
    public static final Date DRAGON_BOAT_FESTIVAL_DATE_LATEST = DateUtils.stringToDate("2017-05-31 00:00:00"); //端午小长假作业任务最晚结束时间

    public static final Date ALLOW_UPDATE_HOMEWORK_START_TIME = DateUtils.stringToDate("2018-11-01 00:00:00"); // 允许更新作业数据（调整，删除，评语，奖励学豆，学生做题）等的最早时间

    public static final Date BASIC_REVIEW_END_DATE = DateUtils.stringToDate("2019-02-01 23:59:59"); // 期末基础复习结束时间
    public static final Date NATIONAL_DAY_HOMEWORK_START_DATE = DateUtils.stringToDate("2018-10-01 00:00:00"); // 国庆假期作业开始时间
    public static final Date NATIONAL_DAY_HOMEWORK_ASSIGN_END_DATE = DateUtils.stringToDate("2018-10-06 00:00:00"); // 国庆假期作业布置结束时间
    public static final Date NATIONAL_DAY_HOMEWORK_PUSH_STAT_DATE = DateUtils.stringToDate("2018-10-01 08:00:00"); // 国庆假期作业学生端push最早时间
    public static final Date NATIONAL_DAY_HOMEWORK_END_DATE = DateUtils.stringToDate("2018-10-10 23:59:59"); // 国庆假期作业结束时间

    // 期末复习不支持的教材系列
    public static final List<String> TERM_REVIEW_NOT_SUPPORTED_BOOK_SERIES = Arrays.asList("BKC_10300188961194", "BKC_10300012213694", "BKC_10300006938562", "BKC_10300001778872", "BKC_10300094464251",
            "BKC_10300140362036", "BKC_10300200575311", "BKC_10300084029272", "BKC_10300080277080", "BKC_10300012219448", "BKC_10300081546864", "BKC_10300095067123", "BKC_10300200753248");

    // 从HBase读取sub相关数据的最晚时间, 配合下面的开关使用
    // 在这个时间之前，开关为true，从HBase读，开关为false，从mongo读
    // 在这个时间之后，从mongo读
    public static final Date HBASE_SUB_HOMEWORK_END_TIME = DateUtils.stringToDate("2018-11-01 00:00:00");
    public static final boolean FROM_HBASE_SUB_HOMEWORK = true;

    public static final Date SHARD_MONGO_START_TIME = DateUtils.stringToDate("2018-02-01 00:00:00");   // 开始使用shard mongo的时间

    // 作业自学巩固任务的类型定义
    public static final List<NewHomeworkType> GenerateHomeworkTypes = Arrays.asList(Normal, TermReview); // 允许生成自学作业的type
    public static final List<HomeworkTag> GenerateHomeworkTags = Arrays.asList(HomeworkTag.Normal, HomeworkTag.Last_TermReview, HomeworkTag.Next_TermReview); // 允许生成自学作业的tag
    public static final List<ObjectiveConfigType> GenerateSelfStudyHomeworkConfigTypes = Arrays.asList(INTELLIGENCE_EXAM, EXAM, UNIT_QUIZ, KEY_POINTS, BASIC_KNOWLEDGE, CHINESE_READING, INTERESTING_PICTURE, FALLIBILITY_QUESTION, INTELLIGENT_TEACHING, ORAL_INTELLIGENT_TEACHING, CALC_INTELLIGENT_TEACHING, OCR_MENTAL_ARITHMETIC, MENTAL_ARITHMETIC, WORD_TEACH_AND_PRACTICE, ONLINE_DICTATION); // 允许生成自学作业的作业形式
    public static final List<Subject> NeedSelfStudyHomeworkSubjects = Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE); // 允许生成自学作业的学科
    public static final List<ObjectiveConfigType> COURSE_APP_CONFIGTYPE = Arrays.asList(DIAGNOSTIC_INTERVENTIONS, ORAL_INTERVENTIONS); // 课程应用类作业形式(目前只用于订正作业)
    public static final List<ObjectiveConfigType> INTELLIGENT_TEACHING_CONFIGTYPE = Arrays.asList(ObjectiveConfigType.INTELLIGENT_TEACHING, ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING);//讲练测作业形式
    public static final List<ObjectiveConfigType> IMMEDIATE_INTERVENTION_CONFIGTYPE = Arrays.asList(INTELLIGENCE_EXAM, UNIT_QUIZ, INTELLIGENT_TEACHING, WORD_TEACH_AND_PRACTICE);//即时干预作业形式
    public static final List<ObjectiveConfigType> JZT_REPORT_PRESCRIPTION_CONFIGTYPE = Arrays.asList(INTELLIGENCE_EXAM, UNIT_QUIZ, INTELLIGENT_TEACHING, FALLIBILITY_QUESTION, MENTAL_ARITHMETIC, KEY_POINTS, INTERESTING_PICTURE);//家长通报告处方作业形式

    public static final List<ObjectiveConfigType> GenerateQuestionPartHomeworkConfigTypesForReport = Arrays.asList(BASIC_APP, LS_KNOWLEDGE_REVIEW, NATURAL_SPELLING, NEW_READ_RECITE, READING, DUBBING, LEVEL_READINGS); // 作业报告按题查看不需要明细的类型
    public static final List<ObjectiveConfigType> VoiceRecommendSupportedTypes = Arrays.asList(BASIC_APP, NATURAL_SPELLING, READ_RECITE_WITH_SCORE); // 优秀录音推荐支持的作业形式(这里不能随便加，目前代码只支持基础类型的形式)


    // 可以通过usertoken验证的作业类型
    public static final List<NewHomeworkType> AllowUserTokenTypes = Arrays.asList(NewHomeworkType.YiQiXue);

    // 新的六种联系类型
    public static final List<Integer> SpecialPracticeTypeList = Arrays.asList(217, 218, 219, 220, 221, 222);

    // UsTalk切换时间点，一切解千愁
    public static final Date USTALK_MOVE_DATE = DateUtils.stringToDate("2017-08-31 21:01:00");

    //老作业查询限制天数
    public static final int LIMIT_SELECT_OLD_HOMEWORK = 30;

    public static final int READ_RECITE_STANDARD = 60; //朗读背诵达标比例
    public static final int WORD_RECOGNITION_AND_READING_STANDARD = 80; //生字认读达标比例
    public static long LIMIT_HOMEWORK_TIME = 40 * 60;
    public static long COURSE_DEFAULT_DURATION = 2 * 60;//课程默认时长2分钟


    public static final List<ObjectiveConfigType> NOT_SHOW_SCORE_TYPE = Arrays.asList(ObjectiveConfigType.READ_RECITE_WITH_SCORE, ObjectiveConfigType.NEW_READ_RECITE, ObjectiveConfigType.READ_RECITE, ObjectiveConfigType.DUBBING,ObjectiveConfigType.WORD_RECOGNITION_AND_READING,ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);//不显示分数类型

    // 一起学布置作业支持的作业形式白名单，避免布置出去了不能做

    public static final List<ObjectiveConfigType> LIVE_CAST_HOMEWORK_SUPPORTED_TYPES = Arrays.asList(BASIC_APP, EXAM, INTELLIGENCE_EXAM, UNIT_QUIZ, FALLIBILITY_QUESTION, INTERESTING_PICTURE, BASIC_KNOWLEDGE, CHINESE_READING, PHOTO_OBJECTIVE, VOICE_OBJECTIVE, DUBBING, LEVEL_READINGS);
    // 老师APP布置作业使用原生打开的作业形式
    public static final List<ObjectiveConfigType> ASSIGN_HOMEWORK_USE_NATIVE_TYPES = Arrays.asList(DUBBING, LEVEL_READINGS, DUBBING_WITH_SCORE, ORAL_COMMUNICATION);

    //即时干预
    public static final String HINT_ID = "hintId";
    public static final String INTERVENTION_ANSWER = "interventionAnswer";
    public static final String HOMEWORK_HINT_URL_PROD = "http://megrez-api.17zuoye.net/homework/hint";
    public static final String HOMEWORK_HINT_URL_TEST = "http://10.7.12.92:5000/homework/hint";
    public static final String JZT_REPORT_ERRORREASON_FLOW_GUIDE_URL = "http://megrez-api.17zuoye.net/prescription";//家长通-报告错因导流试验url
    public static final String JZT_REPORT_ERRORREASON_FLOW_GUIDE_URL_TEST = "http://10.7.12.92:5000/prescription";//家长通-报告错因导流试验url(TEST)
    public static final String OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL = "https://smartlearning.17zuoye.com/knowledge/o2o_point";//口算拍照诊断URL（张弦提供）
    public static final String OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL_STAGING = "https://smartlearning.staging.17zuoye.net/knowledge/o2o_point";//口算拍照诊断URL（张弦提供预发布环境）
    public static final String OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL_TEST = "https://smartlearning.test.17zuoye.net/knowledge/o2o_point";//口算拍照诊断URL（张弦提供测试环境）

    // 老师首页推荐作业内容特定子目标id
    public static final String TEACHER_HOME_INDEX_RECOMMEND_OBJECTIVE_ID = "TON_00000000000";

    public static final String HOMEWORK_CORRECT_STATUS = "hCorrectStatus";//作业订正状态扩展字段名称
    public static final String THE_DEFAULT_ERROR_CAUSE = "对此类型题目陌生，不会做";//题目默认错因描述
    public static final String DETAIL_COURSE_NAME = "辅导课程";//默认课程名称

    public static final String OCR_MENTAL_ARITHMETIC_DEFAULT_IMG = "https://cdn-cnc.17zuoye.cn/resources/app/17student/res/homework/ocr_mental/blank.png";//纸质口算默认替换图片
    public static final String INDEPENDENT_OCR_DEFAULT_IMG = "https://cdn-cnc.17zuoye.cn/resources/app/17student/res/homework/independent_ocr/default_image.png";//独立拍照识别默认替换图片
    public static final String OCR_DICTATION_DEFAULT_IMG = "https://cdn-cnc.17zuoye.cn/resources/app/17student/res/homework/ocr_dictation/blank.png";//纸质听写默认替换图片

    public static final String WORD_TEACH_IMAGE_TEXT_RHYME_DEFAULT_IMG = "https://cdn-cnc.17zuoye.cn/resources/app/17student/res/homework/word_teach_and_practice/ImageText.png";//字词讲练图文入韵课程默认图片
    public static final String WORD_TEACH_CHARACTER_CULTURE_DEFAULT_IMG = "https://cdn-cnc.17zuoye.cn/resources/app/17student/res/homework/word_teach_and_practice/CharacterCulture.png";//字词讲练汉字文化课程默认图片

    public static final String STUDENT_OUTSIDE_READING_BOOKSHELF_URL = "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/book-shop/index.vhtml";  // 我的书架地址
    public static final String STUDENT_ANCIENT_POETRY_ACTIVITY_URL = "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/poem-activity/checkpoint.vhtml";  // 古诗活动学生端活动页面地址

    public static final String OCR_MENTAL_IMAGE_UPLOAD_URL_TEST = "http://10.7.12.129:8087/upload";
    public static final String OCR_MENTAL_IMAGE_UPLOAD_URL_PRODUCT = "http://arith_db.17zuoye.com/upload";

    public static final String OCR_MENTAL_IMAGE_COMPUTE_URL_TEST = "http://10.7.12.129:8087/compute";
    public static final String OCR_MENTAL_IMAGE_COMPUTE_URL_PRODUCT = "http://arith_db.17zuoye.com/compute";

    public static final String OUT_SIDE_READING_DEFAULT_BOOK_ID = "BK_10100000000000"; // 小语阅读OTO用来存使用次数的bookId

    public static final String DIAGNOSIS_HABIT_TEST_URL = "http://10.7.12.92:5000/";
    public static final String DIAGNOSIS_HABIT_STAGING_URL = "http://10.7.12.97:5000/";
    public static final String DIAGNOSIS_HABIT_PRODUCT_URL = "http://megrez-api.17zuoye.net/";

    public static final Date MENTAL_ARITHMETIC_ROUND_DOWN_SCORE_START_DATE = DateUtils.stringToDate("2019-01-05 00:00:00");
    public static final Date EXAM_ROUND_DOWN_SCORE_START_DATE = DateUtils.stringToDate("2019-01-08 00:00:00");
    public static final Date LEVEL_READINGS_ROUND_DOWN_SCORE_START_DATE = DateUtils.stringToDate("2019-01-14 12:00:00");

    public static final String ROOT_MENTAL_KP_ID = "KP_10200073219800"; // 口算知识点根节点id

    public static final String OCR_FORMULA_SYMPTOM_TEST_URL = "http://10.7.12.92:5000/ocr_formula_symptom";
    public static final String OCR_FORMULA_SYMPTOM_STAGING_RUL = "http://10.7.14.54:5000/ocr_formula_symptom";
    public static final String OCR_FORMULA_SYMPTOM_PRODUCT_RUL = "http://megrez-api.17zuoye.net/ocr_formula_symptom";

    // 假期作业过滤相关教材作业
    public static final List<String> VACATION_HOMEWORK_FILTER_BOOK = Arrays.asList("BK_10300000265057", "BK_10300000266810", "BK_10300000263225", "BK_10300000262593");
    public static final List<Integer> VACATION_HOMEWORK_FILTER_CITY = Arrays.asList(230100);        //哈尔滨地区(230100)

    public static final String OCR_MENTAL_ARITHMETIC_SEPARATOR = "$$##$$";

    // 纸质作业默认作业单ID(作业列表显示"查看作业单"使用)
    public static final String OCR_HOMEWORK_DETAIL_DEFAULT_ID = "12345";

    // 诊断接口
    public static final String SELF_STUDY_ZHENDUAN_TEST_URL = "http://10.7.13.137:5000/api/zhenduan/";
    public static final String SELF_STUDY_ZHENDUAN_STAGING_URL = "http://10.7.14.54:5000/api/zhenduan/";
    public static final String SELF_STUDY_ZHENDUAN_PRODUCT_URL = "http://megrez-api.17zuoye.net/api/zhenduan/";

    // 讲练测推题接口
    public static final String INTELLIGENT_DIAGNOSIS_QUESTION_PAKS_TEST_URL = "http://10.7.13.121/api/course_dispatch/intelligent_diagnosis_question_paks";
    public static final String INTELLIGENT_DIAGNOSIS_QUESTION_PAKS_PRODUCT_URL = "http://noctua.17zuoye.net/api/course_dispatch/intelligent_diagnosis_question_paks";

    /**
     * 评语模板，先暂时放在这吧
     * xuesong.zhang
     */
    public static List<String> commentTemplate(Subject subject) {
        String[] englishComments = {
                "完成得不错！",
                "恭喜你，你已经取得了很大的进步！",
                "有些小错误，下次要多加注意。",
                "如果你更加努力的话，我相信你会做得更好！",
                "如果能把所有练习都按时完成，你会进步得很快！",
                "Wonderful!",
                "Excellent!",
                "Nice work!",
                "I think you can do better if you try harder.",
                "I’m glad to see you are making progress."};
        String[] mathComments = {
                "做得太棒了！",
                "你的练习质量比以前有了很大的进步！",
                "你是一个很有数学才能的学生！",
                "你的计算能力有了很大提高！",
                "对于计算题，也要注意留心观察与思考！",
                "多想一想前后知识的联系，你就会变得更聪明！",
                "你的目标，应该是在数学方面成为同学们的榜样！",
                "有的题目如果你能再认真读下已知条件，就一定能做对！"};
        String[] chineseComments = {
                "做得太棒了！",
                "恭喜你，你已经取得了很大的进步！",
                "有些小错误，下次要多加注意。",
                "如果你更加努力的话，我相信你会做得更好！",
                "如果能把所有练习都按时完成，你会进步得很快！",
                "你的练习质量比以前有了很大的进步！"};

        List<String> result = new ArrayList<>();
        switch (subject) {
            case ENGLISH:
                result = Arrays.asList(englishComments);
                break;
            case MATH:
                result = Arrays.asList(mathComments);
                break;
            case CHINESE:
                result = Arrays.asList(chineseComments);
                break;
        }
        return result;
    }

    /**
     * 错题订正报告数据的显示时间
     */
    public static boolean showWrongQuestionInfo(Date homeworkCreateAt, String mode) {
        try {
            long createTime = homeworkCreateAt.getTime();
            if (StringUtils.equalsIgnoreCase(Mode.TEST.name(), mode)) {
                long allowTime = WQ_START_DATE_TEST.getTime();
                return createTime > allowTime;
            } else if (StringUtils.equalsIgnoreCase(Mode.STAGING.name(), mode)) {
                long allowTime = WQ_START_DATE_STAGING.getTime();
                return createTime > allowTime;
            } else {
                long allowTime = WQ_START_DATE.getTime();
                return createTime > allowTime;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 假期作业最早开始时间
     */
    public static Date earliestVacationHomeworkStartDate(Mode mode) {
        if (Mode.TEST == mode) {
            return VH_START_DATE_EARLIEST_TEST;
        } else if (Mode.STAGING == mode) {
            return VH_START_DATE_EARLIEST_STAGING;
        }
        return VH_START_DATE_EARLIEST;
    }
}
