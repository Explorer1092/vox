<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="${clazzName!}" pageJs="editClazz">
    <@sugar.capsule css=["clazzManage"] />
<style>
    .move_btn{
        display:inline-block;
        width:1.7rem;
        height:1rem;
        background:#fff;
        border-radius:10px;
        position:absolute;
        top: 0.05rem;
    }
    .sc-left{
        left:0.05rem;
    }
    .sc-right{
        right:0.05rem;
    }
    .show_alink{
        background:url(<@app.link href="public/images/helping.png"/>)  no-repeat;
        background-size:100%;
        width:0.7rem;
        height:0.7rem;
        display:inline-block;
    }
</style>
    <div class="cTab">
        <#if isFakeTeacher?? && !isFakeTeacher>
            <a href="javascript:void(0);" class="tabList" data-bind="click: transferBtn">
                <i class="tabIcon tabIcon01"></i>
                <p class="text">转让班级</p>
            </a>
        </#if>

        <a data-bind="click: addTeacherBtn" href="javascript:void (0);" class="tabList">
            <i class="tabIcon tabIcon02"></i>
            <p class="text">添加老师</p>
        </a>
        <#--<a data-bind="click: $root.tinyGroupBtn" href="javascript:void (0);" class="tabList">
            <i class="tabIcon tabIcon03"></i>
            <p class="text">小组管理</p>
        </a>-->
        <a data-bind="click: $root.clazzIntegralBtn" href="javascript:void (0);" class="tabList">
            <i class="tabIcon tabIcon04"></i>
            <p class="text">班级学豆</p>
        </a>
    </div>
    <#--<#if (displayShowRank)!false>-->
    <#if false> <#-- #39806 下线学生端排行榜-->
        <div class="show_onoff">
            <span style="font-size:0.7rem; margin-left:1.5rem">显示学生端排行榜</span>
            <a class="show_alink"href="http://help.17zuoye.com/?page_id=1467"></a>
            <div data-clazzid="${clazzId!}" class="JS-move_btn01" style="cursor:pointer;display:inline-block;width:3rem;height:1.1rem;line-height:1rem;background:#269ef8;border-radius:10px;position:absolute;right:5%;margin-top:0.4rem">
                <span style="color:#fff;font-size:60%">ON</span>
            <#if showRank>
                <div class="move_btn sc-right"></div>
            <#else>
                <div class="move_btn sc-left"></div>
            </#if>
                <span style="position:absolute;right:3%;color:#fff;font-size:60%">OFF</span>
            </div>
        </div>
    </#if>
    <div class="cMain" data-bind="visible: $data.studentsDetail" style="display: none;">
        <ul class="cList">
            <!-- ko foreach : {data : $data.studentsDetail, as : '_student'} -->
            <li>
                <a class="list-cell" href="javascript:void (0);" data-bind="attr: {'href': '/teacher/clazzmanage/editstudent.vpage?studentId='+_student.studentId()}">
                    <span class="stu-name" data-bind="text: _student.studentName() ||  _student.studentId()">--</span>
                </a>
            </li>
            <!--/ko-->
        </ul>
    </div>
    <div class="cFooter">
        <div class="footerInner">
            <div class="btnBox">
                <a href="javascript:void (0);" data-bind="click: inviteStudentBtn" class="btn">邀请学生加入</a>
            </div>
        </div>
    </div>

</@activityMain.page>
<script type="text/javascript">
    var $showRank = ${(showRank!false)?string};

</script>