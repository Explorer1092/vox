<#-- @ftlvariable name="startDate" type="java.lang.String" -->

<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <div>
        <form method="get" action="?" class="form-horizontal">
            <fieldset><legend>家长端反馈</legend></fieldset>
            <ul class="inline form_datetime">
                <li>
                    <label for="userId">
                        处理状态
                        <select id="state" name="state">
                            <option value="0" <#if state == 0>selected </#if>>未处理</option>
                            <option value="1" <#if state == 1>selected </#if>>已处理</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="startDate">
                        起始时间
                        <input name="startDate" id="startDate" type="text" placeholder="格式：2013-11-04"/>
                    </label>
                </li>
                <li>
                    <label for="endDate">
                        截止时间
                        <input name="endDate" id="endDate" type="text" placeholder="格式：2013-11-04"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
        </form>
        <table class="table table-striped table-bordered" style="font-size: 14px;">
            <thead>
            <tr>
                <th>反馈时间</th>
                <th>账户</th>
                <th>内容</th>
                <th>回复内容</th>
                <th>回复人</th>
                <th>来源</th>
                <th>处理状态</th>
                <th style="width: 80px;">操作</th>
            </tr>
            </thead>
            <#if wechatQuestions?has_content>
                <#list wechatQuestions as wechatQuestion>
                    <tr>
                        <td>${wechatQuestion.getCreateDatetime()?datetime!''}</td>
                        <td>${wechatQuestion.getOpenId()!''}</td>
                        <td>${wechatQuestion.getContent()!''}</td>
                        <td>${wechatQuestion.getReply()!''}</td>
                        <td>${wechatQuestion.getReplyer()!''}</td>
                        <td>${wechatQuestion.getSourceType().getDescription()!''}</td>
                        <td>
                            <#if wechatQuestion.getState() == 0>
                                未处理
                            <#else>
                                <#if wechatQuestion.getState() == 1>
                                    已处理
                                </#if>
                            </#if>
                        </td>
                        <td>
                            <#if wechatQuestion.getState() == 0>
                                <input name="btnprocess" type="button" value="处理" fid="${wechatQuestion.getId()!''}" />
                            </#if>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<div id="process_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>解决反馈<span id="replied_user_id"></span></h3>
    </div>
    <div class="modal-body dl-horizontal">
        <dl>
            <dt>状态：</dt>
            <dd>
                <select id ="slstate">
                    <option value="1" selected="selected">已处理</option>
                    <option value="0" >未处理</option>
                </select>
            </dd>
        </dl>
        <dl>
            <dt>问题描述：</dt>
            <dd><textarea id="process_dialog_text" cols="35" rows="4"></textarea></dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="process_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        <input type="hidden" id="process_feedback_id"/>
    </div>
</div>

<div id="alert_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>解决反馈<span id="replied_user_id"></span></h3>
    </div>
    <div class="modal-body dl-horizontal">
        您有<span id="alertcount"></span>条未处理请求！<a href="index.vpage">去处理</a></dt>

    </div>
</div>
<div class="warningTone"></div>

<script type="text/javascript" src="http://17zuoye.com/public/plugin/jquery-jmp3/jquery.jmp3.min.js"></script>
<script type="text/javascript">
    // 使用message对象封装反馈
    var message = {
        time : 0,
        title: document.title,
        timer: null,

        // 显示新反馈提示
        show:function(){
            var title = message.title.replace("【　　】", "").replace("【新反馈】", "");
            // 定时器，设置反馈切换频率闪烁效果就此产生
            message.timer = setTimeout(
                    function() {
                        message.time++;
                        message.show();

                        if (message.time % 2 == 0) {
                            document.title = "【新反馈】" + title
                        }else{
                            document.title = "【　　】" + title
                        }
                    },
                    600 // 闪烁时间差
            );
            return [message.timer, message.title];
        },

        // 取消新反馈提示
        clear: function(){
            clearTimeout(message.timer);
            document.title = message.title;
        },

        //播放提示音
        beep : function(){
            $(".warningTone").jmp3({autoStart: 'true', height:0, width:0, file: 'http://www.17zuoye.com/static/project/video/notify.mp3?1.0.2'});
        }
    };

    $(function(){
        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
    });

    var Feedback ={
        init:function(){
            var me = Feedback;
            $('input[name="btnprocess"]').click(function(){
                me.process($(this).attr("fid"));
            });
            $('#process_dialog_btn_ok').click(function(){
                me.submit();
            });
            // var int=setInterval('me.alert()',2000);
        },
        process:function(fid){
            var $dialog = $('#process_dialog');
            $dialog.find('#process_feedback_id').val(fid);
            $dialog.modal('show');
        },
        submit:function(){
            $.post('process.vpage',
                    {
                        id:$('#process_feedback_id').val(),
                        desc:$('#process_dialog_text').val(),
                        state:$('#slstate').find('option:selected').val()
                    },function(data){
                        if(!data.success){
                            alert(data.info);
                        }else{
                            window.location.reload();
                        }

                    });
        }
    };
    $(function(){
        $('#startDate').val('${startDate!''}');
        $('#endDate').val('${endDate!''}');
        Feedback.init();
    });

    var int = setInterval(function(){
        //清除新反馈提示
        message.clear();
        alertform()
    }, 60 * 1000);

    var modalshow = false;
    function alertform(){
        $.get('processcheck.vpage',function(data){
            if(data.success){
                var $dialog = $('#alert_dialog');
                $dialog.find("#alertcount").html(data.info);
                if(!modalshow){
                    $dialog.modal('show');
                }
                $dialog.on('shown',function(){
                    modalshow = true;
                });
                $dialog.on('hidden',function(){
                    modalshow = false;
                    //清除新反馈提示
                    message.clear();
                });
                //新反馈提示
                message.show();
                message.beep();
            }
        });
    }
</script>
</@layout_default.page>
