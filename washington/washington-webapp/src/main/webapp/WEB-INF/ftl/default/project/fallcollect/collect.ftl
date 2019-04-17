<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="false" title="开学季 - 寻找最in互联网教师及班级">
<@app.css href="public/skin/project/fallcollect/css/skin.css"/>
<@sugar.capsule js=["voxLogs"] />
<div class="s-skin">
    <div class="s-banner"><img src="<@app.link href="public/skin/project/fallcollect/images/banner-02.jpg"/>" alt=""></div>
    <div class="m-list">
        <div class="m-item fir-ch">
            <i class="tagIcon">惊喜1</i>
            <div class="sub-title ">时尚先锋 贴心懂你</div>
            <div class="inner-box">
                <p class="in-time"><i class="timeIcon"></i>时间：2017.9.1-10.7</p>
                <p class="in-peopl"><i class="timeIcon timeIcon-2"></i>对象：部分学校的小学英语老师</p>
                <div class="text-info">
                    <img src="<@app.link href="public/skin/project/fallcollect/images/icon-prize.png"/>" alt="">
                    <div class="txt">
                        <p>认证教师组建班级，邀请新学生并布置作业， <span class="type-colr">可领高额流量补贴礼哦！</span></p>
                        <p>新学生最多的前100个老师还可获得<span class="type-colr">《最in互联网教师》称号</span>，班级同学更有额外大奖领取！</p>
                    </div>
                </div>
                <div class="open-btn JS-openBefore" style="display:none;cursor:default"><a href="javascript:void(0);">9月1日开启</a></div>
                <div class="open-btn JS-openNow JS-studApprove" style="display:none;"><a href="javascript:void(0);">即刻参与</a></div>
            </div>
        </div>
        <div class="m-item">
            <i class="tagIcon">惊喜2</i>
            <!-- sub-title-2 皮肤 -->
            <div class="sub-title sub-title-2">惊喜不断 锦上添花</div>
            <div class="inner-box">
                <p class="in-time"><i class="timeIcon"></i>时间：2017.9.1-9.30</p>
                <p class="in-peopl"><i class="timeIcon timeIcon-2"></i>对象：小学认证老师</p>
                <div class="text-info">
                    <img class="icon-posit" src="<@app.link href="public/skin/project/fallcollect/images/icon-baby.png"/>" alt="">
                    <div class="txt">
                        <p>9月狂欢，惊喜不断，天天都是教师节！ 布置作业抽大奖，更有<Br/><span class="type-colr">iPhone 7</span> 等你哦！</p>
                    </div>
                </div>
                <div class="open-btn JS-openBefore" style="display:none;cursor:default"><a href="javascript:void(0);">9月1日开启</a></div>
                <div class="open-btn JS-openNow" style="display:none;"><a href="/project/teacheraward/index.vpage">即刻参与</a></div>
            </div>
        </div>
        <div class="m-item">
            <i class="tagIcon">惊喜3</i>
            <div class="sub-title">呼朋唤友 教师联盟</div>
            <div class="inner-box">
                <p class="in-time"><i class="timeIcon"></i>时间：2017.9.1-10.31</p>
                <p class="in-peopl"><i class="timeIcon timeIcon-2"></i>对象：小学认证老师</p>
                <div class="text-info">
                    <img class="icon-wid" src="<@app.link href="public/skin/project/fallcollect/images/icon-flow.png"/>" alt="">
                    <div class="txt">
                        <p>邀请您身边的语文、数学、英语任意一科老师， 被邀请老师一个月内达成认证且名下新学生不 少于30人，<span class="type-colr" style="color:#fd9120;">10元流量包!</span></p>
                    </div>
                </div>
                <div class="open-btn JS-openBefore" style="display:none;cursor:default"><a href="javascript:void(0);">9月1日开启</a></div>
                <div class="open-btn JS-openNow" style="display:none;"><a href="/project/fallactivities/teacherleague.vpage">即刻参与</a></div>
            </div>
        </div>
        <div class="m-item">
            <i class="tagIcon">惊喜4</i>
            <!-- sub-title-2 皮肤 -->
            <div class="sub-title sub-title-2">精品资源 轻松备课</div>
            <div class="inner-box">
                <p class="in-time"><i class="timeIcon"></i>时间：2017.9.1-9.7</p>
                <p class="in-peopl"><i class="timeIcon timeIcon-2"></i>对象：小学老师</p>
                <div class="text-info">
                    <img class="icon-wid" src="<@app.link href="public/skin/project/fallcollect/images/icon-data.png"/>" alt="">
                    <div class="txt">
                        <p>小学语、数、外三科精品课件，开学备课妥妥哒！<span class="type-colr" style="color:#fd9120;">免费拓展教学素材</span>，拿到手软！</p>
                        <p><span class="type-colr" style="color:#fd9120;font-size:20px;">9月1日在老师app开启哦~</span></p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
$(function () {
    YQ.voxLogs({
        database: "web_teacher_logs",
        module: 'm_lIHTCSqE',
        op : 'o_UOALfm0T'
    });
   var myMonth = new Date().getMonth() + 1;
   if (myMonth < 9){
       $(".JS-openBefore").show();
   }else{
       $(".JS-openNow").show();
   }
    $(".JS-studApprove").on("click",function () {
        $.ajax({
            url: '/teacher/activity/term2017/actone/init.vpage',
            type: 'GET',
            dataType: 'json',
            success: function (res) {
                if (res.success){
                    if (res.schoolLevel == null){
                        $17.alert("您的学校暂时未开放此活动，请关注其他活动哦~");
                    }else{
                        location.href = "/teacher/activity/term2017/actone/index.vpage";
                    }
                }else{
                    $17.alert(res.info);
                }
            }
        });
    });
});

</script>
</@temp.page>