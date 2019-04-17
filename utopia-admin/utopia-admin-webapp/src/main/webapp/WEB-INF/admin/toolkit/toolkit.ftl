<#import "../layout_default.ftl" as layout_default />
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<style type="text/css">
    .juan_upload {
        background-color: #fff;
        border-radius: 4px;
        border: 1px solid #e3e3e3;
        -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
        -moz-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
        box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
    }
</style>
<@layout_default.page page_title="阿娟工具箱" page_num=4>
<div class="span9" style="margin-top: 30px;">
    <!-- Nav tabs -->
    <ul class="nav nav-tabs" role="tablist" id="menuList">
        <li role="presentation" class="active"><a href="#aliyun" aria-controls="aliyun" role="tab" data-toggle="tab">上传图片到阿里云</a>
        </li>
        <li role="presentation"><a href="#learningZone" aria-controls="learningZone" role="tab" data-toggle="tab">自学空间赠送能量</a>
        </li>
        <li role="presentation"><a href="#fairyGrowth" aria-controls="fairyGrowth" role="tab" data-toggle="tab">自学空间精灵增加成长值</a>
        </li>
        <li role="presentation"><a href="#fireCracker" aria-controls="fireCracker" role="tab"
                                   data-toggle="tab">给学生发鞭炮</a></li>
        <li role="presentation"><a href="#nianBlood" aria-controls="nianBlood" role="tab" data-toggle="tab">给年兽加血</a>
        </li>
        <li role="presentation"><a href="#setVitality" aria-controls="setVitality" role="tab"
                                   data-toggle="tab">设置活力值</a></li>
        <li role="presentation"><a href="#setAttack" aria-controls="setAttack" role="tab" data-toggle="tab">设置攻击力</a>
        </li>
        <li role="presentation"><a href="#cleanupMobile" aria-controls="cleanupMobile" role="tab" data-toggle="tab">清除用户绑定手机号</a>
        </li>
        <li role="presentation"><a href="#cleanupEmail" aria-controls="cleanupEmail" role="tab"
                                   data-toggle="tab">清除绑定邮件</a></li>
        <li role="presentation"><a href="#mobileMessage" aria-controls="mobileMessage" role="tab"
                                   data-toggle="tab">查询短信</a></li>
        <li role="presentation"><a href="#userCache" aria-controls="userCache" role="tab" data-toggle="tab">查询用户缓存</a>
        </li>
        <li role="presentation"><a href="#afentiVitality" aria-controls="afentiVitality" role="tab" data-toggle="tab">设置活力</a>
        </li>
        <li role="presentation"><a href="#flush" aria-controls="flush" role="tab" data-toggle="tab">刷新CDN</a></li>
        <li role="presentation"><a href="#babelVitality" aria-controls="babelVitality" role="tab" data-toggle="tab">设置通天塔活力</a>
        </li>
        <li role="presentation"><a href="#parkourlVitality" aria-controls="parkourlVitality" role="tab"
                                   data-toggle="tab">设置沃克酷跑活力</a></li>
        <li role="presentation"><a href="#copyHw" aria-controls="copyHw" role="tab" data-toggle="tab">复制作业</a></li>

        <li role="presentation"><a href="#removeCache" aria-controls="removeCache" role="tab" data-toggle="tab">恢复老师操作，清理缓存</a></li>

        <li role="presentation"><a href="#assignHw" aria-controls="assignHw" role="tab" data-toggle="tab">布置作业</a></li>

        <li role="presentation"><a href="#directionalBook" aria-controls="directionalBook" role="tab" data-toggle="tab">清除缓存</a>
        </li>
        <li role="presentation"><a href="#loctionExam" aria-controls="loctionExam" role="tab"
                                   data-toggle="tab">设置定向考试</a></li>
        <li role="presentation"><a href="#registerExam" aria-controls="registerExam" role="tab"
                                   data-toggle="tab">批量报名</a></li>
        <li role="presentation"><a href="#unregisterExam" aria-controls="unregisterExam" role="tab" data-toggle="tab">批量取消报名</a>
        </li>
        <li role="presentation"><a href="#subAlbum" aria-controls="subAlbum" role="tab" data-toggle="tab">订阅专辑</a></li>
        <li role="presentation"><a href="#action" aria-controls="action" role="tab" data-toggle="tab">添加成长值/成就</a></li>
        <li role="presentation"><a href="#syncOrder" aria-controls="syncOrder" role="tab" data-toggle="tab">点读机(人教、外研)订单同步</a>
        </li>
        <li role="presentation"><a href="#refundOrder" aria-controls="refundOrder" role="tab" data-toggle="tab">点读机(人教)订单取消</a>
        </li>
        <li role="presentation"><a href="#delDev" aria-controls="delDev" role="tab" data-toggle="tab">点读机(人教)用户设备清理</a>
        </li>
        <li role="presentation"><a href="#changeHw" aria-controls="changeHw" role="tab" data-toggle="tab">作业延期</a></li>
        <li role="presentation"><a href="#assign" aria-controls="assign" role="tab" data-toggle="tab">自动布置假期作业</a></li>
        <li role="presentation"><a href="#parentReward" aria-controls="parentReward" role="tab" data-toggle="tab">手动发放完成作业家长端学豆奖励</a>
        </li>
        <li role="presentation"><a href="#assignSmallPaymentHW" aria-controls="assignSmallPaymentHW" role="tab"
                                   data-toggle="tab">小额支付作业生成</a></li>
        <li role="presentation"><a href="#listSmallPaymentHW" aria-controls="listSmallPaymentHW" role="tab"
                                   data-toggle="tab">小额支付作业信息</a></li>
        <li role="presentation"><a href="#repairCorrectHW" aria-controls="repairCorrectHW" role="tab" data-toggle="tab">修复自学作业</a>
        <li role="presentation"><a href="#resendDubbingSynthetic" aria-controls="resendDubbingSynthetic" role="tab" data-toggle="tab">重新发布合并趣配音视频的kafka消息</a>
        </li>

        <li role="presentation"><a href="#repairGrowthValueDIV" aria-controls="repairGrowthValueDIV" role="tab" data-toggle="tab">补成长值</a></li>

        <li role="presentation"><a href="#handleUserRemindDiv" aria-controls="handleUserRemindDiv" role="tab" data-toggle="tab">用户提醒</a></li>
        <li role="presentation"><a href="#deleteNationalDayHomework" aria-controls="deleteNationDayHomework" role="tab" data-toggle="tab">删除国庆假期作业</a></li>
        <li role="presentation"><a href="#manageDict" aria-controls="manageDict" role="tab" data-toggle="tab">字典表维护</a></li>

        <li role="presentation"><a id="blackWhite" href="#blackWhiteList" aria-controls="blackWhiteList" role="tab" data-toggle="tab" >小学作业黑白名单管理</a></li>
    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
        <!--小学作业黑白名单管理-->
        <div role="tabpanel" class="tab-pane" id="blackWhiteList">
            <fieldset>
                <legend>新增黑(白)名单</legend>
            </fieldset>
            <form action="/toolkit/homework/createHomeworkBlackWhiteList.vpage" method="post">
                <ul class="inline">
                    <li>
                        <label>业务类型：
                            <select name="businessType">
                                <option value='VACATION_DAY_PACKAGE'>假期作业</option>
                            </select>
                        </label>
                    </li>
                    <li>
                        <label>ID类型：
                            <select name="idType">
                                <option value='STUDENT_ID'>学生ID</option>
                                <option value='GROUP_ID'>班组ID</option>
                            </select>
                        </label>
                    </li>
                    <li>
                        <label>黑(白)名单ID：<input name="blackWhiteId" value="${blackWhiteId!}"/></label>
                    </li>
                    <li>
                        <button type="submit">提交</button>
                    </li>
                </ul>
            </form>
            <fieldset>
                <legend>黑(白)名单列表:</legend>
            </fieldset>
            <ul class="inline">
                <li>
                    <label>
                        <select name="businessType" id="select_business_type">
                            <option value='VACATION_DAY_PACKAGE'>假期作业</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>
                        <select name="idType" id="select_id_type">
                            <option value='STUDENT_ID'>学生ID</option>
                            <option value='GROUP_ID'>班组ID</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>黑(白)名单ID：<input id="input_black_white_id"/></label>
                </li>
                <li>
                    <button id="query_black_white_list" type="submit">查询</button>
                </li>
            </ul>
            <table class="table table-hover table-striped table-bordered" id="table_black_white">
                </table>
            <ul class="inline">
                <li>
                    <a id='first_page' href="javascript:void(0)">首页</a>
                </li>
                <li>
                    <a id='pre_page' href="javascript:void(0)">上一页</a>
                </li>
                <li>
                    <a id='next_page' href="javascript:void(0)">下一页</a>
                </li>
                <li>
                    <a id='last_page' href="javascript:void(0)">末页</a>
                </li>
            </ul>
            </iframe>
        </div>

        <!--字典表维护-->
        <div role="tabpanel" class="tab-pane" id="manageDict">
            <li><a href="${requestContext.webAppContextPath}/toolkit/homework/fetchHomeworkDictList.vpage">VOX_HOMEWORK_DICT</a></li>
            <li><a href="${requestContext.webAppContextPath}/toolkit/homework/fetchHomeworkStudentAuthDictList.vpage">VOX_HOMEWORK_STUDENT_AUTH_DICT</a></li>
        </div>
        <!--上传图片至阿里云-->
        <div role="tabpanel" class="tab-pane active" id="aliyun">
            <div class="control-group juan_upload">
                <div class="controls">
                    <a class="btn btn-info" href="javascript:void(0);" id="juan_upload_btn" style="margin-bottom: 5px;">
                        <i class="icon-picture icon-white"></i> 上传图片至阿里云
                    </a>
                </div>
                <div id="photoPath">
                    上传成功后，图片的全路径将会显示在这里，请牢记！
                </div>
            </div>
        </div>

        <!--清除用户绑定手机号-->
        <div role="tabpanel" class="tab-pane" id="cleanupMobile">
            <form id="cleanupBindedMobile" name="cleanupBindedMobile"
                  action="${requestContext.webAppContextPath}/toolkit/user/cleanupBindedMobile.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>清除用户绑定手机号</legend>
                    <div class="control-group">
                        <label class="control-label" for="cleanupBindedMobile_mobile">手机号</label>
                        <div class="controls">
                            <input type="text" name="mobile" value="" id="cleanupBindedMobile_mobile" class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="cleanupBindedMobile_mobile">角色</label>
                        <div class="controls">
                            <input type="checkbox" checked="checked" name="cleanup_role" value="teacher"
                                   id="cleanupBindedMobile_teacher" class="checkbox"/>老师
                            <input type="checkbox" checked="checked" name="cleanup_role" value="student"
                                   id="cleanupBindedMobile_student" class="checkbox"/>学生
                            <input type="checkbox" checked="checked" name="cleanup_role" value="parent"
                                   id="cleanupBindedMobile_parent" class="checkbox"/>家长
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="cleanupBindedMobile_reason">原因</label>
                        <div class="controls">
                            <textarea name="reason"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="cleanupBindedMobile_0" value="清除绑定手机号" class="btn btn-primary"/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--清除绑定邮件-->
        <div role="tabpanel" class="tab-pane" id="cleanupEmail">
            <form id="cleanupBindedEmail" name="cleanupBindedEmail"
                  action="${requestContext.webAppContextPath}/toolkit/user/cleanupBindedEmail.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>清除绑定邮件</legend>
                    <div class="control-group">
                        <label class="control-label" for="cleanupBindedEmail_email">邮件</label>
                        <div class="controls">
                            <input type="text" name="email" value="" id="cleanupBindedEmail_email" class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="cleanupBindedEmail_0" value="清除绑定邮件" class="btn btn-primary"/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--查询短信-->
        <div role="tabpanel" class="tab-pane" id="mobileMessage">
            <form id="queryMobilMessage" name="queryMobilMessage"
                  action="${requestContext.webAppContextPath}/toolkit/user/findMobileMessage.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>查询短信</legend>
                    <div class="control-group">
                        <label class="control-label" for="queryMobilMessage_mobile">手机号</label>
                        <div class="controls">
                            <input type="text" name="mobile" value="" id="queryMobilMessage_mobile" class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="queryMobilMessage_0" value="查询短信" class="btn btn-primary"/>
                        </div>
                    </div>
                </fieldset>
            </form>
            <#if smsMessageList??>
                <table style="border-width: 2px;" class="table table-bordered">
                    <thead>
                    <tr>
                        <th colspan="2">手机号：<span style="color:dodgerblue;">${queryMobilMessage_mobile!}</span></th>
                    </tr>
                    <tr>
                        <th style="width:100px;">创建时间</th>
                        <th>短信类型</th>
                        <th width="30%">短信内容</th>
                        <th>状态</th>
                        <th>错误原因</th>
                        <th>发送通道</th>
                    </tr>
                    </thead>
                <tbody>
                    <#if smsMessageList?size gt 0>
                        <#list smsMessageList as SMSMessage>
                        <tr>
                            <td>${(SMSMessage.createTime)?string("yyyy-MM-dd HH:mm:ss")?replace(" ", "<br/>")}</td>
                            <td>
                            ${(SMSMessage.smsType.getDescription())!'--'}
                                <br/>${(SMSMessage.smsType.name())!'--'}
                            </td>
                            <td>
                            ${(SMSMessage.smsContent)!?html}
                            </td>
                            <td>
                                <#if (SMSMessage.status)??>
                                    <#if SMSMessage.status == '1'>
                                        提交成功
                                    <#elseif SMSMessage.status == '2'>
                                        发送成功
                                    <#else>
                                        发送失败
                                    </#if>
                                <#else>
                                    --
                                </#if>
                                <#if (SMSMessage.verification)?? && (SMSMessage.verification)>
                                    <br/> 是否消费: ${(SMSMessage.consumed)?string("是","否")}
                                <#else>
                                    <br/>送达时间：${(SMSMessage.receiveTime)!'--'}
                                </#if>
                            </td>
                            <td>${(SMSMessage.errorDesc)!'--'}(${(SMSMessage.errorCode)!'--'})</td>
                            <td>${(SMSMessage.smsChannel)!'--'}</td>
                        </tr>
                        </#list>
                    </tbody>
                    <#else>
                        <tbody>
                        <tr>
                            <td colspan="6" style="text-align: center;">未查询出符合条件的数据</td>
                        </tr>
                        </tbody>
                    </#if>
                </table>
            </#if>
        </div>
        <!--查询用户缓存-->
        <div role="tabpanel" class="tab-pane" id="userCache">
            <form id="queryUserCache" name="queryUserCache"
                  action="${requestContext.webAppContextPath}/toolkit/user/findUserCache.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>查询用户缓存</legend>
                    <div class="control-group">
                        <label class="control-label" for="queryUserCache_userId">用户ID</label>
                        <div class="controls">
                            <input type="text" name="userId" value="" id="queryUserCache_userId" class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="queryUserCache_0" value="查询用户缓存" class="btn btn-primary"/>
                        </div>
                    </div>
                </fieldset>
            </form>
            <#if userCache??>
            ${userCache!''}
            </#if>
        </div>
        <!--设置活力-->
        <div role="tabpanel" class="tab-pane" id="afentiVitality">
            <form id="setAfentiBasicVitality" name="setAfentiBasicVitality"
                  action="${requestContext.webAppContextPath}/toolkit/afenti/basic/setVitality.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>设置活力</legend>
                    <div class="control-group">
                        <label class="control-label" for="setAfentiBasicVitality">用户ID</label>
                        <div class="controls">
                            <input type="text" name="userIds" value="" id="setAfentiBasicVitality_userIds"
                                   class="input"/>
                        </div>
                        <label class="control-label" for="setAfentiBasicVitality">活力值</label>
                        <div class="controls">
                            <input type="text" name="vitality" value="" id="setAfentiBasicVitality_vitality"
                                   class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="setAfentiBasicVitality" value="设置活力" class="btn btn-primary"/>
                        </div>
                        <div class="controls">
                            添加冒险岛最后一次签到活力值。</br>
                            用户ID:把需要复制的用户ID以逗号隔开</br>
                            活力个数：正数/负数
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--刷新CDN-->
        <div role="tabpanel" class="tab-pane" id="flush">
            <form id="flushcdnform" name="flushcdn" action="${requestContext.webAppContextPath}/toolkit/cdn/flush.vpage"
                  method="post" class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>刷新CDN</legend>
                    <div class="control-group">
                        <label class="control-label" for="flushCdn_url">URL</label>
                        <div class="controls">
                            <textarea id="flushCdn_url" name="flushCdn_url"
                                      style="width: 400px; height: 150px;"></textarea>
                        </div>
                        <div class="controls">
                            每个URL一行，以http://开头,例如：//cdn.17zuoye.com/examgfs/exam-preview-MATH-5369e903a3103e54b4cb99e9
                            注意：推送的URL需要区分大小写
                        </div>
                        <label class="control-label" for="flushCdn_dir">目录</label>
                        <div class="controls">
                            <textarea id="flushCdn_dir" name="flushCdn_dir"
                                      style="width: 400px; height: 150px;"></textarea>
                        </div>
                        <div class="controls">
                            每个目录一行，以http://开头,例如：http://cdn.17zuoye.com/examgfs/
                            注意：目录更新可能对服务造成重大影响，请慎用!
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input id="flushcdn" type="button" value="刷新CDN" class="btn btn-primary"/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--设置通天塔活力-->
        <div role="tabpanel" class="tab-pane" id="babelVitality">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/babel/setVitality.vpage" name="setBabelVitality" id="setBabelVitality">
                <fieldset>
                    <legend>设置通天塔活力</legend>
                    <div class="control-group">
                        <label for="setBabelVitality" class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" id="setBabelVitality_userIds" value="" name="userIds">
                        </div>
                        <label for="setBabelVitality" class="control-label">通天塔活力值</label>
                        <div class="controls">
                            <input type="text" class="input" id="setBabelVitality_vitality" value="" name="vitality">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="设置通天塔活力" id="setBabelVitality">
                        </div>
                        <div class="controls">
                            通天塔活力值。<br>
                            用户ID:把需要复制的用户ID以逗号隔开<br>
                            正数：增加活力（每次最多5点）。负数：减少活力（绝对值不可大于当前活力）。
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--设置沃克酷跑活力-->
        <div role="tabpanel" class="tab-pane" id="parkourlVitality">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/nekketsu/parkour/setVitality.vpage" name="setParkourlVitality"
                  id="setParkourlVitality">
                <fieldset>
                    <legend>设置沃克酷跑活力</legend>
                    <div class="control-group">
                        <label for="setParkourlVitality" class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" id="setParkourlVitality_userIds" value="" name="userIds">
                        </div>
                        <label for="setParkourlVitality" class="control-label">沃克酷跑活力值</label>
                        <div class="controls">
                            <input type="text" class="input" id="setParkourlVitality_vitality" value="" name="vitality">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="设置沃克酷跑活力" id="setParkourlVitality">
                        </div>
                        <div class="controls">
                            沃克酷跑活力值。<br>
                            用户ID:把需要复制的用户ID以逗号隔开<br>
                            活力个数：非负整数 <= 5
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>

        <div role="tabpanel" class="tab-pane" id="removeCache">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/removeCache.vpage" name="removeCache" id="removeCache">
                <fieldset>
                    <legend>恢复老师限制</legend>
                    <div class="control-group">
                        <label for="removeCache" class="control-label">老师ID</label>
                        <div class="controls">
                            <input type="text" class="input" value="" name="tid">
                        </div>
                        <label for="copyHomework" class="control-label">方法名字</label>
                        <div class="controls">
                            <input type="text" class="input" value="" name="methodName">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="恢复老师限制" id="removeCache">
                        </div>
                        <div class="controls">
                            参数<br>
                            老师ID<br>
                            方法名字
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>

        <!--复制作业-->
        <div role="tabpanel" class="tab-pane" id="copyHw">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/newcopyhomework.vpage" name="copyHomework" id="copyHomework">
                <fieldset>
                    <legend>复制作业</legend>
                    <div class="control-group">
                        <label for="copyHomework" class="control-label">作业ID</label>
                        <div class="controls">
                            <input type="text" class="input" id="copyHomework_homeworkId" value="" name="homeworkId">
                        </div>
                        <label for="copyHomework" class="control-label">组ID</label>
                        <div class="controls">
                            <input type="text" class="input" id="copyHomework_groupId" value="" name="groupId">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="复制作业" id="copyHomework">
                        </div>
                        <div class="controls">
                            把已布置的作业复制到目标组中，<br>
                            如果目标组中有作业，先将作业删除，然后布置，<br>
                            支持批量组处理，组格式逗号隔开，<br>
                            不要多于50个组的批处理，会影响性能
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--布置作业-->
        <div role="tabpanel" class="tab-pane" id="assignHw">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/userdefinedassign.vpage" name="userDefinedAssign" id="userDefinedAssign">
                <fieldset>
                    <div class="control-group">
                        <label for="userDefinedAssign" class="control-label">试题ID</label>
                        <div class="controls">
                            <textarea id="question_ids" name="qids" style="width: 400px; height: 150px;"></textarea>
                        </div>
                        <label for="userDefinedAssign" class="control-label">组ID</label>
                        <div class="controls">
                            <input type="text" class="input" id="clazz_group_Id" value="" name="cgid">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="布置作业" id="assignHomework">
                        </div>
                        <div class="controls">
                            把选中的试题布置到目标组中<br>
                            如果目标组中有作业，先将作业检查，然后布置<br>
                            <span style="color: red">禁止使用此功能给正常老师布置作业</span>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--清除缓存-->
        <div role="tabpanel" class="tab-pane" id="directionalBook">
            <form id="directionalNewExam" name="directionalNewExam"
                  action="${requestContext.webAppContextPath}/crm/vacation/homework/report/remove/cachebookinfo.vpage"
                  method="get"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>清除缓存</legend>
                    <div class="control-group">
                        <label class="control-label" for="directionalBookIDs">keys</label>
                        <div class="controls">
                            <input type="text" name="strKeys" value="" id="directionalNewExam_strBookIds"
                                   class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="directionalBookIds" value="清除缓存" class="btn btn-primary"/>
                        </div>
                        <div class="controls">
                            清除缓存。</br>
                            缓存key:逗号隔开
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <div role="tabpanel" class="tab-pane" id="loctionExam">
            <form id="loctionNewExam" name="loctionNewExam"
                  action="${requestContext.webAppContextPath}/toolkit/newexam/change/directional.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>设置定向考试</legend>
                    <div class="control-group">
                        <label class="control-label" for="loctionNewExam">模考ID</label>
                        <div class="controls">
                            <input type="text" name="newExamId" value="" id="loctionNewExam_newExamId" class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="loctionNewExam" value="设置定向考试" class="btn btn-primary"/>
                        </div>
                        <div class="controls">
                            设置定向考试。</br>
                            模考ID
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>


        <!--批量报名-->
        <div role="tabpanel" class="tab-pane" id="registerExam">
            <form id="registerNewExam" name="registerNewExam"
                  action="${requestContext.webAppContextPath}/toolkit/newexam/register.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>批量报名</legend>
                    <div class="control-group">
                        <label class="control-label" for="registerNewExam">模考ID</label>
                        <div class="controls">
                            <input type="text" name="newExamId" value="" id="registerNewExam_newExamId" class="input"/>
                        </div>
                        <label class="control-label" for="registerNewExam">学生ID</label>
                        <div class="controls">
                            <input type="text" name="userIds" value="" id="registerNewExam_userIds" class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="registerNewExam" value="批量报名" class="btn btn-primary"/>
                        </div>
                        <div class="controls">
                            批量报名。</br>
                            考试ID:模考ID</br>
                            报名学生ID:把需要报名的学生ID以逗号隔开
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--批量取消报名-->
        <div role="tabpanel" class="tab-pane" id="unregisterExam">
            <form id="unregisterNewExam" name="unregisterNewExam"
                  action="${requestContext.webAppContextPath}/toolkit/newexam/unregister.vpage" method="post"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>批量取消报名</legend>
                    <div class="control-group">
                        <label class="control-label" for="unregisterNewExam">模考ID</label>
                        <div class="controls">
                            <input type="text" name="newExamId" value="" id="unregisterNewExam_newExamId"
                                   class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="unregisterNewExam">学生ID</label>
                        <div class="controls">
                            <input type="text" name="userIds" value="" id="unregisterNewExam_userIds" class="input"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="unregisterNewExam" value="批量取消报名" class="btn btn-primary"/>
                        </div>
                        <div class="controls">
                            批量取消报名。</br>
                            考试ID:模考ID</br>
                            报名学生ID:把需要报名的学生ID以逗号隔开
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--订阅专辑-->
        <div role="tabpanel" class="tab-pane" id="subAlbum">
            <div style="background-color:#fff;" class="well form-horizontal">
                <fieldset>
                    <legend>订阅专辑</legend>
                    <div class="control-group">
                        <label class="control-label">专辑ID</label>
                        <div class="controls">
                            <input type="text" id="txt_subscribe_albumid" placeholder="专辑ID"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">家长ID</label>
                        <div class="controls">
                            <textarea id="txt_subscribe_userids" placeholder="家长ID,一行一个"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="btn_subscribe_submit" value="提交"/>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div>
        <!--添加成长值/成就-->
        <div role="tabpanel" class="tab-pane" id="action">
            <div style="background-color: #fff;" class="well form-horizontal">
                <fieldset>
                    <legend>添加成长值/成就</legend>
                    <div class="control-group">
                        <label for="finishHomework" class="control-label">操作类型</label>
                        <div class="controls">
                            <select id="actionType">
                                <option value="">请选择操作类型</option>
                                <option value="FinishHomework">完成作业</option>
                                <option value="FinishSelfLearning">完成自学</option>
                                <option value="FinishOral">完成口语作业</option>
                                <option value="SubmitAnswer">提交自学答案</option>
                                <option value="SaveSelfStudyChineseTextRead">完成语文课文朗读</option>
                                <option value="StartSelfStudyEnglishWalkman">完成英语随身听</option>
                                <option value="ClickSelfStudyEnglishPicListen">使用英语点读机</option>
                                <option value="LookHomeworkReport">查看作业报告</option>
                            </select>
                        </div>
                    </div>
                    <div id="finish_homework" style="display:none;" class="control-group">
                        <label for="finishHomework" class="control-label">学科</label>
                        <div class="controls">
                            <select id="fh_subject">
                                <option value="">请选择学科</option>
                                <option value="ENGLISH">英语</option>
                                <option value="MATH">数学</option>
                                <option value="CHINESE">语文</option>
                            </select>
                        </div>
                        <label for="finishHomework" class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="用户ID" id="fh_userid">
                        </div>
                        <label for="finishHomework" class="control-label">分数</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="分数" id="fh_score">
                        </div>
                        <label for="finishHomework" class="control-label">完成时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20160902 20:12:00" id="fh_finish">
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="fh_submit">
                        </div>
                    </div>
                    <div id="finish_selflearning" style="display:none;" class="control-group">
                        <label for="finishSelfLearning" class="control-label">帐号</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="帐号" id="fs_userid"/>
                        </div>
                        <label for="finishSelfLearning" class="control-label">完成时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20160902 20:12:00" id="fs_finish"/>
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="fs_submit">
                        </div>
                    </div>
                    <div id="finish_oral" style="display:none;" class="control-group">
                        <label for="finishOrder" class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="用户ID" id="fo_uid"/>
                        </div>
                        <label for="finishOral" class="control-label">作业ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="作业ID" id="fo_hid"/>
                        </div>
                        <label for="finishOral" class="control-label">完成时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20160902 20:12:00" id="fo_finish"/>
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="fo_submit"/>
                        </div>
                    </div>
                    <div id="submit_answer" style="display:none;" class="control-group">
                        <label for="submitAnswer" class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="用户ID" id="sba_uid"/>
                        </div>
                        <label for="submitAnswer" class="control-label">时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20160902 20:26:00" id="sba_finish"/>
                        </div>
                        <label for="submitAnswer" class="control-label">类型</label>
                        <div class="controls">
                            <select id="sba_type">
                                <option value="">请选择类型</option>
                                <option value="SubmitZoumeiAnswer">走美</option>
                                <option value="SubmitAfentiEnglishAnswer">阿分题英语</option>
                                <option value="SubmitAfentiMathAnswer">阿分题数学</option>
                                <option value="SubmitAfentiChineseAnswer">阿分题语文</option>
                            </select>
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="sba_submit"/>
                        </div>
                    </div>
                    <div id="chinese_text_read" style="display:none;" class="control-group">
                        <label class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="用户ID" id="ctr_uid"/>
                        </div>
                        <label class="control-label">时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20160902 20:26:00" id="ctr_finish"/>
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="ctr_submit"/>
                        </div>
                    </div>
                    <div id="english_walk_man" style="display:none;" class="control-group">
                        <label class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="用户ID" id="ewm_uid"/>
                        </div>
                        <label class="control-label">时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20160902 20:26:00" id="ewm_finish"/>
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="ewm_submit"/>
                        </div>
                    </div>
                    <div id="piclisten" style="display:none;" class="control-group">
                        <label class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="用户ID" id="pl_uid"/>
                        </div>
                        <label class="control-label">时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20160902 20:26:00" id="pl_finish"/>
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="pl_submit"/>
                        </div>
                    </div>
                    <div id="lookhomeworkreport" style="display:none;" class="control-group">
                        <lable class="control-label">学科</lable>
                        <div class="controls">
                            <select id="lhr_select">
                                <option value="">请选择学科</option>
                                <option value="ENGLISH">英语</option>
                                <option value="CHINESE">语文</option>
                                <option value="MATH">数学</option>
                            </select>
                        </div>
                        <label class="control-label">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="用户ID" id="lhr_uid"/>
                        </div>
                        <label class="control-label">时间</label>
                        <div class="controls">
                            <input type="text" class="input" placeholder="20170320 10:10:10" id="lhr_finish"/>
                        </div>
                        <div class="controls">
                            <input type="button" class="btn btn-primary" value="提交" id="lhr_submit"/>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div>
        <!--外研社订单同步-->
        <div role="tabpanel" class="tab-pane" id="syncOrder">
            <div style="background-color: #fff;" class="well form-horizontal">
                <fieldset>
                    <legend>点读机订单同步</legend>
                    <div class="control-group">
                        <label for="synOrder" class="control-lable">订单号:</label>
                        <div class="controls">
                            <textarea id="txt_syn_order_id" placeholder="人教／外研社订单号，每行一个"
                                      style="width:200px;height:300px;"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="btn_sync_order_submit" value="提交"/>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div>
        <!--人教点读机用户设备记录清理-->
        <div role="tabpanel" class="tab-pane" id="delDev">
            <div style="background-color: #fff;" class="well form-horizontal">
                <fieldset>
                    <legend>人教点读机设备记录清理</legend>
                    <div class="control-group">
                        <label class="control-label">用户ID</label>
                        <div class="controls">
                            <input class="form-control" type="text" name="user_id" id="user_id" value=""
                                   placeholder="用户ID"/>
                            <input class="btn btn-primary" type="button" id="query_submit" value="查询"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <table class="table table-striped table-bordered">
                                <thead>
                                <tr>
                                    <td>操作</td>
                                    <td>设备id</td>
                                    <td>设备名</td>
                                    <td>创建时间</td>
                                </tr>
                                </thead>
                                <tbody id="devInfoList" style="font-size: 13px;">

                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input class="btn btn-danger" type="button" id="del_submit" value="删除"/>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div>
        <!-- 点读机订单取消-->
        <div role="tabpanel" class="tab-pane" id="refundOrder">
            <div style="background-color: #fff;" class="well form-horizontal">
                <fieldset>
                    <legend>点读机订单取消</legend>
                    <div class="control-group">
                        <label for="refundOrder" class="control-lable">订单号:</label>
                        <div class="controls">
                            <textarea id="txt_refund_order_id" placeholder="人教订单号，每行一个"
                                      style="width:200px;height:300px;"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="btn_refund_order_submit" value="提交"/>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div
                <!--作业延期-->
        <div role="tabpanel" class="tab-pane" id="changeHw">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/changehomeworkendtime.vpage" name="changeHomeworkEndTime"
                  id="changeHomeworkEndTime">
                <fieldset>
                    <legend>作业延期</legend>
                    <div class="control-group">
                        <label for="userDefinedAssign" class="control-label">作业结束时间 </label>
                        <div class="controls">
                            <input name="startDate" value="" type="text" placeholder="格式：2016-11-08 18:59:00"/> -
                            <input name="endDate" value="" type="text" placeholder="格式：2016-11-08 18:59:00"/>
                        </div>
                        <label for="userDefinedAssign" class="control-label">延后到 </label>
                        <div class="controls">
                            <input type="text" name="endTime" value="" placeholder="格式：2016-11-08 18:59:00"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="提交" id="changeHomeworkEndTime">
                        </div>
                        <div class="controls">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--自动布置假期作业-->
        <div role="tabpanel" class="tab-pane" id="assign">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/vacation/autoassign.vpage" target="_blank">
                <fieldset>
                    <legend>自动布置假期作业</legend>
                    <div class="control-group">
                        <label class="control-label">老师id列表</label>
                        <div class="controls">
                            <textarea name="teacherIds" placeholder="老师ID,一行一个,一次最多50个"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="自动布置" id="autoAssign">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--删除国庆假期作业-->
        <div role="tabpanel" class="tab-pane" id="deleteNationalDayHomework">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/nationalday/delete.vpage" target="_blank">
                <fieldset>
                    <legend>删除国庆假期作业</legend>
                    <div class="control-group">
                        <label class="control-label">老师ID</label>
                        <div class="controls">
                            <input name="teacherId" id="teacherId" value="" placeholder="老师ID">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="确认删除" id="deleteNationDayHomework">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--手动发放完成作业家长端学豆奖励-->
        <div role="tabpanel" class="tab-pane" id="parentReward">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/addrewardinparentapp.vpage" target="_blank">
                <fieldset>
                    <legend>手动发放完成作业家长端学豆奖励</legend>
                    <div class="control-group">
                        <label class="control-label">学生ID</label>
                        <div class="controls">
                            <input name="studentId" id="studentId" value="" placeholder="学生ID">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">作业ID</label>
                        <div class="controls">
                            <input name="homeworkId" id="homeworkId" value="" placeholder="作业ID">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">奖励数量</label>
                        <div class="controls">
                            <input name="count" id="count" value="" placeholder="奖励数量,1-5的数字">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="发放奖励" id="addReward">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <!--生成小额支付作业-->
        <div role="tabpanel" class="tab-pane" id="assignSmallPaymentHW">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/batchsmallpayment.vpage" name="batchSmallPaymentAssign"
                  id="batchSmallPaymentAssign">
                <legend>批量生成小额支付作业</legend>
                <fieldset>
                    <div class="control-group">
                        <label for="smallPaymentAssign" class="control-label">所在周的任意一天时间：</label>
                        <div class="controls">
                            <input type="text" class="input" id="weekDate2" value="" name="weekDate"
                                   placeholder="格式：2017-04-24"/>
                        </div>
                        <br/>
                        <label></label>
                        <div class="controls">
                            <select name="homeworkTag">
                                <option value="Platinum">白金任务</option>
                            </select>
                        </div>
                        <label for="batchSmallPaymentAssign" class="control-label">内容</label>
                        <div class="controls">
                            <textarea id="assignContent" name="assignContent"
                                      style="width: 800px; height: 300px;"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="批量布置" id="batchSmallPaymentAssign">
                        </div>
                        <div class="controls">
                            格式：<br/>
                            CHINESE|5|Q_10100879612777,Q_10100879604904,Q_10100879573929<br/>
                            CHINESE|6|Q_10100737295268,Q_10100737425783,Q_10100737512456<br/>
                            MATH|1|Q_10200927935164,Q_10200768961233,Q_10205994217337<br/>
                            MATH|2|Q_10206470980009,Q_10200354574234,Q_10200738394517<br/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>

        <!-- 小额支付的作业列表 -->
        <div role="tabpanel" class="tab-pane" id="listSmallPaymentHW">
            <div style="background-color: #fff;" class="well form-horizontal">
                <legend>小额支付的作业列表</legend>
                <div class="control-group">
                    <ul class="inline">
                        <li>
                            <label for="provinces">
                                所在省：
                                <select id="provinces" name="provinces" class="multiple district_select"
                                        next_level="citys">
                                    <option value="-1">全国</option>
                                    <#if provinces??>
                                        <#list provinces as p>
                                            <option value="${p.key}">${p.value}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label for="citys">
                                所在市：
                                <select id="citys" data-init='false' name="citys" class="multiple district_select"
                                        next_level="countys">
                                    <option value="-1">全部</option>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label for="countys">
                                所在区：
                                <select id="countys" data-init='false' name="countys" class="multiple district_select">
                                    <option value="-1">全部</option>
                                </select>
                            </label>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <label for="listSmallPaymentHW">
                                时间：
                                <input type="text" class="input" id="weekDate1" value="" name="listDay"
                                       placeholder="格式：20170727"/>
                            </label>
                        </li>
                    </ul>
                    <div class="controls">
                        <a type="button" class="btn btn-primary" id="listsmallpayment">查询作业</a>
                    </div>
                </div>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <td>作业id</td>
                        <td>周数</td>
                        <td>学科</td>
                        <td>年级</td>
                        <td>题目信息</td>
                        <td>所属周</td>
                        <td>创建时间</td>
                    </tr>
                    </thead>
                    <tbody id="smallPaymentContext" style="font-size: 13px;">

                    </tbody>
                </table>
            </div>
        </div>

        <!-- 修复自学作业 -->
        <div role="tabpanel" class="tab-pane" id="repairCorrectHW">
            <form style="background-color: #fff;" class="well form-horizontal" method="post"
                  action="/toolkit/homework/repairselfstudycorrecthomework.vpage" name="repairCorrectHomework"
                  id="repairCorrectHomework">
                <legend>修复自学作业</legend>
                <fieldset>
                    <div class="control-group">
                        <label for="repairCorrectHomework" class="control-label">作业ID</label>
                        <div class="controls">
                            <input style="width: 300px" type="text" class="input" id="RCH_homework_id" value=""
                                   name="homeworkId">
                        </div>
                        <br/>
                        <label for="repairCorrectHomework" class="control-label">学生ID</label>
                        <div class="controls">
                            <input type="text" class="input" id="RCH_student_id" value="" name="studentId">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" class="btn btn-primary" value="修复订正作业">
                        </div>
                        <div class="controls">
                            用于本该生成自学作业，但是由于某些原因并没有生成<br>
                            <span style="color: red">请先确认是否真的没有生成订正作业</span>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>

        <!--重新发布合并趣配音视频的kafka消息-->
        <div role="tabpanel" class="tab-pane" id="resendDubbingSynthetic">
            <form id="resendDubbingSynthetic" name="resendDubbingSynthetic"
                  action="${requestContext.webAppContextPath}/toolkit/homework/resenddubbingsynthetic.vpage"
                  method="get"
                  class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <legend>重新发布合并趣配音视频的kafka消息</legend>
                    <div class="control-group">
                        <label class="control-label" for="resendDubbingSynthet">ids</label>
                        <div class="controls">
                            <textarea type="text" name="ids" value="" id="resendDubbingSynthetic_ids" style="width: 400px; height: 150px;"></textarea>
                    </div>

                    <div class="control-group">
                        <input type="submit" id="resendDubbingSynthetic" value="重新发送" class="btn btn-primary"/>
                    </div>
                    <div class="control-group">
                        id:逗号隔开（id=homeworkId + "-" + userId + "-" + dubbingId）
                    </div>

                </fieldset>
            </form>
        </div>

        <div role="tabpanel" class="tab-pane" id="repairGrowthValueDIV"  >
           <div class="well form-horizontal" style="background-color: #fff;">

               <fieldset>
                   <legend>补成长值</legend>
                   <div class="control-group">
                       <label for="repairGrowthValueLabel" class="control-label">操作类型</label>
                       <div class="controls">
                           <select id="repairGrowthSelector">
                               <option value="studentFinishHomeworkDIV">学生完成语数英作业</option>
                               <option value="studentRepairHomeworkDIV">学生补做语数英作业</option>
                               <option value="studentGrowthWorldAthleticsDIV">学生完成成长世界竞技岛挑战</option>
                               <option value="studentGrowthWorldSubjectDIV">学生完成成长世界语数英智慧岛任务</option>
                               <option value="studentGoldenMissionDIV">学生完成黄金任务</option>
                               <option value="studentPlatinumMissionDIV">学生完成白金任务</option>
                               <option value="studentFightBossDIV">学生参与打败坏习惯</option>
                               <option value="parentLoginDIV">家长登陆</option>
                               <option value="parentViewHomeworkReportDIV">家长查看作业报告</option>
                               <option value="parentViewSelfStudyReportDIV">家长查看自学报告</option>
                               <option value="parentSendFlowerToTeacherDIV">家长给老师送鲜花</option>
                               <option value="parentSendRewardDIV">家长发奖励</option>
                               <option value="supplementParentInfoDIV">家长首次补全信息</option>
                               <option value="parentPaymentDIV">家长给孩子开通产品</option>
                               <option value="clearActivationCacheDIV">清理活跃值缓存-生产环境</option>
                           </select>
                       </div>
                   </div>
                   <!--完成作业-->
                   <div role="tabpanel" class="tab-pane" style="display: block;" id="studentFinishHomeworkDIV">
                       <form id="studentFinishHomework" name="studentFinishHomework"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/studentFinishHomework.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>完成语数英作业补成长值</legend>
                               <div class="control-group">
                                   <label for="studentFinishHomework" class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">学科</label>
                                   <div class="controls">
                                       <input type="radio" name="subject" checked="checked" value="CHINESE" class="input"/>语文
                                       <input type="radio" name="subject" value="MATH" class="input"/>数学
                                       <input type="radio" name="subject" value="ENGLISH" class="input"/>英语
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label for="studentFinishHomework" class="control-label">作业Id</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="homeworkId">
                                   </div>
                               </div>

                               <div class="control-group">
                                   <input type="submit" id="sendStudentFinishHomeworkBTN" value="操作" class="btn btn-primary"/>
                               </div>
                       </form>
                   </div>
                   <!--补做作业-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="studentRepairHomeworkDIV">
                       <form id="studentRepairHomework" name="studentRepairHomework"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/studentRepairHomework.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>补做语数英作业补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">学科</label>
                                   <div class="controls">
                                       <input type="radio" name="subject" checked="checked" value="CHINESE" class="input"/>语文
                                       <input type="radio" name="subject" value="MATH" class="input"/>数学
                                       <input type="radio" name="subject" value="ENGLISH" class="input"/>英语
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">作业Id</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="homeworkId">
                                   </div>
                               </div>

                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--完成竞技岛挑战-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="studentGrowthWorldAthleticsDIV">
                       <form id="studentGrowthWorldAthletics" name="studentGrowthWorldAthletics"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/studentGrowthWorldAthletics.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>完成成长世界竞技岛挑战补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>

                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--成长世界任务-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="studentGrowthWorldSubjectDIV">
                       <form id="studentGrowthWorldSubject" name="studentGrowthWorldSubject"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/studentGrowthWorldSubject.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>完成成长世界语数英智慧岛任务补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">学科</label>
                                   <div class="controls">
                                       <input type="radio" name="subject" checked="checked" value="CHINESE" class="input"/>语文
                                       <input type="radio" name="subject" value="MATH" class="input"/>数学
                                       <input type="radio" name="subject" value="ENGLISH" class="input"/>英语
                                   </div>
                               </div>

                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--黄金任务-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="studentGoldenMissionDIV">
                       <form id="studentGoldenMission" name="studentGoldenMission"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/studentGoldenMission.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>完成黄金任务补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">学科</label>
                                   <div class="controls">
                                       <input type="radio" name="subject" checked="checked" value="CHINESE" class="input"/>语文
                                       <input type="radio" name="subject" value="MATH" class="input"/>数学
                                       <input type="radio" name="subject" value="ENGLISH" class="input"/>英语
                                   </div>
                               </div>

                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--白金任务-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="studentPlatinumMissionDIV">
                       <form id="studentPlatinumMission" name="studentPlatinumMission"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/studentPlatinumMission.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>完成白金任务补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">学科</label>
                                   <div class="controls">
                                       <input type="radio" name="subject" checked="checked" value="CHINESE" class="input"/>语文
                                       <input type="radio" name="subject" value="MATH" class="input"/>数学
                                       <input type="radio" name="subject" value="ENGLISH" class="input"/>英语
                                   </div>
                               </div>

                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--打败boss-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="studentFightBossDIV">
                       <form id="studentFightBoss" name="studentFightBoss"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/studentFightBoss.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>参与打败坏习惯补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">BOSS</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="boss">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长每日登陆-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="parentLoginDIV">
                       <form id="parentLogin" name="parentLogin"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/parentLogin.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长每日登陆</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长查看作业报告-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="parentViewHomeworkReportDIV">
                       <form id="parentViewHomeworkReport" name="parentViewHomeworkReport"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/parentViewHomeworkReport.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长查看作业报告</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="parentId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">作业ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="homeworkId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长查看自学报告-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="parentViewSelfStudyReportDIV">
                       <form id="parentViewSelfStudyReport" name="parentViewSelfStudyReport"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/parentViewSelfStudyReport.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长查看自学报告</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="parentId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长给老师送鲜花-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="parentSendFlowerToTeacherDIV">
                       <form id="parentSendFlowerToTeacher" name="parentSendFlowerToTeacher"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/parentSendFlowerToTeacher.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长给老师送鲜花</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="parentId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">老师ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="teacherId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长发奖励-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="parentSendRewardDIV">
                       <form id="parentSendReward" name="parentSendReward"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/parentSendReward.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长发奖励</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="parentId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">学生ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="studentId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长首次补全信息-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="supplementParentInfoDIV">
                       <form id="supplementParentInfo" name="supplementParentInfo"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/supplementParentInfo.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长首次补全信息</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="parentId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长给孩子开通产品补成长值-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="parentPaymentDIV">
                       <form id="parentPayment" name="parentPayment"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/parentPayment.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长给孩子开通产品补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="parentId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">支付总额</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="amount">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <label class="control-label">订单id</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="orderId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
                   <!--家长给孩子开通产品补成长值-->
                   <div role="tabpanel" class="tab-pane" style="display: none;" id="clearActivationCacheDIV">
                       <form id="clearActivationCache" name="clearActivationCache"
                             method="post"
                             action="${requestContext.webAppContextPath}/toolkit/userlevel/clearActivationCache.vpage"
                             class="well form-horizontal" style="background-color: #fff;">
                           <fieldset>
                               <legend>家长给孩子开通产品补成长值</legend>
                               <div class="control-group">
                                   <label class="control-label">家长ID</label>
                                   <div class="controls">
                                       <input type="text" class="input" value="" name="userId">
                                   </div>
                               </div>
                               <div class="control-group">
                                   <input type="submit" value="操作" class="btn btn-primary"/>
                               </div>
                           </fieldset>
                       </form>
                   </div>
               </fieldset>
           </div>

        </div>

        <div role="tabpanel" class="tab-pane" id="handleUserRemindDiv">
            <form id="userRemindForm" class="well form-horizontal" action="/toolkit/userremind.vpage" method="post">
                <fieldset>
                    <legend>用户消息处理</legend>
                    <div class="control-group">
                        <label class="control-label">用户ID</label>
                        <div class="controls">
                            <input name="userId" type="text" style="height: 30px">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <#if positions?has_content>
                                <select name="reminderPosition">
                                <#list positions as position>
                                    <option value="${position}">${position}</option>
                                </#list>
                                </select>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <select name="handleType">
                                <option value="incr">增加</option>
                                <option value="decr">减少</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <select name="remindType">
                                <option value="dot">红点</option>
                                <option value="number">数字</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="remindBtn" value="确定" class="btn btn-primary">
                        </div>
                    </div>
                </fieldset>

            </form>
        </div>

    </div>

    <!--上传图片-->
    <div id="uploaderDialog" class="modal fade hide" style="width:550px; height: 300px;">
        <div class="modal-header">
            <button type="button" id="uploaderDialogClose" class="close" data-dismiss="modal" aria-hidden="true">×
            </button>
            <h3>上传图片</h3>
        </div>
        <div class="modal-body">
            <div style="float: left; width: 280px;">
                <div style="height: 200px; width: 280px;">
                    <img id="imgSrc" src="" alt="预览" style="height: 200px; width: 280px;"/>
                </div>
            </div>
            <div style="float: right">
                <div>
                    <textarea placeholder="请填写文件路径" id="uploadPath" style="resize: none;" rows="3"></textarea>
                </div>
                <div style="display: block;">
                    <a href="javascript:void(0);" class="uploader">
                        <input type="file" name="file" id="uploadFile"
                               accept="image/gif, image/jpeg, image/png, image/jpg, audio/*" onchange="previewImg(this)">选择素材
                    </a>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button title="确认上传" class="uploader" id="upload_confirm">
                <i class="icon-ok"></i>
            </button>
            <button class="uploader" data-dismiss="modal" aria-hidden="true"><i class="icon-trash"></i></button>
        </div>
    </div>
</div>
<script type="text/javascript">

    function clearNextLevel(obj) {
        if (obj.attr("next_level")) {
            clearNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
        }
    }

    $(function () {
        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#query_info_btn').click();
            }
        });
        $(".district_select").on("change", function () {
            var html = null;
            var $this = $(this);
            var next_level = $this.attr("next_level");
            var regionCode = $this.val();
            if (next_level) {
                var codeType = next_level;
                next_level = $("#" + next_level);
                clearNextLevel($this);
                $.ajax({
                    type: "post",
                    url: "regionlist.vpage",
                    data: {
                        regionCode: regionCode
                    },
                    success: function (data) {
                        html = '';
                        var regionList = data.regionList;
                        for (var i in regionList) {
                            html += '<option value="' + regionList[i]["code"] + '">' + regionList[i]["name"] + '</option>';
                        }
                        next_level.html(html);
                        <#if conditionMap?has_content>
                            if (codeType == 'citys' && !next_level.data('init')) {
                                next_level.val(${conditionMap.citys!'-1'});
                                next_level.data('init', true);
                            } else if (codeType == 'countys' && !next_level.data('init')) {
                                next_level.val(${conditionMap.countys!'-1'});
                                next_level.data('init', true);
                            }
                        </#if>
                        next_level.trigger('change');
                    }
                });
            }
        });

        <#if conditionMap?has_content>
            $("#provinces").val('${conditionMap.provinces!"-1"}');
            $("#provinces").trigger('change');
        </#if>
    });

    $(function () {

        <#if smsMessageList??>
            $('#menuList').find('a[href="#mobileMessage"]').tab('show');
        </#if>

        $("#flushcdn").on("click", function () {
            var url = $("#flushCdn_url").val().trim();
            var dir = $("#flushCdn_dir").val().trim();
            if (url == "" && dir == "") {
                alert("url或目录为空！");
                return false;
            }
            $.post("cdn/flush.vpage", {
                flushCdn_url: url,
                flushCdn_dir: dir
            }, function (data) {
                $("#flushCdn_url").val("");
                $("#flushCdn_dir").val("");
                var str = "执行结果：\r\n";
                $.each(data.result, function (key, value) {
                    str += "成功：" + value.success + "\r\n";
                    str += "cdn：" + value.cdnName + "\r\n";
                    str += "信息：" + value.message + "\r\n";
                    str += "\r\n";
                })
                alert(str);
            });
        });

        $('#actionType').on('change', function () {
            $('#finish_homework').hide();
            $('#finish_selflearning').hide();
            $('#finish_oral').hide();
            $('#submit_answer').hide();
            $('#chinese_text_read').hide();
            $('#english_walk_man').hide();
            $('#piclisten').hide();
            $('#lookhomeworkreport').hide();

            var actionType = $('#actionType option:selected').val();

            if (actionType.length == 0) return;
            if (actionType == 'FinishHomework') {
                $('#finish_homework').show();
            } else if (actionType == 'FinishSelfLearning') {
                $('#finish_selflearning').show();
            } else if (actionType == 'FinishOral') {
                $('#finish_oral').show();
            } else if (actionType == 'SubmitAnswer') {
                $('#submit_answer').show();
            } else if (actionType == 'SaveSelfStudyChineseTextRead') {
                $('#chinese_text_read').show();
            } else if (actionType == 'StartSelfStudyEnglishWalkman') {
                $('#english_walk_man').show();
            } else if (actionType == 'ClickSelfStudyEnglishPicListen') {
                $('#piclisten').show();
            } else if (actionType == 'LookHomeworkReport') {
                $('#lookhomeworkreport').show();
            }
        });

        //完成作业
        $('#fh_submit').on('click', function () {
            var userId = $('#fh_userid').val();
            var subject = $('#fh_subject option:selected').val();
            var score = $('#fh_score').val();
            var actionType = $('#actionType option:selected').val();
            var date = $('#fh_finish').val();

            $.post('action.vpage', {
                type: actionType,
                userId: userId,
                subject: subject,
                score: score,
                date: date
            }, function (data) {
                if (data.success) {
                    alert('操作成功');
                } else {
                    alert(data.info);
                }
            });
        });
        //完成自学
        $('#fs_submit').on('click', function () {
            var userId = $('#fs_userid').val();
            var date = $('#fs_finish').val();
            var actionType = $('#actionType option:selected').val();

            $.post('action.vpage', {type: actionType, userId: userId, date: date}, function (data) {
                if (data.success) {
                    alert('操作成功');
                } else {
                    alert(data.info);
                }
            });
        });
        //完成口语
        $('#fo_submit').on('click', function () {
            var hid = $('#fo_hid').val();
            var uid = $('#fo_uid').val();
            var date = $('#fo_finish').val();
            var actionType = $('#actionType option:selected').val();

            if (hid.length <= 0) {
                alert('homeworkId不能为空');
                return;
            }

            $.post('action.vpage', {
                type: actionType,
                userId: uid,
                hid: hid,
                date: date
            }, function (data) {
                if (data.success) {
                    alert('操作成功');
                } else {
                    alert(data.info);
                }
            });
        });
        //提交答案
        $('#sba_submit').on('click', function () {
            var uid = $('#sba_uid').val();
            var date = $('#sba_finish').val();
            var actionType = $('#sba_type option:selected').val();

            $.post('action.vpage', {type: actionType, userId: uid, date: date}, function (data) {
                if (data.success) {
                    alert('操作完成');
                } else {
                    alert(data.info);
                }
            });
        });
        //完成语文朗读
        $('#ctr_submit').on('click', function () {
            var uid = $('#ctr_uid').val();
            var date = $('#ctr_finish').val();
            var actionType = $('#actionType option:selected').val();

            $.post('action.vpage', {type: actionType, userId: uid, date: date}, function (data) {
                if (data.success) {
                    alert('操作完成');
                } else {
                    alert(data.info);
                }
            });
        });
        //完成英语随身听
        $('#ewm_submit').on('click', function () {
            var uid = $('#ewm_uid').val();
            var date = $('#ewm_finish').val();
            var actionType = $('#actionType option:selected').val();

            $.post('action.vpage', {type: actionType, userId: uid, date: date}, function (data) {
                if (data.success) {
                    alert('操作完成');
                } else {
                    alert(data.info);
                }
            });
        });
        //使用英语点读机
        $('#pl_submit').on('click', function () {
            var uid = $('#pl_uid').val();
            var date = $('#pl_finish').val();
            var actionType = $('#actionType option:selected').val();

            $.post('action.vpage', {type: actionType, userId: uid, date: date}, function (data) {
                if (data.success) {
                    alert('操作完成');
                } else {
                    alert(data.info);
                }
            });
        });
        //查看作业报告
        $('#lhr_submit').on('click', function () {
            var uid = $('#lhr_uid').val();
            var date = $('#lhr_finish').val();
            var actionType = $('#actionType option:selected').val();
            var subject = $('#lhr_select option:selected').val();

            if (subject.length == 0) {
                alert("请选择学科");
                return;
            }
            $.post('action.vpage', {type: actionType, userId: uid, date: date, subject: subject}, function (data) {
                if (data.success) {
                    alert('操作完成');
                } else {
                    alert(data.info);
                }
            });
        });

        $("#juan_upload_btn").on('click', function () {
            $('#uploadFile').val("");
            $('#imgSrc').attr("src", "");
            $('#uploadPath').val("");
            $('#uploaderDialog').modal("show");
        });

        $('#upload_confirm').on('click', function () {
            // 获取参数
            var path = $('#uploadPath').val();
            // 拼formData
            var formData = new FormData();
            var file = $('#uploadFile')[0].files[0];
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('path', path);
            // 发起请求
            $.ajax({
                url: 'uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        //alert(res.info);
                        $("#photoPath").text(res.info);
                        // window.location.reload();
                        $('#uploaderDialogClose').trigger("click");
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        $('#btn_subscribe_submit').on('click', function () {
            var albumId = $('#txt_subscribe_albumid').val();
            var userIds = $('#txt_subscribe_userids').val();

            if (albumId.length == 0) {
                alert('请输入专辑ID');
                return;
            }
            if (userIds.length == 0) {
                alert('请输入用户ID');
                return;
            }

            $.post('subscribe.vpage', {albumId: albumId, userIds: userIds}, function (data) {
                if (data.success) {
                    alert('操作成功');
                } else {
                    alert('操作完成，部分用户失败，非家长：' + data.notExist + ',订阅失败:' + data.failUser);
                }
            });
        });

        //同步点读机订单
        $('#btn_sync_order_submit').on('click', function () {
            var orderIds = $('#txt_syn_order_id').val();
            if (0 === orderIds.length) {
                alert('请输入订单号');
                return;
            }

            $.post('syncFltrpOrder.vpage', {ids: orderIds}, function (data) {
                if (data.success) {
                    alert('操作成功');
                } else {
                    alert(data.info);
                }
            });
        });
        $('#btn_refund_order_submit').on('click', function () {
            var orderIds = $('#txt_refund_order_id').val();
            if (0 === orderIds.length) {
                alert('请输入订单号');
                return;
            }
            $.post('refundpiclistenbookorder.vpage', {ids: orderIds}, function (data) {
                if (data.success) {
                    alert('操作成功');
                } else {
                    alert(data.info);
                }
            });
        });
        $("#weekDate1").datepicker({
            dateFormat: 'yymmdd',
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

        $("#weekDate2").datepicker({
            dateFormat: 'yy-mm-dd',
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


        $("#listsmallpayment").on("click", function () {

            var province = $("#provinces").val();
            var city = $("#citys").val();
            var county = $("#countys").val();
            var weekDate = $("#weekDate1").val();

            var $smallPaymentContext = $("#smallPaymentContext");
            var generateTRTD = function (homework) {
                return "<tr><td>"
                        + homework.id + "</td><td>"
                        + homework.week + "</td><td>"
                        + homework.subject + "</td><td>"
                        + homework.clazzLevel + "年级</td><td>"
                        + homework.questionIds + "</td><td>"
                        + homework.betweenTime + "</td><td>"
                        + homework.createAt + "</td></tr>";
            };

            $.get("/toolkit/homework/listsmallpayment.vpage", {
                province: province,
                city: city,
                county: county,
                weekDate: weekDate
            }, function (data) {
                var homeworkList = data.homeworkList;
                var trStr = "";
                if (data.success && $.isArray(homeworkList) && homeworkList.length > 0) {
                    $.each(homeworkList, function (i, homework) {
                        trStr += generateTRTD(homework);
                    });
                    $smallPaymentContext.empty().html(trStr);
                } else {
                    $smallPaymentContext.empty().html("<tr><td colspan='6'>暂无数据</td></tr>");
                }
            }).fail(function () {
                $smallPaymentContext.empty().html("<tr><td colspan='6'>数据错误</td></tr>");
            });

        });
        //人教点读机用户设备查询
        $("#query_submit").on("click", function () {
            var userId = $("#user_id").val();
            var devTable = $("#devInfoList");
            var generateTable = function (dev) {
                return "<tr><td>"
                        + "<input type='checkbox' name='devIds' data-dev_id='" + dev.devId + "'/>" + "</td><td>"
                        + dev.devId + "</td><td>"
                        + dev.devName + "</td><td>"
                        + dev.createTime + "</td></tr>";
            };
            $.get("/toolkit/getRJUserDevList.vpage", {
                userId: userId
            }, function (data) {
                var devList = data.devList;
                var tabStr = "";
                if (data.success && $.isArray(devList) && devList.length > 0) {
                    $.each(devList, function (i, dev) {
                        tabStr += generateTable(dev);
                    });
                    devTable.empty().html(tabStr);
                } else {
                    devTable.empty().html("<tr><td colspan='6'>暂无数据</td></tr>");
                }
            }).fail(function () {
                devTable.empty().html("<tr><td colspan='6'>数据错误</td></tr>");
            });
        });
        $("#del_submit").on("click", function () {
            var userId = $("#user_id").val();
            var devIds = [];
            $("input[name='devIds']:checked").each(function () {
                devIds.push($(this).data('dev_id'));
            });
            $.post("/toolkit/delRJUserDevs.vpage", {
                userId: userId,
                devIds: JSON.stringify(devIds)
            }, function (data) {
                if (data.success) {
                    alert("删除设备成功");
                } else {
                    alert("删除设备失败");
                }
                $("#query_submit").trigger("click");
            })
        })


    });

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
    var original = "studentFinishHomeworkDIV";
    $("#repairGrowthSelector").change(function () {
        var current = $(this).val();
        if(current == original){
            return;
        }
        $("#"+original).hide();
        original = current;
        $("#"+original).show();
    });

    var business_type = "VACATION_DAY_PACKAGE";
    var id_type = "STUDENT_ID";
    var black_white_id = "";
    var total_page = 1;
    var page_num = 1;
    function delete_black_white_list_Action (id){

        if(!confirm("确定要删除吗？")){
            return false;
        }
        $.post('/toolkit/homework/deleteHomeworkBlackWhiteList.vpage',{
            id:id
        },function(data){
            alert(data.info);
            loadHomeworkBlackWhiteList(business_type, id_type, null, null);
        });
    }

    function loadHomeworkBlackWhiteList(businessType, idType, blackWhiteId, pageNum){
        $.get('/toolkit/homework/loadHomeworkBlackWhiteList.vpage',{
            businessType:businessType,
            idType:idType,
            blackWhiteId:blackWhiteId,
            pageNum:pageNum
        },function(data){
            total_page = data.totalPages;
            page_num = data.currentPage;
            $("#table_black_white").html("");
            var tr_head = $("<tr>\n" +
                    "                    <th>业务类型</th>\n" +
                    "                    <th>ID类型</th>\n" +
                    "                    <th>黑(白)名单ID</th>\n" +
                    "                    <th>添加时间</th>\n" +
                    "                    <th>操作</th>\n" +
                    "                </tr>");
            $("#table_black_white").append(tr_head);
            for(var i=0;i<data.homeworkBlackWhiteLists.length;i++){
                var data_list = data.homeworkBlackWhiteLists;
                var tr_body = $("<tr>" +
                        "             <td>"+data_list[i].businessType+"</td>\n" +
                        "             <td>"+data_list[i].idType+"</td>\n" +
                        "             <td>"+data_list[i].blackWhiteId+"</td>\n" +
                        "             <td>"+formatLongDate(data_list[i].createAt)+"</td>\n" +
                        "<td><a href='javascript:void(0);' class='btn btn-inverse' onclick='delete_black_white_list_Action(\""+data_list[i].id+"\")'><i class='icon-remove icon-white'></i> 删除</a></td>"+
                        "        </tr>");
                $("#table_black_white").append(tr_body);
            }
        });
    }

    $(function(){
        $('#blackWhite').click(function () {
            loadHomeworkBlackWhiteList(business_type, id_type, null, null);
        });

        $('#query_black_white_list').click(function () {
           business_type = $('#select_business_type')[0].value;
           id_type = $('#select_id_type')[0].value;
           black_white_id = $('#input_black_white_id')[0].value;
           loadHomeworkBlackWhiteList(business_type, id_type, black_white_id, null);
        });

        $('#first_page').click(function () {
            loadHomeworkBlackWhiteList(business_type, id_type, black_white_id, 1);
        });
        $('#pre_page').click(function () {
            page_num = page_num - 1;
            if (page_num < 1) {
                page_num = 1;
            }
            loadHomeworkBlackWhiteList(business_type, id_type, black_white_id, page_num);
        });

        $('#next_page').click(function () {
            page_num = page_num +1;
            if (page_num > total_page) {
                page_num = total_page;
            }
            loadHomeworkBlackWhiteList(business_type, id_type, black_white_id, page_num);
        });

        $('#last_page').click(function () {
            page_num = total_page;
            loadHomeworkBlackWhiteList(business_type, id_type, black_white_id, page_num);
        });
    });

    function formatLongDate(timestamp) {
        var now=new Date(timestamp);
        var year = now.getFullYear();
        var month = now.getMonth() + 1;
        var date = now.getDate();
        var hour = now.getHours();
        var minute = now.getMinutes();
        var second = now.getSeconds();
        return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
    }
</script>
</@>