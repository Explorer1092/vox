/*
 * 会员中心-订单列表
 */
define(["jquery", "$17", "knockout", "userpopup"], function ($, $17, knockout, userpopup) {
    /****************变量声明***********/
    var reSetStuPwdModalAndView = {
        sNumber: knockout.observable(0),
        sBeans: knockout.observable(0),
        newPwd: knockout.observable(""),
        newPwdConfirm: knockout.observable(""),
        changePwd: changePwd
    };

    /****************方法声明***********/
    function changePwd() {
        var pwd = reSetStuPwdModalAndView.newPwd();
        var pwdConfirm = reSetStuPwdModalAndView.newPwdConfirm();
        if (pwd == "") {
            $17.jqmHintBox("密码不能为空");
            reSetStuPwdModalAndView.newPwdConfirm("");
            return;
        }
        if (pwd != "" && pwdConfirm == "") {
            $17.jqmHintBox("请再次输入新密码");
            return;
        }
        if (pwd != "" && pwdConfirm != "" && pwd != pwdConfirm) {
            $17.jqmHintBox("两次输入密码不一致");
            reSetStuPwdModalAndView.newPwd("");
            reSetStuPwdModalAndView.newPwdConfirm("");
            return;
        }
        var data = {
            sid: reSetStuPwdModalAndView.sNumber,
            pwd: pwd
        };

        $.post("/parent/ucenter/resetstudentpwd.vpage", data, function (result) {
            if (result.success) {
                $17.jqmHintBox("密码修改成功");
                setTimeout(function () {
                    location.href = "/parent/ucenter/index.vpage";
                }, 1000);
            }
        });
    }


    /****************事件交互***********/
    userpopup.selectStudent("resetstudentpwd");

    knockout.applyBindings(reSetStuPwdModalAndView);

    return {
        loadMessageById: function (sid) {
            for (var i = 0; i < students.length; i++) {
                if (students[i].id == sid) {
                    reSetStuPwdModalAndView.sNumber(sid);
                    reSetStuPwdModalAndView.sBeans(students[i].integral);
                    break;
                }
            }
        }
    };


});