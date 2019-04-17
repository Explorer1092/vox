

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
            topItemList:[],
            weekPointList:[],
            bottomItemList:[],
            playStatus:false,
            urlArrayTop:[],
            urlArrayWp:[[]],
            urlArrayBot:[],
            currentPlayingAudio: null
        },
        methods: {
            playVideoOnce: function (e) {
                var video = document.getElementById('userVideo');
                if(this.playStatus){
                    video.pause();
                    this.playStatus=false;
                }else{
                    $('audio').each(function(){
                        this.pause();
                    });
                    $('.icon_play').removeClass('icon_playing');
                    video.play();
                    video.controls = true;
                    $(e.target).hide();
                }
            },
            //播放按钮
            playAudio: function (value, i, event) {
                $('audio').each(function(i, e) {
                    this.pause();
                    this.currentTime = 0;
                    $(this).siblings('.icon_play').removeClass('icon_playing');
                });
                var $target = $(event.currentTarget);
                $target.removeClass('iconRedDot');
                var audio = $target.find('audio').get(0);
                if(audio && this.currentPlayingAudio !== i) {
                    this.currentPlayingAudio = i;
                    audio.play();
                    $target.find('.icon_play').addClass('icon_playing');
                    audio.onended = function(e) {
                        $(e.target).siblings('.icon_play').removeClass('icon_playing');
                    };
                }else {
                    audio.pause();
                    audio.currentTime = 0;
                    this.currentPlayingAudio = null;
                }
            },
            //截取第一帧视频作为预览图
            captureImage: function () {
                var _this = this;
                var img = new Image();
                _this.video = document.getElementById("userVideo");
                _this.video.addEventListener('canplay', function () {
                    _this.vWidth = this.videoWidth;
                    _this.vHeight = this.videoHeight;
                });
            },
            initialize: function () {
                var _this = this;
                _this.output = document.getElementById("modal");
                _this.video = document.getElementById("userVideo");
                if(_this.video) {
                    _this.video.addEventListener('loadeddata', _this.captureImage);
                    _this.video.addEventListener('canplay', function () {
                        _this.vWidth = this.videoWidth;
                        _this.vHeight = this.videoHeight;
                    });
                }
            },
            //获取音频时长并渲染到页面上
            loadDurationTop: function (i) {
                var _this = this;
                var videoBox = document.getElementById('top_audioTag_' + i);
                var testTime = parseInt(videoBox.duration);
                var timeText = document.getElementById('top_audioTime_' + i);
                timeText.innerText=testTime+' \'';
            },
            loadDurationWp: function (i, k) {
                var _this = this;
                var videoBox = document.getElementById(k + 'wp_audioTag_' + i);
                var testTime = parseInt(videoBox.duration);
                var timeText = document.getElementById(k + 'wp_audioTime_' + i);
                timeText.innerText=testTime+' \'';
            },
            loadDurationBot: function (i) {
                var _this = this;
                var videoBox = document.getElementById('bot_audioTag_' + i);
                for(var j=0;j<_this.urlArrayBot.length;j++){
                    if( _this.urlArrayBot[j].index == i){
                    }
                }
                var testTime = parseInt(videoBox.duration);
                var timeText = document.getElementById('bot_audioTime_' + i);
                timeText.innerText=testTime+' \'';
            },
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
                    _this.topItemList = res.pojo.topItemList;
                    _this.weekPointList = res.pojo.weekPointList;
                    _this.bottomItemList = res.pojo.bottomItemList;
                } else {
                    alert(res.info)
                }
            });
        }
    });
});

