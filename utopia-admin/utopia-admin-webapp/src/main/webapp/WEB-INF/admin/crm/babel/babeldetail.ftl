<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="vitalityLogList" type="java.util.List<com.voxlearning.utopia.admin.data.VitalityMapper>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>学生<a href="../student/studenthomepage.vpage?studentId=${userId!}">${userName!}</a>通天塔信息
            </legend>
        </fieldset>
        <ul class="inline">
            <li>
                <a class="btn" onclick="popModifyVitality()">修改精力</a>
            </li>
            <li>
                <button id="change_pk_gender" class="btn" onclick="popModifyItem()">修改道具</button>
            </li>
            <li>
                <button id="clear_role_bag_btn" class="btn" onclick="popModifyStar()">修改星星</button>
            </li>
        </ul>

        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr id="vitalitylog_title">
                    <td>开启楼层</td>
                    <td>星星</td>
                    <td>当前活力</td>
                    <td>持有道具数量</td>
                </tr>
                <tr >
                    <td>${role.floor!}层 第${role.stageIndex+1!}关</td>
                    <td id="starcount">${role.starCount!}</td>
                    <td id="vitalitycount">${vitality!}</td>
                    <td id="bagitem">${bag!"无道具"}</td>
                </tr>
            </table>
        </div>

        <fieldset>
            <legend>活力变更记录
            </legend>
        </fieldset>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr id="vitalitylog_title">
                    <td>时间</td>
                    <td>数量</td>
                    <td>变更前</td>
                    <td>变更后</td>
                    <td>来源</td>
                </tr>
                <#if vitalityHistory?has_content>
                    <#list vitalityHistory as vitalityLog>
                        <tr>
                            <td>${vitalityLog.changeTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${vitalityLog.changeCount!}</td>
                            <td>${vitalityLog.before!""}</td>
                            <td>${vitalityLog.after!}</td>
                            <td>${vitalityLog.addtionalInfo.description!}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>

        <fieldset>
            <legend>战斗记录
            </legend>
        </fieldset>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr id="vitalitylog_title">
                    <td>战斗时间</td>
                    <td>敌人</td>
                    <td>答题类型（2英语应用，3为数学应用）</td>
                    <td>是否胜利</td>
                    <td>奖励</td>
                    <td>得分（只在班级战开启的楼层有效）</td>
                </tr>
                <#if play?has_content>
                    <#list play as playLog>
                        <tr>
                            <td>${playLog.gameStartTime?string("yyyy-MM-dd HH:mm:ss")} 至 ${playLog.saveTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${playLog.floor!""}层 第${playLog.stageIndex + 1!""}关 第${playLog.npcIndex + 1!""}个怪</td>
                            <td>${playLog.questionType!}</td>
                            <td>${playLog.win?string('是','否')!}</td>
                            <td>
                                <#if playLog.reward?exists>
                                    ${BabelReward.toShowText(playLog.reward)}
                                </#if>
                            </td>
                            <td>
                                <#if playLog.additionalInfo.score?exists>
                                    ${playLog.additionalInfo.score!""}
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>

        <fieldset>
            <legend>星星变更记录
            </legend>
        </fieldset>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr id="vitalitylog_title">
                    <td>变更时间</td>
                    <td>数量</td>
                    <td>来源</td>
                    <td>结余</td>
                </tr>
                <#if star?has_content>
                    <#list star as starLog>
                        <tr>
                            <td>${starLog.createTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${starLog.amount!""}</td>
                            <td>${starLog.description!}</td>
                            <td>${starLog.balance!}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>

        <div id="vitaliti_mod" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3>修改活力 正数增加，负数减少</h3>
            </div>
            <div class="modal-body">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>数量</dt>
                            <dd ><input class="vitalityModCount" type="text"/></dd>
                        </li>
                    </ul>
                </dl>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary saveVitalityMod">保 存</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </div>

        <div id="star_mod" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3>修改星星数量 正数增加，负数减少</h3>
            </div>
            <div class="modal-body">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>数量</dt>
                            <dd ><input class="starModCount" type="text"/></dd>
                        </li>
                    </ul>
                </dl>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary saveStarMod">保 存</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </div>

        <div id="item_mod" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3>修改道具数量 正数增加，负数减少 请小心使用，以免道具被扣为负数！！</h3>
            </div>
            <div class="modal-body">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <#list itemList as it>
                            <li>
                                <dt>${it.getItemName()}:</dt>
                                <dd ><input class="itemModCount" itemId="${it.getItemId()}" type="text"/></dd>
                            </li>
                        </#list>
                    </ul>
                </dl>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary saveItemMod">保 存</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </div>
</div>
<script>
    var userId = ${userId!};
    $(function(){
        $(document).on("click",".saveVitalityMod",function(){
            var queryUrl="modVitality.vpage";
            $.post(queryUrl, {
                    userId : userId,
                    count : $(".vitalityModCount").val()
                }, function (data) {
                    if(!data.success){
                        alert("操作失败，请刷新页面重试");
                        return;
                    }
                    alert("操作完毕")
                    $("#vitalitycount").text(data.newBalance);
                        $("#vitaliti_mod").modal('hide');
                }
                ,'json');
        });

        $(document).on("click",".saveStarMod",function(){
            var queryUrl="modStar.vpage";
            $.post(queryUrl, {
                        userId : userId,
                        count : $(".starModCount").val()
                    }, function (data) {
                        if(!data.success){
                            alert("操作失败，请刷新页面重试");
                            return;
                        }
                        alert("操作完毕")
                        $("#starcount").text(data.role.starCount);
                        $("#star_mod").modal('hide');
                    }
                    ,'json');
        });

        $(document).on("click",".saveItemMod",function(){
            var queryUrl="modItem.vpage";
            var changeList = new Array();
            $(".itemModCount").each(function(){
                changeList.push({itemId:$(this).attr("itemId"),count:$(this).val()});
            });
            $.post(queryUrl, {
                        userId : userId,
                        changeList : JSON.stringify(changeList)
                    }, function (data) {
                        if(!data.success){
                            alert("操作失败，请刷新页面重试");
                            return;
                        }
                        alert("操作完毕")
                        $("#bagitem").text(data.bag);
                        $("#item_mod").modal('hide');
                    }
                    ,'json');
        });
    });
    function popModifyVitality(){
        $("#vitaliti_mod").modal('show');
        $("#vitalityModCount").val("");
    }

    function popModifyStar(){
        $("#star_mod").modal('show');
        $("#starModCount").val("");
    }

    function popModifyItem(){
        $("#item_mod").modal('show');
        $("#itemModCount").val("");
    }

</script>
</@layout_default.page>