package com.voxlearning.utopia.service.business.constant;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;

public class TeacherTaskConstant {

    // 测试环境2月19号0点 线上2月26号0点
    public static final long ROOKIE_TASK_SPLIT = RuntimeMode.le(Mode.STAGING) ? 1550505600000L : 1551110400000L;

    public static final int ROOKIE_CHECK_STUDENT_SIZE = RuntimeMode.ge(Mode.STAGING) ? 10 : 2;

    public static final int ROOKIE_COMMENT_STUDENT_SIZE = RuntimeMode.ge(Mode.STAGING) ? 5 : 2;

    public static final long MONTH_START_TIME = RuntimeMode.isProduction() ? 1551369600000L : 0L; // 3月1号

    public static final long INVI_REWARD_DAY_LIMIT = 30;


}
