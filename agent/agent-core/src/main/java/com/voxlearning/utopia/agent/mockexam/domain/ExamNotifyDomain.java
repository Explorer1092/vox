package com.voxlearning.utopia.agent.mockexam.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 通知服务：通知方式有邮件、天机和天权三种方式
 *
 * @Author: peng.zhang
 * @Date: 2018/8/22
 */
public interface ExamNotifyDomain {

    /**
     * 发送通知
     *
     * @param request 通知信息
     * @return
     */
    void send(Request request);

    /**
     * 消息参数
     */
    @Data
    class Request implements Serializable {

        /**
         * 标题
         */
        @NotNull(message = "标题不可为空")
        String title;

        /**
         * 消息
         */
        @NotNull(message = "标题不可为空")
        String message;

        /**
         * 接收人
         */
        @NotNull(message = "标题不可为空")
        List<Long> receiver;

        /**
         * 通知类型
         */
        List<Type> types;

        /**
         * 通知类型
         */
        @AllArgsConstructor
        public enum Type {
            EMAIL("邮件"),
            SYSTEM("系统通知"),
            SMS("短信"),
            PUSH("PUSH消息");
            public final String desc;
        }
    }
}
