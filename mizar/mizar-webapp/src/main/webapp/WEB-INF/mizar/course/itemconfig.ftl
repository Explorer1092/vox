<#--例：layout 1 -->
<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="投放策略"
pageJsFile={"siteJs" : "public/script/course/item"}
pageCssFile={"mizar" : ["/public/skin/css/skin"]}
pageJs=["siteJs"]
>
<#include "bootstrapTemp.ftl">
<div>
    <ul class="nav nav-pills nav-stacked" role="tablist" style="float:left; height:360px;width: 150px; margin-right: 10px;">
        <li class="configTab<#if has_3?? && has_3 || targetType==3> active</#if>" data-modal="config-all" data-type="3"><a href="javascript:void(0);">投放所有用户</a></li>
        <li class="configTab<#if has_1?? && has_1> active</#if>" data-modal="config-region" data-type="1"><a href="javascript:void(0);">投放指定地区</a></li>
        <li class="configTab<#if has_2?? && has_2> active</#if>" data-modal="config-school" data-type="2"><a href="javascript:void(0);">投放指定学校</a></li>
    </ul>
    <input type="hidden" id="targetType" value="${targetType!3}">
    <div style="float:right; width: 340px;">
        <div id="config-all" style="<#if !(has_3?? && has_3 || targetType==3)>display:none;</#if>">
            <div id="noticeBoard" style="height: 360px;text-align: center;">
                <h3>确认此项之后<br>将针对所有用户投放</h3>
                <h4 style="color: red;"><#if has_3?? && has_3>（已确认）</#if><h4>
                </div>
            </div>
        </div>
        <div id="config-region" style="height: 360px; overflow-y: scroll; <#if !(has_1?? && has_1)>display:none;</#if>">
            <div class="fancytree-checkbox" id="regionTree" ></div>
        </div>
        <div id="config-school" style="height: 360px;overflow-y: auto;<#if !(has_2?? && has_2)>display:none;</#if>">
            <h5 style="position: relative;margin:10px;">
                记录总数：${schoolSize!0}
            </h5>
            <textarea id="schools" class="form-control" style="width:360px; height:280px; resize: none; margin:10px"
                    placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">${targetSchool!}</textarea>
        </div>
    </div>
    <div id="toolBox">
        <a href="javascript:void(0)" id="saveConfig" class="btn btn-success" style="margin-right: 5px;"><i class="glyphicon glyphicon-check"></i> 确 认</a>
        <a href="javascript:void(0)" id="clearConfig" class="btn btn-danger"><i class="glyphicon glyphicon-trash"></i> 清 除</a>
    </div>
</div>
<script>
    var targetRegion = ${targetRegion!''};
    var courseId = "${courseId!}"
</script>
</@layout.page>


