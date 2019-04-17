<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <form method="post" action="batchawardsend.vpage">
        <ul class="inline">
            <li>
                <label>
                    输入奖励内容：<textarea name="content" cols="100" rows="10" placeholder="请在这里输入要发送的用户ID,类型,内容及数量（请在excel里编辑好，直接贴进来）"></textarea>
                    <div style="margin: -202px 363px; position: absolute;">
                        注意：PK时装不需要输入数量,通天塔宠物蛋需要输入ID，例如：<br />
                        <a href="javascript:void(0);" onclick="javascript:showDialog();">点击查看宠物蛋列表：</a><br />
                        <img src="/public/img/award_examples.jpg" alt=""/>
                    </div>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <input class="btn" type="submit" value="提交" />
            </li>
        </ul>
    </form>
    <div>
        <label>统计：</label>
        <table class="table table-bordered">
            <tr>
                <td>发送成功：</td><td><#if successlist??>${successlist?size}</#if>件</td>
                <td>发送失败：</td><td><#if failedlist??>${failedlist?size}</#if>件</td>
            </tr>
        </table>
        <label>失败记录：</label>
        <table class="table table-bordered">
            <#if failedlist??>
                <#list failedlist as l>
                    <tr>
                        <td>${l}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<div id="pet_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>宠物蛋列表</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal inline">
            <table class="table table-bordered">
                <tr>
                    <td>宠物蛋ID</td>
                    <td>宠物蛋名称</td>
                </tr>
                <#if babelPets??>
                    <#list babelPets as pet>
                        <tr>
                            <td>${pet.id!}</td>
                            <td>${pet.petName!}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">确定</button>
    </div>
</div>
<script type="text/javascript">
    function showDialog() {
        $('#pet_dialog').modal("show");
    }
</script>
</@layout_default.page>
