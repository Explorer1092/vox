(function (window,$17,constantObj,undefined) {
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

    function TermBasicApp(obj,termCarts){
        var self = this;
        self.termCarts = termCarts || {};
        self.ctLoading = ko.observable(true);
        self.contentList = ko.observableArray([]);
        self.homeworkType = "BASIC_APP";
        self.param = {
            bookId : null,
            bookName : ""
        }; //param是添加题目时使用的
    }
    TermBasicApp.prototype = {
        constructor : TermBasicApp,
        run         : function(data){
            if(!data || !data.success){
                return false;
            }
            for(var p in this.param){
                if(this.param.hasOwnProperty(p)){
                    this.param[p] = data[p] || null;
                }
            }
            var self = this, paramData = {
                subject      : constantObj.subject,
                type         : data.type,
                bookId       : data.bookId,
                unitIds      : data.unitIds.join(","),
                clazzGroupId : data.clazzGroupId
            };
            $.get("/teacher/termreview/content.vpage", paramData, function(data){
                 if(data.success){
                     var _apps = [],questionsByHomework = self.termCarts.getQuestionsByHomeworkType;
                     typeof questionsByHomework === 'function' && (_apps = _apps.concat(questionsByHomework(self.homeworkType)));
                     var lessonCategoryIds = []; //通过作业类型获取选中的应用
                     for(var m = 0,mLen = _apps.length; m < mLen; m++){
                         lessonCategoryIds.push([_apps[m].unitId,_apps[m].lessonId,_apps[m].categoryId].join(":"));
                     }
                     var unitsContent = data.content || [];
                     for(var k = 0,kLen = unitsContent.length; k < kLen; k++){
                         var contents = unitsContent[k].unitContent || [];
                         for(var i = 0, iLen = contents.length; i < iLen; i++){
                             var _categoryList = contents[i].categories || [];
                             for(var t = 0, tLen = _categoryList.length; t < tLen; t++){
                                 var lessonCategory = unitsContent[k].unitId + ":" + contents[i].lessonId + ":" + _categoryList[t].categoryId;
                                 _categoryList[t]["checked"] = (lessonCategoryIds.indexOf(lessonCategory) != -1);
                             }
                         }
                     }
                     self.contentList(ko.mapping.fromJS(unitsContent)());
                     self.ctLoading(false);
                 }else{
                     (data.errorCode !== "200") && $17.voxLog({
                         module : "API_REQUEST_ERROR",
                         op     : "API_STATE_ERROR",
                         s0     : "/teacher/termreview/content.vpage",
                         s1     : $.toJSON(data),
                         s2     : $.toJSON(paramData),
                         s3     : constantObj.env
                     });
                 }
            });
        },
        covertSentences    : function(sentences){
            if(!$.isArray(sentences)){
                return "";
            }
            return sentences.join(" / ");
        },
        getCategoryIconUrl : function(categoryIcon){
            categoryIcon = +categoryIcon || 9;
            return constantObj.basicIconPrefixUrl + "e-icons-" + categoryIcon + ".png";
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
            return false;
        },
        addOrRemoveCategory : function(parents,self,checked){
            var categoryKO = this
                ,unitKO = parents[1]
                ,lessonKO = parents[0]
                ,practiceKO = null
                ,questions = null
                ,content = null;
            if(!categoryKO.practices || !$.isArray(categoryKO.practices()) || categoryKO.practices().length == 0){
                $17.info("没有practices字段");
                return false;
            }
            practiceKO = categoryKO.practices()[0];
            if(!practiceKO.questions || !$.isArray(practiceKO.questions()) || practiceKO.questions().length == 0){
                $17.info("没有questions字段");
                return false;
            }
            questions = practiceKO.questions();
            content = {
                type                : self.homeworkType,
                practice            : {
                    bookId              : self.param.bookId,
                    bookName            : self.param.bookName,
                    unitId              : unitKO.unitId(),
                    unitName            : unitKO.unitName(),
                    lessonId            : lessonKO.lessonId(),
                    lessonName          : lessonKO.lessonName(),
                    sentences           : self.covertSentences(lessonKO.sentences()),
                    categoryId          : categoryKO.categoryId(),
                    categoryIcon        : categoryKO.categoryIcon(),
                    practiceCategory    : categoryKO.categoryName(),
                    teacherAssignTimes  : categoryKO.teacherAssignTimes(),
                    practiceId          : practiceKO.practiceId(),
                    practiceName        : practiceKO.practiceName(),
                    questions           : ko.mapping.toJS(questions)
                }
            };
            checked ? self.termCarts.addQuestion(content) : self.termCarts.removeQuestion(content);
            categoryKO.checked(checked);
        }
    };

    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getBasic_app : function(obj,termCarts){
            return new TermBasicApp(obj,termCarts);
        }
    });
}(window,$17,constantObj));