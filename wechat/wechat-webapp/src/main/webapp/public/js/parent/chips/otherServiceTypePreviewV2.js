

/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){

    function getUrlParam(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    // 页面被加载打点
    logger.log({
        module: 'm_XzBS7Wlh',
        op: 'aspage_load',
        so: getUrlParam("lessonId"),
        s1: getUrlParam("qid")
    });

    var vm = new Vue({
        el: "#today_study_normal",
        data: {
            sharePrimeTitle:"",
            shareSubTitle:"",
            pageTitle: '',
            templateList: [],
            userScoreList: [],
            teacher: {
                name:"",
                headPortrait:""
            },
        },
        methods: {
//            playVideoOnce: function(e) {
//                var video = document.getElementById('userVideo');
//                video.play();
//                video.controls=true;
//                $(e.target).hide();
//            }
        },
        created: function () {
            var _this = this;
            $.get('/chips/activeService/queryOtherServiceTypeUserTemplate.vpage', {
                serviceType: getUrlParam("serviceType"),
                userId: getUrlParam("userId"),
                templateId: getUrlParam("templateId"),
                clazzId: getUrlParam("clazzId"),
            }, function (res) {
                if (res.success) {
                    _this.sharePrimeTitle = res.sharePrimeTitle;
                    _this.shareSubTitle = res.shareSubTitle;
                    _this.pageTitle = res.pageTitle;
                    _this.templateList = res.templateList;
                    _this.userScoreList = res.userScoreList;
                    _this.teacher.name = res.teacherName;
                    _this.teacher.headPortrait = res.headPortrait;
                } else {
                    alert(res.info)
                }
            });
        }
    });
});

