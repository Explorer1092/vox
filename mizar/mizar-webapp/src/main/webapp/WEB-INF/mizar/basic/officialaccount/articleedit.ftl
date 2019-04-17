<#import "../../module.ftl" as module>
<@module.page
title="发布管理-新建、编辑"
leftMenu="发布管理"
pageJsFile={"articleedit" : "public/script/basic/officialaccount/articleedit"}
pageJs=["articleedit"]
>

<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业公众号—新建/编辑发布</span>
</div>
<div class="matManage-box">
    <div class="mat-info">提示：一次发布最多可添加4篇文章</div>
</div>
<table class="data-table">
    <thead>
    <tr>
        <th style="width: 68px;">序号 </th>
        <th style="width: 300px;">URL</th>
        <th style="width: 278px;">标题</th>
        <th>封面</th>
    </tr>
    </thead>
    <tbody id="listBox">
        <#if articles?? && articles?size gt 0>
            <#list articles as at>
                <tr data-pdt="${(at.publishDatetime)!''}" data-status="${(at.status)!''}" data-bundle_id="${(at.bundleId)!''}" data-id="${(at.id)!''}">
                    <td>${at_index + 1}</td>
                    <td>
                        <div class="mat-column">
                            <select class="articleUrl" name="" id="" title="" style="width: 240px; height: 35px;">
                                <option value="">请选择</option>
                                <#if materials?? && materials?size gt 0>
                                    <#list materials as ma>
                                        <option value="${(ma.id)!}" <#if (at.materialId == ma.id)!false>selected</#if>  data-url="${(ma.generateUrl)!}">${(ma.title)!}</option>
                                    </#list>
                                </#if>

                            </select>
                        </div>
                    </td>
                    <td>
                        <div class="mat-column">
                            <input value="${(at.articleTitle)!''}" maxlength="20" type="text" placeholder="请输入标题" class="txt articleTitle">
                        </div>
                    </td>
                    <td>
                        <div class="mat-side">
                            <div class="imgShowBox" <#if at_index == 0> data-width="660" data-height="360" <#else> data-width="170" data-height="124" </#if>>
                                <#if (at.imgUrl)?has_content>
                                    <img src="${(at.imgUrl)!''}" style="width: 100px; height: 70px"/>
                                <#else>
                                    <#if at_index == 0>
                                        <p>660×360</p>
                                    <#else>
                                        <p>170×124</p>
                                    </#if>
                                    <div class="bg"></div>
                                </#if>
                            </div>

                            <input type="file" class="fileUploadBtn" style="width: 100%; height: 100%; display:block; position: relative; opacity: 0; border: 1px solid red;top:-47px;">
                        </div>
                    </td>
                </tr>
            </#list>
        </#if>
    </tbody>
</table>
<div class="op-wrapper clearfix">
    <#--<div class="input-control">
        <p>定时发布时间</p>
        <input title="" class="item" style="width: 250px;">
    </div>-->
</div>
<div class="clearfix mat-footer">
    <#if isSavable!false>
        <a id="submitBtn" class="submit-btn orange-btn" href="javascript:void(0)">保 存</a>
    <#else>
        <a class="submit-btn gray-btn" href="javascript:void(0)">保 存</a>
    </#if>

</div>
</@module.page>