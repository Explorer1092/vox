<#import "../layout.ftl" as layout>
<@layout.page title="孩子的课程日历" pageJs="chipsTravelCatalog">
    <@sugar.capsule css=['chipsAll'] />

<style>
    [v-cloak]{
        display: none;
    }
</style>

<div class="travelWrap">
    <div class="travelTitle">${pname!}</div>
    <div class="travelMain">
        <ul class="catalogBox">
             <#if mapList?? && mapList?size gt 0>
                 <#list mapList as e >
                  <li <#if e.finished ?? && !e.finished> class="active"</#if>>
                      <span class="dot"><i></i></span>
                      <span class="day">第${e.rank!}天</span>
                      <span class="title">${e.name!}</span>
                      <span class="state">
                          <#if e.finished ?? && e.finished>
                              完成
                          <#else>
                              未完成
                          </#if>
                      </span>
                  </li>
                 </#list>
             <#else >
               <li>
                   <strong>暂无数据</strong>
               </li>
             </#if>

        </ul>
    </div>
</div>



</@layout.page>



