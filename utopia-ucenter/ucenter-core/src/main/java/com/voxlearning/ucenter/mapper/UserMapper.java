package com.voxlearning.ucenter.mapper;

import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * User mapper data structure.
 *
 * @author Lin Zhu
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @author Yizhou Zhang
 * @serial
 * @since 2011-08-03
 */
@NoArgsConstructor
public class UserMapper implements Serializable {
    private static final long serialVersionUID = 586396934437497084L;

    @Getter
    @Setter
    private Boolean disabled = false;           // 是否有效
    @Getter
    @Setter
    private Date createTime;                    // 创建时间
    @Getter
    @Setter
    private Integer authenticationState = 3;    // 认证状态
    @Getter
    @Setter
    private Integer homeworkState = 0;
    @Getter
    @Setter
    private Integer isFirst = 0;
    @Getter
    @Setter
    private Integer registerType = 0;
    @Getter
    @Setter
    private Integer userType;                   // 用户类型
    @Getter
    @Setter
    private InvitationType invitationType;
    @Getter
    @Setter
    private Long childId;                       // 孩子学号
    @Getter
    @Setter
    private Long id;                            // 一起作业学号
    @Getter
    @Setter
    private String callName;                    // 称呼
    @Getter
    @Setter
    private String childName;                   // 孩子姓名
    @Getter
    @Setter
    private String childPassword;
    @Getter
    @Setter
    private String childRole;                   // 孩子角色
    @Getter
    @Setter
    private String childSchoolName;
    @Getter
    @Setter
    private String clazzId;                     // 班级编号
    @Getter
    @Setter
    private String clazzName;                   // 班级名称
    @Deprecated
    @Getter
    @Setter
    private String dataAuthorityRegion;         // 数据权限范围地理信息编码
    @Getter
    @Setter
    private String email;                       // 电子邮件
    @Getter
    @Setter
    private String inviteEmail = "";
    @Getter
    @Setter
    private String inviteInfo;
    @Getter
    @Setter
    private String inviteMobile = "";
    @Getter
    @Setter
    private String mobile;                      // 手机号码
    @Getter
    @Setter
    private String password;                    // 密码
    @Getter
    @Setter
    private String realname;                    // 真实姓名
    @Getter
    @Setter
    private String role;                        // 用户角色
    @Getter
    @Setter
    private String subject;                     //老师所属学科
    @Getter
    @Setter
    private String code;                        // 手机验证码
    @Getter
    @Setter
    private String dataKey;                     // 缓存数据Key
    @Getter
    @Setter
    private String invitation;                  // 邀请CODE
    @Getter
    @Setter
    private String webSource;                   // 数据来源
    @Getter
    @Setter
    private String teacherId;                   // 老师ID
    @Getter
    @Setter
    private String gender;                      // 性别
    @Getter
    @Setter
    private String scanNumber;                  //填涂号
}
