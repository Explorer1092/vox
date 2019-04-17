<#import "../../layout/webview.layout.ftl" as layout/>
<#assign currentTitle = ((type == 'CHEATING')!false)?string("作业异常申诉", "账号异常申诉")/>
<@layout.page
title=currentTitle!'异常申诉'
pageJs=["appeal"]
pageJsFile={"appeal" : "public/script/teacherv3/appealinit"}
pageCssFile={"appeal" : ["public/skin/project/appeal/appeal"]}
>
<!-- ko if: !isSuccess() -->
<div data-bind="visible : type == 'CHEATING'" style="display: none;">
    <#--CHEATING("作业异常")-->
    <div class="tips">您负责班级的作业情况出现异常，被系统判定为异常作业（了解什么是异常作业）如您对本次系统判定存有异议，请提交申诉，我们将尽快安排人工复审。</div>
    <div class="main">
        <div class="title">申诉详情填写</div>
        <textarea placeholder="请详细描述出现异常的作业的学生完成情况，越详细越有助于工作人员加快审核，不少于100字。" data-bind="textInput : content"></textarea>
    </div>
</div>

<div data-bind="visible : type != 'CHEATING'">
    <#--FAKE("账号异常")-->
    <div class="tips">您当前的账号存在异常，为保护您的权益，已将您的账号暂时冻结，部分功能使用会受到限制。请您填写以下申诉信息，我们会尽快核实并恢复误冻结账号。</div>
    <div class="main">
        <div class="title">填写您的学校名</div>
        <input type="text" placeholder="请填写您的学校全称" data-bind="textInput : content"/>
    </div>
</div>

<div class="main">
    <div id="uploader-demo">
        <div class="title">个人资料证明<a href="javascript:void(0)" id="filePicker">上传照片</a></div>
    </div>
    <!-- ko if: fileName() != '' -->
    <div class="picBox" data-bind="visible: fileName() != ''" style="display: none;"><img src="" data-bind="attr : {src : '<@app.avatar href='/'/>' + fileName()}"/></div>
    <!-- /ko -->
    <p class="text">1、拍摄带有个人详情的教师资格证或工作证等相关教师身份证明资料</p>
    <p class="text">2、资料信息清晰无误</p>
    <p class="text">注：您的资料仅在本次申诉中使用，不会泄露，请放心上传。</p>
</div>

<div class="footer">
    <div class="fixBottom">
        <p class="text">如有问题请拨打客服电话：<span class="tel">400-160-1717</span></p>
        <div class="btnBox">
            <a href="javascript:void(0)" class="w-btn js-submitAppeal">提交</a>
        </div>
    </div>
</div>
<!-- /ko -->

<!-- ko if: isSuccess() -->
<div data-bind="visible: isSuccess()" style="display: none;">
    <div class="success">
        <p>提交成功！</p>
        <!-- ko if: type == 'CHEATING'-->
        <p class="text">您的申诉将在3-5个工作日左右被处理，<br>结果会发送至您的消息中心</p>
        <!-- /ko -->
        <!-- ko if: type == 'FAKE'-->
        <p class="text">您的申诉将在1个工作日内处理，申诉结果会发送至消息中心，请耐心等待。</p>
        <!-- /ko -->
    </div>
    <div class="footer">
        <div class="fixBottom">
            <div class="btnBox">
                <a href="javascript:void(0)" class="w-btn js-gotIt">知道了</a>
            </div>
        </div>
    </div>
</div>
<!-- /ko -->

<script type="text/javascript">
    var moduleType = "${((type == 'CHEATING')!false)?string("CHEATING", "FAKE")}";
</script>

<div class="dialog-alert" style="display: none;" id="DialogAlert">
    <div class="da-mask"></div>
    <div class="da-content">
        <div class="da-hd js-content">数据错误！</div>
        <div class="da-ft">
            <a class="btn_dialog primary js-submit">知道了</a>
        </div>
    </div>
</div>
</@layout.page>