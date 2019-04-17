/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/9/9
 */

define(['jquery', 'knockout',"$17", "weui", 'voxLogs'], function ($, ko) {

    var RemarkModel = function () {
        var self = this;
        self._from = $17.getQuery("_from");

        self.searchContent = ko.observable('');
        self.searchDetail = ko.observableArray([]);
        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(0);
        self.totalPage = ko.observable(0);

        self.loadSearchList = function () {
            $.showLoading();
            $.post('/mizar/loadshops.vpage', {
                shopName: self.searchContent(),
                pageSize: self.pageSize(),
                pageNum: self.pageNum()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    if (data.rows) {
                        ko.utils.arrayForEach(data.rows, function (rows) {
                            self.searchDetail.push(rows);
                        });
                        self.totalPage(data.totalPage);
                    }
                } else {
                    $.alert(data.info);
                }
            }).fail(function(){
                $.hideLoading();
            });
        };

        /*搜索*/
        self.searchBtn = function () {
            self.searchDetail([]);
            self.pageNum(0);
            self.loadSearchList();
        };

        /*滚屏加载数据*/
        self.scrolled = function () {
            if (self.pageNum() < self.totalPage()) {
                self.pageNum(self.pageNum() + 1);
                self.loadSearchList();
            }
        };

        self.gotoDetailBtn = function(){
            location.href = '/mizar/remark/detail.vpage?shopId='+this.id+"&_from=search";
        };

        YQ.voxLogs({
            database: 'parent',
            module: 'm_Ug7dW2ob',
            op: "o_BfU0ntHu",
            s0: self._from == 'top' ? ' 搜索框' : '参与投票',
            s1: self.searchContent()
        });

    };
    ko.applyBindings(new RemarkModel());
});