/*
* 题包+题 结构
* */
!function($17,ko) {
    "use strict";

    var termExam = function(obj,termCarts){
        var self = this;
        self.termType               = obj.type;
        self.homeworkType           = obj.homeworkType;
        self.termCarts              = termCarts;
        self.packageList            = ko.observableArray([]);
        self.currentPackage         = ko.observable({});
        self.currentPackageSelected = ko.observable(false);
        self.silderObj              = {};
        self.questionHandles        = [];//题目预览
        self.currentPackageQidCount = function(type){
            if(self.currentPackage().questions){
                var showCount = 0,totalTime = 0;
                $.each(self.currentPackage().questions(),function(){

                    totalTime += this.seconds();
                    showCount++;
                });
                if(type == "totalTime"){
                    return Math.ceil(totalTime/60) + '分钟完成';
                }else{
                    return '全选' + showCount + '道大题';
                }
            }
        };
        self.pageSize             = 5;
        self.currentPackagePageNo = ko.observable(1);
        self.focusExamList        = ko.pureComputed(function(){

            var pageNo     = self.currentPackagePageNo(),
                startIndex = (pageNo - 1) * self.pageSize,
                endIndex   = pageNo * self.pageSize,
                qids       = [];
            if(self.currentPackage && self.currentPackage().questions){
                $.each(self.currentPackage().questions(),function(){

                    qids.push(this);
                });
            }
            return qids.slice(startIndex, endIndex);
        },self);

        self.termPage = new $17.pagination.initPages({ //初始化null会出现第一次无法加载问题
            totalPage   : 1,
            pageClickCb : self.page_click.bind(self)
        });
    };
    termExam.prototype = {
        constructor : termExam,

        run : function(option){
            var self = this;
            var paramData = {
                type         : option.type,
                bookId       : option.bookId,
                subject      : constantObj.subject,
                clazzGroupIds: option.clazzGroupId
            };
            $.get("/teacher/termreview/content.vpage", paramData, function(data){
                if(data.success && data.packages){

                    $.each(data.packages,function () {
                        this.selCount = 0;
                        $.each(this.questions,function(){

                            this.checked = false;
                        });
                    });

                    self.packageList(ko.mapping.fromJS(data.packages)());
                    self.currentPackage(ko.mapping.fromJS(data.packages[0]));

                    self.setTermPage();
                    self.resetSelectStatus();
                }else{
                    (data.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/termreview/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : constantObj.env
                    });
                    //清除上次查询记录
                    self.packageList([]);
                    self.currentPackage({});
                }
            });
        },
        
        addQuestion: function(element,self,type){
            var data = [];
            if(type == "click_package"){
                data = ko.mapping.toJS(self.currentPackage()).questions;

                $17.voxLog({
                    module : "m_8NOEdAtE",
                    op     : "final_review_packageDetail_selectAll_click",
                    s0     : constantObj.subject,
                    s1     : self.currentPackage().id(),
                    s2     : $(element).hasClass("checked") ? "cancel" : "select"
                });
            }else{
                data[0] = ko.mapping.toJS(this);

                $17.voxLog({
                    module : "m_8NOEdAtE",
                    op     : "final_review_packageDetail_ques_select_click",
                    s0     : constantObj && constantObj.subject,
                    s1     : data[0].id,
                    s2     : $(element).hasClass("cancel") ? "cancel" : "select"
                });
            }
            var questionsInfo = self.resetQuestionData(data,this);

            if($(element).hasClass("cancel") || $(element).hasClass("checked")){
                self.termCarts.removeQuestion(questionsInfo.data);
            }else{
                if(questionsInfo.repeatCount > 0){
                    $17.alert("有" + questionsInfo.repeatCount + "道题与已选题目重复")
                }
                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo",
                    border: "5px #39f solid",
                    time  : 600
                });
                self.termCarts.addQuestion(questionsInfo.data);
            }

            self.resetSelectStatus();
        },

        resetQuestionData : function(data,questionInfo){
            var self = this, questions = [],repeatCount = 0;
            var selQuestions  = this.termCarts.getQuestionsByHomeworkType(self.homeworkType);

            $.each(data,function(){
                var that = this;

                $.each(selQuestions, function () {
                    if (this.questionId == that.id) {
                        repeatCount++
                    }
                });
                questions.push({
                    sourceType        : self.termType,
                    bookId            : this.book.bookId,
                    unitId            : this.book.unitId,
                    sectionId         : this.book.sectionId,
                    questionBoxId     : self.currentPackage().id(),
                    questionId        : this.id,
                    similarQuestionId : this.similarQuestionIds && this.similarQuestionIds[0],
                    seconds           : this.seconds,
                    assignTimes       : this.assignTimes,
                    difficulty        : this.difficulty,
                    difficultyName    : this.difficultyName,
                    questionType      : this.questionType
                });
            });

            return {
                repeatCount : repeatCount,
                data : {
                    type       : this.homeworkType,
                    questions  : questions
                }
            }
        },

        resetSelectStatus : function(){
            var selQuestions  = this.termCarts.getQuestionsByHomeworkType(this.homeworkType);
            var currentPackage = this.currentPackage();
            var count = 0;

            if(this.packageList && this.packageList().length > 0){
                $.each(currentPackage.questions(),function(){
                    var that = this;
                    that.checked(false);
                    $.each(selQuestions,function(){
                        if(that.id() == this.questionId && currentPackage.id() == this.questionBoxId){
                            that.checked(true);
                            count++;
                        }
                    });
                });

                $.each(this.packageList(),function(){
                    var that = this,selCount = 0;
                    $.each(selQuestions,function(){
                        if(that.id() && this.questionBoxId == that.id()){
                            selCount++
                        }
                    });
                    that.selCount(selCount);
                });

                this.currentPackageSelected(count == currentPackage.questions().length);
            }
        },

        changePackage : function(element,self){
            if(!$(element).hasClass("active")){
                $(element).addClass("active").siblings().removeClass("active");
                self.currentPackage(this);
                self.setTermPage();
                self.resetSelectStatus();
            }
        },

        viewQuestion : function(self){
            var _questionId = this.id();
            if(self.questionHandles[_questionId]){
                self.questionHandles[_questionId].showAnalysis(_questionId);
            }
        },

        setTermPage   : function(){
            var self = this,
                totalPage = 1;
            if(self.currentPackage && self.currentPackage().questions){
                var count = 0;
                $.each(self.currentPackage().questions(),function(){
                    count++ ;
                });
                totalPage = Math.ceil(count/5);
            }
            if(!self.termPage){
                self.termPage = new $17.pagination.initPages({
                    totalPage   : totalPage,
                    pageClickCb : self.page_click.bind(self)
                });

            }else{
                self.currentPackagePageNo(1);
                self.termPage.setPage(undefined,totalPage);
            }
        },

        page_click     : function(currentPage){
            //翻页回调
            var self = this;
            self.currentPackagePageNo(currentPage);
        },

        initSubject : function(qid){
            var $mathExamImg = $("#subjective_" + qid);
            $mathExamImg.empty();
            $("<div></div>").attr("id","examImg_" + qid).appendTo($mathExamImg);
            var node = document.getElementById("examImg_" + qid);
            var obj = vox.exam.render(node, 'normal', {
                ids       : [qid],
                imgDomain : constantObj.imgDomain,
                env       : constantObj.env,
                domain    : constantObj.domain
            });
            this.questionHandles[qid] = obj;
            return false;
        }
    };

    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getExam: function(obj,termCarts){
            return new termExam(obj,termCarts);
        }
    });
}($17,ko);


