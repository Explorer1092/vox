<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='课件大赛'
pageJs=["index"]
pageJsFile={"index" : "public/script/teacher_courseware/index"}
pageCssFile={"skin" : ["public/skin/teacher_courseware/css/skin"]}>
<div class="header">
    <div class="inline">
        <div class="title">我的课件</div>
        <div class="upload_btn" data-bind="click:uploadPage">上传新课件</div>
    </div>
</div>
<div class="content_nav">
    <div class="nav">
        <div class="tab active" data-bind="click:switchFetch.bind($data,'ALL'),css:{'active':currentState() =='ALL'}">全部</div>
        <div class="tab" data-bind="click:switchFetch.bind($data,'DRAFT'),css:{'active':currentState() =='DRAFT'}">未提交</div>
        <div class="tab" data-bind="click:switchFetch.bind($data,'EXAMINING'),css:{'active':currentState() =='EXAMINING'}">审核中</div>
        <div class="tab" data-bind="click:switchFetch.bind($data,'PUBLISHED'),css:{'active':currentState() =='PUBLISHED'}">已发布</div>
        <div class="tab" data-bind="click:switchFetch.bind($data,'REJECTED'),css:{'active':currentState() =='REJECTED'}">被退回</div>
    </div>
</div>
<div class="main_content">
    <div class="courseware_list">
        <div class="inner" data-bind="visible:coursewares().length != 0,foreach:coursewares" style="display: none;">
            <div class="item">
                <div class="head_img">
                    <img src="" alt="" data-bind="attr:{src:'<@app.link href='/'/>'+image}" width="180px" height="116px;" style="display: block;">
                </div>
                <div class="content_desc">
                    <div class="title" data-bind="text:title,click:$root.showDetail"></div>
                    <div class="state_and_op">
                        <div class="state">状态：<span data-bind="text:statusDesc"></span></div>
                        <div class="edit_btn" data-bind="visible: opFlag,click:$root.edit"> <i class="edit"></i>编辑</div>
                        <div class="del_btn" data-bind="visible: opFlag,click:$root.del"> <i class="del"></i>删除</div>
                    </div>
                    <div class="update_time">更新时间：<span class="time" data-bind="text:date"></span></div>
                </div>
                <div class="content_count" data-bind="visible: countFlag">
                    <div class="desc">综合评分：<span data-bind="text:score"></span></div>
                    <div class="desc">评分人数：<span data-bind="text:commentNum"></span></div>
                    <div class="desc">浏览量：<span data-bind="text:visitNum"></span></div>
                </div>
            </div>
        </div>
        <div class="item" data-bind="visible:coursewares().length == 0">
            <p style="text-align: center;margin-top: 30px;">暂无数据</p>
        </div>
        <#--TODO 限制10条，暂时不考虑-->
        <div class="item more" data-bind="visible: showMoreBtn" style="display: none;">
            <div class="load_more">加载更多>></div>
        </div>
    </div>
</div>
</@layout.page>