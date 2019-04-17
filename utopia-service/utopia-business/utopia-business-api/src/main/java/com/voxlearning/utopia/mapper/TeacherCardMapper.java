package com.voxlearning.utopia.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.business.api.constant.TeacherCardType;
import lombok.Data;

import java.io.Serializable;

/**
 * @author guoqiang.li
 * @since 2017/4/17
 */
@Data
public class TeacherCardMapper implements Serializable {
    private static final long serialVersionUID = 1457453875588499210L;

    private String cardName;                // 卡片名称
    private TeacherCardType cardType;       // 卡片类型
    private String cardDescription;         // 卡片描述
    private String progress;                // 任务完成进度
    private String progressColor;           // 进度颜色值(移动端用到),可能为null或者空字符串
    private String imgUrl;                  // 图片地址
    private String detailUrl;               // 移动端详情页地址
    private Long teacherId;                 // 老师id
    private Subject subject;                // 学科
    private String cardDetails;             // 详情json
    private String btnContent;              // 按钮文字
}
