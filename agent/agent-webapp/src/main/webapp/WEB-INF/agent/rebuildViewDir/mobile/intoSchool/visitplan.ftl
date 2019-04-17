<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="计划列表" pageJs="visitplan" footerIndex=4>
<@sugar.capsule css=['school']/>
<div class="head fixed-head">
    <a class="return" href="/mobile/my/index.vpage"><i class="return-icon"></i>返回</a>
    <span class="return-line"></span>
    <span class="h-title">拜访计划</span>
</div>
<div class="list">
    <#if (msgList![])?size gt 0>
        <#list msgList as msg>
            <div class="item js-planItem clearfix">
                <p class="name">${msg.schoolName!""}<span class="inner-right js-vtime">${msg.visitTime?string("yyyy-MM-dd")!""}</span></p>
                <div class="dl">
                    <div class="dt">计划内容：</div>
                    <div class="dh">${msg.content!''}</div>
                </div>
            <#if .now lt (msg.visitTime!'')>
                <div class="op-btn c-flex c-flex-2">
                    <div>
                        <div class="btn-stroke fix-width js-updateTime center" data-pid="${msg.id!""}">修改时间</div>
                    </div>
                    <div>
                        <div class="btn-stroke fix-width orange js-delItem center" data-pid="${msg.id!""}">删除</div>
                    </div>
                </div>
            </#if>
            </div>
        </#list>
    <#else>
        <p style="text-align: center;margin-top: 50px;">
            暂无任何计划
        </p>
    </#if>
</div>
<div id="updateDateDialog" style="display:none">
    <div class="clazz-popup">
        <div class="text">
            <input type="date" id="upDate" class="textDate">
        </div>
        <div class="popup-btn">
            <a href="javascript:void(0);" class="js-remove" id="upDateCancel">取消</a>
            <a href="javascript:void(0);" style="background:#ff7d5a;color:#fff;border-bottom-right-radius:0.2rem;" class="js-submit" id="upDateSure">确定</a>
        </div>
    </div>
    <div class="popup-mask js-remove"></div>
</div>
</@layout.page>