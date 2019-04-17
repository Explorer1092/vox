/**
 * @author xinqiang.wang
 * @description "电商导购"
 * @createDate 2016/9/21
 */
define(['jquery', 'knockout', 'komapping', 'knockoutscroll', "weui", 'voxLogs'], function ($, ko, komapping) {

    var EcsgModule = function () {
        var self = this;
        self.menuList = ko.observableArray([]);
        self.contentDetail = ko.observableArray([]);
        self.selectedCategoryCode = ko.observable('');
        self.selectedCategoryName = ko.observable('');
        self.orderField = ko.observable('recommend'); //排序维度 [推荐排序:recommend,销量:saleCount, 时间:time]
        self.orderType = ko.observable('desc'); //排序方式 desc asc
        self.categoryList = _categoryList;
        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(0);
        self.totalPage = ko.observable(0);
        self.childMenuList = komapping.fromJS([
            {name: '推荐排序', value: "recommend", checked: true, rank: 0},//rank 0:desc  1:asc
            {name: '按销量', value: "saleCount", checked: false, rank: 1},
            {name: '按时间', value: "time", checked: false, rank: 0}
        ]);

        self.getChildMenuSelectedVal = function () {
            ko.utils.arrayForEach(self.childMenuList(), function (list,index) {
                if (list.checked()) {
                    self.orderField(list.value());
                    self.orderType(list.rank() == 0 ? 'desc' : 'asc');
                    //推荐排序 默认为  desc
                    if(index == 0){
                        self.orderType('desc');
                    }
                }
            });
        };

        self.init = function () {
            self.pageNum(0);
            self.contentDetail([]);
        };

        self.showOtherListBox = ko.observable(false);

        self.countDown = ko.observable();

        for (var i = 0; i < self.categoryList.length; i++) {
            self.categoryList[i].checked = false;
            self.categoryList[i].show = i < 4; //默认显示
        }
        self.categoryList.unshift({id: -1, categoryName: '综合', categoryCode: '', checked: true, show: true});

        self.menuList(komapping.fromJS(self.categoryList)());

        /*动态显示导航位置*/
        self.graceShowTopMenu = function () {
            var cmb = $('#categoryListMenuBox');
            var liWidth = cmb.find('span.active').innerWidth();
            var index = cmb.find('span.active').data('index');
            cmb.scrollLeft(liWidth * index - 80);
        };

        /*选择类别*/
        self.categoryBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.menuList(), function (list) {
                list.checked(false);
            });
            that.checked(true);
            self.selectedCategoryCode(that.categoryCode());
            self.selectedCategoryName(that.categoryName());
            self.graceShowTopMenu();
            self.showOtherListBox(false);

            self.init();
            self.getContentDetail();

            self.sendLog({
                op: "o_JDF4jrYZ",
                s0: that.categoryName()
            });
        };

        /*二级导航选择*/
        self.childMenuBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.childMenuList(), function (list) {
                list.checked(false);
            });
            that.checked(true);
            that.rank(!that.rank());

            self.init();
            self.getContentDetail();

            self.sendLog({
                op: "o_novDksep",
                s0: self.selectedCategoryName(),
                s1: that.name()
            });
        };

        /*‘其他’按钮点击*/
        self.otherBtn = function () {
            self.showOtherListBox(!self.showOtherListBox());
            self.sendLog({
                op: "o_JDF4jrYZ",
                s0: '其他'
            });
        };

        /*去抢购*/
        self.goToBuyBtn = function () {
            var that = this;
            setTimeout(function () {
                location.href = that.url;
            }, 200);

            self.sendLog({
                op: "o_TJhS6YhO",
                s0: that.id
            });
        };

        /*获取数据*/
        self.getContentDetail = function () {
            self.getChildMenuSelectedVal();
            if (self.contentDetail().length == 0) {
                $.showLoading();
            }
            $.post('/groupon/getgoods.vpage', {
                category: self.selectedCategoryCode(),
                orderDimension: self.orderField(),//排序维度
                orderType: self.orderType(),//排序方式 asc desc
                pageNum: self.pageNum(),
                pageSize: self.pageSize()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    ko.utils.arrayForEach(data.goodsList, function (rows) {
                        if (rows.image && rows.image.indexOf('oss-image.17zuoye.com') !== -1) {
                            rows.image = rows.image + "@310h_1o";
                        }
                        self.contentDetail.push(rows);
                    });
                    self.totalPage(data.totalPage);
                } else {
                    $.alert(data.info);
                }

            }).fail(function () {
                $.hideLoading();
                $.toast('数据异常', 'weui_toast_forbidden');
            });

        };

        self.getContentDetail();

        /*滚屏加载数据*/
        self.scrolled = function () {
            if (self.pageNum() < self.totalPage()) {
                self.pageNum(self.pageNum() + 1);
                self.getContentDetail();
            }
        };

        /*打点*/
        self.sendLog = function () {
            var logMap = {
                database: 'parent',
                module: 'm_sMNiwxrS'
            };
            $.extend(logMap, arguments[0]);
            YQ.voxLogs(logMap);
        };

        self.sendLog({
            op: "o_tzkfgDei"
        });
    };


    /*number to HHMMSS*/
    Number.prototype.toHHMMSS = function () {
        var seconds = Math.floor(this), days = Math.floor(seconds / 3600 / 24), hours = Math.floor(seconds / 3600);
        seconds -= hours * 3600;
        var minutes = Math.floor(seconds / 60);
        seconds -= minutes * 60;
        if (hours < 10) {
            hours = "0" + hours;
        }
        if (minutes < 10) {
            minutes = "0" + minutes;
        }
        if (seconds < 10) {
            seconds = "0" + seconds;
        }

        var r = hours + ':' + minutes + ':' + seconds;
        if (days > 0) {
            r = days + '天';
            if (hours > 0) {
                r += (hours - days * 24) + "时"
            }
        }

        return r
    };
    ko.bindingHandlers.timer = {
        update: function (element, valueAccessor) {
            setTimeout(function () {
                var sec = $(element).text() * 1;
                $(element).text('剩' + sec.toHHMMSS()).show();
                var timer = setInterval(function () {
                    --sec;
                    $(element).text('剩' + sec.toHHMMSS());
                    if (sec == 0) {
                        clearInterval(timer);
                    }
                }, 1000);

            }, 0);

        }
    };

    ko.applyBindings(new EcsgModule());
});

