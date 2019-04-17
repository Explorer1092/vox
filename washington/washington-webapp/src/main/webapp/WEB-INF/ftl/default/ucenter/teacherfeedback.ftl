<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起作业-反馈建议"
fastClickFlag=false
pageJs=["teacherfeedback"]
pageJsFile={"teacherfeedback" : "public/script/teacherv3/teacherfeedback"}
pageCssFile={"teacherfeedback" : ["public/skin/project/appeal/pcappeal"]}
>
<div class="unusualAppeal">
    <ul class="tab" id="feedbackTab">
        <li data-type="FEEDBACK">提供建议</li>
        <li data-type="CHEATING" data-bind="visible : showCheating()" style="display: none;">作业异常申诉</li>
        <li data-type="FAKE" data-bind="visible : showFake()" style="display: none;">账号异常申诉</li>
    </ul>

    <div data-bind="visible : type() == 'CHEATING'" style="display: none;">
        <#--CHEATING("作业异常")-->
        <div class="tips">您负责班级的作业情况出现异常，被系统判定为异常作业（了解什么是<a href="http://help.17zuoye.com/?p=1159" target="_blank"><span class="txtBlue">异常作业</span></a>）如您对本次系统判定存有异议，请提交申诉，我们将尽快安排人工复审。</div>
        <div class="main">
            <div class="title">申诉详情填写：</div>
            <div class="describe">
                <textarea placeholder="请详细描述出现异常的作业的学生完成情况，越详细越有助于工作人员加快审核，不少于100字。" data-bind="textInput : content"></textarea>
                <p class="count"><span data-bind="text: count()">0</span>字</p>
            </div>
        </div>
    </div>

    <div data-bind="visible : type() == 'FAKE'" style="display: none;">
        <#--FAKE("账号异常")-->
        <div class="tips">您当前的账号存在异常，为保护您的权益，已将您的账号暂时冻结，部分功能使用会受到限制。请您填写以下申诉信息，我们会尽快核实并恢复误冻结账号。</div>
        <div class="main">
            <div class="title">您任教的学校：</div>
            <input type="text" placeholder="请填写您的学校全称" data-bind="textInput : content"  maxlength="100"/>
        </div>
    </div>

    <div class="main" data-bind="visible : (type() == 'CHEATING' || type() == 'FAKE')" style="display: none;">
        <div class="title">个人资料证明：</div>
        <div class="picBox" style="position: relative;">
            <!-- ko if: fileName() != '' -->
            <div data-bind="visible: fileName() != ''" style="display: none; position: absolute; left: 0; top: 0; width: 100%; height: 100%;"><img src="" data-bind="attr : {src : '<@app.avatar href='/'/>' + fileName()}"/></div>
            <!-- /ko -->
            <div id="filePicker">点击上传照片</div>
        </div>

        <div class="info">
            <p>1、拍摄带有个人详情的教师资格证或工作证等相关教师身份证明资料；</p>
            <p>2、资料信息清晰无误</p>
            <p class="text">注：您的资料仅在本次申诉中使用，不会泄露，请放心上传。</p>
            <p class="text">您的申诉将在<span data-bind="text: (type() == 'FAKE' ? '1个工作日' : '3-5个工作日')">1个工作日</span>内被处理，申诉结果会发送至消息中心。</p>
        </div>
        <div class="footer"><a href="javascript:void(0)" class="w-btn w-btn-well js-submitAppeal">提交</a></div>
    </div>

    <#--FEEDBACK-->
    <div class="main" data-bind="if : (type() != 'CHEATING' && type() != 'FAKE')">
        <div class="title">反馈内容填写：</div>
        <div class="describe">
            <textarea placeholder="请在这里填写建议，您的关注就是我们成长的动力..." data-bind="textInput : feedbackCtn"  maxlength="140"></textarea>
            <p class="count"><span data-bind="text: feedbackCount()">0</span>/140字</p>
        </div>
        <div class="title">
            如遇紧急问题请直接拨打客服热线 <@ftlmacro.hotline phoneType="teacher"/>
        </div>
        <div class="footer"><a href="javascript:void(0)" class="w-btn w-btn-well js-submitFeedback">发送反馈</a></div>
    </div>
</div>

<div class="dialog-alert" style="display: none;" id="DialogAlert">
    <div class="da-mask"></div>
    <div class="da-content">
        <div class="da-hd js-content">数据错误！</div>
        <div class="da-ft">
            <a class="btn_dialog primary js-submit">知道了</a>
        </div>
    </div>
</div>
<script type="text/javascript">
    var moduleType = "${((type == 'CHEATING')!false)?string("CHEATING", "FAKE")}";
    var refUrl = "${refUrl!''}";
    var feedbackType = "${feedbackType!''}";
    var httpCdnUrl = "<@app.link href=""/>";
    var showCheating = ${(showCheating!false)?string};
    var showFake = ${(showFake!false)?string};
    var currentUserId = "${(currentUser.id)!}";
</script>
</@layout.page>