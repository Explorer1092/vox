/**
 * Author:   xianlong.zhang
 * Date:     2019/1/30 15:12
 * Description: 初始化公司用户
 * History:
 */
package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserAccount {
    private String accountName;      //账号
    private String realName;         // 用户真实姓名
//    private String tel;                     // 电话
    private String email;                   // 邮箱
    private Float cashAmount = 0.0f;               // 现金账户余额
//    private Float pointAmount = 0.0f;              // 点数账户余额
    private Float usableCashAmount = 0.0f;         // 现金账户可用余额
//    private Float usablePointAmount = 0.0f;        // 点数账户可用余额
    private Integer status = 1;                    // 用户状态，0:新建，强制更新密码，1:有效，9:关闭
    private String accountNumber;           // 工号
    private String password;                 //密码明文
//    private String passwd;                  // 用户密码  加密
//    private String passwdSalt;              // 用户密码Salt
    private Long groupId;
    private Date contractStartDate;         // 合同开始时间

//    public CreateUserAccount(String accountName, String realName, String tel, String email, Float cashAmount, Float pointAmount, Float usableCashAmount, Float usablePointAmount, Integer status,
//                             String accountNumber,String password,  String passwd, String passwdSalt, Long groupId, Date contractStartDate) {
//        this.accountName = accountName;
//        this.realName = realName;
//        this.tel = tel;
//        this.email = email;
//        this.cashAmount = cashAmount;
//        this.pointAmount = pointAmount;
//        this.usableCashAmount = usableCashAmount;
//        this.usablePointAmount = usablePointAmount;
//        this.status = status;
//        this.accountNumber = accountNumber;
//        this.password = password;
//        this.passwd = passwd;
//        this.passwdSalt = passwdSalt;
//        this.groupId = groupId;
//        this.contractStartDate = contractStartDate;
//    }
}
