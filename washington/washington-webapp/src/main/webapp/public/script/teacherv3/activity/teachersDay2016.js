/**
 * @author xinqiang.wang
 * @description "2016教师节活动专题页"
 * @createDate 2016/8/25
 */

define(['jquery', 'knockout', 'komapping', "$17", 'impromptu', 'voxLogs'], function ($, ko, komapping) {
    var TeachersDayModel = function () {
        var self = this;
        self.clazzDetail = ko.observableArray([]);
        self.from = $17.getQuery('_from');

        self.totalPage = ko.observable(0);
        self.currentPage = ko.observable(0);
        self.pageSize = ko.observable(4);
        self.clazzId = ko.observable(0);

        var list = teachersDayMap.blessList;

        self.totalPage(Math.ceil(list.length / self.pageSize()));
        for (var i = 0; i < list.length; i++) {
            list[i].checked = !i;
            list[i].pageNum = Math.floor(i / self.pageSize());
            for (var j = 0; j < list[i].blesses.length; j++) {
                list[i].blesses[j].avatar = teachersDayMap.imgDomain + list[i].blesses[j].avatar;
            }
            if (list[i].checked) {
                self.clazzId(list[i].clazzId);
            }
        }
        self.clazzDetail(komapping.fromJS(list)());

        /*选择年级*/
        self.selectClazzBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.clazzDetail(), function (_clazz) {
                _clazz.checked(false);
            });
            that.checked(true);
            self.clazzId(that.clazzId());
        };

        /*班级左滚动*/
        self.scrollLeftBtn = function () {
            if (self.currentPage() > 0) {
                self.currentPage(self.currentPage() - 1);
            }
        };

        /*班级右滚动*/
        self.scrollRightBtn = function () {
            if (self.currentPage() + 1 < self.totalPage()) {
                self.currentPage(self.currentPage() + 1);
            }
        };


        /*鲜花兑换学豆*/
        self.gotoIntegralBtn = function () {
            setTimeout(function () {
                location.href = '/teacher/flower/exchange.vpage?ref=teachersday';
            }, 200);
        };

        /*分享教师节祝福*/
        self.shareBtn = function () {
            if (teachersDayMap.toWechatJava.indexOf('http') == -1) {
                teachersDayMap.toWechatJava = location.protocol + teachersDayMap.toWechatJava;
            }
            var param = {
                teacherId: teachersDayMap.teacherId,
                clazzId:self.clazzId(),
                _from: 'qr_code'
            };
            var returnUlr = encodeURIComponent(teachersDayMap.toWechatJava + '/activity/teachersdayshare.vpage?' + $.param(param));
            var url = '/qrcode?m=' + returnUlr;
            $.prompt("<div style='text-align: center;'><p>使用微信扫描二维码</p><img src='" + url + "'></div>", {
                title: "系统提示",
                buttons: {
                    "关闭": true
                }

            });
            YQ.voxLogs({
                database: "web_teacher_logs",
                module: 'm_nkEI8RHF',
                op: 'o_l4jxXI8K'
            });
        };

        var s0 = '';
        if (self.from == 'news_feed') {
            s0 = 'news_feed';
        } else if (self.from == 'news_gift') {
            s0 = 'news_gift';
        } else {
            s0 = 'news_notice';
        }

        YQ.voxLogs({
            database: "web_teacher_logs",
            module: 'm_nkEI8RHF',
            op: 'o_nHYCwxk8',
            s0: s0
        });
    };

    ko.applyBindings(new TeachersDayModel());
});