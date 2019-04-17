<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" style="margin-bottom: 20px;">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="box">
                <div class="side-fl">分校 <span style="color: #999">（填写缺失年级所在学校）</span></div>
            </div>
        </li>
    <#if branchSchools?has_content>
        <#list branchSchools as school>
            <li class="js-branchSchoolItem">
                <a  <#if locked?? && locked> <#else> class="save_session"</#if> data-value="${school.schoolId}" href="javascript:void(0)">
                    <div class="link link-ico">
                        <div class="side-fl">${school.schoolName!''}</div>
                    </div>
                </a>
            </li>
        </#list>
    </#if>
        <li>
            <a class="save_session" data-value="add_school" id="addBranchSchool" href="javaScript:void(0)">
                <div class="link link-ico">
                        <div class="side-fl"><span class="add_branch_school_icon"></span>添加分校</div>
                </div>
            </a>
        </li>
    </ul>
</div>