/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){


    $(function(){

        // 页面被加载打点
        // logger.log({
        //     module: 'm_XzBS7Wlh',
        //     op: ''
        // });
    });

    (function ($) {
        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }
    })(jQuery);

    var vm = new Vue({
        el:"#today_study",
        data:{
            preview:0,
            data:{
                day:0,
                eggContent:"",
                eggImg:"",
                eggVideoUrl:"",
                hotContent:"",
                hotRankData:[],
                level_a:0,
                level_b:0,
                level_c:0,
                subject:"",
                summaryContent:"",
                summaryLink:"",
                tipsNextClass:"",
                tipsVideoImg:"",
                tipsVideoUrl:"",
                title:"",
                videoContent:"",
                videoDesc:"",
                videoImg:"",
                videoUrl:"",
            }
        },
        created:function(){
            var _this = this;
            var unitId = $("#data").data("unitid");
            var clazzId = $("#data").data("clazzid");
            var preview = $.getUrlParam("preview");
            _this.preview = preview;
            if(preview == 1){
                unitId = $.getUrlParam("unitId");
                clazzId = '5b1f40a5ac74592d4f24d596_1'
            }
            var _this = this;
            $.get('/chips/dailyLesson/messageReport.vpage',{
                unitId:unitId,
                clazzId:clazzId
            },function(res){
                if(res.success){
                    _this.data = $.extend(_this.data,res.data)
                }else{
                    alert(res.info)
                }
            })
        }
    })

});