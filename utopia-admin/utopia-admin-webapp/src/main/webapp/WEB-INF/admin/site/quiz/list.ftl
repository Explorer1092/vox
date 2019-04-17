<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>

<!--/span-->
<div class="span9">
<#--//start-->
    <div style="padding: 0 0 10px;">
        <a href='add.vpage' class="btn"><i class="icon-plus"></i> 新建</a>
    </div>
    <table class="table table-hover table-striped table-bordered ">
        <thead>
        <tr>
            <th>id</th>
            <th>试卷ID</th>
            <th>推送区域</th>
            <th>推送年级</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <td>操作</td>
        </tr>
        </thead>
        <tbody>
           <#if refList?? && refList?size gt 0>
              <#list refList as ref>
                  <tr>
                      <td>${ref.id}</td>
                      <td>${ref.examPaperId}</td>
                      <td>${ref.regionCode}(${ref.regionName})</td>
                      <td>${ref.clazzLevel}</td>
                      <td>${ref.getBeginDateTimeStr()}</td>
                      <td>${ref.getEndDateTimeStr()}</td>
                      <td><a href="edit.vpage?refId=${ref.id}">编辑</a></td>
                  </tr>
              </#list>
           <#else>
             <tr>
                 <td colspan="8">暂无数据</td>
             </tr>
           </#if>
        </tbody>
    </table>
<#--end//-->
</div>
<!--/span-->

</@layout_default.page>