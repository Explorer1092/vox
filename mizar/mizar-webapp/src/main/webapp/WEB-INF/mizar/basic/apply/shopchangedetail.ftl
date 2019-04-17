<#import "../../module.ftl" as module>
<@module.page
title="机构变更详情"
pageJsFile={"siteJs" : "public/script/basic/apply"}
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
    <a class="parent-dir" href="/basic/apply/index.vpage">我的申请</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">机构变更详情</a>
</div>
<h3 class="h3-title">
    机构变更详情
</h3>
<div>
    <input  type="hidden" id="id"  name="id" <#if (record.id)??>value="${record.id!}"</#if> >
    <div style="float:left; width: 300px; margin-right: 20px;">
        <div class="input-control">
            <label>
                机构ID：
            </label>
            <span>
                <#if ((analysis[name].diff)!false)>
                ${(analysis["id"].after)!"新增机构"}
                    <#if (analysis["id"].before)?has_content>
                        <i class="old" title="${(analysis["id"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["id"].before)!""}
                </#if>
            </span>

        </div>

        <div class="input-control">
            <label>机构全称：</label>
            <span>
                <@diffShow analysisBean=analysis name="fullName" />
            </span>
        </div>

        <div class="input-control">
            <label>机构简称：</label>
            <span>
                <@diffShow analysisBean=analysis name="shortName" />
            </span>
        </div>
        <div class="input-control">
            <label>机构介绍：</label>
            <span>
                <@diffShow analysisBean=analysis name="introduction" />
            </span>
        </div>

        <div class="input-control">
            <label>机构类型：</label>
            <span>
                <@diffShow analysisBean=analysis name="shopType" />
            </span>
        </div>

        <div class="input-control">
            <label>地区编码：</label>
            <span>
                <@diffShow analysisBean=analysis name="regionCode" />
            </span>
        </div>

        <div class="input-control">
            <label> 适配年级：</label>
            <span>
                <@diffShow analysisBean=analysis name="matchGrade" />
            </span>
        </div>

        <div class="input-control">
            <label>合作等级分数：</label>
            <span>
                <@diffShow analysisBean=analysis name="cooperationLevel" />
            </span>
        </div>

        <div class="input-control">
            <label>人工调整分数：</label>
            <span>
                <@diffShow analysisBean=analysis name="adjustScore" />
            </span>
        </div>
        <div class="input-control">
            <label>状态：</label>
            <span>
                <@diffShow analysisBean=analysis name="shopStatus" />
            </span>
        </div>

        <div class="input-control">
            <label> 是否线上机构：</label>
            <span>
               <#if analysis["type"].diff!false >
                   <#if (analysis["type"].before)??>
                       <#if analysis["type"].before==1>
                           <i class="old" title="线上机构">旧</i>
                       <#else>
                           <i class="old" title="线下机构">旧</i>
                       </#if>
                   <#else>
                       <i class="new">新</i>
                   </#if>
               </#if>
                <#if (analysis["type"].after)??>
                    <#if analysis["type"].after==1>线上机构
                    <#else>线下机构
                    </#if>
                </#if>

            </span>
        </div>

    </div>
    <!-- end left div -->
    <div style="float:left;width: 480px;">

        <div class="input-control">
            <label>
                所属商圈：
            </label>
            <span style="width: 100px">
                <@diffShow analysisBean=analysis name="tradeArea" />
            </span>

        </div>
        <div class="input-control">
            <label>
                详细地址：
            </label>
            <span>
                <@diffShow analysisBean=analysis name="address" />
            </span>
        </div>
        <div class="input-control">
            <label>
                VIP：
            </label>
            <span style="width: 100px">
                     <#if ((analysis["vip"].diff)!false) >
                         <#if (analysis["vip"].before)??>
                             <i class="old" title="${analysis["vip"].before?string('是','否')}">旧</i>
                         <#else>
                             <i class="new">新</i>
                         </#if>
                     </#if>
                <#if (analysis["vip"].after)??>
                ${analysis["vip"].after?string('是','否')}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>
                是否合作机构：
            </label>
            <span style="width: 100px">
                     <#if ((analysis["cooperator"].diff)!false) >
                         <#if (analysis["cooperator"].before)??>
                             <i class="old" title="${analysis["cooperator"].before?string('是','否')}">旧</i>
                         <#else>
                             <i class="new">新</i>
                         </#if>
                     </#if>
                <#if (analysis["cooperator"].after)??>
                ${analysis["cooperator"].after?string('是','否')}
                </#if>
            </span>
        </div>

        <div class="input-control">
            <label>
                GPS经度：
            </label>
            <span>
                <@diffShow analysisBean=analysis name="longitude" />
            </span>
        </div>

        <div class="input-control">
            <label>
                GPS纬度：
            </label>
            <span>
                <@diffShow analysisBean=analysis name="latitude" />
            </span>
        </div>

        <div class="input-control">
            <label>
                到店礼：
            </label>
            <span>
                <@diffShow analysisBean=analysis name="welcomeGift" />
            </span>
        </div>
        <div class="input-control">
            <label>
                联系电话：
            </label>
            <span>
                <#if ((analysis["contactPhone"].diff)!false)>
                    <#if (analysis["contactPhone"].before)??>
                        <i class="old" title="${(analysis["contactPhone"].before)!?join(',')}">旧</i>
                    <#else>
                        <i class="new">新</i>
                    </#if>
                </#if>
            ${(analysis["contactPhone"].after)!?join(',')}
            </span>
        </div>

        <div class="input-control">
            <label>
                一级分类：
            </label>
            <span>
                <#if ((analysis["firstCategory"].diff)!false)>
                    <#if  (analysis["firstCategory"].before)??>
                        <i class="old" title="${(analysis["firstCategory"].before)!?join(',')}">旧</i>
                    <#else>
                        <i class="new">新</i>
                    </#if>
                </#if>
            ${(analysis["firstCategory"].after)!?join(',')}
            </span>
        </div>
        <div class="input-control">
            <label>
                二级分类：
            </label>
            <span>
                <#if ((analysis["secondCategory"].diff)!false) >
                    <#if (analysis["secondCategory"].before)??>
                        <i class="old" title="${(analysis["secondCategory"].before)!?join(',')}">旧</i>
                    <#else>
                        <i class="new">新</i>
                    </#if>
                </#if>
            ${(analysis["secondCategory"].after)!?join(',')}
            </span>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>
                机构图片：
                <#if (analysis["photo"].diff)!false>
                    <#if (analysis["photo"].before)?has_content>
                        <a id="detailHistoryBtn" data-detail="${(analysis["photo"].before)!?join(',')}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <input id="detail-img" type="hidden" name="detailImg" />
                <#if (analysis["photo"].after)?? && analysis["photo"].after?size gt 0>
                    <#list analysis["photo"].after as imgUrl>
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