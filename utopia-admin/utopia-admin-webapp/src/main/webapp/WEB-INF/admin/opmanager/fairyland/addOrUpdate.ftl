<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用管理" page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<br/><br/>
<div id="main_container" class="span9">
    <form class="form-horizontal" action="" method="post" id="fairylandProductForm">
        <input type="hidden" id="fairylandProductId" name="fairylandProductId"
               value="<#if fairylandProduct??> ${fairylandProduct.id!}</#if>"/>

        <#if errMsg??>
            <div class="control-group">
                <label class="col-sm-2 control-label"></label>
                <div class="controls">
                ${errMsg}
                </div>
            </div>
        </#if>
        <div class="control-group">
            <label class="col-sm-2 control-label">平台类型</label>
            <div class="controls">
                <select id="platform" name="platform">
                    <#if platformTypeMap?exists>
                        <#list platformTypeMap?keys as key>
                            <option value="${key}"
                                    <#if fairylandProduct?? && fairylandProduct.platform?default("") == key>selected</#if>>${platformTypeMap[key]?default("")}</option>
                        </#list>
                    </#if>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">选择产品类型</label>
            <div class="controls">
                <select id="productType" name="productType">
                    <#if productTypeMap?exists>
                        <#list productTypeMap?keys as key>
                            <option value="${key}"
                                    <#if fairylandProduct?? && fairylandProduct.productType?default("") == key>selected</#if>>${productTypeMap[key]?default("")}</option>
                        </#list>
                    </#if>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">产品名称</label>
            <div class="controls">
                <input type="text" id="productName" name="productName" class="form-control"
                       value="${(fairylandProduct.productName)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">appKey</label>
            <div class="controls">
                <input type="text" id="appKey" name="appKey" class="form-control"
                       value="${(fairylandProduct.appKey)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">产品描述</label>
            <div class="controls">
                <textarea id="productDesc" name="productDesc" class="form-control"
                          rows="5">${(fairylandProduct.productDesc)!''}</textarea>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">产品图标地址(128&times;128)</label>
            <div class="controls">
                <input type="text" id="productIcon" name="productIcon" class="form-control"
                       value="${(fairylandProduct.productIcon)!}"/>
                <button type="button" onclick="uploadImage('#productIcon')" class="btn btn-primary">上传图片</button>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">产品图标地址(326&times;180)</label>
            <div class="controls">
                <input type="text" id="productRectIcon" name="productRectIcon" class="form-control"
                       value="${(fairylandProduct.productRectIcon)!}"/>
                <button type="button" onclick="uploadImage('#productRectIcon')" class="btn btn-primary">上传图片</button>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">背景图片地址</label>
            <div class="controls">
                <input type="text" id="backgroundImage" name="backgroundImage" class="form-control"
                       value="${(fairylandProduct.backgroundImage)!}"/>
                <button type="button" onclick="uploadImage('#backgroundImage')" class="btn btn-primary">上传图片</button>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">购买页面顶部banner</label>
            <div class="controls">
                <input type="text" id="bannerImage" name="bannerImage" class="form-control"
                       value="${(fairylandProduct.bannerImage)!}"/>
                <button type="button" onclick="uploadImage('#bannerImage')" class="btn btn-primary">上传图片</button>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">购买页面底部详细图</label>
            <div class="controls">
                <input type="text" id="descImage" name="descImage" class="form-control"
                       value="${(fairylandProduct.descImage)!}"/>
                <button type="button" onclick="uploadImage('#descImage')" class="btn btn-primary">上传图片</button>
                <span class="controls-desc"></span>
            </div>
        </div>

        <div class="control-group">
            <label class="col-sm-2 control-label">进入应用地址</label>
            <div class="controls">
                <input type="text" id="launchUrl" name="launchUrl" class="form-control"
                       value="${(fairylandProduct.launchUrl)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">预发布进入应用地址</label>
            <div class="controls">
                <input type="text" id="stagingLaunchUrl" name="stagingLaunchUrl" class="form-control"
                       value="${(fairylandProduct.stagingLaunchUrl)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">点击按钮文字</label>
            <div class="controls">
                <input type="text" id="launchBtnText" name="launchBtnText" class="form-control"
                       value="${(fairylandProduct.launchBtnText)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">提示信息</label>
            <div class="controls">
                <input type="text" id="promptMessage" name="promptMessage" class="form-control"
                       value="${(fairylandProduct.promptMessage)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">维护提示</label>
            <div class="controls">
                <input type="text" id="suspendMessage" name="suspendMessage" class="form-control"
                       value="${(fairylandProduct.suspendMessage)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">产品排名</label>
            <div class="controls">
                <input type="text" id="rank" name="rank" class="form-control" value="${(fairylandProduct.rank)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">使用平台描述</label>
            <div class="controls">
                <input type="text" id="usePlatformDesc" name="usePlatformDesc" class="form-control"
                       value="${(fairylandProduct.usePlatformDesc)!}"/>
                <span class="controls-desc"></span>
            </div>

        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">运营消息</label>
            <div class="controls">
                <input type="text" id="operationMessage" name="operationMessage" class="form-control"
                       value="${(fairylandProduct.operationMessage)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">使用人数预设值</label>
            <div class="controls">
                <input type="text" id="baseUsingNum" name="baseUsingNum" class="form-control" value="${(fairylandProduct.baseUsingNum)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">分类描述</label>
            <div class="controls">
                <input type="text" id="catalogDesc" name="catalogDesc" class="form-control" value="${(fairylandProduct.catalogDesc)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">热点提示</label>
            <div class="controls">
                <select id="hotFlag" name="hotFlag" class="form-control">
                    <option value="true"
                            <#if (fairylandProduct.hotFlag)?? && fairylandProduct.hotFlag == true>selected</#if>>有提示
                    </option>
                    <option value="false"
                            <#if (fairylandProduct.hotFlag)?? && fairylandProduct.hotFlag == false>selected</#if>>无提示
                    </option>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">新产品标记</label>
            <div class="controls">
                <select id="newFlag" name="newFlag" class="form-control">
                    <option value="true"
                            <#if (fairylandProduct.newFlag)?? && fairylandProduct.newFlag == true>selected</#if>>有提示
                    </option>
                    <option value="false"
                            <#if (fairylandProduct.newFlag)?? && fairylandProduct.newFlag == false>selected</#if>>无提示
                    </option>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">是否推荐标记</label>
            <div class="controls">
                <select id="recommendFlag" name="recommendFlag" class="form-control">
                    <option value="true"
                            <#if (fairylandProduct.recommendFlag)?? && fairylandProduct.recommendFlag == true>selected</#if>>有提示
                    </option>
                    <option value="false"
                            <#if (fairylandProduct.recommendFlag)?? && fairylandProduct.recommendFlag == false>selected</#if>>无提示
                    </option>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>

        <div class="control-group">
            <label class="col-sm-2 control-label">跳转类型</label>
            <div class="controls">
                <select id="redirectType" name="redirectType">
                    <#if fairylandProductRedirectTypeMap?exists>
                        <#list fairylandProductRedirectTypeMap?keys as key>
                            <option value="${key}"
                                    <#if fairylandProduct?? && fairylandProduct.redirectType?default("") == key>selected</#if>>${fairylandProductRedirectTypeMap[key]?default("")}</option>
                        </#list>
                    </#if>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">当前状态</label>
            <div class="controls">
                <select id="status" name="status" class="form-control">
                    <option value="ONLINE"
                            <#if (fairylandProduct.status)?? && fairylandProduct.status == 'ONLINE'>selected</#if>>
                        上架
                    </option>
                    <option value="OFFLINE"
                            <#if (fairylandProduct.status)?? && fairylandProduct.status == 'OFFLINE'>selected</#if>>
                        下架
                    </option>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <button type="button" id="btn_cancel" name="btn_cancel" class="btn">取消</button>
                &nbsp;&nbsp;
                <button type="button" id="btn_save" name="btn_save" class="btn btn-primary">保存</button>
            </div>
        </div>
    </form>
    <#include "../uploadFile.ftl" />

</div>
<script type="text/javascript">

    var parentValidInput = {
        'appKey': '\t填写提示：(平台类型＋appKey)唯一',
        'productName': '\t填写提示：长度不能超过10个文字',
        'productDesc': '\t填写提示：长度不能超过40个文字',
        'productIcon': '\t填写提示：购买列表图标(128×128)',
        'productRectIcon': '\t填写提示：购买列表图标(326×180)',
        'backgroundImage': '\t填写提示：学习圈背景图',
        'launchUrl': '\t填写提示：APP使用入口地址',
        'stagingLaunchUrl':'\t填写提示：APP预发布入口地址',
        'status': '',
        'operationMessage': '\t填写提示：长度不能超过15个文',
        'hotFlag': '',
        'newFlag': '',
        'usePlatformDesc': '\t填写提示：APP/电脑适用 APP适用',
        'baseUsingNum': '\t填写提示：用户使用人数＝后台配置人数+实际购买人数',
        'rank':'',
        'redirectType': '',
        'recommendFlag': '',
        'catalogDesc': '\t填写提示：分类描述',
        'bannerImage':'\t填写提示：购买页面顶部banner',
        'descImage':'\t填写提示：购买页面底部详细描述',
        'promptMessage':'\t填写提示：购买页面弹框提示',
        'btn_save':''
    };
    var parentOaValidInput = {
        'appKey': '\t填写提示：(平台类型＋appKey)唯一',
        'productName': '\t填写提示：长度不能超过10个文字',
        'productDesc': '\t填写提示：长度不能超过40个文字',
        'productIcon': '\t填写提示：购买列表图标(128×128)',
        'productRectIcon': '\t填写提示：购买列表图标(326×180)',
        'status': '',
        'operationMessage': '\t填写提示：长度不能超过15个文',
        'hotFlag': '',
        'newFlag': '',
        'rank':'',
        'redirectType': '',
        'recommendFlag': '',
        'btn_save':''
    };
    var studentValidInput = {
        'appKey': '\t填写提示：(平台类型＋appKey)唯一',
        'productName': '\t填写提示：长度不能超过10个文字',
        'productDesc': '\t填写提示：长度不能超过40个文字',
        'productIcon': '\t填写提示：购买列表图标(128×128)',
        'productRectIcon': '\t填写提示：购买列表图标(326×180)',
        'launchUrl': '\t填写提示：APP使用入口地址',
        'stagingLaunchUrl':'\t填写提示：APP预发布入口地址',
        'rank':'',
        'status': '',
        'hotFlag': '',
        'newFlag': '',
        'redirectType': '',
        'recommendFlag': '',
        'catalogDesc': '\t填写提示：分类描述',
        'btn_save':''
    };

    var pcStudentValidInput = {
        'appKey': '',
        'productName': '',
        'productDesc': '',
        'productIcon': '\t填写提示：购买列表图标(128×128)',
        'productRectIcon': '\t填写提示：购买列表图标(326×180)',
        'operationMessage': '\t填写提示：长度不能超过15个文字',
        'launchUrl': '',
        'stagingLaunchUrl':'\t填写提示：APP预发布入口地址',
        'status': '',
        'btn_save':''
    };

    var validNotNullData = {
        "#productName": "标题",
        "#appKey": "appKey"
    };

    //依据不同平台显示不同参数
    function hideInvalidInput(platformType) {
        if(platformType == '')return;
        $(".control-group").attr("style","display:none");

        var displayInputs;
        if(platformType == "STUDENT_APP"){
            displayInputs = studentValidInput;
        }else if(platformType == "PARENT_APP"){
            displayInputs = parentValidInput;
        }else if(platformType == "STUDENT_PC" ){
            displayInputs = pcStudentValidInput;
        }else if(platformType == "PARENT_OA"){
            displayInputs = parentOaValidInput;
        }
        $("[name=platform]").parent().parent().attr("style","display:");
        $("[name=productType]").parent().parent().attr("style","display:");
        $.each(displayInputs, function (key, value) {
            $("[name="+key+"]").parent().parent().attr("style","display:");
            $("[name=" + key + "]").siblings(".controls-desc").html(value);
        });
    }

    function uploadImage(node) {
        initParamData(node);
        $('#uploadphotoBox').modal('show');
    }

    $(function () {

        hideInvalidInput($("#platform").val());

        $("#platform").change( function() {
            hideInvalidInput($("#platform").val());
        });

        //修改状态下不能够编辑的字段
        if ($("#fairylandProductId").val() != "") {
            $("#appKey").attr("disabled", true);
            $("#platform").attr("disabled", true);
            $("#productType").attr("disabled", true);
        }

        $('#btn_cancel').on('click', function () {
            $("#vendor_app_edit_form").attr("action", "index.vpage");
            $("#vendor_app_edit_form").submit();
        });

        $('#btn_save').on('click', function () {
                    if (validInputNotNull(validNotNullData)) {
                        if (validInputLength("#productName", "标题", 10)
                                && validInputLength("#productDesc", "副标题面熟", 40)
                                && validInputLength("#operationMessage", "运营消息", 15)) {
                            if ($("#fairylandProductId").val() == "") {
                                $("#fairylandProductForm").attr("action", "add.vpage");
                            } else {
                                $("#fairylandProductForm").attr("action", "update.vpage");
                            }

                            $("#fairylandProductForm").submit();
                        }
                    }
                }
        );
    });

    //判断长度是否满足
    function validInputLength(node, nodeName, maxlength) {
        if ($(node).val() != "" && $(node).val().length > maxlength) {
            alert(nodeName + "长度不能超过" + maxlength);
            return false;
        }else{
            return true;
        }

    }
    //判断是否为空
    function validInputNotNull(nodeDataList) {
        var result = true;
        $.each(nodeDataList, function (key, value) {
            if ($(key) != null && $(key).val() == "") {
                alert(value + "不能为空");
                result = false;
                return false;
            }
        });
        return result;
    }


</script>
</@layout_default.page>