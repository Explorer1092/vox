/**
 * @author xinqiang.wang
 * @description "添加老师"
 * @createDate 2016/8/11
 */

define(["$17", "knockout","logger", "komapping", "weuijs"], function ($17, ko, logger) {
    var AddTeacherModel = function () {
        var self = this;
        self.clazzId = $17.getQuery("clazzId");
        self.subjectListDetail = ko.observableArray([]);

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

        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_nhtorhOt'
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);
        };

        self.getTeacherList = function () {

            $.post("getclazzotherteachers.vpage", {cid: self.clazzId}, function (data) {
                var subjectList = [];
                if (data.success) {
                    var linkApplicationSentSubjects = data.linkApplicationSentSubjects;
                    for (var i in data.teachers) {
                        var subjectMap = {};
                        subjectMap.subjectName = self.getSubjectValue(i);
                        subjectMap.subjectEnName = i;
                        subjectMap.teacher = data.teachers[i];
                        if (linkApplicationSentSubjects.indexOf(i) != -1) {
                            subjectMap.justSentApplication = true;
                        } else {
                            subjectMap.justSentApplication = false;
                        }

                        subjectList.push(subjectMap);
                    }

                }
                self.subjectListDetail(ko.mapping.fromJS(subjectList)());
            });

        };
        self.getTeacherList();


        self.addTeacherBtn = function () {
            var that = this;
            var param = {
                clazzId: self.clazzId,
                subject: that.subjectEnName(),
                subjectName : that.subjectName()
            };
            setTimeout(function(){
                location.href = 'teacherlist.vpage?'+ $.param(param);
            },200);
            var op = '';
            switch (that.subjectEnName()){
                case "ENGLISH":
                    op = 'o_zR4BG95X';
                    break;
                case "MATH":
                    op = 'o_QPhL3xL8';
                    break;
                case "CHINESE":
                    op = 'o_1ywCJ8C6';
                    break;
            }
            self.sendLog({
                op : op
            });
        };

        self.sendLog({
            op: "o_1TXogGLY"
        });
    };
    ko.applyBindings(new AddTeacherModel());
});