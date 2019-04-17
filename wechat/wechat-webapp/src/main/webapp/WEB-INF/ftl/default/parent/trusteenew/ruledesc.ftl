<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='' pageJs="">
    <@sugar.capsule css=['trusteetwo'] />
    <#assign ruleIntroduction= {
        "14": {
            "introduction": ["预约成功后，机构会与您进行联系，约定体验时间，需在预定时间到指定机构体验，过期需要重新协商体验时间。","托管班报名时，必须在平台支付，方可享受优惠。","仅限于目前还没有在鑫晨阳教育上托管的学生"],
            "name":"鑫晨阳教育"
        },
        "15": {
            "introduction": ["预约成功后，机构会与您进行联系，约定体验时间，需在预定时间到指定机构体验，过期需要重新协商体验时间。","托管班报名时，必须在平台支付，方可享受优惠。","仅限于目前还没有在玥龙教育上托管的学生"],
            "name":"玥龙教育"
        },
        "16": {
            "introduction": ["预约成功后，机构会与您进行联系，约定体验时间，需在预定时间到指定机构体验，过期需要重新协商体验时间。","托管班报名时，必须在平台支付，方可享受优惠。","仅限于目前还没有在贵族子弟上托管的学生"],
            "name":"贵族子弟"
        },
        "17": {
            "introduction": ["预约成功后，机构会与您进行联系，约定体验时间，需在预定时间到指定机构体验，过期需要重新协商体验时间。","托管班报名时，必须在平台支付，方可享受优惠。","日常午、晚、全托仅限每周二体验","仅限于目前还没有在乾玺国际教育上托管的学生"],
            "name":"乾玺国际教育托管中心"
        },
        "18": {
            "introduction": ["预约成功后，机构会与您进行联系，约定体验时间，需在预定时间到指定机构体验，过期需要重新协商体验时间。","托管班报名时，必须在平台支付，方可享受优惠。","仅限于目前还没有在爱德教育上托管的学生"],
            "name":"爱德教育"
        },
        "19": {
            "introduction": ["预约成功后，机构会与您进行联系，约定体验时间，需在预定时间到指定机构体验，过期需要重新协商体验时间。","托管班报名时，必须在平台支付，方可享受优惠。","仅限于目前还没有在伊河路小学书香源上托管的学生"],
            "name":"伊河路小学书香源"
        },
        "20": {
            "introduction": ["预约成功后，机构会与您进行联系，约定体验时间，需在预定时间到指定机构体验，过期需要重新协商体验时间。","托管班报名时，必须在平台支付，方可享受优惠。","仅限于目前还没有在乐思教育上托管的学生"],
            "name":"乐思教育"
        }

    }>
    <#assign _shopId = shopId?string!"14">
    <div class="active-wrap active-bgyellow">
        <div class="active04-box">
            <#list ruleIntroduction[_shopId].introduction as introduction>
                <div class="list">
                    <span>${introduction_index + 1}</span>
                    <p>${introduction}</p>
                </div>
            </#list>
        </div>
    </div>

    <script>
        ga('trusteeTracker.send', 'pageview');
    </script>
</@trusteeMain.page>