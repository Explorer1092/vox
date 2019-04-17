package com.voxlearning.utopia.service.newhomework.api.constant;

/**
 * 作业订正题目状态枚举
 * @author majianxin
 * @date 2018/5/25
 * @version V1.0
 */
public enum QuestionCorrectStatus {
    TODO,           //(待学习/待订正)
    DOING,           //一个课程对应多个后测题, 订正中(后测题没有全部完成)
    CORRECT,        //(测试正确/订正正确)
    WRONG,          //(测试错误/订正错误)
    FINISH;         //课程后测题全部做完

    public static QuestionCorrectStatus of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
