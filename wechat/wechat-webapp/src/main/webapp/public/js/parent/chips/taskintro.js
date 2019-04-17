/*
* create by chunbao.cai on 2018-6-10
* 薯条英语公众号
* -- 任务对话目标页面
*
* */
define(["jquery","logger","../../public/lib/vue/vue.js"],function($,logger,Vue){

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

        //	试用_任务对话_返回按钮_被点击
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'probation_task_returnbutton_click'
        });
    }, false);


    var vm = new Vue({
        el:'#taskintro',
        data:{
            id:window.localStorage.getItem('taskId'),
            audio:document.getElementById("audio"),
            questions:'',
            taskIntroShow:true,
            taskShow:false,
            isPlay:false,
            goal:'',
            goalAudio:'',
            title:"",
            content:"",
            goShow:true,
            taskdata:''
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            playAudio:function(src){
                var audio = document.getElementById("audio"),
                    _this = this;
                audio.src = src;
                audio.play();
            },
            lookGoal:function(){
                var _this = this;
                _this.title = "这一课的目标";
                _this.content = _this.goal;
                _this.goShow = false;
                _this.playAudio(_this.goalAudio);

                //	试用_任务对话_任务说明页面_做任务按钮_被点击
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'probation_task_intropage_nextbutton_click'
                });

                //	试用_任务对话_任务背景页面_被加载
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'probation_task_backpage_load'
                });

            },
            openTask:function(){
                var _this = this;
                _this.taskIntroShow = false;
                _this.taskShow = true;
                _this.playAudio(_this.taskdata.content.audio);

                //试用_任务对话_任务背景页面_人物名称按钮_被点击
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'probation_task_backpage_NPCbutton_click'
                });

            },
            showSummary:function(){
                var _this = this;
                window.location.href = "/chips/center/summary.vpage?id="+_this.id+"&from=task";
            }
        },
        components:{
            "task":{
                data:function(){
                    return {
                        status:'',
                        isPlay:true,
                        helpStatus:false,
                        helpAudioIcon:false,
                        tip:'',
                        helpAudio:'',
                        helpAudioTextCn:'',
                        helpAudioTextEn:'',
                        translationStatus:false,
                        dialogAudioIcon:true,
                        recordAudioIcon:false,
                        recordBtnDisabledIcon:true,
                        recordBtnIngIcon:false,
                        localId:'',
                        isRecord:false,
                        startTime:0,
                        dialogAudio:'',
                        dialogTranslation:'',
                        dialogCnTranslation:'',
                        end:false,
                        dialogArr:[],

                    }
                },
                props:['audio','taskdata','questions'],
                template:"#task",
                methods:{
                    showHelp:function(){
                        var _this = this;
                        if(!_this.helpStatus){
                            _this.playAudio(_this.helpAudio);
                            //试用_任务对话_人物对话页面_帮助卡片_被激活
                            logger.log({
                                module: 'm_XzBS7Wlh',
                                op: 'probation_task_NPCpage_helpcard_activate'
                            });

                        }else{
                            _this.stopAudio(_this.helpAudio);
                            //试用_任务对话_人物对话页面帮助卡片_关闭按钮_被点击
                            logger.log({
                                module: 'm_XzBS7Wlh',
                                op: 'probation_task_NPCpage_translatebutton_click'
                            });

                            // _this.playAudio(_this.dialogAudio);
                        }
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
                    translate:function(index){
                        var _this = this;
                        _this.translationStatus = true;
                        _this.translationIndex = index;

                        //试用_任务对话_人物对话页面_译按钮_被点击
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_taskdialogue_NPCpage_translatebutton_click'
                        });

                    },
                    playAudio:function(src){
                        var _this = this;
                        _this.audio.src = src;
                        _this.audio.play();
                        if(src === _this.taskdata.help.help_audio){
                            _this.helpAudioIcon = true;
                        }else if(src === _this.dialogAudio){
                            _this.dialogAudioIcon = true;
                        }

                        if(src === _this.helpAudio){
                            //试用_任务对话_人物对话页面_帮助卡片_喇叭图标_被点击
                            logger.log({
                                module: 'm_XzBS7Wlh',
                                op: 'probation_task_NPCpage_helpcard_playbutton_click'
                            });
                        }
                    },
                    stopAudio:function(){
                        var _this = this;
                        _this.audio.src = '';
                        _this.isPlay = false;
                        _this.helpAudioIcon = false;
                        _this.recordBtnDisabledIcon = false;
                        _this.recordAudioIcon = true;
                        _this.dialogAudioIcon = false;
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
                                                        var star = _this.score_star(score);
                                                        var status = JSON.parse(window.localStorage.getItem("studyListStatus"));
                                                        status[2].isLock = false;
                                                        status[2].finished = true;
                                                        status[2].star = star;
                                                        window.localStorage.setItem("studyListStatus",JSON.stringify(status));
                                                        _this.taskDialog(res);

                                                        _this.dialogArr.push({
                                                            type:'right',
                                                            mediaId:_this.localId,
                                                            audio:'',
                                                            cn:'',
                                                            en:'',
                                                            image:_this.questions[4][0].roles[0].image
                                                        });

                                                        window.localStorage.setItem("task_dialog",JSON.stringify(_this.dialogArr))

                                                    }
                                                })
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    },
                    taskDialog:function(res){
                        var _this = this;
                        $.post('/ai/dialogue/task.vpage',{
                            input:res.score_json,
                            lessonId:_this.id,
                            name:_this.questions[4][0].roles[0].name
                        },function(data){
                            if(data.success){
                                if(data.data[0].content.level === 'E'){
                                    if(!_this.helpStatus){
                                        _this.showHelp();
                                    }
                                    _this.playAudio(_this.helpAudio);
                                    _this.recordBtnDisabledIcon = true;
                                    _this.recordAudioIcon = false;
                                }else{

                                    _this.dialogArr.push({
                                        type:'left',
                                        audio:data.data[0].content.audio,
                                        cn:data.data[0].content.cn_translation,
                                        en:data.data[0].content.translation,
                                        image:_this.questions[4][0].roles[0].image,
                                        suggestion:data.data[0].knowledge.sentences
                                    });

                                    window.localStorage.setItem("task_dialog",JSON.stringify(_this.dialogArr))

                                    _this.dialogAudio = data.data[0].content.audio;
                                    _this.dialogTranslation = data.data[0].content.translation;
                                    _this.dialogCnTranslation = data.data[0].content.cn_translation;

                                    if(data.data.length === 1){
                                        _this.tip = data.data[0].content.tip;
                                        _this.helpAudio = data.data[0].help.help_audio;
                                        _this.helpAudioTextCn = data.data[0].help.help_cn;
                                        _this.helpAudioTextEn = data.data[0].help.help_en;
                                    }

                                    _this.playAudio(_this.dialogAudio);
                                    _this.recordBtnDisabledIcon = true;
                                    _this.recordAudioIcon = false;
                                }
                                _this.status = data.data[0].status;

                            }
                        });
                    },
                    next:function(){
                        var _this = this;
                        _this.$emit('show', '');
                    }
                },
                created:function() {
                    var _this = this;

                    _this.dialogArr.push({
                        type:'left',
                        audio:_this.taskdata.content.audio,
                        cn:_this.taskdata.content.cn_translation,
                        en:_this.taskdata.content.translation,
                        image:_this.questions[4][0].roles[0].image,
                        suggestion:[]
                    });

                    /*
                    * 初始化帮助中心的内容
                    * */

                    _this.tip = _this.taskdata.content.tip;
                    _this.helpAudio = _this.taskdata.help.help_audio;
                    _this.helpAudioTextCn = _this.taskdata.help.help_cn;
                    _this.helpAudioTextEn = _this.taskdata.help.help_en;

                    /*
                    * 初始化对话的音频
                    * */
                    _this.dialogAudio = _this.taskdata.content.audio;
                    _this.dialogTranslation = _this.taskdata.content.translation;
                    _this.dialogCnTranslation = _this.taskdata.content.cn_translation;

                    /*
                    * 监听所有音频播放完成之后执行的逻辑
                    * */
                    _this.audio.addEventListener("ended",function(){

                        _this.isPlay = false;
                        _this.helpAudioIcon = false;
                        _this.recordBtnDisabledIcon = false;
                        _this.recordAudioIcon = true;
                        _this.dialogAudioIcon = false;

                        //试用_任务对话_人物对话页面_录音按钮_被激活
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'probation_taskdialogue_NPCpage_recordingbutton_activate'
                        });

                        /*
                        * 当前播放完的音频为正确对话的音频，则结束任务对话
                        * */
                        if(_this.dialogAudio === "http://cdn.17zuoye.com/fs-resource/5aded8dbd700a0429fb76826.sy3"){
                            _this.end = true;
                        }
                        if(_this.status){
                            _this.end = true;
                        }

                    },false);

                    //试用_任务对话_人物对话页面_被加载
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'probation_task_NPCpage_click'
                    });

                    //试用_任务对话_人物对话页面_人物音频_被激活
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'probation_task_NPCpage_recordingbutton_activate'
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
                    _this.questions = data.questions;
                    _this.content = data.background;
                    _this.goal = data.goal;
                    _this.goalAudio = data.goalAudio;
                    _this.background = data.background;
                    _this.backgroundAudio = data.backgroundAudio;

                    $.get('/ai/dialogue/task.vpage',{
                        input:_this.id,
                        lessonId:_this.id,
                        name:_this.questions[4][0].roles[0].name,
                    },function(data){
                        if(data.success){

                            _this.taskdata = data.data[0];
                        }
                    })
                }
            });

            //试用_任务对话_任务说明页面_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'probation_task_intropage_load'
            });

        }
    })

});