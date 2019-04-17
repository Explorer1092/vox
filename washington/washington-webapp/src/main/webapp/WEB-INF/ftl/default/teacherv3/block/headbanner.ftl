<div class="switchBox" id="headSwitchBanner" style="position: relative; margin: 0 0 15px; overflow: hidden; *zoom: 1;">
    <ul style="overflow: hidden;">
        <#if ([130800]?seq_contains(currentTeacherDetail.cityCode) && (currentTeacherDetail.subject)?? && currentTeacherDetail.subject == "ENGLISH") || ftlmacro.devTestSwitch>
        <li>
            <a href="${ProductConfig.getMainSiteBaseUrl()}/ucenter/partner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}%2fopen.php%3fmod%3dregister&urlInput=${ProductConfig.getBbsSiteBaseUrl()}%2fforum.php%3Fmod%3Dforumdisplay%26fid%3D60" target="_blank">
                <img src="//cdn.17zuoye.com/static/project/teacherGround/teachingResourcesBanner.jpg">
            </a>
        </li>
        </#if>
        <#if .now gt '2015-04-16 23:59:59'?datetime('yyyy-MM-dd HH:mm:ss')>
        <li>
            <a target="_blank" href="//cdn.17zuoye.com/static/project/papercollection/index.html">
                <img src="//cdn.17zuoye.com/static/project/papercollection/images/teacheraActivitiesBanner.jpg">
            </a>
        </li>
        </#if>
        <#if ((currentTeacherDetail.cityCode)?? && [370300,371600,370500,371200,370700,370100]?seq_contains(currentTeacherDetail.cityCode))>
            <li style="display: block;">
                <a href="http://help.17zuoye.com/?p=538" target="_blank"><img src="//cdn.17zuoye.com/static/project/teacherGround/teacherSmtBanner.jpg" /></a>
            </li>
        </#if>
    </ul>
    <div class="tab" style="position: absolute; bottom: 0; right: 10px;"></div>
</div>
<@ftlmacro.allswitchbox target="#headSwitchBanner" second=4000/>
<#--新学期申请换班 step-->
<#--
<#if currentUser.fetchCertificationState() == "SUCCESS" >
    <div class="t-shift-box">
        <ul>
            <li>
                <a href="javascript:void(0)" class="v-theApplication-click">
                    <span class="sf-icon sf-icon-1"></span>
                    <h4>向别的老师索要班级</h4>
                </a>
            </li>
            <li>
                <a href="javascript:void(0)" id="theApplication-btn">
                    <span class="sf-icon sf-icon-2"></span>
                    <h4>转出班级给别的老师</h4>
                </a>
            </li>
            <li>
                <a href="javascript:void(0)" class="v-theApplication-click">
                    <span class="sf-icon sf-icon-3"></span>
                    <h4>添加新班级</h4>
                </a>
            </li>
            <li>
                <a href="http://www.17zuoye.com/ucenter/partner.vpage?url=http://www.17huayuan.com/open.php?mod=register&urlInput=http%3A%2F%2Fwww.17huayuan.com%2Fforum.php%3Fmod%3Dviewthread%26tid%3D12928%26extra%3Dpage%253D1" target="_blank">
                    <span class="sf-icon sf-icon-4"></span>
                    <h4>换班攻略</h4>
                </a>
            </li>
            <li>
                <a href="javascript:void(0);" style="cursor: default;">
                    <span class="sf-icon sf-icon-5"></span>
                    <h4 style="font-size: 12px;">求助热线<br/><@ftlmacro.hotline phoneType="teacher"/></h4>
                </a>
            </li>
            &lt;#&ndash;换班求助-弹窗&ndash;&gt;
            &lt;#&ndash;<li>
                <a href="javascript:void(0);" class="v-clazzChanger-click">
                    <span class="sf-icon sf-icon-5"></span>
                    <h4 style="font-size: 12px;">换班求助</h4>
                </a>
            </li>&ndash;&gt;
        </ul>
    </div>

    <script type="text/html" id="t:换班求助">
        <div class="w-form-table" style="padding: 0;">
            <dl>
                <dt style="width:90px;">问题描述：</dt>
                <dd style="margin-left:90px;">
                    <textarea style="width: 300px; height: 100px;" class="w-int" id="v-content-me"></textarea>
                </dd>
            </dl>
        </div>
    </script>

    <script type="text/javascript">
        $(function(){
            $(".v-theApplication-click").on("click", function(){
                $.get("/teacher/getclazzlist.vpage", {} ,function(data){
                    if(data.clazzs.length < 8){
                        location.href = "/teacher/clazz/createclazz.vpage";
                    }else{
                        $17.alert("<div>您的班级数量已达到上限，不能再添加新班级。</div><div>如有删除班级问题请拨打客服电话</div>");
                    }
                });
            });

            $(".v-clazzChanger-click").on("click", function(){
                $.prompt(template("t:换班求助", {}), {
                    title: "换班求助",
                    buttons : { "取消" :  false, "确定" : true},
                    focus: 1,
                    submit : function(e, v){
                        if(v){
                            $.post("/teacher/clazzexchangehelper.vpage", {content: $("#v-content-me").val()} ,function(data){});
                        }
                    }
                });
            });
        });
    </script>
</#if>-->
