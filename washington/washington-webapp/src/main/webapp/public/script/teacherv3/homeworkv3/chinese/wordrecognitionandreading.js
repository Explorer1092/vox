(function($17,ko){
    "use strict";

    function WordRecognitionAndReading(){
        var self = this;
        self.packageList = ko.observableArray([]);
    }
    var newConfig;
    WordRecognitionAndReading.prototype = {
        constructor : WordRecognitionAndReading,
        initialise : function(config){
            var self = this;
            newConfig = $.extend(true,{},config);
            self.carts = newConfig.carts || null;

            var $ufo = $("p[type='" + newConfig.tabType +"']",".J_UFOInfo");
            if($ufo.has("span").length === 0){
                $ufo.empty().html([
                    "<span class=\"name\">"+ newConfig.tabTypeName + "</span>" +
                    "<span class=\"count\" data-count=\"0\">0</span>" +
                    "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join(""));
            }
        },
        run : function () {
            var self = this;
            var _sectionIds = $.map(newConfig.sections,function(item){
                return item.sectionId;
            }),paramData = {
                sections : _sectionIds.join(","),
                type     : newConfig.tabType,
                unitId   : newConfig.unitId,
                bookId   : newConfig.bookId,
                subject  : constantObj.subject,
                objectiveConfigId : newConfig.objectiveConfigId
            };
            $.get("/teacher/new/homework/objective/content.vpage",paramData,function(data){
                if(data.success && data.content){
                    var packageList = [];
                    $.each(data.content,function () {
                        if(this.type === "package"){
                            var boxQuestionMap = {};
                            $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps, function () {
                                var selInfo = this;
                                var boxId = selInfo.questionBoxId || "undefined";
                                var questionArr = boxQuestionMap[boxId] || [];
                                $.each(selInfo.questions,function(){
                                    var selQuestion = this;
                                    var questionId = selQuestion.questionId;
                                    (questionArr.indexOf(questionId) === -1) && questionArr.push(questionId);
                                });
                                boxQuestionMap[boxId] = questionArr;
                            });

                            $.each(this.packages,function () {
                                var packageInfo = this;
                                packageInfo.packageChecked = boxQuestionMap.hasOwnProperty(packageInfo.questionBoxId);
                            });
                            packageList = [].concat(this.packages);
                        }
                    });
                    self.packageList(ko.mapping.fromJS(packageList)());
                }else{
                    self.packageList([]);
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
            });
        },
        addPackage : function (self) {
            var packageKO = this,
                totalTime = 0,
                content = [],
                data = ko.mapping.toJS(packageKO);

            $.each(data.questions,function () {
                totalTime += this.seconds;
                content.push({
                    questionId : this.id,
                    seconds    : this.seconds,
                    submitWay  : this.submitWay
                });
            });

            $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps,function (index) {
                if(this.questionBoxId === data.questionBoxId){

                    $.each(this.questions,function () {
                        constantObj._moduleSeconds[newConfig.tabType] -= this.seconds;
                    });
                    constantObj._homeworkContent.practices[newConfig.tabType].apps.splice(index,1);

                    $.each(constantObj._reviewQuestions[newConfig.tabType],function (index) {
                        if(this.questionBoxId === data.questionBoxId){
                            constantObj._reviewQuestions[newConfig.tabType].splice(index,1);
                            return false;
                        }
                    });
                    return false;
                }
            });

            if(!this.packageChecked()){
                constantObj._homeworkContent.practices[newConfig.tabType].apps.push({
                    questionBoxId   : data.questionBoxId,
                    book            : data.book,
                    questions       : content,
                    objectiveId     : newConfig.objectiveTabType
                });
                constantObj._moduleSeconds[newConfig.tabType] += totalTime;
                constantObj._reviewQuestions[newConfig.tabType].push(data);
            }

            this.packageChecked(!this.packageChecked());
            $17.voxLog({
                module : "m_H1VyyebB",
                op : "read_back_select_click",
                s0 : this.packageChecked() ? "选入" : "移除"
            });
            self.reSetUFO();
        },
        reSetUFO : function(){
            var self = this,
                count = 0;

            $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps,function () {
                count += this.questions.length;
            });

            self.carts && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(newConfig.tabType,count);
        },
        packageDetailPopup : function(rootObj,callback){
            var packageKO = this;
            $.prompt(template("t:viewWordRecognitionDetail",{}), {
                title    : "生字预览",
                buttons  : {},
                position : { width: 960},
                close    : function () {
                    $('body').css('overflow', 'auto');
                    //关闭所有播放
                    /*selfPlayer.stopAll();*/
                    $.isFunction(callback) && callback();
                },
                loaded : function(){
                    ko.applyBindings({
                        lessonName            : packageKO.lessonName(),
                        packageChecked        : packageKO.packageChecked, //弹出窗中【全部选入】接钮状态
                        questionBoxName       : "认读生字",
                        currentQuestions      : packageKO.questions, //这是包下总的题数
                        playingQuestionId     : ko.observable(null),
                        questionLoading      : true,
                        selAllCurrentQuestions : function () {
                            rootObj.addPackage.call(packageKO,rootObj);
                            $17.voxLog({
                                module : "m_H1VyyebB",
                                op : "word_detail_list_page_select_click",
                                s0 : packageKO.packageChecked() ? "选入" : "移除"
                            });
                        },
                        getQuestion : function(questionId){
                            return QuestionDB.getQuestionById(questionId);
                        },
                        playAudio : function(audioParam){
                            audioParam = audioParam || {};
                            var audioUrl = audioParam.audioUrl;
                            if(!audioUrl){
                                return false;
                            }
                            var self = this;
                            var questionId = audioParam.id;
                            var playingQuestionIdFn = self.playingQuestionId;
                            if(questionId !== playingQuestionIdFn()){
                                playingQuestionIdFn(questionId);
                                $17.audioPlayer.playAudio(audioUrl,function(){
                                    playingQuestionIdFn(null);
                                });
                            }else{
                                $17.audioPlayer.stopAudio();
                                playingQuestionIdFn(null);
                            }
                        }
                    }, document.getElementById('jqistate_state0'));
                }
            });
            $17.voxLog({
                module : "m_H1VyyebB",
                op : "read_back_preview_click"
            });
        },
        packageDetail  :function (rootObj) {
            var packageKO = this;
            var questionIds = [];
            ko.utils.arrayForEach(packageKO.questions(),function(question){
                questionIds.push(question.id());
            });
            QuestionDB.addQuestions(questionIds,function(result){
                if(!result.success){
                    $17.alert("获取题目失败");
                }else{
                    rootObj.packageDetailPopup.call(packageKO,rootObj,function(){
                        QuestionDB.deleteQuestions(questionIds);
                    });
                }
            });
            $('body').css('overflow', 'hidden');

        }
    };

    var QuestionDB = (function(){
        var questionMap = {};   //用来存储每道题的详细信息，即题库数据
        return {
            addQuestions : function(questionIds,callback){
                callback = $.isFunction(callback) ? callback : function(){};
                if(!$.isArray(questionIds) || questionIds.length == 0){
                    callback({
                        success : true,
                        info    : "题目为空"
                    });
                    return false;
                }
                var unLoadQuestionIds = [];
                for(var m = 0,mLen = questionIds.length; m < mLen; m++){
                    !questionMap.hasOwnProperty(questionIds[m]) && unLoadQuestionIds.push(questionIds[m]);
                }
                $.get("/exam/flash/load/newquestion/byids.vpage",{
                    data:JSON.stringify({ids: unLoadQuestionIds,containsAnswer:false})
                }).done(function(res){
                    if(res.success){
                        var result = res.result;
                        for(var t = 0,tLen = result.length; t < tLen; t++){
                            questionMap[result[t].id] = result[t];
                        }
                    }
                    callback({
                        success : res.success
                    });
                }).fail(function(e){
                    callback({
                        success : false,
                        info    : e.message
                    });
                });
            },
            deleteQuestions : function(questionIds){
                if(!$.isArray(questionIds) || questionIds.length === 0){
                    return false;
                }
                for(var m = 0,mLen = questionIds.length; m < mLen; m++){
                    delete questionMap[questionIds[m]];
                }
                return true;
            },
            getQuestionById : function(questionId){
                return questionMap[questionId] || null;
            }
        }
    }());

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getWord_recognition_and_reading : function(){
            return new WordRecognitionAndReading();
        }
    });
}($17,ko));