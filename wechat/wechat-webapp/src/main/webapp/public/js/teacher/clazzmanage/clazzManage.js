/**
 * @author xinqiang.wang
 * @description 班级管理
 * @createDate 2016/8/4
 */

define(["$17", "knockout", "komapping","logger","weuijs"], function ($17, ko,kom, logger) {
    var ClazzModel = function () {
        var self = this;

        self.clazzDetail = ko.observableArray([]);
        self.isFakeTeacher = ko.observable(false); //是否为假老师
        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_nhtorhOt'
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);
        };

        self.getClazzDetail = function () {
            $.showLoading();
            $.post("/teacher/clazzmanage/clazzlist.vpage", {}, function (data) {
                $.hideLoading();
                if (data.success) {
                    self.clazzDetail(ko.mapping.fromJS(data.teacherClazzList)());
                    self.isFakeTeacher(data.isFakeTeacher);
                }else{
                    $.toast(data.info, 'text');
                }

            }).fail(function(){
                $.hideLoading();
            });
        };

        /*申请记录*/
        self.applicationRecordBtn = function(){
            setTimeout(function(){
                location.href = LoggerProxy.wechatJavaToPythonUrl+'teacher/clazzmanage/unprocessedapplication.vpage';
            },200);
            self.sendLog({
                op: "o_lFLgu14s"
            });
        };

        /*创建/退出班级*/
        self.createClazzBtn = function(){
            setTimeout(function(){
                location.href = '/teacher/clazzmanage/createclazz.vpage';
            },200);
            self.sendLog({
                op: "o_T6E4em52"
            });
        };

        //init
        self.getClazzDetail();

        self.sendLog({
            op: "o_NzNeYcmT"
        });
    };
    ko.applyBindings(new ClazzModel());
});
