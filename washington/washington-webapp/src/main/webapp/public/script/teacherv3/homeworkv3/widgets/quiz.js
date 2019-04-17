!function($17,ko) {
    "use strict";

    var slide = null, config_quiz={}, isInitUFO={};
    var quiz = function(){
        this.content     = ko.observableArray([]);
        this.questions   = [];
        this.packageInfo = ko.observable(null);

        this.currentPageQuestions = ko.observableArray([]);
        this.currentPageQuestionsCount = 5;
        this.currentPage     = ko.observable(1);
        this.userInputPage   = ko.observable(null);
        this.totalPage       = ko.observable(1);

        this.questionHandles = [];
    };
    quiz.prototype = {
        constructor : quiz,
        initialise : function(config){
            var self = this;
            config_quiz = config;
            self.carts = config.carts || null;
            if(!isInitUFO.hasOwnProperty(config_quiz.tabType)){
                isInitUFO[config_quiz.tabType] = false;
                var str = ["<span class=\"name\">"+ config_quiz.tabTypeName + "</span>" +
                "<span class=\"count\" data-count=\"0\">0</span>" +
                "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join("");
                $(".J_UFOInfo p[type='"+config_quiz.tabType+"']").html(str);
            }
        },

        run : function(obj){
            var self = this;
            obj = $.isPlainObject(obj) ? obj : {};
            config_quiz.clazzGroupIdsStr = obj.clazzGroupIdsStr;
            var _sectionIds = $.map(config_quiz.sections,function(item){
                return item.sectionId;
            }),paramData = {
                sections : _sectionIds.join(","),
                type : config_quiz.tabType,
                unitId : config_quiz.unitId,
                bookId : config_quiz.bookId,
                subject : constantObj.subject,
                objectiveConfigId : config_quiz.objectiveConfigId
            };
            $.get("/teacher/new/homework/objective/content.vpage",paramData,function(data){
                if(data.success){
                    var content = data.content || [];
                    if(content.length>0){
                        self.showQuestions(content,0,true);
                        $($("#container-quiz li")[0]).addClass("active");

                        $("#container-quiz").on("click",".slideItem",function(){
                            if($(this).hasClass("active")) return false;

                            $(this).addClass("active").siblings().removeClass("active");
                            var index = $(this).attr("index");
                            self.showQuestions(content,index,false);
                            $17.voxLog({
                                module: "m_H1VyyebB",
                                op : "page_assign_exam_examName_click",
                                s0 : constantObj.subject,
                                s1 : config_quiz.tabType,
                                s3 : $(this).attr("paperid")
                            });
                        });
                        self.initSelectedCount();

                    }else{
                        $(".J_dataLoading").html("没有符合条件的试卷");
                    }
                }else{
                    $(".J_dataLoading").html("数据加载失败");
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

        showQuestions : function(content,index,isFirst){
            var totalTime = 0,selctedCount=0;
            $.each(content[index].questions,function(){
                var item = this;
                totalTime += this.seconds;
                this.isSelected = false;
                $.each(constantObj._reviewQuestions[config_quiz.tabType],function(){
                    if(item.id == this.id && content[index].id == this.paperId && content[index].unitId == this.unitId){
                        item.isSelected = true;
                        return false;
                    }
                });
            });

            this.questions = content[index].questions;
            $.each(this.questions,function(){
                if(this.isSelected){
                    selctedCount += 1;
                }
            });
            if(isFirst){

                this.content(ko.mapping.fromJS(content)());
            }
            this.currentPage(1);
            this.totalPage(Math.ceil(content[index].questions.length/this.currentPageQuestionsCount));
            this.currentPageQuestions(ko.mapping.fromJS(content[index].questions.slice(0,this.currentPageQuestionsCount))());
            this.packageInfo(ko.mapping.fromJS({
                title        : content[index].title,
                totalCount   : content[index].questions.length,
                totalTime    : Math.ceil(totalTime/60),
                paperId      : content[index].id,
                unitId       : content[index].unitId,
                isSelAll     : selctedCount == content[index].questions.length,
                showAssigned : content[index].showAssigned || false
            }));

            this.initSubject(content[index].questions.slice(0,this.currentPageQuestionsCount));
        },

        initSubject : function(data){
            var self = this;
            $.each(data,function(i){
                var $mathExamImg = $("#subjective_" + this.id);
                $mathExamImg.empty();
                $("<div></div>").attr("id","examImg_" + this.id).appendTo($mathExamImg);
                var node = document.getElementById("examImg_" + this.id);
                var obj = vox.exam.render(node, 'normal', {
                    ids       : [this.id],
                    imgDomain : constantObj.imgDomain,
                    env       : constantObj.env,
                    domain    : constantObj.domain
                });
                if(self.questionHandles){
                    self.questionHandles[i] = obj;
                }
                $17.examExposureLog({
                    subject         : constantObj.subject,
                    homeworkType    : config_quiz.tabType,
                    packageId       : self.packageInfo() && self.packageInfo().paperId ? self.packageInfo().paperId() : null,
                    examId          : this.id,
                    clazzGroups     : config_quiz.clazzGroupIdsStr
                });
            });
        },

        initSelectedCount : function(){
            var self = this,selCount = {},contentLen = $.isArray(self.content()) ? self.content().length : 0;
            var index = $("#container-quiz li.active").attr("index");
            $.each(constantObj._reviewQuestions[config_quiz.tabType],function(){
                if(selCount.hasOwnProperty(this.unitId + ":" + this.paperId)){
                    selCount[(this.unitId + ":" + this.paperId)] += 1;
                }else{
                    selCount[(this.unitId + ":" + this.paperId)] = 1;
                }
            });

            $("#container-quiz li").each(function(i){
                var $item = $(this);
                var paperId = $item.attr("paperId");
                var selectCount = selCount[config_quiz.unitId + ":"+paperId] || 0;
                if(selectCount > 0 && selectCount<=99){
                    $item.find("span[class='state']").text(selectCount).show();
                }else if(selectCount>99){
                    $item.find("span[class='state']").text("99+").show();
                }else{
                    if(i < contentLen && self.content()[i].showAssigned()){
                        $item.find("span[class='state']").text("用过").show();
                    }else{
                        $item.find("span[class='state']").text(selectCount).hide();
                    }
                }
                if(selectCount == self.questions.length && index == i){
                    self.packageInfo().isSelAll(true);
                }
            });
        },

        addQuestion : function(data,self,index){
            constantObj._homeworkContent.practices[config_quiz.tabType].questions.push({
                questionId      : data.id,
                seconds         : data.seconds,
                paperId         : self.content()[index].id(),
                submitWay       : data.submitWay,
                book            : data.book,
                objectiveId     : config_quiz.objectiveTabType
            });

            constantObj._moduleSeconds[config_quiz.tabType] += data.seconds;

            var reviewObj = $.extend(true,{
                unitId      : config_quiz.unitId,
                paperId     : self.content()[index].id(),
                paperName   : self.content()[index].title(),
                paperSource : self.content()[index].paperSource()
            },ko.mapping.toJS(data));
            constantObj._reviewQuestions[config_quiz.tabType].push(reviewObj);
        },

        removeQuestion : function(qid,seconds){
            var ischecked = false;
            $.each(constantObj._homeworkContent.practices[config_quiz.tabType].questions,function(i){
                if(this.questionId == qid){
                    ischecked = true;
                    constantObj._homeworkContent.practices[config_quiz.tabType].questions.splice(i,1);
                    return false;
                }
            });
            
            $.each(constantObj._reviewQuestions[config_quiz.tabType],function(i){
                if(this.id == qid){
                    constantObj._reviewQuestions[config_quiz.tabType].splice(i,1);
                    return false;
                }
            });
            if(ischecked){
                constantObj._moduleSeconds[config_quiz.tabType] -= seconds;
            }
        },

        addOrCancelQuiz : function(e,self){
            var data = this,element= e,isRepeat = false;
            var index = $("#container-quiz li.active").attr("index");

            if(!$(element).hasClass("cancel")){
                $.each(constantObj._reviewQuestions[config_quiz.tabType],function(){
                    if(this.id == data.id()){
                        $17.alert("该题目与已选题目重复~");
                        isRepeat = true;
                        return false;
                    }
                });
                if(isRepeat) return false;

                self.addQuestion(ko.mapping.toJS(data),self,index);

                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + config_quiz.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });
                $17.voxLog({
                    module : "m_H1VyyebB",
                    op     : "page_assign_exam_examName_select_click",
                    s0     : constantObj.subject,
                    s1     : config_quiz.tabType,
                    s3     : self.packageInfo().paperId(),
                    s4     : data.id()
                });
            }else{
                self.removeQuestion(data.id(),data.seconds());
                self.packageInfo().isSelAll(false);
                $17.voxLog({
                    module : "m_H1VyyebB",
                    op     : "page_assign_exam_examName_deselect_click",
                    s0     : constantObj.subject,
                    s1     : config_quiz.tabType,
                    s3     : self.packageInfo().paperId(),
                    s4     : data.id()
                });
            }

            data.isSelected(!data.isSelected());
            self.reSetUFO();
            self.initSelectedCount();
        },

        selectAll : function(element){
            if($(element).hasClass("checked")) {
                this.deleteAll();
                $17.voxLog({
                    module : "m_H1VyyebB",
                    op     : "page_assign_exam_examName_deselectAll_click",
                    s0     : constantObj.subject,
                    s1     : config_quiz.tabType,
                    s3     : this.packageInfo().paperId()
                });
                return false;
            }

            var self = this;
            var index = $("#container-quiz li.active").attr("index"),selCount = 0;
            var data = self.questions,isSelectedAll = true,isShowPrompt = false;

            $.each(data,function(i){
                var that = this,isNewItem = true;
                $.each(constantObj._reviewQuestions[config_quiz.tabType],function(){
                    if(this.id == that.id){
                        isNewItem = false;
                        if(this.unitId != config_quiz.unitId || this.paperId != self.content()[index].id()){
                            isSelectedAll = false;
                            isShowPrompt = true;
                            selCount += 1;
                        }
                        return false;
                    }
                });
                if(isNewItem){
                    self.questions[i].isSelected = true;
                    var startIndex = (self.currentPage() - 1)*self.currentPageQuestionsCount;
                    var endIndex = self.currentPage()*self.currentPageQuestionsCount;
                    if(i >= startIndex && i < endIndex){
                        self.currentPageQuestions()[i - startIndex].isSelected(true);
                    }

                    self.addQuestion(data[i],self,index);
                }
            });

            this.reSetUFO();
            self.initSelectedCount();
            if(isSelectedAll){
                this.packageInfo().isSelAll(true);
            }else{
                this.packageInfo().isSelAll(false);
                if(isShowPrompt){
                    $17.alert("有" + selCount + "道题与已选题目重复");
                }
            }
            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "page_assign_exam_examName_selectAll_click",
                s0     : constantObj.subject,
                s1     : config_quiz.tabType,
                s3     : this.packageInfo().paperId()
            });
        },

        deleteAll : function(){
            var self = this;
            this.packageInfo().isSelAll(false);
            $.each(this.questions,function(){
                this.isSelected = false;
                self.removeQuestion(this.id,this.seconds);
            });
            $.each(self.currentPageQuestions(),function(){
                this.isSelected(false);
            });
            self.reSetUFO();
            self.initSelectedCount();
        },

        showFeedback : function(element){
            $(element).addClass('hover');
            $(element).find('.J_viewAnswer').show();
        },

        hideFeedback : function(element){
            $(element).removeClass('hover');
            $(element).find('.J_viewAnswer').hide();
        },

        viewExamAnswer : function(self,index){
            var that = this;
            if(self.questionHandles[index]){
                self.questionHandles[index].showAnalysis(that.id());
            }

            self.sendlog("Newhomework_assign_" + $uper.subject.key,config_quiz.tabType + "_view_the_answer_btn");
        },

        feedback : function(self){
            var that = this;
            $.prompt("<div><span class='text_blue'>如果您发现题目出错了，请及时反馈给我们，感谢您的支持！</span><textarea id='feedbackContent' cols='91' rows='8' style='width: 94%' class='int_vox'></textarea><p class='init text_red'></p></div>", {
                title: "错题反馈", focus: 1, buttons: {"取消": false, "提交": true}, submit: function(e, v){
                    if(v){
                        var feedbackContent = $("#feedbackContent");
                        if($17.isBlank(feedbackContent.val())){
                            feedbackContent.siblings(".init").html("错题反馈不能为空。");
                            feedbackContent.focus();
                            return false;
                        }
                        $.post("/project/examfeedback.vpage", {
                            feedbackType: 4,
                            examId      : that.id(),
                            content     : feedbackContent.val()
                        }, function(data){
                            if(data.success){
                                $17.alert("提交成功，感谢您的支持！");
                            }
                        });
                    }
                }
            });

            self.sendlog("Newhomework_assign_" + $uper.subject.key,config_quiz.tabType + "_feedback_btn");
        },

        reSetUFO : function(){
            var self = this;
            /*var totalTime = 0;
            for(var z in constantObj._moduleSeconds){
                if(constantObj._moduleSeconds.hasOwnProperty(z)){
                    totalTime += constantObj._moduleSeconds[z];
                }
            }*/
            var count = constantObj._homeworkContent.practices[config_quiz.tabType].questions.length;
            /*$(".J_UFOInfo p[type="+config_quiz.tabType+"] .count").attr("data-count",count).html(count);
            $("#assignTotalTime").html(Math.ceil(totalTime/60)) ;*/

            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(config_quiz.tabType,count);
        },

        sendlog : function(module,op){
            $17.voxLog({
                module : module,
                op : op
            });
        },

        page_click : function(self,pageNo){
            pageNo = +pageNo || 0;
            if(pageNo < 1 || pageNo > self.totalPage() || pageNo == self.currentPage()){
                return false;
            }
            self.currentPage(pageNo);
            var _startIndex = (pageNo - 1) * self.currentPageQuestionsCount;

            self.currentPageQuestions(ko.mapping.fromJS(self.questions.slice(_startIndex,_startIndex+5))());
            self.initSubject(self.questions.slice(_startIndex,_startIndex+5));

        },
        goSpecifiedPage:function(){
            var self = this;
            var pageNo = self.userInputPage();
            if(/\D/g.test(pageNo)){
                self.userInputPage(null);
            }else{
                self.page_click(self,pageNo);
            }
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getUnit_quiz: function(){
            return new quiz();
        },
        getMid_quiz: function(){
            return new quiz();
        },
        getEnd_quiz: function(){
            return new quiz();
        }
    });
}($17,ko);


