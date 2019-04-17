<#include "./index.ftl" />

<div class="container">
    <legend>${pageMessage!''}</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <#if entry?? >
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>路径</td>
                        <td>所属业务系统App</td>
                        <td>功能操作名称</td>
                        <td>角色</td>
                    </tr>
                    <tr>
                        <td>${entry.pathName!}</td>
                        <td>${entry.appName!}</td>
                        <td>${entry.appDescription!}</td>
                        <td>${entry.roleName!}</td>
                    </tr>
                </table>
                </#if>
                <form id="form1" name="form1" method="post" action="" >
                   <#if includeUrl??><#include includeUrl! /></#if>
                    <div class="">
                        <button type="submit" class="btn btn-primary">${pageMessage!''}</button>
                        <button type="reset" class="btn" onclick="javascript:history.go(-1);">取消</button>
                        <input type="hidden" name="next" value="{{ request.GET.next }}" />
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

