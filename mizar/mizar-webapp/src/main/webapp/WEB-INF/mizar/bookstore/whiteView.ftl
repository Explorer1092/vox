<#import "../module.ftl" as module>
<@module.page
title="新建白名单"
pageJsFile={"siteJs" : "public/script/bookstore/white"}
pageJs=["siteJs"]
leftMenu="白名单列表"
>
<@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
<@app.script href="/public/plugin/watermark/jquery.watermark.min.js"/>

<style>
    .input-control > label{width: 109px;}

</style>
<div class="bread-nav">
    <a class="parent-dir" href="/bookstore/manager/whiteList/list.vpage">门店转介绍权限白名单</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">${isNew?string("新增","编辑")}白名单</a>
</div>



    <form id="add-form" >
        <div style="margin-top: 10px; padding:0 45px;">
            <div class="input-control">
                <label><span class="red-mark">*</span>门店ID：</label>

                 <#if isNew?? && isNew>
                        <textarea id="comments"  placeholder="请输入门店ID，换行区分ID，最多100行<br/>1、请不要重复填写ID<br/>2、已满足转介绍条件的门店不用填写" class="jq_watermark"  style="height:250px;width: 380px;border: 1px solid #d3d8df;padding: 10px" ></textarea>
                 <#else>
                     <#if whiteListExtendBean.content??>
                      <textarea id="comments"  placeholder="请输入门店ID，换行区分ID，最多100行<br/>1、请不要重复填写ID<br/>2、已满足转介绍条件的门店不用填写" class="jq_watermark"  style="height:250px;width: 380px;border: 1px solid #d3d8df;padding: 10px" >${whiteListExtendBean.content?replace(",", "\n")!''}</textarea>
                     <#else>
                     <textarea id="comments"  placeholder="请输入门店ID，换行区分ID，最多100行<br/>1、请不要重复填写ID<br/>2、已满足转介绍条件的门店不用填写" class="jq_watermark"  style="height:250px;width: 380px;border: 1px solid #d3d8df;padding: 10px" ></textarea>
                     </#if>
                 </#if>
                    <#--<span class="help-inline">您已输入<span style="color:#F00;" id="areaRows">0</span>个门店ID-->
                    <#--<p style="margin-left: 150px">(输入门店ID，一行一个，最多可输入100个)</p>-->
                    <#--<span style="color:#F00; display:none;" id="errorText">亲，最多可输入100个</span></span>-->
                <div class="input-control" style="margin-top: 10px">
                <label>说明：</label>
                 <#if isNew?? && isNew>
                 <textarea id="remark"  placeholder="请输入说明文案，选填，字数在100字以内" maxlength="100" class="jq_watermark" rows="3" cols="50" value="" style="height:150px;width: 380px;border: 1px solid #d3d8df;padding: 10px"></textarea>
                 <#else>
                  <textarea id="remark" placeholder="请输入说明文案，选填，字数在100字以内" maxlength="100" class="jq_watermark" rows="3" cols="50" style="height:150px;width: 380px;border: 1px solid #d3d8df;padding: 10px">${whiteListExtendBean.remark!''}</textarea>
                </#if>
            </div>
            </div>

            <div class=" submit-box">
                <a id="add-save-newpage" data-type="add" class="submit-btn save-btn" href="javascript:void(0)" >提交</a>
                <a id="abandon-btn" class="submit-btn abandon-btn" href="/bookstore/manager/whiteList/list.vpage">取消</a>
            </div>

        </div>
    </form>


</@module.page>


