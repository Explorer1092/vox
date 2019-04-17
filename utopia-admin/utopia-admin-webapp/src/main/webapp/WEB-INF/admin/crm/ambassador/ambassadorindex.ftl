<#-- @ftlvariable name="totalPageNum" type="java.lang.Integer" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#-- @ftlvariable name="ambassadorInfoList" type="java.util.List<java.util.Map>" -->
<#-- @ftlvariable name="authStateMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <div>
        <form action="?" method="post" class="form-horizontal">
            <fieldset>
                <legend>推荐认证</legend>
            </fieldset>
            <ul class="inline">
                <li>
                    <label>认证情况：<select name="authState">
                        <#if authStateMap?has_content>
                            <#list authStateMap?keys as authStateMapKey>
                                <option value="${authStateMapKey}" <#if authStateMapKey = (conditionMap.authState)!>selected="selected"</#if>>${authStateMap[authStateMapKey]}</option>
                            </#list>
                        </#if>
                    </select></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <button type="submit" id="submit" class="btn btn-primary">查询</button>
                </li>
                <li>
                    <input type="hidden" id="pageNum" name="pageNum" value="${(conditionMap.pageNum)!'0'}"/>
                    <input type="hidden" id="totalPageNum" name="pageNum" value="${(conditionMap.totalPageNum)!'0'}"/>
                </li>
            </ul>
        </form>
    </div>

    <div>
        <#if ambassadorInfoList??>
            <a id="first_page" href="javascript:void(0)">首页</a>
            <a id="pre_page" href="javascript:void(0)">上一页</a>
            第${((conditionMap.totalPageNum > 0)?string(conditionMap.pageNum + 1, 0))!'0'}页/共${(conditionMap.totalPageNum)!'0'}页
            <a id="next_page" href="javascript:void(0)">下一页</a>
            <a id="last_page" href="javascript:void(0)">末页</a>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <th></th>
                    <th>被推荐老师</th>
                    <th>校园大使</th>
                    <th>被推荐老师学校</th>
                    <th>认证情况</th>
                    <th>推荐时间</th>
                </tr>
                <#list ambassadorInfoList as ambassadorInfo>
                    <tr>
                        <td>${ambassadorInfo_index + 1}</td>
                        <td><a href="../teacher/teacherhomepage.vpage?teacherId=${ambassadorInfo.recommendedTeacherId!}">${ambassadorInfo.recommendedTeacherName!}</a>(${ambassadorInfo.recommendedTeacherId!})</td>
                        <td><a href="../teacher/teacherhomepage.vpage?teacherId=${ambassadorInfo.ambassadorId!}">${ambassadorInfo.ambassadorName!}</a>(${ambassadorInfo.ambassadorId!})</td>
                        <td><a href="../school/schoolhomepage.vpage?schoolId=${ambassadorInfo.schoolId!}">${ambassadorInfo.schoolName!}</a>(${ambassadorInfo.schoolId!})</td>
                        <td>${((ambassadorInfo.authState = 1)?string('已认证', '未认证'))!}</td>
                        <td>${(ambassadorInfo.createDatetime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                    </tr>
                </#list>
            </table>
        </#if>
    </div>
</div>
<script>
    $(function () {

        $('#first_page').click(function () {
            $('#pageNum').val(0);
            $('#submit').trigger('click');
        });

        $('#last_page').click(function () {
            $('#pageNum').val($('#totalPageNum').val());
            $('#submit').trigger('click');
        });

        $('#pre_page').on('click', function () {
            var $pageNum = $('#pageNum');
            var pageNum = parseInt($pageNum.val());
            $pageNum.val(pageNum - 1);
            $('#submit').trigger('click');
        });

        $('#next_page').on('click', function () {
            var $pageNum = $('#pageNum');
            var pageNum = parseInt($pageNum.val());
            $pageNum.val(pageNum + 1);
            $('#submit').trigger('click');
        });

    });
</script>
</@layout_default.page>