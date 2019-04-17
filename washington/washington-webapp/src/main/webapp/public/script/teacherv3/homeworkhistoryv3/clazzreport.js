/*
* 查看详情和类题模块
* */
(function(){
    var SimilarDetail = function(obj){
        var self            = this;
        self.subject        = obj.subject || null;
        self.homeworkId     = obj.homeworkId || null;
        self.clazzName      = obj.clazzName || "";
        self.questionId     = obj.questionId || null;
        self.tabType        = obj.tabType || null;
        self.tabTypeName    = obj.tabTypeName || "";
        self.homeworkType   = obj.homeworkType || "";
        self.useVenus       = obj.useVenus || false;
        self.info           = ko.observable("");
        self.questionObject = ko.observable(null);
        self.submitNum      = ko.observable(0);
        self.error          = ko.observable(0);
        self.averageScore   = ko.observable(0);
        self.focusExamMap = {};
        return self;
    };
    SimilarDetail.prototype = {
        constructor : SimilarDetail,
        questionAndSimilarMap : {},
        getQuestion : function(examId){
            var self = this;
            var questionObj = self.focusExamMap[examId];
            if(!questionObj){
                return 	[];
            }
            var questions = questionObj.questions;
            if(!$.isArray(questions) || questions.length === 0){
                return [];
            }
            return questions.slice(0,1);
        },
        loadExamImg : function(rootEleId,prefixElemId,examId,index){
            if($17.isBlank(rootEleId) || $17.isBlank(prefixElemId) || $17.isBlank(index)){
                return "";
            }
            index = index || 0;
            var $mathExamImg = $("#" + rootEleId);
            if(!$17.isBlank(examId)){
                $mathExamImg.empty();
                var elemId = prefixElemId + "-" + index;
                $("<div></div>").attr("id", elemId).appendTo($mathExamImg);
                var node = document.getElementById(elemId);
                vox.exam.render(node, 'normal', {
                    ids       : [examId]
                });
            }else{
                $mathExamImg.html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
            }
            return "";
        },
        _displayPopup: function(){
            var self = this;
            var allConversation = {
                loadImage : {
                    title       : '题目详情',
                    html        : template("t:LOAD_IMAGE",{}),
                    buttons     : {},
                    position    : { width: 450},
                    focus       : 0,
                    submit:function(e,v,m,f){}
                },
                questionDetail: {
                    title       : '题目详情',
                    html        : template("t:PREVIEW_QUESTION", {}),
                    position    : { width: 770},
                    buttons     : {},
                    focus       : 1,
                    submit:function(e,v,m,f){}
                },
                responseError : {
                    title       : '题目详情',
                    html        : "<div class='w-ag-center' data-bind='text:info'></div>",
                    position    : { width: 450},
                    buttons     : {"确定": true},
                    focus       : 0,
                    submit:function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.close();
                    }
                }
            };
            $.prompt(allConversation,{
                loaded : function(){
                    var initData = function(data){
                        self.submitNum(data.submitNum || 0);
                        self.averageScore(data.averageScore || 0);
                        self.error(data.error || 0);
                        self.questionObject(ko.mapping.fromJS(data.questionObject));
                        $.prompt.goToState("questionDetail");
                        ko.applyBindings(self, document.getElementById("jqistate_questionDetail"));
                    };
                    var groupKey = self.questionId + "_" + self.tabType
                        ,paramData = {
                        homeworkId : self.homeworkId,
                        questionId : self.questionId,
                        objectiveConfigType : self.tabType
                    };
                    if(self.questionAndSimilarMap.hasOwnProperty(groupKey)){
                        initData(self.questionAndSimilarMap[groupKey]);
                    }else{
                        $.get("/teacher/new/homework/report/examandquiz/detailinfo.vpage", paramData,function(data){
                            if(data.success){
                                self.questionAndSimilarMap[groupKey] = data;
                                initData(data);
                            }else{
                                self.info(data.info || "获取数据失败");
                                $.prompt.goToState("responseError");
                                ko.applyBindings(self, document.getElementById("jqistate_responseError"));
                                $17.voxLog({
                                    module : "API_REQUEST_ERROR",
                                    op     : "API_STATE_ERROR",
                                    s0     : "/teacher/new/homework/report/examandquiz/detailinfo.vpage",
                                    s1     : $.toJSON(data),
                                    s2     : $.toJSON(paramData),
                                    s3     : self.subject
                                });
                            }
                        });
                    }

                    $17.voxLog({
                        module : "m_Odd245xH",
                        op     : "popup_hometype_timu_details_show",
                        s0     : self.subject,
                        s1     : self.homeworkType,
                        s2     : self.tabType,
                        s4     : self.questionId
                    });
                },
                close : function () {
                }
            });
        },
        run : function(){
            var self = this;
            if(self.useVenus){
                $17.QuestionDB.getQuestionByIds(self.questionId,function(result){
                    self.focusExamMap = result.success ? result.questionMap : {};
                    self._displayPopup();
                });
            }else{
                self._displayPopup();
            }
        }
    };
    $17.clazzReport = $17.clazzReport || {};
    $17.extend($17.clazzReport, {
        getSimilarDetail : function(obj){
            return new SimilarDetail(obj);
        }
    });
}());

$(function(){
    function LoadingObj(){
        this.webLoading = ko.observable(true);
        this.reponseCode = ko.observable(0);
        this.reponseText = ko.observable("正在加载中..");
    }
    //按学生查看
    function StudentView(opts){
        var self = this;
        var defaultOpts = {
            homeworkId      : null,
            homeworkType    : null,
            subject         : null,
            clazzId         : null,
            env             : "test"
        };
        self.newOpts = $.extend(true,defaultOpts,opts);
        LoadingObj.call(self);
        //self.subject = null;
       // self.subjectName = null;
        self.canMarking = ko.observable(false);
        self.finishedUserNum = ko.observable(0);
        self.totalUserNum = ko.observable(0);
        self.avgScore = ko.observable(0);
        self.finishCorrectNum = ko.observable(0); //完成订正人数
        self.needCorrectNum = ko.observable(0);
        self.showCorrect = ko.observable(false); //是否显示订正信息
        self.clazzName = ko.observable("");
        self.createAt = ko.observable("");
        self.homeworkName = ko.pureComputed(function(){
            return self.clazzName() + " " + self.createAt() + "作业报告";
        },self);
        self.studentReportList = ko.observableArray([]);
        self.typeNames = ko.observableArray([]);
    };
    StudentView.prototype = {
        constructor : StudentView,
        run : function(){
            var self = this;
            var newOpts = self.newOpts || {};
            var paramData = {
                homeworkId : self.newOpts.homeworkId
            };
            $.get("/teacher/new/homework/report/studentpart.vpage",paramData).done(function(res){
                if(res.success){
                    self.canMarking(res.canMarking || false);
                    self.finishedUserNum(res.finishedUserNum || 0);
                    self.totalUserNum(res.totalUserNum || 0);
                    self.avgScore($17.isBlank(res.avgScore) ? '--' : (res.avgScore || 0));
                    self.finishCorrectNum(res.finishCorrectNum || 0);
                    self.needCorrectNum(res.needCorrectNum || 0);
                    self.showCorrect(res.showCorrect || false);
                    self.clazzName(res.clazzName || "");
                    self.createAt(res.createAt || "");
                    // 因为要监控评语字段，所以使用ko.mapping.fromJS把每个字段都变成observable
                    self.studentReportList(ko.mapping.fromJS(res.studentPersonalInfos || [])());
                    self.typeNames(res.typeNames || []);
                    self.webLoading(false);
                }else{
                    (res.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/report/pc/student.vpage",
                        s1     : $.toJSON(res),
                        s2     : $.toJSON(paramData),
                        s3     : self.newOpts.env
                    });
                    $17.alert(res.info,function(){
                        window.location.href = "/teacher/new/homework/report/list.vpage?subject=" + newOpts.subject;
                    });
                }
            }).fail(function (jqXHR,textStatus,errorThrown) {
                $17.alert(errorThrown.message || '获取失败',function(){
                    window.location.href = "/teacher/new/homework/report/list.vpage?subject=" + newOpts.subject;
                });
            });

            var $clazzReportEventDiv =  $("#clazzHomeworkReportEventDiv");
            $clazzReportEventDiv.on("reportSingleComment.editComment", function(event){
                self.updateComment(event.studentIds, event.comment);
            });
            return self;
        },
        studentNameClick : function(self){
            var student = ko.mapping.toJS(this);
            var newOpts = self.newOpts || {};
            $17.voxLog({
                module: "m_Odd245xH",
                op    : "page_reportdetails_stuTab_stuname_click",
                s0    : newOpts.subject,
                s1    : newOpts.homeworkType,
                s2    : newOpts.homeworkId
            });
            setTimeout(function(){
                location.href = '/teacher/new/homework/report/studentreportdetail.vpage?homeworkId=' + newOpts.homeworkId + '&studentId=' + student.userId
            },200);
        },
        leftMove : function(element){
            var $table = $(element).siblings("table.configTypesTable");
            if($table.length == 1){
                var tableLeft = +$table.css("left").replace("px","");
                tableLeft = tableLeft + 75;
                if(tableLeft > 0){
                    tableLeft = 0;
                }
                $table.css("left",tableLeft + "px");
            }
        },
        rightMove  : function(element){
            var self = this;
            var $table = $(element).siblings("table.configTypesTable");
            if($table.length == 1){
                var tableLeft = +$table.css("left").replace("px","");
                var maxLeft = self.typeNames().length * 75 - 290;
                tableLeft = tableLeft - 75;
                if(Math.abs(tableLeft) >= maxLeft){
                    tableLeft = 0 - maxLeft;
                }
                $table.css("left",tableLeft + "px");
            }
        },
        showComment : function(self){
            var that = this
                ,userId = that.userId()
                ,userName = that.userName();
            var newOpts = self.newOpts || {};
            if(!that.comment()){
                $("#clazzHomeworkReportEventDiv").trigger({
                    type     : "reportStudentInfo.singleComment",
                    userId   : userId,
                    userName : userName,
                    subject  : newOpts.subject,
                    okBtnFn  : function(comment){
                        //评语保存结构
                        var detail = {};
                        detail.userIds = $.trim(userId + "");
                        detail.comment = comment;
                        detail.homeworkId = $.trim(newOpts.homeworkId);

                        $17.voxLog({
                            module: "m_Odd245xH",
                            op    : "popup_stu_write_comments_confirm_click",
                            s0    : newOpts.subject,
                            s1    : newOpts.homeworkType,
                            s2    : newOpts.homeworkId,
                            s3    : userId
                        });

                        $.post('/teacher/new/homework/report/writehomeworkcomment.vpage', detail, function(data){
                            if(data.success){
                                /*评语更新*/
                                $("#clazzHomeworkReportEventDiv").trigger({
                                    type       : "reportSingleComment.editComment",
                                    studentIds : [userId],
                                    comment    : comment
                                });
                                $.prompt.goToState("commentSuccess");
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                });

                $17.voxLog({
                    module: "m_Odd245xH",
                    op    : "page_reportdetails_stuTab_write_comments_click",
                    s0    : newOpts.subject,
                    s1    : newOpts.homeworkType,
                    s2    : newOpts.homeworkId
                });
            }else{
                if(!that.audioComment())
                {
                    $17.alert(that.comment());
                }
                else{
                    $.prompt("<div class='w-ag-center'><div id='jp_container_1' class='voice-box '> <span class='voice-inner ' id='playAudioComment'> <i class='i-voice'></i> <i class='jp-duration'></i> </span> </div></div>",
                        { title: "系统提示", buttons: { "知道了": true }, position: {width: 500},submit:function () {
                            $.prompt.close(true);
                        },close:function () {
                            $("#jquery_jplayer_1").jPlayer("destroy");
                            $(document).off("click",'#playAudioComment');
                        }}
                    );
                    $(document).on("click",'#playAudioComment',function () {
                        if($(this).find('.i-voice').hasClass("play"))
                        {
                            $(this).find('.i-voice').removeClass("play");
                            $("#jquery_jplayer_1").jPlayer("stop")
                        }
                        else{
                            $(this).find('.i-voice').addClass("play");
                            $("#jquery_jplayer_1").jPlayer("play")
                        }
                    });
                    $("#jquery_jplayer_1").jPlayer({
                        ready: function (event) {
                            $(this).jPlayer("setMedia", {
                                mp3: that.audioComment()
                            });
                        },
                        error: function(event) {
                            $('.i-voice').removeClass("play");
                        },
                        ended: function(event) {
                            $('.i-voice').removeClass("play");
                        },
                        volume: 0.8,
                        solution: "flash, html",
                        swfPath: "/public/plugin/jPlayer",
                        supplied: "mp3",
                        wmode: "window",
                        remainingDuration: false,
                        toggleDuration: false
                    });
                }
            }
        },
        rewardBeans : function(self){
            var that = this;
            var newOpts = self.newOpts || {};
            $("#clazzHomeworkReportEventDiv").trigger({
                type    : "reportStudentInfo.singleRewardBeans",
                userId   : that.userId(),
                userName : that.userName(),
                okBtnFn  : function(rewardIntegral){
                    var recordData = {
                        clazzId     : newOpts.clazzId,
                        homeworkId  : newOpts.homeworkId,
                        details     : [{
                            studentId   : that.userId(),
                            count       : rewardIntegral
                        }],
                        subject     : newOpts.subject
                    };
                    $17.voxLog({
                        module: "m_Odd245xH",
                        op    : "popup_stu_award_confirm_click",
                        s0    : newOpts.subject,
                        s1    : newOpts.homeworkType,
                        s2    : newOpts.homeworkId,
                        s3    : that.userId(),
                        s4    : rewardIntegral
                    });

                    App.postJSON('/teacher/report/batchsendintegral.vpage', recordData, function(data){
                        if(data.success){
                            $17.alert("奖励学豆成功！");
                        }else{
                            $17.alert(data.info);
                        }
                    });
                }
            });

            $17.voxLog({
                module: "m_Odd245xH",
                op    : "page_reportdetails_viewstu_award_click",
                s0    : newOpts.subject,
                s1    : newOpts.homeworkType,
                s2    : newOpts.homeworkId
            });
        },
        updateComment : function(studentIds,comment){
            var self = this;
            if(studentIds && studentIds.length > 0){
                var studentArr = self.studentReportList();
                for(var z = 0,zLen = studentArr.length; z < zLen; z++){
                    if($.inArray(studentArr[z].userId(),studentIds) != -1){
                        self.studentReportList()[z].comment(comment);
                    }
                }
            }else{
                $17.info("学生ID为空");
            }
        }
    };

    //按题目查看
    function SummaryReport(opts){
        var self = this;
        var defaultOpts = {
            homeworkId : null,
            env        : 'test'
        };
        self.newOpts = $.extend(true,defaultOpts,opts);
        LoadingObj.call(self);
        self.clazzName = ko.observable("");
        self.showCorrect = ko.observable(false);
        self.typeReportList = ko.observableArray([]);
        self.wrongReasonInformation = ko.observableArray([]);
        self.needCorrectNum = ko.observable(0);
        self.finishCorrectNum = ko.observable(0);
        self.userCount = ko.observable(0);
        self.examInitResult = {};
    }
    SummaryReport.prototype = {
        constructor : SummaryReport,
        templateMap : {
            BASIC_APP           : "t:BASIC_APP_PIC",
            NATURAL_SPELLING    : "t:BASIC_APP_PIC",
            EXAM                : "t:EXAM_PIC",
            ORAL_PRACTICE       : "t:EXAM_PIC",
            ORAL_INTELLIGENT_TEACHING : "t:EXAM_PIC",
            MENTAL              : "t:MENTAL",
            UNIT_QUIZ           : "t:EXAM_PIC",
            MID_QUIZ            : "t:EXAM_PIC",
            END_QUIZ            : "t:EXAM_PIC",
            LISTEN_PRACTICE     : "t:EXAM_PIC",
            PHOTO_OBJECTIVE     : "t:SUBJECTIVE",
            VOICE_OBJECTIVE     : "t:SUBJECTIVE",
            WORD_PRACTICE       : "t:EXAM_PIC",
            READ_RECITE         : "t:SUBJECTIVE",
            READING             : "t:NORMAL_TEXT",
            KEY_POINTS          : "t:EXAM_PIC",
            INTELLIGENCE_EXAM   : "t:EXAM_PIC",
            KNOWLEDGE_REVIEW    : "t:EXAM_PIC",
            FALLIBILITY_QUESTION: "t:EXAM_PIC",
            BASIC_KNOWLEDGE     : "t:EXAM_PIC",
            CHINESE_READING     : "t:EXAM_PIC",
            INTERESTING_PICTURE : "t:EXAM_PIC",
            INTELLIGENT_TEACHING: "t:EXAM_PIC",
            LS_KNOWLEDGE_REVIEW : "t:BASIC_APP_PIC",
            RW_KNOWLEDGE_REVIEW : "t:EXAM_PIC",
            NEW_READ_RECITE     : "t:SUBJECTIVE",
            DUBBING             : "t:DUBBING",
            DUBBING_WITH_SCORE  : "t:DUBBING",
            MENTAL_ARITHMETIC   : "t:MENTAL",
            CALC_INTELLIGENT_TEACHING   : "t:EXAM_PIC",
            LEVEL_READINGS      : "t:NORMAL_TEXT",
            READ_RECITE_WITH_SCORE : "t:READ_RECITE_WITH_SCORE",
            WORD_RECOGNITION_AND_READING : "t:WORD_RECOGNITION_AND_READING",
            OCR_MENTAL_ARITHMETIC : "t:OCR_MENTAL_ARITHMETIC",
            ORAL_COMMUNICATION : "t:NORMAL_TEXT",
            WORD_TEACH_AND_PRACTICE : "t:NORMAL_TEXT",
            ONLINE_DICTATION : "t:EXAM_PIC",
            OCR_DICTATION    : "t:OCR_MENTAL_ARITHMETIC"
        },
        initExamCore : function(successCb,failCb){
            var self = this;
            var examInitResult = self.examInitResult;
            if(examInitResult.hasOwnProperty('vox-exam') && examInitResult.success){
                $.isFunction(successCb) && (successCb.call(this,data));
            }else{
                //初始化加载应试
                try{
                    vox.exam.create(function(data){
                        if(data.success){
                            self.examInitResult['vox-exam'] = data;
                            //成功
                            $.isFunction(successCb) && (successCb.call(this,data));
                        }else{
                            $17.voxLog({
                                module: 'vox_exam_create',
                                op:'create_error'
                            });
                            $.isFunction(failCb) && (failCb.call(this,data));
                        }
                    },false,{
                        imgDomain : constantObj.imgDomain,
                        env       : constantObj.env,
                        domain    : constantObj.domain
                    });
                }catch(exception){
                    $17.voxLog({
                        module: 'vox_exam_create',
                        op: 'examCoreJs_error',
                        errMsg: exception.message,
                        userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
                    });
                    $.isFunction(failCb) && (failCb.call(this,{success : false,info : exception.message}));
                }
            }

        },
        run : function(){
            var self = this;
            var paramData = {
                homeworkId : self.newOpts.homeworkId
            };
            $.get("/teacher/new/homework/report/questionpart.vpage",paramData).done(function(res){
                if(res.success){
                    self.clazzName(res.clazzName || "");
                    self.showCorrect(res.showCorrect || false);
                    self.typeReportList(res.typeReportList || []);
                    self.wrongReasonInformation(res.wrongReasonInformation || []);
                    self.needCorrectNum(res.needCorrectNum || 0);
                    self.finishCorrectNum(res.finishCorrectNum || 0);
                    self.userCount(res.totalUserNum || 0);
                    self.webLoading(false);
                }else{
                    (res.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/report/pc/student.vpage",
                        s1     : $.toJSON(res),
                        s2     : $.toJSON(paramData),
                        s3     : self.newOpts.env
                    });
                    $17.alert(res.info || '获取数据失败',function(){
                        window.location.href = "/teacher/new/homework/report/list.vpage?subject=" + self.newOpts.subject;
                    });
                }
            }).fail(function(jqXHR,textStatus,errorThrown){

            });
            return self;
        },
        displayModel : function(tabObj,bindingContext){
            var self = this;
            return self.templateMap[tabObj.type] || "t:NON_SUPPORT_TYPE";
        },
        viewReportDetail : function(self){
            var newOpts = self.newOpts || {};
            var that = this;
            $17.voxLog({
                module: "m_Odd245xH",
                op    : "page_reportdetails_timuTab_hometype_details_click",
                s0    : newOpts.subject,
                s1    : newOpts.homeworkType,
                s2    : newOpts.homeworkId,
                s3    : that.type
            });
            setTimeout(function(){
                location.href = '/teacher/new/homework/report/clazzreportdetail.vpage?homeworkId=' + newOpts.homeworkId + '&tabType=' + that.type;
            },200);
        },
        previewQuestion : function(tabType,tabTypeName,self){
            var tabObj = this;
            var newOpts = self.newOpts || {};
            $17.voxLog({
                module: "m_Odd245xH",
                op    : "page_reportdetails_tmTab_hometype_details_s_click",
                s0    : newOpts.subject,
                s1    : newOpts.homeworkType,
                s2    : tabType,
                s3    : 100 - tabObj.proportion,
                s4    : tabObj.questionId
            });
            self.initExamCore(function(){
                $17.clazzReport.getSimilarDetail({
                    clazzName       : self.clazzName(),
                    subject         : newOpts.subject,
                    homeworkId      : newOpts.homeworkId,
                    questionId      : tabObj.questionId,
                    proportion      : tabObj.proportion,
                    tabType         : tabType,
                    tabTypeName     : tabTypeName,
                    homeworkType    : newOpts.homeworkType,
                    useVenus        : newOpts.useVenus
                }).run();
            });
        },
        rowspanCount : function(lessons){
            if(!$.isArray(lessons)){
                return 1;
            }
            var rowCount = 0;
            for(var m = 0,mLen = lessons.length; m < mLen; m++){
                rowCount += (lessons[m].categories ? lessons[m].categories.length : 0);
            }
            return rowCount < 1 ? 1 : rowCount;
        }
    };

    function ClazzReportV2(opts){
        var self = this;
        var defaultOpts = {
            homeworkId          : null,
            homeworkType        : null,
            tabIconPrefixUrl    : null,
            subject             : null
        };
        LoadingObj.call(self);
        var newOpts = $.extend(true,defaultOpts,opts);
        self.newOpts = newOpts;
        self.homeworkName = ko.observable("");
        self.canMarking = ko.observable(false);
        self.finishedUserNum = ko.observable(0);
        self.totalUserNum = ko.observable(0);
        self.avgScore = ko.observable(0);
        self.showCorrect = ko.observable(false);
        self.finishCorrectNum = ko.observable(0); //完成订正人数
        self.needCorrectNum = ko.observable(0);

        self.studentView = new StudentView(self.newOpts);
        //订阅
        self.studentView.webLoading.subscribe(function(newValue){
            self.webLoading(newValue);
        });
        self.studentView.homeworkName.subscribe(function(newValue){
            self.homeworkName(newValue);
        });
        self.studentView.canMarking.subscribe(function(newValue){
            self.canMarking(newValue);
        });
        self.studentView.finishedUserNum.subscribe(function(newValue){
            self.finishedUserNum(newValue);
        });
        self.studentView.totalUserNum.subscribe(function (newValue) {
            self.totalUserNum(newValue);
        });
        self.studentView.avgScore.subscribe(function(newValue){
            self.avgScore(newValue);
        });
        self.studentView.showCorrect.subscribe(function(newValue){
            self.showCorrect(newValue);
        });
        self.studentView.finishCorrectNum.subscribe(function(newValue){
            self.finishCorrectNum(newValue);
        });
        self.studentView.needCorrectNum.subscribe(function(newValue){
            self.needCorrectNum(newValue);
        });

        self.summaryReport = new SummaryReport(newOpts);

        self.focusTabType = ko.observable("");
        self.tabs = ko.observableArray([{
            tabType : "studentInfo",
            tabName : "按学生查看",
            icon    : newOpts.tabIconPrefixUrl ? newOpts.tabIconPrefixUrl + '/tab-icon-wcqk.png' : '/tab-icon-wcqk.png'
        },{
            tabType : "summaryReport",
            tabName : "按题目查看",
            icon    : newOpts.tabIconPrefixUrl ? newOpts.tabIconPrefixUrl + '/tab-icon-zyhz.png' : '/tab-icon-zyhz.png'
        }]);
        self.tabClick.call(self.tabs()[0],self);
        self.reportFastComment = null;
        self.reportFastReward = null;
    };
    ClazzReportV2.prototype = {
        constructor : ClazzReportV2,
        tabClick    : function(self){
            var that = this;
            var _tabType = that.tabType;
            if(self.focusTabType() == _tabType){
                return false;
            }
            var newOpts = self.newOpts || {};
            switch (_tabType){
                case "studentInfo":
                    $.isFunction(self.studentView.webLoading) && self.studentView.webLoading() && (self.studentView.run());
                    break;
                case "summaryReport":
                    $.isFunction(self.summaryReport.webLoading) && self.summaryReport.webLoading() && (self.summaryReport.run());
                    break;
                default:
            }

            self.focusTabType(_tabType);

            $17.voxLog({
                module: "m_Odd245xH",
                op    : _tabType === "studentInfo" ? "page_reportdetails_stuTab_click" : "page_reportdetails_timuTab_click",
                s0    : newOpts.subject,
                s1    : newOpts.homeworkType,
                s2    : newOpts.homeworkId
            });
        },
        fastComment : function(topComment){
            var self = this;
            var newOpts = self.newOpts || {};
            var subject = newOpts.subject;
            var homeworkType = newOpts.homeworkType;
            var homeworkId = newOpts.homeworkId;
            $17.voxLog({
                module: "m_Odd245xH",
                op : topComment === "topComment" ? "page_reportdetails_write_comments_click" : "page_reportdetails_onekey_write_comments_click",
                s0 : subject,
                s1 : homeworkType,
                s2 : homeworkId
            });
            if(self.reportFastComment && self.reportFastComment instanceof rewardAndComment.ReportFastComment){
                self.reportFastComment.fastComment();
            }else{
                $.post("/teacher/new/homework/homeworkfinishinfo.vpage",{
                    homeworkId : homeworkId
                },function(data){
                    if(data.success){
                        self.reportFastComment = new rewardAndComment.ReportFastComment({
                            homeworkId : homeworkId,
                            subject    : subject,
                            homeworkType: homeworkType,
                            title       : data.title || [],
                            result      : data.result || {}
                        },function(studentIds,comment){
                            self.studentView.updateComment.call(self.studentView,studentIds,comment);
                        });
                        self.reportFastComment.fastComment();
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        },
        fastRewards : function(){
            var self = this;
            var newOpts = self.newOpts || {};
            $17.voxLog({
                module: "m_Odd245xH",
                op    : "page_reportdetails_onekey_award_click",
                s0    : newOpts.subject,
                s1    : newOpts.homeworkType,
                s2    : newOpts.homeworkId
            });
            if(self.reportFastReward && self.reportFastReward instanceof rewardAndComment.ReportFastRewards){
                self.reportFastReward.fastRewards();
            }else{
                $.post("/teacher/new/homework/homeworkfinishinfo.vpage",{
                    homeworkId : newOpts.homeworkId
                },function(data){
                    if(data.success){
                        self.reportFastReward = new rewardAndComment.ReportFastRewards({
                            homeworkId  : newOpts.homeworkId,
                            subject     : newOpts.subject,
                            homeworkType: newOpts.homeworkType,
                            clazzId     : newOpts.clazzId,
                            title       : data.title || [],
                            result      : data.result || {}
                        });
                        self.reportFastReward.fastRewards();
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        },
        toClazzreportDetail : function () {
            var self = this;
            var newOpts = self.newOpts || {};
            window.location.href = "/teacher/new/homework/report/clazzreportdetail.vpage?homeworkId=" + newOpts.homeworkId;
            $17.voxLog({
                module: "m_Odd245xH",
                op    : "homework_detail_correct_click"
            });
        }
    };
    

    var clazzReportv2 = new ClazzReportV2(constantObj);

    ko.applyBindings(clazzReportv2,document.getElementById("clazzReport"));


});