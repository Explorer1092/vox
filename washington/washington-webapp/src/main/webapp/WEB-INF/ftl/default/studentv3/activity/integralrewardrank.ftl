<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="奖励榜" header="hide">
<@sugar.capsule js=["DD_belatedPNG"] css=[] />
<@app.css href="public/skin/project/integralrewardrank/skin.css" />
<#assign JZT_CHANNEL_ID = "202011">
<div class="t-beanList-box">
    <div class="bgs">
        <div class="bg01"></div>
        <div class="bg02"></div>
        <div class="bg03"></div>
        <div class="bg04"></div>
    </div>
    <div class="container">
        <div class="rl-top">
            <div class="rl-logo"></div>
            <a href="/student/index.vpage" class="return-home">返回首页</a>
        </div>
        <div class="rl-column">
            <div class="rl-title">本月学豆奖励榜：第${(myRankMap.integral_rank)!'---'}名</div>
            <div class="rl-tips">你有${notReceivedRewardCount!0}个学豆未领取（未领取的学豆过期将清零）</div>
            <div class="rl-btn">
                <a href="javascript:void(0);" class="btn-dole getLearnBeansBtn">立即领取</a>
            </div>
        </div>
        <div class="rl-table">
            <table>
                <thead>
                <tr class="rl-top">
                    <td>目前排名</td>
                    <td>姓名</td>
                    <td>获得学豆奖励数量</td>
                </tr>
                <tr class="rl-mid">
                    <td>
                        ${(myRankMap.integral_rank)!'---'}
                    </td>
                    <td>
                        <div class="name">${(myRankMap.real_name)!'---'}</div>
                    </td>
                    <td>
                        ${((myRankMap.teacher_award_amount)!0) + ((myRankMap.parent_receive_amount)!0)}学豆
                    </td>
                </tr>
                <tr class="rl-bot">
                    <td>
                        <div class="mid-sub"><i class="rl-icon"></i>老师奖励：${(myRankMap.teacher_award_amount)!0}学豆</div>
                    </td>
                    <td  colspan="2">
                        <div class="mid-sub"><i class="rl-icon icon-1"></i>家长在家长通APP中领取：${(myRankMap.parent_receive_amount)!0}学豆</div>
                    </td>
                </tr>
                </thead>
            </table>
            <div style="height: 605px;overflow-y: auto;">
                <table>
                    <tbody>
                    <#if rankList?? && rankList?size gt 0>
                        <#list rankList as student>
                            <tr>
                                <td><#if student_index==0><span class="badge"><#elseif student_index==1><span class="badge badge-1"><#elseif student_index==2><span class="badge badge-2"><#else>${student.integral_rank}</span></#if></td>
                                <td><div class="name">${student.real_name}</div></td>
                                <td>${student.teacher_award_amount+student.parent_receive_amount}学豆</td>
                            </tr>
                        </#list>
                    <#else>
                        <tr>
                            <td><div style="padding: 50px 0; text-align: center;">暂无排名</div></td>
                        </tr>
                    </#if>
                    </tbody>
                </table>
            </div>

        </div>
        <div class="rl-mode">
            <div class="beans-icon"></div>

            <p class="rl-info">老师奖励学生学豆：</p>
            <ul class="list-1">
                <li>1. 老师布置或检查作业时，可奖励学生学豆</li>
                <li>2. 老师使用智慧课堂，可奖励学生学豆</li>
                <li>3. 老师通过小组功能，可奖励学生学豆</li>
                <li>4. 老师写评语时，可奖励学生学豆</li>
                <li>5. 其他奖励学生学豆的功能</li>
            </ul>
            <p class="rl-info">家长在家长通APP中领取：</p>
            <ul class="list-1">
                <li>1. 家长在家长通APP查看学生作业报告为学生领取的学豆</li>
                <li>2. 家长登录家长通APP、双家长登录家长通APP领取的每月学豆奖励</li>
            </ul>
        </div>
    </div>
</div>
<script type="text/html" id="T:receiveRewardPop">
    <div class="t-receiveReward-pop">
        <div class="r-title">扫描下方二维码，安装最新版一起作业家长通APP，即可在“消息”－“班级群”－“查看动态”中领取学豆</div>
        <div class="r-column">
            <div class="r-left">
                <div class="r-step">步骤一</div>
                <div class="r-code">
                <img src="<@app.link href="public/skin/project/integralrewardrank/beanList-code.png"/>" height="189">
                    <#--<img src="<%= JZT_QR_URL %>" height="189">-->
                </div>
                <div class="r-info">扫一扫下载家长通<br>学习动态全知道</div>
            </div>
            <div class="r-arrow"></div>
            <div class="r-right">
                <div class="r-step">步骤二</div>
                <div class="r-image">
                    <img src="<@app.link href="public/skin/project/integralrewardrank/beanList-step.png"/>">
                </div>
            </div>
        </div>
        <div style="clear:both;"></div>
    </div>
</script>
<script type="text/javascript">
    $(function () {
        //领取奖励
        var qrCodeUrlSuccess = false;
        $(document).on("click", ".btn-dole", function(){
            var $this = $(this);
            if($this.hasClass("dole-disabled")){
                return false;
            }
            var popupTitle = "领取奖励";
            var qrCodeUrlSuccessCb = function(){
                $17.get_jzt_qr("${(JZT_CHANNEL_ID)!}", function(JZT_QR_URL){
                    $.prompt(template("T:receiveRewardPop", {
                        JZT_QR_URL : JZT_QR_URL
                    }), {
                        title: popupTitle,
                        buttons: { },
                        position: {width: 734}
                    });
                });
            };
            qrCodeUrlSuccessCb();
        });
    })
</script>
</@temp.page>