<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='托管班介绍' pageJs="trusteeclazzdesc">
    <@sugar.capsule css=['trusteetwo'] />
    <#assign introduction= {
        "14": {
            "introduction": "鑫晨阳教育提供精致午托，专业晚辅服务，在专业老师的晚辅导下，让孩子养成良好的学习习惯，提升学习效率，有爱心、有耐心、赢得家长信赖和口碑。",
            "name":"鑫晨阳教育",
            "mobile":"13923783049",
            "title_1":"地理位置：距离桥头小学500米 ",
            "title_2":"桥头小学有150名学生在机构托管",
            "img_top" : 'public/images/parent/trustee/map/xcy.jpg',
            "img_left" : 'public/images/parent/trustee/detail/xcy_l.jpg',
            "img_right" : 'public/images/parent/trustee/detail/xcy_r.jpg',
            "word_left" :"经验丰富的教师提供作业辅导",
            "word_right" :"宽敞舒适的环境"
        },
        "15": {
            "introduction": "玥龙教育机构开办于2011年9月1日，位于兴华宾馆上步片区，公司50米内拥有南园小学及红岭中学两所校区，目前公司培训项目涉及小学托管项目、国际象棋班、手恼速算班系列、一对一小学语数英辅导、绘画及周末作业辅导班等项目。",
            "name":"玥龙教育",
            "mobile":"18566762327",
            "title_1":"距离南园小学120米",
            "title_2":"南园小学有60名学生在机构托管",
            "img_top" : 'public/images/parent/trustee/map/yl.jpg',
            "img_left" : 'public/images/parent/trustee/detail/yl_l.jpg',
            "img_right" : 'public/images/parent/trustee/detail/yl_r.jpg',
            "word_left" :"环境整洁",
            "word_right" :"专业培训帮助提升孩子综合素质"
        },
        "16": {
            "introduction": "贵族子弟是福永宣传部支持的唯一一家教育机构，成立于2003年，十年来，一直致力于文化和艺术的优质培训，拥有一支文化能力强，教学方法独特的教师团队。",
            "name":"贵族子弟",
            "mobile":"18926024485",
            "title_1":"地理位置：距离福永街道中心小学300米",
            "title_2":"福永街道中心小学有40名学生在机构托管",
            "img_top" : 'public/images/parent/trustee/map/gzzd.jpg',
            "img_left" : 'public/images/parent/trustee/detail/gz_l.jpg',
            "img_right" : 'public/images/parent/trustee/detail/gz_r.jpg',
            "word_left" :"经营多年，师资力量雄厚",
            "word_right" :"环境整洁"
        },
        "17": {
            "introduction": "乾玺国际教育托管中心拥有600平米超大空间，有专业的舞蹈室、钢琴练习房和舒适午休室。电子签到、实时监控，随时了解孩子的动向。",
            "name":"乾玺国际教育托管中心",
            "mobile":"13925208335",
            "title_1":"寒假托管班课程表",
            "title_2":"新洲小学、新沙小学、众孚小学、绿洲小学已有10名学生在机构托管",
            "img_top" : 'public/images/parent/trustee/course/qx.jpg',
            "img_left" : 'public/images/parent/trustee/detail/qx_l.jpg',
            "img_right" : 'public/images/parent/trustee/detail/qx_r.jpg',
            "word_left" :"600平米超大空间",
            "word_right" :"环境优美，干净整洁"
        },
        "18": {
            "introduction": "爱德是一家专业的午托、晚托、同步课程辅导的教育机构，在这里有整洁的宿舍和教室；饮食讲究科学营养搭配；有专职的上学、放学、看护人员；老师是兼教师、家长、保姆、朋友的四重身份证，给孩子德、智、体、劳等全方面的教育。",
            "name":"爱德教育",
            "mobile":"13265777761",
            "title_1":"爱德教育寒假托管课程表",
            "title_2":"前海学校小学部有40名学生在机构托管",
            "img_top" : 'public/images/parent/trustee/course/adjy.jpg',
            "img_left" : 'public/images/parent/trustee/detail/adjy_l.jpg',
            "img_right" : 'public/images/parent/trustee/detail/adjy_r.jpg',
            "word_left" :"爱德教育，爱的托付",
            "word_right" :"舒适的学习环境让孩子更好的学习"
        },
        "19": {
            "introduction": "伊河路小学书香源位于伊河路小学旁，教学场所环境正规，标准化管理，专业精品8-10人作业班为重点产品,为学生、家长提供规范化、高质量的一站式服务。",
            "name":"伊河路小学书香源 ",
            "mobile":"67976388",
            "title_1":"好习惯训练营课表",
            "title_2":"伊河路小学有170名学生在机构托管",
            "img_top" : 'public/images/parent/trustee/course/sxy.jpg',
            "img_left" : 'public/images/parent/trustee/detail/sxy_l.jpg',
            "img_right" : 'public/images/parent/trustee/detail/sxy_r.jpg',
            "word_left" :"寒假好习惯感恩训练营",
            "word_right" :"经验丰富的教师团队"
        },
        "20": {
            "introduction": "乐思教育由毕业于211,985高校的老师创办，致力于帮助孩子培养良好的学习习惯，建立高效的学习方法，提高学习效率。现开设晚托，午托，新概念英语，奥数，单词拼读，拼音辅导。",
            "name":"乐思教育",
            "mobile":"13590389001",
            "title_1":"地理位置：距离西丽小学50米",
            "title_2":"西丽小学有30名学生在机构托管",
            "img_top" : 'public/images/parent/trustee/map/ls.jpg',
            "img_left" : 'public/images/parent/trustee/detail/ls_l.jpg',
            "img_right" : 'public/images/parent/trustee/detail/ls_r.jpg',
            "word_left" :"专业教师提供作业辅导",
            "word_right" :"教学秩序井然，提高学习效率"
        }
    }>
    <#assign _shopId = (shopId?string)!"14">
    <div class="active-wrap active-bgpink">
        <div class="active03-box active-spacing">
            <div class="ab02-box-1">为您匹配到学校附近的托管机构</div>
            <div class="ab02-box-2">
                <span class="add">${(introduction[_shopId].name)!'--'}</span>
                <span class="tel">${(introduction[_shopId].mobile)!'--'}</span>
            </div>
            <div class="ab02-box-3">${(introduction[_shopId].introduction)!'--'}</div>
            <div class="ab02-box-4">
                <h2><span>1</span>${(introduction[_shopId].title_1)!'--'}</h2>
                <div class="box">
                    <img src="<@app.link href="${(introduction[_shopId].img_top)!'--'}"/>" alt="">
                </div>
            </div>
            <div class="ab02-box-4 ab02-box-4a js-evenBox">
                <h2><span>2</span>${(introduction[_shopId].title_2)!'--'}</h2>
                <div class="box box-fl">
                    <img src="<@app.link href="${(introduction[_shopId].img_left)!'--'}"/>" alt="">
                    <p>${(introduction[_shopId].word_left)!'--'}</p>
                </div>
                <div class="box box-fr">
                    <img src="<@app.link href="${(introduction[_shopId].img_right)!'--'}"/>" alt="">
                    <p>${(introduction[_shopId].word_right)!'--'}</p>
                </div>
            </div>
            <div class="footer">
                <div class="empty"></div>
                <a href="javascript:void(0);" class="active-bottom-know order_icon js-goToBookBtn">去预约体验</a>
            </div>
        </div>
    </div>
    <script>
        var uid = ${currentUserId!0000};
        ga('trusteeTracker.send', 'pageview');
    </script>
</@trusteeMain.page>