/**
 * @author xinqiang.wang
 * @description "转让班级"
 * @createDate 2016/8/11
 */

define(["$17", "knockout", 'logger', "komapping", "weuijs"], function ($17, ko, logger) {

    var TransferClazzModel = function () {
        var self = this;

        self.getSubjectValue = function (subjectName) {
            var subjectValue = '';
            switch (subjectName) {
                case "ENGLISH":
                    subjectValue = '英语';
                    break;
                case "MATH":
                    subjectValue = '数学';
                    break;
                case "CHINESE":
                    subjectValue = '语文';
                    break;
            }
            return subjectValue;
        };
        self.subject = ko.observable(LoggerProxy.subject);
        self.subjectList = transferclazzMap.subjects;
        for (var i = 0; i < self.subjectList.length; i++) {
            self.subjectList[i]['checked'] = !i;
            if(self.subjectList[i]['checked']){
                self.subject(self.subjectList[i].name);
            }

        }
        self.subjectList = ko.mapping.fromJS(self.subjectList);

        /*选择学科*/
        self.selectSubjectBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.subjectList(), function (subject) {
                subject.checked(false);
            });
            that.checked(true);
            self.subject(that.name());
            self.teacherId(0);
            self.findLinkTeacher();
        };

        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_nhtorhOt'
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);
        };

        self.clazzId = $17.getQuery("clazzId");
        self.teachersList = ko.observableArray([]);
        self.teacherId = ko.observable(0);


        self.findLinkTeacher = function () {
            $.showLoading();
            $.post('findlinkteacher.vpage', {cid: self.clazzId, subject: self.subject()}, function (data) {
                $.hideLoading();
                if (data.success) {
                    for (var i = 0; i < data.teachers.length; i++) {
                        data.teachers[i].checked = false;
                    }
                    self.teachersList(ko.mapping.fromJS(data.teachers)());
                }

            });
        };

        /*初始化*/
        self.findLinkTeacher();

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
            $.showLoading();
            $.post('sendtransferapp.vpage', {
                cid: self.clazzId,
                respondentId: self.teacherId(),
                subject: self.subject()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    self.sendLog({
                        op: "o_JTmFoCkD"
                    });
                    $.toast("转让请求发送成功", function () {
                        location.href = '/teacher/clazzmanage/list.vpage';
                    });
                } else {
                    $.alert(data.info);
                }
            });
        };

        /*邀请*/
        self.inviteBtn = function(){
            setTimeout(function(){
                location.href = LoggerProxy.wechatJavaToPythonUrl+'teacher/ucenter/invite/face2face/showqrcode.vpage?_from=index';
            },200);
            self.sendLog({
                op: "o_t1i3N9Sg"
            });
        };

        self.sendLog({
            op: "o_dlovHM2a"
        });
    };
    ko.applyBindings(new TransferClazzModel());

});