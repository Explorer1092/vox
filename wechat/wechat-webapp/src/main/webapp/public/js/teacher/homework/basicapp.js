/**
 * @author xinqiang.wang
 * @description "基础练习"
 * @createDate 2016/7/25
 */

define(["$17", "logger", "jbox", 'swiper3'], function ($17, logger, jbox, swiper) {
    function BasicAppModel() {
        var self = this;
        self.basicAppCard = ko.observableArray([]);

        /*选入基础练习*/
        self.addCategory = function (parent) {
            var that = this;
            if (that.practices) {
                var practices = that.practices();
                var _apps = homeworkConstant._homeworkContent.practices.BASIC_APP.apps;
                if (practices && $.isArray(practices) && practices[0].questions) {
                    var _questions = ko.mapping.toJS(practices[0].questions());
                    var _lessonId = parent.lessonId();
                    var _categoryId = that.categoryId();
                    var lc = _lessonId + _categoryId;
                    if ($.inArray(lc, self.basicAppCard()) == -1) {
                        _apps.push({
                            practiceCategory: that.categoryName(),
                            categoryId: _categoryId,
                            practiceId: practices[0].practiceId(),
                            practiceName: practices[0].practiceName(),
                            lessonId: _lessonId,
                            questions: _questions
                        });
                        self._basicAppUnitIntoBooks("add", _lessonId, _categoryId);
                        that.checked(true);
                        self.basicAppCard.push(lc);
                    } else {
                        logger.log({
                            app: "teacher",
                            module: 'englishHomework',
                            op: 'basicAppRedo',
                            s0: lc,
                            s1: self.basicAppCard().join(',')
                        });
                    }
                } else {
                    logger.log({
                        app: "teacher",
                        module: 'englishHomework',
                        op: '应用未配题'
                    });
                }
            } else {
                logger.log({
                    app: "teacher",
                    module: 'englishHomework',
                    op: '应用不存在或应用未配题'
                });
            }

            self.sendLog({
                op: "page_select_title_BasicPractice_select_click",
                s1: self.homeworkEnum.BASIC_APP,
                s2: that.categoryId()
            });
        };

        /*移除基础练习*/
        self.removeCategory = function (parent) {
            var that = this;
            var _lessonId = parent.lessonId();
            var _categoryId = that.categoryId();
            var lc = _lessonId + _categoryId;
            var _apps = homeworkConstant._homeworkContent.practices.BASIC_APP.apps;
            var zIndex = -1;
            var sec = 0;
            for (var k = 0, kLen = _apps.length; k < kLen; k++) {
                if (_apps[k].categoryId === _categoryId && _lessonId === _apps[k].lessonId) {
                    zIndex = k;
                    var questions = _apps[k].questions;
                    for (var t = 0, tLen = questions.length; t < tLen; t++) {
                        sec += (+questions[t].seconds || 0);
                    }
                    break;
                }
            }
            if (zIndex != -1) {
                _apps.splice(zIndex, 1);
                self._basicAppUnitIntoBooks("remove", _lessonId, _categoryId)
            }
            that.checked(false);
            var _lc = self.basicAppCard.indexOf(lc);
            self.basicAppCard.splice(_lc, 1);

            self.sendLog({
                op: "page_select_title_BasicPractice_deselect_click",
                s1: self.homeworkEnum.BASIC_APP,
                s2: _categoryId
            });
        };

        /*句型展示*/
        self.basicAppCovertSentences = function (sentences) {
            if (!$.isArray(sentences)) {
                return "";
            }
            return sentences.join(" / ");
        };

        /*设置保存数据中的books*/
        self._basicAppUnitIntoBooks = function (operate, lessonId, categoryId) {
            var unitId = homeworkConstant.unitId || null;
            if ($17.isBlank(lessonId)) {
                return false;
            }
            if ($17.isBlank(categoryId)) {
                return false;
            }
            var _bookExams = homeworkConstant._homeworkContent.books.BASIC_APP;
            var unitIndex = -1;
            for (var k = 0, kLen = _bookExams.length; k < kLen; k++) {
                if (_bookExams[k].unitId == unitId) {
                    unitIndex = k;
                    break;
                }
            }
            if (unitIndex == -1 && operate == 'add') {
                var _bookObj = {
                    bookId: homeworkConstant.bookId || null,
                    unitId: homeworkConstant.unitId || null,
                    categories: [lessonId + ":" + categoryId]
                };
                homeworkConstant._homeworkContent.books.BASIC_APP.push(_bookObj);
            } else if (unitIndex != -1) {
                var lc = lessonId + ":" + categoryId;
                var categoryIndex = _bookExams[unitIndex].categories.indexOf(lc);
                var _categories = _bookExams[unitIndex].categories;
                if (operate == 'remove' && categoryIndex != -1) {
                    _categories.splice(categoryIndex, 1);
                    if (_categories.length == 0) {
                        _bookExams.splice(unitIndex, 1);
                    }
                } else if (operate == 'add') {
                    if (categoryIndex != -1) {
                        return false;
                    }
                    if ($.inArray(lc, _bookExams[unitIndex].categories) == -1) {
                        _bookExams[unitIndex].categories.push(lc);
                    }
                }
            }
            return true;
        };

        /*预览*/
        self.viewImgBtn = function () {
            var that = this;
            var imgHtml = '<div class="swiper-container" ><div class="swiper-wrapper">';
            var imgUrlPrefix = homeworkConstant.tabIconPrefixUrl;
            var _categoriesIds = [];
            ko.utils.arrayForEach(that.categories(), function (_categories) {
                _categoriesIds.push(_categories.categoryId());
                imgHtml += '<div class="swiper-slide">' + _categories.categoryName() + '' +
                    '<img src="' + imgUrlPrefix + _categories.categoryIcon() + '.png?1.0.1"></div>';
            });
            imgHtml += '</div><div class="swiper-pagination"></div></div>';
            var confirm = new jBox('Modal', {
                width: 630,
                attach: "",
                title: '预览',
                content: imgHtml,
                delayOpen: 500,
                onOpen: function () {
                    setTimeout(function () {
                        var swiper = new Swiper('.swiper-container', {
                            nextButton: '.swiper-button-next',
                            prevButton: '.swiper-button-prev',
                            pagination: '.swiper-pagination',
                            paginationType: 'fraction'
                        });
                    }, 50);
                }
            });
            confirm.open();
            if (_categoriesIds.length == 0) {
                return false;
            }
            self.sendLog({
                op: "page_select_title_BasicPractice_preview_click",
                s1: self.homeworkEnum.BASIC_APP,
                s2: _categoriesIds.join(',')
            });
        };
    }

    return BasicAppModel;
});