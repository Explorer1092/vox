<#--教学调查-->
<script type="text/html" id="T:教学小调查POPUP">
    <div class="w-form-table" style="margin: -40px -20px -20px; padding: 0;" id="teachingSurveyPopup">
        <div style="color: #c9621c; background: #f2e9a8; border-bottom: 1px solid #fbe099; padding: 10px; margin-bottom: 20px; text-align: center;">我们会根据实际情况为您推送最合适的作业内容</div>
        <dl>
            <dt style="width: 230px;">1、您现在教的教材内容是：</dt>
            <dd style="margin-left: 230px;">
                <select class="w-int" style="width: 250px;" data-teachingsurvey="materials">
                    <option value="0">请选择教材内容</option>
                    <#if (currentTeacherDetail.subject == "ENGLISH")!false>
                        <option>PEP-小学英语</option>
                        <option>北京版-小学英语</option>
                        <option>北师大版-小学英语(一年级起点)</option>
                        <option>北师大版-小学英语(三年级起点)</option>
                        <option>川教版新路径-小学英语(一年级起点)</option>
                        <option>川教版新路径-小学英语(三年级起点)</option>
                        <option>广东人民出版社-儿童英语</option>
                        <option>广东版-开心学英语</option>
                        <option>牛津小学英语(上教版)</option>
                        <option>苏教-译林版-牛津小学英语</option>
                        <option>人教版精通英语</option>
                        <option>深圳朗文版-小学英语</option>
                        <option>牛津上海版小学英语-深圳用</option>
                        <option>湘少版-小学英语</option>
                        <option>小学英语-山东科技版(三年级起点)</option>
                        <option>外研社-新标准-小学英语(一年级起点)</option>
                        <option>外研社-新标准-小学英语(三年级起点)</option>
                        <option>广州版-英语口语</option>
                        <option>广州版小学英语（教育科学出版社）</option>
                        <option>辽师大版-快乐英语(一年级起点)</option>
                        <option>辽师大版-快乐英语(三年级起点)</option>
                        <option>人教-新起点小学英语</option>
                        <option>EEC-小学英语</option>
                        <option>冀教版-小学英语(一年级起点)</option>
                        <option>冀教版-小学英语(三年级起点)</option>
                        <option>科普版-小学英语(三年级起点)</option>
                        <option>闽教版-小学英语(三年级起点)</option>
                        <option>牛津上海版(试用本)</option>
                        <option>英语Join In</option>
                        <option>陕旅版-小学英语</option>
                        <option>重庆大学版小学英语</option>
                        <option>上外新世纪版小学英语</option>
                        <option>清华大学版小学英语</option>
                        <option>新蕾天津版快乐英语</option>
                        <option>人教版灵通英语(三年级起点)</option>
                        <option>鲁湘版-小学英语</option>
                        <option>典范英语</option>
                        <option>鄂教版-小学英语</option>
                        <option>乐唱英语</option>
                    </#if>
                    <#if (currentTeacherDetail.subject == "MATH")!false>
                        <option>人教版</option>
                        <option>北京版</option>
                        <option>北师大版</option>
                        <option>苏教版</option>
                        <option>浙教版</option>
                        <option>冀教版</option>
                        <option>青岛版</option>
                        <option>青岛五四制版</option>
                        <option>西师版</option>
                        <option>沪教版</option>
                    </#if>
                    <#if (currentTeacherDetail.subject == "CHINESE")!false>
                        <option>人教版</option>
                        <option>苏教版</option>
                        <option>北师大版</option>
                        <option>湘教版</option>
                        <option>鄂教版</option>
                        <option>冀教版</option>
                        <option>鲁教版</option>
                        <option>西师大版</option>
                        <option>语文S版</option>
                        <option>语文A版</option>
                        <option>北京版</option>
                        <option>教育科学出版社</option>
                        <option>长春版</option>
                        <option>沪教版</option>
                        <option>浙教义务版</option>
                    </#if>
                </select>
            </dd>
            <dt style="width: 230px;">如没有找到，请在此填写：</dt>
            <dd style="margin-left: 230px;">
                <input type="text" class="w-int" value="" placeholder="请填写教材名称" style="width: 230px;" data-teachingsurvey="jcName" maxlength="30">
            </dd>
            <dt style="width: 230px;">2、您现在使用最多的教辅是：</dt>
            <dd style="margin-left: 230px;">
                <input type="text" class="w-int" value="" placeholder="请填写教辅名称" style="width: 230px;" data-teachingsurvey="jfName" maxlength="30">
            </dd>
        </dl>
        <div class="alertInfoBox" style="text-align: center;clear: both; color: #f00; display: none;"></div>
    </div>
</script>
<script type="text/javascript">
    (function($){
        function getTeachingSurvey(){
            $.prompt(template("T:教学小调查POPUP", {}),{
                title  : '教学小调查',
                buttons : {"确定" : true},
                position : { width : 550},
                close : function() {
                    $17.setCookieOneDay("teachsv", "2", 2);
                },
                submit: function(e, v){
                    if(v){
                        if( $17.isBlank(getIntVal("materials")) || getIntVal("materials") == 0 ){
                            alertInfo("请选择教材内容", "#teachingSurveyPopup .alertInfoBox");
                            return false;
                        }else if( $17.isBlank(getIntVal("jcName")) ){
                            alertInfo("请填写教材名称", "#teachingSurveyPopup .alertInfoBox");
                            return false;
                        }else if( $17.isBlank(getIntVal("jfName")) ){
                            alertInfo("请填写教辅名称", "#teachingSurveyPopup .alertInfoBox");
                            return false;
                        }

                        $17.voxLog({
                            app : "teacher",
                            module : "teachingSurveyPopup",
                            op : "submit",
                            materials : getIntVal("materials"),
                            jcName : getIntVal("jcName"),
                            jfName : getIntVal("jfName")
                        });

                        $17.setCookieOneDay("teachsv", "35", 35);

                        setTimeout(function(){
                            $17.alert("感谢参与，一起作业将为您做到更好");
                        }, 10);
                    }
                }
            });

            function alertInfo(msg, eleName){
                $(eleName).text(msg).slideDown();

                setTimeout(function(){
                    $(eleName).slideUp(function(){
                        $(eleName).text("");
                    });
                }, 3000);
            }

            function getIntVal(name){
                return $("#teachingSurveyPopup").find("[data-teachingsurvey='" + name + "']").val();
            }
        }

        $.extend($, {
            getTeachingSurvey : getTeachingSurvey
        });
    })($);
</script>