/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/8/26
 */

define(["$17", "knockout", "komapping", 'logger'], function ($17, ko, komapping, logger) {
    var TeachersDayModel = function () {
        var self = this;
        self.clazzDetail = ko.observableArray([]);
        self.clazzId = ko.observable(0);
        self.from = $17.getQuery('_from');
        self.sendLogToTeacherApp = function(logMap){
            if (window.external && ('log_b' in window.external)) {
                window.external.log_b('',JSON.stringify(logMap));
            }
        };

        var list = teachersDayMap.blessList;

        for (var i = 0; i < list.length; i++) {
            list[i].checked = !i;
            if (list[i].checked) {
                self.clazzId(list[i].clazzId);
            }
        }
        self.clazzDetail(ko.mapping.fromJS(list)());

        /*选择年级*/
        self.selectClazzBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.clazzDetail(), function (_clazz) {
                _clazz.checked(false);
            });
            that.checked(true);
            self.clazzId(that.clazzId());
        };


        /*鲜花兑换学豆*/
        self.gotoIntegralBtn = function () {
            setTimeout(function () {
                location.href = LoggerProxy.wechatJavaToPythonUrl + "teacher/flower/list.vpage";
            }, 200);
        };

        /*生成照片墙*/
        self.gotoPhotosWallBtn = function () {
            setTimeout(function () {
                var param = {
                    teacherId: teachersDayMap.teacherId,
                    clazzId: self.clazzId()
                };
                location.href = "/activity/teachersdayshare.vpage?" + $.param(param);
            }, 200);
        };
        if(isFromTeacherApp){
            self.sendLogToTeacherApp({
                module: 'm_YGUa6u9c',
                op: 'o_ByWjRu7G',
                s0: self.from == 'banner' ? 'banner' : 'app_store'
            });

        }else{
            logger.log({
                app: 'teacher',
                module: 'm_B8i52sJm',
                op: 'o_XtGSZ8yk'
            });
        }
    };

    ko.applyBindings(new TeachersDayModel());
});
