/*
* create by chunbao.cai on 2018-5-31
* 薯条英语公众号
* -- 跟读单词列表
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){

    // 点击返回按键返回到学习列表页面
    function pushHistory() {
        var state = {
            title: "title",
            url: "#"
        };
        window.history.pushState(state, "title", "#");
    }
    //这个必须在文档加载时就触发，创建出来的新的history实体
    pushHistory();
    window.addEventListener("popstate", function(e) {
        /*
        * popstate 只有在history实体被改变时才会触发
        * 跳转到学习列表页
        * */
        window.location.href =  decodeURIComponent(window.localStorage.getItem('studyListUrl'));

        //	试用_热身模块_返回按钮_被点击
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'probation_warmup_return_click'
        });
    }, false);


    var vm = new Vue({
        el:'#followwordlist',
        data:{
            questions:[],
            id:window.localStorage.getItem('warmUpId'),
            audio:document.getElementById("audio"),
            followWordListShow:true,
            followWordShow:false,
            followSentenceListShow:false,
            followSentenceShow:false,

        },
        methods:{
            showWordFollow:function(){
                /*
                * 打开单词跟读页面，并且播放单词音频
                * */
                var _this = this;
                _this.followWordShow = true;
                _this.followWordListShow = false;
                _this.audio.src = _this.questions[1][0].audio;
                _this.audio.play();

                //试用_热身模块_跟读页面_开始go按钮_被点击
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'probation_warmup_drilling_begingo_click'
                });

            },
            showFollowSentenceList:function(){
                var _this = this;
                _this.followWordShow = false;
                _this.followSentenceListShow = true;
            },
            showFollowSentence:function(){
                var _this = this;
                _this.followSentenceListShow = false;
                _this.followSentenceShow = true;
            },
            showSummary:function(){
                var _this = this;
                window.location.href = "/chips/center/summary.vpage?id="+_this.id+"&from=warm_up";
            }
        },
        components:{
            'followword':{
                data:function(){
                    return {
                        helpStatus:false,
                        star:-1,
                        playTimes:0,
                        isPlay:true,
                        wordAudioIcon:true,
                        recordAudioIcon:false,
                        recordBtnDisabledIcon:true,
                        recordBtnIngIcon:false,
                        recordTime:0,
                        localId:0,
                        startTime:0,
                        isRecord:false,
                        recordTimeBtn:false,
                        nextBtn:false
                    }
                },
                props:['questions','audio'],
                template:"#followword",
                methods:{
                    showHelp:function(){
                        var _this = this;
                        _this.helpStatus = !_this.helpStatus;

                        //试用_热身模块_跟读页面_问号按钮_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_warmup_drilling_question_click'
                        });
                    },
                    score_star:function(score){
                        var scoreTmp = score;
                        if(scoreTmp < 20){
                            return 0;
                        }else if(scoreTmp < 51){
                            return 1;
                        }else if(scoreTmp < 73){
                            return 2;
                        }else{
                            return 3;
                        }
                    },
                    playAudio:function(src){
                        var audio = document.getElementById("audio"),
                            _this = this;
                        audio.src = src;
                        audio.play();
                        _this.wordAudioIcon = true;

                        //试用_热身模块_跟读页面_喇叭按钮_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_warmup_drilling_play_click'
                        });

                    },
                    playRecord:function(){
                        var _this = this;
                        _this.recordAudioIcon = true;
                        wx.playVoice({
                            localId: _this.localId // 需要播放的音频的本地ID，由stopRecord接口获得
                        });
                        wx.onVoicePlayEnd({
                            success: function (res) {
                                _this.recordAudioIcon = false;
                            }
                        });

                        //试用_热身模块_跟读页面_已录音播放按钮_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_warmup_drilling_userplay_click'
                        });

                    },
                    record:function(text){
                        var _this = this,startTime,endTime;
                        if(!_this.recordBtnDisabledIcon){
                            if(!_this.isRecord){
                                _this.isRecord = true;
                                // 开始录音
                                wx.startRecord();
                                _this.startTime = Number(Date.now());
                            }else{
                                wx.stopRecord({
                                    success: function (stopRecordRes) {
                                        _this.isRecord = false;
                                        _this.playTimes++;
                                        endTime = Number(Date.now());
                                        _this.audioTime = Math.floor((endTime - _this.startTime)/100)/10;
                                        _this.localId = stopRecordRes.localId;
                                        wx.uploadVoice({
                                            localId: stopRecordRes.localId, // 需要上传的音频的本地ID，由stopRecord接口获得
                                            isShowProgressTips: 1, // 默认为1，显示进度提示
                                            success: function (uploadVoiceRes) {
                                                var serverId = uploadVoiceRes.serverId; // 返回音频的服务器端ID
                                                $.ajax({
                                                    type:"post",
                                                    url:"/ai/voice/score.vpage",
                                                    dataType:"json",
                                                    data:{
                                                        mediaId:serverId,
                                                        text:text
                                                    },
                                                    success:function(res){
                                                        var score = JSON.parse(res.score_json).score;
                                                        _this.star = _this.score_star(score);
                                                        _this.recordTime = _this.audioTime;

                                                        // 录音之后显示录音时长
                                                        _this.recordTimeBtn = true;

                                                        /*
                                                        * 录音在两颗星以上，显示下一步按钮
                                                        * */
                                                        if(_this.star >= 2){
                                                            _this.nextBtn = true;
                                                        }

                                                        /*
                                                        * 录音三次都是两颗星以下之后，显示下一步按钮
                                                        * */
                                                        if(_this.playTimes >= 3){
                                                            _this.nextBtn = true;
                                                        }
                                                    }
                                                })
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    },
                    next:function(){

                        var _this = this;
                        /*
                        * 保存单词跟读的星星数据
                        * */
                        window.localStorage.setItem("wordStar",_this.star);
                        _this.$emit('show', '');
                        _this.playAudio(_this.questions[2][0].preloads[0].audio);

                        //试用_热身模块_跟读页面_下一步按钮_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_warmup_drilling_next_click'
                        });


                    }
                },
                created:function(){
                    var _this = this;
                    _this.audio.addEventListener("ended",function(){
                        _this.isPlay = false;
                        _this.wordAudioIcon = false;
                        _this.recordAudioIcon = false;
                        _this.recordBtnDisabledIcon = false;

                        //试用_热身模块_跟读页面_录音按钮_被激活
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_warmup_drilling_record_activate'
                        });

                    },false);
                }
            },
            'followsentencelist':{
                data:function(){
                    return {
                        proLoadDesIcon:true,
                        sentence1Icon:false,
                        sentence2Icon:false,
                        isPlay:true
                    }
                },
                props:['questions','audio'],
                template:"#followsentencelist",
                methods:{
                    playAudio:function(src){
                        var _this = this;
                        _this.audio.src = src;
                        _this.audio.play();
                        if(src === _this.questions[2][0].preloads[0].sentences[0].audio){
                            _this.sentence1Icon = true;
                            _this.proLoadDesIcon = false;
                            _this.sentence2Icon = false;
                        }else if(src === _this.questions[2][0].preloads[0].sentences[1].audio){
                            _this.sentence2Icon = true;
                            _this.proLoadDesIcon = false;
                            _this.sentence1Icon = false;
                        }else{
                            _this.proLoadDesIcon = true;
                            _this.sentence1Icon = false;
                            _this.sentence2Icon = false;
                        }
                    },
                    next:function(){
                        var _this = this;
                        _this.$emit('show', '');
                        _this.playAudio(_this.questions[2][0].audio);

                        //试用_热身模块_过场页面_去跟读按钮_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_warmup_transition_next_click'
                        });
                    }
                },
                created:function(){
                    var _this = this;
                    _this.audio.addEventListener("ended",function(){
                        _this.isPlay = false;
                        _this.proLoadDesIcon = false;
                        _this.sentence1Icon = false;
                        _this.sentence2Icon = false;
                    },false);

                    //试用_热身模块_过场页面_被加载
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'probation_warmup_transition_load'
                    });

                }
            },
            'followsentence':{
                data:function(){
                    return {
                        helpStatus:false,
                        star:-1,
                        playTimes:0,
                        isPlay:true,
                        sentenceIcon:true,
                        recordAudioIcon:false,
                        recordBtnDisabledIcon:true,
                        recordBtnIngIcon:false,
                        recordTime:0,
                        localId:0,
                        startTime:0,
                        isRecord:false,
                        recordTimeBtn:false,
                        nextBtn:false,
                        wordstar:{
                            'where':-1,
                            'is':-1,
                            'your':-1,
                            'passport':-1,
                        }
                    }
                },
                props:['questions','audio'],
                template:"#followsentence",
                methods:{
                    showHelp:function(){
                        var _this = this;
                        _this.helpStatus = !_this.helpStatus;
                    },
                    score_star:function(score){
                        var scoreTmp = score;
                        if(scoreTmp < 20){
                            return 0;
                        }else if(scoreTmp < 51){
                            return 1;
                        }else if(scoreTmp < 73){
                            return 2;
                        }else{
                            return 3;
                        }
                    },
                    playAudio:function(src){
                        var audio = document.getElementById("audio"),
                            _this = this;
                        audio.src = src;
                        audio.play();
                        _this.sentenceIcon = true;
                    },
                    playRecord:function(){
                        var _this = this;
                        _this.recordAudioIcon = true;
                        wx.playVoice({
                            localId: _this.localId // 需要播放的音频的本地ID，由stopRecord接口获得
                        });
                        wx.onVoicePlayEnd({
                            success: function (res) {
                                _this.recordAudioIcon = false;
                            }
                        });
                    },
                    record:function(){
                        var _this = this,startTime,endTime;
                        if(!_this.recordBtnDisabledIcon){
                            if(!_this.isRecord){
                                _this.isRecord = true;
                                // 开始录音
                                wx.startRecord();
                                _this.startTime = Number(Date.now());
                            }else{
                                wx.stopRecord({
                                    success: function (stopRecordRes) {
                                        _this.isRecord = false;
                                        _this.playTimes++;
                                        endTime = Number(Date.now());
                                        _this.audioTime = Math.floor((endTime - _this.startTime)/100)/10;
                                        _this.localId = stopRecordRes.localId;
                                        wx.uploadVoice({
                                            localId: stopRecordRes.localId, // 需要上传的音频的本地ID，由stopRecord接口获得
                                            isShowProgressTips: 1, // 默认为1，显示进度提示
                                            success: function (uploadVoiceRes) {
                                                var serverId = uploadVoiceRes.serverId; // 返回音频的服务器端ID
                                                $.ajax({
                                                    type:"post",
                                                    url:"/ai/voice/score.vpage",
                                                    dataType:"json",
                                                    data:{
                                                        mediaId:serverId,
                                                        text:_this.questions[2][0].content
                                                    },
                                                    success:function(res){
                                                        var score = JSON.parse(res.score_json).score;
                                                        var words = JSON.parse(res.score_json).lines[0].words;
                                                        console.log(words);

                                                        words.forEach(function(item){
                                                            if(item.text === 'Where'){
                                                                _this.wordstar.where = item.score;
                                                            }
                                                            if(item.text === 'is'){
                                                                _this.wordstar.is = item.score;
                                                            }
                                                            if(item.text === 'your'){
                                                                _this.wordstar.your = item.score;
                                                            }
                                                            if(item.text === 'passport'){
                                                                _this.wordstar.passport = item.score;
                                                            }
                                                        });

                                                        console.log(_this.wordstar);

                                                        _this.star = _this.score_star(score);
                                                        _this.recordTime = _this.audioTime;

                                                        // 录音之后显示录音时长
                                                        _this.recordTimeBtn = true;

                                                        /*
                                                        * 录音在两颗星以上，显示下一步按钮
                                                        * */
                                                        if(_this.star >= 2){
                                                            _this.nextBtn = true;
                                                        }

                                                        /*
                                                        * 更新跟读的数据
                                                        * */
                                                        // window.localStorage.setItem("sentenceStar",_this.star);
                                                        var wordStar = Number(window.localStorage.getItem("wordStar"));
                                                        var sentenceStar = Number(_this.star);
                                                        var studyListStatus = JSON.parse(window.localStorage.getItem('studyListStatus'));
                                                        // 更新跟读状态，解锁情景对话状态
                                                        studyListStatus[0].finished = true;
                                                        studyListStatus[0].star = Math.ceil((wordStar+sentenceStar)/2);
                                                        studyListStatus[1].isLock = false;

                                                        window.localStorage.setItem('studyListStatus',JSON.stringify(studyListStatus));

                                                        /*
                                                        * 录音三次都是两颗星以下之后，显示下一步按钮
                                                        * */
                                                        if(_this.playTimes >= 3){
                                                            _this.nextBtn = true;
                                                        }
                                                    }
                                                })
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    },
                    next:function(){
                        var _this = this;
                        _this.$emit('show', '');
                    }
                },
                created:function(){
                    var _this = this;
                    _this.audio.addEventListener("ended",function(){
                        _this.isPlay = false;
                        _this.sentenceIcon = false;
                        _this.recordAudioIcon = false;
                        _this.recordBtnDisabledIcon = false
                    },false);
                }
            }
        },
        created:function(){
            var _this = this;

            //试用_热身模块_跟读页面_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'probation_warmup_drilling_load'
            });

            /*
            * 获取课程详细信息
            * */
            $.get('/ai/1.0/lesson/questions.vpage',{
                id:_this.id
            },function(data){
                if(data.success){
                    _this.questions = data.questions
                }
            })

        }
    })

});