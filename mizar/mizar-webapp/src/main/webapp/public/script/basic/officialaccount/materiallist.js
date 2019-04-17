/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/11/9
 */

define(["jquery", "knockout", 'clipboard',"prompt", "paginator"], function ($, ko,Clipboard) {
    var MaterialListModule = function () {
        var self = this;

        self.pageNum = ko.observable(1);
        self.pageSize = ko.observable(10);
        self.contentDetail = ko.observableArray([]);
        self.showDetail = ko.observableArray([]);

        self.titleVal = ko.observable('');

        self.getContent = function () {
            $.post('/basic/officialaccount/materiallist.vpage', {
                title: self.titleVal()
            }, function (data) {
                if (data.success) {
                    self.contentDetail(data.jxtNewsList);
                    self.getOnePageArticleList(1);

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

        self.getContent();

        self.getOnePageArticleList = function (page) {
            self.showDetail([]);
            var group = self.contentDetail.slice((page - 1) * self.pageSize(), page * self.pageSize());
            ko.utils.arrayForEach(group, function (at) {

                self.showDetail.push(at);
            });
        };

        //复制
        self.clipboard = new Clipboard('.copyBtn');
        self.clipboard.on('success', function (e) {
            alert("复制成功");
        });
        self.clipboard.on('error', function (e) {
            alert("复制失败，请手动复制");
            window.prompt("Copy to clipboard: Ctrl+C, Enter",e.text)
        });

        self.editBtn = function () {
            location.href = '/basic/officialaccount/materialedit.vpage?id='+this.id+'&submitted='+this.submitted;
        };

        self.searchBtn = function () {
            self.getContent();
        };

        //投稿
        self.submittedBtn = function () {
            var that = this;
            $.prompt("是否需要投稿该文章", {
                title: "提示",
                buttons: { "取消": false, "提交": true },
                submit: function(e,v,m,f){
                    if(v){
                        $.post('/basic/officialaccount/submitmaterial.vpage', {
                            id: that.id
                        }, function (data) {
                            if(data.success){
                                self.getContent();
                            }
                        });
                    }

                }
            });
        };

        //预览
        self.viewBtn = function (data) {
            //data.generateUrl = "https://www.test.17zuoye.net/view/mobile/parent/information/detail?rel=list_0&newsId=587875a8e92b1b082f935663&ut=1487069500583&content_type=IMG_AND_TEXT&style_type=OFFICIAL_ACCOUNT_SUBMIT&accountsKey=81";

            $.prompt('<iframe src="'+ data.generateUrl +'" style="margin: -40px -20px -20px; position: relative; z-index: 2;" allowtransparency="true" frameborder="0" width="320" height="480" scrolling="auto"></iframe>', {
                position: {width: 320},
                title: "手机预览",
                buttons: {}
            });
        };

    };
    ko.applyBindings(new MaterialListModule())
});