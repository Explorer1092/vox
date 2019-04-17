<#--headerTabTemp-->
<#--tempOptions.headerTab = {
    headerText:"排行榜",
    liList:[
            {
                linkUrl:"link1",
                divText:"新增排行榜"
            },
            {
                linkUrl:"link2",
                divText:"高覆盖排行榜"
            },
            {
                linkUrl:"link3",
                divText:"双科排行榜"
            }
    ]
};-->
<script id="headerTabTemp" type="text/x-handlebars-template">
    {{#if headerTab }}
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        {{#if headerTab.headerLink }}
            <a href="{{headerTab.headerLink}}" class="hdLink">
        {{else}}
            <a href="javascript:void(0);" class="hdLink">
        {{/if}}
        {{#if headerTab.headerText }}
            <span class="side-fl">{{headerTab.headerText}}</span>
        {{/if}}
        {{#if headerTab.headerDesc }}
            <span class="side-fr side-time">{{{headerTab.headerDesc}}}</span>
        {{/if}}
        </a>
        <div class="mobileCRM-V2-rankInfo">
            <div class="infoBox">
                {{#each headerTab.liList }}
                    {{#if linkUrl}}
                        <a href="{{linkUrl}}">
                    {{else}}
                        <a href="javascript:void(0);">
                    {{/if}}
                        {{#if divCount}}
                            <div class="boxNum">{{divCount}}</div>
                        {{/if}}
                        {{#if divText}}
                            <div class="boxFoot">{{divText}}</div>
                        {{/if}}
                    </a>
                {{/each}}
            </div>
        </div>
    </div>
        {{#if headerTab.withMore}}
            <ul class=" mobileCRM-V2-list" style=" border-top: 1px #f0eff5 solid; ">
                {{#each headerTab.moreLiList}}
                    <li>
                        {{#if linkUrl}}
                            <a href="{{linkUrl}}" class="link link-ico">
                        {{else}}
                            <a href="javascript:void(0);" class="link">
                        {{/if}}
                            <div class="side-fl">{{divText}}</div>
                        </a>
                    </li>
                {{/each}}
            </ul>
        {{/if}}
    {{/if}}
</script>