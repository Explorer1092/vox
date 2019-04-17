/**
 * @author xinqiang.wang
 * @description "编辑学生"
 * @createDate 2016/8/5
 */

define(["$17", "knockout", "logger", 'weuijs'], function ($17, ko, logger) {
    var StudentModel = function () {
        var self = this;
        self.studentId = $17.getQuery("studentId");
        self.clazzId = ko.observable(0);
        self.studentDetail = ko.observable({});

        self.getStudent = function () {
            $.showLoading();
            $.post("studentdetail.vpage", {studentId: self.studentId}, function (data) {
                $.hideLoading();
                if (data.success) {
                    self.studentDetail(data.student);
                    self.clazzId(data.student.clazzId);
                    top.document.title = data.student.studentName || data.student.studentId;
                }else{
                    $.toast(data.info, "forbidden");
                }
            });
        };
        self.getStudent();

        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_nhtorhOt'
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);
        };


        /*删除该学生*/
        self.deleteStudentBtn = function () {
            $.confirm({
                title: '提示',
                text: '确定删除该学生？',
                onOK: function () {
                    $.showLoading();
                    $.post("batchremovestudents.vpage", {sids: self.studentId, cid: self.clazzId()}, function (data) {
                        $.hideLoading();
                        if (data.success) {
                            $.toast("删除成功。", function () {
                                location.href = 'editclazz.vpage?clazzId=' + self.clazzId();
                            })
                        } else {
                            $.toast(data.info, "forbidden")
                        }
                    });
                    self.sendLog({
                        op: "o_uaNk4bf9"
                    });
                },
                onCancel: function () {
                }
            });

            self.sendLog({
                op: "o_2Xda6SIy"
            });
        };

        /*重置密码*/
        self.resetPasswordBtn = function () {
            var mobile = self.studentDetail().mobile;
            var name = self.studentDetail().studentName;
            if (!$17.isBlank(mobile)) {
                $.modal({
                    title: "提示",
                    text: "正在帮" + name + "同学重置密码，请取得学生或家长同意后操作密码将发送至学生家长手机" + mobile + "",
                    buttons: [
                        {
                            text: "取消",
                            className: "default",
                            onClick: function(){

                            }
                        },
                        {
                            text: "重置密码",
                            onClick: function () {
                                $.post("resetstudentpassword.vpage", {
                                    sid: self.studentId,
                                    cid: self.clazzId()
                                }, function (data) {
                                    if (data.success) {
                                        self.sendLog({
                                            op: "o_6u6iPOzP"
                                        });
                                        $.toast("重置成功", 'text', function () {
                                            location.href = '';
                                        })
                                    } else {
                                        $.alert(data.info);
                                        self.sendLog({
                                            op: "o_zWb7Wpw7",
                                            s0: data.info
                                        });
                                    }
                                }).fail(function(){
                                    self.sendLog({
                                        op: "o_zWb7Wpw7",
                                        s0: "请求失败"
                                    });
                                });
                                self.sendLog({
                                    op: "o_DIIPsoOt"
                                });
                            }
                        }
                    ]
                });
                self.sendLog({
                    op: "o_xU9IYgvC"
                });

            } else {
                var param = {
                    studentId: self.studentId,
                    clazzId: self.clazzId()
                };
                location.href = 'password.vpage?' + $.param(param);
            }

            self.sendLog({
                op: "o_Eh97DWzR"
            });
        };

        self.sendLog({
            op: "o_RlrUZB7E"
        });
    };
    ko.applyBindings(new StudentModel());
});