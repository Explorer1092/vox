/**
 * @author xinqiang.wang
 * @description "机构收集点评活动页"
 * @createDate 2016/9/8
 */
define(['jquery', 'knockout', "knockoutscroll", "weui", 'voxLogs'], function ($, ko) {
    var RemarkModel = function () {
        var self = this;
        self.listDetail = ko.observableArray([]);
        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(1);
        self.totalPage = ko.observable(0);
        self.module = 'm_Ug7dW2ob';

        /*获取列表*/
        self.loadActivityRatings = function () {
            if (self.listDetail().length == 0) {
                $.showLoading();
            }
            $.post('/mizar/loadactivityratings.vpage', {
                time: remarkMap.time,
                pageSize: self.pageSize(),
                pageNum: self.pageNum()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    ko.utils.arrayForEach(data.rows, function (rows) {
                        if (rows.userAvatar == '' || rows.userAvatar == null) {
                            rows.userAvatar = remarkMap.avatar;
                        }else{
                            if (rows.userAvatar.indexOf('oss-image.17zuoye.com') != -1) {
                                rows.userAvatar = rows.userAvatar + "@100w_1o";
                            }
                        }
                        if (rows.photo && rows.photo.length > 0) {
                            for (var i = 0; i < rows.photo.length; i++) {
                                if (rows.photo[i].indexOf('oss-image.17zuoye.com') != -1) {
                                    rows.photo[i] = rows.photo[i] + "@100w_1o";
                                }
                            }
                        }
                        self.listDetail.push(rows);
                    });
                    self.totalPage(data.totalPage);
                } else {
                    $.alert(data.info);
                }
            }).fail(function () {
                $.hideLoading();
            });
        };

        /*详情页*/
        self.gotoDetailBtn = function () {
            var that = this;
            location.href = '/mizar/remark/detail.vpage?shopId=' + that.shopId;
        };

        /*滚屏加载数据*/
        self.scrolled = function () {
            if (self.pageNum() < self.totalPage()) {
                self.pageNum(self.pageNum() + 1);
                self.loadActivityRatings();
            }
        };

        /*初始化*/
        self.loadActivityRatings();

        YQ.voxLogs({
            database: 'parent',
            module: self.module,
            op: "o_pqo2KQ5T"
        });
    };

    ko.applyBindings(new RemarkModel());
});
