/*
* create by chunbao.cai on 2018-6-10
* 薯条英语公众号
* -- 总结
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){



    var vm = new Vue({
        el:'#sharevideo',
        data:{
            tipStatus:false,
            isPlay:false
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            share:function(){
                var _this = this;
                _this.tipStatus = !_this.tipStatus;
            },
            playAudio:function(){
                var _this = this;
                var audioElem = document.getElementById("audio");
                if(!_this.isPlay){
                    audioElem.play();
                    _this.isPlay = true;
                }else{
                    audioElem.pause();
                    _this.isPlay = false;
                }


            }
        },

        created:function(){

        }
    })

});