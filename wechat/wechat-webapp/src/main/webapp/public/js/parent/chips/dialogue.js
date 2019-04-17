/*
* create by chunbao.cai on 2018-5-31
* 薯条英语公众号
* -- 对话实录
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){

    var vm = new Vue({
        el:'#dialogue',
        data:{
            id:window.localStorage.getItem('taskId'),
            audio:document.getElementById("audio"),
            dialog_data:[],
            dialogAudioIcon:-1,
            dialogSuggestionAudioIcon:-1,
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            playAudio:function(src,index,sIndex){
                var _this = this;
                var sIndexTmp = -1;
                _this.audio.src = src;
                _this.audio.play();
                _this.dialogAudioIcon = index;
                if(sIndex){
                    sIndexTmp = sIndex;
                }else{
                    if(sIndex === 0){
                        sIndexTmp = 0;
                    }else{
                        sIndexTmp = -1;
                    }
                }
                _this.dialogSuggestionAudioIcon = sIndexTmp;
            },
            playLocalAudio:function(mediaId,index){
                var _this = this;
                _this.dialogAudioIcon = index;
                _this.dialogSuggestionAudioIcon = -1;
                wx.playVoice({
                    localId: mediaId // 需要播放的音频的本地ID，由stopRecord接口获得
                });

                wx.onVoicePlayEnd({
                    success: function (res) {
                        _this.dialogAudioIcon = -1;
                    }
                });
            }
        },
        created:function(){
            var _this = this;
            var from = _this.getParams('from');
            if(from === 'scene'){
                _this.dialog_data = JSON.parse(window.localStorage.getItem('scene_dialog'))
            }else if(from === 'task'){
                _this.dialog_data = JSON.parse(window.localStorage.getItem('task_dialog'))
            }
            console.log(_this.dialog_data)
            _this.audio.addEventListener("ended",function(){
                _this.dialogAudioIcon = -1;
                _this.dialogSuggestionAudioIcon = -1;
            },false)
        }
    })

});