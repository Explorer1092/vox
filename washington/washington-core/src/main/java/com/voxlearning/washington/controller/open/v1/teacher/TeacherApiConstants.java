package com.voxlearning.washington.controller.open.v1.teacher;

/**
 * 详情参考wiki http://wiki.17zuoye.net/pages/viewpage.action?pageId=9734563
 * Created by Alex on 14-12-29.
 */
public class TeacherApiConstants {

    // REQUEST
    public static final String REQ_CLAZZ_LEVEL = "clazz_level";
    public static final String REQ_BOOK_TYPE = "book_type";
    public static final String REQ_UNIT_ID = "unit_id";
    public static final String REQ_HOMEWORK_TYPE = "homework_type";
    public static final String REQ_HOMEWORK_ID = "homework_id";
    public static final String REQ_HOMEWORK_IDS = "homework_ids";
    public static final String REQ_HOMEWORK_COMMENT = "comment";
    public static final String REQ_HOMEWORK_AUDIO_COMMENT = "audio_comment";
    public static final String REQ_CLAZZ_LIST = "clazz_list";
    public static final String REQ_HOMEWORK_DATA = "homework_data";
    public static final String REQ_SMART_CLAZZ_REWARD_DATA = "reward_data";
    public static final String REQ_HOMEWORK_ENDTIME = "homework_end_time";
    public static final String REQ_EXAM_DATA = "exam_data";
    public static final String REQ_EXAM_DATE = "exam_date";
    public static final String REQ_EXAM_ID = "exam_id";

    public static final String REQ_PROVINCE_CODE = "province_code";
    public static final String REQ_PROVINCE_NAME = "province_name";
    public static final String REQ_CITY_CODE = "city_code";
    public static final String REQ_CITY_NAME = "city_name";
    public static final String REQ_COUNTY_CODE = "county_code";
    public static final String REQ_COUNTY_NAME = "county_name";
    public static final String REQ_DETAIL_ADDRESS = "detail_address";
    public static final String REQ_RECEIVER = "receiver";
    public static final String REQ_RECEIVER_PHONE = "receiver_phone";
    public static final String REQ_POST_CODE = "post_code";
    public static final String REQ_SHIPPING_TYPE = "shipping_type";

    public static final String REQ_PAGE_NUMBER = "page_number";
    public static final String REQ_INTEGRAL_COUNT = "integral_count";
    public static final String REQ_MESSAGE_ID = "message_id";
    public static final String REQ_MESSAGE_CONTENT = "message_content";
    public static final String REQ_STUDENT_LIST = "student_list";
    public static final String REQ_IS_CORRECT = "is_correct";
    public static final String REQ_CLAZZ_GROUP_IDS = "clazz_group_ids";
    public static final String REQ_CLAZZID_GROUPID_LIST = "clazzid_groupid_list";
    public static final String REQ_INTEGRAL_USER_JSON = "integral_user_json"; // 发学豆的json
    public static final String REQ_CORRECT_JSON = "correct_json"; // 批改数据json
    public static final String REQ_HOMEWORK_STATUS = "homework_status";
    public static final String REQ_CONTENT_DATA = "content_data";
    public static final String REQ_SEARCH_DATA = "search_data";
    public static final String REQ_HISTORY_DATA = "history_data";
    public static final String REQ_COLLECTION_DATA = "collection_data";


    // RESPONSE
    public static final String RES_TEACHER_SUBJECT = "subject";
    public static final String RES_TEACHER_KTWELVE = "ktwelve";
    public static final String RES_TEACHER_CLAZZ_LIST = "clazz_list";
    public static final String RES_TEACHER_COMMENT_LIST = "teacher_comment_list";
    public static final String RES_CLAZZ_SIZE = "clazz_size";
    public static final String RES_CLAZZ_TEACHER_LIST = "clazz_teachers";

    public static final String RES_GROUP_FREE_JOIN = "free_join";
    public static final String RES_CLAZZ_SHOW_RANK = "show_rank";
    public static final String RES_HOMEWORK_STATUS_TYPE = "hw_status_type";
    public static final String RES_HOMEWORK_STATUS_DESC = "hw_status_desc";
    public static final String RES_HOMEWORK_CAN_ASSIGN = "hw_can_assign";
    public static final String RES_HOMEWORK_MESSAGE = "hw_message";
    public static final String RES_HOMEWORK_ID = "hw_id";
    public static final String RES_HOMEWORK_NAME = "hw_name";
    public static final String RES_HOMEWORK_START_DATE = "hw_start_date";
    public static final String RES_HOMEWORK_END_DATE = "hw_end_date";
    public static final String RES_HOMEWORK_PAST_DUE = "hw_past_due";
    public static final String RES_HOMEWORK_FINISH_COUNT = "hw_finish_count";
    public static final String RES_HOMEWORK_UNFINISH_COUNT = "hw_unfinish_count";
    public static final String RES_HOMEWORK_UNDO_COUNT = "hw_undo_count";
    public static final String RES_HOMEWORK_CREATE_TIME = "hw_create_time";
    public static final String RES_HOMEWORK_ASSIGNED_UNIT_NAME = "hw_assigned_unit_name";
    public static final String RES_HOMEWORK_INTEGRAL = "hw_integral";
    public static final String RES_HOMEWORK_CLAZZ_AVERAGE_FINISH_TIME = "hw_average_finish_time";
    public static final String RES_HOMEWORK_CLAZZ_AVERAGE_SCORE = "hw_average_score";
    public static final String RES_HOMEWORK_BASIC_EXIST = "hw_basic_exist";
    public static final String RES_HOMEWORK_EXAM_EXIST = "hw_exam_exist";
    public static final String RES_HOMEWORK_READING_EXIST = "hw_reading_exist";
    public static final String RES_HOMEWORK_SPECIAL_EXIST = "hw_special_exist";
    public static final String RES_HOMEWORK_STUDENT_SCORE = "hw_student_score";
    public static final String RES_HOMEWORK_STUDENT_BASIC_SCORE = "hw_student_basic_score";
    public static final String RES_HOMEWORK_STUDENT_BASIC_CATEGORY_SCORE = "hw_student_basic_category_score";
    public static final String RES_HOMEWORK_STUDENT_EXAM_SCORE = "hw_student_exam_score";
    public static final String RES_HOMEWORK_STUDENT_READING_SCORE = "hw_student_reading_score";
    public static final String RES_HOMEWORK_STUDENT_SPECIAL_SCORE = "hw_student_special_score";
    public static final String RES_HOMEWORK_STUDENT_DETIAL = "hw_student_detial";
    public static final String RES_SPEECH_DATA_URL = "speech_data_url";

    public static final String RES_HOMEWORK_STUDENT_FINISH_STATE = "hw_student_finish_state";
    public static final String RES_HOMEWORK_STUDENT_JOIN_STATE = "hw_student_join_state";
    public static final String RES_HOMEWORK_STUDENT_FINISH_TIME = "hw_student_finish_time";
    public static final String RES_HOMEWORK_STUDENT_RECORDING = "hw_student_recording";
    public static final String RES_HOMEWORK_STUDENT_BASIC_DETAILS = "hw_student_basic_details";
    public static final String RES_HOMEWORK_STUDENT_EXAM_DETAILS = "hw_student_exam_details";
    public static final String RES_HOMEWORK_STUDENT_SPECIAL_DETAILS = "hw_student_special_details";
    public static final String RES_HOMEWORK_STUDENT_SCORE_TYPE = "hw_student_score_type"; //1=基础练习，2=应试练习，3=阅读绘本(英语)，4=知识点练习(数学)
    public static final String RES_HOMEWORK_STUDENT_SCORE_TITLE = "hw_student_score_title"; //基础练习，应试练习，阅读绘本(英语)，知识点练习(数学)
    public static final String RES_HOMEWORK_STUDENT_SCORE_LIST = "hw_student_score_list";
    public static final String RES_HOMEWORK_COMMENT = "hw_comment";
    public static final String RES_HOMEWORK_COMMENT_TEMPLATE = "hw_comment_template";

    public static final String RES_HOMEWORK_NORM_FINISH_TIME = "hw_norm_finish_time";
    public static final String RES_HOMEWORK_JSON = "hw_json";
    public static final String RES_HOMEWORK_PAPER_JSON = "hw_paper_json";
    public static final String RES_HOMEWORK_TYPE = "hw_type";
    public static final String RES_HOMEWORK_DO_USER = "hw_do_user";
    public static final String RES_HOMEWORK_UNDO_USER = "hw_undo_user";
    public static final String RES_HOMEWORK_PART0_USER = "hw_part0_user";
    public static final String RES_HOMEWORK_PART1_USER = "hw_part1_user";
    public static final String RES_HOMEWORK_PART2_USER = "hw_part2_user";
    public static final String RES_HOMEWORK_PART3_USER = "hw_part3_user";
    public static final String RES_HOMEWORK_PART4_USER = "hw_part4_user";
    public static final String RES_HOMEWORK_FINISHED_USER = "hw_finished_user";
    public static final String RES_HOMEWORK_UNFINISHED_USER = "hw_unfinished_user";
    public static final String RES_HOMEWORK_RESULT_USERS = "hw_result_users";
    public static final String RES_HOMEWORK_ONLY_SUBJECTIVE = "hw_only_subjective";
    public static final String RES_EXAM_LIST = "exam_list";

    public static final String RES_BOOK_LIST = "book_list";
    public static final String RES_HOMEWORK_LESSON_LIST = "hw_lesson_list";
    public static final String RES_HOMEWORK_SPECIAL_LESSON_LIST = "hw_special_lesson_list";
    public static final String RES_UNIT_NAMES = "unit_names";
    public static final String RES_LESSON_ID = "lesson_id";
    public static final String RES_LESSON_NAME = "lesson_name";
    public static final String RES_POINT_LIST = "point_list";
    public static final String RES_PRACTICE_CATEGORY_ID = "practice_category_id";
    public static final String RES_PRACTICE_CATEGORY = "practice_category";
    public static final String RES_PRACTICE_CATEGORY_CHECKED = "practice_category_checked";
    public static final String RES_PRACTICE_CATEGORY_ICON = "practice_category_icon";
    public static final String RES_PRACTICE_CATEGORY_PREVIEW_ICON = "practice_category_preview_icon";
    public static final String RES_PRACTICE_CATEGORY_ARRANGE_COUNT = "practice_category_arrange_count";
    public static final String RES_PRACTICE_ID = "practice_id";
    public static final String RES_PRACTICE_NAME = "practice_name";
    public static final String RES_PRACTICE_TIME = "practice_time";
    public static final String RES_PRACTICE_CHECKED = "practice_checked";
    public static final String RES_PRACTICE_COUNT = "practice_count";
    public static final String RES_PRACTICE_LIST = "practice_list";
    public static final String RES_PRACTICE_TYPE_LIST = "practice_type_list";    //英语
    public static final String RES_PRACTICE_POINT_LIST = "practice_point_list";   //数学
    public static final String RES_PRACTICE_POINT_ID = "practice_point_id";
    public static final String RES_PRACTICE_POINT_NAME = "practice_point_name";
    public static final String RES_PRACTICE_POINT_CHECKED = "practice_point_checked";
    public static final String RES_PRACTICE_POINT_PLAYTIME = "practice_point_playtime";
    public static final String RES_PRACTICE_POINT_ICON = "practice_point_icon";
    public static final String RES_PRACTICE_POINT_COUNT = "practice_point_count";                              //每个知识点下选中的练习题数量
    public static final String RES_PRACTICE_POINT_ARRANGE_COUNT = "practice_point_arrange_count";
    public static final String RES_PRACTICE_SPECIAL_POINT_ICON_91 = "practice_special_point_icon_91";
    public static final String RES_PRACTICE_SPECIAL_POINT_ICON_92 = "practice_special_point_icon_92";
    public static final String RES_PRACTICE_SPECIAL_POINT_ICON_93 = "practice_special_point_icon_93";
    public static final String RES_DATA_TYPE_COUNT_LIST = "data_type_count_list";//专项知识点类型：91-预习，92-巩固，93-拓展

    public static final String RES_HOMEWORK_READING_LIST = "hw_reading_list";
    public static final String RES_READING_ID = "reading_id";
    public static final String RES_READING_CNAME = "reading_cname";
    public static final String RES_READING_ENAME = "reading_ename";
    public static final String RES_READING_IMG_URL = "reading_img_url";
    public static final String RES_READING_COVER2 = "reading_cover2";
    public static final String RES_READING_WORDS_COUNT = "reading_words_count";
    public static final String RES_READING_COVER_WORD_COUNT = "reading_cover_word_count";
    public static final String RES_READING_ABOVE_LEVEL_WORD_COUNT = "reading_above_level_word_count";
    public static final String RES_READING_RECOMMENT_TIME = "reading_recomment_time";
    public static final String RES_READING_ARRANGE_COUNT = "reading_arrange_count";

    public static final String RES_HOMEWORK_EXAM_LIST = "hw_exam_list";
    public static final String RES_HOMEWORK_EXAM_POINT_LIST = "hw_exam_point_list";
    public static final String RES_HOMEWORK_EXAM_TIME = "hw_exam_time";         //应试，每个exam的时间，单位秒
    public static final String RES_HOMEWORK_EXAM_ID = "hw_exam_id";
    public static final String RES_HOMEWORK_EXAMS = "hw_exams";
    public static final String RES_HOMEWORK_EXAM_PATTERN = "hw_exam_pattern";
    public static final String RES_HOMEWORK_EXAM_ANSWER = "hw_exam_answer";
    public static final String RES_OFFLINE_UNIT_TEST_URL = "offlineunittest_url";
    public static final String RES_OFFLINE_UNIT_TEST_URL_VALUE = "/view/newexamv2/homework_card";

    public static final String RES_NEWHOMEWORK_QUESTION_URL = "question_url";
    public static final String RES_NEWHOMEWORK_COMPLETED_URL = "completed_url";
    public static final String RES_NEWHOMEWORK_OBJECTIVETYPE = "type";
    public static final String RES_NEWHOMEWORK_QUESTIONS = "/teacher/new/homework/questions";
    public static final String RES_NEWHOMEWORK_QUESTIONS_ANSWER = "/teacher/new/homework/questions/answer";
    public static final String RES_NEWHOMEWORK_LIST = "hw_list";
    public static final String RES_NEWHOMEWORK_TERMREVIEW_LIST = "hw_basic_review_list";
    public static final String RES_DOMAIN = "domain";
    public static final String RES_HOMEWORK_SHARE_URL = "homework_share_url";
    public static final String RES_HOMEWORK_NEED_SHARE = "homework_need_share";
    public static final String RES_HOMEWORK_URL = "homework_url";
    public static final String RES_PREVIEW_URL = "preview_url";
    public static final String RES_REPORT_URL = "report_url";
    public static final String RES_EXAM_DETAIL_URL = "exam_detail_url";
    public static final String RES_OFFLINEHOMEWORK_URL = "offlinehomework_url";
    public static final String RES_OFFLINEHOMEWORK_DETAIL_URL = "offlinehomework_detail_url";
    public static final String RES_KNOWLEDGE_DETAIL_URL = "knowledge_detail_url";
    public static final String RES_GOAL_DETAIL_URL = "goal_detail_url";
    public static final String RES_EXPAND_HOMEWORK_URL = "expand_homework_url";
    public static final String RES_EXPAND_HOMEWORK_PREVIEW_URL = "expand_homework_preview_url";
    public static final String RES_EXPAND_HOMEWORK_VIDEO_DETAIL_URL = "expand_homework_video_detail_url";
    public static final String RES_HOMEWORK_URL_VALUE = "/view/homeworkv5/index";
    public static final String RES_TERMREVIEW_URL_VALUE = "/view/termreview/package_detail_dispatch";
    public static final String RES_PREVIEW_URL_VALUE = "/view/homeworkv5/previewhomework";
    public static final String RES_REPORT_URL_VALUE = "/view/report/index";
    public static final String RES_EXAM_DETAIL_URL_VALUE = "/view/newexam/teacherreport";
    public static final String RES_OFFLINEHOMEWORK_URL_VALUE = "/view/offlinehomework/index";
    public static final String RES_OFFLINEHOMEWORK_DETAIL_URL_VALUE = "/view/offlinehomework/detail";
    public static final String RES_KNOWLEDGE_DETAIL_URL_VALUE = "/view/homework/knowledgedetail";
    public static final String RES_PAPER_DETAIL_URL_VALUE = "/view/newexam/paperpreview";
    public static final String RES_EXPAND_HOMEWORK_URL_VALUE = "/view/extracurricular/assign/index";
    public static final String RES_EXPAND_HOMEWORK_PREVIEW_URL_VALUE = "/view/extracurricular/assign/homeworkpreview";
    public static final String RES_EXPAND_HOMEWORK_VIDEO_DETAIL_URL_VALUE = "/view/extracurricular/report/videopreview";
    public static final String RES_QUESTION_INFOS = "question_infos";
    public static final String RES_GOAL_SUMMARY = "goal_summary";
    public static final String OPEN_GOAL_SETTING = "open_goal_setting";
    public static final String RES_PAPER_MODULE_LIST = "paper_module_list";
    public static final String RES_CHANNEL_LIST = "channel_list";
    public static final String RES_DUBBING_LIST = "dubbing_list";
    public static final String RES_DUBBING_DETAIL = "dubbing_detail";
    public static final String RES_TOTAL_SZIE = "total_size";
    public static final String RES_PAGE_COUNT = "page_count";
    public static final String RES_PAGE_NUM = "page_num";
    public static final String RES_TOPIC_LIST = "topic_list";
    public static final String RES_SERIES_LIST = "series_list";
    public static final String RES_WORDS = "words";
    public static final String RES_PICTURE_BOOK_LIST = "picture_book_list";
    public static final String RES_DESCRIPTION = "desc";
    public static final String RES_IMG_URL = "img_url";
    public static final String RES_BOOK = "book";
    public static final String RES_OBJECTIVE_CONFIG_ID = "objective_config_id";
    public static final String RES_ORAL_COMMUNICATION_LIST = "oral_communication_list";
    public static final String RES_ORAL_COMMUNICATION_DETAIL = "oral_communication_detail";

    public static final String RES_AUTH_STATE = "auth_state";
    //    public static final String RES_JPUSH_LIST = "jpush_tags";
    public static final String RES_DETAIL_ADDRESS = "detail_address";
    public static final String RES_RECEIVER = "receiver";
    public static final String RES_RECEIVER_PHONE = "receiver_phone";
    public static final String RES_POST_CODE = "post_code";
    public static final String RES_SHIPPING_TYPE = "shipping_type";
    public static final String REQ_CONFIG_KEYS = "config_keys";
    public static final String RES_DEFAULT_INTEGRAL_COUNT = "dc";
    public static final String RES_MAX_INTEGRAL_COUNT = "mc";
    public static final String RES_OVEW_TIME_GIDS = "over_time_gids";
    public static final String RES_LIMIT_TIME = "limit_time";


    public static final String RES_CLAZZ_INTEGRAL = "clazz_integral";
    public static final String RES_STUDENT_LIST = "student_list";
    public static final String RES_SHARE_URL = "share_url";
    public static final String RES_SHARE_CLAZZ_CONFIG = "share_clazz_config";
    public static final String RES_CENTER_CONFIG = "center_config";
    public static final String RES_SHARE_TITLE = "share_title";
    public static final String RES_SHARE_CONTENT = "share_content";
    public static final String RES_STUDENT_INTEGRAL = "student_integral";
    public static final String RES_TEACHER_INTEGRAL_COUNT = "teacher_integral_count";
    public static final String RES_CURRENT_SUBJECT = "current_subject";

    public static final String RES_MESSAGE_ID = "message_id";
    public static final String RES_MESSAGE_TITLE = "message_title";
    public static final String RES_MESSAGE_PAYLOAD = "message_payload";
    public static final String RES_MESSAGE_CREATE_TIME = "message_create_time";
    public static final String RES_MESSAGE_STATUS = "message_status";
    public static final String RES_MESSAGE_SENDER_AVATAR = "message_sender_avatar";
    public static final String RES_MESSAGE_LIST = "message_list";
    public static final String RES_LETTER_ID = "letter_id";
    public static final String RES_MESSAGE_MODERATOR = "message_moderator";

    public static final String RES_REPLY_ID = "reply_id";
    public static final String RES_REPLY_TIME = "reply_time";
    public static final String RES_REPLY_PAYLOAD = "reply_payload";
    public static final String RES_REPLY_SENDER_ID = "reply_sender_id";
    public static final String RES_REPLY_SENDER_NAME = "reply_sender_name";
    public static final String RES_REPLY_LIST = "reply_list";
    public static final String RES_DIAGNOSIS_HABIT_DETAIL = "diagnosis_habit_detail";


    public static final String RES_UNREAD_MESSAGE_COUNT = "unread_message_count";

    // MESSAGE
    public static final String RES_NOT_CLAZZ_TEACHER_MSG = "您不是此班级的老师，不能进行此操作！";
    public static final String RES_SUBJECT_ERROR_MSG = "目前作业只支持英语和数学！";
    public static final String RES_UNIT_NOT_EXIST_MSG = "不存在此单元！";
    public static final String RES_UNAUTHENTICATION_TEACHER_MSG = "未认证老师不能进行此操作！";
    public static final String RES_CLAZZ_NOT_EXIST_MSG = "不存在此班级！";
    public static final String RES_TEACHER_NO_SUBJECT_MSG = "老师没有学科!";
    public static final String RES_TEACHER_CLAZZ_NO_SUBJECT_MSG = "老师在此班级没有教该学科!";
    public static final String RES_NOT_SUPPORT_BOOK_MSG = "暂不支持该教材";
    public static final String RES_UNITTEST_SUBJECT_ERROR_MSG = "只支持数学老师哦~";

    public static final String RES_RESULT_NOT_BELONG_CLAZZ_MSG = "此班级不属于您!";
    public static final String RES_RESULT_DUPLICATE_ARRANGE_HOMEWORK = "作业布置中，请不要重复布置!";
    public static final String RES_RESULT_DUPLICATE_CHECK_HOMEWORK = "作业检查中，请不要重复检查!";
    public static final String RES_RESULT_HOMEWORK_CHECKED = "作业已经检查，请不要重复检查!";
    public static final String RES_RESULT_DUPLICATE_DELETE_HOMEWORK = "作业删除中，请不要重复操作!";
    public static final String RES_RESULT_HOMEWORK_NOT_EXIST = "不存在此作业！";
    public static final String RES_RESULT_HOMEWORK_ENDDATE_ERROR_MSG = "作业结束时间错误！";
    public static final String RES_RESULT_EXAM_ENDDATE_ERROR_MSG = "考试结束时间错误！";
    public static final String RES_RESULT_INTEGRAL_ZERO_MSG = "学豆数量不能为0!";
    public static final String RES_RESULT_HOMEWORK_NOT_ALLOW_CORRECT = "此份作业已不允许批改！";

    public static final String RES_RESULT_DUPLICATE_NEWEXAM_ASSIGN = "考试发布中，请不要重复发布！";
    public static final String RES_RESULT_NEWEXAM_ASSIGN_FAILE_MSG = "考试发布失败！";

    public static final String RES_RESULT_CLAZZ_INTEGRAL_COUNT_ERROR_MSG = "奖励学豆数必须正整数并且是5的倍数！";

    public static final String RES_RESULT_EMPTY_SUBJECT_ERROR_MSG = "您还没有设置学科及班级，请去PC端完成设置后再登录！";
    public static final String RES_RESULT_SUBJECT_ERROR_MSG = "学科不正确!";
    public static final String RES_RESULT_COMMENT_TOO_LONG_MSG = "您输入的评语过长，请输入100字以内！";
    public static final String RES_RESULT_COMMENT_FAILE_MSG = "评语失败！";
    public static final String RES_RESULT_REPORT_ERROR_MSG = "生成报告错误，请联系客服！";
    public static final String RES_RESULT_SCHOOL_NO_CLAZZ = "该学校没有班级!";
    public static final String RES_RESULT_CREATE_CLAZZ_ERROR = "创建班级失败!";

    public static final String RES_RESULT_CHANGE_NAME_ERROR = "修改姓名失败!";
    public static final String RES_RESULT_CHANGE_GENDER_ERROR = "修改性别失败!";

    public static final String RES_RESULT_SUSPEND_MSG = "移动老师端已暂停服务，请使用电脑登录www.17zuoye.com享受更多功能，新版正在研发中，敬请期待";

    public static final String RES_RESULT_DATA_PROCESSING_MSG = "处理中，请不要重复点击";
    public static final String RES_RESULT_OPERATION_FAILED_MSG = "操作失败，请重试";
    public static final String RES_RESULT_CHECK_SUCCESS_MSG = "检查成功";
    public static final String RES_RESULT_CORRECT_SUCCESS_MSG = "批改成功";
    public static final String RES_RESULT_ADJUST_SUCCESS_MSG = "调整成功";
    public static final String RES_RESULT_DELETE_SUCCESS_MSG = "删除成功";
    public static final String RES_RESULT_SECTION_ERROR_MSG = "课时错误";

    public static final String APP_MESSAGE_APPLICATION_URL_TEMPLATE = "http://www.test.17zuoye.com?id=";

    public static final String RES_RESULT_OK_BUTTON_TEXT = "去下载";
    public static final String RES_RESULT_CANCEL_BUTTON_TEXT = "知道了";
    public static final String RES_RESULT_JUNIOR_TEACHER = "您是中学老师，请您下载“一起中学老师”进行体验。";
    public static final String RES_RESULT_PRIMARY_TEACHER_MESSAGE = "您是小学老师，请您下载“一起小学老师”进行体验。";
    public static final String RES_RESULT_PRIMARY_TEACHER_BUTTON = "https://www.17zuoye.com/view/mobile/common/download?app_type=17Teacher";


}
