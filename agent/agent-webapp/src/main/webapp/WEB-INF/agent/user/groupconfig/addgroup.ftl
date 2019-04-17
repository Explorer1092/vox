<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加/编辑系统群组' page_num=5>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 编辑区域设置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">区域名称</label>
                        <div class="controls">
                            <input id="group_name" class="input-xlarge focused" type="text" value="<#if agentSysGroup??>${agentSysGroup.groupName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">区域说明</label>
                        <div class="controls">
                            <input id="group_desc" class="input-xlarge focused" type="text" value="<#if agentSysGroup??>${agentSysGroup.groupDesc!}</#if>">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="selectError1">学校级别</label>
                        <div class="controls">
                            <#if schoolLevelTypeList??>
                                <select id="schoolType" style="width: 280px">
                                    <#if !agentSysGroup??>
                                        <option value="0" selected>请选择</option>
                                    </#if>
                                    <#list schoolLevelTypeList as item>
                                        <option value="${item.level!}" <#if agentSysGroup?? && agentSysGroup.schoolLevelType?? && (item.level == agentSysGroup.schoolLevelType)>selected</#if>>
                                        ${item.desc!''}
                                        </option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="selectError1">所属角色</label>
                        <div class="controls">
                            <#if agentRoles??>
                                <select id="role" style="width: 280px">
                                    <#if !agentSysGroup??>
                                        <option value="0" selected>请选择</option>
                                    </#if>
                                    <#list agentRoles as role>
                                        <option value="${role.id!}" <#if agentSysGroup??&& (role.id == agentSysGroup.groupRole)>selected</#if>>
                                        ${allAgentRoleMap[role.id?string].roleName!}
                                        </option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError1">上级区域</label>
                        <div class="controls">
                            <#if parentGroups??>
                                <select id="parent_group_id" style="width: 280px">
                                    <#if !agentSysGroup??>
                                        <option value="-1" selected>请选择</option>
                                    </#if>
                                    <#if requestContext.getCurrentUser().isAdmin()>
                                        <option value="0">根群组</option>
                                    </#if>
                                    <#list parentGroups as group>
                                        <option value="${group.id!}" <#if agentSysGroup??&& (group.id == agentSysGroup.parentGroupId)>selected</#if> >
                                        ${group.groupName!}
                                        </option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError1">负责地区</label>
                        <div class="controls" style="width: 380px;">
                            <#if (agentSysGroup.groupRegionList)??>
                            <table class="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                        <td>省</td>
                                        <td>市</td>
                                        <td>区</td>
                                        <td width="70px">操作</td>
                                    </tr>
                                </thead>
                                <tbody>
                                    <#list agentSysGroup.groupRegionList as groupRegion>
                                    <tr>
                                        <td>${groupRegion.provinceName!}</td>
                                        <td>${groupRegion.cityName!'-'}</td>
                                        <td>${groupRegion.countyName!'-'}</td>
                                        <td><a id="delete_group_region_${groupRegion.groupRegionId!}" class="btn btn-danger" href="javascript:void(0);">
                                            <i class="icon-trash icon-white"></i>&nbsp;删除</a>
                                        </td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                            <#else>
                                请先保存然后使用 选择地区 按钮增加地区
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError1">负责学校</label>
                        <div class="controls" style="width: 380px;">
                            <#if (agentSysGroup.groupSchoolList)??>
                                <table class="table table-striped table-bordered">
                                    <thead>
                                    <tr>
                                        <td>学校</td>
                                        <td width="70px">操作</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#list agentSysGroup.groupSchoolList as groupSchool>
                                        <tr>
                                            <td>${groupSchool.schoolName!}</td>
                                            <td><a id="delete_group_school_${groupSchool.groupSchoolId!}" class="btn btn-danger" href="javascript:void(0);">
                                                <i class="icon-trash icon-white"></i>删除</a>
                                            </td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            <#else>
                                请先保存然后使用 选择学校 按钮增加学校
                            </#if>
                        </div>
                    </div>

                    <div class="form-actions">
                        <#if agentSysGroup ??>
                        <button id="sel_group_region_btn" type="button" class="btn btn-primary">选择地区</button>
                        <button id="sel_group_school_btn" type="button" class="btn btn-primary">选择学校</button>
                        </#if>
                        <button id="add_sys_group_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="groupId" value="${groupId!}">
<#if agentSysGroup?? && agentSysGroup.agentGroupRegionList??>
    <#list agentSysGroup.agentGroupRegionList as region>
    <input type="hidden" name="region" value="${region.regionCode!}">
    </#list>
</#if>

<div id="region_select_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择地区</h4>
            </div>
            <form class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <div id="regiontree" class="controls" style="width: 280px;height: 400px"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="region_select_btn" type="button" class="btn btn-primary">确定</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div id="school_select_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择学校</h4>
            </div>
            <form>
                <div class="modal-body" style="height: 400px; overflow: visible; width: auto">
                    <div class="control-group">
                        <div class="controls">
                            <select id="modal_province" name="modal_province" style="width:90px">
                                <option value="0">-选择省-</option>
                                <#list userProvinceList as province>
                                    <option value="${province.code}">${province.name}</option>
                                </#list>

                            </select>
                            <select id="modal_city" name="modal_city" style="width:90px">
                                <option value="">-选择市-</option>
                            </select>
                            <select id="modal_county" name="modal_county" style="width:90px">
                                <option value="">-选择区-</option>
                            </select>
                            <input id="modal_school_name" name="modal_school_name" class="input" type="text" style="width:150px">
                            <button id="school_search" type="button" class="btn btn-primary">查询</button>
                        </div>
                    </div>

                    <div class="controls" style="height: 360px; overflow:auto; width: auto">
                    <table class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <td width="30px">&nbsp;</td>
                            <td>学校</td>
                        </tr>
                        </thead>
                        <tbody id="tbody">
                        </tbody>
                    </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="school_select_btn" type="button" class="btn btn-primary">确定</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        $('#sel_group_region_btn').live('click',function(){
            $("#regiontree").fancytree('destroy');
            $("#regiontree").fancytree({
                source: {
                    url: "loadregion.vpage",
                    cache:false
                },
                checkbox: true,
                selectMode: 2
            });

            $('#region_select_dialog').modal('show');
        });

        $('#region_select_btn').live('click',function(){
            var regionTree = $("#regiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if(regionNodes == null || regionNodes == "undefined") {
                return;
            }

            var selectRegionList = new Array();
            $.map(regionNodes, function(node){
                selectRegionList.push(node.key);
            });

            var regionIds = selectRegionList.join(',');
            var groupId = $('#groupId').val();

            $.post('addgroupregion.vpage',{
                groupId:groupId,
                regionIds:regionIds
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $('#region_select_dialog').modal('hide');
                    window.location.reload();
                }
            });
        });

        $("a[id^='delete_group_region_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_group_region_".length);
            if(!confirm("将要删除此区域的地区，确定要删除吗?")){
                return false;
            }

            $.post('delgroupregion.vpage',{
                groupRegionId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });


        $('#sel_group_school_btn').live('click',function(){
            $('#school_select_dialog').modal('show');
        });

        $('#modal_province').on('change',function(){
            loadCityList();
        });

        $('#modal_city').on('change',function(){
            loadCountyList();
        });

        $('#school_search').live('click',function(){
            loadSchoolList();
        });

        $('#school_select_btn').live('click',function(){
            var schoolIds = "";
            $("input[id^='batch_add_school_']:checked").each(function(){
                schoolIds += $(this).attr("id").substring("batch_add_school_".length) + ",";
            });
            if (schoolIds == '') {
                alert("请选择学校!");
                return false;
            }

            var groupId = $('#groupId').val();

            $.post('addgroupschool.vpage',{
                groupId:groupId,
                schoolIds:schoolIds
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $('#school_select_dialog').modal('hide');
                    window.location.reload();
                }
            });
        });

        $("a[id^='delete_group_school_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_group_school_".length);
            if(!confirm("将要删除此区域的学校，确定要删除吗?")){
                return false;
            }

            $.post('delgroupschool.vpage',{
                groupSchoolId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });

        $('#add_sys_group_btn').live('click',function(){
            var groupName = $('#group_name').val();
            var desc = $('#group_desc').val();
            var role = $('#role').find('option:selected').val();
            var parentId = $('#parent_group_id').find('option:selected').val();

            var schoolType = $("#schoolType").val();

            if(!checkAddGroup(groupName,role,parentId, schoolType)){
                return false;
            }

            $.post('addsysgroup.vpage',{
                groupName:groupName,
                desc: desc,
                role: parseInt(role),
                parentId:parseInt(parentId),
                groupId:$('#groupId').val(),
                schoolLevel:parseInt(schoolType)
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'addsysgroup.vpage?id=' + data.groupId);
                }
            });
        });
    });

    function checkAddGroup(groupName,role,parentId, schoolType){
        if(groupName.trim() == ''){
            alert("请输入群组名称!");
            return false;
        }
        if(role == '0'){
            alert("请选择角色!");
            return false;
        }
        if(parentId == '-1'){
            alert("请选择父群组!");
            return false;
        }

        if(schoolType == '0'){
            alert("请选择学校级别!");
            return false;
        }
        return true;
    }

    function loadRegion(){
        $("#regiontree").fancytree({
            extensions: ["filter"],
            source: {
                url: "/common/region/loadregion.vpage",
                cache:true
            },
            checkbox: true,
            selectMode: 2,

            init: function(event, data, flag) {
                var tree = $("#regiontree").fancytree("getTree");
                tree.visit(function(node){
                    $("input[name='region']").each(function(){
                        if (node.key == $(this).attr("value")) {
                            node.setSelected(true);
                            node.setActive();
                        }
                    });
                });
            }
        });
    }

    function getSelectedRegionCode(){
        var regionTree = $("#regiontree").fancytree("getTree");
        var regionNodes = regionTree.getSelectedNodes();
        if(regionNodes == null || regionNodes == "undefined") return null;
        var codes = new Array();
        $.map(regionNodes, function(node){
            codes.push(node.key);
        });

        return codes;
    }

    function loadCityList(){
        $('#modal_city').html('<option value="0">-市-</option>');
        $('#modal_city').change();
        var province = $('#modal_province').find('option:selected').val();
        if(province == '0'){
            return false;
        }

        $.post('loaduserraltedcities.vpage',{
            province:province
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                for(var i=0; i<data.cityList.length; i++){
                    $('<option value="'+data.cityList[i].code+'">'+data.cityList[i].name+'</option>').appendTo($('#modal_city'));
                }
            }
        });
    }

    function loadCountyList(){
        $('#modal_county').html('<option value="0">-区-</option>');
        $('#modal_county').change();
        var city = $('#modal_city').find('option:selected').val();
        if(city == '0'){
            return false;
        }

        $.post('loaduserraltedcounties.vpage',{
            city:city
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                for(var i=0; i<data.countyList.length; i++){
                    $('<option value="'+data.countyList[i].code+'">'+data.countyList[i].name+'</option>').appendTo($('#modal_county'));
                }
            }
        });
    }

    function loadSchoolList(){
        var county = $('#modal_county').find('option:selected').val();
        if(county == '0'){
            alert("请选择地区后再进行查询操作!");
            return false;
        }

        $.post('loadschools.vpage',{
            county:county,
            schoolName:$('#modal_school_name').val()
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                var html='';
                for(var i =0; i<data.schoolList.length; i++){
                    var schoolId = data.schoolList[i].schoolId;
                    var schoolName = data.schoolList[i].schoolName;
                    html +='<tr>';
                    html +='<td>&nbsp;<input type=checkbox id="batch_add_school_' + schoolId +  '\"></td>';
                    html +='<td>' + schoolName + '</td>';
                    html +='</tr>';
                }

                if(html == ""){
                    html ="<tr><td colspan=2>未查询到数据</td></tr>";
                }

                $('#tbody').html(html);
            }
        });
    }
</script>

</@layout_default.page>