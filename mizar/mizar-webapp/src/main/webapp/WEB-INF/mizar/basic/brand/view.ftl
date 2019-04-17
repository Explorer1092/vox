<#import "../../module.ftl" as module>
<@module.page
title="品牌管理"
leftMenu="品牌管理"
>
<style>
    .input-control > label{width: 120px;}
    .input-control > input, .input-control > textarea{width: 680px; }
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/basic/brand/index.vpage">品牌列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">品牌详情</a>
</div>
<h3 class="h3-title">
    品牌详情
</h3>
<h3 class="h3-title">
</h3>
    <div style="float:left;">
        <div class="input-control">
            <label><span class="red-mark">*</span>品牌名称：</label>
            <input class="item readonly" readonly value="${(mizarBrand.brandName)!''}"/>
        </div>
        <div class="input-control">
            <label>是否显示在列表：</label>
            <input class="item readonly" readonly value="<#if (mizarBrand.showList)??>${(mizarBrand.showList)?string("是","否")}<#else>否</#if>">
        </div>
        <div class="input-control">
            <label>排序：</label>
            <input class="item readonly" readonly value="${(mizarBrand.orderIndex)!}">
        </div>

        <div class="input-control">
        <label>品牌规模：</label>
        <input class="item readonly" readonly  style="resize: none;" value="${(mizarBrand.shopScale)!''}"/>
    </div>
        <div class="input-control">
            <label>品牌介绍：</label>
            <div style="font-size:15px; line-height: 28px;padding-left:130px; color: black;">${(mizarBrand.introduction)!''}</div>
        </div>
        <div class="input-control">
            <label>品牌特点：</label>
            <textarea class="readonly" readonly style="resize: none;"><#if (mizarBrand.points)??>${(mizarBrand.points)?join(",")}</#if></textarea>
        </div>
        <div class="input-control">
            <label>获奖证书描述：</label>
            <textarea class="readonly" readonly style="resize: none;">${(mizarBrand.certificationName)!''}</textarea>
        </div>

        <div class="input-control">
            <label>创立时间：</label>
            <input class="item readonly" readonly value="${(mizarBrand.establishment)!''}"/>
        </div>

        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>品牌LOGO：</label>
            </div>
            <div class="image-preview brandLogo  clearfix">
                <#if (mizarBrand.brandLogo)??>
                <div id=img-preview-brandLogo class="image">
                    <img src="${mizarBrand.brandLogo!}"  />
                </div>
                </#if>
            </div>
        </div>
        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>中心图片：</label>
            </div>
            <div class="image-preview brandPhoto clearfix">
                <#if (mizarBrand.brandPhoto)??>
                    <#list mizarBrand.brandPhoto as brandPhoto>
                    <div id=img-preview-brandLogo class="image">
                        <img src="${brandPhoto!}" />
                    </div>
                    </#list>
                </#if>
           </div>
        </div>
        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>获奖证书：</label>
            </div>
            <div class="image-preview certificationPhotos clearfix">
                <#if (mizarBrand.certificationPhotos)??>
                    <#list mizarBrand.certificationPhotos as certificationPhoto>
                    <div id=img-preview-brandLogo class="image">
                        <img src="${certificationPhoto!}"/>
                    </div>
                    </#list>
                </#if>
            </div>
        </div>
        <#--师资力量字段隐藏-->
        <#--<div class="clearfix" style="clear:both;">-->
            <#--<div class="input-control">-->
                <#--<label>师资力量：</label>-->
            <#--</div>-->
            <#--<div class="image-preview clearfix">-->
                <#--<div id="facultyBox">-->
                    <#--<#if (mizarBrand.faculty)??>-->
                        <#--<table class="data-table one-page displayed">-->
                            <#--<thead>-->
                            <#--<tr>-->
                                <#--<th>教师名称</th>-->
                                <#--<th>教师教龄</th>-->
                                <#--<th>教师科目</th>-->
                                <#--<th>教师描述</th>-->
                                <#--<th>教师图片</th>-->
                            <#--</tr>-->
                            <#--</thead>-->
                        <#--<tbody>-->
                            <#--<#list mizarBrand.faculty as facultyMap>-->
                            <#--<tr>-->
                                <#--<td>${facultyMap["name"]!''}</td>-->
                                <#--<td>${facultyMap["experience"]!''}</td>-->
                                <#--<td>${facultyMap["course"]!''}</td>-->
                                <#--<td>${facultyMap["description"]!''}</td>-->
                                <#--<td><div class="image"> <img src='${facultyMap["photo"]!}'></div></td>-->
                            <#--</tr>-->
                            <#--</#list>-->
                        <#--</tbody>-->
                        <#--</table>-->
                    <#--</#if>-->
                <#--</div>-->
            <#--</div>-->
        <#--</div>-->
        <div class=" submit-box">
            <a id="abandon-btn" class="submit-btn abandon-btn" href="/basic/brand/index.vpage">返回</a>
        </div>
    </div>
</@module.page>

