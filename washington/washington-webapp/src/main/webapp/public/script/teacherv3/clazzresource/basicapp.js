(function($17,ko) {
    "use strict";
    var module = constantObj.logModule || "m_aHrND8yNXX";
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
                subject  : constantObj.subject
            };
            self.ctLoading(true);
            self.resetBasicApp(null);
            $.get("/teacher/new/homework/content.vpage",paramData,function(data){
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
            var lessonCategoryIds = [];
            for(var i = 0, iLen = content.length; i < iLen; i++){
                var _categroyList = content[i].categories || [];
                for(var t = 0, tLen = _categroyList.length; t < tLen; t++){
                    var lessonCategory = content[i].lessonId + ":" + _categroyList[t].categoryId;
                    _categroyList[t]["checked"] = (lessonCategoryIds.indexOf(lessonCategory) !== -1);
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
                module: module,
                op    : "resource_basicpreview_click",
                s0    : categoryKO.categoryId()
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
        initialise      : function (option) {
            var self = this;
            option = option || {};
            self.param = option;
            self.categoryIconPrefixUrl = option.categoryIconPrefixUrl || null;

        }
    };
    $17.clazzresource = $17.clazzresource || {};
    $17.extend($17.clazzresource, {
        getBasic_app: function(){
            return new BasicApp();
        }
    });
}($17,ko));