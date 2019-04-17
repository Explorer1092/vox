<#import "module.ftl" as com>
<@com.page title="activate">
    <@sugar.capsule js=["qtip", "ZeroClipboard"] css=["plugin.jquery.qtip"] />
<style>
    .userListBox .avatar .checkboxs { position: absolute; bottom: 0px; right: -5px; }
    .userListBox .avatar .checkboxs_active { _bottom: 1px; }
    .top_class { background-color: #7CD442; height: 60px; width: auto; border-radius: 10px 10px 0 0; text-align: center; }
    .top_class span { font-size: 18px; color: #ffffff; position: relative; top : 22px; width: 730px; }
    #clip_container div{ left: 0 !important; top: -5px !important; width: 225px !important; height: 45px !important;}
    #clip_container div embed{ width: 225px; height: 45px;}
</style>
<#include "teacher.ftl"/>
<#--学生-->
<#if ftlmacro.isInJuneForInvite>
<div class="top_class">
    <span>
        唤醒一个同学，最多奖励50学豆 <a class="viewRulesBut" data-rt="studentRules" href="javascript:void (0);" style="font-size: 12px;color: blue;">查看规则</a>
    </span>
</div>
<div class="rule">
<#--唤醒学生列表-->
    <div class="summary">
        <#if (studentList_Never?has_content && studentList_Never?size gt 0) || (studentList_twoWeeks?has_content && studentList_twoWeeks?size gt 0) || (studentList_aMonth?has_content && studentList_aMonth?size gt 0)>
            <div class="userListBox" style="margin: 0; padding:10px 0 ;">
            <#--<div style="margin-left: 650px;" id="selectAllStudents">
                <span class="text_gray_9"><i class="checkboxs"></i>全选</span>
            </div>-->
                <div id="show_student_list" style="position:relative; overflow-y:auto; overflow-x:hidden; height:410px; width: 99%;">
                    <#if studentList_aMonth?? && studentList_aMonth?has_content>
                        <div style="clear: both; text-align: center; line-height: 30px;">这些同学离开一个月了，邀请成功可以获得
                            <strong style="color: orange;">30学豆+PK免费转职次数</strong>
                            <a class="viewRulesBut" data-rt="giftBox" href="javascript:void (0);" style="font-size: 12px;color: blue;">查看规则</a>
                        </div>
                        <ul style="margin: 0 auto; width: 650px;">
                            <#list studentList_aMonth as sm>
                                <@userInfo student=sm/>
                            </#list>
                        </ul>
                    </#if>
                    <#if studentList_twoWeeks?? && studentList_twoWeeks?has_content>
                        <div style="clear: both; text-align: center; line-height: 30px;">这些同学离开两周了，邀请成功可以获得
                            <strong style="color: orange;">10学豆</strong></div>
                        <ul style="margin: 0 auto; width: 650px;">
                            <#list studentList_twoWeeks as st>
                                <@userInfo student=st/>
                            </#list>
                        </ul>
                    </#if>
                    <#if studentList_Never?? && studentList_Never?has_content>
                        <div style="clear: both; padding: 30px; text-align: center;">这些同学从未做过作业，邀请成功可以获得
                            <strong style="color: orange;">50学豆</strong></div>
                        <ul style="margin: 0 auto; width: 650px;">
                            <#list studentList_Never as s>
                                <@userInfo student=s/>
                            </#list>
                        </ul>
                    </#if>
                </div>
            </div>
            <div style="text-align: center; padding: 20px;">
                <a id="invite_student_but" href="javascript:void(0);" class="w-btn w-btn-green">
                    <strong>激活同学</strong>
                </a>
            </div>

            <#macro userInfo student>
                <#compress>
                    <li>
                        <div class="avatar">
                            <div class="new-icon"></div>
                            <img src="<@app.avatar href='${student.userAvatar!}'/>" style="width: 70px; height: 71px;">
                            <i class="checkboxs selectStudentCheckbox"
                               data-student_id="${student.userId!}"
                               data-student_name="${student.userName!}"
                               data-student_img="${student.userAvatar!}"
                               data-student_active_type="${student.type!}"></i>
                        </div>
                        <div class="title">${student.userName!}</div>
                    </li>
                </#compress>
            </#macro>
        <#else>
            <div class="w-ag-center w-ft-large" style="padding:50px 0;">目前没有学生需要激活，明天再过来看看吧。</div>
        </#if>
    </div>
    <div class="bot"></div>
</div>


</#if>


<div class="inrule" style="padding: 20px 0; overflow: hidden; *zoom:1; *display: inline;">

    <p style="width: 100%"><strong>特别声明：</strong>
        1.主办方将对所有参与者进行严格审核，任何恶意注册、重复注册、虚假信息等均视为舞弊，一经查出，除取消获奖资格外，还将从系统中扣除所有学豆和奖品兑换资格；<br>
        2. 一起作业网拥有对此次活动的最终解释。
    </p>
    <!--end//-->
</div>
<script type="text/html" id="t:成功提示框">
    <div id="success_box" style=" font:14px/1.125 arial; text-align:center; padding:0; color:#333; margin: 0;">
        <div class="invite_pop_image_2"></div>
        <b>邀请已发送！</b>
        <p style="padding:10px 0 20px;">点击下方“复制链接”按钮，将复制的文字通过QQ号 发给你想邀请的人。</p>
        <div>
            <textarea readonly="readonly" id="copy_info_url" class="w-int" style="width: 400px; height:45px; line-height: 15px;">一起作业PK大改版，《走遍美国》、《通天塔》免费开放！提高成绩又好玩！请在24小时内点击下面链接登录，记得先勾选我再去完成作业哟~www.17zuoye.com</textarea>
        </div>
        <div style="padding: 10px 0 0; position: relative; width: 118px; margin: 0 auto;"><a href="javascript:void(0);" class="w-btn w-btn-green" id="clip_container"><strong id="clip_button">复制链接</strong></a></div>
    </div>
</script>



<script type="text/javascript">

    function selectAlluser(state) {
        $('#show_student_list li').each(function () {
            var $that = $(this).find('.selectStudentCheckbox');
            if (state) {
                $that.addClass('checkboxs_active');
            } else {
                $that.removeClass('checkboxs_active');
            }
        });
    }

    $(function () {
        //激活同学
        $("#invite_student_but").on('click', function () {
            var checkStudent = $("#show_student_list li i.checkboxs_active");
            var students = [];
            checkStudent.each(function () {
                var $that = $(this);
                students.push({
                    userId: $that.data('student_id'),
                    userName: $that.data('student_name'),
                    userAvatar: $that.data('student_img'),
                    type: $that.data('student_active_type')
                });
            });
            if (students.length == 0) {
                $17.alert("选择你要激活的学生。");
                return false;
            }
            App.postJSON("/student/invite/studentactivatestudent.vpage", {userList: students, classId: "${clazzId!''}"}, function (data) {
                if (data.success) {
                    $.prompt(template("t:成功提示框", {}), {
                        title: "系统提示",
                        buttons: {},
                        position:{width : 520},
                        loaded : function(){
                            //复制链接
                            $17.copyToClipboard($("#copy_info_url"), $("#clip_button"), "clip_button", "clip_container");
                        }
                    });
                } else {
                    $17.alert(data.info);
                }
            });
        });

        //选择要激活的同学
        $('#show_student_list').on('click', 'li', function () {
            $(this).find('.selectStudentCheckbox').toggleClass('checkboxs_active');

            //全选状态标记
            var practiceTypeCount = $('#show_student_list ul li').length;
            var practiceTypeSelectCount = $('#show_student_list ul li i.checkboxs_active').length;
            var select_all_but = $("#selectAllStudents span i");
            if (practiceTypeCount != practiceTypeSelectCount) {
                select_all_but.removeClass('checkboxs_active');
            } else {
                select_all_but.addClass('checkboxs_active');
            }
        });

        //全选
        $("#selectAllStudents").click(function () {
            var i = $(this).find('i');
            if (i.hasClass('checkboxs_active')) {
                i.removeClass('checkboxs_active');
                selectAlluser(false);
            } else {
                i.addClass('checkboxs_active');
                selectAlluser(true);
            }
        });
    });
</script>
</@com.page>