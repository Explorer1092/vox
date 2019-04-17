/*
* create by chunbao.cai on 2018-6-10
* 薯条英语公众号
* -- 总结
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
        window.location.href =  decodeURIComponent(window.localStorage.getItem('beUrl'));

        //试用_任务对话_结果页面_返回按钮_被点击
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'probation_task_completepage_returnbutton_click'
        });
    }, false);


    var vm = new Vue({
        el:'#summary',
        data:{
            from:'',
            id:'',
            star:2,
            url:{
                'warm_up':'/chips/center/sceneintro.vpage',
                'scene':'/chips/center/taskintro.vpage',
                'task': decodeURIComponent(window.localStorage.getItem('studyListUrl'))
            }
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            open:function(){
                var _this = this;
                window.location.href = '/chips/center/dialogue.vpage?id='+_this.id+'&from='+_this.getParams("from");
                if(_this.from === 'warm_up'){
                    //试用_热身模块_完成页面_继续按钮_被点击
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'probation_warmup_complete_next_click'
                    });
                }else if(_this.from === 'scene'){
                    //试用_情景对话_对话结果页面_做任务按钮_被点击
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'probation_situational_complete_next_click'
                    });
                }else if(_this.from === 'task'){
                    //试用_任务对话_结果页面_完成按钮_被点击
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'probation_task_completepage_nextbutton_click'
                    });
                }

            }
        },
        created:function(){
            var _this = this;
            _this.from = _this.getParams("from");

            var result = JSON.parse(window.localStorage.getItem("studyListStatus"));

            console.log(result)
            if(_this.from === 'warm_up'){
                _this.star = result[0].star;
                _this.id = window.localStorage.getItem('warmUpId');
                //试用_热身模块_完成页面_被加载
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'probation_warmup_complete_load'
                });
            }else if(_this.from === 'scene'){
                _this.star = result[1].star;
                _this.id = window.localStorage.getItem("sceneId");

                //试用_情景对话_对话结果页面_被加载
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'probation_situational_complete_load'
                });
            }else if(_this.from === 'task'){
                _this.star = result[2].star;
                _this.id = window.localStorage.getItem("taskId");

                //试用_任务对话_结果页面_被加载
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'probation_task_completepage_load'
                });
            }

        }
    })

});