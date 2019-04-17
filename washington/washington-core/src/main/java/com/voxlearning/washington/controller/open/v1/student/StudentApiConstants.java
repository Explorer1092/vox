package com.voxlearning.washington.controller.open.v1.student;

/**
 * 详情参考wiki http://wiki.17zuoye.net/pages/viewpage.action?pageId=11501847
 * Created by Shuai Huan on 2015/05/08.
 */

public class StudentApiConstants {

    // REQUEST
    public static final String REQ_HOMEWORK_TYPE = "homework_type";
    public static final String REQ_HOMEWORK_ID = "homework_id";
    public static final String REQ_QUIZ_ID = "quiz_id";
    public static final String REQ_NEWEXAM_ID = "newexam_id";
    public static final String REQ_VACATION_HOMEWORK_PACKAGE_ID = "package_id";
    public static final String REQ_WORKBOOK_ID = "workbook_id";
    public static final String REQ_WORKBOOK_CATALOG_ID = "workbook_catalog_id";
    public static final String REQ_WORKBOOK_CONTENT_ID = "workbook_content_id";
    public static final String REQ_HOMEWORK_FINISH_TIME = "homework_finish_time";
    public static final String REQ_HOMEWORK_NORMAL_FINISH_TIME = "homework_normal_finish_time";
    public static final String REQ_HOMEWORK_FINISHED = "homework_finished";
    public static final String REQ_HOMEWORK_STANDARDS = "homework_standards";
    public static final String REQ_INTEGRAL_REWARD_HOMEWORK_ID = "hid";
    public static final String REQ_INTEGRAL_REWARD_HOMEWORK_TYPE = "htype";

    // RESPONSE
    public static final String RES_HOMEWORK_ID = "hw_id";
    public static final String RES_HOMEWORK_TYPE = "hw_type";                             // 作业/测验ID
    public static final String RES_HOMEWORK_END_DATE = "hw_end_date";                     // 作业截止日期
    public static final String RES_HOMEWORK_END_DATE_STR = "hw_end_date_str";             // 作业截止日期字符串
    public static final String RES_HOMEWORK_START_COMMENT = "hw_start_comment";           // 开始作业内容
    public static final String RES_HOMEWORK_FINISH_COUNT = "hw_finish_count";             // 学生完成的数量
    public static final String RES_HOMEWORK_COUNT = "hw_count";                           // 作业总题量
    public static final String RES_HOMEWORK_CARD_TYPE = "hw_card_type";                   // 作业类型：ENGLISH，MATH，QUIZ_ENGLISH，QUIZ_MATH
    public static final String RES_HOMEWORK_CARD_DESC = "hw_card_desc";                   // 作业描述：英语作业，数学测验，语文主观作业等
    public static final String RES_HOMEWORK_CARD_SUBTITLE = "hw_card_subtitle";           // 作业副标题：请学习01-15作业的课程等
    public static final String RES_HOMEWORK_CARD_LIST = "hw_card_list";                   // 作业卡列表
    public static final String RES_HOMEWORK_MAKE_UP_FLAG = "hw_make_up_flag";             // 补做卡标志：true补做卡，false非补做卡
    public static final String RES_HOMEWORK_UNFINISH_PRACTICES = "hw_unfinish_practices"; // 未完成的作业练习
    public static final String RES_HOMEWORK_AVG_SCORE = "hw_avg_score";                   // 作业平均分
    public static final String RES_HOMEWORK_SCORE = "hw_score";                           // 作业分数
    public static final String RES_HOMEWORK_FINISH_FLAG = "hw_finish_flag";               // 作业是否完成
    public static final String RES_HOMEWORK_PRACTICE_URL = "hw_practice_url";
    public static final String RES_HOMEWORK_SCORE_URL = "hw_score_url";
    public static final String RES_HOMEWORK_CARD_SOURCE = "hw_card_source";               // 作业卡类型：native or h5
    public static final String RES_HOMEWORK_CARD_VARIETY = "hw_card_variety";             // 作业卡的作业类型：homework or quiz
    public static final String RES_HOMEWORK_CARD_SUPPORT_FLAG = "hw_card_support_flag";   // 当前版本的app是否支持某种作业卡
    public static final String RES_HOMEWORK_CARD_SUPPORT_TYPE = "hw_card_support_type";   // 当前版本的app如果不支持某种作业卡，分类：新版本支持 or 全不支持.把这个值编入notSupportUrl的参数里传给h5
    public static final String RES_NO_SUPPORT_OBJECTIVE_CONFIG_TYPE = "noSupportObjectiveConfigType";
    public static final String RES_HTML5_URL = "html5_url";
    public static final String RES_HTML5_HOMEWORK_RESULT_URL = "html5_hw_result_url";
    public static final String RES_PRACTICE_NEED_RECORD = "practice_need_record"; //
    public static final String REQ_CATEGORY_ID = "category_id";
    public static final String RES_PRACTICE_ID = "practice_id";
    public static final String RES_PRACTICE_NAME = "practice_name";
    public static final String RES_PICTURE_BOOK_IDS = "picture_book_ids";
    public static final String RES_PRACTICE_VERSION = "practice_version";
    public static final String RES_PRACTICE_BIG_VERSION = "practice_big_version";
    public static final String RES_PRACTICE_CATEGORY = "practice_catagory";
    public static final String RES_UNIT_ID = "unit_id";
    public static final String RES_LESSON_ID = "lesson_id";
    public static final String RES_POINT_ID = "point_id";
    public static final String RES_QUESTION_NUM = "question_num";
    public static final String RES_FINISH_QUESTION_NUM = "finish_question_num";
    public static final String RES_DATE_TYPE = "date_type";
    public static final String RES_PRACTICE_ONLINE = "practice_online";
    public static final String RES_VACATION_HOMEWORK_PACKAGE_ID = "package_id";
    public static final String RES_SURPASS_COUNT = "surpass_count";                   // 超越多少个同学
    public static final String RES_TAB_LIST = "tab_list";
    public static final String RES_OBJECTIVE_CONFIG_TYPE = "objective_config_type";
    public static final String RES_HTML5_NEWHOMEWORK_INDEX_URL = "html5_nh_index_url";
    public static final String RES_NEWHOMEWORK_INDEX_URL = "nh_index_url";
    public static final String RES_NEWHOMEWORK = "nh";
    public static final String RES_HTML5_NEWEXAM_INDEX_URL = "html5_newexam_index_url";
    public static final String RES_NEWEXAM_INDEX_URL = "newexam_index_url";
    public static final String RES_NEWEXAM_ID = "newexam_id";
    public static final String RES_RENDER_TYPE = "renderType";

    public static final String RES_VOICE_RATIO = "voice_ratio";
    public static final String RES_TIMEOUT_PARAM = "timeout_param";
    public static final String RES_ORAL_SCORE_INTERVAL = "oral_score_interval";

    public static final String RES_SHOW_BUTTON = "show_button";
    public static final String RES_SHOW_JZT = "show_jzt";

    public static final String RES_QUIZ_ID = "quiz_id";
    public static final String RES_QUIZ_CATEGORY = "quiz_category";
    public static final String RES_QUIZ_PAPER_ID = "quiz_paper_id";
    public static final String RES_QUIZ_PUSH_ID = "quiz_push_id";

    public static final String RES_MESSAGE_LIST = "message_list";
    public static final String RES_MESSAGE_ID = "message_id";
    public static final String RES_MESSAGE_TITLE = "message_title";
    public static final String RES_MESSAGE_SUMMARY = "message_summary";
    public static final String RES_MESSAGE_IMGURL = "message_imgurl";
    public static final String RES_MESSAGE_LINK = "message_link";
    public static final String RES_MESSAGE_CREATETIME = "message_createtime";
    public static final String RES_MESSAGE_TYPE = "message_type";
    public static final String RES_MESSAGE_IS_TOP = "is_top";

    public static final String RES_ERROR_POINTS_COUNT = "error_points_count";
    public static final String RES_ERROR_POINTS_LIST = "error_points_list";
    public static final String RES_ERROR_POINTS_DATE = "error_points_date";
    public static final String RES_ERROR_POINTS_RESULT = "error_points_result";
    public static final String RES_TOTAL_COUNT = "total_count";
    public static final String RES_TOTAL_ERROR_COUNT = "total_error_count";

    public static final String RES_ONLINE_CSM = "online_csm";
    public static final String RES_NOT_SUPPORT_HOMEWORK_LINK = "not_support_homework_link";// 不支持的作业类型，会跳转到这个地址，引导用户下载新版本App
    public static final String RES_NOT_SUPPORT_HOMEWORK_LINK_PARAM = "not_support_homework_link_param"; // 给不支持页面传递的参数列表，json字符串格式
    public static final String RES_TEACHER_WORKBOOK_LIST = "teacher_workbook_list";        // 老师选择的教辅列表,supplementary list
    public static final String RES_STUDENT_WORKBOOK_LIST = "student_workbook_list";        // 学生选择的教辅列表,supplementary list
    public static final String RES_WORKBOOK_RECOMMEND_FLAG = "workbook_recommend_flag";    // true表示是老师推荐的，false表示使用地区内部的教辅
    public static final String RES_WORKBOOK_MORE_FLAG = "workbook_more_flag";              // true表示学生还有更多选择
    public static final String RES_WORKBOOK_TITLE = "workbook_title";
    public static final String RES_WORKBOOK_ALIAS = "workbook_alias";
    public static final String RES_WORKBOOK_ID = "workbook_id";
    public static final String RES_WORKBOOK_COVER = "workbook_cover";
    public static final String RES_WORKBOOK_LIST = "workbook_list";
    public static final String RES_WORKBOOK_HOMEWORK_FINISHED = "workbook_homework_finished";
    public static final String RES_WORKBOOK_HOMEWORK_LIST = "workbook_homework_list";
    public static final String RES_WORKBOOK_HOMEWORK_INTEGRAL = "workbook_homework_integral";

    public static final String RES_WORKBOOK_HOMEWORK_STANDARD = "workbook_homework_standard";     // 教辅作业播放时长比例标准，60代表播放时长需要大于60%才算达标
    public static final String RES_OCR_MENTAL_CONFIG = "ocr_mental_config"; // 纸质拍照配置
    public static final String RES_OCR_MENTAL_IMAGE_WIDTH = "image_width"; // 纸质拍照图片宽度
    public static final String RES_OCR_MENTAL_IMAGE_QUALITY = "image_quality"; // 纸质拍照图片质量
    public static final String RES_OCR_MENTAL_IMAGE_BRIGHT_TH = "image_bright_th"; // 纸质拍照图片亮度
    public static final String RES_OCR_MENTAL_IMAGE_DARK_TH = "image_dark_th"; // 纸质拍照图片暗度
    public static final String RES_OCR_MENTAL_IMAGE_CLEAR_TH = "image_clear_th"; // 纸质拍照图片清晰度
    public static final String RES_OCR_MENTAL_AD_LIST = "ad_list"; // 纸质拍照banner广告
    public static final String RES_OCR_MENTAL_SHOW_PARENT_LINK = "show_parent_link"; // 纸质拍照是否显示家长端下载入口
    public static final String RES_OCR_MENTAL_PARENT_LINK_TEXT = "parent_link_text"; // 纸质拍照家长端下载文案
    public static final String RES_OCR_MENTAL_PARENT_LINK_URL = "parent_link_url"; // 纸质拍照家长端下载链接

    public static final String RES_CONTENT_LIST = "content_list";
    public static final String RES_CONTENT_CATALOG = "content_catalog";
    public static final String RES_WORKBOOK_CATALOG = "workbook_catalog";
    public static final String RES_WORKBOOK_CATALOG_ID = "workbook_catalog_id";
    public static final String RES_CONTENT_START_PAGE = "content_start_page";
    public static final String RES_CONTENT_WAVE_URI = "content_wave_uri";
    public static final String RES_CONTENT_WAVE_DURATION = "content_wave_duration";
    public static final String RES_CONTENT_ID = "workbook_content_id";

    public static final String RES_DUPLICATE_REALNAME = "duplicate_realname";
    public static final String RES_HAS_LOGIN = "has_login";
    public static final String RES_FEATURE_LIST = "feature_list";
    public static final String RES_PARAMS = "params";
    public static final String RES_INIT_PARAMS = "initParams";


    public static final String RES_HOMEWORK_UNAUTH_MSG = "不是你的作业/消息，无法查看";
    public static final String RES_DIFFERENT_CLAZZ_MSG = "学生已有班级，但选择的班级跟加入过的班级不一致";
    public static final String RES_ALREADY_IN_CLAZZ_MSG = "已在此班级内,如需更换老师,请点击更换班级";
    public static final String RES_SUP_NOT_EXIST_MSG = "不存在此教辅";


    // 主观作业 RES begin
    public static final String RES_SUBJECTIVE_HOMEWORK_ID = "subjective_homework_title";
    public static final String RES_SUBJECTIVE_CONTENT = "subjective_content";
    public static final String RES_SUBJECTIVE_SUBJECT = "subjective_subject";
    public static final String RES_SUBJECTIVE_FILETYPE = "subjective_filetype";
    public static final String RES_SUBJECTIVE_IMAGE_SIZE = "subjective_image_size";                                     // 主观作业图片上传的最大容量，单位兆
    public static final String RES_SUBJECTIVE_AUDIO_SIZE = "subjective_audio_size";                                     // 主观作业音频录制的最大时间，单位秒
    public static final String RES_SUBJECTIVE_AUDIO_MIN_SIZE = "subjective_audio_min_size";                             // 主观作业音频录制的最小时间，单位秒
    public static final String RES_SUBJECTIVE_AUDIO_COUNT = "subjective_audio_count";                                   // 主观作业音频录制的最大个数限制
    public static final String RES_SUBJECTIVE_IMAGE_COUNT = "subjective_image_count";                                   // 主观作业图片上传的最大个数限制


    public static final String RES_HOMEWORK_NOT_EXIST_MSG = "没有作业ID";
    public static final String RES_HOMEWORK_ALREADY_COMMIT_MSG = "该次作业已提交";
    public static final String RES_SUBJECTIVE_NOT_EXIST_MSG = "找不到主观作业";
    public static final String RES_SUBJECTIVE_FILETYPE_ERROR_MSG = "主观作业类型错误";
    public static final String RES_SUBJECTIVE_UPLOAD_FAIL_MSG = "上传失败！";
    public static final String RES_SUBJECTIVE_UPLOAD_FILE_SIZE_MSG = "上传文件失败！";
    // 主观作业 RES end

    public static final String RES_NOT_SUPPORT_JUNIOR_MSG = "该版本不支持中学账号登录";
    public static final String RES_VH_COMPLETE_LAST_HALF_START_DATE_MSG = "7月任务已完成，请8月1日再做吧~";

    public static final String RES_SERVICE_TEMPORARILY_UNAVAILIBLE = "前方网络异常拥堵,正在加紧疏导,请一小时后再试";

    public static final String RES_NEED_BACKGROUND_SCORE = "need_background_score";    // 使用后台打分模式

    public static final String RES_RESULT_OK_BUTTON_TEXT = "去下载";
    public static final String RES_RESULT_CANCEL_BUTTON_TEXT = "知道了";
    public static final String RES_RESULT_JUNIOR_MESSAGE = "中学生请下载“一起中学学生”哦。";
    public static final String RES_RESULT_JUNIOR_STUDENT = "亲爱的中学生，“一起中学学生”应用发布了，快去下载哦";
    public static final String RES_RESULT_PRIMARY_MESSAGE = "您是小学学生，请您下载“一起小学学生”进行体验。";
    public static final String RES_RESULT_PRIMARY_MESSAGE2 = "如需换班至小学，请您下载“一起小学学生”进行体验。";
    public static final String RES_RESULT_PRIMARY_LINK = "https://www.17zuoye.com/view/mobile/common/download?app_type=17student";

}
