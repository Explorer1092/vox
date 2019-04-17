<div id="singleRewardComment"></div>
<script type="text/html" id="t:conversationSingle">
    <div id="conversationSingle">
        <div  style="width:660px; line-height:25px;" class="sms_us_list">
            <div>
                评语：
                <select id="selectBox" data-bind="options:defaultComment(),value:optionComent,optionsCaption:'请选择'" class="w-int"></select>
                <textarea name="conversationContent" maxlength="100" data-bind="textInput:comment" style="width:660px; margin:10px 0;" placeholder="填写您要发送的评语" cols="30" rows="10"></textarea>
                <span id="conversation_content_number" style="float:right;color:#b8b8b8;" class="text_bold text_gray_9 text_small">还可以输入<strong data-bind="text:(100 - comment().length)">100</strong>个字</span>
            </div>
            <div class="clear"></div>
        </div>
        <#--<div class="historyReward historyAllTableBox historyShow">
            <i class="info-icon"></i>
            <p style="padding:0 20px 0 100px;">给选中的学生发学豆：</p>
            <p>
                <span class="historyRewardBtn">
                    <a class="text_blue btn_mark btn_mark_well btn_l minusBtn_single btn_disable" data-bind="if:(rewardIntegral() <= 0),visible:(rewardIntegral() <= 0)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">-</a>
                    <a class="text_blue btn_mark btn_mark_well btn_l minusBtn_single" data-bind="if:(rewardIntegral() > 0),visible:(rewardIntegral() > 0),click:changeReward.bind($data,-5)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">-</a>
                    <span class="tempNum_single" data-bind="text:rewardIntegral">0</span>
                    <a class="text_blue btn_mark btn_mark_well btn_r plusBtn_single btn_disable" data-bind="if : ((consumerGold() + 1) > enableGold()),visible:((consumerGold() + 1) > enableGold())" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">+</a>
                    <a class="text_blue btn_mark btn_mark_well btn_r plusBtn_single" data-bind="if : ((consumerGold() + 1) <= enableGold()),visible:((consumerGold() + 1) <= enableGold()),click:changeReward.bind($data,5)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">+</a>
                </span>
                <i class="w-icon w-icon-39" style="margin:0 15px 0 10px;"></i>
            </p>
            <p class="silverInfo silverInfo_single" data-bind="if:consumerGold() > 0,visible:consumerGold() > 0" style="color:#189cfb;text-align: center;">奖励<strong class="text_red silverCountBox" style="color:#ff0000;" data-bind="text:rewardIntegral">0</strong>学豆消耗<strong class="text_red" style="color:#ff0000;" data-bind="text:consumerGold">0</strong>园丁豆</p>
            <p class="errorInfo text_red text_bold" style="display:none;">园丁豆不足!</p>
        </div>-->
    </div>
</script>

<script type="text/javascript">
    var reportSingleStudent;
    $(function(){
        function ReportSingleComment(param){
            var self               = this;
            var _param             = param || {};
            var debug              = _param.debug || false;
            self.homeworkId        = _param.homeworkId;
            self.homeworkType      = _param.homeworkType;
            self.subject           = _param.subject;
            self.userId            = null;
            self.userName          = "";
            self.optionComent      = ko.observable("");
            self.comment           = ko.observable("");
            self.okBtnFn           = null;
            self.optionComent.subscribe(function(newValue) {
                var tempComment = self.comment() + newValue;
                if(!$17.isBlank(newValue) && tempComment.length <= 100){
                    self.comment(tempComment);
                }
                $17.voxLog({
                    module: "m_Odd245xH",
                    op    : "popup_stu_write_comments_mould_click",
                    s0    : self.subject,
                    s1    : self.homeworkType,
                    s2    : self.homeworkId
                });
            });
            self.teacherCommentList= _param.teacherCommentList || [];
            self.defaultComment    = function(){
                var defaultCmt;
                if(self.subject == 'ENGLISH'){
                    defaultCmt = [
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
                }else if(self.subject == 'MATH'){
                    defaultCmt = [
                        "做得太棒了！",
                        "你的作业质量比以前有了很大的进步！",
                        "你是一个很有数学才能的学生！",
                        "你的计算能力有了很大提高！",
                        "对于计算题，也要注意留心观察与思考！",
                        "多想一想前后知识的联系，你就会变得更聪明！",
                        "你的目标，应该是在数学方面成为同学们的榜样！",
                        "有的题目如果你能再认真读下已知条件，就一定能做对！"
                    ];
                }else if(self.subject == 'CHINESE'){
                    defaultCmt = [
                        "做得太棒了！",
                        "恭喜你，你已经取得了很大的进步！",
                        "有些小错误，下次要多加注意。",
                        "如果你更加努力的话，我相信你会做得更好！",
                        "如果能把所有作业都按时完成，你会进步得很快！",
                        "你的作业质量比以前有了很大的进步！"
                    ];
                }else{
                    defaultCmt = [];
                }

                return _.uniq(this.teacherCommentList.concat(defaultCmt)).slice(0,20);
            };
            self.showComment       = function(){
                var singleConversation = {
                    state         : {
                        title   : self.userName,
                        html    : template("t:conversationSingle", {}),
                        position: { width: 700 },
                        buttons : { "取消": false, "确定": true },
                        focus   : 1,
                        submit  : function(e, v, m, f){
                            e.preventDefault();
                            if(v){
                                var conversationContentVal = self.comment();
                                if(conversationContentVal.length == 0){
                                    $.prompt.goToState('contentNull');
                                    return false;
                                }

                                if(conversationContentVal.length > 100){
                                    $.prompt.goToState('contentTooLang');
                                    return false;
                                }

                                conversationContentVal = conversationContentVal.replace(/</g, '&lt;').replace(/>/g, '&gt;');

                                $.isFunction(self.okBtnFn) && self.okBtnFn(conversationContentVal);

                            }else{
                                $.prompt.close();
                            }
                        }
                    },
                    contentNull   : {
                        title   : '写评语',
                        html    : '填写您要发送的评语内容',
                        position: { width: 450 },
                        buttons : { "确定": true },
                        focus   : 0,
                        submit  : function(e, v, m, f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    contentTooLang: {
                        title   : '写评语',
                        html    : '您发送的评语内容超过了100字',
                        position: { width: 450 },
                        buttons : { "确定": true },
                        focus   : 0,
                        submit  : function(e, v, m, f){
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    commentSuccess:{
                        title   : '写评语',
                        html    : '评语成功',
                        position: { width: 450 },
                        buttons : { "确定": true },
                        focus   : 0,
                        submit  : function(e, v, m, f){
                            e.preventDefault();
                            $.prompt.close();
                        }
                    }
                };

                $.prompt(singleConversation, {
                    loaded: function(){
                        ko.applyBindings(self, document.getElementById("conversationSingle"));

                        $17.voxLog({
                            module: "m_Odd245xH",
                            op    : "popup_stu_write_comments_show",
                            s0    : self.subject,
                            s1    : self.homeworkType,
                            s2    : self.homeworkId,
                            s3    : self.userId
                        });
                    }
                });
            };
            self.init = function(){
                self.userId            = null;
                self.userName          = "";
                self.optionComent("");
                self.comment("");
            };

            $("#clazzHomeworkReportEventDiv").on("reportStudentInfo.singleComment",function(event){
                self.init();
                self.userId = event.userId;
                self.userName = event.userName;
                self.subject = event.subject;
                self.okBtnFn = event.okBtnFn || null;
                self.showComment();
            });

            if(debug){
                reportSingleStudent = self;
            }
        }
        ko.applyBindings(new ReportSingleComment(constantObj),document.getElementById("singleRewardComment"));

    });
</script>