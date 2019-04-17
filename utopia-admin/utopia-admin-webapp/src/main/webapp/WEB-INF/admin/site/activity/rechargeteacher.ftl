<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    span { font: "arial"; }
</style>
<div id="main_container" class="span9">
    <div>
        <form id="s_form" action="${requestContext.webAppContextPath}/site/activity/batchimportrechargeteacher.vpage" method="post" class="form-horizontal">
            <fieldset>
                <legend>各项均为必填项，请按规定格式填写</legend>
            </fieldset>
            <ul class="inline">
                <li>
                    学生数量：<input name="stuCount" placeholder="请填入正整数" />
                    充值金额：<input name="amount" placeholder="请填入正整数，单位（元）" />
                    老师ID：<textarea name="teacherIds" cols="45" rows="10" placeholder="换行符分隔，一行一条"></textarea>
                </li>
                <li>
                    <button id="editBut" type="submit" class="btn btn-primary">导入</button>
                </li>
            </ul>
        </form>
    </div>

    <div>
        <label>失败记录：</label>
        <table class="table table-bordered">
            <#if failedlist?has_content>
                <#list failedlist as index>
                    <tr>
                        <td>${index.teacherId!}</td>
                        <td>${index.msg!}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>
