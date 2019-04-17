package com.voxlearning.utopia.agent.mockexam.service.dto.enums;

import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 试卷中的枚举值
 *
 * @Author: peng.zhang
 * @Date: 2018/8/24
 */
public interface ExamPaperEnums {

    /**
     * 试卷来源
     *
     * @see <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=40406376#id-%E4%B8%80%E8%B5%B7%E6%B5%8B%E6%8E%A5%E5%8F%A3-%E6%9F%A5%E8%AF%A2%E8%AF%95%E5%8D%B7">一起测接口-查询试卷</a>
     */
    @AllArgsConstructor
    enum Source {
        //       0:'通用', 1:'本地化', 2:'O2O/2.0', 3:'自制试卷'
        LOCAL("1", "本地化"),
        GENERAL("0", "通用化");
        public final String code;
        public final String desc;

        public static Source codeOf(String code) {
            return Arrays.stream(values()).filter(i -> i.code.equals(code)).findFirst().orElse(null);
        }

        public static Source nameOf(String name) {
            return Arrays.stream(values()).filter(i -> i.name().equals(name)).findFirst().orElse(null);
        }
    }

    @AllArgsConstructor
    enum Form {
        MONTHLY("月考"),
        UNITE("单元考"),
        MID("期中考"),
        FINAL("期末考");
        public final String desc;
    }

    /**
     * 是否开放给其它地区
     */
    @AllArgsConstructor
    enum isPublic {

        IS_OPEN("已开放"),
        NOT_OPEN("未开放");

        public final String desc;
    }

    /**
     * 试卷状态
     */
    @AllArgsConstructor
    enum Status {
        PAPER_CHECKING("审核中"),
        PAPER_REJECT("被驳回"),
        PAPER_PROCESSING("录入中"),
        PAPER_READY("已录入");
        public final String desc;
    }

    /**
     * 模块类型
     */
    @AllArgsConstructor
    enum PartType {
        NORMAL("normal", "普通"),
        ORAL("oral", "口语"),
        LISTENING("listening", "听力");

        @Getter
        private String type;
        public final String desc;

        public static List<PartType> of(List<String> names) {
            return Arrays.stream(values()).filter(i -> names.contains(i.getType())).collect(Collectors.toList());
        }

        public static List<ExamPlanEnums.Type> mapper(List<ExamPaperEnums.PartType> partTypes) {
            if (CollectionUtils.isEmpty(partTypes)) {
                return Collections.emptyList();
            }
            List<ExamPlanEnums.Type> result = new ArrayList<>();

            partTypes.forEach(partType -> {
                switch (partType) {
                    case NORMAL:
                        result.add(ExamPlanEnums.Type.GENERAL);
                        break;
                    case ORAL:
                        result.add(ExamPlanEnums.Type.SPOKEN);
                        break;
                    case LISTENING:
                        result.add(ExamPlanEnums.Type.AUDITION);
                        break;
                    default:break;
                }
            });
            return result;
        }
    }
}
