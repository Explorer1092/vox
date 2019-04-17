package com.voxlearning.utopia.service.ai.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum TobbitScoreType {


    SIGNUP(30, "首次登录", 1),
    SHARE(5, "成功分享", 2),
    CHECK(5, "成功批改", 3),
    INVITE(10, "邀请新用户", 4),
    REDEEM50(-50, "兑换课程", 5),
    UNKNOW(0, "未定义", 0);


    private int score;
    private int type;
    private String name;

    private static TobbitScoreType[] arrays = TobbitScoreType.values();


    TobbitScoreType(int score, String name, int type) {
        this.score = score;
        this.type = type;
        this.name = name;
    }


    public static TobbitScoreType of(int type) {

        if (type > 0 && type < arrays.length) {
            return TobbitScoreType.values()[type - 1];
        }
        return UNKNOW;
    }


    public Map<String, Object> json() {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("name", getName());
        tmp.put("score", getScore());
        return tmp;

    }
}
