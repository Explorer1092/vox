/*
* create by chunbao.cai on 2018-6-22
* 薯条英语公众号
* -- 电子教材目录
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){

    var vm = new Vue({
        el:'#bookcatalog',
        data:{
            toast:false,
            toast_txt:'',
            list:[]
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
            open:function(id){
                window.location.href = '/chips/center/bookdrama.vpage?id=' + id;
            }
        },
        created:function(){
            var _this = this;
            $.get('/chips/lesson/list.vpage').then(function(res){
                if(res.success){
                    _this.list = res.lessonList;
                }else{
                    _this.com_toast(res.info)
                }
            });

            // 电子教材_列表页面_被加载 打点
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'ebook_listpage_load'
            });
        }
    })

});