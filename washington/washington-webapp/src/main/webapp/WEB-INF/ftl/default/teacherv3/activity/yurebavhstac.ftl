<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<div class="w-base">
    <img src="<@app.link href="public/skin/teacherv3/images/publicbanner/yurebavhstac-banner.png"/>" style="width: 100%;">
</div>

<#if (currentTeacherDetail.subject == "ENGLISH")!false>
    <div class="w-base" >
        <div class="w-base-title">
            <h3>内容特色</h3>
        </div>
        <div class="w-base-container" style="padding: 15px;">
        <#--start-->
            <p style="padding: 5px;"><span style="background-color: #4fa2f9; display: inline-block; vertical-align: middle; width: 20px; height: 20px; font-size: 14px; text-align: center;line-height: 20px; color: #fff; border-radius: 100%;">1</span> 暑假特别呈现，近百个经典绘本，原汁原味</p>
            <p style="padding: 5px;"><span style="background-color: #4fa2f9; display: inline-block; vertical-align: middle; width: 20px; height: 20px; font-size: 14px; text-align: center;line-height: 20px; color: #fff; border-radius: 100%;">2</span> 重磅推出：世界经典教材，牛津原版引进，《典范英语》1-5册（上）</p>
            <p style="padding: 5px;"><span style="background-color: #4fa2f9; display: inline-block; vertical-align: middle; width: 20px; height: 20px; font-size: 14px; text-align: center;line-height: 20px; color: #fff; border-radius: 100%;">3</span> 复习本学期重难点内容</p>
        <#--end-->
        </div>
    </div>
</#if>

<div class="w-base" >
    <div class="w-base-title">
        <h3>活动详情</h3>
    </div>
    <div class="w-base-container">
    <#--start-->
        <div class="t-holiday-homework">
            <div class="t-holiday-step"></div>
            <ul>
                <li class="hs-li">
                    <div class="hs-arrow"><div class="arrow-top"></div><div class="arrow-top-inner"></div></div>
                    <h3>7月底前(06.20—07.31)</h3>
                    <div class="content">
                        <h4>布置作业立得园丁豆：</h4>
                        <ul>
                            <li>布置假期作业，立得<strong>100园丁豆</strong></li>
                            <li>作业布置成功后，将自动分成任务包下发给学生</li>
                        </ul>
                    </div>
                </li>
                <li class="hs-li">
                    <div class="hs-arrow"><div class="arrow-top"></div><div class="arrow-top-inner"></div></div>
                    <h3>整个暑假(06.20—08.31)</h3>
                    <div class="content">
                        <h4>写评语、发奖励：</h4>
                        <ul>
                            <li>可在假期作业列表中查看学生的完成情况</li>
                            <li>对学生进行评语或奖励学豆，每周免费抽奖5次</li>
                        </ul>
                    </div>
                </li>
                <li class="hs-li">
                    <div class="hs-arrow"><div class="arrow-top"></div><div class="arrow-top-inner"></div></div>
                    <h3>开学期间(08.20—09.20)</h3>
                    <div class="content">
                        <h4>开学领大奖：</h4>
                        <ul>
                            <li>开学可抽取<strong>智能电视、手机大奖</strong></li>
                            <li>累计30/60/90名学生全部完成，可分别领取<strong>50/100/150园丁豆</strong></li>
                        </ul>
                    </div>
                </li>
            </ul>
        </div>
    <#--end-->
    </div>
</div>
<script type="text/javascript">
    $(function(){
        LeftMenu.focus("holidayhomework");
    });
</script>
</@shell.page>