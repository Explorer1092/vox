<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='消息中心' page_num=18>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
    <#assign type_list = [1,2,3,4,5,6,7,8,9,10,11,12,13,14]>
<style>
    body{
        text-shadow:none;
    }
    .controls span .sendRange{margin-left:0}
    .region_name{padding: 5px 10px;border:1px solid #eaeaea;border-radius: 5px;position:relative;margin:8px;display: inline-block}
    .delete_region{position: absolute;top:-5px;right:-5px;cursor: pointer;color:#000}
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 消息中心</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">消息类型</label>
                        <div class="controls">
                            <label class="control-label">
                                <#if messageTypes == '1'>发送Push<#elseif messageTypes == '2'>发送系统消息</#if>
                            </label>
                        </div>
                    </div>
                    <#if messageTypes == '1'>
                        <fieldset class="fieldset_1">
                            <div class="control-group">
                                <label class="control-label">Push消息</label>
                                <div class="controls">
                                    <label class="control-label">
                                        <textarea name="pushContent" id="pushContent" cols="30" rows="10" maxlength="99" placeholder="最多输入99字">${messageInfo.pushContent!''}</textarea>
                                    </label>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">过期时间</label>
                                <div class="controls">
                                    <label class="control-label">
                                        <select name="expireTime" id="expireTime">
                                            <option value="0"></option>
                                            <#list type_list as list>
                                                <option value="${list}" <#if (messageInfo.expireTime!14) == list>selected</#if>>${list}天</option>
                                            </#list>
                                        </select>
                                    </label>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">跳转地址</label>
                                <div class="controls" id="sourceFileWrap">
                                    <input id="linkUrl" name="linkUrl" type="url" value="${messageInfo.linkUrl!''}"/>
                                </div>
                            </div>
                        </fieldset>
                    </#if>
                    <#if messageTypes == '2'>
                        <fieldset class="fieldset_2">
                            <div class="control-group schoolManager">
                                <label class="control-label">系统消息类型</label>
                                <div class="controls">
                                    <select id="notifyType" name="notifyType">
                                        <option value="SYSTEM" selected>默认</option>
                                    </select>
                                </div>
                            </div>
                            <div class="control-group schoolPopularity">
                                <label class="control-label">系统消息标题</label>
                                <div class="controls">
                                    <label style="text-align: left;float: left;padding-right: 20px;top: 50px;padding-top: 5px;">
                                        <input type="text" id="notifyTitle" name="notifyTitle" value="${messageInfo.notifyTitle!''}" maxlength="8"/>
                                    </label>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">系统消息内容</label>
                                <div class="controls">
                                    <textarea class="input-xlarge" id="notifyContent" rows="5" style="width: 880px;" maxlength="60" placeholder="最多输入60字">${messageInfo.notifyContent!''}</textarea>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">系统消息配图</label>
                                <div class="controls" id="sourceFileWrap">
                                    <input id="sourceFile" class="photoUrl" name="sourceExcelFile" type="file" />
                                    <#if messageInfo.photoUrl?has_content>
                                        <img id="uploadImage" src="${messageInfo.photoUrl!''}" class="upload_image" style="width: 160px;height:120px;display: block; "/>
                                    </#if>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">跳转地址</label>
                                <div class="controls" id="sourceFileWrap">
                                    <input id="linkUrl" name="linkUrl" type="url" value="${messageInfo.notifyUrl!''}"/>
                                </div>
                            </div>
                        </fieldset>
                    </#if>
                    <div class="control-group noedit-item">
                        <label class="control-label">发送范围</label>
                        <div class="controls">
                            <input class="sendRange" name="sendRange" type="radio" value="1" <#if messageInfo.sendRange == 1>checked</#if>>指定部门
                            <input class="sendRange" name="sendRange" type="radio" value="2" <#if messageInfo.sendRange == 2>checked</#if>>指定用户
                        </div>
                    </div>
                    <fieldset>
                        <fieldset class="group_user_1" <#if messageInfo.sendRange == 2>style="display:none"</#if>>
                            <div class="control-group noedit-item">
                                <label class="control-label">指定部门</label>
                                <div class="controls">
                                    <input id="choose_region" class="span2" type="button" value="选择部门" style="width:;">
                                </div>
                            </div>
                            <div class="control-group noedit-item">
                                <label class="control-label">&nbsp;</label>
                                <div id="seleced_region" class="controls">
                                    <#if groupListWithName?has_content && groupListWithName?size gt 0>
                                        <#list groupListWithName as list>
                                            <span class="region_name" data-key="${list.id!''}">${list.groupName!''} <a class="delete_region">X</a></span>
                                        </#list>
                                    </#if>
                                </div>
                            </div>
                            <div class="control-group noedit-item">
                                <label class="control-label">角色限制</label>
                                <div class="controls">
                                    <input type="checkbox"  name ="roleId" class="selectAll"> 全部
                                </div>
                            </div>
                            <div class="control-group noedit-item">
                                <div class="controls">
                                    <#if marketList?? && marketList?size gt 0>
                                        <#list marketList as list>
                                            <input type="checkbox"  name ="roleType" value="${list.id!0}" <#if roleTypes?has_content && roleTypes?size gt 0><#list roleTypes as type><#if type.id == list.id>checked</#if></#list></#if>> ${list.roleName}
                                        </#list>
                                    </#if>
                                </div>
                            </div>
                            <div class="control-group noedit-item">
                                <label class="control-label">&nbsp;</label>
                                <div class="controls">
                                    <#if channelList?? && channelList?size gt 0>
                                        <#list channelList as list>
                                            <input type="checkbox"  name ="roleType" value="${list.id!0}" <#if roleTypes?has_content && roleTypes?size gt 0><#list roleTypes as type><#if type.id == list.id>checked</#if></#list></#if>> ${list.roleName}
                                        </#list>
                                    </#if>
                                </div>
                            </div>
                            <div class="control-group noedit-item">
                                <label class="control-label">&nbsp;</label>
                                <div class="controls">
                                    <#if bigCustomerList?? && bigCustomerList?size gt 0>
                                        <#list bigCustomerList as list>
                                            <input type="checkbox"  name ="roleType" value="${list.id!0}" <#if roleTypes?has_content && roleTypes?size gt 0><#list roleTypes as type><#if type.id == list.id>checked</#if></#list></#if>> ${list.roleName}
                                        </#list>
                                    </#if>
                                </div>
                            </div>
                            <div class="control-group noedit-item">
                                <label class="control-label">&nbsp;</label>
                                <div class="controls">
                                    <#if others?? && others?size gt 0>
                                        <input type="checkbox"  name ="roleType"  value=" <#list others as list>${list.id!0}<#if list_has_next>,</#if></#list>" <#list roleTypes as type><#list others as list><#if type.id == list.id>checked </#if></#list></#list>> 其他
                                    </#if>
                                </div>
                            </div>
                        </fieldset>
                        <fieldset class="group_user_2" <#if messageInfo.sendRange == 1>style="display: none;"</#if>>
                            <div class="control-group">
                                <label class="control-label">用户ID</label>
                                <div class="controls">
                                    <label class="control-label">
                                        <textarea name="" id="userId" cols="30" rows="10" maxlength="99" placeholder="回车分隔">${userIds!''}</textarea>
                                    </label>
                                </div>
                            </div>
                        </fieldset>
                    </fieldset>
                    <div class="form-actions">
                        <button id="submitBtn" type="button" class="btn btn-primary">提交申请</button>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
    <div id="addDepartment_dialog" class="modal fade hide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">选择权限区域</h4>
                </div>
                <div class="modal-body">
                    <div class="row-fluid">
                        <div id="dialogAreaTree"></div>
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
</div>
<script id="region_info" type="text/html">
    {{#each selectArr}}
        <span class="region_name" data-key="{{key}}">{{cityName}} <a class="delete_region">X</a></span>
    {{/each}}
</script>
<script type="text/javascript">
    var groupIds = "${groupIds!''}";
    var renderDepartment = function(tempSelector,data,container){
        var source   = $(tempSelector).html();
        var template = Handlebars.compile(source);

        $(container).html(template(data));
    };
    $(document).on('change','input[name="sendRange"]',function(){
        var _this = $(this).val();
        if($(this).attr('checked') == 'checked'){
            $('.group_user_'+_this).show().siblings().hide();
        }else {
            $('.group_user_' + _this).hide().siblings().show();
        }
    });

    //设置状态 显示数据用
    var setFlagFunc = function (item) {
        item.forEach(function(i){
            if(i.children && i.children.length>0){
                var num = 0,len = i.children.length;
                for(var j=0;j<len;j++){
                    if(i.children[num].selected){
                        num++
                    }
                }
                if(num == len){
                    i.selected = true;
                }
                setFlagFunc(i.children)
            }
            i.selectFlag = i.selected;
        })
    };
    //每次选择地区获取已选择的数据
    var getAreaFunc = function(item,arr){
        item.forEach(function(i){
            if(i.selected){
                arr.push({data:i.data,cityName:i.title,key:i.key})
            } else if(i.children && i.children.length>0){
                getAreaFunc(i.children,arr)
            }
        })
    };
    var viewDepartment = function () {
        var subDialogTree = $("#dialogAreaTree");
        var selectTree = subDialogTree.fancytree("getTree").rootNode.children;
        selectArr = [];
        getAreaFunc(selectTree,selectArr);
    };

    var selectArr = [];
    var _res ;
    //初始化负责区域dialog数据
    var initSubDialogData = function(gid){
        var  subDialogTree = $("#dialogAreaTree");
        $.get("/message/manage/get_user_department_tree.vpage?groupIds="+gid,function (res) {
//            if(_res){
//                res = _res;
//            }
            setFlagFunc(res);
            subDialogTree.fancytree("destroy");
            subDialogTree.fancytree({
                extensions: ["filter"],
                source: res,
                checkbox: true,
                selectMode: 3,
                autoCollapse:true,
                select:function () {
                    viewDepartment();
                },
                init:function(){
                    var tree = $("#dialogAreaTree").fancytree("getTree");
                    tree.visit(function(node){
                        if(node.data.selectFlag){
                            node.setSelected(true);
                        }
                    });
                }
            });
            viewDepartment();
        });
    };
    initSubDialogData(groupIds);
    $(document).on("click","#choose_region",function () {
        $('#addDepartment_dialog').modal('show');
    });
    $(document).on("click",".delete_region",function () {
        var selectedIds = [];
        var _key = $(this).parent().data('key').toString();
        selectArr.forEach(function (v) {
            selectedIds.push(v.key);
        });
        selectArr.splice(selectedIds.indexOf(_key),1);
        selectedIds.splice(selectedIds.indexOf(_key),1);
        renderDepartment("#region_info",{selectArr:selectArr},"#seleced_region");
        initSubDialogData(selectedIds.toString())
    });
    $(document).on("change","#userId",function () {

        var re=/\n/g;
        var _this = $(this).val().replace(re,",");
        console.log(_this)
        $('#userId').val(_this);
    });
    $(document).on("click",'#chooseRegionBtn',function () {
        $("#addDepartment_dialog").modal('hide');
        renderDepartment("#region_info",{selectArr:selectArr},"#seleced_region");
        console.log(selectArr)
    });
    var imageFile = undefined;
    //选择封面
    $("#sourceFile").on('change',function (e) {
        imageFile = e.target.files;
        var sourceFile = $("#sourceFile").val();
        if (blankString(sourceFile)) {
            layer.alert("请选择封面！");
            return;
        }
        var fileParts = sourceFile.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt !== "jpg" && fileExt !== "jpeg" && fileExt !== "png") {
            $("#sourceFile").val('');
            $('.filename').text('No file selected');
            layer.alert("请上传正确格式(jpg、jpeg、png)的图片！");
            return false;
        }

        var reader = new FileReader();
        var width = 0;
        var height = 0;
        reader.onload = function(e) {
            var data = e.target.result;
            var image = new Image();
            image.src= data;
            image.onload=function(){
                width = image.width;
                height = image.height;
                if(width!=670||height!=300){
                    layer.alert("请上传670*300的图片！");
                    return false;
                }
                var img = '<img id="uploadImage" src="' + e.target.result + '" class="upload_image" style="width: 160px;height:120px;display: block; "/>';
                $('#sourceFileWrap img').remove();
                $('#sourceFileWrap').append(img);
            };
        };
        reader.readAsDataURL(e.target.files[0]);
        var postImage = new FormData();
        postImage.append(1,imageFile[0]);
        $.ajax({
            url: '/file/multiple_file_upload.vpage',
            type: "POST",
            data: postImage,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType : false,
            success: function (res) {
                if(res.success){
                    $('.upload_image').attr('src',res.imageUrlList[0]);
                    //上传封面成功 开始上传数据
//                                save(postData,$this);
                }
            }
        });
    });
    $('.selectAll').on('click',function () {
        var $this = $(this);
        if($this.prop('checked')){
            $('input[name="roleType"]').parent('span').addClass('checked');
            $('input[name="roleType"]').attr('checked',true);
        }else {
            $('input[name="roleType"]').parent('span').removeClass('checked');
            $('input[name="roleType"]').attr('checked',false);
        }
    });
    $(document).on('change','input[name="messageTypes"]',function(){
        var _this = $(this).val();
        if($(this).attr('checked') == 'checked'){
            $('.fieldset_'+_this).show().addClass('show');
        }else {
            $('.fieldset_' + _this).hide().removeClass('show');
        }
    });

    $(function(){
        if($('input[name="roleType"]:checked').length == $('input[name="roleType"]').length){
            $('.selectAll').parent('span').addClass('checked');
            $('.selectAll').attr('checked',true);
        }else{
            $('.selectAll').parent('span').removeClass('checked');
            $('.selectAll').attr('checked',false);
        }
        $('#submitBtn').live('click',function(){
            var postData = {};
            postData.messageTypes = ${messageTypes!0};
            postData.id = '${messageInfo.id!0}';
            if(postData.messageTypes == 1){
                var pushContent = $('#pushContent').val().trim();
                if(pushContent == ""){
                    alert("请输入Push内容！");
                    return false;
                }
                postData.linkUrl = $('#linkUrl').val().trim();
                postData.pushContent = pushContent;
                var expireTime = $('#expireTime option:selected').val().trim();
                if(expireTime == ""){
                    alert('请选择过期时间');
                    return false;
                }
                postData.expireTime = expireTime;
            }
            if(postData.messageTypes == 2){
                postData.linkUrl = $('#linkUrl').val().trim();
//                if(linkUrl == ""){
//                    alert("请填写跳转地址！");
//                    return false;
//                }
                var notifyType = $('#notifyType option:selected').val().trim();
                postData.notifyType = notifyType;
                var notifyTitle = $('#notifyTitle').val().trim();
                if(notifyTitle == ""){
                    alert("请填写通知内容！");
                    return false;
                }
                postData.notifyTitle = notifyTitle;
                var notifyContent = $('#notifyContent').val().trim();
                if(notifyContent == ""){
                    alert("请填写通知内容！");
                    return false;
                }
                postData.notifyContent = notifyContent;
                postData.photoUrl = $('.upload_image').attr('src');
            }
            postData.sendRange = $('input[name="sendRange"]:checked').val();
            if(postData.sendRange != 2 && postData.sendRange != 1){
                alert('请选择发送范围');
                return false;
            }
            if(postData.sendRange == 1){
                var groupIds = [];
                $("#dialogAreaTree").fancytree("getTree").rootNode.children.forEach(function(v){
                    groupIds.push(v.key);
                });
                if(groupIds.length == 0){
                    alert('请选择指定部门');
                    return false;
                }
                postData.groupIds = groupIds.toString();
                var roleTypes = [];
                $('input[name="roleType"]:checked').each(function () {
                    var roleType = $(this).val();
                    roleTypes.push(roleType);
                });
                if(roleTypes.length === 0){
                    layer.alert('请选择发布对象');
                    layer.close(_loading);
                    return false;
                }else{
                    postData.roleIds = roleTypes.toString();
                }
            }else if(postData.sendRange == 2){
                postData.userIds = $('#userId').val();
            }
            $.get('/message/manage/saveData.vpage',postData,function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    if(confirm("提交成功") == true){
                        window.location.href="message_list.vpage?messageType=${messageTypes!0}";
                    }
                }
            });
        });
    });
</script>

</@layout_default.page>
