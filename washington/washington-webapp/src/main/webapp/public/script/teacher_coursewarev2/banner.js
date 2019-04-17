/**
 * @author: pengmin.chen
 * @description: "课件大赛-banner"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'voxLogs'], function ($, ko, YQ) {
    var bannerModal = function () {
        var self = this;
        $.extend(self, {
            newCoursewareList: ko.observableArray([]) // banner上显示的作品
        });

        // 请求banner最新课件信息
        var requestNewCourseware = function () {
            $.ajax({
                url: '/courseware/contest/newest.vpage',
                type: 'GET',
                data: {
                    limitNum: 6
                },
                success: function (res) {
                    if (res.success) {
                        self.newCoursewareList(res.data);
                        initBannerCourseSwiper();
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！')
                    }
                }
            });
        };

        // banner swiper
        function initBannerSwiper() {
            // banner
            new Swiper('.bannerSwiper', {
                loop: true,
                autoplay: 3000,
                pagination: '.swiper-pagination',
                paginationClickable: true,
                autoplayDisableOnInteraction: false,
                onSlideClick: function (swiper) {
                    if (swiper.activeLoopIndex === 0) {
                        window.location.href = '/courseware/contest/award.vpage';
                    } else if (swiper.activeLoopIndex === 2) {
                        window.location.href = '/courseware/contest/ranking.vpage';
                    }
                }
            });
        }
        // banner course swiper
        function initBannerCourseSwiper() {
            // banner课件
            new Swiper('.bannerCourseSwiper', {
                mode: 'vertical',
                loop: true,
                autoplay: 3000,
                slidesPerView: 'auto', // 同时显示的数量，auto不兼容loop模式
                loopedSlides: 6, // loop的个数
                noSwiping: true, // 无法滑动
                cssWidthAndHeight: true, // 无法slides不会自动生成宽高
                onSlideClick: function () {
                    // window.open('/courseware/contest/detail.vpage?courseId=' + self.newCoursewareList()[swiper.activeLoopIndex].courseId)
                }
            });
        }

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

        initBannerSwiper();
        requestNewCourseware(); // 请求最新的课件（banner）
    };

    ko.applyBindings(new bannerModal(), document.getElementById('bannerContent'));
});