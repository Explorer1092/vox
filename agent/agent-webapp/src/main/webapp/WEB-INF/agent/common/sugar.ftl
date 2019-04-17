<#macro capsule js=[] css=[] block=[] cdn=false>
    <#compress>
        <#list css as name>
            <#switch name>
                <#case 'loginBase'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/auth/loginBase.css"/>
                <#break>
                <#case 'new_home'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/home/new_home.css"/>
                <#break>
                <#case 'intoSchool'>
                <link rel="stylesheet" href="/public/rebuildRes/css/mobile/home/intoSchool.css"/>
                    <#break>
                <#case 'home'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/home/home.css"/>
                <#break>
                <#case 'team'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/team/team.css"/>
                    <#break>
                <#case 'record'>
                <link rel="stylesheet" href="/public/rebuildRes/css/mobile/workRecord/record.css"/>
                    <#break>
                <#case 'work_statistic'>
                <link rel="stylesheet" href="/public/rebuildRes/css/mobile/workRecord/work_statistic.css"/>
                    <#break>
                <#case 'school'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/intoSchool/school.css"/>
                    <#break>
                <#case 'notice'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/notice/notice.css"/>
                    <#break>
                <#case 'feedback'>
                <link rel="stylesheet" href="/public/rebuildRes/css/mobile/feedback/feedback.css"/>
                    <#break>
                <#case 'swiper3'>
                    <link rel="stylesheet" href="/public/rebuildRes/lib/swiper3/css/swiper.min.css"/>
                <#break>
                <#case 'res'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/res/res.css"/>
                    <#break>
                <#case 'audit'>
                <link rel="stylesheet" href="/public/rebuildRes/css/mobile/audit/audit.css"/>
                    <#break>
                <#case 'custSer'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/res/custSer.css"/>
                    <#break>
                <#case 'skin'>
                    <link rel="stylesheet" href="/public/css/business/skin.css"/>
                    <#break>
                <#case 'intoSchoEffeNew'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/intoSchool/intoSchoEffeNew.css"/>
                    <#break>
                <#case 'researchers'>
                    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/researchers/researchers.css"/>
                    <#break>
                <#case 'analysis'>
                <link rel="stylesheet" href="/public/rebuildRes/css/mobile/intoSchool/analysis.css"/>
                    <#break>
                <#case 'photo_pic'>
                <link rel="stylesheet" href="/public/css/business/photo_pic.css"/>
                <#case 'rewardLogistics'>
                 <link rel="stylesheet" href="/public/rebuildRes/css/mobile/rewardLogistics/rewardLogistics.css"/>
                <#break>
            </#switch>
        </#list>

        <#list js as name>
            <#switch name>
                <#case "jquery">
                    <script type="application/javascript" src="/public/rebuildRes/lib/jquery/jquery-1.9.1.min.js"></script>
                <#break>
                <#case "requirejs">
                    <script type="application/javascript" src="/public/rebuildRes/lib/requirejs/require.min.js"></script>
                <#break>
                <#case "fastclick">
                    <script type="application/javascript" src="/public/rebuildRes/lib/fastclick/fastclick.min.js"></script>
                <#break>
            </#switch>
        </#list>
    </#compress>
</#macro>