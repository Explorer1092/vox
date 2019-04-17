<#import "../../layout_default.ftl" as layout_default/>
<#import "researchstaffquery.ftl" as researchStaffQuery/>
<@layout_default.page page_title="教研员查询" page_num=3>
<div id="main_container" class="span9">
    <@researchStaffQuery.queryPage/>
    <#if researchStaffInfoList?has_content>
        <div>
            <table class="table table-striped table-bordered" id="students">
                <tr>
                    <th>教研员ID</th>
                    <th>姓名</th>
                    <th>收货手机</th>
                    <th>学科</th>
                    <th>地区</th>
                    <th>园丁豆</th>
                    <th>是否可用</th>
                </tr>
                <#list researchStaffInfoList as researchStaffInfo>
                    <tr>
                        <td>${researchStaffInfo.userId!}</td>
                        <td><a href="researchstaffhomepage.vpage?researchStaffId=${researchStaffInfo.userId!}" target="_blank">${researchStaffInfo.userName!}</a></td>
                        <td>${researchStaffInfo.phone!}</td>
                        <td>${researchStaffInfo.subjectName!}</td>
                        <td>${researchStaffInfo.regionName!}(${researchStaffInfo.regionCode!})</td>
                        <td>${researchStaffInfo.goldCoin!}</td>
                        <td>${researchStaffInfo.disabled?string("否", "是")}</td>
                    </tr>
                </#list>
            </table>
        </div>
    </#if>
</div>
</@layout_default.page>