<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="VOX_HOMEWORK_STUDENT_AUTH_DICT EDIT" page_num=4>
    <!--/span-->
<fieldset>
    <legend>VOX_HOMEWORK_STUDENT_AUTH_DICT</legend>
</fieldset>
    <div class="span9">
        <form method="post" action="?id=${homeworkStudentAuthDict.id!''}" class="form-inline">

            <ul class="inline">
                <li>
                    <label>
                        HomeworkType
                        <input type="text" name="homeworkType" value="${homeworkStudentAuthDict.homeworkType!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        HomeworkType_name
                        <input type="text" name="homeworkTypeName" value="${homeworkStudentAuthDict.homeworkTypeName!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        HomeworkFormType
                        <input type="text" name="homeworkFormType" value="${homeworkStudentAuthDict.homeworkFormType!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        Homeworkform_name
                        <input type="text" name="homeworkFormName" value="${homeworkStudentAuthDict.homeworkFormName!''}" />
                    </label>
                </li>
            </ul>

            <p><input type="submit" class="btn btn-primary btn-large" value="提交" /></p>
        </form>
    </div>
    <!--/span>
</@layout_default.page>