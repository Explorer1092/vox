/**
 * 应试题目展示
 * params参数结构
     {
        contentId : 容器的id，（必须）
        formulaContainer : 公式渲染容器（必须）
        questions : 试题数组，包含完整的试题json结构， （必须）
        showAnalysis : 是否展示解析
        showUserAnswer : 是否展示用户答案
        showRightAnswer : 是否展示正确答案
     }
    showUseranswer为true时，需要在questions数组中的每个question对象下扩展以下字段
     {
          userAnswers: [["1"]],  //用户答案
          subMaster: [[true]],  //题目每个项（空）的掌握情况
          master: true   //整道题的掌握情况
     }
 */
(function($17,ko) {
    "use strict";
    var env = window.env;
    ko.components.register('ko-venus-question',{
        viewModel : function(params){
            var vm = this;
            params = ko.mapping.toJS(params);
            vm.contentId = params.contentId;
            vm.formulaContainer = params.formulaContainer;
            vm.questions = params.questions;
            vm.questionIds = ko.observable("");
            vm.showAnalysis = params.showAnalysis || false;
            vm.showUserAnswer = params.showUserAnswer || false;
            vm.showRightAnswer = params.showRightAnswer || false;
            vm.showIntervene = params.showIntervene || false;
            vm.downloadTip = ko.observable(false);
            var onSendLogFn = params.onSendLog || ko.observable(function(){});
            vm.env = env;
            this.initQuestion = function(){
                var questionArr = vm.questions;
                if (questionArr.length > 0) {
                    var questionIds = [];
                    for(var m = 0,mLen = questionArr.length; m < mLen; m++){
                        questionIds.push(questionArr[m].id);
                    }
                    vm.questionIds(questionIds.join(","));
                    vm.downloadTip(false);
                    var config = {
                        container: '#' + vm.contentId, //容器的id，（必须）
                        formulaContainer:'#' + vm.formulaContainer, //公式渲染容器（必须）
                        questionList: vm.questions, //试题数组，包含完整的试题json结构， （必须）
                        framework: {
                            vue: Vue, //vue框架的外部引用
                            vuex: Vuex //vuex框架的外部引用
                        },
                        showAnalysis: vm.showAnalysis, //是否展示解析
                        showUserAnswer: vm.showUserAnswer, //是否展示用户答案
                        showRightAnswer: vm.showRightAnswer, //是否展示正确答案
                        showIntervene : vm.showIntervene, //语文控制显示干预的字段
                        startIndex : 0, //从第几题开始
                        onSendLog:function (obj) {
                            onSendLogFn(obj);
                        }
                    };
                    window["Venus"].init(config);
                }else{
                    vm.downloadTip(true);
                }
            }.bind(this);
        },
        template : '<div data-bind="if:!downloadTip(),visible:!downloadTip()"><div style="font-size: 18px;padding: 40px 20px;" data-bind="attr:{id: contentId, tip: initQuestion()}"></div>'
                    + '<p style="margin-top: 30px;" data-bind="visible:env == \'test\',text:questionIds"></p></div>'
                    + '<div class="w-noData-block" data-bind="if:downloadTip,visible:downloadTip">'
                    + '如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，'
                    + '<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a>'
                    + '</div>'
    });

}($17,ko));