<#import "../../layout_default.ftl" as layout_default>
<#import "teachercondition.ftl" as teacherConditionList>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="新版老师查询" page_num=3>
        <div class="container-fluid">
            <div class="span9">
                <@headsearch.headSearch/>
                <@teacherConditionList.teacherCondition/>
            </div>
    </div>

</@layout_default.page>