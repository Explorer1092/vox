<#import "../../layout/layout.ftl" as temp />
<#macro page title="register">
    <@temp.page pageName=''>
    <@sugar.capsule js=["ZeroClipboard"] css=["teacher.invite"] />
    <style>
        .tabsMain li.active span {
            color: #5eac14;
        }

        .intiveBox {
            margin: 50px 35px 0;
        }

        .intiveBox .intiveSelect {
            margin: 0 auto;
        }
    </style>
    <div class="t-center-container">
        <div class="t-center-slide w-fl-left">
            <span class="leaf leaf-1"></span>
            <span class="leaf leaf-2"></span>
            <span class="leaf leaf-3"></span>
            <span class="leafcope"></span>
            <span class="ts-top"></span>

            <div class="ts-center">
                <ul>
                    <#if hasTeachers?? && !hasTeachers>
                        <li <#if title=="register">class="active"</#if>>
                            <a href="/student/invite/register.vpage">邀请注册</a>
                        </li>
                    </#if>
                </ul>
            </div>
            <div class="ts-bottom"></div>
        </div>
        <div class="t-center-box w-fl-right">
            <span class="center-rope"></span>
            <span class="center-rope center-rope-1"></span>
            <div class="intiveBox ">
                <#nested />
            </div>
        </div>
        <div class="w-clear"></div>
    </div>
    </@temp.page>
</#macro>
