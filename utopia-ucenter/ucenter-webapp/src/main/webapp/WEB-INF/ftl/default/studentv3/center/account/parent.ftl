<div class="tb-box">
    <div class="t-center-list">
        <#assign parentListLength = parentList?size>
        <#if parentListLength != 0><#--关联-->
            <div class="tf-left w-fl-left">
                <span class="w-detail w-right"></span>
            </div>
            <div class="tf-center w-fl-left">
                <p class="w-green">关联家长账号：已关联<span class="parentsListDisplay">
                <#list parentList as parent>
                    <#if parent.callName != "">
                        ${parent.callName}<#if parent_has_next>,</#if>
                    </#if>
                </#list>
                </span></p>
                <p>关联后，可以登录家长账号重置学生密码。 </p>
            </div>
            <div class="tf-right w-fl-left">
                <a class="w-btn-dic w-btn-gray-new accountBut" data-box_type="parent" href="javascript:void(0);">查看</a>
            </div>
        <#else>
            <div class="tf-left w-fl-left">
                <span class="w-detail w-wrong"></span>
            </div>
            <div class="tf-center w-fl-left">
                <p class="w-red">关联家长账号：未关联</p>
                <p>关联后，可以登录家长账号重置学生密码。  </p>
            </div>
            <div class="tf-right w-fl-left">
                <a class="w-btn-dic w-btn-green-new accountBut v-studentVoxLogRecord" data-op="bindMobile" data-box_type="parent" href="javascript:void(0);">关联家长</a>
            </div>
        </#if>
        <div class="w-clear"></div>
    </div>
    <div class="w-form-table accountBox" data-box_type="parent" style="display: none;text-align: center;">
        <#if parentListLength != 0>
            <style>
                .bindParentTable {
                    width: 470px;
                    margin: 20px auto 0 auto;
                    border-collapse: collapse;
                }
                .bindParentTable td{
                    border: 1px solid #ccc;
                    padding: 8px 0;
                }
                .bindParentTable td.name{
                    width: 30%;
                }
            </style>
            <div style="text-align: center;">
                <table class="bindParentTable">
                    <thead>
                    <tr>
                        <td class="name">已绑定家长</td>
                        <td>手机号</td>
                    </tr>
                    </thead>
                    <tbody>
                        <#list parentMoiles as parent>
                        <tr>
                            <td>${(parent.callName)!""}</td>
                            <td>${(parent.mobile)!"未绑定手机"}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>

        </#if>
        <p class="p-code" style="margin-top: 35px;">
            <img src="<@app.link href="public/skin/studentv3/images/parent-code.png"/>"/>
        <div style="font-size: 20px;margin: 20px;">
            <strong>扫一扫下载家长app,随时随地关注孩子学习</strong>
        </div>
        </p>
    </div>
</div>
<script type="text/javascript">
</script>