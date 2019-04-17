<#import '../layout/layout.ftl' as temp>
<@temp.page>
    <@sugar.capsule js=["ko"] css=["new_student.newexam"]/>
<div class="t-app-homework-box">
    <div class="t-app-homework">
        <div class="h-enrollModule" id="examApplyDiv">
            <div class="topPicture"></div>
            <div class="mainInfo">
                <div class="title"><span data-bind="text:name"></span> 开始报名啦！</div>
                <div class="time">距报名截止时间还有：<span data-bind="text:displayTime()"></span></div>
                <div class="link">
                    <a href="javascript:void(0);" data-bind="click:noApply.bind($data,$element)">暂不报名</a><a data-bind="click:submitApply.bind($data,$element)" class="btn-green" href="javascript:void(0)">我要报名</a>
                </div>
                <div class="footerText"></div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        function ExamApply(){
            var self = this;
            var clockId;
            self.id                         = ko.observable("${id!}");
            self.name                       = ko.observable("${name!}");
            self.time                       = ko.observable(${remainTime!0});
            self.redirectUrlAfterUnregister = "${((currentStudentDetail.isPrimaryStudent())!false)?string('/student/index.vpage','/')}";
            self.redirectUrlAfterRegister   = "${((currentStudentDetail.isPrimaryStudent())!false)?string('/student/learning/examination.vpage','/')}";
            self.displayTime                = ko.pureComputed(function(){
                var _time = self.time();
                if(_time <= 0){
                    if(clockId){
                        window.clearInterval(clockId);
                    }
                    return "0秒";
                }
                var day = Math.floor(_time / (3600 * 24));
                var dayMod = _time % (3600 * 24);
                var hour = Math.floor(dayMod / 3600);
                var hourMod = dayMod % 3600;
                var min = Math.floor(hourMod / 60);
                var minMod = hourMod % 60;
                var displayText = "";
                if(day > 0){
                    displayText += day + "天 ";
                }
                if(hour > 0){
                    displayText += hour + "小时 ";
                }
                if(min > 0){
                    displayText += min + "分 ";
                }
                displayText += minMod + "秒 ";
                return displayText;
            });
            self.noApply = function(element){
                var $element = $(element);
                if($element.isFreezing()){
                    $17.alert("请求已提交，请勿频繁点击");
                    return false;
                }
                $17.voxLog({
                    module : "m_86DDZQCl",
                    op     : "o_PLWEena8",
                    s0     : "暂不报名"
                },"student");
                $element.freezing();
                $.get("/student/newexam/unregister.vpage",{newExamId:self.id()},function(data){
                    $element.thaw();
                    if(data.success){
                        window.location.href = $17.isBlank(self.redirectUrlAfterUnregister) ? "/" : self.redirectUrlAfterUnregister;
                    }else{
                        $17.alert(data.info);
                    }
                });
            };
            self.submitApply = function(element){
                var $element = $(element);
                if(self.time() <= 0){
                    $17.alert("报名已结束");
                    return false;
                }
                if($element.isFreezing()){
                    $17.alert("报名正在提交，请勿频繁点击");
                    return false;
                }
                $17.voxLog({
                    module : "m_86DDZQCl",
                    op     : "o_PLWEena8",
                    s0     : "我要报名"
                },"student");
                $element.freezing();
                $.get("/student/newexam/register.vpage",{newExamId:self.id()},function(data){
                    $element.thaw();
                    $17.alert(data.info,function(){
                        if(data.success){
                            window.location.href = $17.isBlank(self.redirectUrlAfterRegister) ? "/" : self.redirectUrlAfterRegister;
                        }
                    });
                });
            };
            self.autoClock = function(){
                clockId = setInterval(function(){
                    self.time(self.time() - 1);
                },1000);
            };
            self.autoClock();
        }
        ko.applyBindings(new ExamApply(),document.getElementById("examApplyDiv"));

        $17.voxLog({
            module : "m_86DDZQCl",
            op     : "o_dBRHtv7j",
            s0     : "${id!}",
            s1     : $17.getQuery("from")
        },"student");
    });
</script>



</@temp.page>

