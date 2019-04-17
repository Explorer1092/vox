<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='角色管理' page_num=6>
<style>
    .row-fluid .span12 table tr td {
        padding:0;
        clear:both
    }
    div.checker span{margin-top:10px}
    div.checker span input{margin-top:-20px}
    .row-fluid .span12 table tr td div {
        height:40px;line-height: 40px;
    }
    .role_path ul{clear: both;width:100%;overflow: hidden;border-top: 1px solid #dddddd;}
    .role_path ul li {float:left;list-style: none;width:47.5%;text-align:left;border-right:1px solid #dddddd;box-sizing: border-box;height:40px;line-height:40px;padding-left:1%}
    .role_path ul li:last-child{width:5%;text-align:center;padding:0}
    .row-fluid .span12 table tr td{text-align: center;}
    body .table th,body .table td{vertical-align: middle}
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>${role.roleName!''}权限配置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a type="button" class="submit_btn btn btn-success" href="javascript:;">保存</a>
            </div>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 10px;">序号</th>
                        <th class="sorting" style="width: 20px;">模块</th>
                        <th class="sorting" style="width: 160px;">子模块</th>
                    </tr>
                    </thead>
                    <tbody class="role_path">
                        <#if moduleAndOperations?has_content>
                            <#list moduleAndOperations?keys as key>
                                <#assign module = moduleAndOperations[key]>
                                <tr>
                                    <td>${(key_index + 1 )!0}</td>
                                    <td>${key!''}</td>
                                    <td>
                                        <table style="width:100%">
                                            <#if module?has_content && module?size gt 0>
                                                <#list module?keys as key1>
                                                    <tr class="parent_tr">
                                                        <td style="width:200px">
                                                            <div>
                                                                ${key1!''}
                                                            </div>
                                                        </td>
                                                        <td style="width:50px">
                                                            <input class="choose_input" type="checkbox">
                                                        </td>
                                                        <td>
                                                            <#assign item = module[key1]>
                                                            <#if item?has_content && item?size gt 0>
                                                                <#list item as list1>
                                                                <ul style="margin:0">
                                                                    <li style="<#if list1.systemNotExist?? && list1.systemNotExist> color:red</#if>">
                                                                        ${list1.operationDesc!''}
                                                                    </li>
                                                                    <li style="<#if list1.systemNotExist?? && list1.systemNotExist> color:red</#if>">
                                                                        ${list1.path!''}
                                                                    </li>
                                                                    <li>
                                                                        <input class="postJsonIpt" name="child_input" type="checkbox" <#if list1.selected?? && list1.selected>checked</#if> value="${list1.path!'1'}">
                                                                    </li>
                                                                </ul>
                                                                </#list>
                                                            </#if>
                                                        </td>
                                                    </tr>
                                                </#list>
                                            </#if>
                                        </table>
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="application/javascript">
    $(function () {

        $(document).on('click','.submit_btn',function () {
            var paths = [],
                postJson = {};
            for(var i = 0 ; i<$('.postJsonIpt').length ; i++){
                if($('.postJsonIpt').eq(i).is(':checked')){
                    paths.push($('.postJsonIpt').eq(i).val())
                }
            }
            postJson.roleId = '${role.id!0}';
            postJson.paths = paths.toString();
            console.log(postJson);
            $.post('save_role_paths.vpage',postJson,function (res) {
                if(res.success){
                    alert('设置成功');
                }else{
                    alert(res.info);
                }
            })
        });
        $(document).on('click','.choose_input',function () {
            if(!$(this).attr('checked')){
                $(this).closest('tr').find('input[name="child_input"]').attr("checked",false);
                $(this).closest('tr').find('input[name="child_input"]').parent('span').removeClass('checked');
            }else{
                $(this).closest('tr').find('input[name="child_input"]').attr("checked",true);
                $(this).closest('tr').find('input[name="child_input"]').parent('span').addClass('checked');
            }
        });
//        $(document).on('click','.postJsonIpt',function () {
//            if(!$(this).attr('checked')){
//                $(this).closest('.parent_tr').find('.choose_input').attr("checked",false);
//                $(this).closest('.parent_tr').find('.choose_input').parent('span').removeClass('checked');
//            }
//        });
    });
</script>
</@layout_default.page>