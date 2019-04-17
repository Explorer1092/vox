<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="青苗乐园" page_num=24>

<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<style xmlns="http://www.w3.org/1999/html">
    .panel-info {
        border-color: #bce8f1;
    }

    .panel {
        margin-bottom: 10px;
        background-color: #fff;
        border: 1px solid transparent;
        border-radius: 4px;
        -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
        box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
    }

    .panel-info > .panel-heading {
        color: #31708f;
        background-color: #d9edf7;
        border-color: #bce8f1;
    }

    .panel-heading {
        padding: 10px 15px;
        border-bottom: 1px solid transparent;
        border-top-left-radius: 3px;
        border-top-right-radius: 3px;
    }

    .panel-title {
        margin-top: 0;
        margin-bottom: 0;
        font-size: 16px;
        color: inherit;
    }

    .panel-body {
        padding: 15px;
    }

    .btn {
        height: 25px;
        font-size: 6px;
    }

    .table {
        width: 100%;
        margin-bottom: 0px;
    }

</style>

<span class="span9" style="font-size: 14px">
    <#include '../userinfotitle.ftl' />
    <form class="form-horizontal" action="/equator/newwonderland/pet/list.vpage" method="post">
        <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
        </#if>
        <ul class="inline selectbtn">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus"
                        placeholder="输入学生ID" onkeyup="value=value.replace(/[^\d]/g,'')"/>
            <input type="submit" value="查询" class="btn btn-primary"/>
        </ul>
    </form>

    <#setting datetime_format="yyyy-MM-dd HH:mm:ss"/>
    <div class="panel panel-info" style="width: 70%;display: inline-block;vertical-align: top;">
        <table class="table table-bordered">
            <div class="panel-heading">
                <h4 class="panel-title">
                    用户的宠物
                </h4>
            </div>
            <tr>
                <th style="text-align:center;vertical-align:middle;">用户宠物id</th>
                <th style="text-align:center;vertical-align:middle;">宠物类型</th>
                <th style="text-align:center;vertical-align:middle;">经验</th>
                <th style="text-align:center;vertical-align:middle;">当前阶段</th>
                <th style="text-align:center;vertical-align:middle;">总阶段</th>
                <th style="text-align:center;vertical-align:middle;">获得时间</th>
            </tr>
            <tbody id="tbody">
                <#if studentPet?? && (studentPet?size>0)>
                    <#list studentPet as pet>
                    <tr>
                        <td style="text-align:center;vertical-align:middle;">
                            ${pet.id?default('null')}
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            <#if pet.petType=='SaplingSprite'>
                                树灵
                            <#else>
                                其他类型
                            </#if>
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            ${pet.exp?default(0)}
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            ${pet.stage?default(1)}
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            ${pet.totalStage?default('null')}
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            <#if pet.ownTime??>
                                ${pet.ownTime?number_to_datetime}
                            </#if>
                        </td>
                    </tr>
                    </#list>
                <#else>
                    <td colspan="7" style="text-align:center;vertical-align:middle;">暂无</td>
                </#if>
            </tbody>
        </table>

    </div>
    <div id="grant_sun_dialog" class="modal hide fade">
        <div class="modal-header">
            <input type="hidden" name="sunType" id="sunType"/>
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>调整

                阳光</h3>
        </div>
        <div class="modal-body">
            <label for="adjustType">学科：<select id="adjustType">
                        <option value="ChineseSun">语文</option>
                        <option value="EnglishSun">英语</option>
                        <option value="MathSun">数学</option>
                        <option value="OtherSun">其他</option>
                    </select></label>

            <label for="contractDays">增加阳光数：<input id="sunNum" type="number" min="0"/></label>
        <#--<label for="newExpireDate" id="dateLabel">过期时间：-->
        <#--<input id="newExpireDate" type="text" class="input-large" placeholder="过期日期" name="newExpireDate"-->
        <#--value="">-->
        <#--</label>-->
        </div>
        <div class="modal-footer">
            <button id="grant_sun_dialog_confirm_btn" class="btn btn-primary">确定</button>
            <button class="btn btn-primary" data-dismiss="modal">取消</button>
        </div>
    </div>
</span>
<script>
    $(function () {

        $("#grant_sun_dialog_confirm_btn").click(function () {
            var studentId = $('#studentId').val();
            var sunType = $('#adjustType').val();
            var sunNum = $('#sunNum').val();
            $.post('/equator/newwonderland/sapling/acquiresubjectsuns.vpage', {
                'studentId': studentId,
                'sunType': sunType,
                'sunNum': sunNum
            }, function (data) {
                if (data.success) {
                    location.reload();
                } else {
                    alert(data.info);
                }
            });
        })

    });

</script>
</@layout_default.page>