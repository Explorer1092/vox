/**
 * @author xinqiang.wang
 * @description "机构介绍"
 * @createDate 2016/9/8
 */
define(['jquery', 'knockout', "knockoutscroll", "weui", "$17", 'voxLogs'], function ($, ko) {
    var RemarkModel = function () {
        var self = this;
        self.shopId = $17.getQuery('shopId');
        self.activityId = remarkDetailMap.activityId;
        self._from = $17.getQuery("_from") || '';

        self.liked = ko.observable(remarkDetailMap.liked == 'true'); //freemarker boolean to string

        self.remarkList = ko.observableArray([]);
        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(1);
        self.totalPage = ko.observable(0);


        /*参与投票*/
        self.voteBtn = function () {
            $.showLoading();
            $.post('/mizar/likeshop.vpage', {shopId: self.shopId, activityId: self.activityId}, function (data) {
                $.hideLoading();
                if (data.success) {
                    self.liked(true);
                } else {
                    $.alert(data.info);
                }

            }).fail(function () {
                $.hideLoading();
            });
        };

        /*获取家长点评*/
        self.getRemarkList = function () {
            if (self.remarkList().length == 0) {
                $.showLoading();
            }
            $.post('/mizar/loadratingpage.vpage', {
                shopId: self.shopId,
                pageNum: self.pageNum(),
                pageSize: self.pageSize()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    ko.utils.arrayForEach(data.rows, function (rows) {
                        if (rows.avatar == null || rows.avatar == "") {
                            rows.avatar = remarkDetailMap.avatar;
                        } else {
                            if (rows.avatar.indexOf('oss-image.17zuoye.com') != -1) {
                                rows.avatar = rows.avatar + "@100w_1o";
                            }
                        }
                        if (rows.photos && rows.photos.length > 0) {
                            for (var i = 0; i < rows.photos.length; i++) {
                                if (rows.photos[i].indexOf('oss-image.17zuoye.com') != -1) {
                                    rows.photos[i] = rows.photos[i] + "@100w_1o";
                                }
                            }
                        }
                        self.remarkList.push(rows);
                    });
                    self.totalPage(data.totalPage);
                } else {
                    $.alert(data.info);
                }

            }).fail(function () {
                $.hideLoading();
            });
        };

        /*滚屏加载数据*/
        self.scrolled = function () {
            if (self.pageNum() < self.totalPage()) {
                self.pageNum(self.pageNum() + 1);
                self.getRemarkList();
            }
        };

        /*写点评*/
        self.remarkBtn = function () {
            location.href = '/mizar/remark/remark.vpage?shopId=' + self.shopId + "&activityId=" + remarkDetailMap.activityId + '&shopName=' + remarkDetailMap.shopName;
        };

        /*初始化*/
        self.getRemarkList();

        YQ.voxLogs({
            database: 'parent',
            module: 'm_Ug7dW2ob',
            op: "o_UzghXsYV",
            s0: self.shopId,
            s1: self._from == 'search' ? '搜索结果页' : '口碑机构活动页'
        });
    };

    ko.applyBindings(new RemarkModel());
});

