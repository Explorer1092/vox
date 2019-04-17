
/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue) {
    function getUrlParam(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }

    //页面被加载打点
    logger.log({
        module: 'm_XzBS7Wlh',
        op: 'aspage_load',
        so: getUrlParam("lessonId"),
        s1: getUrlParam("qid")
    });
    
    var vm = new Vue({
        el: "#today_study_normal",
        data: {
            pronList: [],
            gramList: [],
            knowledgeList: [],
            summary: "",
            teacher: {
                name: "",
                headPortrait: ""
            },
            userAnswer: "",
            sharePrimeTitle: "",
            shareSubTitle: "",
            pageTitle: '',
            index: null,
            dTime: null,
            knowledgeListBox: [],
            audio: [],
            video: '',
            output: '',
            scale: 0.8,
            vWidth: '',
            vHeight: '',
            audioUrl:'',
            playStatus:false,
            urlArray:[],
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
                if($('#userVideo').get(0)) {
                    $('#userVideo').get(0).pause();
                    $('#userVideo').get(0).controls = false;
                }
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
            loadDuration: function (i) {
                var _this = this;
                _this. getAudioUrl();
                var videoBox = document.getElementById('audioTag_' + i);
                for(var j=0;j<_this.urlArray.length;j++){
                   if( _this.urlArray[j].index == i){
                   }
                }
                var testTime = parseInt(videoBox.duration);
                var timeText = document.getElementById('audioTime_' + i);
                timeText.innerText=testTime+' \'';
            },
            getAudioUrl:function(){
                var _this=this;
                for(var i=0;i<_this.knowledgeListBox.length;i++){
                    if(_this.knowledgeListBox[i].type === 'audio'){
                        _this.urlArray.push({index:i,value:_this.knowledgeListBox[i].value});
                    }
                }
            }
        },
        created: function () {
            var _this = this;
            $.get('/chips/activeService/queryActiveServiceUserTemplate.vpage', {
                qid: getUrlParam("qid"),
                userId: getUrlParam("userId"),
                aid: getUrlParam("aid"),
                lessonId: getUrlParam("lessonId"),
                unitId: getUrlParam("unitId"),
                bookId: getUrlParam("bookId")
            }, function (res) {
                if (res.success) {
                    _this.sharePrimeTitle = res.sharePrimeTitle;
                    _this.shareSubTitle = res.shareSubTitle;
                    _this.pageTitle = res.pageTitle;
                    _this.pronList = res.userTemplate.pronList;
                    _this.gramList = res.userTemplate.grammarList;
                    _this.knowledgeList = res.userTemplate.knowledgeList;
                    _this.summary = res.userTemplate.learnSummary;
                    _this.teacher.name = res.teacherName;
                    _this.teacher.headPortrait = res.headPortrait;
                    _this.userAnswer = res.userAnswer;
                    _this.knowledgeListBox = res.knowledgeList;
                    _this.length = res.knowledgeList.length;
                } else {
                    alert(res.info)
                }
            });
        },
        watch: {
            userAnswer: function () {
                this.$nextTick(function () {
                    this.initialize();
                });
            },
        }

    });
});

