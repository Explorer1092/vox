<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="老师判假">
<div id="contaier">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <div class="headerBack"><a href="/mobile/apply/index.vpage">&lt;&nbsp;返回</a></div>
                <div class="headerText">老师判假</div>
            </div>
        </div>
    </div>
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-message">
        <div class="list">
            <div class="activtaor active" active-value="PASS">
                <div class="hd red">${(fakeTeachers.PASS)?size}</div>
                <div class="ft">已判假</div>
            </div>
            <div class="activtaor" active-value="REJECT">
                <div class="hd red">${(fakeTeachers.REJECT)?size}</div>
                <div class="ft">被驳回</div>
            </div>
        </div>
    </div>
    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <ul class="mobileCRM-V2-list mobileCRM-V2-dropDown activity" active-value="PASS">
        <#if (fakeTeachers.PASS)?has_content>
            <#list fakeTeachers.PASS as fake>
                <li>
                    <div class="box">
                        <div class="box">
                            <div class="side-fl">${fake.teacherName!} (${fake.teacherId!})</div>
                            <#if fake.reviewer?? && fake.reviewer != "SYSTEM">
                                <div class="side-fr"><span class="qa red">人工</span></div>
                            </#if>
                        </div>
                        <div class="personalInfo">申请时间：${fake.createTime!}</div>
                        <div class="personalInfo">判假原因：${fake.fakeNote!}</div>
                        <div class="personalInfo">审核时间：${fake.reviewTime!}</div>
                        <div class="personalInfo">审核人：${fake.reviewerName!}</div>
                    </div>
                </li>
            </#list>
        </#if>
    </ul>
    <ul class="mobileCRM-V2-list mobileCRM-V2-dropDown activity" style="display: none" active-value="REJECT">
        <#if (fakeTeachers.REJECT)?has_content>
            <#list fakeTeachers.REJECT as fake>
                <li>
                    <div class="box">
                        <div><a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${fake.teacherId!}">${fake.teacherName!}</a>（${fake.teacherId!}）</div>
                        <a href="javascript:viewer.view('${fake.id!}');" class="link link-ico">
                            <div class="personalInfo">申请时间：${fake.createTime!}</div>
                            <div class="personalInfo">判假原因：${fake.fakeNote!}</div>
                            <div class="personalInfo">审核时间：${fake.reviewTime!}</div>
                        </a>
                    </div>
                    <div id="view-${fake.id!}" class="dropDown" style="display:none;">
                        <ul class="gray">
                            <li>审核人：${fake.reviewerName!}</li>
                            <li>取消排假原因：${fake.reviewNote!}</li>
                        </ul>
                    </div>
                </li>
            </#list>
        </#if>
    </ul>
</div>
<script>
    $(function () {
        activtaor.bind();
    });
</script>
</@layout.page>