<#import "../../layout_view.ftl" as activityMain>
<@activityMain.page title="作业单详情" pageJs="offlineHomeworkDetail">
    <@sugar.capsule css=["offlinehomework"] />
<div class="jc-detailsMain offlineHomeworkDetail" style="display: none;" data-bind="if:!$root.webLoading(),visible:!$root.webLoading()">
    <div class="jc-details">
        <div class="details-mn pad0">
            <p class="text">学科：<!--ko text:subjectName--><!--/ko--></p>
            <p class="text">截止时间 ：<!--ko text:endDateTime--><!--/ko--></p>
        </div>
    </div>
    <div class="jc-details">
        <div class="details-hd">在线作业（预计用时：10分钟）</div>
        <div class="details-mn">
            <div class="textBox">
                <p class="text">单元：<!--ko text:$root.newHomeworkContents().units--><!--/ko--></p>
                <!--ko foreach:{data:$root.newHomeworkContents().objectiveConfigs,as:'config'}-->
                <p class="text"><span class="num" data-bind="text:($index() + 1) + '.'">&nbsp;</span><!--ko text:config--><!--/ko--></p>
                <!--/ko-->
            </div>
        </div>
    </div>
    <div class="jc-details">
        <div class="details-hd">线下作业</div>
        <div class="details-mn">
            <div class="textBox">
                <!--ko foreach:{data:$root.offlineHomeworkContents(),as:'offlineContent'}-->
                <p class="text"><span class="num" data-bind="text:($index() + 1) + '.'">&nbsp;</span><!--ko text:offlineContent--><!--/ko--></p>
                <!--/ko-->
            </div>
        </div>
        <div class="details-tool">
            <p class="name">推荐自学工具给家长：</p>
            <ul class="toolBox">
                <li data-bind="click:$root.toolsPreview.bind($data,'PICLISTEN_ENGLISH','<@ftlmacro.wechatJavaToWebSite />')"><i class="tool tool02"></i><p>英语点读机</p></li>
                <li data-bind="click:$root.toolsPreview.bind($data,'WALKMAN_ENGLISH','<@ftlmacro.wechatJavaToWebSite />')"><i class="tool tool01"></i><p>英语随身听</p></li>
            </ul>
        </div>
    </div>
    <div class="jc-signMain" data-bind="if:$root.signedCount() > 0 || $root.unSignedCount() > 0,visible:$root.signedCount() > 0 || $root.unSignedCount() > 0">
        <ul class="signNav">
            <li class="active" data-bind="css:{'active':$root.focusTab() == 'SIGN'},click:$root.changeTab.bind($data,'SIGN','MANUAL_CLICK')">已签字(<span data-bind="text:$root.signedCount">0</span>)</li>
            <li data-bind="css:{'active':$root.focusTab() == 'UNSIGN'},click:$root.changeTab.bind($data,'UNSIGN','MANUAL_CLICK')">未签字(<span data-bind="text:$root.unSignedCount">0</span>)</li>
        </ul>
        <ul class="signInner" data-bind="visible:$root.showStudents() && $root.showStudents().length > 0">
            <!--ko foreach:{data:$root.showStudents(),as:'student'}-->
            <li>
                <div class="cell">
                    <span class="name" data-bind="text:student.studentName">&nbsp;</span>
                </div>
                <div class="cell" style="display: none;" data-bind="visible:$root.focusTab() == 'SIGN'">
                    <span class="playBtn voicePlayer" data-bind="click:$root.playAudio.bind($data,$element,$root)"></span>
                </div>
            </li>
            <!--/ko-->
        </ul>
    </div>
</div>
<div id="jplayerId"></div>
</@activityMain.page>