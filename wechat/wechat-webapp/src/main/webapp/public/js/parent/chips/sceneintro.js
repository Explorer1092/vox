/*
* create by chunbao.cai on 2018-6-10
* 薯条英语公众号
* -- 情景对话目标页面
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){

    function pushHistory() {
        var state = {
            title: "title",
            url: "#"
        };
        window.history.pushState(state, "title", "#");
    }
    pushHistory();  //这个必须在文档加载时就触发，创建出来的新的history实体
    window.addEventListener("popstate", function(e) {  //popstate 只有在history实体被改变时才会触发
        // alert("我监听到了浏览器的返回按钮事件啦");//根据自己的需求实现自己的功能
        window.location.href =  decodeURIComponent(window.localStorage.getItem('studyListUrl'));
        //	试用_情景对话_返回按钮_被点击
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'probation_situational_return_click'
        });
    }, false);


    var vm = new Vue({
        el:'#sceneintro',
        data:{
            id:window.localStorage.getItem('sceneId'),
            audio:document.getElementById("audio"),
            goal:'',
            goalAudio:'',
            questions:'',
            sencedata:'',
            title:"",
            content:"",
            sceneIntroShow:true,
            sceneTalkShow:false,
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            playAudio:function(src){
                var _this = this;
                _this.audio.src = src;
                _this.audio.play();
            },
            showSceneTalk:function(){
                var _this = this;
                _this.sceneIntroShow = false;
                _this.sceneTalkShow = true;
                /*
                * 播放情景对话第一段音频
                * */
                _this.playAudio(_this.sencedata[0].content.video);
            },
            showSummary:function(){
                var _this = this;
                window.location.href = "/chips/center/summary.vpage?id="+_this.id+"&from=scene";
            }
        },
        components:{
            'scenetalk':{
                data:function(){
                    return {
                        status:'',
                        tip:'',
                        firstEntry:true,
                        finishedDialogStatus:false,
                        isPlay:true,
                        helpStatus:false,
                        helpAudioIcon:false,
                        helpBtnBg:false,
                        translationIndex:-1,
                        dialogAudioIconIndex:-1,
                        helpAudio:'',
                        helpAudioTextCn:'',
                        helpAudioTextEn:'',
                        cameraStatus:true,
                        isRecording:false,
                        isStart:false,
                        dialogArr:[],
                        noCanVideoTip:0,
                        localId:'',
                        star:2,
                    }
                },
                template:'#scenetalk',
                props:['sencedata','audio'],
                methods:{
                    next:function(){
                        var _this = this;
                        _this.$emit('show', '');
                    },
                    showHelp:function(){
                        var _this = this;
                        console.log(_this.helpStatus);
                        if(_this.isStart){
                            if(!_this.helpStatus){
                                _this.helpStatus = true;
                                _this.helpAudioIcon = true;
                                _this.helpBtnBg = true;
                                _this.playAudio(_this.helpAudio);

                                //试用_情景对话_视频对话页面_帮助卡片_被激活
                                logger.log({
                                    module: 'm_XzBS7Wlh',
                                    op: 'probation_situational_mainpage_help_activate'
                                });

                            }else{
                                _this.helpStatus = false;
                                _this.helpAudioIcon = false;
                                _this.helpBtnBg = false;
                                _this.stopAudio();

                                //试用_情景对话_视频对话页面_帮助卡片_关闭按钮_被点击
                                logger.log({
                                    module: 'm_XzBS7Wlh',
                                    op: 'probation_situational_mainpage_help_close_click'
                                });
                            }
                        }

                    },
                    closeNoCanVideoTip:function(){
                        var _this = this;
                        _this.noCanVideoTip = false;
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
                    changeCamera:function(){
                        var _this = this;
                        if(_this.isStart){
                            _this.cameraStatus = false;
                            _this.noCanVideoTip++;
                            setTimeout(function(){
                                _this.noCanVideoTip++;
                            },3000)
                        }

                        //试用_情景对话_视频对话页面_录音按钮_被激活
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_situational_mainpage_recording_activate'
                        });

                    },
                    record:function(){

                        var _this = this;

                        // 关闭帮助弹层
                        _this.helpStatus = false;
                        _this.helpAudioIcon = false;
                        _this.helpBtnBg = false;
                        _this.stopAudio();

                        if(!_this.isRecording){
                            _this.isRecording = true;
                            // 开始录音
                            wx.startRecord();

                            //试用_情景对话_提示页面_开始拍摄按钮_被点击
                            logger.log({
                                module: 'm_XzBS7Wlh',
                                op: 'probation_situational_notice_start_click'
                            });

                        }else{
                            wx.stopRecord({
                                success: function (stopRecordRes) {
                                    _this.isRecording = false;
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
                                                    text:_this.sencedata[1].jsgf
                                                },
                                                success:function(res){
                                                    var score = JSON.parse(res.score_json).score;
                                                    _this.star = _this.score_star(score);

                                                    _this.dialogArr.push({
                                                        type:'right',
                                                        mediaId:_this.localId,
                                                        audio:'',
                                                        cn:'',
                                                        en:'',
                                                        image:_this.sencedata[0].content.role_image
                                                    });

                                                    window.localStorage.setItem("scene_dialog",JSON.stringify(_this.dialogArr));

                                                    _this.sceneDialog(res)

                                                    /*
                                                    * 更新情景对话的数据
                                                    * */
                                                    var studyListStatus = JSON.parse(window.localStorage.getItem('studyListStatus'));
                                                    // 更新情景对话状态，解锁任务对话状态
                                                    studyListStatus[1].finished = true;
                                                    studyListStatus[1].star = 2;
                                                    studyListStatus[2].isLock = false;

                                                    window.localStorage.setItem('studyListStatus',JSON.stringify(studyListStatus));

                                                }
                                            })
                                        }
                                    });
                                }
                            });
                        }
                    },
                    sceneDialog:function(res){
                        var _this = this;

                        $.post('/ai/dialogue/scene.vpage',{
                            input:res.score_json,
                            lessonId:_this.id
                        },function(data){
                            _this.cameraStatus = true;
                            if(data.success){
                                console.log(data);

                                if(data.data[0].content.level === 'E'){
                                    if(!_this.helpStatus){
                                        _this.showHelp();
                                    }else{
                                        _this.playAudio(_this.helpAudio);
                                        _this.helpAudioIcon = true;
                                    }
                                }else if(data.data[0].content.level === 'F1'){
                                    if(!_this.helpStatus){
                                        _this.showHelp();
                                    }else{
                                        _this.playAudio(_this.helpAudio);
                                        _this.helpAudioIcon = true;
                                    }
                                }else if(data.data[0].content.level === 'F2'){
                                    if(!_this.helpStatus){
                                        _this.showHelp();
                                    }else{
                                        _this.playAudio(_this.helpAudio);
                                        _this.helpAudioIcon = true;
                                    }
                                }else{
                                    _this.dialogArr.push({
                                        type:'left',
                                        audio:data.data[0].content.video,
                                        cn:data.data[0].content.cn_translation,
                                        en:data.data[0].content.translation,
                                        image:data.data[0].content.role_image,
                                        suggestion:data.data[0].knowledge.sentences
                                    });
                                    _this.isStart = false;
                                    _this.playAudio(data.data[0].content.video,_this.dialogArr.length - 1 );
                                    window.localStorage.setItem("scene_dialog",JSON.stringify(_this.dialogArr))
                                }

                                _this.status = data.data[0].status;

                            }
                        })
                    },
                    translate:function(index){
                        var _this = this;
                        _this.translationIndex = index;

                        //试用_情景对话_视频对话页面_译按钮_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_situational_mainpage_translate_click'
                        });

                    },
                    stopAudio:function(){
                        var _this = this;
                        _this.audio.src = '';
                    },
                    playAudio:function(src,index){
                        var _this = this;
                        _this.dialogAudioIconIndex = index;
                        _this.audio.src = src;
                        _this.audio.play();

                        if(src === _this.helpAudio){
                            //试用_情景对话_视频对话页面_帮助卡片_喇叭图标_被点击
                            logger.log({
                                module: 'm_XzBS7Wlh',
                                op: 'probation_situational_mainpage_help_play_click'
                            });
                        }
                    },
                    playLocalAudio:function(mediaId,index){
                        var _this = this;
                        _this.dialogAudioIconIndex = index;
                        wx.playVoice({
                            localId: mediaId // 需要播放的音频的本地ID，由stopRecord接口获得
                        });

                        wx.onVoicePlayEnd({
                            success: function (res) {
                                _this.dialogAudioIconIndex = -1;
                            }
                        });

                        //情景对话_视频对话页面_用户录音气泡_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_situational_mainpage_user_click'
                        });

                    }
                },
                created:function(){
                    var _this = this;
                    console.log(_this.sencedata);
                    /*
                    * 初始化第一次的帮助数据
                    * */
                    _this.helpAudio = _this.sencedata[1].help.help_audio;
                    _this.helpAudioTextCn = _this.sencedata[1].help.help_cn;
                    _this.helpAudioTextEn = _this.sencedata[1].help.help_en;
                    /*
                    * 初始化播放音频
                    * */
                    _this.dialogArr.push({
                        type:'left',
                        audio:_this.sencedata[0].content.video,
                        cn:_this.sencedata[0].content.cn_translation,
                        en:_this.sencedata[0].content.translation,
                        image:_this.sencedata[0].content.role_image,
                        suggestion:[]
                    });

                    // 音频播放完之后音频动画图片才动
                    _this.audio.addEventListener('canplay',function(){
                        if(_this.audio.src === _this.sencedata[0].content.video){
                            _this.dialogAudioIconIndex = 0;
                        }
                    });

                    // 音频播放完之后的监听
                    _this.audio.addEventListener("ended",function(){
                        /*
                        * 第一次进入情景对话播放的两段音频结束
                        * */
                        if(_this.audio.src === _this.sencedata[0].content.video && _this.firstEntry){
                            console.log('第一段音频播放完毕了');
                            _this.firstEntry = false;
                            _this.playAudio(_this.sencedata[1].content.video);
                            _this.dialogArr.push({
                                type:'left',
                                audio:_this.sencedata[1].content.video,
                                cn:_this.sencedata[1].content.cn_translation,
                                en:_this.sencedata[1].content.translation,
                                image:_this.sencedata[1].content.role_image,
                                suggestion:[]
                            });
                            _this.dialogAudioIconIndex = 1;

                            //试用_情景对话_视频对话页面_对话视频_被加载
                            logger.log({
                                module: 'm_XzBS7Wlh',
                                op: 'probation_situational_mainpage_video_load'
                            });

                        }
                        else if(_this.audio.src === _this.sencedata[1].content.video){
                            console.log('第二段音频播放完毕了');
                            _this.isStart = true;
                            _this.tip = _this.sencedata[1].content.tip;
                            _this.dialogAudioIconIndex = -1;
                        }
                        else{
                            _this.dialogAudioIconIndex = -1;
                            _this.isStart = true;
                            _this.helpAudioIcon = false;
                        }

                        if(_this.status){
                            _this.finishedDialogStatus = true;
                        }
                    },false);

                    //试用_情景对话_剧情介绍页面_被加载
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'probation_situational_intropage_load'
                    });
                }
            }
        },
        created:function(){
            var _this = this;
            $.get('/ai/1.0/lesson/questions.vpage',{
                id:_this.id
            },function(data){
                if(data.success){
                    _this.goal = data.goal;
                    _this.goalAudio = data.goalAudio;
                    _this.title = "这课的目标";
                    _this.content = data.goal;
                    $.get('/ai/dialogue/scene.vpage',{
                        input:_this.id,
                        lessonId:_this.id
                    },function(data){
                        if(data.success){
                            console.log(data);
                            _this.sencedata = data.data;
                        }
                    })
                }
            });

            //试用_情景对话_提示页面_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'probation_situational_notice_load'
            });

        }
    })

});