<table class="table_vox table_vox_bordered table_vox_striped edge_vox_bot">
    <thead>
    <tr>
        <th colspan="2">总 表</th>
    </tr>
    <tr>
        <td style="width: 50%">认证老师总数</td>
        <td>使用学生总数</td>
    </tr>
    </thead>
    <tbody>
    <tr>
    <#if authenticateNum??>
        <td><h6 class="text_gray_9">${authenticateNum.teacherCount!""} 人</h6></td>
        <td><h6 class="text_gray_9">${authenticateNum.rstaffAuthCount!""} 人</h6></td>
    <#else>
        <th><h6 class="text_gray_9">暂无相关数据</h6></th>
        <th><h6 class="text_gray_9">暂无相关数据</h6></th>
    </#if>
    </tr>
    </tbody>
</table>
<table class="table_vox table_vox_bordered table_vox_striped edge_vox_bot">
    <thead>
    <tr>
        <th colspan="10">明 细</th>
    </tr>
    <tr>
        <td valign="top"><p>省</p></td>
        <td valign="top"><p>市</p></td>
        <td valign="top"><p>区</p></td>
        <td valign="top"><p>学校名称</p></td>
        <td valign="top"><p>老师编号</p></td>
        <td valign="top"><p>老师姓名</p></td>
        <td valign="top"><p>班级数量</p></td>
        <td valign="top"><p>注册学生人数</p></td>
        <td valign="top"><p>认证使用学生数量</p></td>

        <td valign="top"><p>作业录音</p></td>
    </tr>
    </thead>
    <tbody>
    <#if authenticateNum?? && authenticateNum.details?? &&authenticateNum.details?has_content >
        <#list authenticateNum.details as t>
        <tr>
            <th>${t.province!}</th>
            <th>${t.city!}</th>
            <th>${t.area!}</th>
            <th>${t.schoolName!}</th>
            <#if t.teacherId??>
                <td>${t.teacherId!}</td>
            <#else>
                <td>${t.mathTeacherId!}</td>
            </#if>
            <#if t.teacherId??>
                <th>${t.teacherName!}</th>
            <#else>
                <th>${t.mathTeacherName!}</th>
            </#if>
            <td>${t.classNumber!}</td>
            <td>${t.classSize!}</td>
            <td>${t.restaffAuthTotal!}</td>
            <th>
                <#if t.ifVoice ?? && t.ifVoice gt 0 >&nbsp;&nbsp;
                    <a class="btn_vox btn_vox_small" href="/rstaff/report/voicelist.vpage?teacherId=${t.teacherId!}" target= "_blank" title="voice" style="width: 45px;">
                        <i class="icon_rstaff icon_rstaff_11"></i>录音
                    </a>
                </#if>
            </th>
        </tr>
        </#list>
    <#else>
    <tr>
        <th colspan="10" class="text_center">
            <h5 class="text_gray_9">暂无相关数据</h5>
        </th>
    </tr>
    </#if>
    </tbody>
</table>
