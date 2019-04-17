<#import "../../module.ftl" as module>
<@module.page
title="品牌变更详情"
pageJsFile={"siteJs" : "public/script/basic/brand", "commonJs" :"public/script/common/common"}
pageJs=["siteJs"]
leftMenu="我的申请"
>
<#macro diffShow analysisBean  name >
    <#if ((analysis[name].diff)!false)>
        <#if (analysis[name].before)??>
         <i class="old" title="${(analysis[name].before)!""}">旧</i>
        <#else>
            <i class="new">新</i>
        </#if>
    </#if>
     ${(analysis[name].after)!""}
</#macro>
<style>
    .input-control>label{float: none; font-weight: bolder;}
</style>

<div class="bread-nav">
    <a class="parent-dir" href="javascript:history.back();">变更列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">品牌变更详情</a>
</div>
<h3 class="h3-title">
    品牌变更详情
</h3>
<div>
    <input  type="hidden" id="id"  name="id" <#if (record.id)??>value="${record.id!}"</#if> >
    <input type="hidden"  id="targetId"  name="targetId" <#if (record.targetId)??>value="${record.targetId!}"</#if>>
    <div >
        <div class="input-control">
            <label>
                品牌ID：
            </label>
            <span>
                <#if ((analysis[name].diff)!false)>
                 ${(analysis["id"].after)!"新增品牌"}
                    <#if (analysis["id"].before)?has_content>
                        <i class="old" title="${(analysis["id"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["id"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>品牌名称：</label>
            <span>
                <@diffShow analysisBean=analysis name="brandName" />
        </div>
        <div class="input-control">
            <label>
                是否显示在列表：
            </label>
            <span style="width: 100px">
                     <#if ((analysis["showList"].diff)!false) >
                         <#if (analysis["showList"].before)??>
                             <i class="old" title="${analysis["showList"].before?string('是','否')}">旧</i>
                         <#else>
                             <i class="new">新</i>
                         </#if>
                     </#if>
                <#if (analysis["showList"].after)??>
                ${analysis["showList"].after?string('是','否')}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>在列表中排序值：</label>
            <span>
                <@diffShow analysisBean=analysis name="orderIndex" />
        </div>
        <div class="input-control">
            <label>
                中心图片：
                <#if (analysis["brandLogo"].diff)!false>
                    <#if (analysis["brandLogo"].before)?has_content>
                        <a id="detailHistoryBtn" data-detail="${(analysis["brandLogo"].before)!''}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <input id="detail-img" type="hidden" name="detailImg" />
                <#if (analysis["brandLogo"].after)?? && analysis["brandLogo"].after?has_content>
                        <div class="image"><img src="${analysis["brandLogo"].after}" /></div>
                </#if>
            </div>
        </div>

        <div class="input-control">
            <label>品牌规模：</label>
            <span>
                <@diffShow analysisBean=analysis name="shopScale" />
            </span>
        </div>
        <div class="input-control">
            <label>品牌介绍：</label>
            <span>
                 <@diffShow analysisBean=analysis name="introduction" />
            </span>
        </div>
        <div class="input-control">
            <label>获奖证书描述：</label>
            <span>
                <@diffShow analysisBean=analysis name="certificationName" />
            </span>
        </div>
        <div class="input-control">
            <label>
                品牌特点：
            </label>
            <span>
                <#if ((analysis["points"].diff)!false)>
                    <#if (analysis["points"].before)??>
                        <i class="old" title="${(analysis["points"].before)!?join(',')}">旧</i>
                    <#else>
                        <i class="new">新</i>
                    </#if>
                  </#if>
            ${(analysis["points"].after)!?join(',')}
            </span>
        </div>

    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>
                中心图片：
                <#if (analysis["brandPhoto"].diff)!false>
                    <#if (analysis["brandPhoto"].before)?has_content>
                        <a id="detailHistoryBtn" class="detailImgBtn" data-detail="${(analysis["brandPhoto"].before)!?join(',')}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <input id="detail-img" type="hidden" name="detailImg" />
                <#if (analysis["brandPhoto"].after)?? && analysis["brandPhoto"].after?size gt 0>
                    <#list analysis["brandPhoto"].after as imgUrl>
                        <div class="image"><img src="${imgUrl}" /></div>
                    </#list>
                </#if>
            </div>
        </div>
        <div class="input-control">
            <label>
                获奖证书：
                <#if (analysis["certificationPhotos"].diff)!false>
                    <#if (analysis["certificationPhotos"].before)?has_content>
                        <a id="detailHistoryBtn" class="detailImgBtn" data-detail="${(analysis["certificationPhotos"].before)!?join(',')}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <input id="detail-img" type="hidden" name="detailImg" />
                <#if (analysis["certificationPhotos"].after)?? && analysis["certificationPhotos"].after?size gt 0>
                    <#list analysis["certificationPhotos"].after as imgUrl>
                        <div class="image"><img src="${imgUrl}" /></div>
                    </#list>
                </#if>
            </div>
        </div>
    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>师资力量：</label>
            <#if  (analysis["faculty"].before)?? && analysis["faculty"].before?size gt 0>
            <a id="facultyBoxBeforeBtn" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
            <div id="facultyBoxBefore" style="display: none">
                        <table class="data-table one-page displayed">
                            <thead>
                            <tr>
                                <th>教师名称</th>
                                <th>教师教龄</th>
                                <th>教师科目</th>
                                <th>教师描述</th>
                                <th>教师图片</th>
                            </tr>
                            </thead>
                            <tbody>
                                <#list analysis["faculty"].before as facultyMap>
                                <tr>
                                    <td>${facultyMap["name"]!''}<input type="hidden" name="tName" value="${facultyMap["name"]!''}"/></td>
                                    <td>${facultyMap["experience"]!''}<input type="hidden" name="tSeniority" value="${facultyMap["experience"]!''}"/></td>
                                    <td>${facultyMap["course"]!''}<input type="hidden" name="tCourse" value="${facultyMap["course"]!''}"/></td>
                                    <td>${facultyMap["description"]!''}<input type="hidden" name="tIntroduction" value="${facultyMap["description"]!''}"/></td>
                                    <td><img src='${facultyMap["photo"]!}' style="width: 60px;height: 60px"><input type="hidden" name="tPhoto" value="${facultyMap["photo"]!''}"/></td>
                                </tr>
                                </#list>
                            </tbody>
                        </table>
                    </#if>
            </div>
        </div>
        <div class="image-preview clearfix">
            <div id="facultyBox">
            <#if (analysis["faculty"].diff)!false>
                <#if  (analysis["faculty"].after)?? && analysis["faculty"].after?size gt 0>
                    <table class="data-table one-page displayed">
                        <thead>
                        <tr>
                            <th>教师名称</th>
                            <th>教师教龄</th>
                            <th>教师科目</th>
                            <th>教师描述</th>
                            <th>教师图片</th>
                        </tr>
                        </thead>
                        <tbody>
                            <#list analysis["faculty"].after as facultyMap>
                            <tr>
                                <td>${facultyMap["name"]!''}<input type="hidden" name="tName" value="${facultyMap["name"]!''}"/></td>
                                <td>${facultyMap["experience"]!''}<input type="hidden" name="tSeniority" value="${facultyMap["experience"]!''}"/></td>
                                <td>${facultyMap["course"]!''}<input type="hidden" name="tCourse" value="${facultyMap["course"]!''}"/></td>
                                <td>${facultyMap["description"]!''}<input type="hidden" name="tIntroduction" value="${facultyMap["description"]!''}"/></td>
                                <td><img src='${facultyMap["photo"]!}' style="width: 60px;height: 60px"><input type="hidden" name="tPhoto" value="${facultyMap["photo"]!''}"/></td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </#if>
                </#if>
            </div>
        </div>
    </div>
</div>
<div class="clearfix submit-box">
    <a class="submit-btn abandon-btn " href="/basic/apply/index.vpage">返回</a>
</div>
</@module.page>