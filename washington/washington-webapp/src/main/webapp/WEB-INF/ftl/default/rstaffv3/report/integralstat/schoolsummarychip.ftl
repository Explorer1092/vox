<div class="article">
    <ul class="inline_vox">
        <li>
            <a href="/rstaff/report/integralstat/summary.vpage" class="btn_vox btn_vox_primary">返回</a>
        </li>
    </ul>

    <table class="table_vox table_vox_bordered table_vox_striped edge_vox_bot">
        <thead>
        <tr>
            <td valign="top"><p>省</p></td>
            <td valign="top"><p>市</p></td>
            <td valign="top"><p>区</p></td>
            <td valign="top"><p>学校名称</p></td>
            <td valign="top"><p>使用老师数量</p></td>
            <td valign="top"><p>注册学生数量</p></td>
            <td valign="top"><p>认证使用学生数量</p></td>
        </tr>
        </thead>
        <tbody>
        <#if schoolResult?? && schoolResult?has_content >
            <#list schoolResult as t>
            <tr>
                <th>${t.province!}</th>
                <th>${t.city!}</th>
                <th>${t.area!}</th>
                <th>${t.schoolName!}</th>
                <td>${t.teacherCount!}</td>
                <th>${t.studentCount!}</th>
                <th>${t.rstaffCount!}</th>
            </tr>
            </#list>
        <#else>
        <tr>
            <td colspan="9" class="text_center">
                <h5 class="text_gray_9">暂无相关数据</h5>
            </td>
        </tr>
        </#if>
        </tbody>
    </table>
</div>