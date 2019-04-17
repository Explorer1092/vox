(function($17,ko){
    "use strict";

    function WordTeachAndPractice(){
        var self = this;
        self.packageList = ko.observableArray([]);
        self.newConfig = {};
        self.clazzGroupIdsStr = "";
    }
    WordTeachAndPractice.prototype = {
        constructor : WordTeachAndPractice,
        initialise : function(config){
            var self = this;
            var newConfig = $.extend(true,{},config);
            self.newConfig = newConfig;
            self.carts = newConfig.carts || null;

            var $ufo = $("p[type='" + newConfig.tabType +"']",".J_UFOInfo");
            if($ufo.has("span").length === 0){
                $ufo.empty().html([
                    "<span class=\"name\">"+ newConfig.tabTypeName + "</span>" +
                    "<span class=\"count\" data-count=\"0\">0</span>" +
                    "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join(""));
            }
        },
        run : function (option) {
            var self = this,newConfig = self.newConfig;
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
            option = option || {};
            self.clazzGroupIdsStr = option.clazzGroupIdsStr || "";
            $.get("/teacher/new/homework/objective/content.vpage",paramData,function(data){
                if(data.success && data.content){
                    var boxQuestionMap = self.getSelectBoxByCart();
                    $.each(data.content,function () {
                        $.each(this.stoneData,function (index) {
                            var packageInfo = this;
                            var selectModules = boxQuestionMap[packageInfo.stoneDataId] || [];
                            var wordsPractice = packageInfo.wordsPractice || {};
                            var packageChecked = boxQuestionMap.hasOwnProperty(packageInfo.stoneDataId);
                            if(packageChecked){
                                //如果购物车有此题包，看题包下的三个模块是否都包含
                                $.each(["wordExerciseMap","imageTextMap","chineseCharacterCultureMap"]).each(function(zIndex,prop){
                                    if(wordsPractice.hasOwnProperty(prop)){
                                        packageChecked = packageChecked && selectModules.indexOf(wordsPractice[prop]["questionBoxType"]) !== -1;
                                    }
                                });
                            }
                            packageInfo.stoneDataTitle = packageInfo.stoneDataTitle || ('题包' + (index + 1));
                            packageInfo.packageChecked = ko.observable(packageChecked);
                            packageInfo.selectModules = ko.observableArray(selectModules);
                        });
                    });
                    self.packageList(data.content);
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
        getSelectBoxByCart : function(){
            var self = this,newConfig = self.newConfig;
            var boxQuestionMap = {};
            $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps, function () {
                var selInfo = this;
                var boxId = selInfo.stoneDataId || "undefined";
                var questionBoxTypes = [],practiceTypes = selInfo["practiceTypes"] || [];
                for(var m = 0,mLen = practiceTypes.length; m < mLen; m++){
                    questionBoxTypes.push(practiceTypes[m].type);
                }
                boxQuestionMap[boxId] = questionBoxTypes;
            });
            return boxQuestionMap;
        },
        fetchModuleObj : function(type,seconds){
            //向购题车放模块时，题包下模块转换新对象
            return {
                type : type || "unknown",
                seconds : seconds || 0
            }
        },
        addPackage : function (self,parent) {
            var packageObj = this,
                newConfig = self.newConfig,book = parent.book;

            var wordPractice = packageObj.wordsPractice;
            var newSelectModules = [];
            var questionBoxType,seconds; //模块类型与时间
            if(wordPractice.chineseCharacterCultureMap){
                //汉字文化
                questionBoxType = wordPractice.chineseCharacterCultureMap.questionBoxType;
                seconds = (wordPractice.chineseCharacterCultureMap.seconds || 0);
                newSelectModules.push(self.fetchModuleObj(questionBoxType,seconds));
            }

            if(wordPractice.imageTextMap){
                //图文入韵
                questionBoxType = wordPractice.imageTextMap.questionBoxType;
                seconds = (wordPractice.imageTextMap.seconds || 0);
                newSelectModules.push(self.fetchModuleObj(questionBoxType,seconds));
            }

            if(wordPractice.wordExerciseMap){
                 //字词训练
                questionBoxType = wordPractice.wordExerciseMap.questionBoxType;
                seconds = (wordPractice.wordExerciseMap.seconds || 0);
                newSelectModules.push(self.fetchModuleObj(questionBoxType,seconds));
            }

            //清除
            $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps,function (index) {
                if(this.stoneDataId === packageObj.stoneDataId){
                    $.each(this.practiceTypes,function(){
                        constantObj._moduleSeconds[newConfig.tabType] -= this.seconds;
                    });

                    constantObj._homeworkContent.practices[newConfig.tabType].apps.splice(index,1);

                    $.each(constantObj._reviewQuestions[newConfig.tabType],function (index) {
                        if(this.stoneDataId === packageObj.stoneDataId){
                            constantObj._reviewQuestions[newConfig.tabType].splice(index,1);
                            return false;
                        }
                    });
                    return false;
                }
            });

            var totalTime = 0;
            for(var t = 0,tLen = newSelectModules.length; t < tLen; t++){
                totalTime += newSelectModules[t].seconds;
            }

            if(!this.packageChecked()){
                constantObj._homeworkContent.practices[newConfig.tabType].apps.push({
                    stoneDataId: packageObj.stoneDataId,
                    book : book,
                    practiceTypes: newSelectModules,
                    objectiveId: newConfig.objectiveTabType
                });
                constantObj._moduleSeconds[newConfig.tabType] += totalTime;


                var moduleTypes = newSelectModules.map(function(obj){
                    return obj.type;
                });
                var newPackageObj = $.extend(true,{
                    id : parent.id,
                    sectionName : parent.sectionName
                },packageObj);
                newPackageObj = ko.mapping.toJS(newPackageObj);
                newPackageObj.selectModules = [].concat(moduleTypes);
                constantObj._reviewQuestions[newConfig.tabType].push(newPackageObj);
                this.selectModules(moduleTypes);
            }else{
                this.selectModules([]);
            }

            this.packageChecked(!this.packageChecked());

            $17.voxLog({
                module : "m_H1VyyebB",
                op : "word_training_package_select_click",
                s0 : self.clazzGroupIdsStr,
                s1 : this.packageChecked() ? "选入" : "移除"
            });
            self.reSetUFO();
        },
        moduleClick : function(packageObj,self,parent){
            var moduleObj = this;
            var selectModules = packageObj.selectModules;
            var moduleIndex = selectModules.indexOf(moduleObj.questionBoxType);
            var newConfig = self.newConfig;
            var btnText;
            if(moduleIndex === -1){
                btnText = "选入";
                //处理购题车
                var stoneDataIndex = -1;
                $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps,function (index) {
                    (this.stoneDataId === packageObj.stoneDataId) && (stoneDataIndex = index);
                });

                var moduleSeconds = (moduleObj.seconds || 0);
                if(stoneDataIndex === -1){
                    constantObj._homeworkContent.practices[newConfig.tabType].apps.push({
                        stoneDataId: packageObj.stoneDataId,
                        book : parent.book,
                        practiceTypes: [self.fetchModuleObj(moduleObj.questionBoxType,moduleSeconds)],
                        objectiveId: newConfig.objectiveTabType
                    });
                    constantObj._moduleSeconds[newConfig.tabType] += moduleSeconds;

                    var newPackageObj = ko.mapping.toJS(packageObj);
                    newPackageObj = $.extend(true,{
                        id : parent.id,
                        sectionName : parent.sectionName
                    },newPackageObj);
                    newPackageObj.selectModules.push(moduleObj.questionBoxType);
                    constantObj._reviewQuestions[newConfig.tabType].push(newPackageObj);
                }else{
                    //初始化过了购题车,只需要添加模块即可
                    $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps,function (index) {
                        if(this.stoneDataId === packageObj.stoneDataId){
                            this.practiceTypes.push(self.fetchModuleObj(moduleObj.questionBoxType,moduleSeconds));
                            constantObj._moduleSeconds[newConfig.tabType] += moduleSeconds;

                            $.each(constantObj._reviewQuestions[newConfig.tabType],function (index) {
                                if(this.stoneDataId === packageObj.stoneDataId){
                                    this.selectModules.push(moduleObj.questionBoxType);
                                    return false;
                                }
                            });
                            return false;
                        }
                    });
                }
                //选入操作
                selectModules.push(moduleObj.questionBoxType);
            }else{
                btnText = "移除";
                //选择过模块，要做移除模块操作
                $.each(constantObj._homeworkContent.practices[newConfig.tabType].apps,function (zIndex) {
                    if(this.stoneDataId === packageObj.stoneDataId){
                        var practiceIndex = -1;
                        for(var m = 0,mLen = this.practiceTypes.length; m < mLen; m++){
                            if(this.practiceTypes[m].type === moduleObj.questionBoxType){
                                practiceIndex = m;
                                break;
                            }
                        }
                        (practiceIndex >= 0) && (this.practiceTypes.splice(practiceIndex,1));
                        constantObj._moduleSeconds[newConfig.tabType] -= (moduleObj.seconds || 0);

                        (this.practiceTypes.length === 0) && (constantObj._homeworkContent.practices[newConfig.tabType].apps.splice(zIndex,1));

                        $.each(constantObj._reviewQuestions[newConfig.tabType],function (index) {
                            if(this.stoneDataId === packageObj.stoneDataId){
                                moduleIndex = this.selectModules.indexOf(moduleObj.questionBoxType);
                                (moduleIndex > -1) && (this.selectModules.splice(moduleIndex,1));
                                (this.selectModules.length === 0) && (constantObj._reviewQuestions[newConfig.tabType].splice(index,1));
                                return false;
                            }
                        });
                        return false;
                    }
                });
                selectModules.splice(moduleIndex,1);
            }
            var wordsPractice = packageObj.wordsPractice || {};
            //模块都选择了，就把题包状态置为选中
            var moduleArr = [];
            if(wordsPractice.chineseCharacterCultureMap){
                moduleArr.push(wordsPractice.chineseCharacterCultureMap["questionBoxType"]);
            }
            if(wordsPractice.imageTextMap){
                moduleArr.push(wordsPractice.imageTextMap["questionBoxType"]);
            }
            if(wordsPractice.wordExerciseMap){
                moduleArr.push(wordsPractice.wordExerciseMap["questionBoxType"]);
            }
            if(moduleArr.length === 0){
                $17.alert("模块类型为空");
                return false;
            }
            var selectAll = false;
            if(moduleArr.length <= selectModules().length){
                var selectModuleArr = selectModules();
                selectAll = true;
                for(var m = 0,mLen = selectModuleArr.length; m < mLen; m++){
                    selectAll = selectAll && (moduleArr.indexOf(selectModuleArr[m]) !== -1);
                }
            }
            packageObj.packageChecked(selectAll);


            $17.voxLog({
                module : "m_H1VyyebB",
                op : "word_training_module_select_click",
                s0 : self.clazzGroupIdsStr,
                s1 : moduleObj.questionBoxType,
                s2 : btnText
            });
            self.reSetUFO();
        },
        reSetUFO : function(){
            var self = this,newConfig = self.newConfig,
                count = constantObj._homeworkContent.practices[newConfig.tabType].apps.length;

            self.carts && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(newConfig.tabType,count);
        },
        previewModule : function(packageObj,self,parent){
            var moduleObj = this;
            switch (moduleObj.questionBoxType){
                case "CHINESECHARACTERCULTURE":
                    //汉字文化
                    self._chinesePreivew.call(this,packageObj,self,parent);
                    break;
                case "IMAGETEXTRHYME":
                     //图文入韵
                    self._imageTextPreview.call(this,packageObj,self,parent);
                    break;
                case "WORDEXERCISE":
                      //字词训练
                    self._wordExercisePreview.call(this,packageObj,self,parent);
                    break;
                default:
                    break;
            }
        },
        _chinesePreivew : function(packageObj,self,parent){
            var moduleObj = this;
            $.prompt(template("t:CHINESECHARACTERCULTURE_PREVIEW",{}), {
                title    : moduleObj.questionBoxTypeTitle,
                buttons  : {},
                position : { width: 895},
                close    : function () {},
                loaded : function(){
                    ko.applyBindings($.extend(true,{},moduleObj,{
                        questionBoxTypeTitle : moduleObj.questionBoxTypeTitle,
                        selectModules : packageObj.selectModules,
                        focusId : ko.observable(null),
                        addPackage : function(){
                            self.moduleClick.call(moduleObj,packageObj,self,parent);
                        },
                        switchMode : function(courseId){
                            this.focusId(courseId);
                            $17.voxLog({
                                module : "m_H1VyyebB",
                                op : "word_course_list_item_click",
                                s0 : self.clazzGroupIdsStr
                            });
                        }
                    }), document.getElementById('jqistate_state0'));

                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op : "word_course_list_load",
                        s0 : self.clazzGroupIdsStr
                    });
                }
            });
        },
        _imageTextPreview : function(packageObj,self,parent){
            var moduleObj = this;
            $.prompt(template("t:IMAGETEXTRHYME_PREVIEW",{}), {
                title    : moduleObj.questionBoxTypeTitle,
                buttons  : {},
                position : { width: 895},
                close    : function () {},
                loaded : function(){
                    window.addEventListener("message",function(e){
                        $.prompt.close();
                    });
                    ko.applyBindings($.extend(true,{},moduleObj,{
                        questionBoxTypeTitle : moduleObj.questionBoxTypeTitle,
                        doUrl : moduleObj.doUrl,
                        selectModules : packageObj.selectModules,
                        focusId    : ko.observable(null),
                        addPackage : function(){
                            self.moduleClick.call(moduleObj,packageObj,self,parent);
                        },
                        fetchPreviewUrl : function(){
                            var self = this;
                            var domain;
                            if($uper.env === "test"){
                                domain = "//www.test.17zuoye.net/";
                            }else{
                                domain = location.protocol + "//" + location.host;
                            }
                            var params = {
                                doModuleUrl : self.doUrl,
                                chapterId : self.focusId()
                            };
                            return domain + "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-speak-training/read.vhtml?" + $.param({
                                    __p__ : JSON.stringify(params)
                                });
                        },
                        switchMode : function(courseId){
                            this.focusId(courseId);
                            $17.voxLog({
                                module : "m_H1VyyebB",
                                op : "graphic_reading_list_item_click",
                                s0 : self.clazzGroupIdsStr
                            })
                        }
                    }), document.getElementById('jqistate_state0'));
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op : "graphic_reading_list_load",
                        s0 : self.clazzGroupIdsStr
                    });
                }
            });
        },
        _wordExercisePreview : function(packageObj,self,parent){
            var moduleObj = this;

            var questionList = moduleObj.questions;
            if(!questionList || questionList.length === 0){
                $17.alert("没有应试题目,请反馈给客服");
                return false;
            }

            var questionIds = [];
            for(var m = 0,mLen = questionList.length; m < mLen; m++){
                questionIds.push(questionList[m].id);
            }
            $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                var focusExamMap = result.success ? result.questionMap : {};

                $.prompt(template("t:WORDEXERCISE_PREVIEW",{}), {
                    title    : moduleObj.questionBoxTypeTitle,
                    buttons  : {},
                    position : { width: 895},
                    close    : function () {},
                    loaded : function(){
                        ko.applyBindings($.extend(true,{},moduleObj,{
                            questionBoxTypeTitle : moduleObj.questionBoxTypeTitle,
                            selectModules : packageObj.selectModules,
                            focusId    : ko.observable(null),
                            addPackage : function(){
                                self.moduleClick.call(moduleObj,packageObj,self,parent);
                            },
                            getQuestion : function(questionId){
                                var questionObj = focusExamMap[questionId];
                                if(!questionObj){
                                    return 	[];
                                }
                                var questions = questionObj.questions;
                                if(!$.isArray(questions) || questions.length === 0){
                                    return [];
                                }
                                return questions.slice(0,1);
                            },
                            switchMode : function(courseId){
                                this.focusId(courseId);
                            },
                            onSendLog : function(obj){
                                obj = obj || {};
                                $17.voxLog({
                                    module : "m_H1VyyebB",
                                    op : obj.type || "word_question_list_clue",
                                    s0 : self.clazzGroupIdsStr,
                                    s1 : obj.qobj && obj.qobj.id
                                })
                            }
                        }), document.getElementById('jqistate_state0'));

                        $17.voxLog({
                            module : "m_H1VyyebB",
                            op : "word_question_list_load",
                            s0 : self.clazzGroupIdsStr
                        });
                    }
                });
            });
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getWord_teach_and_practice : function(){
            return new WordTeachAndPractice();
        }
    });
}($17,ko));