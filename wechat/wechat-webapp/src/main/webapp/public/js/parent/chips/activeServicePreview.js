
/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){
    (function ($) {
        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }
    })(jQuery);

    // $(function(){
    //
    //     // 页面被加载打点
    //     logger.log({
    //         module: 'm_XzBS7Wlh',
    //         op: 'aspage_load',
    //         so: $.getUrlParam("lessonId"),
    //         s1: $.getUrlParam("qid")
    //     });
    // });



    var vm = new Vue({
        el: "#today_study_normal",
        data: {
            title: '',
            itemList: [],
            teacher: {
                name:"",
                headPortrait:""
            },
            userAnswer:"",
        },
        methods: {
            playVideoOnce: function(e) {
                var video = document.getElementById('userVideo');
                video.play();
                video.controls=true;
                $(e.target).hide();
            }
        },
        created: function () {
            var _this = this;
            console.log()
            $.get('/chips/activeService/questionTemplate.vpage', {
                qid: $.getUrlParam("qid"),
                userId: $.getUrlParam("userId"),
                aid: $.getUrlParam("aid"),
                lessonId: $.getUrlParam("lessonId"),
                unitId: $.getUrlParam("unitId"),
                bookId: $.getUrlParam("bookId")
            }, function (res) {
                if (res.success) {
                    console.log(res);
                    _this.itemList = res.itemList;
                    _this.teacher.name = res.teacherName;
                    _this.teacher.headPortrait = res.headPortrait;
                    _this.userAnswer = res.userAnswer;
                } else {
                    alert(res.info)
                }
            });
        }
    });

});

