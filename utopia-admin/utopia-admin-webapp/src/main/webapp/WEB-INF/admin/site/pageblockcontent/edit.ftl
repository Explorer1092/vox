<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="页面内容编辑" page_num=4>
    <!--/span-->
    <div class="span9">
        <form method="post" action="?id=${pageBlockContent.id!''}" class="form-inline">

            <ul class="inline">
                <li>
                    <label>
                        开始时间
                        <input type="text" name="startDatetime" value="${(pageBlockContent.startDatetime?datetime)!''}" />
                    </label>
                </li>
                <li>
                    <label>
                        结束时间
                        <input type="text" name="endDatetime" value="${(pageBlockContent.endDatetime?datetime)!''}" />
                    </label>
                    <label><input type="checkbox" name="disabled" value="true" ${(pageBlockContent.disabled?string('checked', ''))!''} /> 禁用</label>
                    <input type="hidden" name="disabled" value="false" />
                </li>
                <li>
                    <label>
                        页面
                        <input type="text" name="pageName" value="${(pageBlockContent.pageName)!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">

                <li>
                    <label>
                        显示次序
                        <input type="text" name="displayOrder" value="${(pageBlockContent.displayOrder)!''}" />
                    </label>
                </li>
                <li>
                    <label>
                        位置
                        <input type="text" name="blockName" value="${(pageBlockContent.blockName)!''}" />
                    </label>
                </li>
                <li>
                    <label>
                        备注
                        <input type="text" name="memo" value="${(pageBlockContent.memo)!''}" />
                    </label>
                </li>
            </ul>

           <p>
               <textarea name="content" style="width: 100%; height: 600px;">${(pageBlockContent.content?html)!''}</textarea>
           </p>

            <p><input type="submit" class="btn btn-primary btn-large" value="提交" /></p>
        </form>
    </div>
    <!--/span>
</@layout_default.page>