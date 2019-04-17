/**
 * @author xinqiang.wang
 * @description "班级管理"
 * @createDate 2016/8/4
 */

define(["$17", "knockout", "logger", "komapping", "weuijs"], function ($17, ko, logger) {

    var ClazzModel = function () {
        var self = this;
        self.studentsDetail = ko.observableArray([]);
        self.clazzId = $17.getQuery("clazzId");

        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_nhtorhOt'
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);
        };

        self.getStudents = function () {
            $.showLoading();
            $.post("/teacher/clazzmanage/clazzdetail.vpage", {cid: self.clazzId}, function (data) {
                if (data.success) {
                    self.studentsDetail(ko.mapping.fromJS(data.students)());
                }
                $.hideLoading();
            }).fail(function () {
                $.hideLoading();
            });
        };


        /*转让班级*/
        self.transferBtn = function () {
            setTimeout(function () {
                location.href = "/teacher/clazzmanage/transferclazz.vpage?clazzId=" + self.clazzId;
            }, 200);

        };

        /*添加老师*/
        self.addTeacherBtn = function () {
            setTimeout(function () {
                location.href = '/teacher/clazzmanage/addteacher.vpage?clazzId=' + self.clazzId;
            }, 200);

        };

        /*小组管理*/
        /*self.tinyGroupBtn = function () {
            setTimeout(function () {
                location.href = LoggerProxy.wechatJavaToPythonUrl + 'teacher/tinygroup/awardadjust.vpage?cid=' + self.clazzId;
            }, 200);
            self.sendLog({
                op: 'o_0Mk2MbGR'
            });
        };*/

        /*班级学豆*/
        self.clazzIntegralBtn = function () {
            setTimeout(function () {
                location.href = LoggerProxy.wechatJavaToPythonUrl + 'teacher/clazzmanage/clazzintegral.vpage?cid=' + self.clazzId;
            }, 200);
            self.sendLog({
                op: 'o_E2PabG1P'
            });
        };

        /*邀请学生加入*/
        self.inviteStudentBtn = function () {
            setTimeout(function () {
                location.href = LoggerProxy.wechatJavaToPythonUrl + 'teacher/regist/success.vpage?_from=class';
            }, 200);
            self.sendLog({
                op: 'o_k25EqHU3'
            });
        };

        /*是否显示排行榜*/
        $(document).on('click', '.JS-move_btn01', function () {
            var $this = $('.move_btn');
            var $content = "是否确定开启学生端排行榜？<br/>将会通知班级学生和其他老师哦";
            var $currentShow = true;
            //判断当前showRank状态 若为true,传递false，反之亦然
            if ($showRank) {
                $content = "是否确定关闭学生端排行榜？<br/>将会通知班级学生和其他老师哦";
                $currentShow = false;
            }

            $.confirm({
                title: '提示',
                text: $content,
                onOK: function () {
                    $.post("updateshowrankflag.vpage", {showRank: $currentShow, cid: self.clazzId}, function (data) {
                        if (data.success) {
                            $.alert("设置成功", function () {
                                $showRank = $currentShow;
                                if ($showRank) {
                                    $this.addClass("sc-right").removeClass("sc-left");
                                } else {
                                    $this.addClass("sc-left").removeClass("sc-right");
                                }
                            });
                        } else {
                            $.alert(data.info);
                        }
                    });
                }
            });
        })


        /*init*/
        self.getStudents();

        self.sendLog({
            op: 'o_TOblSNYw'
        });
    };

    ko.applyBindings(new ClazzModel());
});
