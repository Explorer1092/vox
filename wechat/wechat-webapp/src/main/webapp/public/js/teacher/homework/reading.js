/**
 * @author xinqiang.wang
 * @description "阅读绘本"
 * @createDate 2016/7/27
 */

define(["$17", "logger", 'jbox'], function ($17, logger, jbox) {
    /*处理图片加载失败*/
    ko.bindingHandlers.img = {
        update: function (element, valueAccessor) {
            var value = ko.utils.unwrapObservable(valueAccessor()),
                src = ko.utils.unwrapObservable(value.src),
                fallback = ko.utils.unwrapObservable(value.fallback),
                $element = $(element);

            if (src) {
                $element.attr("src", src);
            } else {
                $element.attr("src", fallback);
            }
        },
        init: function (element, valueAccessor) {
            var $element = $(element);

            $element.error(function () {
                var value = ko.utils.unwrapObservable(valueAccessor()),
                    fallback = ko.utils.unwrapObservable(value.fallback);

                $element.attr("src", fallback);
            });
        }
    };

    function ReadingModel() {
        var self = this;
        var subject = $17.getQuery("subject");
        self.readingWeeklyDetail = ko.observableArray([]);
        self.readingSynchronousDetail = ko.observableArray([]);
        self.readingAllDetail = ko.observableArray([]);
        self.readingClazzLevel = ko.observable("");

        self.readingSearchByClazzDetail = ko.observableArray([]);
        self.readingSearchByTopicDetail = ko.observableArray([]);
        self.readingSearchBySeriesDetail = ko.observableArray([]);
        self.readingSearchByClazzBox = ko.observable(false);
        self.readingSearchByTopicBox = ko.observable(false);
        self.readingSearchBySeriesBox = ko.observable(false);

        self.readingPageNum = ko.observable(1);
        self.redingTotalPageNum = ko.observable(0);
        self.readingPageSize = 16;
        self.readingSearchName = ko.observable();
        self.readingSeriesId = ko.observable(""); //系列ID
        self.readingTopicId = ko.observable(""); //主题ID
        self.readingRecommendResult = ko.observable();
        self.readingLoading = ko.observable(false);

        self.readingViewDetail = ko.observableArray([]);
        self.readingViewFlashUrl = ko.observable(""); //绘本预览URL

        self.readingTopMenuScrollLock = ko.observable(false);

        self.readingGetScreenHeight = $(window).height();

        self._readingSetBookInfo = function () {
            homeworkConstant._homeworkContent.books.READING = [{
                bookId: homeworkConstant.bookId,
                unitId: homeworkConstant.unitId,
                includePictureBooks: self.readingCard()
            }];
        };

        /*选入*/
        self.addReading = function (readingType) {
            var that = this;
            var pictureBookId = that.pictureBookId();
            if ($.inArray(pictureBookId, self.readingCard()) == -1) {
                that.checked(true);
                self.readingCard.push(pictureBookId);
                var _questionObj = {
                    pictureBookId: pictureBookId
                };
                homeworkConstant._homeworkContent.practices.READING.apps.push(_questionObj);
                self._readingSetBookInfo();
                self.showQuestionsTotalCount();
                self.readingDuration(self.readingDuration() + that.seconds());
            } else {
                $17.jqmHintBox("该题与已选题目重复");
            }

            switch (readingType) {
                case "weekly":
                    self.sendLog({
                        op: "page_select_title_PicBook_recommend_select_click",
                        s1: self.homeworkEnum.READING,
                        s2: "本周推荐",
                        s3: pictureBookId
                    });
                    break;
                case "synchronous":
                    self.sendLog({
                        op: "page_select_title_PicBook_tongbu_select_click",
                        s1: self.homeworkEnum.READING,
                        s2: "同步绘本",
                        s3: pictureBookId
                    });
                    break;

                case "all":
                    self.sendLog({
                        op: "page_select_title_PicBook_all_select_click",
                        s1: self.homeworkEnum.READING,
                        s2: "全部绘本",
                        s3: pictureBookId
                    });
                    break;
            }
        };

        /*移除*/
        self.removeReading = function (readingType) {
            var that = this;
            var pictureBookId = that.pictureBookId();
            that.checked(false);
            var _questionIndex = self.readingCard.indexOf(pictureBookId);
            self.readingCard.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.READING.apps.splice(_questionIndex, 1);
            self._readingSetBookInfo();
            self.showQuestionsTotalCount();
            self.readingDuration(self.readingDuration() - that.seconds());

            switch (readingType) {
                case "weekly":
                    self.sendLog({
                        op: "page_select_title_PicBook_recommend_deselect_click",
                        s1: self.homeworkEnum.READING,
                        s2: "本周推荐",
                        s3: pictureBookId
                    });
                    break;
                case "synchronous":
                    self.sendLog({
                        op: "page_select_title_PicBook_tongbu_deselect_click",
                        s1: self.homeworkEnum.READING,
                        s2: "同步绘本",
                        s3: pictureBookId
                    });
                    break;

                case "all":
                    self.sendLog({
                        op: "page_select_title_PicBook_all_deselect_click",
                        s1: self.homeworkEnum.READING,
                        s2: "全部绘本",
                        s3: pictureBookId
                    });
                    break;
            }
        };

        /*根据选择条件查询reading*/
        self.readingSearch = function () {
            var postData = {
                bookId: homeworkConstant.bookId,
                unitId: homeworkConstant.unitId,
                readingName: self.readingSearchName(),
                clazzLevels: self.readingClazzLevel() == -1 ? '' : self.readingClazzLevel(),
                topicIds: self.readingTopicId() == -1 ? '' : self.readingTopicId(),
                seriesIds: self.readingSeriesId() == -1 ? '' : self.readingSeriesId(),
                pageNum: self.readingPageNum(),
                pageSize: self.readingPageSize,
                subject: subject

            };
            self.readingLoading(true);
            $.post('/teacher/homework/reading/search.vpage', postData, function (data) {
                if (data.success) {
                    for (var i = 0; i < data.readings.length; i++) {
                        data.readings[i].checked = false;
                    }
                    var readings = ko.mapping.fromJS(data.readings)();
                    ko.utils.arrayForEach(readings, function (exam) {
                        self.readingAllDetail.push(ko.mapping.fromJS(exam));
                    });
                    self.redingTotalPageNum(data.pageCount);
                    $("#reading_list_null_info_box").show().css({'height': $(window).height() - 490});
                }
                self.readingLoading(false);
                if (self.readingTopMenuScrollLock()) {
                    self.readingSearchMenuShow();
                }
                self.readingTopMenuScrollLock(true);

            }).fail(function () {
            });
        };

        /*reading预览*/
        self.readingViewBtn = function (readingType) {
            var that = this;
            var pid = that.pictureBookId();
            self.readingViewDetail([]);
            var _readingViewDetail = '';
            switch (readingType) {
                case "weekly":
                    _readingViewDetail = self.readingWeeklyDetail();
                    self.sendLog({
                        op: "page_select_title_PicBook_thumbnail_click",
                        s1: self.homeworkEnum.READING,
                        s2: "本周推荐",
                        s3: pid
                    });
                    break;
                case "synchronous":
                    _readingViewDetail = self.readingSynchronousDetail();

                    self.sendLog({
                        op: "page_select_title_PicBook_thumbnail_click",
                        s1: self.homeworkEnum.READING,
                        s2: "同步绘本",
                        s3: pid
                    });
                    break;
                case "all":

                    self.sendLog({
                        op: "page_select_title_PicBook_thumbnail_click",
                        s1: self.homeworkEnum.READING,
                        s2: "全部绘本",
                        s3: pid
                    });
                    _readingViewDetail = self.readingAllDetail();
                    break;
            }
            ko.utils.arrayForEach(_readingViewDetail, function (week) {
                if (week.pictureBookId() == pid) {
                    self.readingViewDetail.push(week);
                }
            });

            //self.koTemplateName("readingViewTemplate");

            var domain = homeworkConstant.env == "test" ? "//www.test.17zuoye.net" : (homeworkConstant.env == "staging" ? "//www.staging.17zuoye.net":"//www.17zuoye.com");
            var url = domain + "/resources/apps/hwh5/homework/V2_5_0/drawBook/index.html?__p__=";
            url += encodeURIComponent(JSON.stringify({
                domain          : domain,
                img_domain      : homeworkConstant.imgDomain,
                env             : homeworkConstant.env,
                subject         : subject,
                pictureBookIds  : pid,
                isMobileReview  : true
            }));

            self.readingViewFlashUrl(url);
            self.koTemplateName("readingViewTemplate_Flash");
        };

        self.readingSearchLock = function () {
            return self.readingSearchByClazzBox() || self.readingSearchByTopicBox() || self.readingSearchBySeriesBox()
        };

        /*搜索按钮*/
        self.readingSearchBtn = function () {
            self.readingPageNum(1);
            self.readingAllDetail([]);
            self.readingSearch();
            self._readingCloseAllSearchBox();

            self.sendLog({
                op: "page_select_title_PicBook_all_search_click",
                s1: self.homeworkEnum.READING,
                s2: self.readingSearchName()
            });
        };

        self.readingSearchMenuShow = function () {
            var readingAll_box = $('#readingAll_box');
            var topBox = $('#topBox').height();
            var pdf = readingAll_box.find('._pft');
            if (readingAll_box.length == 0) {
                return;
            }
            var top = readingAll_box.offset().top;
            var weekHeight = $("#readingWeeklyDetailBox").height() || 0;
            var synchronousHeight = $("#readingSynchronousDetailBox").height() || 0;
            $('#scrollListBox').scrollTop(Math.abs(top) + weekHeight + synchronousHeight);

        };

        /*年级*/
        self.readingSearchByClazzClick = function () {
            self.readingSearchByClazzBox(!self.readingSearchByClazzBox());
            self.readingSearchByTopicBox(false);
            self.readingSearchBySeriesBox(false);
            self.readingSearchMenuShow();

            self.sendLog({
                op: "page_select_title_PicBook_all_more_click",
                s1: self.homeworkEnum.READING,
                s2: "年级"
            });
        };

        /*主题*/
        self.readingSearchByTopicClick = function () {
            self.readingSearchMenuShow();
            self.readingSearchByClazzBox(false);
            self.readingSearchByTopicBox(!self.readingSearchByTopicBox());
            self.readingSearchBySeriesBox(false);
            self.sendLog({
                op: "page_select_title_PicBook_all_more_click",
                s1: self.homeworkEnum.READING,
                s2: "主题"
            });
        };

        /*系列*/
        self.readingSearchBySeriesClick = function () {
            self.readingSearchMenuShow();
            self.readingSearchByClazzBox(false);
            self.readingSearchByTopicBox(false);
            self.readingSearchBySeriesBox(!self.readingSearchBySeriesBox());

            self.sendLog({
                op: "page_select_title_PicBook_all_more_click",
                s1: self.homeworkEnum.READING,
                s2: "系列"
            });
        };

        self._readingCloseAllSearchBox = function () {
            self.readingSearchMenuShow();
            self.readingSearchByClazzBox(false);
            self.readingSearchByTopicBox(false);
            self.readingSearchBySeriesBox(false);
        };

        self.readingCloseAllSearchBox = function () {
            self._readingCloseAllSearchBox();
        };

        self.readingSearchSelectClick = function (searchType) {
            var that = this;
            self.readingAllDetail([]);

            switch (searchType) {
                case "clazz":
                    ko.utils.arrayForEach(self.readingSearchByClazzDetail(), function (clazz) {
                        clazz.checked(false);
                    });
                    self.readingClazzLevel(that.clazzLevel());

                    self.sendLog({
                        op: "page_select_title_PicBook_all_gradeSift_click",
                        s1: self.homeworkEnum.READING,
                        s2: that.clazzLevel()
                    });

                    break;
                case "topic":
                    ko.utils.arrayForEach(self.readingSearchByTopicDetail(), function (clazz) {
                        clazz.checked(false);
                    });
                    self.readingTopicId(that.topicId());

                    self.sendLog({
                        op: "page_select_title_PicBook_all_themeSift_click",
                        s1: self.homeworkEnum.READING,
                        s2: that.topicId()
                    });

                    break;
                case "series":
                    ko.utils.arrayForEach(self.readingSearchBySeriesDetail(), function (clazz) {
                        clazz.checked(false);
                    });
                    self.readingSeriesId(that.seriesId());

                    self.sendLog({
                        op: "page_select_title_PicBook_all_seriesSift_click",
                        s1: self.homeworkEnum.READING,
                        s2: that.seriesId()
                    });
                    break;
            }

            that.checked(true);

            self.readingSearch();
            self.readingCloseAllSearchBox();
        };
    }

    return ReadingModel;
});