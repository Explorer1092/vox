!function($17,ko) {
    "use strict";
    var isInitUFO = true;
    var photoobjective = function(){
        this.params = {
            sections : "",
            type: "",
            unitId: "",
            bookId: "",
            subject: constantObj.subject,
            objectiveConfigId : ""
        };
        this.content = ko.observableArray([]);
        this.isShowExplain = ko.observable(true);
    };
    photoobjective.prototype = {
        constructor : photoobjective,
        initialise : function(config){
            var _sectionIds = [];
            if($.isArray(config.sections) && config.sections.length > 0){
                $.each(config.sections,function(i,section){
                    _sectionIds.push(section.sectionId);
                });
            }

            this.params = {
                sections : _sectionIds.join(","),
                type: config.tabType || "PHOTO_OBJECTIVE",
                unitId: config.unitId,
                bookId: config.bookId,
                objectiveConfigId : config.objectiveConfigId
            };

            if(isInitUFO){
                isInitUFO = false;
                var str = ["<span class=\"name\">"+(config.tabTypeName || "动手做一做")+"</span>" +
                "<span class=\"count\">0</span>" +
                "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join("");
                $(".J_UFOInfo p[type='PHOTO_OBJECTIVE']").html(str);
            }
        },
        run : function(){
            var that = this;
            $.get("/teacher/new/homework/objective/content.vpage",that.params,function(data){
                if(data.success){
                    var content = data.content || [];
                    if(content.length>0){
                        $.each(content,function(index,item){
                            var isShow = false;
                            for(var i=0;i<constantObj._homeworkContent.practices.PHOTO_OBJECTIVE.questions.length;i++){
                                if(constantObj._homeworkContent.practices.PHOTO_OBJECTIVE.questions[i].questionId == item.questionId){
                                    isShow = true;
                                }
                            }
                            item.isSelected = !!isShow;
                        });
                        that.content(ko.mapping.fromJS(content)());

                        $.each(content,function(i,item){
                            var $mathExamImg = $("#subjective_" + i);
                            $mathExamImg.empty();
                            $("<div></div>").attr("id","examImg-" + i).appendTo($mathExamImg);
                            var node = document.getElementById("examImg-" + i);
                            vox.exam.render(node, 'normal', {
                                ids       : [item.questionId],
                                imgDomain : constantObj.imgDomain,
                                env       : constantObj.env,
                                domain    : constantObj.domain
                            });
                        });
                    }else{
                        $(".J_dataLoading").html("没有符合条件的题目");
                    }
                }else{
                    $(".J_dataLoading").html("数据加载失败");
                }
            });
        },
        showExplain : function(data){
            data.isShowExplain(!data.isShowExplain());
        },
        addSubjective : function (data,e) {
            if($(e.currentTarget).hasClass("cancel")){
                photoobjective.prototype.sendlog("Newhomework_assign_"+$uper.subject.key,"PHOTO_OBJECTIVE_cancel_btn");
            }else{
                photoobjective.prototype.sendlog("Newhomework_assign_"+$uper.subject.key,"PHOTO_OBJECTIVE_choose_btn");
                $(e.currentTarget).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='PHOTO_OBJECTIVE']",
                    border: "5px #39f solid",
                    time  : 600
                });
            }
            data.isSelected(!data.isSelected());
            photoobjective.prototype.resetPhotoobjectiveData.call(this,e);
        },
        resetPhotoobjectiveData :function(e){
            var that = this;
            if(!$(e.currentTarget).hasClass("cancel")){
                var isNewId = true;
                constantObj._homeworkContent.practices.PHOTO_OBJECTIVE.questions.push({
                    questionId: that.questionId(),
                    seconds: that.seconds(),
                    submitWay: that.submitWay(),
                    book    : ko.mapping.toJS(that.book)
                });

                constantObj._moduleSeconds["PHOTO_OBJECTIVE"] += that.seconds();
                constantObj._reviewQuestions["PHOTO_OBJECTIVE"].push(ko.mapping.toJS(that));
            }else{
                constantObj._homeworkContent.practices.PHOTO_OBJECTIVE.questions.splice($.inArray(that.questionId(),constantObj._homeworkContent.practices.PHOTO_OBJECTIVE.questions),1);

                constantObj._moduleSeconds["PHOTO_OBJECTIVE"] -= that.seconds();
                $.each(constantObj._reviewQuestions["PHOTO_OBJECTIVE"],function(i){
                    if(this.questionId==that.questionId()){
                        constantObj._reviewQuestions["PHOTO_OBJECTIVE"].splice(i,1);
                        return false;
                    }
                });

            }
            $("#assignTotalTime").html(photoobjective.prototype.reSetUFO());
            var data_count = constantObj._homeworkContent.practices.PHOTO_OBJECTIVE.questions.length;
            $(".J_UFOInfo p[type='PHOTO_OBJECTIVE'] .count").attr("data-count",data_count).html(data_count);
        },
        reSetUFO : function(){
            var totalTime = 0;
            for(var z in constantObj._moduleSeconds){
                if(constantObj._moduleSeconds.hasOwnProperty(z)){
                    totalTime += constantObj._moduleSeconds[z];
                }
            }
            return Math.ceil(totalTime/60);
        },
        sendlog : function(module,op){
            $17.voxLog({
                module : module,
                op : op
            });
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getPhoto_objective: function(){
            return new photoobjective();
        }
    });
}($17,ko);


