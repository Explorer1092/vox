/**
 * @author xinqiang.wang
 * @description "直播课列表"
 * @createDate 2016/11/17
 */
define(['jquery', 'knockout', 'komapping', "weui", 'voxLogs'], function ($, ko, komapping) {

    var LiveLessonModule = function () {
        var self = this;
        self.contentDetail = ko.observableArray([]);

        self.imagesDetail = ko.observableArray([]);

        self.getQuery = function (item) {
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        };
        self.ossImg = function (imgUrl, width) {
            width = width || 360;
            if (imgUrl && imgUrl != "" && imgUrl.indexOf('oss-image.17zuoye.com') > -1) {
                return imgUrl + '@' + width + 'w_1o';
            } else {
                return imgUrl;
            }
        };

        self.showTopBanner = ko.observable(self.getQuery('topb') == 'show');

        self.category = self.getQuery("category");  //长期课: NORMAL_LIVE_COURSE, 公开课: OPEN_LIVE_COURSE

        self.menuList = komapping.fromJS([
            {id: -1, categoryName: '推荐', categoryCode: '', checked: true},
            {id: 1, categoryName: '英语', categoryCode: '英语', checked: false},
            {id: 2, categoryName: '数学', categoryCode: '数学', checked: false},
            {id: 3, categoryName: '语文', categoryCode: '语文', checked: false},
            {id: 4, categoryName: '家庭教育', categoryCode: '家庭教育', checked: false},
            {id: 5, categoryName: '才艺', categoryCode: '才艺', checked: false}
        ]);

        //正式课没有导航
        if (self.category == 'NORMAL_LIVE_COURSE') {
            self.menuList([]);
        }

        self.selectedCategoryCode = ko.observable('');


        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(1);
        self.totalPage = ko.observable(0);

        self.ajaxIsLoading = ko.observable(false);
        self.ajaxLoadFinished = ko.observable(false);

        //设置标签颜色
        self.setTagColor = function (tag) {
            //fix tab只有汉语，所以用汉语比较
            switch (tag) {
                case "作文课":
                case "语文课":
                case "英语课":
                    return 'yellow';
                    break;
                case "才艺":
                case "家庭教育":
                    return 'red';
                    break;
                case "数学课":
                case "奥数课":
                    return "blue";
                    break;
                default:
                    return "red";
            }
        };

        self.topBannerBtn = function () {
            var that = this;
            var url = that.url;
            setTimeout(function () {
                if (window['external'] && window.external['openSecondWebview']) {
                    window.external.openSecondWebview( JSON.stringify({
                        url : url
                    }) );
                } else {
                    location.href = url;
                }
            }, 200);
        };

        //我的课程
        self.myCourseBtn = function () {
            setTimeout(function () {
                var myCourseLink = '/mizar/course/mycourse.vpage';
                if (window['external'] && window.external['openSecondWebview']) {
                    window.external.openSecondWebview( JSON.stringify({
                        url : myCourseLink
                    }) );
                } else {
                    location.href = myCourseLink;
                }
            }, 200);
        };

        //标签选择
        self.categoryBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.menuList(), function (list) {
                list.checked(false);
            });
            that.checked(true);

            self.selectedCategoryCode(that.categoryCode());

            self.init();

            self.getContentList();
        };

        //初始化
        self.init = function () {
            self.pageNum(1);
            self.contentDetail([]);
        };

        //获取列表
        self.getContentList = function () {
            if (self.contentDetail().length == 0) {
                //$.showLoading();
            }
            self.ajaxIsLoading(true);
            $.post('/mizar/course/loadcoursepage.vpage', {
                category: self.category,
                pageNum: self.pageNum(),
                pageSize: self.pageSize(),
                tag: self.selectedCategoryCode()
            }, function (data) {
                if (data.success) {
                    ko.utils.arrayForEach(data.rows, function (rows) {
                        var tags = [];
                        for (var i = 0; i < rows.tags.length; i++) {
                            tags.push({name: rows.tags[i], color: self.setTagColor(rows.tags[i])});
                        }
                        rows.background = self.ossImg(rows.background, 670);
                        rows.speakerAvatar = self.ossImg(rows.speakerAvatar, 50);
                        rows.tags = tags;
                        self.contentDetail.push(rows);
                    });

                    self.totalPage(data.totalPage);
                } else {
                    $.alert(data.info);
                }
                self.ajaxIsLoading(false);
                self.ajaxLoadFinished(true);
            }).fail(function () {
                self.ajaxIsLoading(false);
                $.toast('数据异常');
            });
        };

        self.getContentList();


        //图片广告位
        self.getImageAdp = function () {
            $.post('/be/newinfo.vpage', {p: 220702}, function (data) {
                if (data.success) {
                    ko.utils.arrayForEach(data.data, function (value) {
                        value.img = self.ossImg(data.imgDoMain + 'gridfs/' + value.img);
                    });
                    self.imagesDetail(data.data);
                }
            });
        };
        if (self.category == 'OPEN_LIVE_COURSE') {
            self.getImageAdp();
        }

        //跳转
        self.redirectUrlBtn = function () {
            var that = this;
            setTimeout(function () {
                if (window['external'] && window.external['openSecondWebview']) {
                    window.external.openSecondWebview( JSON.stringify({
                        url : that.redirectUrl
                    }) );
                } else {
                    location.href = that.redirectUrl;
                }
            }, 200);
        };

        $(window).scroll(function () {
            if (($(document).height() - 200 <= $(window).height() + $(window).scrollTop())) {
                if (self.pageNum() < self.totalPage() && !self.ajaxIsLoading()) {
                    self.pageNum(self.pageNum() + 1);
                    self.getContentList();
                }
            }
        });

        YQ.voxLogs({
            database: 'parent',
            module: 'm_SK5wQZLl',
            op: 'o_EpyJSIUu'
        });
    };

    ko.applyBindings(new LiveLessonModule());
});