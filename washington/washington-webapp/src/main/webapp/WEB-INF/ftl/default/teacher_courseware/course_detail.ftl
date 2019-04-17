<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='课件大赛'
pageJs=["index"]
pageJsFile={"index" : "public/script/teacher_courseware/detail"}
pageCssFile={"skin" : ["public/skin/teacher_courseware/css/skin"]}>
<div class="header">
    <div class="inline">
        <div class="title">我的课件</div>
    </div>
</div>
<div class="main_content_detail">
    <div class="courseware_list">
        <div class="inner">
            <div class="item">
                <div class="head_img">
                    <img data-bind="attr: { src: '<@app.link href=""/>'+coverImageUrl(), title: '教材封面' }" width="230px" height="160px;">

                </div>
                <div class="content_desc">
                    <div class="title" data-bind="text:title"></div>
                    <div class="state_and_op">
                        <div class="state">状态：<span data-bind="text:statusDesc"></span></div>
                        <div class="edit_btn" data-bind="click:edit,visible:opFlag" style="display: none;"> <i class="edit"></i>编辑</div>
                        <div class="del_btn" data-bind="click:del,visible:opFlag" style="display: none;"> <i class="del"></i>删除</div>
                    </div>
                    <div class="update_time">更新时间：<span class="time" data-bind="text:date"></span></div>
                </div>
                <div class="content_count" data-bind="visible:countFlag" style="display: none;">
                    <div class="desc">册别：<span data-bind="text:termDesc">上册</span></div>
                    <div class="desc">年级：<span data-bind="text:classLevelDesc">一年级</span></div>
                    <div class="desc">教材：<span data-bind="text:bookName">数学逻辑</span></div>
                    <div class="desc">单元：<span data-bind="text:unitName">第一单元</span></div>
                </div>
            </div>
        </div>
    </div>
    <div class="course">
        <div class="per_view_content">
            <div class="per_view_header">
                <div class="title">教学课件 <span data-bind="text:coursewareFileName" style="font-size: 14px;"></span></div>
                <div class="tips">评分列表（暂未开放）</div>
            </div>
            <div class="per_view_box" style="overflow-y: scroll;">
                <div class="view_icon" id="perViewListContainer">
                    <#--<img src="" alt="" width="54px;" height="54px;">-->
                    <p>PPT 预览</p>
                </div>
            </div>
        </div>
        <div class="teach_design">
            <div class="title">教学设计</div>
            <div class="desc"><textarea name="" id="" cols="50" rows="10" placeholder="请输入文字（500字以内）" maxlength="500" data-bind="text:description" disabled="disabled"></textarea></div>
        </div>
    </div>
</div>
</@layout.page>