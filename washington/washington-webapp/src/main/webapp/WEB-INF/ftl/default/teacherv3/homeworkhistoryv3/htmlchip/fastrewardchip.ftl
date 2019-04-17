<script type="text/html" id="t:rewardAll">
    <div class="h-comment-box" id="fastRewards">
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
                    <p data-bind="css:{'w-checkbox-current':$root.studentIsChecked(student.user_id)},click:$root.clickStudent.bind($root,student.user_id)">
                        <span class="w-checkbox"></span>
                        <span class="w-icon-md" data-bind="text:student.user_name"></span>
                    </p>
                    <!--/ko-->
                </div>
                <!--/ko-->
                <!--ko ifnot:showStudentList() != null && showStudentList().length > 0-->
                <div class="list-cell" style="text-align: center;padding: 25px 0;">该分数段下没有学生</div>
                <!--/ko-->
            </div>
        </div>
        <div class="historyReward historyAllTableBox historyShow">
            <i class="info-icon"></i>
            <p style="padding:0 0 0 40px;">给选中的学生发学豆：</p>
            <p style="margin-right:10px;">
            <span class="historyRewardBtn">
                <a class="text_blue btn_mark btn_mark_well btn_l btn_disable" data-bind="if:(rewardIntegral() <= 0),visible:(rewardIntegral() <= 0)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">-</a>
                <a class="text_blue btn_mark btn_mark_well btn_l" data-bind="if:(rewardIntegral() > 0),visible:(rewardIntegral() > 0),click:changeReward.bind($data,-1)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">-</a>
                <span data-bind="text:rewardIntegral">0</span>
                <a class="text_blue btn_mark btn_mark_well btn_r" data-bind="click:changeReward.bind($data,1)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">+</a>
            </span>
                <span class="w-icon w-icon-39"></span>
            </p>
            <p class="silverInfo silverInfo_all" data-bind="visible:rewardIntegral() > 0" style="color:#189cfb;text-align: center;">奖励<strong class="text_red" data-bind="text:selectStudentIds().length" style="color:#ff0000;">0</strong>名同学每人<strong class="text_red" data-bind="text:rewardIntegral" style="color:#ff0000;">0</strong>学豆需要消耗您<strong class="text_red" data-bind="text:consumerGold" style="color:#ff0000;">0</strong>园丁豆</p>
            <p class="errorInfo text_red text_bold" style="display:none;">园丁豆不足!</p>
        </div>
        <div style="text-align:center;">注：该奖励优先消耗班级学豆，当班级学豆数量不足时，将从您的园丁豆账户兑换产生</div>
        <div data-bind="css:{display : rewardInfo != null ? '' : 'none'},text:rewardInfo" style="clear: both; color: #f00; text-align: center; padding: 15px;" class="v-groupRewardInfo"></div>
    </div>
</script>

<script type="text/javascript">
    (function(window,$,ko,$17,undefined){

        function ReportFastRewards(param){
            var self = this;
            param = param || {};
            param              = (typeof param === 'object' ? param : {});
            var studentList         = [];
            var debug               = param.debug || false;
            self.homeworkId         = param.homeworkId;
            self.homeworkType       = param.homeworkType;
            self.subject            = param.subject;
            self.clazzId            = param.clazzId || null;
            self.studentList        = [];
            self.rewardInfo         = ko.observable("");
            self.showStudentList    = ko.observableArray([]);
            self.rewardIntegral    = ko.observable(0);
            self.consumerGold      = ko.observable(0);
            self.selectStudentIds = ko.observableArray([]);
            var _scoreRanges = $.isArray(param.title) ? param.title : [];
            self.scoreRanges = ko.observableArray(_scoreRanges);
            self.focusScoreKey = ko.observable(_scoreRanges.length > 0 ? _scoreRanges[0].key : "");
            self.scoreStudentsMap = param.result || {};
        }
        ReportFastRewards.prototype = {
            constructor : ReportFastRewards,
            studentIsChecked : function(user_id){
                var self = this;
                return $.inArray(user_id,self.selectStudentIds()) != -1;
            },
            changeReward     :  function(num){
                var self = this, totalSilver = 0;
                if(num){
                    totalSilver = self.rewardIntegral() + num * 1;
                }else{
                    totalSilver = self.rewardIntegral();
                }
                var studentLen = self.selectStudentIds().length;
                if(studentLen > 0){
                    var _consumerGold = Math.ceil((totalSilver / 5) * studentLen);
                    self.rewardIntegral(totalSilver);
                    self.consumerGold(_consumerGold);
                }else{
                    self.consumerGold("0");
                    self.rewardInfo("请选择学生");
                }
            },
            clickStudent     : function(user_id){
                var self = this,_index = $.inArray(user_id,self.selectStudentIds());
                if(_index != -1){
                    self.selectStudentIds.splice(_index,1);

                }else{
                    self.selectStudentIds.push(user_id);
                }
                if(self.selectStudentIds.length == 0){
                    self.rewardInfo("");
                }
                self.changeReward();
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

                self.rewardInfo("");
                //重新计算奖励
                self.changeReward(0);
            },
            resetData : function(){
                var self = this;
                self.showStudentList([]);
                self.rewardIntegral(0);
                self.consumerGold(0);
                self.selectStudentIds([]);
            },
            fastRewards : function(){
                var self = this,selectedStudentCount = 0, integral = 0;
                var allConversation = {
                    state: {
                        title       : '一键奖学豆',
                        html        : template("t:rewardAll", {}),
                        position    : { width: 710},
                        buttons     : { "取消" : false, "确定": true},
                        focus       : 1,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            if(v){
                                var studentsList = self.selectStudentIds() || [];
                                var lastIntegral = self.rewardIntegral();
                                if(studentsList.length == 0){
                                    $.prompt.goToState('nullStudents');
                                    return false;
                                }
                                var _rewardIntegral = self.rewardIntegral() * 1;
                                if(_rewardIntegral < 1){
                                    $.prompt.goToState('integralZero');
                                    return false;
                                }

                                var rewardDetail = [];
                                $.each(studentsList,function(i,studentId){
                                    rewardDetail.push({
                                        studentId : studentId,
                                        count     : self.rewardIntegral()
                                    });
                                });

                                var recordData = {
                                    clazzId : self.clazzId,
                                    homeworkId : self.homeworkId,
                                    details : rewardDetail,
                                    subject : self.subject
                                };

                                $17.voxLog({
                                    module: "m_Odd245xH",
                                    op    : "popup_onekey_award_confirm_click",
                                    s0    : self.subject,
                                    s1    : self.homeworkType,
                                    s2    : self.rewardIntegral(),
                                    s3    : self.homeworkId
                                });

                                App.postJSON('/teacher/report/batchsendintegral.vpage', recordData, function(data){
                                    if(data.success){
                                        $.prompt.goToState('gotoConversation');
                                        return false;
                                    }else{
                                        var _info = data.info || '一键奖励发送失败，请重试';
                                        $17.alert(_info);
                                    }
                                });
                            }else{
                                $.prompt.close();
                            }
                        }
                    },
                    nullStudents : {
                        title       : '一键奖学豆',
                        html        : '请选择您要发送奖励的同学。',
                        position    : { width: 450},
                        buttons     : {"确定": true},
                        focus       : 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    integralError: {
                        title       : '一键奖学豆',
                        html        : '您的园丁豆不足。',
                        position    : { width: 450},
                        buttons     : {"确定": true},
                        focus       : 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    integralZero: {
                        title       : '一键奖学豆',
                        html        : '奖励学豆不能零。',
                        position    : { width: 450},
                        buttons     : {"确定": true},
                        focus       : 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    gotoConversation : {
                        title       : '一键奖学豆',
                        html        : '奖励成功',
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
                        ko.applyBindings(self, document.getElementById("fastRewards"));
                        $17.voxLog({
                            module: "m_Odd245xH",
                            op    : "popup_onekey_award_show",
                            s0    : self.subject,
                            s1    : self.homeworkType,
                            s2    : self.homeworkId
                        });
                    },
                    close : function(){
                        self.resetData();
                    }
                });
            }
        };
        window.rewardAndComment = window.rewardAndComment || {};
        window.rewardAndComment.ReportFastRewards = ReportFastRewards;
    }(window,jQuery,ko,$17));
</script>