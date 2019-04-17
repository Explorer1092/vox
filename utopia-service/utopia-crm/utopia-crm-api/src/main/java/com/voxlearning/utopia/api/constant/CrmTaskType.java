package com.voxlearning.utopia.api.constant;

/**
 * @author Jia HuanYin
 * @since 2015/7/8
 */
public enum CrmTaskType {
    退款,
    远程协助,
    客户回馈,
    投诉回电,
    电话回访,
    产品BUG,
    未呼通需跟进,
    呼通重点跟进,
    呼通一般,
    预约,
    回访流失老师,
    老师转校,
    老师新建班级,
    老师手机绑定解绑;

    public static CrmTaskType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
