<#--mobile版 头部模板 TO-DO 可兼容多配置-->
<#--参数说明
var headerOption = {
    headerTitle:'首页',
    headerBackLink: "javascript:window.history.back();",
    headerBackText:"&lt;&nbsp;返回",
    headerBtnLink: "/mobile/performance/index.vpage",
    headerBtnAttribute:{id: "schoolLevel", name: "schoolLink" },
    headerBtnText:"提交"
}

直接在js中调用
new compileTemp(headerOption);

-->
<script id="headerTemp" type="text/x-handlebars-template">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                {{#if header.headerBackLink}}
                    <div class="headerBack">
                        <a href="{{#if header.headerBackLink}}
                                    {{ header.headerBackLink }}
                                 {{/if}}">
                            {{#if header.headerBackText }}
                                {{{ header.headerBackText }}}
                            {{/if}}</a>
                    </div>
                {{/if}}
                <div class="headerText">{{{ header.headerTitle }}}</div>
                    {{#if header.headerBtnText}}
                    <div class="headerBtn">
                            <a
                                    {{#if header.headerBtnAttribute}}
                                        {{#each header.headerBtnAttribute}}
                                            {{@key}} = {{this}}
                                        {{/each}}
                                    {{/if}}
                            href="{{#if header.headerBtnLink}}
                                    {{header.headerBtnLink}}
                                  {{/if}}">
                            {{header.headerBtnText}}</a>
                    </div>
                    {{/if}}
            </div>
        </div>
    </div>
</script>
