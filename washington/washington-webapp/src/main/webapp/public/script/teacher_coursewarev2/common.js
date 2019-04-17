/**
 * @author: pengmin.chen
 * @description: "课件大赛-通用"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

// 此页面由于所有页面都导入了，故采用jquery的实现方式
define(['jquery','YQ', 'voxLogs'], function ($, YQ) {
    var createReqeustFlag = false; // 创建flag
    var awardSwiper = null; // 获奖弹窗swiper

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

    // 显示右下角小popup
    function showPopupTip (content, type, time) {
        <!-- default: message, error, success, warn, message -->
        var notificationPopupHtml = "<div class=\"notification notificationTip\">" +
            "<div class=\"type-icon icon " + type + "\"></div>" +
            "<div class=\"notification-inner\">" +
                "<p class=\"title\">提示</p>" +
                "<p class=\"content popupTipContent\">" + content + "</p>" +
            "</div>" +
            "<div class=\"close-btn closePopupTip closeNotificationTip\"></div>" +
        "</div>";
        // 不存在则插入dom
        if (!$('.notificationTip').length) {
            $('body').append(notificationPopupHtml);
            setTimeout(function() {
                $('.notificationTip').addClass('show');
            }, 0)
        }
        // 自动关闭
        setTimeout(function () {
            removePopupTip();
        }, time || 3000);
        // 监听手动关闭
        $(document).one('click', '.closeNotificationTip',function(){ // 关闭
            removePopupTip();
        });
    }
    // 关闭popupTip
    function removePopupTip() {
        $('.notificationTip').removeClass('show'); // 新隐藏
        setTimeout(function () {
            $('.notificationTip').remove(); // 再删除
        }, 300);
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

    // header 分享二维码
    function shareQrcode() {
        if (window.location.href.indexOf('17zuoye.com') > -1) {
            $('.appLinkQrcode').attr('src', cdnHeader + '/public/skin/teacher_coursewarev2/images/qrcode_prod_v2.png');
        } else if (window.location.href.indexOf('staging.17zuoye.net') > -1) {
            $('.appLinkQrcode').attr('src', cdnHeader + '/public/skin/teacher_coursewarev2/images/qrcode_staging_v2.png');
        } else {
            $('.appLinkQrcode').attr('src', cdnHeader + '/public/skin/teacher_coursewarev2/images/qrcode_test_v2.png');
        }
    }

    // 激活当前页面对应的tab
    function activeTab() {
        var pageLink = window.location.pathname;
        var pageRoute = pageLink.substring(pageLink.lastIndexOf('/') + 1, pageLink.indexOf('.vpage'));
        var navRouteArr = ['award', 'index', 'rule', 'course', 'personalcenter']; // ranking、vote下线先去掉

        // 激活tab
        if (navRouteArr.indexOf(pageRoute) > -1) {
            $('.navList li').eq(navRouteArr.indexOf(pageRoute)).addClass('active');
        }

        // 判断优秀作品评选tab 红点
        if (YQ.getCookie('readAwardPage') === 'true' || pageRoute === 'award') { // cookie已记录或切换到获奖tab时移除class
            YQ.setCookie('readAwardPage', true, 365); // 更新最新的cookie
            $('.JS-awardTab').removeClass('redDot');
        } else {
            $('.JS-awardTab').addClass('redDot');
        }
    }

    // 请求用户信息
    function requestUserInfo() {
        $.ajax({
            url: '/courseware/contest/userInfo.vpage',
            type: 'GET',
            success: function (res) {
                if (res.success) {
                    userInfo = $.extend(res.userInfo, {
                        isLogin: true
                    });
                    // userInfo除了在个人中心页有使用外，其余的都给打点使用
                    $('.personalcenterUserName').text(userInfo.name);
                    $('.personalcenterProductNum').text(userInfo.productNum);
                    $('.personalcenterSubject').text(userInfo.subject);
                    $('.personalcenterRegionName').text(userInfo.regionName);
                    $('.personalcenterSchoolName').text(userInfo.schoolName);

                    queryAward(userInfo.teacherId);
                } else if (res.errorCode === '-200') { // 未登录的状态下只有个人中心tab才弹窗提示，其余页面取不到数据没关系
                    if (window.location.href.indexOf('personalcenter') > -1) {
                        alertTip('请先登录', function () {
                            window.location.href = '/login.vpage';
                        });
                    }
                } else {
                    alertTip(res.info || '请求失败，稍后重试！');
                }
            }
        });
    }

    // 查询是否中奖
    function queryAward(teacherId) {
        var awardInfo = null;
        for (var i = 0; i < awardTeachers.length; i++) {
            if (awardTeachers[i].teacherId === +teacherId) {
                awardInfo = awardTeachers[i];
                break;
            }
        }
        if (awardInfo) {
            $('.awardAlert .teacherName').text('尊敬的' + awardInfo.teacherName + '老师，恭喜您'); // 姓名
            $('.awardAlert .teacherAvatar').attr('src', awardInfo.teacherAvatar); // 头像

            // clone dom
            for (var j = 0; j < awardInfo.awards.length - 1; j++) {
                $('.awardAlert .swiperWrapper').append($('.awardAlert .swiperSlide').eq(0).clone(true));
            }
            // 遍历赋值
            var personalAwardHtml = '';
            for (var m = 0; m < awardInfo.awards.length; m++) {
                $('.awardAlert .swiperSlide').eq(m).attr('data_awardid', m);
                $('.awardAlert .awardName').eq(m).text(awardInfo.awards[m]);
                personalAwardHtml += "首届信息化教学设计大赛--" + awardInfo.awards[j] + "<br>";
            }

            // 只有一个不展示swiper左右按钮
            if (awardInfo.awards.length === 1) {
                $('.awardPopupContainer .leftArrow, .awardPopupContainer .rightArrow').hide();
            }

            $('.personalcenterAward').show().find('.personalcenterAwardName').html(personalAwardHtml);
            $('.awardAlert').show();

            awardSwiper = new Swiper('.awardPopupContainer', {
                loop: true,
                pagination: awardInfo.awards.length > 1 ?'.swiper-pagination2' : '',
                paginationClickable: true,
                autoplayDisableOnInteraction: true,
                noSwiping: true
            });

            // 设置cookie
            if (YQ.getCookie('seeAwardAlert') === 'true') { // cookie已记录
                YQ.setCookie('seeAwardAlert', true, 365); // 更新最新的cookie
                $('.awardAlert').hide();
            } else {
                $('.awardAlert').show();
            }
        }
    }

    // 请求是否参加过
    function requestHasJoin() {
        $.ajax({
            url: '/courseware/contest/joinInfo.vpage',
            type: 'GET',
            success: function (res) {
                if (res.success) {
                    if (res.data === 'Y') {
                        $('.headerJoinGame').addClass('uploadBtn').text('上传作品');
                        $('.bannerJoinGame').addClass('uploadBtn');
                        $('.courseRuleAlertBtn').addClass('disabled').text('已同意');
                    } else {
                        $('.headerJoinGame').removeClass('uploadBtn').text('立即报名');
                        $('.bannerJoinGame').removeClass('uploadBtn');
                        $('.courseRuleAlertBtn').removeClass('disabled').text('同意');
                    }
                } else if (res.errorCode !== '-200') { // 未登录不弹窗
                    alertTip(res.info || '请求失败，稍后重试！');
                }
            }
        });
    }

    // 请求参加活动
    function requestJoinActivity() {
        $.ajax({
            url: '/courseware/contest/join.vpage',
            type: 'POST',
            success: function (res) {
                if (res.success) {
                    joinGame();
                } else {
                    alertTip(res.info || '请求失败，稍后重试！');
                }
            }
        });
    }

    // 参加活动
    function joinGame() {
        if (createReqeustFlag) {
            showPopupTip('正在创建中...', 'message');
            return ;
        }
        createReqeustFlag = true;

        $.ajax({
            url: '/courseware/contest/myworks/create.vpage',
            type: 'POST',
            success: function (res) {
                if (res.success) {
                    window.location.href = '/courseware/contest/upload.vpage?courseId=' + res.id + '#nav';
                } else {
                    alertTip(res.info || '请求失败，稍后重试！');
                }
            },
            complete: function() {
                createReqeustFlag = false;
            }
        });
    }

    // 立即报名、参加活动（header、banner）
    $(document).on('click', '.headerJoinGame, .bannerJoinGame', function() {
        if (!userInfo.isLogin) {
            alertTip('请先登录', function () {
                window.location.href = '/login.vpage';
            });
            return;
        }
        if ($(this).hasClass('uploadBtn')) { // 上传
            if ($(this).hasClass('headerJoinGame')) {
                doTrack('o_RozVUHNAtS', userInfo.subject);
            } else {
                doTrack('o_eJ8TmBpihf', userInfo.subject);
            }
            joinGame();
        } else { // 参加活动
            if ($(this).hasClass('headerJoinGame')) {
                doTrack('o_sa3Wr8GfD0', userInfo.subject);
            } else {
                doTrack('o_KTKhk53BJG', userInfo.subject);
            }
            $('.courseRuleAlert').show(); // 报名须知弹窗被展示
            doTrack('o_IA8bnwJZqt', userInfo.subject);
        }
    });

    // 点击展示须知弹窗
    $(document).on('click', '.seeJoinNote', function() {
        $('.courseRuleAlert').show();
        doTrack('o_IA8bnwJZqt', userInfo.subject);
    });

    // 关闭报名须知弹窗
    $(document).on('click', '.closeCourseRuleAlert', function() {
        $('.courseRuleAlert').hide();
    });

    // 同意报名须知
    $(document).on('click', '.courseRuleAlertBtn', function() {
        if ($(this).hasClass('disabled')) return;
        $('.courseRuleAlert').hide();
        requestJoinActivity(); // 请求接口参加活动
    });

    // 创建课件（个人中心+）
    $(document).on('click', '.createCourse', function() {
        joinGame();
    });

    // 给非a链接dom绑定打点(绑定data_op)
    $(document).on('click', '.needtrack', function () {
        var $this = $(this);
        doTrack($this.attr('data_op'), userInfo.subject);
    });

    // 给a链接dom打点(绑定data_op和data_link)
    $(document).on('click', '.linktrack', function () {
        var $this = $(this);
        doTrack($this.attr('data_op'), userInfo.subject || '');
        setTimeout(function () {
            window.location.href = $this.attr('data_link');
        }, 100);
    });

    // 给nav跳转打点(因为需要记录s0，故单独处理)
    $(document).on('click', '.navList li', function () {
        var aLink = $(this).find('a');
        doTrack('o_EmfhvliyYY', userInfo.subject, aLink.text());
        setTimeout(function () {
            window.location.href = aLink.attr('data_jumplink');
        }, 100);
    });

    // 关闭获奖通知弹窗
    $(document).on('click', '.awardCloseBtn', function() {
        $('.awardAlert').hide();
        $('.awardAlert').hide();
        YQ.setCookie('seeAwardAlert', true, 365); // 更新最新的cookie
    });

    // 获奖通知swiper 左按钮
    $(document).on('click', '.awardPopupContainer .leftArrow', function() {
        awardSwiper.swipePrev();
    });

    // 获奖通知swiper 右按钮
    $(document).on('click', '.awardPopupContainer .rightArrow', function() {
        awardSwiper.swipeNext();
    });

    // 获奖通知swiper 去分享
    $(document).on('click', '.awardPopupContainer .gotoShare', function() {
        var cuurentAwardId = $(this).parents('.swiperSlide').attr('data_awardid');
        var mobileAwardShareUrl = window.location.protocol + '//' + window.location.host + '/view/mobile/teacher/activity2018/coursewarematch/share/shareaward?teacherId=' + userInfo.teacherId + '&awardId=' + cuurentAwardId;
        window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileAwardShareUrl)); // 跳转通用二维码分享页
    });

    // 下载模板
    $(document).on('click', '.downloadTemplate', function () {
        var downloadIframe = "<iframe style='display:none;' src='/courseware/contest/downloadExample.vpage'/>";
        $("body").append(downloadIframe);
    });

    requestUserInfo(); // 请求用户信息
    requestHasJoin(); // 请求是否加入过活动
    shareQrcode(); // 设置header二维码
    activeTab(); // 激活tab
});