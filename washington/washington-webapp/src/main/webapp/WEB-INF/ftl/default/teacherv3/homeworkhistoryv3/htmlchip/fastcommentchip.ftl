<script type="text/html" id="t:conversationAll">
    <div id="fastCommentStudentList" class="h-comment-box">
        <div class="selectItem">
            <ul>
                <!--ko foreach:{data : scoreRanges,as:'scRange'}-->
                <li data-bind="css:{'active':scRange.key == $root.focusScoreKey()},click:$root.selectScoreRange.bind($root,scRange.key)"><span data-bind="text:scRange.name">&nbsp;</span></li>
                <!--/ko-->
                <li style="display: none;"><span>&lt;60分</span></li>
            </ul>
        </div>
        <div class="h-checked">
            <div class="h-checkList">
                <!--ko if:showStudentList() != null && showStudentList().length > 0-->
                <div class="list-cell">
                    <!--ko foreach:{data:showStudentList(),as:'student'}-->
                    <p data-bind="css:{'w-checkbox-current':$root.studentIsChecked(student.user_id)},click:$root.clickStudent.bind($data,$root)">
                        <span class="w-checkbox"></span>
                        <span class="w-icon-md" data-bind="text:student.user_name"></span>
                        <!--ko if:student.teacher_comment-->
                        <span class="tips">已评</span>
                        <!--/ko-->
                    </p>
                    <!--/ko-->
                </div>
                <!--/ko-->
                <!--ko ifnot:showStudentList() != null && showStudentList().length > 0-->
                <div class="list-cell" style="text-align: center;padding: 25px 0;">该分数段下没有学生</div>
                <!--/ko-->
            </div>
        </div>
        <div class="t-changeclass-alert">
            <div class="check" style="margin: 0 -19px;">
                <div class="info">
                    <div class="left">评语：</div>
                    <div class="w-select left">
                        <select data-bind="options:defaultComment($root.subject),value:optionComent,optionsCaption:'请选择'" class="w-int">
                        </select>
                    </div>
                </div>
                <div class="text" style="padding: 8px 22px;">
                    <textarea class="w-int" data-bind="textInput:comment" style="width: 98%; height:100px; line-height: 24px;" rows="10" maxlength="100" cols="30" placeholder="填写您要发送的评语内容" name="dd"></textarea>
                    <p>还可以输入<strong data-bind="text:(100 - comment().length)">100</strong>个字</p>
                </div>
            </div>
        </div>
    </div>
</script>

<script type="text/javascript">
    (function(window,$,ko,_,$17,undefined){
        function defaultCommentList(subject){
            var comments;
            switch(subject){
                case "ENGLISH":
                    comments = [
                        "完成得不错！",
                        "恭喜你，你已经取得了很大的进步！",
                        "有些小错误，下次要多加注意。",
                        "如果你更加努力的话，我相信你会做得更好！",
                        "如果能把所有作业都按时完成，你会进步得很快！",
                        "Wonderful!",
                        "Excellent!" ,
                        "Nice work!" ,
                        "I think you can do better if you try harder." ,
                        "I’m glad to see you are making progress."
                    ];
                    break;
                case "MATH":
                    comments = [
                        "做得太棒了！",
                        "你的作业质量比以前有了很大的进步！",
                        "你是一个很有数学才能的学生！",
                        "你的计算能力有了很大提高！",
                        "对于计算题，也要注意留心观察与思考！",
                        "多想一想前后知识的联系，你就会变得更聪明！",
                        "你的目标，应该是在数学方面成为同学们的榜样！",
                        "有的题目如果你能再认真读下已知条件，就一定能做对！"
                    ];
                    break;
                case "CHINESE":
                    comments = [
                        "做得太棒了！",
                        "恭喜你，你已经取得了很大的进步！",
                        "有些小错误，下次要多加注意。",
                        "如果你更加努力的话，我相信你会做得更好！",
                        "如果能把所有作业都按时完成，你会进步得很快！",
                        "你的作业质量比以前有了很大的进步！"
                    ];
                    break;
                default:
                    comments = [];

            }
            return comments;
        }

        function ReportFastComment(param,cb){
            var self = this;
            param = param || {};
            param              = (typeof param === 'object' ? param : {});
            self.homeworkId         = param.homeworkId;
            self.subject            = param.subject;
            self.homeworkType       = param.homeworkType || null;
            self.studentList        = [];
            self.showStudentList    = ko.observableArray([]);
            self.teacherCommentList = [];
            self.optionComent       = ko.observable("");
            self.comment            = ko.observable("");
            self.optionComent.subscribe(function(newValue) {
                var tempComment = self.comment() + newValue;
                if(!$17.isBlank(newValue) && tempComment.length <= 100){
                    self.comment(tempComment);
                }
                !$17.isBlank(newValue) && $17.voxLog({
                    module: "m_Odd245xH",
                    op : "popup_write_comments_mould_click",
                    s0 : self.subject,
                    s1 : self.homeworkType
                });
            });
            self.defaultComment    = function(subject){
                var self = this;
                return _.uniq(self.teacherCommentList.concat(defaultCommentList(subject))).slice(0,20);
            };
            var _scoreRanges = $.isArray(param.title) ? param.title : [];
            self.scoreRanges = ko.observableArray(_scoreRanges);
            self.focusScoreKey = ko.observable(_scoreRanges.length > 0 ? _scoreRanges[0].key : "");
            self.scoreStudentsMap = param.result || {};
            self.selectStudentIds = ko.observableArray([]);
            self.callbackFn = cb;
        }
        ReportFastComment.prototype = {
            constructor : ReportFastComment,
            studentIsChecked : function(user_id){
                var self = this;
                return $.inArray(user_id,self.selectStudentIds()) != -1;
            },
            selectScoreRange : function(scoreKey){
                var self = this;
                var _studentIds = [];
                var _student = self.scoreStudentsMap[scoreKey] || [];
                for(var m = 0,mLen = _student.length; m < mLen; m++){
                    _studentIds.push(_student[m].user_id);
                }
                self.focusScoreKey(scoreKey);
                self.selectStudentIds(_studentIds);
                self.showStudentList(_student);
            },
            clickStudent   : function(self){
                var that = this;
                var _index = $.inArray(that.user_id,self.selectStudentIds());
                if(_index != -1){
                    self.selectStudentIds.splice(_index,1);
                }else{
                    self.selectStudentIds.push(that.user_id);
                }
            },
            fastComment : function(){
                var self = this,selectedStudentCount = 0, integral = 0;
                var allConversation = {
                    state: {
                        title       : '写评语',
                        html        : template("t:conversationAll", {}),
                        position    : { width: 710},
                        buttons     : { "取消" : false, "确定": true},
                        focus       : 1,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            if(v){
                                var studentsList = self.selectStudentIds() || [];
                                if(studentsList.length == 0){
                                    $.prompt.goToState('nullStudents');
                                    return false;
                                }

                                var conversationContent = $.trim(self.comment());
                                if(conversationContent.length > 100){
                                    $.prompt.goToState('contentTooLang');
                                    return false;
                                }
                                if(conversationContent.length == 0){
                                    $.prompt.goToState('contentNull');
                                    return false;
                                }
                                var studentsListObj = {};
                                var studentsStrList = []; //后端接收字符串学生ID
                                $.each(studentsList,function(i,val){
                                    studentsStrList.push($.trim(val+""));
                                });
                                studentsListObj.comment = conversationContent;
                                studentsListObj.userIds = studentsStrList.join(",");
                                studentsListObj.homeworkId = $.trim(self.homeworkId);

                                $17.voxLog({
                                    module: "m_Odd245xH",
                                    op : "popup_write_comments_confirm_click",
                                    s0 : self.subject,
                                    s1 : self.homeworkType,
                                    s3 : self.homeworkId
                                });

                                $.post('/teacher/new/homework/report/writehomeworkcomment.vpage', studentsListObj, function(data){
                                    if(data.success){
                                        typeof self.callbackFn === 'function' && self.callbackFn(studentsList,conversationContent);
                                        $.prompt.goToState('gotoConversation',function(){
                                            for(var key in self.scoreStudentsMap){
                                                if(self.scoreStudentsMap.hasOwnProperty(key)){
                                                    var upStudents = self.scoreStudentsMap[key] || [];
                                                    for(var m = 0,mLen = upStudents.length; m < mLen; m++){
                                                        var studentId = $.trim(upStudents[m].user_id + "");
                                                        if(studentsStrList.indexOf(studentId) != -1){
                                                            upStudents[m]["teacher_comment"] = conversationContent;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        return false;
                                    }else{
                                        $17.alert(data.info);
                                    }
                                });
                            }else{
                                $.prompt.close();
                            }
                        }
                    },
                    contentNull : {
                        title       : '写评语',
                        html        : '填写您要发送的评语内容。',
                        position    : { width: 450},
                        buttons     : {"确定": true},
                        focus       : 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    contentTooLang : {
                        title       : '写评语',
                        html        : '您发送的评语内容超过了100字，请重新填写。',
                        position    : { width: 450},
                        buttons     : {"确定": true},
                        focus       : 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    nullStudents : {
                        title       : '写评语',
                        html        : '请选择您要发送评语的同学。',
                        position    : { width: 450},
                        buttons     : {"确定": true},
                        focus       : 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    gotoConversation : {
                        title       : '写评语',
                        html        : '评语成功',
                        position    : { width: 450},
                        buttons     : {"确定": true},
                        focus       : 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.close(true);
                        }
                    }
                };
                self.selectScoreRange(self.focusScoreKey());
                $.prompt(allConversation,{
                    loaded : function(){
                        ko.applyBindings(self, document.getElementById("fastCommentStudentList"));
                        $17.voxLog({
                            module: "m_Odd245xH",
                            op : "popup_write_comments_show",
                            s0 : self.subject,
                            s1 : self.homeworkType,
                            s2 : "一键",
                            s3 : self.homeworkId
                        });
                    },
                    close : function(){
                        self.resetData();
                    }
                });
            },
            resetData : function(){
                var self = this;
                self.showStudentList([]);
                self.optionComent("");
                self.comment("");
                self.selectStudentIds([]);
            }
        };
        window.rewardAndComment = window.rewardAndComment || {};
        window.rewardAndComment.ReportFastComment = ReportFastComment;
    }(window,jQuery,ko,_,$17));
</script>