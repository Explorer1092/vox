        <#--<div class="box-header well">-->
            <#--<h2><i class="icon-th"></i>下发设置</h2>-->
        <#--</div>-->
        <fieldset>
            <div class="control-group">
                <label class="control-label">标题</label>
                <div class="controls">
                    <label class="control-label">
                        <input type="text" id="title" name="title" AUTOCOMPLETE="OFF" value="${title!''}" placeholder="请输入工作表名称"/>
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">上级是否可以看下属</label>
                <div class="controls">
                    <label class="control-label" style="text-align: left;">
                        <input type="radio" name="subordinate" value="true"  <#if control?? && control.allowViewSubordinateData?has_content && control.allowViewSubordinateData>checked</#if>/>是
                    </label>
                    <label class="control-label" style="text-align: left;">
                        <input type="radio" name="subordinate" value="false"  <#if control?? && control.allowViewSubordinateData?has_content && !control.allowViewSubordinateData>checked</#if>/>否
                    </label>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">是否可下载</label>
                <div class="controls">
                    <label class="control-label" style="text-align: left;">
                        <input type="radio" name="_download" value="true" <#if control?? && control.allowDownload?has_content && control.allowDownload>checked</#if>/>是
                    </label>
                    <label class="control-label" style="text-align: left;">
                        <input type="radio" name="_download" value="false" <#if control?? && control.allowDownload?has_content && !control.allowDownload>checked</#if>/>否
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">适用角色</label>
                <div class="controls">
                    <#if marketRoleTypeList?? && marketRoleTypeList?size gt 0>
                    <#list marketRoleTypeList as list>
                        <input type="checkbox"  name ="roleType" value="${list.id!0}" <#if control?? && control.roleTypeList?? && control.roleTypeList?size gt 0><#list control.roleTypeList as role><#if list.id == role.id>checked</#if></#list></#if>> ${list.roleName}
                    </#list>
                    </#if>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">选择部门</label>
                <div class="controls">
                    <div id="useUpdateDep_con_dialog" class="span4"></div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">备注说明：</label>
                <div class="controls">
                    <textarea class="input-xlarge" id="comment" rows="5" style="width: 880px;" maxlength="500" placeholder="选填，限500字以内">${comment!''}</textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-primary submitBtn" data-info="0">取消</button>
                <button type="button" class="btn btn-primary submitBtn" data-info="save_data.vpage">保存</button>
                <#if control?? && !control.publishId?has_content>
                    <button type="button" class="btn btn-primary submitBtn" data-info="publish_onOffLine.vpage">保存并发布</button>
                </#if>
            </div>
        </fieldset>
        <script>


        </script>


