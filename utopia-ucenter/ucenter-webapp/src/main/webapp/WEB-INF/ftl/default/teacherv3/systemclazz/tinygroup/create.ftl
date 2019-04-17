<script>
    window.location.replace("/");
</script>
<#--
<#import "loyout.ftl" as temp />
<@temp.tinyGroup title="任命${temp.curSubjectText!}小组长">
&lt;#&ndash;//start&ndash;&gt;
<#if students?size gt 0>
    <div class="dropDownBox_tip" style="position: relative; ">
        <#if students?size lte 3>
            <div class="tip_content" style="width: 100%; padding: 10px 0; text-align: center; border-width: 0 0 1px;">
                该班级人数较少，请让学生通过老师<#if mobile?has_content>手机</#if>号 <#if mobile?has_content>${mobile!''}<#else>${currentUser.id!}</#if> 加入班级。
            </div>
        </#if>
    </div>
    <div class="w-gray" style="text-align: center; padding: 20px 0 0; font-size: 14px;">
        建议您按照班里真实小组（座位等）的方式来分组
    </div>
    <div class="t-addclass-case">
        <dl>
            <dt>学生：</dt>
            <dd>
                <div class="w-check-list">
                    <#list students as sdt>
                        <a href="javascript:void(0);" class="v-check-student" title="${sdt.userName}" data-userid="${sdt.userId}" style="height: 18px; width: 130px;">
                            <span class="w-checkbox"></span>
                            <span class="w-icon-md" style="width: 70px;"><#if (sdt.userName)?has_content>${sdt.userName}<#else>${sdt.userId}</#if></span>
                            <span class="w-icon-public w-icon-leader" style="margin: 0; display: none;"></span>
                        </a>
                    </#list>
                </div>
            </dd>
        </dl>
    </div>
    <div class="t-pubfooter-btn">
        &lt;#&ndash;<a class="w-btn w-btn-green w-btn-small data-ImportStudentName" data-clazzid="${clazzId!0}" data-creatortype="SYSTEM" href="javascript:void(0);">导入学生姓名</a>&ndash;&gt;
        <a class="v-next w-btn w-btn-small w-btn-green" href="javascript:history.back();">返回</a>
        <a class="v-next w-btn w-btn-small v-appointedLeader-submit" href="javascript:void(0);">任命小组长</a>
    </div>
<#else>
    <div class="w-gray" style="padding: 40px 0; text-align: center;">暂时没有学生，请让学生通过老师老师<#if mobile?has_content>手机</#if>号 <#if mobile?has_content>${mobile!''}<#else>${currentUser.id!}</#if> 加入班级。</div>
    <div class="t-pubfooter-btn">
        <a class="v-next w-btn w-btn-small w-btn-green" href="javascript:history.back();">返回</a>
    </div>
</#if>
&lt;#&ndash;end//&ndash;&gt;

<script type="text/javascript">
    $(function(){
        //选择学生
        var _tempRecardStudents = [];
        var _tempGroupId = ${groupId!0};
        var _tempGroupSize = ${19 - ((size!0) - students?size)};
        $(document).on("click", ".v-check-student", function(){
            var $this = $(this);
            var $userId = $this.attr("data-userid");

            if($this.hasClass("active")){
                $this.removeClass("active");
                if($.inArray($userId, _tempRecardStudents) > -1){
                    _tempRecardStudents.splice($.inArray($userId, _tempRecardStudents), 1);
                }
            }else{
                if(_tempRecardStudents.length > _tempGroupSize){
                    $17.alert("小组数量不能多于20");
                    return false;
                }

                $this.addClass("active");
                _tempRecardStudents.push($userId);
            }
        });

        //提交任命小组长
        $(document).on("click", ".v-appointedLeader-submit", function(){
            var $this = $(this);

            if(_tempRecardStudents.length < 1){
                $17.alert("请选择任命学生");
                return false;
            }

            if($this.hasClass("dis")){
                return false;
            }

            $this.addClass("dis");
            $.post("/teacher/clazz/tinygroup/ctg.vpage?subject=${temp.curSubject!}", {
                groupId : _tempGroupId,
                studentIds : _tempRecardStudents.join()
            }, function(data){
                if(data.success){
                    $17.alert("任命完成！请在课堂上通知同学们，点击“班级小组卡片”加入对应的小组。", function(){
                        location.href = "/teacher/clazz/tinygroup/editcrew.vpage?clazzId=${clazzId!0}&subject=${temp.curSubject!}";
                    });
                }else{
                    $17.alert(data.info);
                }
                $this.removeClass("dis");
            });
        });
    });
</script>
&lt;#&ndash;<#include "../../block/batchAddStudentName.ftl"/>&ndash;&gt;
</@temp.tinyGroup>-->
