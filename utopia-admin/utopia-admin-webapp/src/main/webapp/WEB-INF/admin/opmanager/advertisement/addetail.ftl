<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑广告" page_num=9 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>

<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑广告
        <a id="btn_cancel" href="adindex.vpage" name="btn_cancel" class="btn btn-xs"><i class="icon-home"></i> 返回列表</a>
        <#if ad??>
        <#--<#if requestContext.getCurrentAdminUser().realName == ad.creatorName && ad.status != 1>-->
            <#if editable?? && editable>
                <a id="save_ad_btn" href="javascript:void(0);" class="btn btn-xs btn-primary" title="保存广告"><i class="icon-file icon-white"></i> 保存广告</a>
                <a id="delete_ad_btn" href="javascript:void(0);" class="btn btn-xs btn-danger" title="删除广告"><i class="icon-trash icon-white"></i> 删除广告</a>
            </#if>
            <#if ad.auditStatus == 0>
            <div class="btn-group btn-group-xm">
                <button type="button" class="btn btn-xs btn-success dropdown-toggle" data-toggle="dropdown">
                    <i class="icon-user icon-white"></i> 提交审核 <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li><a href="javascript:void(0);" id="submit_ad_btn"><i class="icon-ok"></i> 广告审核</a></li>
                    <li><a href="javascript:void(0);" id="submit_fast_btn" title="广告投放对象为用户，并且数量少于五个"><i class="icon-fire"></i> 快速不审核上线</a></li>
                </ul>
            </div>
            </#if>
        <#else>
            <a id="save_ad_btn" class="btn btn-xs btn-primary" title="保存广告"><i class="icon-file icon-white"></i>保存广告</a>
        </#if>
        <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=30327452" target="_blank" class="btn btn-xs btn-info"><i class="icon-heart icon-white"></i> 流程详解</a>

        <#if ad??>
            <div style="float: right;">
                <button class="btn btn-xs  btn-primary" onclick="showOpLog(${ad.id})"><i class="icon-search icon-white"></i> 操作历史</button>
                <div class="btn-group btn-group-xs">
                    <button type="button" class="btn btn-xs btn-info dropdown-toggle" data-toggle="dropdown">
                        <i class="icon-th icon-white"></i> 查看数据 <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a title="查看广告实时数据" href="/opmanager/advertisement/config/realtimedata.vpage?adId=${ad.id}"><i class="icon-time"></i> 实时数据</a></li>
                        <li><a title="查看广告数据详情" href="/opmanager/advertisement/config/dataindex.vpage?adId=${ad.id}"><i class="icon-th-list"></i> 数据详情</a></li>
                    </ul>
                </div>
                <a title="配置投放策略" href="/opmanager/advertisement/config/adconfig.vpage?adId=${adId!}" class="btn btn-xs btn-warning"><i class="icon-cog icon-white"></i> 配置投放策略</a>
            </div>
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" name="detail_form" enctype="multipart/form-data" action="saveaddetail.vpage" method="post">
                    <input id="adId" name="adId" value="${adId!0}" type="hidden">
                    <input id="status" name="status" value="<#if ad??>${ad.status}</#if>" type="hidden">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">广告名称</label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control" value="<#if ad??>${ad.name!}</#if>" style="width: 336px"/>
                                <span id="tip-name" style="color:red;"></span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">广告编码</label>
                            <div class="controls">
                                <input type="text" id="code" name="code" class="form-control" value="<#if ad??>${ad.adCode!''}</#if>" style="width: 336px" placeholder="如不填写将随机生成12位字符串"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">广告说明</label>
                            <div class="controls">
                            <textarea id="description" name="description" class="form-control" rows="3" style="width: 336px;resize: none;" placeholder="请简要描述广告投放之意图"><#if ad??>${ad.description!}</#if></textarea>
                            <#--<textarea class="notice-info" style="width:260px; height: 60px; resize: none;overflow-y:none; color:red;" rows="3" disabled>-->
                                <#--广告说明在以下广告位产生作用：-->
                                <#--1. 学生APP-活动中心， 对应一级标题-->
                                <#--2. 老师微信福利活动，对应一级标题-->
                            <#--</textarea>-->
                            <span id="tip-desc" style="color:red;"></span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">广告位</label>
                            <div class="controls">
                                <select id="slotId" name="slotId" style="width: 350px;">
                                    <option value="">--请选择广告位--</option>
                                    <#list slotList as slot>
                                        <option data-stype="${slot.type!''}" data-utype="${slot.userType!''}"
                                                value="${slot.id}" <#if ad??><#if ad.adSlotId == slot.id>selected</#if></#if>>${slot.name!}
                                            (<#if slot.type == '纯文本'>纯文本<#else>${slot.width}×${slot.height}</#if>)
                                        </option>
                                    </#list>
                                </select>
                            </div>
                        </div>
                        <div class="control-group" id="div_marks" style="display: none;">
                            <label class="control-label" for="productName">新标签：</label>
                            <div class="controls">
                                <button data-toggle="modal"
                                        data-target="#chooseTagTree" class="btn btn-default">选择
                                </button>
                                <input type="text" class="selectValue" id="selectedtags" readonly>
                                <input type="hidden" name="markIds" id="hid_text_markIds"/>
                                <input type="hidden" name="markText" id="hid_text_markText"/>
                                <input type="hidden" name="advertisementMarkId" value="${advertisementMarkId!0}"/>
                            </div>
                        </div>
                        <#if isAdmin?? && isAdmin>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">优先级</label>
                            <div class="controls">
                                <select id="priority" name="priority" style="width: 350px;" <#if !isAdmin?? || !isAdmin>disabled</#if>>
                                    <option value=10>默认</option>
                                    <#list [1,2,3,4,5,6,7,8,9] as p>
                                        <option value=${p} <#if ad??><#if ad.priority == p> selected</#if></#if>>Lv.${p}</option>
                                    </#list>
                                    <option value=0 <#if ad??><#if ad.priority == 0> selected</#if></#if>>置顶</option>
                                </select>
                                <span style="color:red">(等级越高表示优先级越高)</span>
                            </div>
                        </div>
                        </#if>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转URL</label>
                            <div class="controls">
                                <input type="text" id="resourceUrl" name="resourceUrl" class="form-control" value="<#if ad??>${ad.resourceUrl!}</#if>" style="width: 336px;" placeholder="https://www.17zuoye.com(可空)"/>
                                <input type="checkbox" id="redirectWithUid" name="redirectWithUid" <#if ad?? && ad.isRedirectWithUid()> checked </#if>>&nbsp;&nbsp;跳转链接是否带用户ID
                                <span id="tip-url" style="color:red;"></span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">广告内容</label>
                            <div class="controls">
                            <textarea id="content" name="content" class="form-control" rows="4" style="width: 336px;resize: none;"
                                      placeholder="请根据实际场景填写广告投放内容"><#if ad??>${ad.adContent!}</#if></textarea>
                                <#--<textarea class="notice-info" style="text-align:left;width:260px;resize: none;overflow-y:none; color:red;" rows="4" disabled>-->
                                    <#--广告内容在以下广告位产生作用：-->
                                    <#--1. 学生APP-活动中心， 对应二级标题-->
                                    <#--2. 老师微信福利活动，对应二级标题-->
                                    <#--3. 所有PopUp消息，对应消息内容-->
                                <#--</textarea>-->
                                <span id="tip-text" style="color:red;"></span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">弹窗按钮文字</label>
                            <div class="controls">
                                <input type="text" id="btnContent" name="btnContent" class="form-control" value="<#if ad??>${ad.btnContent!}</#if>" style="width: 336px" placeholder="Popup消息时不能为空"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">投放日期</label>
                            <div class="controls">
                                <input type="text" id="showTimeStart" name="showTimeStart" class="form-control" value="<#if ad??><#if ad.showTimeStart??>${ad.showTimeStart?string('yyyy-MM-dd HH:mm:ss')}</#if></#if>" style="width: 150px;"/>
                                ~ <input type="text" id="showTimeEnd" name="showTimeEnd" class="form-control" value="<#if ad??><#if ad.showTimeEnd??>${ad.showTimeEnd?string('yyyy-MM-dd HH:mm:ss')}</#if></#if>" style="width: 150px;"/>
                                <span style="color:red">(投放日期必须填写)</span>
                            </div>
                        </div>
                        <input type="hidden" name="periods" id="periods" value="<#if ad??><#if ad.displayPeriod??>${ad.displayPeriod!''}</#if></#if>/">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">投放时间段</label>
                            <div class="controls">
                                <div style="width:360px;">
                                    <div style="float:left; width:240px;display: block;padding: 9px;margin: 0 0 10px;font-size: 13px;line-height: 20px;border: 1px solid #dddddd;">
                                        注意事项 :<br/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;1. 最多五个时间段<br/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;2. 每个时间段不少于60分钟<br/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;3. 时间段时间间隔不少于60分钟
                                    </div>
                                    <div style="float: right; width: 100px;height: 110px;">
                                    <a id="add_period_btn" href="javascript:void(0);" class=" btn btn-warning" style="margin: 65px 5px 5px 20px ; <#if periods?? && periods?size gte 5> display: none;</#if>">
                                        <i class="icon-plus icon-white"></i>增 行
                                    </a>
                                    </div>
                                </div>
                                <table class="table table-striped table-condensed table-bordered" style="width:360px;">
                                    <thead>
                                        <th>开始时间</th>
                                        <th>结束时间</th>
                                        <th style="width: 62px;">操作</th>
                                    </thead>
                                    <tbody id="periodTable">
                                    <#if periods?? && periods?size gt 0>
                                        <#list periods as p>
                                        <tr class="period-tr">
                                            <td><input type="text" class="input input-small time-chooser" name="period-start" value="${p.startTime?string("HH:mm")}"></td>
                                            <td><input type="text" class="input input-small time-chooser" name="period-end" value="${p.endTime?string("HH:mm")}"></td>
                                            <td><a href="javascript:void(0);" class="del-period">删除</a></td>
                                        </tr>
                                        </#list>
                                    </#if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">闪屏广告配置</label>
                            <div class="controls">
                                <input type="hidden" id="showLogo" name="showLogo" value="0"/>
                               <#-- <input type="checkbox" id="showLogo" name="showLogo" class="form-control" <#if ad?? && ad.showLogo> checked </#if>/>&nbsp;显示底部LOGO &nbsp;&nbsp;&nbsp;&nbsp;-->
                                显示时间：<input type="text" id="duration" name="duration" class="form-control" value="<#if ad??><#if ad.displayDuration??>${ad.displayDuration!0}</#if></#if>" style="width: 80px;"/> 秒
                                <span style="color:red">(请填写 3 或 4)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">单用户曝光阈值</label>
                            <div class="controls">
                                <input type="text" id="viewQuota" name="viewQuota" class="form-control" value="<#if ad??>${ad.userViewQuota!0}</#if>" style="width: 336px"/>
                                <span style="color:red">(不限制请不填写或者填写0)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">单用户点击阈值</label>
                            <div class="controls">
                                <input type="text" id="clickQuota" name="clickQuota" class="form-control" value="<#if ad??>${ad.userClickQuota!0}</#if>" style="width: 336px"/>
                                <span style="color:red">(不限制请不填写或者填写0)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">单用户日展示次数</label>
                            <div class="controls">
                                <input type="text" id="userDailyShowLimit" name="userDailyShowLimit" class="form-control" value="<#if ad??>${ad.userDailyShowLimit!0}</#if>" style="width: 336px"/>
                                <span style="color:red">(不限制请不填写或者填写0)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">展示次数</label>
                            <div class="controls">
                                <input type="text" id="showLimit" name="showLimit" class="form-control" value="<#if ad??>${ad.showLimit!0}</#if>" style="width: 336px"/>
                                <span style="color:red">(不限制请不填写或者填写0)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">每日最大点击次数</label>
                            <div class="controls">
                                <input type="text" id="dailyShowLimit" name="dailyClickLimit" class="form-control" value="<#if ad??>${ad.dailyClickLimit!0}</#if>" style="width: 336px"/>
                                <span style="color:red">(不限制请不填写或者填写0)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">点击次数</label>
                            <div class="controls">
                                <input type="text" id="clickLimit" name="clickLimit" class="form-control" value="<#if ad??>${ad.clickLimit!0}</#if>" style="width: 336px"/>
                                <span style="color:red">(不限制请不填写或者填写0)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">业务</label>
                            <div class="controls">
                                <select id="businessCategory" name="businessCategory" style="width: 350px">
                                    <#if categoryList??>
                                        <#list categoryList as c>
                                            <option <#if ad?? && ad.businessCategory??><#if ad.businessCategory == c.name()> selected="selected"</#if></#if> value = ${c.name()!}>${c.name()!}</option>
                                        </#list>
                                    </#if>
                                </select>
                                <span style="color:red">(请选择广告所属业务类型)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">类型</label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 350px">
                                    <#if typeList??>
                                        <#list typeList as t>
                                            <option <#if ad?? && ad.type??><#if ad.type == t.name()> selected="selected"</#if></#if> value = ${t.name()!}>${t.getDesc()!}</option>
                                        </#list>
                                    </#if>
                                </select>
                                <span style="color:red">(家长通新注册15天以内的家长对商业广告不可见)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否记录曝光日志</label>
                            <div class="controls">
                                <select id="logCollected" name="logCollected" style="width: 350px">
                                    <option <#if !ad?? || !ad.isLogCollected()>selected="selected"</#if> value="0">否</option>
                                    <option <#if ad?? && (ad.isLogCollected() || ad.adSlotId[0] == "2") >selected="selected"</#if> value="1">是</option>
                                </select>
                                <span style="color:red">(默认否，如果不需要默认否即可)</span>
                            </div>
                        </div>
                        <div class="control-group" id="multiAccountSupportView" style="display: none;">
                            <label class="col-sm-2 control-label">是否支持包班制老师查看</label>
                            <div class="controls">
                                <select id="multiAccountSupport" name="multiAccountSupport" style="width: 350px">
                                    <option <#if !ad?? || !ad.isMultiAccountSupport()>selected="selected"</#if> value="0">否</option>
                                    <option <#if ad?? && ad.isMultiAccountSupport()>selected="selected"</#if> value="1">是</option>
                                </select>
                                <span style="color:red">(默认否，如果不需要默认否即可)</span>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <#if ad??>
            <legend>广告投放素材</legend>
            <div class="well">
                <table class="table table-striped table-condensed table-bordered">
                    <tr>
                        <td>素材</td>
                        <td>素材预览</td>
                        <td>素材地址</td>
                        <td>操作</td>
                    </tr>
                    <tr style="display: none;">
                        <td></td>
                        <td></td>
                        <td></td>
                        <td>
                            <form id="form_wtf" onsubmit="return false;"></form>
                        </td>
                    </tr>
                    <tr>
                        <td>素材1</td>
                        <td><img id="imgSrc"
                                 <#if ad.imgUrl?? && ad.imgUrl?has_content>src="${prePath!}${ad.imgUrl!}" </#if>
                                 style="height:150px;"/></td>
                        <td><#if ad.imgUrl?? && ad.imgUrl?has_content><a href="${prePath!}${ad.imgUrl!}"
                                                                         target="_blank">${ad.imgUrl!}</a></#if></td>
                        <td>
                            <#if editable?? && editable>
                                <form id="form_img" name="form_img" enctype="multipart/form-data" action="uploadsrc.vpage" method="post">
                                    <a href="javascript:void(0);" class="uploader">
                                        <input type="file" name="file" id="file" accept="image/gif, image/jpeg, image/png, image/jpg" onchange="previewImg(this)">选择素材
                                    </a>
                                    <input type="hidden" name="type" value="img">
                                    <input type="hidden" name="adId" value="${ad.id}" id="ad_img">
                                    <a title="确认上传" href="javascript:void(0);" class="uploader" id="upload_img"><i class="icon-ok"></i></a>
                                    <#if ad.imgUrl?? && ad.imgUrl?has_content>
                                        <a title="删除" href="javascript:void(0);" class="uploader" id="clear_img"><i class="icon-trash"></i></a>
                                    </#if>
                                </form>
                            </#if>
                        </td>
                    </tr>
                    <#if hasGif?? && hasGif>
                        <tr>
                            <td>素材2</td>
                            <td><img id="gifSrc"<#if ad.gifUrl?? && ad.gifUrl?has_content>src="${prePath!}${ad.gifUrl!}" </#if> style="height:150px;"/></td>
                            <td><#if ad.gifUrl?? && ad.gifUrl?has_content><a href="${prePath!}${ad.gifUrl!}" target="_blank">${ad.gifUrl!}</a></#if></td>
                            <td>
                                <#if editable?? && editable>
                                    <form id="form_gif" name="form_gif" enctype="multipart/form-data" action="uploadsrc.vpage" method="post">
                                        <a href="javascript:void(0);" class="uploader">
                                            <input type="file" name="file" id="file" accept="image/gif, image/jpeg, image/png, image/jpg" onchange="previewGif(this)">选择素材
                                        </a>
                                        <input type="hidden" name="type" value="gif">
                                        <input type="hidden" name="adId" value="${ad.id}" id="ad_gif">
                                        <a title="确认上传" href="javascript:void(0);" class="uploader" id="upload_gif"><i class="icon-ok"></i></a>
                                        <#if ad.gifUrl?? && ad.gifUrl?has_content>
                                            <a title="删除" href="javascript:void(0);" class="uploader" id="clear_gif"><i class="icon-trash"></i></a>
                                        </#if>
                                    </form>
                                </#if>
                            </td>
                        </tr>
                    </#if>
                    <#if hasExt?? && hasExt>
                        <tr>
                            <td><#if hasGif?? && hasGif>素材3<#else>素材2</#if></td>
                            <td><img id="extSrc"<#if ad.extUrl?? && ad.extUrl?has_content>src="${prePath!}${ad.extUrl!}" </#if> style="height:150px;"/></td>
                            <td><#if ad.extUrl?? && ad.extUrl?has_content><a href="${prePath!}${ad.extUrl!}" target="_blank">${ad.extUrl!}</a></#if></td>
                            <td>
                                <#if editable?? && editable>
                                    <form id="form_ext" name="form_ext" enctype="multipart/form-data" action="uploadsrc.vpage" method="post">
                                        <a href="javascript:void(0);" class="uploader">
                                            <input type="file" name="file" id="file" accept="image/gif, image/jpeg, image/png, image/jpg" onchange="previewExt(this)">选择素材
                                        </a>
                                        <input type="hidden" name="type" value="ext">
                                        <input type="hidden" name="adId" value="${ad.id}" id="ad_ext">
                                        <a title="确认上传" href="javascript:void(0);" class="uploader" id="upload_ext"><i class="icon-ok"></i></a>
                                        <#if ad.extUrl?? && ad.extUrl?has_content>
                                            <a title="删除" href="javascript:void(0);" class="uploader" id="clear_ext"><i class="icon-trash"></i></a>
                                        </#if>
                                    </form>
                                </#if>
                            </td>
                        </tr>
                    </#if>
                </table>
            </div>
        </#if>
    </div>
</div>
<div id="log-dialog" class="modal fade hide" style="width: 60%; left: 30%; height:500px; overflow: scroll;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">广告操作历史</h4>
            </div>
            <div class="box-content">
                <table class="table table-condensed" style="height: 400px">
                    <thead>
                    <tr>
                        <th>操作</th>
                        <th>操作人</th>
                        <th>处理备注</th>
                        <th>操作时间</th>
                    </tr>
                    </thead>
                    <tbody id="logBody">
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script id="T:TimePicker" type="text/html">
    <tr class="period-tr">
        <td><input type="text" class="input input-small time-chooser" name="period-start"></td>
        <td><input type="text" class="input input-small time-chooser" name="period-end"></td>
        <td><a href="javascript:void(0);" class="del-period">删除</a></td>
    </tr>
</script>

<div class="modal fade" id="chooseTagTree" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true" style="display: none">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">选择标签</h4>
            </div>
            <div class="modal-body">
                <div id="tagTree" class="sampletree"
                     style="width:60%; height: 410px; float: left; "></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<script type="text/javascript">
    var tagTree =${tagTree!''};
    var markIds = ${markIds!'[]'};
    var displayMarkSlotId = "${displayMarkSlotId!''}";
    var isTest = "${isTest!''}";
    $(function () {
        initTip();

        $("textarea[class='notice-info']").each(function() {
            var temp = $(this).text().replace(/\r*\n/g,"<br/>").replace(/\s/g,"").replace(/<br\/>/g,"\n");
            $(this).html(temp.substring(0, temp.lastIndexOf("\n")));
        });

        //广告位绑定change事件
        $('#slotId').change(function(){
            if ($(this).val().indexOf("1") == 0) {
                $('#multiAccountSupportView').show();
            } else {
                $('#multiAccountSupportView').hide();
            }
            if($(this).val() == displayMarkSlotId){
                $('#div_marks').show();
            }else{
                $('#div_marks').hide();
            }
            initTip();
        });

        if($('#slotId').val().indexOf("1") == 0){
            $('#multiAccountSupportView').show();
        }

        if($('#slotId').val() == displayMarkSlotId){
            $('#div_marks').show();
        }

        $('#showTimeStart').datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $('#showTimeEnd').datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $('#frm').on('submit', function () {
            var adId = $('#adId').val();
            // 拼接时间
            var periods = [];
            $('.period-tr').each(function(){
                var $children = $(this).children();
                var start = $children.find("input[name='period-start']").val();
                var end = $children.find("input[name='period-end']").val();
                if (start != '' && end != '') {
                    start = start.replace(new RegExp(':'),'')+"00";
                    end = end.replace(new RegExp(':'),'')+"00";
                    var str = start + "#" + end;
                    periods.push(str);
                }
            });
//            alert(periods.join(","));
            $('#periods').val(periods.join(","));
            $('#frm').ajaxSubmit({
                type: 'post',
                url: 'saveaddetail.vpage',
                success: function (data) {
                    if (data.success) {
                        if (data.warning != '' && !confirm(data.warning)) {
                            return false;
                        }
                        alert("保存成功");
                        window.location.href = 'addetail.vpage?adId=' + data.id;
                    } else {
                        alert("保存失败:\n" + data.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_ad_btn').on('click', function () {
            var tree = $("#tagTree").fancytree("getTree");
            var ids = [];
            var texts = [];
            tree.getSelectedNodes().forEach(function (item) {
                ids.push(item.data.id);
                texts.push(item.data.name);

            });
            var adDetail = {
                adId: $('#adId').val(),
                name: $('#name').val().trim(),
                description: $('#description').val().trim(),
                content: $('#content').val().trim(),
                slotId: $('#slotId').find('option:selected').val(),
                resourceUrl: $('#resourceUrl').val().trim(),
                redirectWithUid: $('#redirectWithUid').is(":checked"),
                priority: $('#priority').find('option:selected').val(),
                showTimeStart: $('#showTimeStart').val().trim(),
                showTimeEnd: $('#showTimeEnd').val().trim(),
                imgFile: $('#imgFile').val(),
                isTop: $('#isTop').val(),
                isDefault: $('#isDefault').val(),
                viewQuota: $('#viewQuota').val(),
                clickQuota: $('#clickQuota').val(),
                userDailyShowLimit: $('#userDailyShowLimit').val(),
                showLimit: $('#showLimit').val(),
                clickLimit: $('#clickLimit').val(),
                markIds:ids.join(','),
                markText:texts.join(',')
            };
            //填充广告标签
            $('#hid_text_markIds').val(ids.join(','));
            $('#hid_text_markText').val(texts.join(','));

            if (!validateInput(adDetail)) {
                return false;
            }
            // 增加URL是不是https的判断
            if (adDetail.resourceUrl.indexOf("https://") < 0 && adDetail.resourceUrl.indexOf("17zuoye") >= 0) {
                if (!confirm("当前填写的跳转链接不是 https://，是否确认？")) {
                    return;
                }
            }
            if (confirm("是否确认保存？")) {
                $('#frm').submit();
            }
        });

        $('.time-chooser').datetimepicker({
            datepicker:false,
            format:'hh:ii',
            startView:1
        });

        // 删除功能
        $('#delete_ad_btn').on('click', function () {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var id = $('#adId').val();
            var status = $('#status').val();
            if (status == 1) {
                if (!confirm("广告已经上线，再次确认是否删除？")) {
                    return false;
                }
            }
            $.post('deladdetail.vpage', {
                adId: id
            }, function (data) {
                if (data.success) {
                    alert("删除成功！");
                } else {
                    alert("删除失败:" + data.info);
                }
                window.location.href = 'adindex.vpage';
            });
        });

        // 提交功能
        $("#submit_ad_btn").on('click', function () {
            if (!confirm("是否确认提交审核？")) {
                return false;
            }
            var id = $('#adId').val();
            $.post('submitad.vpage', {adId: id}, function (data) {
                if (data.success) {
                    alert("提交成功，请等待审核");
                    window.location.href = 'adindex.vpage';
                } else {
                    alert("提交审核失败:" + data.info);
                }
            });
        });

        // 快速测试提交功能
        $("#submit_fast_btn").on('click', function () {
            if (!confirm("是否确认快速提交？快速上线立马会看到广告效果，但不会经过【广告投放约束】来过滤，需要测试过滤的请等待定时缓存刷新")) {
                return false;
            }
            var id = $('#adId').val();
            $.post('submitFast.vpage', {adId: id}, function (data) {
                if (data.success) {
                    alert("提交上线成功");
                    window.location.href = 'adindex.vpage';
                } else {
                    alert(data.info);
                }
            });
        });

        // 上传、清除素材
        $("a[id^='upload_']").on('click', function () {
            var type = $(this).attr("id").substring("upload_".length);
            $("#form_" + type).submit();
        });

        $("form[id^='form_']").on('submit', function () {
            $(this).ajaxSubmit({
                type: 'post',
                url: 'uploadsrc.vpage',
                success: function (data) {
                    if (data.success) {
                        alert("素材上传成功！");
                    } else {
                        alert("素材上传失败:" + data.info);
                    }
                    window.location.reload();
                },
                error: function (msg) {
                    alert("素材上传失败！");
                }
            });
            return false;
        });

        $("a[id^='clear_']").on('click', function () {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var type = $(this).attr("id").substring("clear_".length);
            var adId = $("#ad_" + type).val();
            $.post('clearsrc.vpage', {adId: adId, type: type}, function (data) {
                if (!data.success) {
                    alert("清除素材失败:" + data.info);
                }
                window.location.reload();
            });
        });

        // 投放时间段
        $('#add_period_btn').on('click', function() {

            $('#periodTable').append(template("T:TimePicker", {}));

            $('.time-chooser').datetimepicker({
                datepicker:false,
                format:'hh:ii',
                startView:1
            });

            var cnt = $(".period-tr").length;
            if (cnt >= 5) {
//                alert("最多选择5个时间段");
                $(this).hide();
                return false;
            }
        });

        $('#tagTree').fancytree({
            extensions: [],
            source: tagTree,
            checkbox: true,
            selectMode: 2,
            select: function (event, data) {
                // 重算选中的ids
                var currentSelectedTags = [];
                data.tree.getSelectedNodes().forEach(function (item) {
                    currentSelectedTags.push(item.data.name);
                });
                $("#selectedtags").val(currentSelectedTags.join(","));
            }.bind(this)
        });
        // collapse all initially;
//        $("#tagTree").fancytree("getRootNode").visit(function (node) {
//            node.setExpanded(false);
//        });

        // 选中标签，展开父标签树
        var tree = $("#tagTree").fancytree("getTree");

        if(markIds.length>0 && markIds[0]){
            for (var i = 0; i < markIds.length; i++) {
                console.info(markIds[i]);
                var node = tree.getNodeByKey(parseInt(markIds[i]));
                node.setSelected(true);
                // expand all the parent
                var currentNode = node;
                while (true) {
                    currentNode = currentNode.getParent();
                    if (currentNode == null) {
                        break;
                    } else {
                        currentNode.setExpanded(true);
                    }
                }
            }
        }

    });

    function initTip () {
        var slotId = $('#slotId').find('option:selected').val();
        var tipMap = {
            "120201" : {"text" : "(对应消息内容)"} // 老师端PopUp消息
            ,"120401" : {"desc" : "(对应一级级标题)", "text" : "(对应二级标题)"}  // 老师微信福利活动
            ,"120111" : {"url" : "(SET_HOMEWORK:布置作业，CHECK_HOMEWORK:检查作业，CLAZZ_MANAGER:我的班级, 1.6.4以上版本支持)"}

            ,"220401" : {"text" : "(对应消息内容)"} // 家长端PopUp消息

            ,"320102" : {"name" : "(对应图片上方文字)"} // 学生APP作业完成未绑定家长通
            ,"320103" : {"name" : "(对应tips文字)"} // 学生APP-tips
            ,"320104" : {"name" : "(对应图片上方文字)"} // 学生APP未布置作业卡片
            ,"320105" : {"name" : "(对应图片上方文字)"} // 学生APP作业完成已绑定家长通
            ,"320201" : {"text" : "(对应消息内容)"} // 学生端PopUp消息
            ,"320501" : {"desc" : "(对应一级级标题)", "text" : "(对应二级标题)"} // 学生APP活动中心
        };
        var keys = ["name", "desc", "text", "url"];
        var tip = tipMap[slotId];
        for (var i = 0; i < keys.length; ++i) {
            if (tip == null || tip == 'undefined') {
                $('#tip-' + keys[i]).html('');
            } else {
                var info = tip[keys[i]];
                if (info != '' && info != 'undefined') {
                    $('#tip-' + keys[i]).html(info);
                } else {
                    $('#tip-' + keys[i]).html('');
                }
            }
        }
    }

    function validateInput(adDetail) {
        var msg = "";
        if (adDetail.name == '') {
            msg += "广告名称不能为空！\n";
        }
        if (adDetail.slotId == '') {
            msg += "请选择广告位！\n";
        }
        if (adDetail.priority == '') {
            msg += "请选择优先级！\n";
        }
        if (adDetail.showTimeStart == '' || adDetail.showTimeEnd == '') {
            msg += "请填写投放区间！\n";
        }
        if (adDetail.showTimeStart != '' && adDetail.showTimeEnd != '' && adDetail.showTimeStart > adDetail.showTimeEnd) {
            msg += "投放开始时间不能晚于结束时间！\n";
        }
        if (adDetail.resourceUrl != '' && adDetail.resourceUrl.indexOf('/mizar/microcourse/newgate.vpage') < 0) {
            if (new RegExp("^http://").test(adDetail.resourceUrl) && adDetail.resourceUrl.indexOf('17zuoye') > 0 && isTest !== "0") {
                msg += "跳转链接请认准 【https://】 !!!!\n";
            }
        }
        if (msg.length > 0) {
            alert(msg);
            return false;
        }
        return true;
    }

    function previewImg(file) {
        var prevDiv = $('#imgSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }

    function previewGif(file) {
        var prevDiv = $('#gifSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }

    function previewExt(file) {
        var prevDiv = $('#extSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }

    function showOpLog(adId) {
        $.post('adtracelog.vpage', {
            adId: adId
        }, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                $('#logBody').html('');
                for(var i=0; i<data.logs.length; i++){
                    var str = "<tr><td>"+data.logs[i].operation+"</td>";
                    str += "<td class=\"center\">"+data.logs[i].operator+"</td>";
                    str += "<td class=\"center\">"+data.logs[i].comment+"</td>";
                    str += "<td class=\"center\">"+data.logs[i].createtime+"</td></tr>";
                    $('#logBody').append(str);
                }
                $('#log-dialog').modal('show');
            }
        });
    }

    $(document).on('click', ".del-period", function () {
        var cnt = $(".period-tr").length;
        if (cnt >= 3) {
            $('#add_period_btn').show();
        }
        $(this).parent().parent().remove();

    });

</script>
</@layout_default.page>