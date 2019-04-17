/**
 * 发布管理页
 */

define(["jquery", "knockout", "$17", "prompt", "datetimepicker", "paginator"], function ($, ko, $17) {
    var ArticleListModule = function () {
        var self = this;

        self.pageNum = ko.observable(1);
        self.pageSize = ko.observable(10);

        self.accountValue = ko.observable("");

        self.publishLeftNumsD = ko.observable(0); //今日还可发布数
        self.publishLeftNumsM = ko.observable(0); //本月还可发布数

        self.statDate = ko.observable("");
        self.endDate = ko.observable("");
        self.statusValue = ko.observable("");
        self.statusList = [{name: '全部', value: ''}, {name: '已发布', value: 'Published'}, {
            name: '未发布',
            value: 'Online'
        }, {name: '撤回', value: 'Offline'}];

        self.contentDetail = ko.observableArray([]);
        self.showDetail = ko.observableArray([]);
        self.accountList = ko.observableArray([]);
        self.isFirst = ko.observable(true);


        self.getArticleList = function () {

            $.post('/basic/officialaccount/articlelist.vpage', {
                accountId: self.accountValue(),
                start: $('#startTime').val(),
                end: $('#endTime').val(),
                status: self.statusValue()
            }, function (data) {
                if (data.success) {
                    self.accountList(data.accountList);
                    self.contentDetail(data.result);
                    self.getOnePageArticleList(1);

                    self.publishLeftNumsD(data.publishLeftNumsD);
                    self.publishLeftNumsM(data.publishLeftNumsM);

                    var paginator = $('#paginator');
                    setTimeout(function () {
                        if (paginator.length > 0 && self.contentDetail().length > 0) {
                            paginator.jqPaginator({
                                totalPages: Math.ceil(self.contentDetail().length / self.pageSize()),
                                visiblePages: 10,
                                currentPage: self.pageNum(),
                                first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
                                prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
                                next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
                                last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
                                page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
                                onPageChange: function (pageIndex, opType) {
                                    if (opType == 'change') {
                                        //self.pageNum(pageIndex);
                                        self.getOnePageArticleList(pageIndex);
                                        $('html, body').animate({scrollTop: '0px'}, 0);
                                    }
                                }

                            });
                        }
                    }, 100);
                }
            });
        };

        self.getArticleList();

        //公众号
        self.accountChanged = function () {
            if (!self.isFirst()) {
                self.getArticleList();
            }
            self.isFirst(false);
        };

        //状态
        self.statusChanged = function () {
            self.getArticleList();
        };

        self.getOnePageArticleList = function (page) {
            self.showDetail([]);
            var group = self.contentDetail.slice((page - 1) * self.pageSize(), page * self.pageSize());
            ko.utils.arrayForEach(group, function (at) {
                ko.utils.arrayForEach(at.articles, function (articles) {
                    articles.simgUrl = $17.ossImg(articles.imgUrl, 55);
                });

                self.showDetail.push(at);
            });
        };

        //发布
        self.pushBtn = function () {
            var that = this;
            if (self.publishLeftNumsD() <= 0 || self.publishLeftNumsM() <= 0) {
                alert('当天/本月发布次数已达到最大值，无法再新建发布');
                return false;
            }

            var md = (self.publishLeftNumsD() - 1) <= 0 ? 0 : self.publishLeftNumsD() - 1;
            var mm = (self.publishLeftNumsM() - 1) <= 0 ? 0 : self.publishLeftNumsM() - 1;
            $.prompt("<p>本次发布成功后今日还可发布" + md + "次，本月还可发布" + mm + "次</p><p>是否确认发布？文章发布后将不能修改。</p>", {
                title: "提示",
                buttons: {"取消": false, "发布": true},
                submit: function (e, v, m, f) {
                    if (v) {
                        $.post('/basic/officialaccount/publisharticle.vpage', {
                            bundleId: that.bundleId,
                            accountId: self.accountValue()
                        }, function (data) {
                            if (data.success) {
                                self.getArticleList();
                            }
                        });
                    }
                }
            });
        };


        //撤回
        self.offlineBtn = function () {
            var that = this;
            $.prompt("<p>发布过的文章撤回不会增加发布次数，确认撤回？</p>", {
                title: "提示",
                buttons: {"取消": false, "撤回": true},
                submit: function (e, v, m, f) {
                    if (v) {
                        $.post("/basic/officialaccount/articleoffline.vpage", {
                            bundleId: that.bundleId,
                            accountId: self.accountValue()
                        }, function (data) {
                            if (data.success) {
                                self.getArticleList();
                            }
                        });
                    }
                }
            });
        };

        //编辑
        self.editBtn = function () {
            var bundleId = this.bundleId || '';
            location.href = '/basic/officialaccount/articleedit.vpage?accountId=' + self.accountValue() + '&bundleId=' + bundleId;
        };

        //图片预览
        self.viewImgBtn = function (that) {
            $.prompt("<img src='" + that.imgUrl + "'/ style='width:100%'>", {
                title: "图片预览",
                buttons: {"关闭": true},
                position: {width: 600}
            });


        };

        var startTime = $('#startTime'), endTime = $('#endTime');

        /*时间控件*/
        startTime.datetimepicker({
            format: 'yyyy-mm-dd',
            endDate: new Date(),
            defaultDate: new Date(),
            minView: 3,
            autoclose: true
        }).on('changeDate', function (ev) {
            self.statDate($(this).val());

        });
        endTime.datetimepicker({
            format: 'yyyy-mm-dd',
            endDate: new Date(),
            defaultDate: new Date(),
            minView: 3,
            autoclose: true
        }).on('changeDate', function (ev) {
            self.endDate($(this).val());
        });

        //查询
        self.searchBtn = function () {
            self.getArticleList();
        }
    };
    ko.applyBindings(new ArticleListModule())
});