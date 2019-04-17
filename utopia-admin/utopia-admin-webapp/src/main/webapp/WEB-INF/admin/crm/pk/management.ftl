<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="第三方应用管理" page_num=10>
    <legend>角色缓存信息</legend>
    <table>
        <tr>
            <td colspan="12">角色缓存信息</td>
        </tr>
        <tr>
            <td>角色ID</td><td><input id="roleId" name="roleId" value="" type="text"/></td>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="cache_info_btn">角色缓存信息</button>
    <button type="button" class="btn btn-primary" id="refresh_cache_info_btn">刷新角色缓存信息</button>

    <legend>初始化角色背包信息</legend>
    <table>
        <tr>
            <td colspan="12">初始化角色背包</td>
        </tr>
        <tr>
            <td>角色ID</td><td><input id="roleId_1" name="roleId_1" value="" type="text"/></td>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="init_bag_btn">初始化角色背包</button>
    <button type="button" class="btn btn-primary" id="init_skill_btn">初始化角色技能</button>
    <button type="button" class="btn btn-primary" id="init_equipment_btn">初始化角色武装</button>


    <legend>修改角色技能信息</legend>
    <table>
        <tr>
            <td colspan="12">修改角色技能</td>
        </tr>
        <tr>
            <td>角色ID</td><td><input id="roleId_2" name="roleId_2" value="" type="text"/></td>
            <td>技能ID</td><td><input id="skillId" name="skillId" value="" type="text"/></td>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="add_skill_btn">增加技能</button>
    <button type="button" class="btn btn-primary" id="equiped_skill_btn">装备技能</button>
    <button type="button" class="btn btn-primary" id="unequiped_skill_btn">卸载技能</button>
    <button type="button" class="btn btn-primary" id="delete_skill_btn">删除技能</button>

    <legend>修改角色武装信息</legend>
    <table>
        <tr>
            <td colspan="12">修改角色武装</td>
        </tr>
        <tr>
            <td>角色ID</td><td><input id="roleId_3" name="roleId_3" value="" type="text"/></td>
            <td>武装ID</td><td><input id="equipmentOriginalId" name="equipmentOriginalId" value="" type="text"/></td>
            <td>武装实例ID</td><td><input id="equipmentId" name="equipmentId" value="" type="text"/></td>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="add_equipment_btn">增加武装</button>
    <button type="button" class="btn btn-primary" id="equiped_equipment_btn">装备武装</button>
    <button type="button" class="btn btn-primary" id="unequiped_equipment_btn">卸载武装</button>
    <button type="button" class="btn btn-primary" id="delete_equipment_btn">删除武装</button>

    <div id="msg" class="modal hide fade" style="width: 800; height: 600">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>提示</h3>
        </div>
        <div class="modal-body">

        </div>
    </div>

</@layout_default.page>


<script>
    $(function () {
        $('#cache_info_btn').click(function () {
            if (confirm('查看角色缓存信息，确认继续？')) {
                var postData = {
                    roleId: $('#roleId').val()
                };
                $.post('getRoleCache.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#refresh_cache_info_btn').click(function () {
            if (confirm('刷新角色缓存信息，确认继续？')) {
                var postData = {
                    roleId: $('#roleId').val()
                };
                $.post('refreshRoleCache.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });


        $('#init_bag_btn').click(function () {
            if (confirm('初始化角色背包前请确认角色背包为空，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_1').val()
                };
                $.post('initBagForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#init_skill_btn').click(function () {
            if (confirm('初始化角色技能删除所有背包中原有的技能并重新初始化，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_1').val()
                };
                $.post('initSkillForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#init_equipment_btn').click(function () {
            if (confirm('初始化角色武装删除所有背包中原有的武装并重新初始化，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_1').val()
                };
                $.post('initEquipmentForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });





        $('#add_skill_btn').click(function () {
            if (confirm('将为角色增加一条技能信息，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_2').val(),
                    skillId: $('#skillId').val()
                };
                $.post('addSkillForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#equiped_skill_btn').click(function () {
            if (confirm('将技能改为已装备状态，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_2').val(),
                    skillId: $('#skillId').val()
                };
                $.post('equipedSkillForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#unequiped_skill_btn').click(function () {
            if (confirm('将卸载角色的技能信息，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_2').val(),
                    skillId: $('#skillId').val()
                };
                $.post('unequipedSkillForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#delete_skill_btn').click(function () {
            if (confirm('将删除角色的技能信息，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_2').val(),
                    skillId: $('#skillId').val()
                };
                $.post('deleteSkillForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });




        $('#add_equipment_btn').click(function () {
            if (confirm('将为角色增加一条武装信息，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_3').val(),
                    equipmentOriginalId: $('#equipmentOriginalId').val()
                };
                $.post('addEquipmentForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#equiped_equipment_btn').click(function () {
            if (confirm('将武装改为已装备状态，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_3').val(),
                    equipmentOriginalId: $('#equipmentOriginalId').val(),
                    equipmentId: $('#equipmentId').val()
                };
                $.post('equipedEquipmentForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#unequiped_equipment_btn').click(function () {
            if (confirm('将武装改为非装备状态，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_3').val(),
                    equipmentOriginalId: $('#equipmentOriginalId').val(),
                    equipmentId: $('#equipmentId').val()
                };
                $.post('unequipedEquipmentForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#delete_equipment_btn').click(function () {
            if (confirm('将删除角色的武装信息，确认继续？')) {
                var postData = {
                    roleId: $('#roleId_3').val(),
                    equipmentOriginalId: $('#equipmentOriginalId').val(),
                    equipmentId: $('#equipmentId').val()
                };
                $.post('deleteEquipmentForRole.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

    });

</script>