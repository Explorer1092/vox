<div id="singleRewardBeans"></div>
<script type="text/html" id="T:rewardHtmlPopup">
    <div id="rewardHtmlPopup">
        <div class="historyReward historyAllTableBox historyShow">
            <i class="info-icon"></i>
            <p style="padding:0 20px 0 100px;">给选中的学生发学豆：</p>
            <p>
                <span class="historyRewardBtn">
                    <a class="text_blue btn_mark btn_mark_well btn_l minusBtn_single btn_disable" data-bind="if:(rewardIntegral() <= 0),visible:(rewardIntegral() <= 0)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">-</a>
                    <a class="text_blue btn_mark btn_mark_well btn_l minusBtn_single" data-bind="if:(rewardIntegral() > 0),visible:(rewardIntegral() > 0),click:changeReward.bind($data,-1)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">-</a>
                    <span class="tempNum_single" data-bind="text:rewardIntegral">0</span>
                    <a class="text_blue btn_mark btn_mark_well btn_r plusBtn_single" data-bind="click:changeReward.bind($data,1)" href="javascript:void(0);" style="background-color: #189cfb;color:#fff;">+</a>
                </span>
                <i class="w-icon w-icon-39" style="margin:0 15px 0 10px;"></i>
            </p>
            <p class="silverInfo silverInfo_single" data-bind="if:consumerGold() > 0,visible:consumerGold() > 0" style="color:#189cfb;text-align: center;">奖励<strong class="text_red silverCountBox" style="color:#ff0000;" data-bind="text:rewardIntegral">0</strong>学豆消耗<strong class="text_red" style="color:#ff0000;" data-bind="text:consumerGold">0</strong>园丁豆</p>
        </div>
        <div style="text-align:center;">注：该奖励优先消耗班级学豆，当班级学豆数量不足时，将从您的园丁豆账户兑换产生</div>
        <div data-bind="css:{display : rewardInfo != null ? '' : 'none'},text:rewardInfo" style="clear: both; color: #f00; text-align: center; padding: 15px;" class="v-groupRewardInfo"></div>
    </div>
</script>

<script type="text/javascript">
    var reportSingleRewardBeans;
    $(function(){
        function ReportSingleRewardBeans(param){
            var self = this;
            var _param             = param || {};
            var debug              = _param.debug || false;
            self.clazzId           = _param.clazzId || null;
            self.homeworkId        = _param.homeworkId || null;
            self.homeworkType      = _param.homeworkType || null;
            self.userId            = null;
            self.userName          = "";
            self.rewardInfo        = ko.observable(null);
            self.rewardIntegral    = ko.observable(0);
            self.consumerGold      = ko.observable(0);
            self.okBtnFn           = null;
            self.changeReward      = function(num){
                var totalSilver = self.rewardIntegral() + num * 1;
                self.rewardIntegral(totalSilver);
                self.consumerGold(Math.ceil(totalSilver / 5));
            };
            self.rewardBeans = function(){
                $.prompt(template("T:rewardHtmlPopup", {type : "beans"}), {
                    title: '给：' + self.userName + "奖励",
                    position: {width: 700},
                    buttons: {"取消": false, "确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        if(v){
                            if(self.rewardIntegral() < 1){
                                self.rewardInfo("奖励学豆数不能为0");
                                return false;
                            }
                            $.isFunction(self.okBtnFn) && self.okBtnFn(self.rewardIntegral());
                            return false;
                        }
                    },
                    loaded : function(){
                        ko.applyBindings(self, document.getElementById("rewardHtmlPopup"));
                        $17.voxLog({
                            module: "m_Odd245xH",
                            op    : "popup_stu_award_show",
                            s0    : constantObj.subject,
                            s1    : self.homeworkType,
                            s2    : self.homeworkId
                        });
                    }
                });
            };
            self.init = function(){
               // self.clazzId  = null;
                self.userId   = null;
                self.userName = "";
                self.rewardIntegral(0);
                self.rewardInfo(null);
                self.consumerGold(0);
            };

            $("#clazzHomeworkReportEventDiv").on("reportStudentInfo.singleRewardBeans",function(event){
                self.init();
                // self.clazzId = event.clazzId;
                self.userId = event.userId;
                self.userName = event.userName;
                self.subject = event.subject;
                self.okBtnFn = event.okBtnFn || null;
                self.rewardBeans();
            });

            if(debug){
                reportSingleRewardBeans = self;
            }
        }
        ko.applyBindings(new ReportSingleRewardBeans(constantObj),document.getElementById("singleRewardBeans"));

    });
</script>