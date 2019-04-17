/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/10/18
 */

define(['jquery', 'knockout', 'komapping', "weui", 'voxLogs','flexSlider'], function ($, ko, komapping) {

    //顶部导航广告。
    $("#bannerContentBox").flexslider({
        animation: "fade",
        directionNav: false,
        pauseOnAction: true,
        after: function (slider) {
            if (!slider.playing) {
                slider.play();
            }
        },
        slideshowSpeed: 4000,
        animationSpeed: 400
    });

    var CommodityModule = function () {
        var self = this;
        self.menuList = ko.observableArray([]);
        self.contentDetail = ko.observableArray([]);
        self.selectedCategoryCode = ko.observable('');
        self.selectedCategoryName = ko.observable('');
        self.ajaxLoadFinished = ko.observable(false);
        //hot, new, postFree
        self.specialTag = {
            hot: {
                name: 'hot',
                color: 'tag-hot'
            },
            new: {
                name: 'new',
                color: 'tag-new'
            },
            postFree: {
                name: '包邮',
                color: 'tag-send'
            },
            promotions: {
                name: '促销',
                color: 'tag-sale'
            }
        };

        self.goodsSource = {
            tian_mao:{name:'天猫'},
            tao_bao:{name:'淘宝'},
            jing_dong:{name:'京东'},
            dang_dang:{name:'当当'}
        };

        self.categoryList = _categoryList;
        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(0);
        self.totalPage = ko.observable(0);

        self.ajaxIsLoading = ko.observable(false);

        for (var i = 0; i < self.categoryList.length; i++) {
            self.categoryList[i].checked = false;
            self.categoryList[i].show = i < 4; //默认显示
        }
        self.categoryList.unshift({id: -1, categoryName: '推荐', categoryCode: '', checked: true, show: true});

        self.menuList(komapping.fromJS(self.categoryList)());

        /*动态显示导航位置*/
        self.graceShowTopMenu = function () {
            var cmb = $('#categoryListMenuBox');
            var liWidth = cmb.find('li.active').innerWidth();
            var index = cmb.find('li.active').data('index');
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

            self.init();
            self.getContentDetail();

            self.sendLog({
                op: "o_cLaamtyz",
                s0: that.categoryName()
            });
        };

        self.init = function () {
            self.pageNum(0);
            self.contentDetail([]);
        };

        self.getContentDetail = function () {
            if (self.contentDetail().length == 0) {
                $.showLoading();
            }
            self.ajaxIsLoading(true);
            $.post('/groupon/getgoods.vpage', {
                category: self.selectedCategoryCode(),
                orderDimension: 'recommend',//排序维度
                orderType: 'desc',//排序方式 asc desc
                pageNum: self.pageNum(),
                pageSize: self.pageSize()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    ko.utils.arrayForEach(data.goodsList, function (rows) {
                        if (rows.image && rows.image.indexOf('oss-image.17zuoye.com') !== -1) {
                            rows.image = rows.image + "@310h_1o";
                        }
                        if(rows.specialTag){
                            rows.specialTag = rows.specialTag.split(',');
                        }else{
                            rows.specialTag = [];
                        }
                        self.contentDetail.push(rows);
                    });
                    self.totalPage(data.totalPage);
                } else {
                    $.alert(data.info);
                }

                self.ajaxIsLoading(false);
                self.ajaxLoadFinished(true);
            }).fail(function () {
                $.hideLoading();
                self.ajaxIsLoading(false);
                $.toast('数据异常', 'weui_toast_forbidden');
            });

        };

        self.getContentDetail();

        /*滚屏加载数据*/
        /*self.scrolled = function () {
            if (self.pageNum() < self.totalPage()) {
                self.pageNum(self.pageNum() + 1);
                self.getContentDetail();
            }
        };*/

        $(window).scroll(function() {
            if(($(document).height() - 200 <= $(window).height() + $(window).scrollTop())) {
                if (self.pageNum() < self.totalPage() && !self.ajaxIsLoading()) {
                    self.pageNum(self.pageNum() + 1);
                    self.getContentDetail();
                }
            }
        });

        self.gotoBookDetail = function () {
            var id = this.id;
            location.href = '/groupon/goodsdetail.vpage?goodsId='+id;
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
            op: "o_q9pxhr3G"
        });

    };

    ko.applyBindings(new CommodityModule());

});