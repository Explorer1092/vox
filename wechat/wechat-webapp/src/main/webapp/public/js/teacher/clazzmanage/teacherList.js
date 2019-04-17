/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/8/11
 */

define(["$17", "knockout","logger", "komapping", "weuijs"], function ($17, ko, logger) {

    var TeacherListModel = function () {
        var self = this;

        self.clazzId = $17.getQuery("clazzId");
        self.subject = $17.getQuery("subject");
        self.teachersList = ko.observableArray([]);
        self.teacherId = ko.observable(0);
        top.document.title = "选择"+$17.getQuery("subjectName")+"老师";

        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_nhtorhOt'
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);
        };

        self.findLinkTeacher = function () {
            $.showLoading();
            $.post('findlinkteacher.vpage', {cid: self.clazzId, subject: self.subject}, function (data) {
                $.hideLoading();
                if (data.success) {
                    for (var i = 0; i < data.teachers.length; i++) {
                        data.teachers[i].checked = false;
                    }
                    self.teachersList(ko.mapping.fromJS(data.teachers)());
                }

            }).fail(function(){
                $.hideLoading();
            });
        };

        /*选择老师*/
        self.selectTeacherBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.teachersList(), function (_teacher) {
                _teacher.checked(false);
            });
            that.checked(true);
            self.teacherId(that.teacherId());
        };

        self.saveBtn = function () {
            $.confirm({
                title: '提示',
                text: '确认添加老师',
                onOK: function () {
                    $.showLoading();
                    $.post('sendlinkapp.vpage', {
                        cid: self.clazzId,
                        respondentId: self.teacherId(),
                        subject: self.subject
                    }, function (data) {
                        $.hideLoading();
                        if(data.success){
                            self.sendLog({
                                op: "o_8TknHjkG"
                            });
                            $.toast("添加成功", function () {
                                location.href = '/teacher/clazzmanage/addteacher.vpage?clazzId=' + self.clazzId;
                            });
                        } else {
                            $.alert(data.info);
                        }
                    });
                },
                onCancel: function () {
                }
            });
        };

        /*邀请*/
        self.inviteBtn = function(){
            setTimeout(function(){
                location.href = LoggerProxy.wechatJavaToPythonUrl+'teacher/ucenter/invite/face2face/showqrcode.vpage?_from=index';
            },200);
            self.sendLog({
                op: "o_UuZT0WXZ"
            });
        };

        /*初始化*/
        self.findLinkTeacher();

        self.sendLog({
            op: "o_nSnlTY1o"
        });
    };
    ko.applyBindings(new TeacherListModel());

});