<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="金刚位管理" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        金刚位设置
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="form-horizontal">
                    <input type="hidden" id="id" name="id" value="${vicePosition.id!}"/>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">主标题</label>
                        <div class="controls">
                            <input type="text" id="mainTitle" name="mainTitle" class="form-control" value="${vicePosition.mainTitle!}"/>
                            <span style="color: red">*</span>不允许为空
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">英文主标题</label>
                        <div class="controls">
                            <input type="text" id="enMainTitle" name="enMainTitle" class="form-control" <#if vicePosition.enMainTitle?? && vicePosition.enMainTitle != ''>disabled="disabled"</#if> value="${vicePosition.enMainTitle!}"/>
                            <span style="color: red">*</span>不允许为空
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">副标题</label>
                        <div class="controls">
                            <input type="text" id="subheading" name="subheading" class="form-control" value="${vicePosition.subheading!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">选择分类</label>
                        <div class="controls">
                            <select id="classify" name="classify">
                                <#list types as classify>
                                    <option value="${classify!'暂无'}"
                                        <#if vicePosition.classify?? && vicePosition.classify == classify>selected</#if>>${classify!'暂无'}
                                    </option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">标签</label>
                        <div class="controls">
                            <input type="text" id="lable" name="lable" class="form-control" value="${vicePosition.lable!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">标签颜色</label>
                        <div class="controls">
                            <input type="text" id="labelColor" name="labelColor" class="form-control" value="${vicePosition.labelColor!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">附加标签</label>
                        <div class="controls">
                            <input type="text" id="tagText" name="tagText" class="form-control" value="${vicePosition.tagText!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">附加标签颜色</label>
                        <div class="controls">
                            <input type="text" id="tagTextColor" name="tagTextColor" class="form-control" value="${vicePosition.tagTextColor!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">自学习型</label>
                        <div class="controls">
                            <input type="text" id="selfStudyType" name="selfStudyType" class="form-control" value="${vicePosition.selfStudyType!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">跳转类型</label>
                        <div class="controls">
                            <select id="functionType" name="functionType">
                                <option value="" selected>选择任意一项</option>
                                <option value="H5" <#if vicePosition.functionType?? && vicePosition.functionType == 'H5'>selected</#if>>H5</option>
                                <option value="Native" <#if vicePosition.functionType?? && vicePosition.functionType == 'Native'>selected</#if>>Native</option>
                            </select>
                            <span style="color: red">*</span>不允许为空
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">跳转路径</label>
                        <div class="controls">
                            <input type="text" id="jumpUrl" name="jumpUrl" class="form-control" value="${vicePosition.jumpUrl!}"/>
                            <span style="color: red">*</span>绝对路径
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">预发布跳转路径</label>
                        <div class="controls">
                            <input type="text" id="stagingJumpUrl" name="stagingJumpUrl" class="form-control" value="${vicePosition.stagingJumpUrl!}"/>
                            <span style="color: red">*</span>绝对路径
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">Icon地址</label>
                        <div class="controls">
                            <input type="text" id="iconUrl" name="iconUrl" class="form-control"
                                   value="${(vicePosition.iconUrl)!}"/>
                            <button type="button" onclick="uploadImage('#iconUrl')" class="btn btn-primary">上传图片</button>
                            <span class="controls-desc"></span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">背景图片地址</label>
                        <div class="controls">
                            <input type="text" id="backImgUrl" name="backImgUrl" class="form-control"
                                   value="${(vicePosition.backImgUrl)!}"/>
                            <button type="button" onclick="uploadImage('#backImgUrl')" class="btn btn-primary">上传图片</button>
                            <span class="controls-desc"></span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">展示排位</label>
                        <div class="controls">
                            <input type="text" id="order" name="order" class="form-control" value="${vicePosition.order!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">展示位置</label>
                        <div class="controls">
                            <select id="disAddress" name="disAddress">
                                <option value="" selected>选择任意一项</option>
                                <option value="MP" <#if vicePosition.disAddress?? && vicePosition.disAddress=='MP'>selected</#if>>主位</option>
                                <option value="SP" <#if vicePosition.disAddress?? && vicePosition.disAddress=='SP'>selected</#if>>副位</option>
                            </select>
                            <span style="color: red">*</span>主副金刚位显示设置
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">登录后查看</label>
                        <div class="controls">
                            <select id="loginStatus" name="loginStatus">
                                <option value="" selected>选择任意一项</option>
                                <option value="true" <#if vicePosition.loginStatus?? && vicePosition.loginStatus==true>selected</#if>>是</option>
                                <option value="false" <#if vicePosition.loginStatus?? && vicePosition.loginStatus==false>selected</#if>>否</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">是否上架</label>
                        <div class="controls">
                            <select id="undercarriage" name="undercarriage">
                                <option value="" selected>选择任意一项</option>
                                <option value="false" <#if vicePosition.undercarriage?? && vicePosition.undercarriage==false>selected</#if>>是</option>
                                <option value="true" <#if vicePosition.undercarriage?? && vicePosition.undercarriage==true>selected</#if>>否</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">取消</a> &nbsp;&nbsp;
                            <button id="add_ad_btn" type="button" class="btn btn-primary">保存</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "uploadFile.ftl" />
</div>
<script type="text/javascript">

    function uploadImage(node) {
        initParamData(node);
        $('#uploadphotoBox').modal('show');
    }

    function validateInput(mainTitle, functionType, jumpUrl, enMainTitle) {
        if(mainTitle == '') {
            alert('主标题不允许为空！');
            return false;
        }
        if(enMainTitle == '') {
            alert('英文主标题不允许为空！');
            return false;
        }
        if(functionType == '') {
            alert('跳转类型不允许为空！');
            return false;
        }
        if(jumpUrl == '') {
            alert('跳转地址！');
            return false;
        }
        return true;
    }

    $(function() {
        $("#add_ad_btn").on("click",function(){
            var id = $("#id").val();
            var order = $("#order").val();
            var lable = $("#lable").val();
            var jumpUrl = $("#jumpUrl").val();
            var iconUrl = $("#iconUrl").val();
            var tagText = $("#tagText").val();
            var classify = $("#classify").val();
            var mainTitle = $("#mainTitle").val();
            var subheading = $("#subheading").val();
            var labelColor = $("#labelColor").val();
            var backImgUrl = $("#backImgUrl").val();
            var disAddress = $("#disAddress").val();
            var loginStatus = $("#loginStatus").val();
            var enMainTitle = $("#enMainTitle").val();
            var functionType = $("#functionType").val();
            var tagTextColor = $("#tagTextColor").val();
            var undercarriage = $("#undercarriage").val();
            var selfStudyType = $("#selfStudyType").val();
            var stagingJumpUrl = $("#stagingJumpUrl").val();
            if(!validateInput(mainTitle, functionType, jumpUrl, enMainTitle)) {
                return false;
            }
            if ('' == id) {
                $.post('add.vpage',{
                    order:order,
                    lable:lable,
                    jumpUrl:jumpUrl,
                    iconUrl:iconUrl,
                    tagText:tagText,
                    classify:classify,
                    mainTitle:mainTitle,
                    disAddress:disAddress,
                    subheading:subheading,
                    labelColor:labelColor,
                    backImgUrl:backImgUrl,
                    enMainTitle:enMainTitle,
                    loginStatus:loginStatus,
                    functionType:functionType,
                    tagTextColor:tagTextColor,
                    undercarriage:undercarriage,
                    selfStudyType:selfStudyType,
                    stagingJumpUrl:stagingJumpUrl
                },function(data){
                    if(!data.success){
                        alert(data.info);
                    }else{
                        window.location.href = 'index.vpage';
                    }
                });
            } else {
                $.post('update.vpage',{
                    id:id,
                    order:order,
                    lable:lable,
                    jumpUrl:jumpUrl,
                    iconUrl:iconUrl,
                    tagText:tagText,
                    classify:classify,
                    mainTitle:mainTitle,
                    disAddress:disAddress,
                    subheading:subheading,
                    labelColor:labelColor,
                    backImgUrl:backImgUrl,
                    enMainTitle:enMainTitle,
                    loginStatus:loginStatus,
                    functionType:functionType,
                    tagTextColor:tagTextColor,
                    undercarriage:undercarriage,
                    selfStudyType:selfStudyType,
                    stagingJumpUrl:stagingJumpUrl
                },function(data){
                    if(!data.success){
                        alert(data.info);
                    }else{
                        window.location.href = 'index.vpage';
                    }
                });
            }
        });
    });

</script>
</@layout_default.page>