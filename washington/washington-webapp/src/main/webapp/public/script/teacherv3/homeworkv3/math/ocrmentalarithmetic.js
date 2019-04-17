(function($17,ko){
    // maxlength 依赖homework.js中maxlength
    var INPUT_SCHEMA = {
        WRITE : "WRITE",
        READ : "READ"
    };
    var teacherId = window.$uper.userId;
    function OcrMental(){
        var self = this;
        self.workBookId = ko.observable(null);
        self.workBookName = ko.observable("").extend({ maxlength: 100 });
        self.prevWorkBookName = "";
        self.workBookName.subscribe(function(oldValue){
            self.prevWorkBookName = oldValue;
        },self,"beforeChange");
        self.workBookName.subscribe(function(newValue){
            if(self.prevWorkBookName && !newValue){
                self.workBookId(null);
            }
        },self,"change");
        self.homeworkDetailPlaceholder = ko.observable("建议输入练习册页码 例：3-5");
        self.homeworkDetail = ko.observable("").extend({ maxlength: 100 });
        self.schema = ko.observable(INPUT_SCHEMA.WRITE);
        self.totalTime = 300;  //默认5分钟
        self.storageKey = teacherId + "_ocrMentalContent";
    }

    var newConfig;
    OcrMental.prototype = {
        constructor : OcrMental,
        initialise : function(option){
            var self = this;
            newConfig = $.extend(true,{},option);
            self.carts = newConfig.carts || null;

            var $ufo = $("p[type='" + newConfig.tabType +"']",".J_UFOInfo");
            if($ufo.has("span").length == 0){
                $ufo.empty().html([
                    "<span class=\"name\">"+ newConfig.tabTypeName + "</span>" +
                    "<span class=\"count\" data-count=\"0\">0</span>" +
                    "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join(""));
            }
            $ufo = null;
        },
        run : function(){
            var self = this;
            var storage = window.localStorage;
            if(!storage){
                return false;
            }
            var mentalContent = storage[self.storageKey];
            if($17.isBlank(mentalContent)){
                return false;
            }
            try{
                var prevMentalDetail = JSON.parse(mentalContent);
                self.workBookId(prevMentalDetail["workBookId"]);
                self.workBookName(prevMentalDetail["workBookName"]);
                self.homeworkDetailPlaceholder("您上次推荐的练习详情：" + prevMentalDetail["homeworkDetail"]);
            }catch (e) {
                //ignore
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "page_ocr_arithmetic"
            });
        },
        addWorkBookItem : function(){
            var self = this;
            var workBookName = $.trim(self.workBookName());
            var homeworkDetail = $.trim(self.homeworkDetail());
            if($17.isBlank(workBookName) || $17.isBlank(homeworkDetail)){
                $17.alert("请在输入框中填入内容");
                return false;
            }

            var obj = {
                "workBookId": self.workBookId(),
                "workBookName": workBookName,
                "homeworkDetail": homeworkDetail
            };
            var newObj = $.extend(true,{},obj,{
                "book" : {
                    "bookId": newConfig["bookId"],
                    "unitId": newConfig["unitId"]
                },
                "objectiveId": newConfig["objectiveTabType"]
            });
            self._addConstantObj([newObj],self.totalTime);
            self.schema(INPUT_SCHEMA.READ);
            window.localStorage.setItem(self.storageKey,JSON.stringify(obj));
        },
        _addConstantObj : function(contents,totalTime){
            var self = this;
            contents = $.isArray(contents) ? contents : [];
            constantObj._homeworkContent.practices[newConfig.tabType].apps = contents;
            constantObj._moduleSeconds[newConfig.tabType] += totalTime;
            constantObj._reviewQuestions[newConfig.tabType] = contents;
            self.reSetUFO();
        },
        reSetUFO : function(){
            var self = this,count;
            var appList = constantObj._homeworkContent.practices[newConfig.tabType].apps;
            count = $.isArray(appList) ? appList.length : 0;

            self.carts && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(newConfig.tabType,count);
        },
        clearAll : function(){
            var self = this;
            self.schema(INPUT_SCHEMA.WRITE);
        },
        editWorkBookItem : function(){
            var self = this;
            self.schema(INPUT_SCHEMA.WRITE);
            self._addConstantObj([],0 - self.totalTime);
        },
        workBookPopup : function(){
            var rootObj = this;
            var workBookVM = {
                workBooks : ko.observableArray([]),
                focusIndex : ko.observable(-1),
                selectWorkBookClick : function(index,self){
                    self.focusIndex(index);
                },
                getSelectWorkBook : function(){
                    var self = this;
                    var focusIndex = self.focusIndex();
                    if(focusIndex >= 0 && focusIndex < self.workBooks().length){
                        return self.workBooks()[focusIndex];
                    }else{
                        return null;
                    }
                }
            };

            var popState = {
                state0 : {
                    name    : 'load_image',
                    comment : '加载图片',
                    html    : template("t:LOAD_IMAGE",{}),
                    title   : '选择教辅',
                    position: { width : 697},
                    focus   : 1,
                    buttons : {}
                },
                state1 : {
                    name : 'selectWorkBook',
                    comment:'选择教辅页面',
                    title   : '选择教辅',
                    focus  : 1,
                    position: { width : 697},
                    html : template("t:WORK_BOOK_POPUP",{}),
                    buttons: { "确定": true},
                    submit  : function(e,v,m,f){
                        e.preventDefault();
                        var selectWorkBook = workBookVM.getSelectWorkBook();
                        if(selectWorkBook){
                            rootObj.workBookId(selectWorkBook.workBookId);
                            rootObj.workBookName(selectWorkBook.workBookName);
                        }
                        $.prompt.close(true);
                    }
                }
            };
            $.prompt(popState, {
                close    : function () {
                    $('body').css('overflow', 'auto');
                },
                loaded : function(){
                    $.get("/teacher/new/homework/ocrmental/workbook/list.vpage",{
                        bookId  : newConfig["bookId"],
                        subject : constantObj.subject
                    }).done(function(res){
                        if(res.success && res.workBooks.length > 0){
                            workBookVM.workBooks(res.workBooks);
                            $.prompt.goToState("selectWorkBook");
                            ko.applyBindings(workBookVM, document.getElementById("jqistate_selectWorkBook"));
                        }else{

                        }
                    }).fail(function(){

                    });
                }
            });
            $('body').css('overflow', 'hidden');

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "ocr_arithmetic_choose_workbook_click"
            });
        },
        supportTypePopup : function () {
            $.prompt(template("t:OCR_MENTAL_SUPPORT_QUESTION_TYPE",{}), {
                title    : "可批改题型",
                buttons  : {},
                position : { width: 657},
                close    : function () {},
                loaded : function(){}
            });
        },
        viewCourse : function(self){
            var gameUrl = "/teacher/new/homework/previewteachingcourse.vpage?" + $.param({
                courseId : "IDC_10200001200283"  //王志提供的最终课程ID
            });
            var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="393" marginheight="0" scrolling="no" frameborder="0"></iframe>';
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
                },
                loaded : function(){
                    window.addEventListener("message",function(e){
                        $.prompt.close();
                    });
                }
            });

            $17.voxLog({
                module : "m_H1VyyebB",
                op : "ocr_arithmetic_example_course_click"
            });
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getOcr_mental_arithmetic : function(){
            return new OcrMental();
        }
    });
}($17,ko));