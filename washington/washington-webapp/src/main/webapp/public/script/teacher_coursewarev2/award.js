/**
 * @author: pengmin.chen
 * @description: "课件大赛-获奖结果公布页"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'jqPaginator', 'voxLogs'], function ($, ko, YQ) {
    var awardModal = function () {
        var self = this,
            awardSwipeObj = {
                awardSwiper2: null,
                awardSwiper3: null,
                awardSwiper7: null
            }, // 索引值按照奖项的顺序排列
            subjectList = ['语文', '数学', '英语'];

        doTrack('o_VEzJLCzhio');
        $.extend(self, {
            subjectList: ko.observableArray(subjectList), // 学科列表
            informationAwardInfo: ko.observable({}), // 最具信息化精神作品
            innovationAwardInfo: ko.observableArray([]), // 最具创新智慧设计作品
            resourceAwardInfo: ko.observableArray([]), // 最具资源整合能力作品
            excellentYearAwardInfo: ko.observableArray([]), // 优秀教学作品排行榜-年度
            excellentMonthAwardInfo: ko.observable({}), // 优秀教学作品排行榜-月度
            popularYearAwardInfo: ko.observableArray([]), // 最具人气作品排行榜-年度
            popularWeekAwardInfo: ko.observable({}), // 最具人气作品排行榜-月度
            professorList: ko.observableArray(professorList), // 评委列表

            choiceSubjectIndex: ko.observable(0), // 选择的学科索引
            choiceInnovationCourseIndex: ko.observable(-1), // 选择的创新智慧设计作品索引（初始都不选择，设为-1）
            choiceResourceCourseIndex: ko.observable(-1), // 选择的资源整合能力作品索引（初始都不选择，设为-1）
            choiceInnovationCourse: ko.observable({}), // 选择的创新智慧设计作品
            choiceResourceCourse: ko.observable({}), // 选择的资源整合能力作品
            choiceExcellentYearPageIndex: ko.observable(0), // 选择的优秀教学作品 年度 页码
            choiceExcellentTypeIndex: ko.observable(0), // 选择的优秀教学作品排行榜类型（年度/月度）
            choiceExcellentMonthPeriodIndex: ko.observable(0), // 选择的优秀教学作品排行榜期数（0~1）
            choicePopularTypeIndex: ko.observable(0), // 选择的最具人气作品排行榜类型（年度/每周）
            choicePopularWeekPeriodIndex: ko.observable(0), // 选择的最具人气作品排行榜期数（0~6）

            // 切换学科
            choiceSubject: function(index) {
                doTrack('o_aqzNzbSQic', self.subjectList()[index]);
                self.choiceSubjectIndex(index);
                self.informationAwardInfo(awardCourses[index].worksInformaSpirit); // 最具信息化精神作品
                self.innovationAwardInfo(awardCourses[index].worksInnovatWisdom); // 最具创新智慧设计作品
                self.resourceAwardInfo(awardCourses[index].worksResourIntegra); // 最具资源整合能力作品
                self.excellentYearAwardInfo(awardCourses[index].worksExcellentYear); // 优秀教学作品排行榜-年度
                self.excellentMonthAwardInfo(awardCourses[index].worksExcellentMonth); // 优秀教学作品排行榜-月度
                self.popularYearAwardInfo(awardCourses[index].worksPopularYear); // 优秀教学作品排行榜-年度
                self.popularWeekAwardInfo(awardCourses[index].worksPopularWeek); // 最具人气作品排行榜-月度
                self.choiceExcellentYearPageIndex(0); // 优秀教学作品 年度 页码 归1
                self.choiceExcellentTypeIndex(0); // 优秀教学作品排行榜类型变成默认年度
                self.choiceExcellentMonthPeriodIndex(0); // 选择的优秀教学作品排行榜期数归0
                self.choicePopularTypeIndex(0); // 最具人气作品排行榜类型变成默认年度
                self.choicePopularWeekPeriodIndex(0); // 最具人气作品排行榜期数归0
                self.choiceInnovationCourse(self.innovationAwardInfo()[0]); // 选择的创新智慧设计作品-初始第一个
                self.choiceResourceCourse(self.resourceAwardInfo()[0]); // 选择的资源整合能力作品-初始第一个
                setTimeout(function () {
                    initAwardSwiper(self.innovationAwardInfo().length, self.resourceAwardInfo().length); // swiper02、swiper03对应的数量
                    initJQPagination(self.excellentYearAwardInfo().length);
                }, 0);
            },
            // 跳转到课件详情页
            toDetailPage: function(data) {
                doTrack('o_wjs6mcNdaY', self.subjectList()[self.choiceSubjectIndex()], data.courseId);
                window.open('/courseware/contest/detail.vpage?courseId=' + data.courseId);
            },
            // 查看创新智慧设计作品 评价
            seeInnovationReview: function (index, data, event) {
                doTrack('o_8rGLp3d8QJ', self.subjectList()[self.choiceSubjectIndex()], data.courseId);
                event.stopPropagation();
                if (self.choiceInnovationCourseIndex() === index) {
                    self.choiceInnovationCourseIndex(-1);
                    return;
                }
                self.choiceInnovationCourseIndex(index);
                self.choiceInnovationCourse(data);
            },
            // 查看资源整合能力作品 评价
            seeResourceReview: function (index, data, event) {
                doTrack('o_8rGLp3d8QJ', self.subjectList()[self.choiceSubjectIndex()], data.courseId);
                event.stopPropagation();
                if (self.choiceResourceCourseIndex() === index) {
                    self.choiceResourceCourseIndex(-1);
                    return;
                }
                self.choiceResourceCourseIndex(index);
                self.choiceResourceCourse(data);
            },
            // 选择的优秀教学作品类型（年度/每周）
            choiceExcellentType: function(index) {
                self.choiceExcellentTypeIndex(index);
            },
            // 选择的优秀教学作品-月度期数
            choiceExcellentWeekPeriod: function(index) {
                self.choiceExcellentMonthPeriodIndex(index);
            },
            // 选择的最具人气作品类型（年度/每周）
            choicePopularType: function(index) {
                self.choicePopularTypeIndex(index);
            },
            // 选择的最具人气作品-每周期数
            choicePopularWeekPeriod: function(index) {
                self.choicePopularWeekPeriodIndex(index);
            },
            // 点击swiper左箭头（swiper2.0 不自带左右箭头）
            clickSwipePrev: function (index) {
                awardSwipeObj['awardSwiper' + index].swipePrev();
            },
            // 点击swiper右箭头
            clickSwipeNext: function (index) {
                awardSwipeObj['awardSwiper' + index].swipeNext();
            }
        });
        self.choiceSubject(0);

        // 初始化课件swiper
        function initAwardSwiper(len02, len03) {
            if (awardSwipeObj['awardSwiper2']) awardSwipeObj['awardSwiper2'].destroy(true);
            if (awardSwipeObj['awardSwiper3']) awardSwipeObj['awardSwiper3'].destroy(true);
            awardSwipeObj['awardSwiper2'] = new Swiper('.innovateSwiper02', {
                // loop: true,
                slidesPerView: 3, // 同时显示的数量，auto不兼容loop模式
                loopedSlides: len02, // loop的个数
                slidesPerGroup: 1, // slides的数量多少为一组
                noSwiping: true // 无法滑动
            });
            awardSwipeObj['awardSwiper3'] = new Swiper('.innovateSwiper03', {
                // loop: true,
                slidesPerView: 3,
                loopedSlides: len03,
                slidesPerGroup: 1,
                autoplayDisableOnInteraction: false,
                noSwiping: true
            });
        }

        // 初始评委swiper
        function initProfessorSwiper(len07) {
            awardSwipeObj['awardSwiper7'] = new Swiper('.innovateSwiper07', {
                loop: true,
                autoplay: 3000,
                slidesPerView: 3,
                loopedSlides: len07,
                slidesPerGroup: 1,
                autoplayDisableOnInteraction: false,
                noSwiping: true
            });
        }

        // 初始化分页(totalCount: 总数)
        function initJQPagination (totalCount) {
            $('#JS-pagination').jqPaginator({
                totalPages: Math.ceil(totalCount / 6) , // 分页的总页数，6个为1页
                visiblePages: 5, // 同时展示的页码数
                currentPage: 1, // 当前的页面
                first: '<li class="first"><<</li>',
                prev: '<li class="prev"><</li>',
                page: '<li class="page">{{page}}</li>',
                next: '<li class="next">></li>',
                last: '<li class="last">>></li>',
                onPageChange: function (num, type) {
                    self.choiceExcellentYearPageIndex(num - 1);
                }
            });
        }


        // 绑定全局事件
        var bindGlobalEvent = function () {
            $('#gotoTop').fadeOut(0).css('right', ($(window).width() - 1000) / 2 + 24);
            $(window).on('scroll', function() {
                if ($(window).scrollTop() >= 500) { // 滚动了500之后显示
                    $('#gotoTop').fadeIn(300);
                } else {
                    $('#gotoTop').fadeOut(300);
                }
            });

            $('#gotoTop').on('click', function(){ // 点击置顶
                $('html').animate({
                    scrollTop: '0px'
                }, 300);
            });
        };

        // 简易通用弹窗
        function alertTip(content, callback) {
            var commonPopupHtml = "<div class=\"coursePopup commonAlert\">" +
                "<div class=\"popupInner popupInner-common\">" +
                "<div class=\"closeBtn commonAlertClose\"></div>" +
                "<div class=\"textBox\">" +
                "<p class=\"shortTxt\">" + content + "</p>" +
                "</div>" +
                "<div class=\"otherContent\">" +
                "<a class=\"surebtn commonSureBtn\" href=\"javascript:void(0)\">确 定</a>" +
                "</div>" +
                "</div>" +
                "</div>";
            // 不存在则插入dom
            if (!$('.commonAlert').length) {
                $('body').append(commonPopupHtml);
            }
            // 监听按钮点击
            $(document).one('click', '.commonAlertClose',function(){ // 关闭
                $('.commonAlert').remove();
            }).one('click', '.commonSureBtn', function() { // 点击按钮
                if (callback) {
                    callback();
                } else {
                    $('.commonAlert').remove();
                }
            });
        }

        // 打点方法
        function doTrack () {
            var track_obj = {
                database: 'web_teacher_logs',
                module: 'm_f1Bw7hDbxx'
            };
            for (var i = 0; i < arguments.length; i++) {
                if (i === 0) {
                    track_obj['op'] = arguments[i];
                } else {
                    track_obj['s' + (i - 1)] = arguments[i];
                }
            }
            YQ.voxLogs(track_obj);
        }

        bindGlobalEvent();
        setTimeout(function () {
            initProfessorSwiper(self.professorList().length);
        }, 0);
    };

    ko.applyBindings(new awardModal(), document.getElementById('awardContent'));
});