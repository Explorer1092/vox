<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="自学奖学金申请表"
pageJs=["scholarship"]
pageJsFile={"scholarship" : "public/script/parentMobile/fairyland/scholarship"}
pageCssFile={"scholarship" : ["public/skin/project/scholarship/css/skin"]}
>
<div class="selfScholarship-box">
    <div class="ssp-banner">
        <img src="<@app.link href='/public/skin/project/scholarship/images/scholarship/bg_06.jpg' />">
    </div>
    <form id="apply-form" class="clearfix" enctype="multipart/form-data" method="post" action="/activity/selfstudyproduct/scholarship/apply.vpage" data-bind="css: {noEvents: (subject() !== 'none' && auth() === false) || updateNum() === 1 || error().length > 0}">
        <div class="ssp-slider">
            <ul>
                <li class="active">
                    <div class="title">名字：</div>
                    <div id="child_name" style="float: left;margin-left: .625rem;padding: .375rem .5rem .375rem 0;color: #fff;" data-bind="text: childName"></div>
                </li>
                <li class="active">
                    <div class="title">科目：</div>
                    <select title="科目" class="sel" name="subject" style="pointer-events: auto;" data-bind="value: subject, event: {change: subjectChange.bind($element)}, css: {noEvents: error().length > 0}">
                        <option value="none">请选择科目</option>
                        <option value="ENGLISH">英语</option>
                        <option value="MATH">数学</option>
                        <option value="CHINESE">语文</option>
                    </select>
                </li>
                <li class="active">
                    <div class="title">申请项目：</div>
                    <select title="申请项目" class="sel" name="scholarshipType" data-bind="value: scholarshipType">
                        <option value="none">请选择申请项目</option>
                        <option value="excellent">优秀奖</option>
                        <option value="progress">进步奖</option>
                    </select>
                </li>
                <li class="active" data-bind="visible: scholarshipType() !== 'excellent'">
                    <div class="title">进步分数：</div>
                    <input title="进步分数" name="diffScore" placeholder="请输入进步分数" maxlength="3" type="number" class="sel txt" data-bind="textInput: diffScore">
                </li>
                <li><div class="title">联系电话：</div><input type="number" maxlength="11" name="phone" placeholder="请输入联系电话" class="sel txt" data-bind="textInput: phone"></li>
                <li>
                    <div class="title">经验心得：</div>
                    <textarea name="experience" placeholder="请输入使用自学产品的经验心得，不少于50字，不多于1000字" data-bind="value: experience"></textarea>
                </li>
                <li class="not-for-android-parent" style="display: none;">
                    <div class="title titleBar">请将学生的期末考试成绩拍照并上传<span id="friendly_reminder" data-bind="visible: img().length == 0">（请选择光线明亮的地方拍照，每人可上传一张照片）</div>
                    <div class="clearfix" style="position: relative;float:right;">
                        <input class="for-ios-student" id="upload" name="img" type="file" style="position:absolute;right:0;top:0;width:100%;height:100%;opacity: 0;" data-bind="disable">
                        <a href="javascript:void(0);" class="js-upload default_btn red_btn upload_btn" data-bind="css: {disabled: (subject() !== 'none' && auth() === false) || updateNum() === 1 || error().length > 0}">上传照片</a>
                    </div>
                    <div id="preview" class="image" data-bind="visible: img().length > 0">
                        <img id="preview_img" data-bind="attr:{src: img}">
                        <input style="display: none;" title="已上传图片" name="imgUrl" data-bind="value: img"/>
                    </div>
                </li>
            </ul>
        </div>
    </form>
    <div class="ssp-footer">
        <div id="js-toast" class="inner toast" style="display: none">
            <p style="text-align: center; width:100%;"></p>
        </div>
        <div class="js-fixed inner bg">
            <a href="javascript:void(0);" class="js_submit default_btn" data-bind="text: submitText, css: {disabled: subject() !== 'none' && auth() === false, readonly: updateNum() === 1, error: error().length > 0}">提交</a>
        </div>
    </div>
</div>
</@layout.page>