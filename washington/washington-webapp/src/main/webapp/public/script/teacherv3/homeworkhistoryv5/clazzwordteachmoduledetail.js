$(function () {
    var typeTemplateMap = {
        WORDEXERCISE : "EXAM_DETAIL_LIST",
        IMAGETEXTRHYME : "IMAGETEXTRHYME",
        CHINESECHARACTERCULTURE : "CHINESECHARACTERCULTURE"
    };

    function ClazzWordTeachModuleDetail(option){
         this.homeworkId = option.homeworkId;
         this.stoneId = option.stoneId;
         this.wordTeachModuleType = option.wordTeachModuleType;
         this.moduleDetail = ko.observable(null);
         this.clazzGroupId = "";
         this.run();
    }

    ClazzWordTeachModuleDetail.prototype = {
        constructor : ClazzWordTeachModuleDetail,
        displayModel : function(){
            var self = this;
            return typeTemplateMap[self.wordTeachModuleType] || "UNKNOWN_TEMPLATE";
        },
        extendWordExecrise : function(questionMap){
            var self = this;
            $17.extend(self,{
                focusExamMap : questionMap || {},
                getQuestion : function(examId){
                    var questionObj = this.focusExamMap[examId];
                    if(!questionObj){
                        return 	[];
                    }
                    var questions = questionObj.questions;
                    if(!$.isArray(questions) || questions.length === 0){
                        return [];
                    }
                    return questions.slice(0,1);
                },
                getQuestionDifficultyName : function(difficulty){
                    difficulty = difficulty * 1;
                    return difficulty === 3 ? "中等" : (difficulty === 4 || difficulty === 5 ? "困难":"容易")
                },
                getUserNames : function (users) {
                    var userNames = "";
                    users = users || [];
                    for(var m = 0,mLen = users.length; m < mLen; m++){
                        userNames += users[m].userName;
                        (m < mLen - 1) && (userNames += ",");
                    }
                    return userNames;
                }
            });
            return self;
        },
        extendImageText : function(){
            var self = this;
            $17.extend(self,{
                focusChapter : ko.observable(null),
                fetchCurrentStudents : function(){
                    return this.focusChapter() ? (this.focusChapter().studentHomeworkDatas || []) : [];
                },
                switchChapter : function(self){
                    var chapterObj = this;
                    if(self.focusChapter() && self.focusChapter().chapterId === chapterObj.chapterId){
                        return false;
                    }
                    self.focusChapter(this);
                },
                previewChapter : function(){
                    var studentObj = this;
                    var domain = "/";
                    if(answerDetailData.env === "test"){
                        domain = "//www.test.17zuoye.net";
                    }else{
                        domain = location.origin;
                    }
                    var gameUrl = domain + "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-speak-training/works.vhtml?" + $.param({
                        __p__ : JSON.stringify({
                            shareUrl : studentObj.flashvarsUrl
                        })
                    });
                    var dataHtml = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="375" marginwidth="0" height="640" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                    $.prompt(dataHtml, {
                        title   : "预 览",
                        buttons : {},
                        position: { width: 415 },
                        submit  : function(){},
                        close   : function(){}
                    });
                    $17.voxLog({
                        module : "m_Odd245xH",
                        op : "graphic_reading_list_preview_click",
                        s0 : self.clazzGroupId
                    })
                }
            });
            return self;
        },
        extendChineseCulture : function(){
            var self = this;
            $17.extend(self,{
                focusCourse : ko.observable(null),
                fetchCurrentStudents : function(){
                    return this.focusCourse() ? (this.focusCourse().studentHomeworkDatas || []) : [];
                },
                switchCourse : function(self){
                    var courseObj = this;
                    if(self.focusCourse() && self.focusCourse().courseId === courseObj.courseId){
                        return false;
                    }
                    self.focusCourse(this);
                }
            });
            return self;
        },
        run : function(){
            var self = this;
            $.get("/teacher/new/homework/report/clazzWordTeachModuleDetail.vpage",{
                homeworkId  : self.homeworkId,
                stoneId     : self.stoneId,
                wordTeachModuleType : self.wordTeachModuleType
            }).done(function(res){
                if(res.success){
                    self.clazzGroupId = res.clazzGroupId;
                    switch (self.wordTeachModuleType){
                        case "WORDEXERCISE":
                            var questions = res.questions,questionIds = [];
                            for(var t = 0,tLen = questions.length; t < tLen; t++){
                                questionIds.push(questions[t].qid);
                            }
                            $17.QuestionDB.getQuestionByIds(questionIds,function(dbResp){
                                self.extendWordExecrise((dbResp.questionMap || {}));
                                self.moduleDetail(res);
                            });
                            break;
                        case "IMAGETEXTRHYME":
                            res.result = res.result || [];
                            if(res.result.length > 0){
                                self.extendImageText();
                                self.switchChapter.call(res.result[0],self);
                            }
                            self.moduleDetail(res);
                            break;
                        case "CHINESECHARACTERCULTURE":
                            res.result = res.result || [];
                            if(res.result.length > 0){
                                self.extendChineseCulture();
                                self.switchCourse.call(res.result[0],self);
                            }
                            self.moduleDetail(res);
                            break;
                        default:
                            self.moduleDetail(res);
                            break;
                    }
                }else{
                    $17.alert("获取数据失败");
                }
            }).fail(function(){
                $17.alert("获取数据失败，请刷新页面重试");
            });
        }
    };

    ko.applyBindings(new ClazzWordTeachModuleDetail({
        homeworkId : $17.getQuery("homeworkId"),
        stoneId : $17.getQuery("stoneId"),
        wordTeachModuleType : $17.getQuery("wordTeachModuleType")
    }),document.getElementById("tabContentHolder"));
});
