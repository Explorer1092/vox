<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="通天塔关卡配置">
<div class="span9">

    <!-- student login -->
    <div id="npc_edit" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>编辑NPC</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>所处位置</dt>
                        <dd class="npcPos"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>NPC原型</dt>
                        <dd ><select class="npcPrototype">

                        </select></dd>
                    </li>
                    <li>
                        <dt>NPC类型</dt>
                        <dd ><select class="npcType">
                        </select></dd>
                    </li>
                    <li>
                        <dt>HP</dt>
                        <dd ><input class="npcHp" type="text"/></dd>
                    </li>
                    <li>
                        <dt>基础攻击</dt>
                        <dd ><input class="npcBaseAp" type="text"/></dd>
                    </li>
                    <li>
                        <dt>特殊攻击</dt>
                        <dd ><input class="npcAdvAp" type="text"/></dd>
                    </li>
                    <li>
                        <dt>基础伤害</dt>
                        <dd ><input class="npcBaseDp" type="text"/></dd>
                    </li>
                    <li>
                        <dt>特殊伤害</dt>
                        <dd ><input class="npcAdvDp" type="text"/></dd>
                    </li>
                    <li>
                        <dt>npc简介</dt>
                        <dd ><textarea class="npcDesc"></textarea></dd>
                    </li>


                    <li>
                        <dt><button class="btn btn-primary addReward">添加奖励</button></dt>

                    </li>
                </ul>
                <input type="hidden" class="curEditNpc"/>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary savenpc">保 存</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="floor_reward_edit" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>编辑开启楼层奖励</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>楼层：</dt>
                        <dd class="rewardFloorNo"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt><button class="btn btn-primary addFloorRewardBtn">添加奖励</button></dt>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary saveFloorReward">保 存</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="boss_fight_edit" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>编辑BOSS战奖励</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline ">
                    <li>
                        <dt><button class="btn btn-primary addBossTopPrize">添加奖项</button></dt>
                    </li>

                </ul>
                <ul class="inline" >
                    <li>
                        <dt><button class="btn btn-primary addBossPartPrize">添加分段</button></dt>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary saveBossPrize">保 存</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="clazz_battle_edit" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h4>班级抢塔配置<span>(开战时间:_from201420231123_to_212321232211)</span></h4>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline ">
                    <li>
                        <dt>楼层</dt>
                        <dd><input id="clazzBattleOpenFloor" type="text" value=""/></dd>
                    </li>
                    <li>
                        <dt>参赛完成作业人数</dt>
                        <dd><input id="clazzBattleFinishCount" type="text" value=""/></dd>
                    </li>
                </ul>
                <ul class="inline" >
                    <li>
                        <dt><button class="btn btn-primary addClazzBattlePrize">添加奖励</button></dt>
                    </li>
                </ul>
            </dl>
            <input type="hidden" id="clazzBattleId" value=""/>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary saveClazzBattlePrize">保 存</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>
<script>
var stageData=null;
var allNpc=null;
var allPet=null;
var npcType = ["NORMAL","EXPERT","BOSS"];
var rewardType = null;
var itemType = null;
var pkItem = null;
var bossPrizeConf = null;
function getStageConf(callback){
    var queryUrl = "stageConfList.vpage";
    $.post(queryUrl, {}, function (data) {
                stageData = data;
                if(undefined !=callback && null != callback){
                    callback();
                }
            }
            ,'json');
}

function getPkItem(callback){
    var queryUrl = "listPkItem.vpage";
    $.post(queryUrl, {}, function (data) {
                pkItem = data;
                if(undefined !=callback && null != callback){
                    callback();
                }
            }
            ,'json');
}

function getNpcConf(callback){
    var queryUrl = "npcList.vpage";
    $.post(queryUrl, {}, function (data) {
                allNpc = data;
                if(undefined !=callback && null != callback){
                    callback();
                }
            }
            ,'json');
}

function getPetConf(callback){
    var queryUrl = "petList.vpage";
    $.post(queryUrl, {}, function (data) {
                allPet = data;
                if(undefined !=callback && null != callback){
                    callback();
                }
            }
            ,'json');
}

function getRewardTypeList(){
    var queryUrl = "listRewardType.vpage";
    $.post(queryUrl, {}, function (data) {
                rewardType = data;
            }
            ,'json');
}

function getItemTypeList(callback){
    var queryUrl = "listItemType.vpage";
    $.post(queryUrl, {}, function (data) {
                itemType = data;
                if(undefined !=callback && null != callback){
                    callback();
                }
            }
            ,'json');
}

function getBossPrizeConf(callback){
    var queryUrl = "listBossPrize.vpage";
    $.post(queryUrl, {}, function (data) {
        bossPrizeConf = data;
        if(undefined !=callback && null != callback){
            callback();
        }
    });
}
$(function() {
    getPkItem(function(){
        getNpcConf(function(){
            getPetConf(function(){
                getRewardTypeList();
                getItemTypeList(function(){
                    getStageConf(function(){
                        for(var i = 0 ; i < stageData.length ; i++){
                            $(".span9").append('<legend>第'+stageData[i].floorNo+'层</legend>');
                            $(".span9").append('<legend>开启楼层奖励:'+floorRewardText(stageData[i].firstEntryReward)+'<button class="btn btn-info editFloorReward" floor="'+stageData[i].floorNo+'" type="button">编辑</button></legend>');

                            var content='<table class="table table-hover table-striped table-bordered">'
                                    +'<tbody><tr>'
                                    +'<th>关号</th>'
                                    +'<th>怪物1</th>'
                                    +'<th>怪物2</th>'
                                    +'<th>怪物3</th>'
                                    +'<th>怪物4</th>'
                                    +'<th>怪物5</th></tr>'
                            for(var j = 0 ; j < stageData[i].stageList.length; j++){
                                content +='<tr><td><div>第'+(j+1)+'关</div>';
                                content +='<br><button class="btn btn-danger deleteStage" stageindex="'+j+'" floor="'+(stageData[i].floorNo)+'" type="button">删除关卡</button>'
                                content +='</td>'
                                for(var k = 0 ; k < stageData[i].stageList[j].npcList.length; k++){
                                    content +='<td class="npctd">';
                                    content += makeStageNpcTd(stageData[i].stageList[j].npcList[k],stageData[i].floorNo,j,k)
                                    content +='</td>';
                                }
                                if(stageData[i].stageList[j].npcList.length < 5){
                                    content +='<td><input type="button" class="btn btn-primary appendNpc" value="点此添加一个npc" /></td>'
                                }
                                content +='</tr>'
                            }
                            content +='<tr><td><input type="button" floor="'+stageData[i].floorNo+'" class="btn btn-primary appendStage" value="点此添加一个关卡" /><td></tr>';
                            content +='</tbody></table>';
                            $(".span9").append(content);


                        }
                        $(".appendStage:last").after('<button class="btn btn-danger deleteFloor" type="button">删除本层</button>');
                        $(".span9").append('<button class="btn btn-primary addFloor" type="button">添加楼层</button>');

                        getBossPrizeConf(function(){
                            $("legend:first").before('<button type="button" class="btn btn-danger editBossFight">编辑BOSS战奖励</button><br><br>')
                            $("legend:first").before('<button type="button" class="btn btn-danger editThisClazzBattle">编辑当期班级争霸奖励</button><br><br>')
                            $("legend:first").before('<button type="button" class="btn btn-danger editNextClazzBattle">编辑下期班级争霸奖励</button>')
                        });
                    });
                });
            });
        });
    });



    $(document).on("click",".editNpc",function(){
        var floor=parseInt($(this).attr("floor"));
        var stageIndex=parseInt($(this).attr("stageIndex"));
        var npcIndex=parseInt($(this).attr("npcIndex"));
        var npcObj = stageData[floor-1].stageList[stageIndex].npcList[npcIndex];
        popSaveNpcDialog("编辑NPC",npcObj,floor,stageIndex,npcIndex);
        var sourceTd = $(this).parent("td");
        bindSaveNpcClick(afterEditNpcSuccessHtml,sourceTd);
    });

    $(document).on("click",".appendNpc",function(){
        var lastBro = $(this).parent("td").siblings("td:last").find("button:first");
        var floor=parseInt($(lastBro).attr("floor"));
        var stageIndex=parseInt($(lastBro).attr("stageIndex"));
        var npcIndex=parseInt($(lastBro).attr("npcIndex"))+1;
        var npcObj = allNpc[0];
        popSaveNpcDialog("为关卡添加一个NPC",npcObj,floor,stageIndex,npcIndex);
        var sourceTd = $(this).parent("td");
        bindSaveNpcClick(afterAppendNpcSuccessHtml,sourceTd);
    });

    $(document).on("click",".appendStage",function(){
        var floor=parseInt($(this).attr("floor"));
        var stageIndex=parseInt($(this).parents("tr:first").siblings("tr").length)-1;
        var npcIndex=0;
        var npcObj = allNpc[0];
        popSaveNpcDialog("为第"+floor+"层的新关卡添加一个NPC",npcObj,floor,stageIndex,npcIndex);
        var sourceTd = $(this).parent("td");
        bindSaveNpcClick(afterAppendStageSuccessHtml,sourceTd);
    });

    $(document).on("click",".addFloor",function(){
        var floor=$(".appendStage").length+1;
        var stageIndex=0
        var npcIndex=0;
        var npcObj = allNpc[0];
        popSaveNpcDialog("为第"+floor+"层的第一关添加一个NPC",npcObj,floor,stageIndex,npcIndex);
        var sourceTd = $(this).parent("td");
        bindSaveNpcClick(afterAppendFloorSuccessHtml,sourceTd);
    });

    $(document).on("click",".editFloorReward",function(){
        var floor=parseInt($(this).attr("floor"));
        popEditFloorReward(floor);
    });


    $(document).on("click",".deleteNpc",function(){
        var thisFloorStageLength = $(this).parents("td:first").siblings(".npctd").length
        if(thisFloorStageLength < 1){
            alert("本关只剩一个npc，不可删除");
            return;
        }
        if(confirm("真的要删除这个npc吗？\n"+$(this).parent("td").find("div:first").text())){
            var floor = parseInt($(this).attr("floor"));
            var stageIndex = parseInt($(this).attr("stageIndex"));
            var npcIndex = parseInt($(this).attr("npcIndex"));
            var queryUrl = "deleteStageNpc.vpage";
            var parentTd = $(this).parent("td");
            $.post(queryUrl, {
                        floor:floor,
                        stageIndex:stageIndex,
                        npcIndex:npcIndex
                    }, function (data) {
                        if(!data.success){
                            alert("删除失败");
                            return;
                        }

                        $(parentTd).siblings(".npctd").each(function(){
                            var siblingNpcIndex = parseInt($(this).find(".deleteNpc").attr("npcIndex"));
                            if(siblingNpcIndex > npcIndex){//修正删除npc身后的npc的npcIndex
                                siblingNpcIndex--;
                                $(this).find(".deleteNpc").attr("npcIndex",siblingNpcIndex);
                                $(this).find(".editNpc").attr("npcIndex",siblingNpcIndex);
                            }
                        });
                        if($(parentTd).parent("tr").find(".npctd").length ==5){
                            $(parentTd).parent("tr").find(".npctd:last").after('<td><input type="button" value="点此追加关卡npc" class="btn btn-primary appendNpc"></td>');
                        }
                        $(parentTd).remove();//移除被删除npc对应的td
                        stageData=data.allFloor;//刷新页面结构
                    }
                    ,'json');
        }
    });

    $(document).on("click",".deleteStage",function(){
        var thisFloorStageLength = $(this).parents("tr:first").siblings("tr").length-1
        if(thisFloorStageLength < 2){
            alert("本层只剩一个关卡，不可删除");
            return;
        }
        if(confirm("真的要删除这个关卡吗？关卡内npc也会一并删除\n")){
            var floor = parseInt($(this).attr("floor"));
            var stageIndex = parseInt($(this).attr("stageIndex"));
            var queryUrl = "deleteStage.vpage";
            var parentTd = $(this).parent("td");
            $.post(queryUrl, {
                        floor:floor,
                        stageIndex:stageIndex
                    }, function (data) {
                        if(!data.success){

                            alert("删除失败");
                            return;
                        }
                        stageData = data.allFloor;
                        $(parentTd).parent("tr").siblings("tr").each(function(){
                            var oldIndex = parseInt($(this).find("td:first").find("button").attr("stageIndex"))
                            if(oldIndex > stageIndex){
                                oldIndex--;
                                $(this).find("td:first").find("div").text("第"+(oldIndex+1)+"关");
                                $(this).find("td:first").find("button").attr("stageIndex",oldIndex);

                            }
                        });
                        $(parentTd).parent("tr").remove();
                    }
                    ,'json');
        }
    });

    $(document).on("click",".deleteFloor",function(){
        var queryUrl = "deleteFloor.vpage";
        if(confirm("真的要删除这个楼层吗？楼层内的所有关卡会一并删除")){
            $.post(queryUrl, {
                    }, function (data) {
                        if(!data.success){
                            alert("删除失败");
                            return;
                        }
                        stageData = data.allFloor;
                        $("table:last").remove();
                        $(".editFloorReward").parent("legend").remove();
                        $("legend:last").remove();
                    }
                    ,'json');
        }
        $(".appendStage:last").after('<button type="button" class="btn btn-danger deleteFloor">删除本层</button>');
    });

    $(document).on("click",".addReward",function(){
        var rewardIndex = $(".beatReward").length+1;
        var html ='<li class="beatReward">'
                +'<dt>击败奖励'+rewardIndex+'</dt>'
                +'<dd>类型'
                +'<select class="rewardType">';
        for(var i = 0 ; i < rewardType.length; i++){
            html +='<option value="'+rewardType[i].name+'" >'+rewardType[i].chnName+'</option>'
        }
        html +='</select>'
                +'</dd>'
                +'<dd style="display:none;" class="itemIdDd">道具/宠物<select class="itemId"></select></dd>'
                +'<dd>数量<input type="text" class="rewardQuantity"></dd>'
                +'<dd>概率<input type="text" class="rewardRate"></dd>'
                +'<dd>每把钥匙概率<input type="text" class="keyRateUp"></dd>'
                +'<dd>钥匙上限<input type="text" class="keyMaxCount">把</dd>'
                +'</li>';
        $(this).parents("li:first").before(html);
//            $(".beatReward:last").after(html);
    });

    $(document).on("click",".npcPrototype",function(){
        var selectIndex = $(this).get(0).selectedIndex;
        var npcPrototype = allNpc[selectIndex];
        $(".npcBaseAp").val(npcPrototype.baseAp);
        $(".npcAdvAp").val(npcPrototype.advAp);
        $(".npcBaseDp").val(npcPrototype.baseDp);
        $(".npcAdvDp").val(npcPrototype.advDp);
        $(".npcDesc").val(npcPrototype.npcDesc);
    });

    $(document).on("click",".rewardType",function(){
        if(findRewardTypeByRewardName($(this).val()).idRequired){
            $(this).parent("dd").siblings(".itemIdDd").show();
            $(this).parent("dd").siblings(".itemIdDd").find("select").html(buildRewardIdSelect($(this).val(),null));
        }else{
            $(this).parent("dd").siblings(".itemIdDd").hide();
        }
    });

    $(document).on("click",".addFloorRewardBtn",function(){
        $(this).parents("ul:first").before(rewardEditHtml(null));
    });

    $(document).on("click",".floorRewardType",function(){
        if(findRewardTypeByRewardName($(this).val()).idRequired){
            $(this).parents("li:first").siblings(".floorItemId").show();
            $(this).parents("li:first").siblings(".floorItemId").find("select").html(buildRewardIdSelect($(this).val(),null));

        }else{
            $(this).parents("li:first").siblings(".floorItemId").hide();
        }
    });

    $(document).on("click",".saveFloorReward",function(){
        var paramArray = new Array();
        var floor = $("#floor_reward_edit").find(".rewardFloorNo").text();
        $(".addFloorRewardUl").each(function(){
            var rewardObj = {};
            rewardObj.itemId=$(this).find(".floorRewardItemId").val();
            rewardObj.rewardType=$(this).find("select:first").val();
            rewardObj.count = $(this).find(".floorRewardCount").val();
            if($.trim(rewardObj.count)=="" || $.trim(rewardObj.count)=="0"){
                return;
            }
            paramArray.push(rewardObj);
        });
        var queryUrl="saveFloorReward.vpage";
        $.post(queryUrl, {
                    floor:floor,
                    rewardList:JSON.stringify(paramArray)
                }, function (data) {
                    stageData = data.allFloor;
                    $(".editFloorReward").each(function(){
                        if($(this).attr("floor")==floor){
                            var legend = $(this).parent("legend");
                            legend.html('开启楼层奖励:'+floorRewardText(paramArray)+'<button type="button" floor="'+floor+'" class="btn btn-info editFloorReward">编辑</button>');
                        }
                    });
                    floorRewardText(paramArray);
                    $("#floor_reward_edit").modal("hide");
                }
                ,'json');
    });


    $(document).on("click",".editBossFight",function(){
        $("#boss_fight_edit").modal('show')
        $(".topPrize").remove()
        $(".partPrize").remove()
        var topPrize = bossPrizeConf.topPrize;
        var topHtml = '';
        for(var i = 0 ; i < topPrize.length; i++){
            topHtml += '<li class="topPrize">'+
                    '<dt>第'+(i+1)+'名奖励：</dt>'+
                    '<dd ><select class="allPrize" style="width:100px;">'+buildRewardTypeSelect(topPrize[i].rewardType)+'</select><select style="width:100px;">'+buildRewardIdSelect(topPrize[i].rewardType,topPrize[i].itemId)+'</select><input type="text" style="width:15px;" value="'+topPrize[i].count+'"/>个&nbsp;</dd>'+
                    '</li>';

        }
        $(".addBossTopPrize").parent().before(topHtml);
        $(".topPrize:last").find("dd").append('<button type="button" class="btn btn-danger delTopPrize">删除</button>');

        var allPrize = bossPrizeConf.allPrize;
        var allHtml = '';
        for(var i = 0 ; i < allPrize.length; i++){
            allHtml += '<li class="partPrize">'+
                    '<dt>分段奖励：</dt>'+
                    '<dd ><input type="text" style="width:25px;" value="'+allPrize[i].divider+'"/>% <select style="width:80px;" class="partSelect">'+buildRewardTypeSelect(allPrize[i].prize.rewardType)+'</select><select style="width:80px;">'+buildRewardIdSelect(allPrize[i].prize.rewardType,allPrize[i].prize.itemId)+'</select><input type="text" style="width:15px;" value="'+allPrize[i].prize.count+'"/>个&nbsp;<button type="button" class="btn btn-danger delAllPrize">删除</button></dd>'+
                    '</li>';

        }
        $(".addBossPartPrize").parent().before(allHtml);
        $("select").each(function(){
            if('' == $(this).html()){
                $(this).hide();
            }
        });
    });

    var fillClazzBattleHtml = function(data){
        $("#clazz_battle_edit").find("h4").find("span").text('(开战时间:'+data.id+')');
        $("#clazz_battle_edit").find("#clazzBattleId").val(data.id);
        $("#clazz_battle_edit").find("#clazzBattleOpenFloor").val(data.openFloor);
        $("#clazz_battle_edit").find("#clazzBattleFinishCount").val(data.minDoneHomeworkCount);
        var topHtml = '';
        $(".clazzBattlePrizeList").remove();
        for(var i = 0 ; i < data.winnerPrizeList.length; i++){
            var reward = data.winnerPrizeList[i];
            topHtml += '<li class="clazzBattlePrizeList">'+
                    '<dt>奖励</dt>'+
                    '<dd ><select class="allPrize" style="width:100px;">'+buildRewardTypeSelect(reward.rewardType)+'</select><select style="width:100px;">'+buildRewardIdSelect(reward.rewardType,reward.itemId)+'</select><input type="text" style="width:15px;" value="'+reward.count+'"/>个&nbsp;<input type="button" class="btn btn-danger deleteClazzPrize" value="删除"/></dd>'+
                    '</li>';
        }
        $(".addClazzBattlePrize").parent().before(topHtml);
        $("select").each(function(){
            if('' == $(this).html()){
                $(this).hide();
            }
        });
    }
    $(document).on("click",".editThisClazzBattle",function(){
        $("#clazz_battle_edit").modal("show");
        var queryUrl="getThisClazzBattleConf.vpage";
        $.post(queryUrl, {
                }, function (data) {
                    fillClazzBattleHtml(data);
                }
                ,'json');
    });
    $(document).on("click",".addClazzBattlePrize",function(){
        var topHtml = '<li class="clazzBattlePrizeList">'+
                '<dt>奖励</dt>'+
                '<dd ><select class="allPrize" style="width:100px;">'+buildRewardTypeSelect("PK_ITEM")+'</select><select style="width:100px;">'+buildRewardIdSelect("PK_ITEM","")+'</select><input type="text" style="width:15px;"/>个&nbsp;<input type="button" class="btn btn-danger deleteClazzPrize" value="删除"/></dd>'+
                '</li>';
        $(".addClazzBattlePrize").parent().before(topHtml);
    });

    $(document).on("click",".editNextClazzBattle",function(){
        $("#clazz_battle_edit").modal("show");
        var queryUrl="getNextClazzBattleConf.vpage";
        $.post(queryUrl, {
                }, function (data) {
                    fillClazzBattleHtml(data);
                }
                ,'json');
    });

    $(document).on("click",".delTopPrize",function(){
        $(".topPrize:last").remove();
        $(".topPrize:last").find("dd").append('<button type="button" class="btn btn-danger delTopPrize">删除</button>');
    });

    $(document).on("click",".delAllPrize",function(){
        $(this).parents(".partPrize").remove();
    });

    $(document).on("click",".addBossTopPrize",function(){
        var topHtml = '<li class="topPrize">'+
                '<dt>第'+($(".topPrize").length+1)+'名奖励：</dt>'+
                '<dd ><select style="width:100px;" class="allPrize">'+buildRewardTypeSelect("PK_ITEM")+'</select><select style="width:100px;">'+buildRewardIdSelect("PK_ITEM","")+'</select><input type="text" style="width:15px;" value="1"/>个&nbsp;</dd>'+
                '</li>';
        $(".addBossTopPrize").parent().before(topHtml);
        $(".delTopPrize").remove();
        $(".topPrize:last").find("dd").append('<button type="button" class="btn btn-danger delTopPrize">删除</button>');
    });

    $(document).on("click",".addBossPartPrize",function(){
        var allHtml = '<li class="partPrize">'+
                '<dt>分段奖励：</dt>'+
                '<dd ><input type="text" style="width:25px;" />% <select class="partSelect" style="width:80px;">'+buildRewardTypeSelect("BABEL_STAR")+'</select><select style="width:80px;">'+buildRewardIdSelect("BABEL_STAR","")+'</select><input type="text" style="width:15px;" value="1"/>个&nbsp;<button type="button" class="btn btn-danger delAllPrize">删除</button></dd>'+
                '</li>';
        $(".addBossPartPrize").parent().before(allHtml);
    });

    $(document).on("click",".partSelect",function(){
        if(findRewardTypeByRewardName($(this).val()).idRequired){

            $(this).siblings("select").show();
            $(this).siblings("select").html(buildRewardIdSelect($(this).val(),null));
        }else{
            $(this).siblings("select").hide();
        }
    });

    $(document).on("click",".allPrize",function(){
        if(findRewardTypeByRewardName($(this).val()).idRequired){
            $(this).siblings("select").show();
            $(this).siblings("select").html(buildRewardIdSelect($(this).val(),null));
        }else{
            $(this).siblings("select").hide();
        }
    });

    $(document).on("click",".clazzPrizeType",function(){
        if(findRewardTypeByRewardName($(this).val()).idRequired){

            $(this).siblings("select").show();
            $(this).siblings("select").html(buildRewardIdSelect($(this).val(),null));
        }else{
            $(this).siblings("select").hide();
        }
    });

    $(document).on("click",".saveBossPrize",function(){
        var topPrizeArray = new Array();
        var checkOk = true
        $(".topPrize").each(function(){
            if(checkOk){
                var rewardTypeTop = $(this).find("select:first").val()
                var itemIdTop = $(this).find("select:last").is(":visible")?$(this).find("select:last").val():"";
                var countTop = $(this).find("input").val();
                if(!$.isNumeric(countTop) || parseInt(countTop) < 1){
                    alert("奖励数值不可为空，不可小于1");
                    checkOk=false
                    return;
                }
                topPrizeArray.push({rewardType:rewardTypeTop,itemId:itemIdTop,count:countTop});
            }
        });
        if(!checkOk){
            return;
        }

        var allPrizeArray = new Array();
        var divider = 0
        $(".partPrize").each(function(){
            if(checkOk){
                var dividerAll = $(this).find("input:first").val();
                var rewardTypeAll = $(this).find("select:first").val()
                var itemIdAll = $(this).find("select:last").is(":visible")?$(this).find("select:last").val():"";
                var countAll = $(this).find("input:last").val();
                if(!$.isNumeric(countAll) || parseInt(countAll) < 1 || !$.isNumeric(dividerAll) || parseInt(dividerAll) < 1){
                    alert("请输入完整数据。分段奖励数值不可为空，不可小于1");
                    checkOk=false
                    return;
                }
                if(divider >= parseInt(dividerAll) || parseInt(dividerAll) > 100){
                    alert("分段区间不正确。区间需从小到大，不可重复，不可超过100")
                    checkOk=false
                    return;
                }
                divider = parseInt(dividerAll)
                allPrizeArray.push({divider:dividerAll,rewardType:rewardTypeAll,itemId:itemIdAll,count:countAll});
            }
        });
        if(!checkOk){
            return;
        }

        var queryUrl="saveBossPrizeConf.vpage";
        $.post(queryUrl, {
                    prizeConf:JSON.stringify({
                        topPrize:topPrizeArray,
                        allPrize:allPrizeArray
                    })
                }
                , function (data) {
                    bossPrizeConf = data;
                    alert("保存成功")
                    $("#boss_fight_edit").modal("hide")
                }
                ,'json');
    });

    $(document).on("click",".saveClazzBattlePrize",function(){
        var topPrizeArray = new Array();
        var checkOk = true
        $(".clazzBattlePrizeList").each(function(){
            var rewardTypeTop = $(this).find("select:first").val()
            var itemIdTop = $(this).find("select:last").is(":visible")?$(this).find("select:last").val():"";
            var countTop = $(this).find("input").val();
            if(!$.isNumeric(countTop) || parseInt(countTop) < 1){
                alert("奖励数值不可为空，不可小于1");
                checkOk=false
                return;
            }
            topPrizeArray.push({rewardType:rewardTypeTop,itemId:itemIdTop,count:countTop});
        });
        if(!checkOk){
            return;
        }
        var queryUrl="saveClazzBattleConf.vpage";
        $.post(queryUrl, {
            openFloor:$("#clazzBattleOpenFloor").val(),
            id:$("#clazzBattleId").val(),
            finishCount:$("#clazzBattleFinishCount").val(),
            prizeList:JSON.stringify(topPrizeArray)
        }, function(data){
            if(data.success){
                alert("保存成功");
            }else{
                alert("失败:"+data.reason)
            }
            $("#clazz_battle_edit").modal("hide");
        },'json');
    });

    $(document).on("click",".deleteClazzPrize",function(){
        $(this).parents("li:first").remove();
    });

});


function makeStageNpcTd(npcObj,floor,stageIndex,npcIndex){
    var content ='<div>npc原型Id:'+npcObj.id
            +'<br>npc名:'+npcObj.name
            +'<br>npc类型:'+npcObj.npcType
            +'<br>hp:'+npcObj.hp+'<br>攻击属性:'+npcObj.attackType
            +'<br>基础攻击:'+npcObj.baseAp
            +'<br>特殊攻击:'+npcObj.advAp
            +'<br>基础伤害:'+npcObj.baseDp
            +'<br>特殊伤害:'+npcObj.advDp
            +'<br>npc简介:'+npcObj.npcDesc;
    var rewardStr='';
    if(null != npcObj.reward){
        for(var l = 0 ; l < npcObj.reward.length; l++){
            var rewardObj = npcObj.reward[l];
            var itemNameHtml = '';
            if(findRewardTypeByRewardName(rewardObj.rewardType).idRequired){
                itemNameHtml = ' '+findItemObjByRewardObj(rewardObj);
            }
            rewardStr+='<br>{'+findRewardTypeByRewardName(rewardObj.rewardType).chnName
                    +itemNameHtml
                    +' '+rewardObj.count+'个'
                    +',<br>掉落概率:'+rewardObj.rewardRate
                    +',<br>每把钥匙提升概率:'+rewardObj.keyRate
                    +',<br>使用钥匙上限:'+rewardObj.maxKeyCount+'},';
        }

    }
    content +='<br>击败奖励:['+rewardStr+']</div>';
    content +='<button type="button" floor="'+floor+'" stageIndex="'+stageIndex+'" npcIndex="'+npcIndex+'" class="btn btn-info editNpc">编辑</button>&nbsp;'
    content +='<button type="button" floor="'+floor+'" stageIndex="'+stageIndex+'" npcIndex="'+npcIndex+'" class="btn btn-danger deleteNpc">删除</button>'
    return content
}

function afterAppendNpcSuccessHtml(npcObj,floor,stageIndex,npcIndex,td){
    var html = makeStageNpcTd(npcObj,floor,stageIndex,npcIndex);
    $(td).html(html);
    $(td).addClass("npctd");
    if($(td).parent("tr").find(".npctd").length<5){
        $(td).after('<td><input type="button" value="点此追加关卡npc" class="btn btn-primary appendNpc"></td>');
    }
}

function afterAppendStageSuccessHtml(npcObj,floor,stageIndex,npcIndex,td){
    var trHtml='<tr><td><div>第'+(stageIndex+1)+'关</div><br><button type="button" floor="'+floor+'" stageindex="'+stageIndex+'" class="btn btn-danger deleteStage">删除关卡</button></td><td class="npctd">'+makeStageNpcTd(npcObj,floor,stageIndex,npcIndex)+'</td></tr>'
    $(td).parent("tr").before(trHtml);
}

function afterAppendFloorSuccessHtml(npcObj,floor,stageIndex,npcIndex,td){
    var html = '<legend>第'+floor+'层</legend><legend>开启楼层奖励:无<button class="btn btn-info editFloorReward" floor="'+floor+'" type="button">编辑</button></legend><table class="table table-hover table-striped table-bordered"><tbody>'
            +'<tr><th>关号</th><th>怪物1</th><th>怪物2</th><th>怪物3</th><th>怪物4</th><th>怪物5</th></tr>'
            +'<tr><td><div>第1关</div><br><button type="button" floor="'+floor+'" stageindex="0" class="btn btn-danger deleteStage">删除关卡</button></td><td class="npctd">';
    var tdhtml = makeStageNpcTd(npcObj,floor,stageIndex,npcIndex);
    html += tdhtml+'</td>';
    html +='<td><input type="button" class="btn btn-primary appendNpc" value="点此追加关卡npc"></td></tr>';
    html +='<tr><td><input type="button" value="点此添加一个关卡" class="btn btn-primary appendStage" floor="'+floor+'"></tr>'
    html +='</tbody></table>';
    $(".addFloor").before(html);
    $(".deleteFloor").remove();
    $(".appendStage:last").after('<button type="button" class="btn btn-danger deleteFloor">删除本层</button>');
}

function afterEditNpcSuccessHtml(npcObj,floor,stageIndex,npcIndex,td){
    var html = makeStageNpcTd(npcObj,floor,stageIndex,npcIndex)
    $(td).html(html);
}

function getMaxNpcTdId(){
    var rtn = 0;
    $(".npctd").each(function(){
        var index = parseInt($(this).attr("id").substring(5));
        if(index > rtn){
            rtn = index;
        }
    });
    return rtn;
}

function popSaveNpcDialog(action,npcObj,floor,stageIndex,npcIndex){
    $("#npc_edit").modal("show");
    $("#npc_edit").find("h3").text(action);
    $(".npcPos").text("第"+floor+"层,第"+(stageIndex+1)+"关,第"+(npcIndex+1)+"个npc");
    if(null == npcObj){
        npcObj = allNpc[0]
    }
    $(".npcBaseAp").val(npcObj.baseAp);
    $(".npcHp").val(npcObj.hasOwnProperty('hp')?npcObj.hp:100);
    $(".npcAdvAp").val(npcObj.advAp);
    $(".npcBaseDp").val(npcObj.baseDp);
    $(".npcAdvDp").val(npcObj.advDp);
    $(".npcDesc").text(npcObj.hasOwnProperty('npcDesc')?npcObj.npcDesc:"");
    $(".npcType").html("");
    var optionHtml ='';
    for(i=0;i<npcType.length;i++){
        optionHtml+='<option value="'+npcType[i]+'"';
        if(npcObj.npcType == npcType[i]){
            optionHtml +=' selected ';
        }
        optionHtml +='>'+npcType[i]+'</option>';
    }
    $(".npcType").append(optionHtml);

    $(".npcPrototype").html("");
    optionHtml ='';
    for(i=0;i<allNpc.length;i++){
        optionHtml+='<option value="'+allNpc[i].id+'"';
        if(npcObj.id == allNpc[i].id){
            optionHtml +=' selected ';
        }
        optionHtml +='>'+allNpc[i].id+','+allNpc[i].name+','+allNpc[i].attackType+'</option>';
    }
    $(".npcPrototype").append(optionHtml);

    $(".curEditNpc").attr("floor",floor);
    $(".curEditNpc").attr("stageIndex",stageIndex);
    $(".curEditNpc").attr("npcIndex",npcIndex);

    $(".beatReward").remove();
    var rewardHtml = '';
    if(npcObj.hasOwnProperty('reward')){
        for(var i = 0 ; i < npcObj.reward.length ; i ++){
            var rewardObj = npcObj.reward[i];
            rewardHtml +=
                    '<li class="beatReward">'
                    +'<dt>击败奖励'+(i+1)+'</dt>'
                    +'<dd >类型'
                    +'<select class="rewardType">';
            rewardHtml += buildRewardTypeSelect(rewardObj.rewardType);
            rewardHtml +=  '</select>'
                    +'</dd>';
            var itemIdDd = '<dd style="display:none;" class="itemIdDd">道具/宠物<select class="itemId"></select></dd>'
            if(findRewardTypeByRewardName(rewardObj.rewardType).idRequired){
                itemIdDd = '<dd class="itemIdDd">道具/宠物<select class="itemId">'+buildRewardIdSelect(rewardObj.rewardType,rewardObj.itemId)+'</select></dd>';
            }
            rewardHtml += itemIdDd
                    +'<dd>数量<input class="rewardQuantity" type="text" value="'+rewardObj.count+'"/></dd>'
                    +'<dd>概率<input class="rewardRate" type="text" value="'+rewardObj.rewardRate+'"/></dd>'
                    +'<dd>每把钥匙概率<input class="keyRateUp" type="text" value="'+rewardObj.keyRate+'"/></dd>'
                    +'<dd>钥匙上限<input class="keyMaxCount" type="text" value="'+rewardObj.maxKeyCount+'"/>把</dd>'
                    +'</li>';
        }
    }else{
        rewardHtml = '<li class="beatReward">'
                +'<dt>击败奖励1</dt>'
                +'<dd >类型'
                +'<select class="rewardType">';
        for(var i =0; i < rewardType.length; i++){
            rewardHtml += '<option value="'+rewardType[i].name+'" processInternal="'+rewardType[i].processInternal+'">'+rewardType[i].chnName+'</option>';
        }
        rewardHtml+='</select>'
                +'</dd>'
                +'<dd style="display:none;" class="itemIdDd">道具/宠物<select class="itemId">'+buildRewardIdSelect("BABEL_STAR",0)+'</select></dd>'
                +'<dd>数量<input class="rewardQuantity" type="text" value="1"/></dd>'
                +'<dd>概率<input class="rewardRate" type="text" value="100"/></dd>'
                +'<dd>每把钥匙概率<input class="keyRateUp" type="text" value="5"/></dd>'
                +'<dd>钥匙上限<input class="keyMaxCount" type="text" value="0"/>把</dd>'
                +'</li>';
    }
    $(".addReward").parents("li:first").before(rewardHtml);
}

function buildRewardTypeSelect(rewardInput){
    var rewardHtml = ''
    for(var j = 0 ; j < rewardType.length; j++){
        var selected='';
        if(rewardInput == rewardType[j].name){
            selected = 'selected';
        }
        rewardHtml += '<option value="'+rewardType[j].name+'" '+selected+' processInternal="'+rewardType[j].processInternal+'">'+rewardType[j].chnName+'</option>';
    }
    return rewardHtml;
}
function findRewardTypeByRewardName(name){
    for(var i = 0 ; i < rewardType.length;i++){
        if(rewardType[i].name==name){
            return rewardType[i]
        }
    }
}

function findBabelItemById(id){
    for(var i = 0 ; i < itemType.length;i++){
        if(itemType[i].itemId==parseInt(id)){
            return itemType[i];
        }
    }
    return null;
}

//根据奖励对象获取具体奖品对象。奖励有可能是pk道具、通天塔道具或通天塔宠物
function findItemObjByRewardObj(rewardObj){
    switch(rewardObj.rewardType){
        case "BABEL_ITEM":
            var item = findBabelItemById(rewardObj.itemId);
            return null == item?'':item.itemName;
        case "BABEL_PET":
            var pet = getPetByIdFromPage(rewardObj.itemId);
            return null == pet?'':pet.petName;
        case "PK_ITEM":
            var pkItem = getPkItemFromPage(rewardObj.itemId);
            return null == pkItem?'':pkItem.itemName;
        default:
            break;
    }
}

function getPetByIdFromPage(petId){
    for(var i = 0 ; i < allPet.length ; i++){
        if(allPet[i].id == parseInt(petId)){
            return allPet[i];
        }
    }
    return null;
}

function getPkItemFromPage(itemId){
    for(var i = 0 ; i < pkItem.length ; i++){
        if(pkItem[i].itemId == itemId){
            return pkItem[i];
        }
    }
    return null;
}

function bindSaveNpcClick(callback,td){
    $(".savenpc").off("click").on("click",function(){
        var queryUrl = "saveStageNpc.vpage";
        var floor = parseInt($(".curEditNpc").attr("floor"));
        var stageIndex = parseInt($(".curEditNpc").attr("stageIndex"));
        var npcIndex = parseInt($(".curEditNpc").attr("npcIndex"));

        var npcHp = $.trim($(".npcHp").val());
        var npcBaseAp = $.trim($(".npcBaseAp").val());
        var npcAdvAp = $.trim($(".npcAdvAp").val());
        var npcBaseDp = $.trim($(".npcBaseDp").val());
        var npcAdvDp = $.trim($(".npcAdvDp").val());

        if("" == npcHp || "" == npcBaseAp || "" == npcAdvAp || "" == npcBaseDp || "" == npcAdvDp ){
            alert("HP、基础攻击、特殊攻击、基础伤害 及 特殊伤害为必填内容，请填入整数");
        }
        var rewardArray = new Array();
        var check = true;
        var rateSum = 0;
        $(".beatReward").each(function(index){
            if(!check){
                return;
            }

            var count = $.trim($(this).find(".rewardQuantity").val());
            var rewardRate = $.trim($(this).find(".rewardRate").val());
            var keyRate = $.trim($(this).find(".keyRateUp").val());
            var maxKeyCount = $.trim($(this).find(".keyMaxCount").val());
            if(count=="" || rewardRate=="" || keyRate=="" || maxKeyCount == ""){
                alert($(this).find("dt").text()+"数据不完整，将忽略此项奖励");
                return;
            }
            var rewardObj = {};
            rewardObj.rewardType=$(this).find(".rewardType").val();
            rewardObj.itemId=$(this).find(".itemId").val();
            rewardObj.count=parseInt(count);
            if(rewardObj.count <=0){
                alert("奖励数量须为正整数")
                check = false;
                return;
            }
            rewardObj.rewardRate=parseInt(rewardRate);
            if(rewardObj.rewardRate <=0){
                alert("概率须为正整数")
                check = false;
                return;
            }
            rateSum += rewardObj.rewardRate;
            rewardObj.keyRate=parseInt(keyRate);
            if(rewardObj.rewardRate <0){
                alert("每把钥匙概率须为正整数")
                check = false;
                return;
            }
            rewardObj.maxKeyCount=parseInt(maxKeyCount);
            if(rewardObj.maxKeyCount <0){
                alert("钥匙上限须为正整数")
                check = false;
                return;
            }

            if(rewardObj.maxKeyCount * rewardObj.keyRate > 100){
                alert("每把钥匙概率 * 钥匙上限 不可大于100");
                check = false;
                return;
            }
            rewardArray.push(rewardObj)
        });
        if(!check){
            return;
        }
        if(rewardArray.length>0 && rateSum!=100){
            alert("所有奖励的概率总和须为100")
            return;
        }

        $.post(queryUrl, {
                    floor:floor,
                    stageIndex:stageIndex,
                    npcIndex:npcIndex,
                    npcInfo:JSON.stringify({
                        id:$(".npcPrototype").val(),
                        npcType:$(".npcType").val(),
                        hp:$(".npcHp").val(),
                        baseAp:$(".npcBaseAp").val(),
                        advAp:$(".npcAdvAp").val(),
                        baseDp:$(".npcBaseDp").val(),
                        advDp:$(".npcAdvDp").val(),
                        npcDesc:$(".npcDesc").val(),
                        reward:rewardArray
                    })
                }, function (data) {
                    stageData = data.allFloor;
                    callback(data.npc,floor,stageIndex,npcIndex,td);
                    $("#npc_edit").modal("hide");
                }
                ,'json');
    });
}

function floorRewardText(rewardArray){

    var rtnArray='';
    for(var i =0 ; i < rewardArray.length; i++){
        var rewardObj = rewardArray[i];
        var rtnObj = {类型:findRewardTypeByRewardName(rewardObj.rewardType).chnName};
        if(findRewardTypeByRewardName(rewardObj.rewardType).idRequired){
            rtnObj.名称=findItemObjByRewardObj(rewardObj);
        }
        rtnObj.数量=rewardObj.count;
        rtnObj = findRewardTypeByRewardName(rewardObj.rewardType).chnName;
        if(findRewardTypeByRewardName(rewardObj.rewardType).idRequired){
            rtnObj+=' '+findItemObjByRewardObj(rewardObj);
        }
        rtnObj+=' '+rewardObj.count+'个,';
        rtnArray += rtnObj;
    }
    return rewardArray.length==0?"无":rtnArray;
}

function popEditFloorReward(floor){
    var floorObj = stageData[floor-1];
    var firstReward = floorObj.firstEntryReward.length>0?floorObj.firstEntryReward[0]:null;
    var html = rewardEditHtml(firstReward)
    if(floorObj.firstEntryReward.length > 1){
        for(var i = 1 ; i < floorObj.firstEntryReward.length; i++){
            html += rewardEditHtml(floorObj.firstEntryReward[i]);
        }
    }
    $(".addFloorRewardUl").remove();
    $(".addFloorRewardBtn").parents("ul:first").before(html);
    $(".rewardFloorNo").text(floor);
    $("#floor_reward_edit").modal("show");
}

function rewardEditHtml(rewardObj){
    var rewardTypeIn = null==rewardObj?"BABEL_STAR":rewardObj.rewardType;
    var itemId = null==rewardObj?"":rewardObj.itemId;
    var count = null==rewardObj?"":rewardObj.count;
    var html = '<ul class="inline addFloorRewardUl">'
            +'<li><dt>类型</dt><dd ><select class="floorRewardType">';
    for(var i = 0 ; i < rewardType.length ; i++){
        var selected = rewardTypeIn==rewardType[i].name?"selected":"";
        html +='<option value="'+rewardType[i].name+'" '+selected+' processInternal="'+rewardType[i].processInternal+'">'+rewardType[i].chnName+'</option>';
    }

    var display='';
    if(!findRewardTypeByRewardName(rewardTypeIn).idRequired){
        display='style="display:none;"';
    }
    html +='</select></dd></li>'
            +'<li class="floorItemId" '+display+'><dt>物品/宠物ID</dt><dd ><select class="floorRewardItemId">'+buildRewardIdSelect(rewardTypeIn,itemId)+'</select></dd></li>'
            +'<li><dt>数量</dt>'
            +'<dd ><input class="floorRewardCount" type="text" value="'+count+'"/></dd></li></ul>';
    return html;
}

function buildRewardIdSelect(action,id){

    var option = '';
    switch(action){
        case "BABEL_ITEM":
            for(var i = 0 ; i < itemType.length ; i++){
                var selected = '';
                if(id != null){
                    if(parseInt(id) == itemType[i].itemId){
                        selected = 'selected';
                    }
                }
                option +='<option value='+itemType[i].itemId+' '+selected+'>'+itemType[i].itemName+'</option>';
            }
            break;
        case "BABEL_PET":
            for(var i = 0 ; i < allPet.length ; i++){
                var selected = '';
                if(id != null){
                    if(parseInt(id) == allPet[i].id){
                        selected = 'selected';
                    }
                }
                option +='<option value='+allPet[i].id+' '+selected+'>'+allPet[i].petName+'</option>';
            }
            break;
        case "PK_ITEM":
            for(var i = 0 ; i < pkItem.length ; i++){
                var selected = '';
                if(id != null){
                    if(id == pkItem[i].itemId){
                        selected = 'selected';
                    }
                }
                option +='<option value='+pkItem[i].itemId+' '+selected+'>'+pkItem[i].itemName+'</option>';
            }
            break;
        default:
            break;
    }
    return option;
}

</script>
</@layout_default.page>