/**
 * @author xinqiang.wang
 * @description "重置密码"
 * @createDate 2016/8/8
 */


define(["$17", "knockout", "logger", 'weuijs'], function ($17, ko, logger) {
    var StudentModel = function () {
        var self = this;
        self.studentId = $17.getQuery("studentId");
        self.clazzId = $17.getQuery("clazzId");
        self.passwordValue = ko.observable();
        self.passwordConfirmValue = ko.observable();

        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_nhtorhOt'
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);
        };

        /*重置密码*/
        self.passwordSaveBtn = function () {
            if ($17.isBlank(self.passwordValue())) {
                $.toast("新密码不可为空", "forbidden");
                return false;
            }

            if ($17.isBlank(self.passwordConfirmValue())) {
                $.toast("确认新密码不可为空", "forbidden");
                return false;
            }

            if (self.passwordValue() != self.passwordConfirmValue()) {
                $.toast("密码不一致", "forbidden");
                return false;
            }

            $.showLoading();
            $.post("resetstudentpassword.vpage", {
                sid: self.studentId,
                cid: self.clazzId,
                p: self.passwordValue(),
                cp: self.passwordConfirmValue()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    self.sendLog({
                        op: "o_4Yc9j6NO"
                    });
                    $.toast("重置成功。", function () {
                        location.href = 'editstudent.vpage?studentId=' + self.studentId;
                    })
                } else {
                    $.toast(data.info, "forbidden")
                }
            });

            self.sendLog({
                op: "o_k5UlKfKa"
            });
        };

        self.sendLog({
            op: "o_aGOQaDnK"
        });
    };
    ko.applyBindings(new StudentModel());
});