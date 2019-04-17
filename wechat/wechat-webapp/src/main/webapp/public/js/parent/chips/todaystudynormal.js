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
        el:"#today_study_normal",
        data:{
            title:'Grade 2 Unit Lesson 1 视频对话',
            res:{
                firstAudioUrl:'',
                firstCommentDesc:'',
                firstGrammaticalExplanation:'',
                firstKnowledgeStation:'',
                secondAudioUrl:'',
                secondCommentDesc:'',
                secondGrammaticalExplanation:'',
                secondKnowledgeStation:'',
                thirdAudioUrl:'',
                thirdCommentDesc:'',
                thirdGrammaticalExplanation:'',
                thirdKnowledgeStation:'',
            }
        },
        methods:{

        },
        created:function(){
            var _this = this;
            $.get('/chips/activeService/template.vpage',{
                bookId:$.getUrlParam("bookId"),
                unitId:$.getUrlParam("unitId")
            },function(res){
                if(res.success){
                    console.log(res);
                    _this.res = res.data;
                    // firstAudioUrl
                    // firstCommentDesc
                    // firstGrammaticalExplanation
                    // firstKnowledgeStation

                }else{
                    alert(res.info)
                }
            })
        }
    })

});