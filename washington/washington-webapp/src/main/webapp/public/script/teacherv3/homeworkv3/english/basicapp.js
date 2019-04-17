(function($17,ko) {
    "use strict";

    ko.bindingHandlers.singleAppHover = {
        init: function(element, valueAccessor){
            var $element = $(element);
            var value = valueAccessor();
            var valueUnwrapped = ko.unwrap(value);
            if($element.hasClass("previewText")){
                $element.hover(
                    function(){
                        $(element).find("div.preview").show();
                    },
                    function(){
                        $(element).find("div.preview").hide();
                    }
                );
            }else if($element.hasClass("operateBtn")){
                $element.hover(
                    function(){
                        if(!valueUnwrapped){
                            $element.closest("li").addClass("hover");
                        }
                    },
                    function(){
                        $element.closest("li").removeClass("hover");
                    }
                );
            }
        },
        update: function(element, valueAccessor, allBindings, viewModel, bindingContext) {
            var $element = $(element);
            if($element.hasClass("operateBtn")){
                var value = valueAccessor();
                var valueUnwrapped = ko.unwrap(value);
                var $li = $element.closest("li");
                if(valueUnwrapped){
                    $li.addClass("active");
                }else{
                    $li.removeClass("active");
                }
            }

        }
    };

    var BasicApp = function(){
        var self = this;
        self.packageList = ko.observableArray([]);
        self.focusIndex = ko.observable(null);
        self.focusIndex.subscribe(function(index){
            if(typeof index === "number" && index >= 0){
                var content = self.packageList()[index];
                var lessons = content.lessons || [];
                lessons = self.extendCategoryProp(lessons);
                self.contentList(ko.mapping.fromJS(lessons)());
            }else{
                self.contentList([]);
            }
        });
        self.focusPackage = ko.pureComputed(function(){
            var focusIndex = self.focusIndex();
            if(typeof focusIndex === "number" && focusIndex >= 0){
                return self.packageList()[self.focusIndex()];
            }else{
                return null;
            }
        });
        self.ctLoading = ko.observable(false);
        self.contentList = ko.observableArray([]);
        self.tabType = "BASIC_APP";
    };
    BasicApp.prototype = {
        constructor     : BasicApp,
        param           : {},
        categoryIconPrefixUrl : null,
        resetBasicApp   : function(){
            var self = this;
            self.focusIndex(null);
        },
        run             : function () {
            var self = this,paramData = {
                bookId   : self.param.bookId,
                unitId   : self.param.unitId,
                sections : "", //[].toString()
                type     : self.tabType,
                subject  : constantObj.subject,
                objectiveConfigId : self.param.objectiveConfigId
            };
            self.ctLoading(true);
            self.resetBasicApp(null);
            $.get("/teacher/new/homework/objective/content.vpage",paramData,function(data){
                if(data.success){
                    var contents = self.extendCategoryProp(data.content);
                    self.packageList(contents);
                    if(contents.length > 0){
                        self.viewPackage.call(contents[0],self,0);
                    }
                }else{
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
                self.ctLoading(false);
            });
        },
        viewPackage  : function(self,index){
            self.focusIndex(index);
        },
        extendCategoryProp : function(content){
            content = $.isArray(content) ? content : [];
            var _apps = constantObj._homeworkContent.practices.BASIC_APP.apps || [];
            var lessonCategoryIds = [];
            for(var m = 0,mLen = _apps.length; m < mLen; m++){
                lessonCategoryIds.push(_apps[m].lessonId + ":" + (_apps[m]["categoryId"] || ""));
            }

            for(var i = 0, iLen = content.length; i < iLen; i++){
                var _categroyList = content[i].categories || [];
                for(var t = 0, tLen = _categroyList.length; t < tLen; t++){
                    var lessonCategory = content[i].lessonId + ":" + _categroyList[t].categoryId;
                    _categroyList[t]["checked"] = (lessonCategoryIds.indexOf(lessonCategory) != -1);
                }
            }
            return content;
        },
        covertSentences    : function(sentences){
            if(!$.isArray(sentences)){
                return "";
            }
            return sentences.join(" / ");
        },
        getCategroyIconUrl : function(categroyIcon){
            var self = this;
            categroyIcon = +categroyIcon || 50000;
            return self.categoryIconPrefixUrl + "e-icons-" + categroyIcon + ".png";
        },
        categoryPreview: function(lessonId,self){
            var categoryKO = this;
            var practices = categoryKO.practices() || [];
            if(practices.length <= 0){
                $17.alert("没有相应类别应用,暂不能预览");
                return false;
            }
            var questions = practices[0].questions() || [];
            if(questions.length <= 0){
                $17.alert("没有配相应的应试题,暂不能预览");
                return false;
            }
            var qIds = [];
            for(var t = 0, tLen = questions.length; t < tLen; t++){
                qIds.push(questions[t].questionId());
            }
            var paramObj = {
                qids : qIds.join(","),
                lessonId : lessonId,
                practiceId : practices[0].practiceId(),
                fromModule : ""
            };
            var gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
            var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

            $.prompt(data, {
                title   : "预 览",
                buttons : {},
                position: { width: 740 },
                close   : function(){
                    $('iframe').each(function(){
                        var win = this.contentWindow || this;
                        if(win.destroyHomeworkJavascriptObject){
                            win.destroyHomeworkJavascriptObject();
                        }
                    });
                }
            });
            //布置作业预览基础练习
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "page_assign_BasicPractice_preview_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : categoryKO.categoryId()
            });

            return false;
        },
        updateUfoApp : function(sec,questionCnt){
            var self = this;
            constantObj._moduleSeconds[self.tabType] = constantObj._moduleSeconds[self.tabType] + sec;

            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(self.tabType,questionCnt);
        },
        addCategory     : function(parent,self){
            var categoryKO = this;
            var param = self.param || {};
            if(categoryKO.practices){
                var practices = categoryKO.practices();
                var _apps = constantObj._homeworkContent.practices.BASIC_APP.apps;
                if(practices && $.isArray(practices) && practices[0].questions){
                    var _questions = ko.mapping.toJS(practices[0].questions());
                    var _lessonId = parent.lessonId();
                    var _categoryId = categoryKO.categoryId();
                    _apps.push({
                        "practiceCategory"  : categoryKO.categoryName(),
                        "categoryId"        : categoryKO.categoryId(),
                        "practiceId"        : practices[0].practiceId(),
                        "practiceName"      : practices[0].practiceName(),
                        "lessonId"          : _lessonId,
                        "questions"         : _questions,
                        "book"              : ko.mapping.toJS(categoryKO.book),
                        "objectiveId"       : param.objectiveTabType
                    });

                    categoryKO.checked(true);

                    var sec = 0;
                    for(var t = 0,tLen = _questions.length; t < tLen; t++){
                        sec += (+_questions[t].seconds || 0);
                    }
                    self.updateUfoApp(sec,_apps.length);

                    var reviewObj = $.extend(true,{},ko.mapping.toJS(this));
                    reviewObj.lessonName = parent.lessonName();
                    reviewObj.lessonId = parent.lessonId();
                    reviewObj.sentences = parent.sentences().join("/");
                    constantObj._reviewQuestions["BASIC_APP"].push(reviewObj);
                }else{
                    $17.info("应用未配题");
                }
            }else{
                $17.info("应用不存在或应用未配题");
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "page_assign_BasicPractice_select_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : categoryKO.categoryId()
            });
        },
        removeCategory : function(parent,self){
            var categoryKO = this;
            var categoryId = categoryKO.categoryId();

            var _apps = constantObj._homeworkContent.practices.BASIC_APP.apps;
            var zIndex = -1;
            var sec = 0;
            var _lessonId = parent.lessonId();
            for(var k = 0,kLen = _apps.length; k < kLen; k++){
                if(_apps[k].categoryId === categoryId && _lessonId === _apps[k].lessonId){
                    zIndex = k;
                    var questions = _apps[k].questions;
                    for(var t = 0,tLen = questions.length; t < tLen; t++){
                        sec += (+questions[t].seconds || 0);
                    }
                    break;
                }
            }
            if(zIndex != -1){
                _apps.splice(zIndex,1);
                self.updateUfoApp(0 - sec, _apps.length);

                $.each(constantObj._reviewQuestions["BASIC_APP"],function(i){
                    if(this.categoryId == categoryId){
                        constantObj._reviewQuestions["BASIC_APP"].splice(i,1);
                        return false;
                    }
                });

            }else{
                $17.info("未在购物车找到，忽略");
            }
            categoryKO.checked(false);

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "page_assign_BasicPractice_remove_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : categoryKO.categoryId()
            });
        },
        initialise      : function (option) {
            var self = this;
            option = option || {};
            self.param = option;
            self.categoryIconPrefixUrl = option.categoryIconPrefixUrl || null;

            self.carts = option.carts || null;
            //初始化
            var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
            if($ufoexam.has("span").length == 0){
                $ufoexam.empty().html(template("t:UFO_BASIC_APP",{tabTypeName : option.tabTypeName,count : 0}));
            }
        }
    };
    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getBasic_app: function(){
            return new BasicApp();
        }
    });
}($17,ko));