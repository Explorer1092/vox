define(["jquery", "flexSlider", "voxLogs"], function ($) {
    //点击加载更多时，隐藏
    $(".showMore").on("click", function () {
        $(this).hide();
        $('.courseDetails-box .cd-container .hideBox').css('max-height', 'inherit');
    });
    //手动运动
    $(".banner").flexslider({
        animation: "slide",
        slideshowSpeed: 3000, //展示时间间隔ms
        direction: "horizontal",//水平方向
        animationLoop: false,
        controlNav: false,
        directionNav: false,
        itemWidth: 180,
        minItems: 3,
        maxItems: 3
    });
    //tab切换
    $('.tabchecks').click(function (i) {
        var index = $('.tabchecks').index(this);
        $(this).addClass("active").siblings().removeClass('active');
        //只有当前对象有addClass("active")；导航栏下边色
        //
        $('.tabbox').eq(index).show().siblings().hide();
    });

    //点击评价
    $(".footer .inner").on('click', function () {
        location.href = '/mizar/remark/remark.vpage?_from=shopdetail&shopId=' + getQueryString('shopId') + "&shopName=" + baseData.shopName;
    });
    //查看更多
    var len = $('.onCourse-main a').length;
    if (len <= 10) {
        $('.ah-btn').hide();
    } else {
        $('.ah-btn').show();
    }
    $(document).on("click", ".ah-btn", function () {
        $('.onCourse-main').find('a').show();
        $(this).closest('.ah-btn').hide();
    });

    //获取App版本
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
});
