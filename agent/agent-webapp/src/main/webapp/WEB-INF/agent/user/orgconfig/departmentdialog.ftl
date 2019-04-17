<#--选择学校弹窗-->
<div id="region_select_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择学校</h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <textarea id="selectSids" class="controls" style="width:80%;" placeholder="输入学校id,多个学校以“,”分隔"></textarea>
                    <button class="btn btn-large btn-primary" type="button" id="addSchooleBtn">查询</button>
                </div>
                <div class="control-group" id="alertInfoInDialog" style="color: red;display: none;">
                </div>
                <div class="control-group">
                    <div id="schoolTable"></div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="pull-left">
                    <button id="add_school_submit_btn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<#--调整负责区域弹窗-->
<div id="addDepartment_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择权限区域</h4>
            </div>
            <div class="modal-body">
                <div class="row-fluid">
                    <div class="span7">
                        <div id="dialogAreaTree"></div>
                    </div>
                    <div class="span5">
                        <div id="dialogAreaSchool"></div>
                    </div>
                </div>
                <div class="control-group">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="chooseRegionBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<#--修改信息弹窗-->
<div id="editDepInfo_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">修改信息</h4>
            </div>
            <div class="modal-body">
                <div id="editInfoDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="editDepSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<#--修改HC信息弹窗-->
<div id="setHC_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">修改信息</h4>
            </div>
            <div class="modal-body">
                <div id="setHCDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="setHCSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<#--用户调整角色弹窗-->
<div id="userUpdateRole_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">调整角色</h4>
            </div>
            <div class="modal-body">
                <div class="form-horizontal">
                    <div class="control-group">
                        <div class="controls">
                        <div class="userRoleList_dialog"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="updateRoleSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<#--调整部门弹窗-->
<div id="userUpdateDep_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择新部门及角色</h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span2">
                            <label for="">新部门:</label>
                        </div>
                        <div id="useUpdateDep_con_dialog" class="span10"></div>
                    </div>
                </div>
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span2">
                            <label for="">新角色:</label>
                        </div>
                        <div id="newRoleListSelect" class="span10"></div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="updateDepSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<#--修改业绩目标-->
<div id="changeTarget_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">修改业绩目标</h4>
            </div>
            <div class="modal-body">
                <div id="changeTarget_con_dialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="changeTargetSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="targetHistory_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">修改记录：</h4>
            </div>
            <div class="modal-body">
                <div id="targetHistory_con_dialog" class="form-horizontal">

                </div>
            </div>
        </div>
    </div>
</div>
