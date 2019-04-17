<#--ulItemTemp-->
<#--tempOptions.ulItem = {
    default :{
        liList:[
            {
                linkUrl:"/mobile/work_record/region_member_record.vpage",
                divText:"区域工作情况"
            }
        ]
    },
    //支持扩展，单个模板扩展多份页面内容
    extension:{
        liList0:[
            {
                linkUrl:"/mobile/school_clue/new_school_clue.vpage",
                divClass:"side-fl side-orange",
                divText:"新学校线索"
            },
            {
                linkUrl:"/mobile/workbench/user_apply.vpage",
                divText:"我的申请"
            }
        ],
        liList1:[

            {
                linkUrl:"/mobile/work_record/school_sop.vpage",
                divText:"进校攻略 - SOP"
            }
        ],
        liList2:[
            {
                linkUrl:"setting.vpage",
                divText:"设置"
            }
        ]
    }
};-->
<script id="ulItemTemp" type="text/x-handlebars-template">
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    {{#if ulItem.default}}
        {{#if ulItem.default.headerText}}
            <div class="hdText">{{ ulItem.default.headerText }}</div>
        {{/if}}
        {{#if ulItem.default.className}}
            <ul class="{{ ulItem.className }}">
        {{else}}
            <ul class="mobileCRM-V2-list">
        {{/if}}
            {{#each ulItem.default.liList}}
                <li>
                    {{#if linkUrl }}
                        <a href="{{ linkUrl }}" class="link link-ico">
                    {{else}}
                        <a href="javascript:void(0)" class="link">
                    {{/if}}
                        {{#if divText}}
                        <div class="{{#if divClass }} {{ divClass}} {{else}} side-fl {{/if}}">{{ divText }}</div>
                        {{/if}}
                    </a>
                </li>
            {{/each}}
            </ul>
    {{/if}}
</div>
</script>