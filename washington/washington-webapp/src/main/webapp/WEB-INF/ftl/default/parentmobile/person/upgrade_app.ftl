<#import '../layout.ftl' as layout>

<@layout.page className='TrustUpgrade parentApp-bgColor' pageJs="no_require_module" title="版本升级"  extraJs=extraJs![] >
    <#escape x as x?html>
		<body class="parentApp-bgColor">
			<div class="parentApp-upgradeProm">当前版本过低，升级后立即查看更多内容</div>
			<a href="//wx.17zuoye.com/download/17parentapp?cid=202004" class="parentApp-upgradeBtn">立即升级</a>
		</body>
    </#escape>
</@layout.page>
