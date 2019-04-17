<script type="text/html" id="base_kpi_data">
    <span style="font-size:.65rem;padding:0 .5rem;color:#636880;">学校概况</span>
    <ul class="res-list list-dif clearfix" style="clear:both;">
        <li  style="width:25%">
            <div class="sub" style="color:#636880;"><%= res.stuScale%></div>
            <div class="volume">规模</div>
        </li>
        <li  style="width:25%">
            <div class="sub" style="color:#636880;"><%= res.regStuCount%></div>
            <div class="volume">注册</div>
        </li>
        <li  style="width:25%">
            <div class="sub" style="color:#636880;"><%= res.auStuCount%></div>
            <div class="volume">认证</div>
        </li>
        <li  style="width:25%">
            <div class="sub" style="color:#636880;"><%= res.finSglSubjHwGte3AuStuCount%></div>
            <div class="volume"><#if schoolBasicInfo.schoolLevel?? && (schoolBasicInfo.schoolLevel == "JUNIOR" || schoolBasicInfo.schoolLevel == "INFANT")>月活<#else>英语月活</#if></div>
        </li>
    </ul>
</script>
<script type="text/html" id="subject_data">
    <div class="hd" style="font-size:.65rem;padding:0 .5rem;color:#636880;">
        <span>分科数据 <i class="showTipsButton"></i></span>
    </div>
    <table style="width:100%">
        <tr class="tr_title">
            <td></td>
            <td>渗透率</td>
            <td>认证3套月活</td>
            <td>次月留存</td>
        </tr>
        <%for(var key in res.subjectPerformanceMap){%>
        <% var subject = res.subjectPerformanceMap[key]; %>
        <#if schoolBasicInfo.schoolLevel?? && (schoolBasicInfo.schoolLevel == "MIDDLE" || schoolBasicInfo.schoolLevel == "HIGH")>
            <%if(subject.subjectName == "英语"){%>
        </#if>
            <tr class="tr_title">
                <td><%=subject.subjectName%></td>
                <td>
                    <%=Math.round(subject.permeability*100)%>%
                    <p>上月<%=Math.round(subject.previousPermeability*100)%>%</p>
                </td>
                <td>
                    <%=subject.finHwGte3AuStuCount%>（新增<%=subject.finHwGte3IncAuStuCount%>）
                    <p>上月<%=subject.previousFinHwGte3AuStuCount%></p>
                </td>
                <td>
                    <%=Math.round(subject.mrtRate*100)%>%
                    <p>上月<%=Math.round(subject.previousMrtRate*100)%>%</td></p>
            </tr>
        <#if schoolBasicInfo.schoolLevel?? && (schoolBasicInfo.schoolLevel == "MIDDLE" || schoolBasicInfo.schoolLevel == "HIGH")>
            <%}%>
        </#if>
        <%}%>
    </table>
</script>
<script type="text/html" id="wait_trans_data">
    <div class="hd" style="font-size:.65rem;padding:0 .5rem;color:#636880;">
        <span>待转换数据</span>
    </div>
    <table>
        <tr class="tr_title">
            <td></td>
            <td>当月完成0套作业学生数</td>
            <td>当月完成1套作业学生数</td>
            <td>当月完成2套作业学生数</td>
        </tr>
        <%for(var key in res.subjectPerformanceMap){%>
        <% var subject = res.subjectPerformanceMap[key]; %>
    <#if schoolBasicInfo.schoolLevel?? && (schoolBasicInfo.schoolLevel == "MIDDLE" || schoolBasicInfo.schoolLevel == "HIGH")>
        <%if(subject.subjectName == "英语"){%>
    </#if>
        <tr class="tr_title">
            <td><%=subject.subjectName%></td>
            <td>
                <%=subject.finHwEq0StuCount%>
            </td>
            <td>
                <%=subject.finHwEq1StuCount%>
            </td>
            <td>
                <%=subject.finHwEq2StuCount%>
            </td>
        </tr>
    <#if schoolBasicInfo.schoolLevel?? && (schoolBasicInfo.schoolLevel == "MIDDLE" || schoolBasicInfo.schoolLevel == "HIGH")>
        <%}%>
    </#if>
        <%}%>
    </table>
</script>