/*
* create by chunbao.cai on 2018-6-22
* 薯条英语公众号
* -- 电子教材详情
*
* */
define(["jquery","logger","../../public/lib/vue/vue.js"],function($,logger,Vue){

    var vm = new Vue({
        el:'#bookdrama',
        data:{
            audio:document.getElementById("audio"),
            toast:false,
            toast_txt:'',
            current_index:-1,
            is_play:false,
            sign:'dialogueplay',
            translation:false,
            data:{
                taskplay:[],
                dialogueplay:[]
            }
        },
        computed:{
            playlist:function(){
                var _this = this;
                return _this.data[_this.sign]
            },
            styleObject:function(){
                var _this = this,percent;
                if(_this.playlist.length > 0){
                    percent = Math.ceil((_this.current_index+1)/_this.playlist.length*100)
                }else{
                    percent = 0;
                }
                return {
                    width:percent+'%'
                }
            }
        },
        methods:{
            com_toast:function(txt){
                var _this = this;
                _this.toast = true;
                _this.toast_txt = txt;
                setTimeout(function(){
                    _this.toast = false;
                },3000)
            },
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            change_tab:function(sign){
                var _this = this;
                _this.sign = sign;
                _this.current_index = -1;
                _this.is_play = false;
                _this.audio.src = '';
                $(window).scrollTop(0);
            },
            translate:function(){
                var _this = this;
                _this.translation = !_this.translation;

                // 电子教材_课程页面_翻译_被开启 打点
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'ebook_lessonpage_translate_click',
                    s0:_this.getParams('id'),
                    s1:_this.translation? 'on': 'off'
                });
            },
            playAudio:function(index){
                var _this = this;
                _this.current_index = index;
                _this.audio.src = _this.playlist[index].media;
                _this.audio.play();
                _this.is_play = true;

                // 电子教材_课程页面_播放_被激活 打点
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'ebook_lessonpage_play_activate',
                    s0:_this.getParams('id')
                });

            },
            play_paused:function(){
                var _this = this;
                if(_this.current_index === -1){
                    _this.current_index = 0;
                }

                if(_this.is_play){
                    _this.is_play = false;
                    _this.audio.pause()
                }else{
                    _this.is_play = true;
                    if(_this.audio.getAttribute('src')){
                        _this.audio.play();
                    }else{
                        _this.playAudio(_this.current_index);
                    }
                }

            }
        },
        created:function(){
            var _this = this;
            $.get('/chips/lesson/play.vpage',{
                id:_this.getParams('id')
            }).then(function(res){
                if(res.success){
                    _this.data.taskplay = res.taskPlay;
                    _this.data.dialogueplay = res.dialoguePlay;
                }else{
                    _this.com_toast(res.info)
                }
            });

            _this.audio.addEventListener("ended",function(){

                _this.current_index++;

                if(_this.current_index > (_this.playlist.length - 1)){
                    _this.current_index = 0;
                    $(window).scrollTop(0);
                }else{
                    $(window).scrollTop($(".bookTitle").offset().top-50);
                }
                if(_this.playlist.length > 0){
                    _this.playAudio(_this.current_index)
                }
            },false);

            // 电子教材_课程页面_被加载 打点
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'ebook_lessonpage_load',
                s0:_this.getParams('id')
            });
        }

    })

});