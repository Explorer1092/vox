/*
* create by chunbao.cai on 2018-4-28
* 薯条英语公众号相关
* -- 个人中心
*
* */
define(["jquery"], function ($) {
    $(function () {
        $(".personalMain ul li").eq(0).click(function () {
            if ($(this).data("status")) {
                // $(".personalPop").toggleClass("active")
                window.location.href = '/chips/center/myteacher.vpage'
            } else {
                alert("购买课程才能有自己的当期老师哦~")
            }
        })

        $(".closeBtn").click(function () {
            $(".personalPop").toggleClass("active")
        })

        $("#invite").click(function () {
            alert("购买课程才能推荐哦");
            location.href = "/chips/center/robin.vpage"
        })

        $(".logout").click(function () {
            $.post("/signup/chips/logout.vpage", function (res) {
                if (res.success) {
                    location.href = res.returnUrl
                } else {
                    alert(res.info)
                }
            })
        })
    });
});