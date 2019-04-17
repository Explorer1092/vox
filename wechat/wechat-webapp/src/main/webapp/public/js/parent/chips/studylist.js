/*
* create by chunbao.cai on 2018-6-10
* 薯条英语公众号
* -- 学习列表页
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){
    window.localStorage.setItem('beUrl', getParams('back'));
    window.localStorage.setItem('studyListUrl', encodeURIComponent(location.pathname + location.search));

    function pushHistory() {
        var state = {
            title: "title",
            url: "#"
        };
        window.history.pushState(state, "title", "#");
    }
    function getParams(name){
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURI(r[2]); return null;
    }

    pushHistory();  //这个必须在文档加载时就触发，创建出来的新的history实体
    window.addEventListener("popstate", function(e) {  //popstate 只有在history实体被改变时才会触发
        // alert("我监听到了浏览器的返回按钮事件啦");//根据自己的需求实现自己的功能
        window.location.href =  decodeURIComponent(window.localStorage.getItem('beUrl'));
    }, false);

    var vm = new Vue({
        el:'#studyList',
        data:{
            lessons:[
                {
                    isLock:false,
                    finished:false,
                    name:"热身练习",
                    star:0
                },
                {
                    isLock:true,
                    finished:false,
                    name:"情景对话",
                    star:0
                },
                {
                    isLock:true,
                    finished:false,
                    name:"任务对话",
                    star:0
                }
                ],
            url:[
                "/chips/center/followwordlist.vpage?from=warm_up",
                "/chips/center/sceneintro.vpage?from=scene",
                "/chips/center/taskintro.vpage?from=task"
            ]
        },
        created:function(){
            var _this = this;

            /*
            * 获取课程详细信息
            * */
            $.get('/ai/1.0/classdetail.vpage',{
                unitId:'BKC_10300227051151'
            },function(data){
                if(data.success){

                    /*
                    * 热身、情景、任务课程的ID保存到本地
                    * */
                    window.localStorage.setItem('warmUpId',data.lessons[0].id);
                    window.localStorage.setItem('sceneId',data.lessons[1].id);
                    window.localStorage.setItem('taskId',data.lessons[2].id);

                    /*
                    * 试用学习列表状态保存在本地
                    * */
                    if(!window.localStorage.hasOwnProperty('studyListStatus')){
                        window.localStorage.setItem('studyListStatus',JSON.stringify(_this.lessons));
                    }else{
                        _this.lessons = JSON.parse(window.localStorage.getItem('studyListStatus'));
                    }
                }
            });


            //试用_学习列表页面_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'probation_learning_list_load'
            });
        }
    })

});