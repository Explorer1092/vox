/*
* create by chunbao.cai on 2018-6-22
* 薯条英语公众号
* -- 电子教材详情
*
* */
define(["jquery","logger","../../public/lib/vue/vue.js","../../public/lib/echarts/4.0.4/echarts.min.js"],function($,logger,Vue,echarts){
    var vm = new Vue({
        el:'#ranking',
        data:{
            sign:'hotVideoRank',
            data:{
                name:'',
                date:'',
                hotVideoRank:[],
                scoreRank:[]
            }
        },
        computed:{
            rank:function(){
                var _this = this;
                return _this.data[_this.sign]
            }
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            changeTab:function(sign){
                var _this = this;
                _this.sign = sign
            }
        },
        created:function(){
            var _this = this;
            $.get('/chips/daily/rank/load.vpage',{
                id:_this.getParams('id'),
                clazz:_this.getParams('clazz')
            }).then(function(res){
                if(res.success){
                    _this.data = res
                }
            });
        },
        mounted:function(){}

    })

});