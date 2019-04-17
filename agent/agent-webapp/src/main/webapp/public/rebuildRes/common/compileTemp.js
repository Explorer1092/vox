//模板编译，依赖jquery、handlebars
(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery', 'handlebars'], factory);
    } else if (typeof exports === 'object') {
        // CommonJS
        module.exports = factory(require('jquery'), require('handlebars'));
    } else {
        // root or window
        root.getVerifyCodeModal = factory(root.jQuery, root.handlebars);
    }
}(this, function ($, handlebars) {
    function compileTemp (options){
        var _this = this;
        _this.templateData = options;

        var defaultOptions = {
            header:{
                headerTitle:'首页',
                headerBackLink: "javascript:window.history.back();",
                headerBackText:"&lt;&nbsp;返回",
                headerBtnLink: "/mobile/performance/index.vpage",
                headerBtnAttribute:{id: "school-level" },
                headerBtnText:"提交"
            },
            footer:{
                display:true,
                tabList:[
                    {
                        link:"/mobile/performance/index.vpage",
                        tabClass:"",
                        tabText:"首页"
                    },
                    {
                        link:"/mobile/school/index.vpage",
                        tabClass:"",
                        tabText:"搜索"
                    },
                    {
                        link:"/mobile/task/agent_task_detail_list.vpage",
                        tabClass:"",
                        tabText:"任务"
                    },
                    {
                        link:"/mobile/work_record/index.vpage",
                        tabClass:"",
                        tabText:"工作台"
                    }
                ]
            }
        };

        _this.options = $.extend(defaultOptions, options);
        console.log(_this.options);

        var tempArray = $('script[type="text/x-handlebars-template"]');
        $.each(tempArray,function(i,item){
            var tempId = item.id;
            var source = $("#"+tempId).html();

            /*class选择器是对数据集进行操作，存在共用模板的问题*/
            var htmlContainer = $("."+tempId+"Container");
            var html = "";
            var template = handlebars.compile(source);

            for(var index=0; index<htmlContainer.length;index++){

                /*数据集*/
                if(index>0){
                    /*处理_this.options,让其支持既定的模板*/
                    for ( var p in _this.options){
                        var secondObj = _this.options[p];
                        //检测扩展属性
                        if(secondObj.hasOwnProperty("extension")){
                            _this.options[p]["default"]["liList"] = _this.options[p]["extension"]["liList"+(index-1)];
                        }
                    }
                }

                html = template(_this.options);
                $(htmlContainer[index]).html(html);
            }
        });
    }

    return compileTemp;
}));
