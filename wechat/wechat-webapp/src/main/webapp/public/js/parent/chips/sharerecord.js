/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){


    $(function(){

        // 广告页_被加载
        // logger.log({
        //     module: 'm_XzBS7Wlh',
        //     op: 'mainad_load'
        // });
    });

    var vm = new Vue({
        el:'#award',
        data:{
            count:0,
            records:[]
        },
        created:function(){
            var _this = this;
            var bookId = $("#bookid").val();
            $.post("/chips/center/mysharerecorddata.vpage",{
                bookId:bookId
            },function(res){
                if(res.success){
                    _this.count = res.data.count;
                    _this.records = res.data.records;
                }else{
                    alert(res.info)
                }
            })
        }
    })

});