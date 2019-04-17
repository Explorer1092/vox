<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='不活跃老师查询' page_num=1>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 不活跃老师查询</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="inactiveteachers.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span4">
                        <label class="control-label" for="selectError3">选择区县</label>
                        <div class="controls">
                            <input type="text" class="input-large" id="regionNames" readonly="true" value="${regionNames!}" style="cursor: pointer">
                            <input type="hidden" name="regionIds" id="regionIds" value="${regionIds!}"/>
                        </div>
                    </div>
                    <div class="control-group span5">
                        <div class="controls">
                            <button type="submit" id="search_btn" class="btn btn-success">查询</button>
                            <button type="submit" id="export_btn" class="btn btn-success">EXCEL导出</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable" id="DataTables_Table_0">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">省</th>
                        <th class="sorting" style="width: 60px;">市</th>
                        <th class="sorting" style="width: 60px;">区县</th>
                        <th class="sorting" style="width: 90px;">所属学校</th>
                        <th class="sorting" style="width: 90px;">年级</th>
                        <th class="sorting" style="width: 90px;">班级名称</th>
                        <th class="sorting" style="width: 90px;">老师名称</th>
                        <th class="sorting" style="width: 90px;">老师帐号</th>
                        <th class="sorting" style="width: 90px;">老师联系方式</th>
                        <th class="sorting" style="width: 90px;">注册时间</th>
                        <th class="sorting" style="width: 90px;">加入班级时间</th>
                        <th class="sorting" style="width: 90px;">班级人数</th>
                        <th class="sorting" style="width: 90px;">作业数</th>
                        <th class="sorting" style="width: 90px;">最大单次作业完成人数</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if teacherList??>
                            <#list teacherList as teacher>
                            <tr class="odd">
                                <td class="center  sorting_1">${teacher.provinceName!}</td>
                                <td class="center  sorting_1">${teacher.cityName!}</td>
                                <td class="center  sorting_1">${teacher.areaName!}</td>
                                <td class="center  sorting_1">${teacher.schoolName!}</td>
                                <td class="center  sorting_1">${teacher.clazzLevel!}</td>
                                <td class="center  sorting_1">${teacher.clazzName!}</td>
                                <td class="center  sorting_1">${teacher.teacherName!}</td>
                                <td class="center  sorting_1">${teacher.teacherId!}</td>
                                <td class="center  sorting_1">
                                    <#if teacher.teacherMobile?has_content>
                                        ${teacher.teacherMobile!}
                                    <#else>
                                        ${teacher.teacherEmail!}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">${teacher.teacherRegTime!}</td>
                                <td class="center  sorting_1">${teacher.joinClazzTime!}</td>
                                <td class="center  sorting_1">${teacher.classSize!}</td>
                                <td class="center  sorting_1">${teacher.homeworkCount!}</td>
                                <td class="center  sorting_1">${teacher.maxFinishStudentCount!}</td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="region_select_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择查询区域</h4>
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


<script type="text/javascript">
    $(function(){

        $('#regionNames').on('click',function(){
            var $regionTree = $("#regiontree");
            $regionTree.fancytree('destroy');

            $regionTree.fancytree({
                source: {
                    url: "/common/region/loadregion.vpage",
                    cache:false
                },
                checkbox: true,
                selectMode: 2
            });

            $('#region_select_dialog').modal('show');
        });

        $('#region_select_btn').on('click',function(){
            var regionTree = $("#regiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if(regionNodes == null || regionNodes == "undefined") {
                $('#regionIds').val('');
                $('#regionNames').val('');
                return;
            }

            var selectRegionNameList = new Array();
            var selectRegionIdList = new Array();
            $.map(regionNodes, function(node){
                selectRegionIdList.push(node.key);
                selectRegionNameList.push(node.title);
            });

            $('#regionIds').val(selectRegionIdList.join(','));
            $('#regionNames').val(selectRegionNameList.join(','));

            $('#region_select_dialog').modal('hide');

        });

        $('#search_btn').on('click',function(){
            var regionIds = $('#regionIds').val();

            if (regionIds == '') {
                alert("请选择要查询的区域!");
                return false;
            }

            var reg = new RegExp("00$");
            if (reg.test(regionIds) || regionIds.indexOf("00,") > 0) {
                alert("为了保证查询效率,本功能只能按照区县查询,请选择区县!");
                return false;
            }
            $("#query_form").attr("action", "inactiveteachers.vpage");
            return true;
        });

        $('#export_btn').on('click',function(){
            var regionIds = $('#regionIds').val();

            if (regionIds == '') {
                alert("请选择要查询的区域!");
                return false;
            }

            var reg = new RegExp("00$");
            if (reg.test(regionIds) || regionIds.indexOf("00,") > 0) {
                alert("为了保证查询效率,本功能只能按照区县查询,请选择区县!");
                return false;
            }

            $("#query_form").attr("action", "inactiveteachersexport.vpage");

//            $.post('inactiveteachersexport.vpage',{
//                regionIds: regionIds
//            },function(data){
//                if(!data.success){
//                    alert(data.info);
//                }
//            });

        });
    });
</script>
</@layout_default.page>
