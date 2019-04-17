<#include "./constants.ftl">
<#function getShopIconSrc shopIconName>
	<#local
		productImgSuffix = {
			"a17zyspg"       : "jpg",
			"afentiexam"     : "png?v=1",
            "afentimath"     : "png",
			"iandyou100"     : "png",
			"sanguodmz"      : "jpg",
			"travelamerica"  : "png",
			"walker"         : "png",
            "walkerelf"      : "png",
			"stem101"        : "png",
			"petswar"        : "png",
			"wukongshizi"    : "png",
			"wukongpinyin"   : "png",
			"greatadventure" : "png"
		}
		baseImgSrc = "/shopList/"
        shopIconNameLowerCase = (shopIconName!"")?lower_case?trim
        imgSuffix = (productImgSuffix[shopIconNameLowerCase]!"")
        imgName = (imgSuffix == "") ?string("", shopIconNameLowerCase + "." + imgSuffix)
        imgSrc = (imgName == "")?string("", '${buildStaticFilePath(baseImgSrc + imgName, "img")}')
    >
    <#return imgSrc?trim>
</#function>

<#function isSupportMobile shopIconName>
    <#local isCanSupportMobileProducts = [
			"afentiexam",
			"greatadventure",
            "afentimath",
			"usaadventure",
			"afentichinese",
			"encyclopediachallenge",
			"chinesesynpractice",
			"arithmetic"
		]
		shopIconNameLowerCase = (shopIconName!"")?lower_case?trim
    >
    <#return isCanSupportMobileProducts?seq_index_of(shopIconNameLowerCase) gt -1>
</#function>

<#function isNotSupportPc shopIconName>
    <#local isNotSupportPcProducts = [
			"greatadventure",
			"usaadventure",
			"encyclopediachallenge",
			"arithmetic"
	]
		shopIconNameLowerCase = (shopIconName!"")?lower_case?trim
    >
    <#return isNotSupportPcProducts?seq_index_of(shopIconNameLowerCase) gt -1>
</#function>

<#function isNotSupportJxt shopIconName>
	<#local isNotSupportJxt = [
		"greatadventure"
	]
	shopIconNameLowerCase = (shopIconName!"")?lower_case?trim
	>
	<#return isNotSupportJxt?seq_index_of(shopIconNameLowerCase) gt -1>
</#function>

<#function getShopDefaultPeriod shopIconName>
    <#local
		periods = {
			"a17zyspg" : 30,
			"afentiexam" : 90,
			"afentimath" : 90,
			"afentichinese" : 90,
            "walker" : 30,
            "travelamerica" : 90
		}

		shopIconNameLowerCase = (shopIconName!"")?lower_case?trim
    >
    <#return periods[shopIconNameLowerCase]!-1>
</#function>
