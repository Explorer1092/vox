<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='客户端版本管理' page_num=4>
<#--<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>-->
<style>
    span { font: "arial"; }
</style>
<div id="main_container" class="span9">
    <div>
        <form id="s_form" action="?" method="post" class="form-horizontal">
            <fieldset>
                <legend>版本灰度管理
                    <a href="toeditpage.vpage" type="button" class="btn btn-info" >添加</a>
                    <a href="totestpage.vpage" type="button" class="btn btn-warning">测试</a>
                </legend>
            </fieldset>

        </form>
    </div>

    <div>
    <ul class="nav nav-tabs" role="tablist" id="levelList">
        <li role="presentation" class="active">
            <a href="#tab=junior" id="junior-tab">小学</a>
        </li>
        <li role="presentation" >
            <a href="#tab=middle" id="middle-tab">中学</a>
        </li>
        <li role="presentation" >
            <a href="#tab=onlive" id="onlive-tab">直播</a>
        </li>
        <li role="presentation" >
            <a href="#tab=tianji" id="tianji-tab">天玑</a>
        </li>
        <li role="presentation" >
            <a href="#tab=u3d" id="u3d-tab">U3D</a>
        </li>
        <li role="presentation" >
            <a href="#tab=android" id="android-tab">安卓插件</a>
        </li>
    </ul>
    <div class="tab-content" id="productList">
        <#if vers??>
            <div class="row-fluid">
                <div class="span12">
                    <div >
                        <div id="data_table_journal" >
                            <table style=" white-space:nowrap; overflow:hidden;align=:center" class="table table-bordered table-condensed table-striped" >
                                <thead>
                                <th>产品ID</th>
                                <th>产品名称</th>
                                <th>包版本号</th>
                                <th>宿主AP版本</th>
                                <th>优先级</th>
                                <th>渠道号</th>
                                <th>区域编码</th>
                                <th>学校ID</th>
                                <th>用户身份</th>
                                <th>状态</th>
                                <th>操作</th>
                                </thead>
                                <tbody id="productTBody">
                                    <#list vers as  ver >
                                    <tr data-productid="${ver.productId!}">
                                        <td style="display:none">${ver.id!}</td>
                                        <td><a href="toeditpage.vpage?id=${ver.id }">${ver.productId!}</a></td>
                                        <td>
                                            <#list enum as val>
                                                <#if ver.productId == val.getId()?c>${val.getTypeName()}</#if>
                                            </#list>
                                        </td>
                                        <td>${ver.apkVer!}</td>
                                        <td>${ver.ownerAppApkVer!}</td>
                                        <td>${ver.rank!0}</td>
                                        <td>${ver.channel!}</td>
                                        <td>${ver.region!}</td>
                                        <td>${ver.school!}</td>
                                        <td>${ver.userType!}</td>
                                        <td>
                                            <#if ver.status == 'draft'>草稿
                                            <#elseif ver.status == 'published'>已发布
                                            </#if>
                                        </td>
                                        <td>
                                            <#if ver.status == 'draft'><a href="topublish.vpage?id=${ver.id }" type="button" class="btn btn-success">发布</a>
                                            <#elseif ver.status == 'published'><a href="todraft.vpage?id=${ver.id }" type="button" class="btn btn-danger">下线</a>
                                            </#if>
                                        </td>
                                        <td><a href="toremove.vpage?id=${ver.id }" type="button" class="btn btn-danger">删除</a></td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </#if>
    </div>
</div>

<div id="add_dialog" class="modal fade hide" style="width: 60%; left: 40%;">
    <div class="modal-dialog">
        <div class="modal-content" style="padding: 10px 10px;" >


        </div>
    </div>
</div>

<script type="text/javascript">
    $(function() {
        $('#levelList li').on('click', function () {
            var tabId = $(this).find('a').attr('id');
            switchContent(tabId);
        });

        // 根据tab过滤数据
        var filterProductList = function (reg) {
            $('#productTBody tr').each(function (index, productTr) {
                if (reg.test($(productTr).attr('data-productid'))) {
                    $(productTr).show();
                } else {
                    $(productTr).hide();
                }
            })
        };

        // 激活当前tab
        var activeTab = function (tabId) {
            $('#levelList li').removeClass('active');
            $('#' + tabId).parent('li').addClass('active');
        };

        // 选择过滤条件
        var switchContent = function (tabId) {
            activeTab(tabId);
            switch(tabId) {
                case 'junior-tab':
                    filterProductList(/^[1-3][0][0-1]+$/);
                    break;
                case 'middle-tab':
                    filterProductList(/^[1-3][1][0-1]+$/);
                    break;
                case 'onlive-tab':
                    filterProductList(/^([4]|[7])[0][0-1]+$/);
                    break;
                case 'tianji-tab':
                    filterProductList(/^[9]\d+$/);
                    break;
                case 'u3d-tab':
                    filterProductList(/^[5][0][0-1]|[10][0-1][5]\d+$/);
                    break;
                case 'android-tab':
                    filterProductList(/[100][7]\d+$/);
                    break;
                default:
            }
        };

        // getHashParams
        var getHashParameters = function () {
            var arr = (location.hash || "").replace(/^\#/,'').split("&");
            var params = {};
            for(var i=0; i<arr.length; i++){
                var data = arr[i].split("=");
                if(data.length == 2){
                    params[data[0]] = data[1];
                }
            }
            return params;
        };

        // getHash
        var getHashParameter = function (key) {
            var params = getHashParameters();
            return params[key];
        };

        // 初始展示小学tab列表
        switchContent((getHashParameter('tab') || 'junior') + '-tab');
    });
</script>

</@layout_default.page>