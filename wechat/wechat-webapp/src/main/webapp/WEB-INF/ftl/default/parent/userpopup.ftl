<div class="title_box_tb_img">
    <a id="popup_children" href="javascript:void (0);" style="width: auto; display: inline-block;">
        <img src="" style="height: 70px; width: 70px; border-radius: 50%;" id="studentImg_b">
        <span id="studentName_b" class="name"></span><i class="icon icon_5"></i>
    </a>
</div>
<div class="name_more" id="studentsListBox" data-student='${json_encode(students![])}' style="display: none;">
    <ul style="line-height: 60px;">
        <#if students?? && students?size gt 0>
            <#list students as student>
                <a class="select_student_but" data-student_id="${student.id!0}" data-img="<@app.avatar href="${student.img!}"></@app.avatar>" href="javascript:void (0);"  data-student_index="${student_index}">
                    <li style="border-bottom:1px solid #E7E7E7;">${student.name?has_content?string('${student.name}','${student.id!0}')}</li>
                </a>
            </#list>
        </#if>
    </ul>
</div>

