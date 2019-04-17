

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
            lookImage:false,
            bigImgW:'',
            bigImgH:'',
            bigImgUrl:''
        },
        methods: {
            lookImageEvent:function(value,i,event){
                console.log($(event.currentTarget))
                var _this=this;
                var $target = $(event.currentTarget);
                var imgWidth=$target.find('img').get(0).width;
                var imgHeight=$target.find('img').get(0).height;
                var windowW=$(window).width();
                if(windowW>=imgWidth){
                    _this.bigImgW = windowW;
                    _this.bigImgH =windowW/imgWidth*imgHeight
                }else{
                    _this.bigImgW = windowW;
                    _this.bigImgH=imgWidth/windowW*imgHeight;
                }
                document.getElementById('bigImg_' + i).src=value;
                document.getElementById('bigImageBox_'+i).style.display = 'block'
            },
            changeImage:function(i){
                document.getElementById('bigImageBox_'+i).style.display = 'none'
            }
        },
        created: function () {
            var _this = this;
            $.get('/chips/activeService/queryOtherServiceTypeUserTemplate.vpage', {
                serviceType: getUrlParam("serviceType"),
                userId: getUrlParam("userId"),
                templateId: getUrlParam("templateId"),
                clazzId: getUrlParam("clazzId"),
                renewType: getUrlParam("renewType"),
            }, function (res) {
                if (res.success) {
                    _this.sharePrimeTitle = res.sharePrimeTitle;
                    _this.shareSubTitle = res.shareSubTitle;
                    _this.pageTitle = res.pageTitle;
                    // _this.templateList = res.templateList;
                    // _this.userScoreList = res.userScoreList;
                    _this.teacher.name = res.teacherName;
                    _this.teacher.headPortrait = res.headPortrait;
                    _this.templateList = res.pojo;
                    console.log(_this.templateList)
                } else {
                    alert(res.info)
                }
            });
        }
    });
});

