<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>个人微信号管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                帐号类型：<select id="accountType" name="accountType">
                <option value="">全部</option>
                <option value="NORMAL" <#if (((accountType)!'') == 'NORMAL')>selected="selected"</#if>>B端</option>
                <option value="CHANNELC" <#if (((accountType)!'') == 'CHANNELC')>selected="selected"</#if>>C端</option>
            </select>
            </span>
            <span style="white-space: nowrap;">
                课程ID：<select id="selectLessonId" name="selectLessonId">
                <#if lessonIds?? && lessonIds?size gt 0>
                    <#list lessonIds as lessonId>
                        <option value="${lessonId}"
                                <#if (((selectLessonId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                    </#list>
                <#else>
                    <option value="">暂无数据</option>
                </#if>
            </select>
            </span>
            <span style="white-space: nowrap;">
                微信号：<input type="text" id="wechat" name="wechat" value="${wechat!''}"/>
            </span>
        </div>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <a class="btn btn-warning" id="newWechat" name="wechat_detail">新建微信信息</a>
    <a class="btn btn-success" id="exportWechat" name="exportWechat">导出微信数据</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>序号</th>
                        <th>个人微信号</th>
                        <th>个人微信二维码</th>
                        <th>被报名次数</th>
                        <th>组别</th>
                        <th>班级区</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as wechatInfo>
                            <tr>
                                <td style="display: none;">${wechatInfo.id!''}</td>
                                <td>${wechatInfo.index!''}</td>
                                <td>${wechatInfo.self_wechat!''}</td>
                                <td>${wechatInfo.wechat_code!''}</td>
                                <td>${wechatInfo.join_count!''}</td>
                                <td><#if wechatInfo.accountType?? && wechatInfo.accountType=="NORMAL">
                                    B端<#elseif wechatInfo.accountType?? && wechatInfo.accountType=="CHANNELC">
                                    C端</#if></td>
                                <td>${wechatInfo.class_area!''}</td>
                                <td>
                                    <a class="btn btn-primary"
                                       data-op_id="${wechatInfo.id!''}" name="wechat_detail">修改</a>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <div class="message_page_list">
                <#--<li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>-->
                    <#--<#if hasPrev>-->
                        <#--<li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>-->
                    <#--<#else>-->
                        <#--<li class="disabled"><a href="#">&lt;</a></li>-->
                    <#--</#if>-->
                    <#--<li class="disabled"><a>第 ${currentPage!} 页</a></li>-->
                    <#--<li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>-->
                    <#--<#if hasNext>-->
                        <#--<li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>-->
                    <#--<#else>-->
                        <#--<li class="disabled"><a href="#">&gt;</a></li>-->
                    <#--</#if>-->
                </div>
            </div>
        </div>
    </div>
</div>
<div id="wechat_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>个人微信号编辑</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul style="display: none" id="OpId"></ul>
            <ul class="inline">
                <li>
                    <dt>个人微信号</dt>
                    <dd><input type="text" id="wechatNum" value=""/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>个人微信二维码</dt>
                    <dd><img src="" id="wechatCode"/></dd>
                    <dd><input class="fileUpBtn" type="file"
                               accept="image/gif, image/jpeg, image/png, image/jpg"
                               style="float: left"
                    /></dd>

                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>组别</dt>
                    <dd><select id="selectAccountType" name="selectAccountType">
                        <option value="NORMAL">B端</option>
                        <option value="CHANNELC">C端</option>
                    </select></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>班级区</dt>
                    <dd><select id="classArea" name="classArea">
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                        <option value="6">6</option>
                        <option value="7">7</option>
                        <option value="8">8</option>
                        <option value="9">9</option>
                        <option value="10">10</option>
                    </select></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>课程Id</dt>
                    <dd><input type="text" id="lessonId" value=""/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="save_record" class="btn btn-primary">保 存</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<script type="text/javascript">


    $(function () {

        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        //初始化lessonid的list,后面做校验用
        var array = [];
        $("#selectLessonId").find("option").each(function () {
            var txt = $(this).val();
            if (txt != '') {
                array.push(txt);
            }
        });
        console.log(array);

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });
        //新建直接弹出modal
        $("#newWechat").on('click', function () {
            $("#OpId").html("");
            $("#wechatNum").val("");
            $("#selectAccountType").val("");
            $("#lessonId").val("");
            $("#wechatCode").attr('src', "");
            $("#wechatCode").data('file_name', "");
            $("#wechat_dialog").modal('show');
        });
        //编辑的时候弹出modal时加载数据
        $("a[name='wechat_detail']").on('click', function () {
            var opId = $(this).data("op_id");
            if (!opId) {
                console.log("opId null");
                return;
            }
            $.ajax({
                url: 'getOpWechatAccountById.vpage',
                type: 'GET',
                async: false,
                data: {"id": opId},
                success: function (data) {
                    if (data.success) {
                        $("#OpId").html(data.opWechatAccount.id);
                        $("#wechatNum").val(data.opWechatAccount.self_wechat);
                        $("#selectAccountType").val(data.opWechatAccount.accountType);
                        $("#lessonId").val(data.opWechatAccount.lessonId);
                        $("#wechatCode").attr('src', data.opWechatAccount.wechat_code);
                        $("#wechatCode").data('file_name', data.opWechatAccount.wechat_code_file);
                        $("#wechat_dialog").modal('show');
                    } else {
                        console.log("data error");
                    }
                }
            });
        });

        //保存
        $("#save_record").on('click', function () {
            var opId = $("#OpId").text();
            var weChatNum = $("#wechatNum").val();
            var codeUrl = $("#wechatCode").data("file_name");
            var accountType = $("#selectAccountType").val();
            var lessonId = $("#lessonId").val();
            var classArea = $("#classArea").val();

            if (!weChatNum) {
                alert("微信号不能为空！");
                return;
            }
            if (!codeUrl) {
                alert("微信二维码未上传");
                return;
            }
            if (!lessonId || $.inArray(lessonId, array) == -1) {
                alert("请输入正确的课程id");
                return;
            }
            if(!accountType){
                alert("请选择组别！");
                return;
            }
            var postData = {
                id: opId,
                weChatNum: weChatNum,
                codeUrl: codeUrl,
                accountType: accountType,
                lessonId: lessonId,
                classArea: classArea
            };
            $.ajax({
                url: 'saveOpWechatAccount.vpage',
                type: 'POST',
                async: false,
                data: postData,
                success: function (data) {
                    if (data.success) {
                        alert("保存成功");
                        $("#wechat_dialog").modal('hide');
                        window.location.reload();
                    } else {
                        alert(data.info);
                        console.log("data error");
                    }
                }
            });

        });
        //上传图片
        $(".fileUpBtn").change(function () {

            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                    alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                var fileSize = ($this[0].files[0].size / 1024 / 1012).toFixed(4); //MB
                console.info(fileSize);
                if (fileSize >= 2) {
                    alert("图片过大，重新选择。");
                    return false;
                }
                $.ajax({
                    url: 'uploadQrCode.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
//                            var img_html = '<img src="' + data.imgUrl + '" data-file_name="' + data.imgName + '">';
                            $("#wechatCode").attr('src', data.imgUrl);
                            $("#wechatCode").data("file_name", data.imgName);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        //导出数据
        $("#exportWechat").on('click', function () {
            var accountType = $("#accountType").val();
            var lessonId = $("#selectLessonId").val();
            var wechat = $("#wechat").val();
            if (!lessonId) {
                alert("课程ID不能为空");
                return;
            }
            location.href = "/opmanager/studyTogether/exportWechatData.vpage?selectLessonId=" + lessonId + "&accountType=" + accountType + "&wechat=" + wechat;
        });
    });


    function pagePost(pageNumber) {

        $("#pageNum").val(pageNumber);
        $("#op-query").submit();
    }
</script>
</@layout_default.page>