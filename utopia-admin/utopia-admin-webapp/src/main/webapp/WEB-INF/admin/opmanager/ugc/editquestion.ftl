<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=9>
<div id="main_container" class="span9">
    <legend>添加编辑UGC题目</legend>
    <div class="row-fluid">
        <div class="span12">
            <fieldset>
                <div class="control-group">
                    <label class="control-label">题目类型：</label>
                    <div class="controls">
                        <#if types??>
                            <select id="questionType" name="questionType">
                                <#list types as t >
                                    <option value="${t.name()!}" <#if question?? && (question.questionType == t.name())>
                                            selected </#if>>${t.name()!}</option>
                                </#list>
                            </select>
                        </#if>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="questionName">题干：</label>
                    <div class="controls">
                        <textarea name="questionName" id="questionName"
                                  style="width: 400px;height: 120px"><#if question??>${question.questionName!''}</#if>
                        </textarea>
                        <p style="color: red">
                            *说明：如果是填空题，则将题干部分文字正常输入，所需填空的地方用 #INPUT# 代替，
                            如果是比较长的填空题，则用 #LONG_INPUT# 代替。
                            例如：我的学校叫做#LONG_INPUT#，我的老师是#INPUT#老师
                            如果是单选题用#SELECT#代替，多选的话用#MULTISELECT#代替。
                            如果题干部分需要动态替换的则用特定代码替换，目前支持动态显示用户年级，代码#CLAZZLEVEL#
                            例如：#CLAZZLEVEL#的班级是从#INPUT#班到#INPUT#班
                            #CLAZZLEVEL#用户看到的格式为：“二年级”
                        </p>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="questionOptions">题目选项：</label>
                    <div class="controls">
                        <textarea name="questionOptions" id="questionOptions" style="width: 400px;height: 120px"><#if question??>${question.questionOptions!''}</#if>
                        </textarea>
                        <p style="color: red">
                            *说明：选择题此处需要输入选项，多个选项用逗号分隔即可
                            例如：1,2,3,4（四个选项的文本）
                        </p>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">是否删除：</label>
                    <div class="controls">
                        <input type="checkbox" id="disabled" name="disabled"
                               <#if question?? && question.disabled>checked="checked"</#if>/>
                    </div>
                </div>
            </fieldset>
            <div class="modal-footer">
                <button id="saveRuleBtn" class="btn btn-primary">确 定</button>
            </div>

        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $("#saveRuleBtn").on("click", function () {
            var questionMapper = {
                questionType: $("#questionType").val(),
                questionName: $("#questionName").val(),
                questionOptions: $("#questionOptions").val(),
                disabled: $("#disabled").prop("checked")
                <#if question??>, questionId: '${(question.id)!''}'</#if>
            };
            if (questionMapper.questionName == undefined || questionMapper.questionName.trim() == '') {
                alert("请输入活动名称");
                return false;
            }
            $.ajax({
                type: "post",
                url: "savequestion.vpage",
                data: questionMapper,
                success: function (data) {
                    if (data.success) {
                        window.location.href = 'questionindex.vpage';
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>