<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="通天塔NPC原型及宠物配置">
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
                    <dt>ID</dt>
                    <dd ></dd>
                </li>
                <li>
                    <dt>名字</dt>
                    <dd ><input class="npcName" type="text"/></dd>
                </li>
                <li>
                    <dt>攻击属性</dt>
                    <dd ><select class="attackType">
                    </select></dd>
                </li>
                <li>
                    <dt>基础攻击</dt>
                    <dd ><input class="baseAp" type="text"/></dd>
                </li>
                <li>
                    <dt>特殊攻击</dt>
                    <dd ><input class="advAp" type="text"/></dd>
                </li>
                <li>
                    <dt>基础伤害</dt>
                    <dd ><input class="baseDp" type="text"/></dd>
                </li>
                <li>
                    <dt>特殊伤害</dt>
                    <dd ><input class="advDp" type="text"/></dd>
                </li>
                <li>
                    <dt>简介</dt>
                    <dd ><input class="npcDesc" type="text"/></dd>
                </li>
                <input type="hidden" class="saveAction"/>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary savenpc">保 存</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="pet_edit" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>编辑宠物</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>ID</dt>
                        <dd ></dd>
                    </li>
                    <li>
                        <dt>名字</dt>
                        <dd ><input class="petName" type="text"/></dd>
                    </li>
                    <li>
                        <dt>攻击属性</dt>
                        <dd ><select class="petAttackType">
                        </select></dd>
                    </li>
                    <li>
                        <dt>地图号</dt>
                        <dd ><input class="mapNo" type="text"/></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary savepet">保 存</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>
<script>
    var allNpc=null;
    var allPet=null;
    var newNpcId=null;
    var newPetId=null;
    var attackTypeArray=["PLANT","ANIMAL","NATURAL","SUPER_POWER"];

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

    function getNewNpcId(){
        var queryUrl = "getNewNpcId.vpage";
        $.post(queryUrl, {}, function (data) {
                    newNpcId = data;
        });
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

    function getNewPetId(){
        var queryUrl = "getNewPetId.vpage";
        $.post(queryUrl, {}, function (data) {
            newPetId = data;
        });
    }

    function getBossPrizeConf(){
        var queryUrl = "listBossPrize.vpage";
        $.post(queryUrl, {}, function (data) {
            bossPrizeConf = data;
        });
    }
    $(function() {
        getNpcConf(function(){
            var content='<legend>NPC列表</legend><table class="table table-hover table-striped table-bordered">'
                    +'<tbody>';
            for(var i = 0 ; i < allNpc.length ; i++){
                if(i%5==0){
                    content +='<tr>';
                }
                content += '<td>'+makeNpcTd(allNpc[i])+'</td>';
                if(i%5==4){
                    content +='</tr>';
                }
            }

            content +='</tbody></table>';
            $('.span9').append(content);
            var html = '<td><input type="button" class="btn btn-primary appendNpc" value="点此添加一个npc" /></td>';
            if(allNpc.length % 5 == 0){
                html = '<tr>'+html+'</tr>';
                $("tbody").append(html);
            }else{
                $("tr:last").append(html);
            }

        });
        getNewNpcId();

        getPetConf(function(){

            var content='<legend>宠物列表</legend><table class="table table-hover table-striped table-bordered">'
                    +'<tbody>';
            for(var i = 0 ; i < allPet.length ; i++){
                if(i%5==0){
                    content +='<tr>';
                }
                content += '<td>'+makePetTd(allPet[i])+'</td>';
                if(i%5==4){
                    content +='</tr>';
                }
            }

            content +='</tbody></table>';
            $('.span9').append(content);
            var html = '<td><input type="button" class="btn btn-primary appendPet" value="点此添加一个宠物" /></td>';
            if(allPet.length % 5 == 0){
                html = '<tr>'+html+'</tr>';
                $("tbody:last").append(html);
            }else{
                $("tr:last").append(html);
            }

        });
        getNewPetId();
        $(document).on("click",".editNpc",function(){
            popEditNpc(getNpcByIdFromPage($(this).attr("npcId")));
            bindSave(afterEditNpc,$(this).parent("td"));
        });

        $(document).on("click",".editPet",function(){
            popEditPet(getPetByIdFromPage($(this).attr("petid")));
            bindPetSave(afterEditPet,$(this).parent("td"));
        });

        $(document).on("click",".appendNpc",function(){
            popEditNpc(null);
            bindSave(afterAppendNpc,$(this).parent("td"));
        });

        $(document).on("click",".appendPet",function(){
            popEditPet(null);
            bindPetSave(afterAppendPet,$(this).parent("td"));
        });

        $(document).on("click",".deleteNpc",function(){
            var td = $(this).parent("td");
            if(confirm("真的要删除npc吗？\n"+$(this).siblings("div").text())){
                var queryUrl = "deleteNpc.vpage";
                $.post(queryUrl, {id:$(this).attr("npcid")}, function (data) {
                            if(data.success){
                                alert("操作成功");
                                $(td).remove();
                            }else{
                                alert("操作失败");
                            }
                        }
                ,'json');

            }
        });

        $(document).on("click",".deletePet",function(){
            var td = $(this).parent("td");
            if(confirm("真的要删除宠物吗？\n"+$(this).siblings("div").text())){
                var queryUrl = "deletePet.vpage";
                $.post(queryUrl, {id:$(this).attr("petid")}, function (data) {
                            if(data.success){
                                alert("操作成功");
                                $(td).remove();
                            }else{
                                alert("操作失败");
                            }
                        }
                        ,'json');

            }
        });

        $(document).on("click",".savenpc",function(){
            var npcName = $.trim($(".npcName").val());
            var baseAp = $.trim($(".baseAp").val());
            var baseDp = $.trim($(".baseDp").val());
            var advAp = $.trim($(".advAp").val());
            var advDp = $.trim($(".advDp").val());
            var npcDesc = $.trim($(".npcDesc").val());
            if("" == npcName ||"" == baseAp||"" == baseDp||"" == advAp||"" == advDp||"" == npcDesc){
                alert("请将信息填写完整");
                return;
            }

            var queryUrl  ="saveNpc.vpage";
            $.post(queryUrl, {}, function (data) {
                allNpc = data.allNpc;
                var editNpc = data.editNpc;
                switch($(".saveAction").val()){
                    case "editNpc":
                        break;
                    case "appendNpc":
                        newNpcId = data.newNpcId;
                        break;
                }
                 }
                ,'json');

        });
    });

    function bindSave(callback,td){

        $(".savenpc").off("click").on("click",function(){
            var npcName = $.trim($(".npcName").val());
            var baseAp = $.trim($(".baseAp").val());
            var baseDp = $.trim($(".baseDp").val());
            var advAp = $.trim($(".advAp").val());
            var advDp = $.trim($(".advDp").val());
            var npcDesc = $.trim($(".npcDesc").val());
            if("" == npcName ||"" == baseAp||"" == baseDp||"" == advAp||"" == advDp||"" == npcDesc){
                alert("请将信息填写完整");
                return;
            }

            var queryUrl  ="saveNpc.vpage";
            $.post(queryUrl, {
                        id:$("dd:first").text(),
                        attackType:$(".attackType").val(),
                        name:npcName,
                        baseAp:baseAp,
                        baseDp:baseDp,
                        advAp:advAp,
                        advDp:advDp,
                        npcDesc:npcDesc
                    }, function (data) {
                        allNpc = data.allNpc;
                        callback(data,td);
                        $("#npc_edit").modal("hide");
                    }
                    ,'json');

        });
    }

    function bindPetSave(callback,td){
        $(".savepet").off("click").on("click",function(){
            var npcName = $.trim($(".petName").val());
            var mapNo = $.trim($(".mapNo").val());
            if("" == npcName || "" == mapNo){
                alert("请将信息填写完整");
                return;
            }

            var queryUrl  ="savePet.vpage";
            $.post(queryUrl, {
                        id:$("#pet_edit").find("dd:first").text(),
                        name:npcName,
                        mapNo:mapNo,
                        attackType:$(".petAttackType").val()
                    }, function (data) {
                        allPet = data.allPet;
                        callback(data,td);
                        $("#pet_edit").modal("hide");
                    }
                    ,'json');

        });
    }

    function afterEditNpc(data,td){
        var npcObj = data.editNpc;
        $(td).html(makeNpcTd(npcObj));
    }

    function afterAppendNpc(data,td){
        newNpcId = data.newNpcId;
        var npcObj = data.editNpc;
        var thisTrTdLength = $(td).siblings("td").length;
        var newTd = makeNpcTd(npcObj);
        if(thisTrTdLength<4){
            $(td).before('<td>'+newTd+'</td>');
        }else{
            $(td).html(newTd);
            $("tbody").append('<tr><td><input type="button" value="点此添加一个npc" class="btn btn-primary appendNpc"></td></tr>');
        }
    }

    function afterEditPet(data,td){
        var petObj = data.editPet;
        $(td).html(makePetTd(petObj));
    }

    function afterAppendPet(data,td){
        newNpcId = data.newPetId;
        var npcObj = data.editPet;
        var thisTrTdLength = $(td).siblings("td").length;
        var newTd = makePetTd(npcObj);
        if(thisTrTdLength<4){
            $(td).before('<td>'+newTd+'</td>');
        }else{
            $(td).html(newTd);
            $("tbody:last").append('<tr><td><input type="button" value="点此添加一个宠物" class="btn btn-primary appendPet"></td></tr>');
        }
    }

    function makeNpcTd(npcObj){
        var content ='<div>Id:'+npcObj.id
                +'<br>npc名:'+npcObj.name
                +'<br>攻击属性:'+npcObj.attackType
                +'<br>基础攻击:'+npcObj.baseAp
                +'<br>特殊攻击:'+npcObj.advAp
                +'<br>基础伤害:'+npcObj.baseDp
                +'<br>特殊伤害:'+npcObj.advDp
                +'<br>简介:'+npcObj.npcDesc;
        content +='</div>';
        content +='<button type="button" npcId="'+npcObj.id+'" class="btn btn-info editNpc">编辑</button>';
        content +='<button class="btn btn-danger deleteNpc" npcId="'+npcObj.id+'" type="button">删除</button>';
        content +='';
        return content;
    }

    function makePetTd(petObj){
        var content ='<div>Id:'+petObj.id
                +'<br>pet名:'+petObj.petName
                +'<br>攻击属性:'+petObj.attackType
                +'<br>地图号:'+petObj.mapNo;
        content +='</div>';
        content +='<button type="button" petId="'+petObj.id+'" class="btn btn-info editPet">编辑</button>';
        content +='<button class="btn btn-danger deletePet" petId="'+petObj.id+'" type="button">删除</button>';
        content +='';
        return content;
    }

    function getNpcByIdFromPage(npcId){
        for(var i = 0 ; i < allNpc.length ; i++){
            if(allNpc[i].id == parseInt(npcId)){
                return allNpc[i];
            }
        }
        return null;
    }

    function getPetByIdFromPage(petId){
        for(var i = 0 ; i < allPet.length ; i++){
            if(allPet[i].id == parseInt(petId)){
                return allPet[i];
            }
        }
        return null;
    }
    function popEditNpc(npcObj){
        var npcId = null == npcObj?newNpcId:npcObj.id;
        var npcName= null == npcObj?"":npcObj.name;
        var attackType = null == npcObj?"PLANT":npcObj.attackType;
        var baseAp = null == npcObj?0:npcObj.baseAp;
        var baseDp = null == npcObj?0:npcObj.baseDp;
        var advAp = null == npcObj?0:npcObj.advAp;
        var advDp = null == npcObj?0:npcObj.advDp;
        var npcDesc = null == npcObj?"":npcObj.npcDesc;
        $("dd:first").text(npcId);
        $(".npcName").val(npcName);
        $(".baseAp").val(baseAp);
        $(".baseDp").val(baseDp);
        $(".advAp").val(advAp);
        $(".advDp").val(advDp);
        $(".npcDesc").val(npcDesc);
        $(".attackType").find("option").remove();
        for(var i = 0 ; i < attackTypeArray.length ; i++){
            var selected = attackTypeArray[i] == attackType? 'selected':'';
            var option = '<option value="'+attackTypeArray[i]+'" '+selected+'>'+attackTypeArray[i]+'</option>';
            $(".attackType").append(option);
        }
        $("#npc_edit").modal("show");
    }

    function popEditPet(petObj){
        var petId = null == petObj?newPetId:petObj.id;
        var petName= null == petObj?"":petObj.petName;
        var attackType = null == petObj?"PLANT":petObj.attackType;
        var mapNo = null == petObj?"":petObj.mapNo;
        $("#pet_edit").find("dd:first").text(petId);
        $(".petName").val(petName);
        $(".mapNo").val(mapNo);
        $(".petAttackType").find("option").remove();
        for(var i = 0 ; i < attackTypeArray.length ; i++){
            var selected = attackTypeArray[i] == attackType? 'selected':'';
            var option = '<option value="'+attackTypeArray[i]+'" '+selected+'>'+attackTypeArray[i]+'</option>';
            $(".petAttackType").append(option);
        }
        $("#pet_edit").modal("show");
    }
</script>
</@layout_default.page>