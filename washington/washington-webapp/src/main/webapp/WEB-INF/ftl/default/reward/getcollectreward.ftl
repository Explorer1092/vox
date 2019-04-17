<#import "layout/layout.ftl" as temp />
<@temp.page index='' columnType="empty">

<style>
    .switchBox, .ms-box ul li:first-child, .ms-box ul li:nth-child(2) {display: none;}
    .collectreward-box{ width: 1000px; height: 687px; background-color: #6bc0ff; padding-top: 74px; position: relative; }
    .collectreward-main-img{ display: block; width: 786px; height: 385px; margin: 0 auto; }
    .collectreward-bottom-cloud{ position: absolute; bottom: 0; }
    .collectreward-btnbox{ width: 192px; height: 63px; display: block; margin: 28px auto 0; cursor: pointer; display: none; }
    .collectreward-btnbox .collectreward-btn2,.collectreward-btnbox .collectreward-btn3 {display: none;}
    .collectreward-btnbox.disabled .collectreward-btn2{ display: block }
    .collectreward-btnbox.disabled .collectreward-btn1, .collectreward-btnbox.disabled .collectreward-btn3{ display: none }
    .collectreward-btnbox.expired .collectreward-btn3{ display: block }
    .collectreward-btnbox.expired .collectreward-btn1, .collectreward-btnbox.expired .collectreward-btn2{ display: none }
    .collectreward-popup{position: fixed; left:0 ;top: 0;width: 100%;height: 100%;background: rgba(0,0,0,.6); z-index: 10;}
    .collectreward-popup .popup-box{width: 300px; height: 172px; background-color: #ffffff; border-radius: 10px; position: absolute; left: 50%; top: 50%; -webkit-transform: translate(-50%, -50%); -moz-transform: translate(-50%, -50%); -ms-transform: translate(-50%, -50%); -o-transform: translate(-50%, -50%); transform: translate(-50%, -50%);}
    .collectreward-popup .popup-box .dou{ position: absolute; left: 80px; top: -54px; }
    .collectreward-popup .popup-box .closebox{ width: 30px; height:60px; position: absolute; right: 5px; top: -60px; }
    .collectreward-popup .popup-box .closebox .close{ display: block; width: 30px; height: 30px; cursor: pointer;}
    .collectreward-popup .popup-box .closebox .line{ width: 2px; height:30px; background-color: #ffffff; display: block; margin: 0 auto; }
    .collectreward-popup .popup-box .para{ font-size: 18px; color: #1c1c1c; text-align: center; margin-top:62px; }
    .collectreward-popup .popup-box .btn-box{ width: 100%; height: 58px; position: absolute; left: 0; bottom: 0; cursor: pointer;}
    .collectreward-popup .popup-box .btn-box .sure, .collectreward-popup .popup-box .btn-box .mydou{ width: 50%; height: 58px; float: left; line-height: 58px; text-align: center; font-size: 20px; }
    .collectreward-popup .popup-box .btn-box .sure{ background-color: #eeeeee; border-radius: 0 0 0 10px; color: #1c1c1c; }
    .collectreward-popup .popup-box .btn-box .mydou{ display: block; background-color: #49c1ff; border-radius: 0 0 10px 0; color: #ffffff; }
</style>
<div class="collectreward-box">
    <#if (currentTeacherDetail.isPrimarySchool())!false>
    <img class="collectreward-main-img" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-img-primary.png"/>" alt="">
    </#if>
    <#if (currentTeacherDetail.isJuniorTeacher())!false>
    <img class="collectreward-main-img" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-img-junior.png"/>" alt="">
    </#if>
    <div class="collectreward-btnbox" id="getRewardBtn">
        <img class="collectreward-btn1" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-btn.png"/>" alt="">
        <img class="collectreward-btn2" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-btn-disabled.png"/>" alt="">
        <img class="collectreward-btn3" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-btn-expired.png"/>" alt="">
    </div>
    <img class="collectreward-bottom-cloud" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-cloud.png"/>" alt="">
    <div class="collectreward-popup" id="getRewardPopup" style="display: none;">
        <div class="popup-box">
            <img class="dou" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-dou.png"/>" alt="">
            <div class="closebox">
                <img class="close getRewardPopupClose" src="<@app.link href="public/skin/reward/imagesV1/getcollectreward/getreward-close.png"/>" alt="">
                <div class="line"></div>
            </div>
            <#if (currentTeacherDetail.isPrimarySchool())!false>
            <p class="para">50个园丁豆已放入您的账户</p>
            </#if>
            <#if (currentTeacherDetail.isJuniorTeacher())!false>
            <p class="para">500个学豆已放入您的账户</p>
            </#if>
            <div class="btn-box">
                <div class="sure getRewardPopupClose">确认</div>
                <#if (currentTeacherDetail.isPrimarySchool())!false>
                    <a class="mydou" href="/teacher/center/index.vpage#/teacher/center/mygold.vpage">我的园丁豆</a>
                </#if>
                <#if (currentTeacherDetail.isJuniorTeacher())!false>
                    <a class="mydou" href="/teacher/center/index.vpage#/teacher/center/mygold.vpage">我的学豆</a>
                </#if>

            </div>
        </div>
    </div>
</div>

<script>
    $(function () {
        var databaseLogs = "web_teacher_logs";
        $17.voxLog({
            database: databaseLogs,
            module: 'm_2ekTvaNe',
            op : 'o_hkjYjYuP'
        });

        var getRewardBtn = $('#getRewardBtn');
        var getRewardPopup = $('#getRewardPopup');
        var getRewardPopupClose = $('.getRewardPopupClose');

        var getRewardInitData = {
            logisticId: $17.getQueryParams('logisticId').logisticId
        };
        $.post('/reward/collection_reward_status.vpage', getRewardInitData, function (res) {
            if (res.success) {
                getRewardBtn.show();
                if (res.received) {
                    getRewardBtn.addClass('disabled');
                } else if (!res.received && res.expired) {
                    getRewardBtn.addClass('expired');
                }
            } else {
                $17.alert(res.info);
            }
        });
        getRewardBtn.on('click', function () {
            if ($(this).hasClass('disabled') || $(this).hasClass('expired')) return false;
            var getRewardData = {
                taskType: 'REWARD_COLLECTION',
                rewardName: '代收奖励'
            };
            $.post('/reward/getreward.vpage', getRewardData, function (res) {
                if (res.success) {
                    getRewardPopup.show();
                    getRewardBtn.addClass('disabled');
                    $17.voxLog({
                        database: databaseLogs,
                        module: 'm_2ekTvaNe',
                        op : 'o_hPjMKJHD'
                    });
                } else {
                    $17.alert(res.info);
                }
            })
        });
        getRewardPopupClose.on('click', function () {
            getRewardPopup.hide();
        });
    });
</script>
</@temp.page>