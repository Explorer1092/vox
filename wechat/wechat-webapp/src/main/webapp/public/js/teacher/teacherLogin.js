/**
 * @author xinqiang.wang
 * @description "老师登录"
 * @createDate 2016/7/29
 */
define([], function () {
    /*测试入口*/
    $("#teacher_login_submit_btn").on('click', function () {
        var tid = $('#tid').val();
        var pwd = $('#pwd').val();
        $.post("/signup/teacher/login.vpage", {token: tid, pwd: pwd}, function (data) {
            if (data.success) {
                location.href = '/teacher/homework/index.vpage';
            }else{
                alert(data.info);
            }
        });
    });
});