/*
* create by chunbao.cai on 2018-5-31
* 薯条英语公众号
* -- 对话实录
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){

    var vm = new Vue({
        el:'#recommend',
        data:{
            rankList:[],
            showPlay:false,
            toast_txt:'',
            toast:false,
            paid:false
        },
        methods:{
            openBook:function(){
                var _this = this;
                if(_this.showPlay){
                    window.location.href = '/chips/center/bookcatalog.vpage';
                }else{
                    _this.com_toast('邀请1人以上可查看电子教材哦~');

                    //推荐页面_电子教材按钮_被点击 打点
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'recommendpage_ebook_click'
                    });
                }
            },
            com_toast:function(txt){
                var _this = this;
                _this.toast = true;
                _this.toast_txt = txt;
                setTimeout(function(){
                    _this.toast = false;
                },3000)
            },
        },
        created:function(){
            var _this = this;
            $.get("/chips/invitation/rank/load.vpage").then(function(data){
                if(data.success){
                    _this.rankList = data.rankList;
                    _this.showPlay = data.showPlay;
                    _this.paid = data.paid;
                    if(!_this.paid){
                        _this.com_toast('购买课程之后才能推荐哦~');
                    }
                }
            })

            // 推荐页面_被加载 打点
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'recommendpage_load'
            });
        }
    })

});