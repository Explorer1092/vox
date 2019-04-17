package com.voxlearning.utopia.service.mizar.impl.service.talkfun;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TK_CourseData;
import com.voxlearning.utopia.service.mizar.api.utils.talkfun.TalkFunCommand;
import com.voxlearning.utopia.service.mizar.impl.support.TalkFunHttpUtils;
import com.voxlearning.utopia.service.mizar.talkfun.TalkFunUtils;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.mizar.talkfun.TalkFunUtils.*;

/**
 * 用来测试欢拓相关接口
 * Created by Yuechen.Wang on 2017/1/10.
 */
public class TalkFunTest {

    @SneakyThrows
    public static void main(String[] args) throws Exception {
        Map<String, Object> paramMap;
        TalkFunCommand command;
        Class<?> documentClass;

        paramMap = mockLive();
        command = TalkFunCommand.COURSE_LIVE;
        documentClass = TK_CourseData.class;

//        System.out.println(TalkFunUtils.coursePlaybackUrl(paramMap, Mode.TEST, false));
        testGet(paramMap, command, documentClass);
    }

    private static <T> void testPost(Map<String, Object> paramMap, TalkFunCommand command, Class<T> beanType) {
        MapMessage retMsg = TalkFunHttpUtils.post(paramMap, command, Mode.TEST);
        if (!retMsg.isSuccess()) {
            System.out.println(String.format("%s Post Failed", command));
        }
        System.out.println("Response: " + JsonUtils.toJsonPretty(retMsg));
        String json = JsonUtils.toJson(retMsg.get("data"));
        T data = parseReturnData(json, beanType);
        if (data != null) {
            System.out.println("Parsed Data: " + JsonUtils.toJsonPretty(data));
        }
    }

    private static <T> void testGet(Map<String, Object> paramMap, TalkFunCommand command, Class<T> beanType) {
        MapMessage retMsg = TalkFunHttpUtils.get(paramMap, command, Mode.STAGING);
        if (!retMsg.isSuccess()) {
            System.out.println(String.format("%s Get Failed", command));
        }
        System.out.println("Response: " + JsonUtils.toJsonPretty(retMsg));
        String json = JsonUtils.toJson(retMsg.get("data"));
//        T data = parseReturnData(json, beanType);
        List<T> data = parseReturnList(json, beanType);
        if (data != null) {
            System.out.println("Parsed Data: " + JsonUtils.toJsonPretty(data));
        }
    }

    private static Map<String, Object> mockAddCourse() {
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "course_name", "17作业测试新增接口"); // 课程名称
        appendParam(params, "start_time", "2017-01-18 18:30:23"); // 课程开始时间
        appendParam(params, "end_time", "2017-01-18 18:34:23"); // 课程结束时间
        appendParam(params, "account", "587dbda5e92b1bb36fa3db2b"); // 发起直播课程的第三方主播账号
        appendParam(params, "nickname", "刘老师"); // 主播的昵称
        return params;
    }

    private static Map<String, Object> mockTeacher() {
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "account", ObjectId.get().toHexString()); // 接入方自已的主播唯一ID
        appendParam(params, "nickname", "17测试员01"); // 主播昵称
//        appendParam("intro", appendParam("一个大写的人")); // 	主播简介
        return params;
    }

    private static Map<String, Object> mockLive() {
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "course_id", "26485");
        appendParam(params, "uid", "test");
        appendParam(params, "nickname", "ssss");
        appendParam(params, "role", "user");

        System.out.println(JsonUtils.toJson(MiscUtils.m("ssl", true, "avatar", "", "gender", 0)));
        appendParam(params, "options", MiscUtils.m("ssl", true, "avatar", "", "gender", 0));
        return params;
    }

}
