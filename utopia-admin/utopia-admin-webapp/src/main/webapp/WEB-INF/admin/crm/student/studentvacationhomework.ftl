<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend><a href="/crm/student/studenthomepage.vpage?studentId=${(user.id)!}">${(user.profile.realname)!}</a>(${(user.id)!}) 假期作业详情</legend>
        </fieldset>
        <#setting datetime_format="yyyy-MM-dd HH:mm"/>
        <strong>英语作业（ID：${(englishVacationHomework.id)!}，开始时间：${(englishVacationHomework.startDate)!}，结束时间：${(englishVacationHomework.endDate)!}）</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th>PackageID</th>
                <th>作业详情</th>
            </tr>
            <#if englishVacationHomeworkPackages?has_content>
                <#list englishVacationHomeworkPackages as homeworkPackage>
                    <tr>
                        <#assign packageId = homeworkPackage.id!"">
                        <td><a href="/crm/student/studentvacationhomeworkpackage.vpage?groupId=${(englishVacationHomework.groupId)!}&packageId=${packageId}&subject=ENGLISH" target="_blank">${packageId}</a></td>
                        <td>
                            <#assign packageInfo = englishVacationHomeworkPackageInfos[homeworkPackage.id?string]>
                            <#if packageInfo??>
                                <div class="question-box">
                                    <#if packageInfo.practices?has_content>
                                        <#list packageInfo.practices as practice>
                                            <#assign practiceId = practice.practiceId?string>
                                            <a href="http://www.17zuoye.com/flash/loader/selfstudy-${practiceId!}-${(user.id)!}-${practice.bookId!}-${practice.unitId!}-${practice.lessonId!}.vpage" target="_blank">${vacationHomeworkPractices[practiceId]!}</a>
                                        </#list>
                                    </#if>
                                    <#if packageInfo.questions?has_content>
                                        <#list packageInfo.questions as question>
                                            <a href="http://www.17zuoye.com/container/viewpaper.vpage?qid=${question!}" target="_blank">${question!}</a>
                                        </#list>
                                    </#if>
                                </div>
                            </#if>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>

        <strong>数学作业（ID：${(mathVacationHomework.id)!}，开始时间：${(mathVacationHomework.startDate)!}，结束时间：${(mathVacationHomework.endDate)!}）</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th>PackageID</th>
                <th>作业详情</th>
            </tr>
            <#if mathVacationHomeworkPackages?has_content>
                <#list mathVacationHomeworkPackages as homeworkPackage>
                    <tr>
                        <#assign packageId = homeworkPackage.id!"">
                        <td><a href="/crm/student/studentvacationhomeworkpackage.vpage?groupId=${(mathVacationHomework.groupId)!}&packageId=${packageId}&subject=MATH" target="_blank">${packageId}</a></td>
                        <td>
                            <#assign packageInfo = mathVacationHomeworkPackageInfos[packageId?string]>
                            <#if packageInfo??>
                                <div class="question-box">
                                    <#if packageInfo.practices?has_content>
                                        <#list packageInfo.practices as practice>
                                            <#assign practiceId = practice.practiceId?string>
                                            <a href="http://www.17zuoye.com/flash/loader/mathselfstudy-${practiceId!}-${(user.id)!}-${practice.bookId!}-${practice.unitId!}-${practice.lessonId!}-${practice.pointId!}.vpage" target="_blank">${vacationHomeworkPractices[practiceId]!}</a>
                                        </#list>
                                    </#if>
                                    <#if packageInfo.questions?has_content>
                                        <#list packageInfo.questions as question>
                                            <a href="http://www.17zuoye.com/container/viewpaper.vpage?qid=${question!}" target="_blank">${question!}</a>
                                        </#list>
                                    </#if>
                                </div>
                            </#if>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>