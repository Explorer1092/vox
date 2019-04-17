<#import "../layout.ftl" as ucenter>
<@ucenter.page title='我的孩子' pageJs="">
    <@sugar.capsule css=['ucenterchildren'] />
<div class="t-myChild-box">
    <#if childrenInfo?? && childrenInfo?size gt 0>
        <#list childrenInfo as child>
            <div class="myChild-column">
                <dl>
                    <dt><img src="<@app.avatar href='${child.childAvatar!}'/>"/></dt>
                    <dd>
                        <p><span class="name">${child.childName!''}</span>：${child.childId!''}</p>
                        <p><span>学豆</span>：${child.childIntegral!0}</p>
                    </dd>
                </dl>
            </div>
        </#list>
        <#if childrenInfo?size lt 3>
            <div class="btn">
                <a href="/parent/ucenter/bindchild.vpage" class="btn-addChild">
                    <i class="addChild-icon"></i>
                    添加一个孩子
                </a>
            </div>
        </#if>
        <div style="text-align: center;color: #bcbfbf;">提示：最多只能绑定3个孩子。</div>
    <#else>
        <div class="main body_background" style="height: 30%;">
            <h1 class="logo"></h1>
        </div>
        <div style="text-align: center; margin-top: 50px;">
            暂无孩子列表
        </div>
    </#if>
</div>
</@ucenter.page>