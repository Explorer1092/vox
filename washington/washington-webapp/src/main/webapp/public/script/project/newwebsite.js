define(['jquery', 'knockout', 'jqPaging'], function ($, ko) {


    /* 新官网首页 */
    var productList = $('.JS-productServer');
    productList.mouseover(function () {
        $('.JS-productWrap').show();
    });
    productList.mouseout(function () {
        $('.JS-productWrap').hide();
    });

    var _height = $('body').height() - 70;
    /* 首页 */
    $('.education-dream-works').height(_height + 'px');

    /* 关于一起 */
    $('.education-wrap .md-banner').height(_height + 'px');


    /* 一起小学 */
    $('.center-banner').height(_height + 'px');


    /* 一起公益 */
    var city_item  = $('.city-box a'),
        imageUrl = $('.l-pic img'),
        title = $('.r-text h5'),
        text = $('.r-text p'),
        currentIndex = 0;

    var text_list = [
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2017年，携手小小铅笔（第4季）爱心计划',
            inTxt: '携手敬一丹老师与华泰保险集团联合发起“小小铅笔（第4季）爱心计划”，帮助扶贫困学生补充优质教育资源，通过现代科技技术，让留守儿童都能够享受和体验高水平的学习。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2017年，凉山地区教育助学',
            inTxt: '携手中华儿慈会向四川省凉山地区开展多项“教育助学”活动，用于解决该地区儿童的学校学习上的物资匮乏、校服短缺等情况，让贫困地区儿童获得健康平等的童年生活。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2018年，新疆南疆教育关爱行',
            inTxt: '携手石河子大学工学驻阿克苏地区“访惠聚”工作组，深入8所兵团学校开展“教育关爱活动”，为少数民族地区学生捐赠爱心书包及爱心学习文具两千余套，帮助当地学校老师解决贫困学生的学习用品，通过援疆的公益活动践行企业社会责任，让少数群众切身感受到党的关怀和祖国大家庭的温暖。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2013年，“雅安-我们心在一起”',
            inTxt: '雅安地震发生时候，第一时间号召用户为地震灾区小朋友送祝福及募集学习物资，同时一起作业配捐，让灾区小朋友感受到来自全国小朋友的关爱。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2018年，第17号教室爱心计划',
            inTxt: '2018年3月一起教育科技发起该助力乡村教育的公益活动，旨在为教育资源匮乏地区的乡村学校补充优质教育资源，助力教育均衡。其内容主要是帮扶乡村教师利用信息技术提升日常教学能力，同时也让乡村学生通过互联网等方式享受优质教育资源和学习方法。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2018年，“第17号教室”爱心计划携手小小铅笔（第5季）爱心计划',
            inTxt: '2018年一起携手小小铅笔（第5季）爱心计划，在第4季合作的基础上，同时增加了乡村教室培训的教育援助，扶贫先扶智，希望让山区老师提升教育信息化学习能力，仪最终帮助学生获得成长。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2017年至今，乡村儿童图书馆',
            inTxt: '携手上海蒲公英儿童发展为教育资源匮乏地区的小学共建乡村儿童图书馆，为乡村学校教师和家长提供按需设计的长期培训，使儿童能享受到符合他们发展的个性化定制型学习，从而成长为社会性发展良好的人，葆有好奇心的人，富有学习能力的人。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2014年至今，为爱行走，关注孤残儿童健康成长',
            inTxt: '一起教育科技已连续多年作为国际关心中国慈善协会(International China Concern)北京站的爱心合作企业，员工通过“徒步盲走接力+公益募捐”的组织形式，旨在呼吁广大市民关注孤残儿童的健康成长，为孩子们的爱、希望和机会行走。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2015年，智慧教育·新希望公益中国行',
            inTxt: '携手《中国青年报》共同发起“智慧教育·新希望公益中国行”活动，在金寨县希望小学将建立“智慧教育·新希望 一起作业实验室”并为该实验室提供价值百万元的优质的教育资源赠，让这些地方的孩子有机会和城里的孩子一样，接触到优质的学习资源和先进的学习方法。',
        },
        {
            imageUrl: imgCdnHeader + 'pic-05.jpg',
            title: '2016年 北京农民工子弟学校',
            inTxt: '一起教育科技通过爱心员工志愿者为北京市石景山区华奥学校师生捐赠了子弟学校必备的学习及生活用品（学习文具、篮球、足球等），同时爱心员工志愿者也定期为该学校学生通过互联网的方式上课并进行课后辅导工作。',
        },
    ];

    // 默认显示第一个
    imageUrl.attr('src', text_list[currentIndex].imageUrl);

    city_item.mouseover(function (){
        var _this = $(this);
        currentIndex = _this.index();

        city_item.removeClass('active');
        _this.addClass('active');
        filling_content(currentIndex)
    });
    var filling_content = function (index) {
        imageUrl.attr('src', text_list[index].imageUrl);
        title.text(text_list[index].title);
        text.text(text_list[index].inTxt);
    };

    /* 关于一起 */
    var item_year = $('.record-wrap .route');
        item_text = $('.swicth-box .context'),
        btn_prev = $('.prev'),
        btn_next = $('.next'),
        currentIndex = 0;

    var active_item_show = function (index) {
        item_year.removeClass('active');
        item_text.hide();
        item_year.eq(index).addClass('active');
        item_text.eq(index).show();
    };
    var swtich_text = function (index) {
        item_text.hide();
        item_text.eq(index).show();
    };
    item_year.mouseover(function (){
        var _this = $(this);
        currentIndex = _this.index();

        item_year.removeClass('active');
        _this.addClass('active');
        swtich_text(currentIndex);
    });
    btn_prev.on('click', function () {
        if(currentIndex <= 0){
            currentIndex = 0;
            return;
        }else{
            currentIndex -= 1;
        }
        active_item_show(currentIndex);
    });
    btn_next.on('click', function () {
        if(currentIndex >= 7){
            currentIndex = 7;
            return;
        }else{
            currentIndex += 1;
        }
        active_item_show(currentIndex);
    });

    /* 新闻列表 */
    var newsDataList = [
        /*{
            imgUrl: '',
            title: '',
            content: '',
            link: ''
        },*/
        {
            imgUrl: 'newspic23.jpg',
            title: '一起教育科技联合多家企业发布首个学习类APP进校服务行业自律倡议',
            content: '1月5日，教育部等主管部门负责人、中央电化教育馆等机构专家，以及多家在线学习APP运营商等就学习类APP发展与规范开展座谈。现场，一起教育科技、科大讯飞等企业联合发布首个学习类APP进校服务的行业自律倡议。教育部相关部门负责人表示，目前整个互联网信息产业法律法规的监管机制是需要一步步往前走的。',
            link: 'http://epaper.bjnews.com.cn/html/2019-01/10/node_70.htm'
        },
        {
            imgUrl: 'newspic24.jpg',
            title: '首个在线教育行业自律倡议出炉，一起教育科技科大讯飞等共同签署',
            content: '新华社北京1月6日电（记者 张辛欣）一批学习类App企业近日在京共同发布行业自律倡议，倡导建设高效、健康、有价值的“互联网+教育”行业，做学校教育的有益补充。其中，倡议明确在内容审核方面，杜绝色情暴力、网络游戏、商业广告及违背教育教学规律等内容。',
            link: 'http://www.gov.cn/xinwen/2019-01/06/content_5355372.htm'
        },
        {
            imgUrl: 'newspic22.jpg',
            title: '“减负报告”发布，一起教育科技为学生减负发声',
            content: '在日前举行的中国教育三十人论坛上，针对中小学生课业负担过重这一“跨世纪问题”，中国教育三十人论坛学术委员会联合一起教育科技发布了《2018年中小学生减负调查报告》。报告中指出，“中小学生课业负担过重的表现，归结为三个方面：一是课程学习的负担重；二是校外学习的负担重；三是学生的心理负担重。目前，中小学生学业负担过重的阶层和地域分布，正在从大中城市、中小阶级、知识分子家庭向中小城市（县城）、工薪阶层蔓延。”',
            link: 'http://d.youth.cn/newtech/201812/t20181214_11814941.htm'
        },
        {
            imgUrl: 'newspic19.jpg',
            title: '一起教育科技肖盾：智慧教育2.0应具备这六大特性',
            content: '“全球AI+智适应教育峰会”在北京嘉里中心大酒店盛大开幕，峰会由雷锋网联合乂学教育松鼠AI，以及IEEE（美国电气电子工程师学会）教育工程和自适应教育标准工作组共同举办，汇聚国内外顶尖阵容。AI智适应学习是目前产学研三界关注度最高的话题之一。此次峰会，主办方邀请了美国三院院士、机器学习泰斗Michael Jordan，全球公认机器学习之父Tom Mitchell，斯坦福国际研究院（SRI）副总裁Robert Pearlstein、美国大学入学考试机构ACT学习方案组高级研究科学家Michael Yudelson等顶尖学者。',
            link: 'https://www.leiphone.com/news/201811/wLxT7I9G3dtt70gK.html?uniqueCode=lpLWX2CEEoL7Xgs5'
        },
        {
            imgUrl: 'newspic20.jpg',
            title: '一起教育科技刘畅：大数据是教育公平最大的利器',
            content: '2018年11月4日上午，中国教育明德论坛2018年年会暨第十七届全国基础教育学习论坛在北京国家会议中心举行，本次论坛围绕加快推进基础教育现代化这一议题展开讨论。会上，一起教育科技CEO刘畅受邀参会并发表主题演讲——《在线教育让学习成为美好体验》。',
            link: 'https://baijiahao.baidu.com/s?id=1616982414169650965&wfr=spider&for=pc'
        },
        {
            imgUrl: 'newspic21.jpg',
            title: '公益行：海拔4500米上的长江第一课',
            content: '让所有的孩子都享受公平而有质量的教育，作为一个企业，若能尽绵薄之力，必义不容辞。谈起孩子，原本应该是一件温馨的事情，可当你走进贫穷落后的山区里时，孩子却成为了一个沉重的话题。当城里的孩子在父母陪伴下，接受着最好的教育；在乡村里有一些孩子，连拥有一个老师都是一种奢侈。这两个世界的孩子未来的差别，似乎一眼就可以望到底。作为教育从业者，面对这种困境，我们能为孩子们做些什么？',
            link: 'https://mp.weixin.qq.com/s/TPjtR_CbL7XYr2JhM_VYnA'
        },
        {
            imgUrl: 'newspic03.jpg',
            title: '人民网：界限被打破 在线教育改变传统“教与学”模式',
            content: '在“互联网+”时代，教育界限被完全打破，一种全新的教育模式——在线教育“横空出世”。它打破了时空、人数和地点的限制，等车、吃饭、休息的时候，人们都可以拿出手机、笔记本电脑等移动设备进入在线课堂，聆听名师授课。在网上课堂，为你授课的教师可能来自哈佛大学、斯坦福大学、北京大学、清华大学等顶尖学府。',
            link: 'http://it.people.com.cn/n1/2018/0306/c1009-29851711.html'
        },
        {
            imgUrl: 'newspic04.jpg',
            title: '人民日报：让公益意识成为孩子“心动力”—在线教育平台“一起作业”',
            content: '山东省肥城市王瓜店镇中心小学五年级的教室，处处充满了现代化气息。可实时互动的液晶触摸屏幕，学生们人手一台平板电脑……课堂上师生间的对话也颇有趣味：“蛇会游泳吗？”“会！”“老鼠能逃走吗？”“不能！”“所以这个故事逻辑上是有漏洞的，同学们要学会独立思考。”给孩子们授课的是校长张益强，有28年教龄的他已经在中心小学当了14年校长。',
            link: 'http://paper.people.com.cn/rmrb/html/2018-04/03/nw.D110000renmrb_20180403_3-16.htm'
        },
        {
            imgUrl: 'newspic17.jpg',
            title: '新华网：人工智能逼近，“无用阶级”产生？智能教育说“NO”',
            content: '刚刚过去的2017年，互联网+热潮还未完全退去，AI+已经呼啸而来。无人超市的出现、机器人参加高考10分钟交卷、AlphaGo与柯洁进行“乌镇论剑”、国务院印发新一代人工智能发展规划，要求建设人工智能学科……这一系列由AI引起的大事件令人们目不暇接。  一夜之间AI离我们如此之近。AI对人类牛刀小试的挑战，不断冲刷着人们已有的认知。',
            link: 'http://www.xinhuanet.com/itown/2018-02/24/c_136996360.htm'
        },
        {
            imgUrl: 'newspic06.jpg',
            title: '中国日报网：一起科技肖盾亮相ASU-GSV：AI最大教育价值是帮助孩子个性化成长',
            content: '近日，全球教育、科技圈的人都在关注2018 ASU-GSV教育科技创新峰会。作为全美乃至世界高规格的教育科技盛会，今年的亮点之一是首次设立中国教育展区。一起科技、好未来等中国教育企业一齐亮相，引发共同针对中国教育市场生态、中外教育合作、中国教育市场的本土创新模式等议题展开深入讨论。一起科技联合创始人肖盾受邀参加，并参加“中国市场上的本土创新和独特教育模式”讨论环节，对话耶鲁大学前校长、《哈佛商业评论》主编Adi等嘉宾。',
            link: 'http://tech.chinadaily.com.cn/2018-04/23/content_36077032.htm'
        },
        {
            imgUrl: 'newspic07.jpg',
            title: '中国日报网：一起科技刘畅：全球化人才培养需要知识与能力共同成长',
            content: '5月20，由全球化智库主办的“第四届中国与全球化论坛”在北京盛大启幕。在当前中美贸易战、全球一体化背景下，本届论坛得到了不少媒体的关注。也吸引了龙永图、王石、徐小平等多位大咖参与。本次大会一起科技创始人兼CEO刘畅作为教育领域代表，与嘉宾共同探讨了全球化趋势下对教育行业的改变和机遇。',
            link: 'http://caijing.chinadaily.com.cn/chanye/2018-05/22/content_36247166.htm'
        },
        {
            imgUrl: 'newspic18.jpg',
            title: '中国新闻网：改变教与学 APP作业的正确打开方式',
            content: '学生动动手指，就能在平板上预习、复习、完成作业；老师翻翻页面，就能完成作业编写和推送、反馈、了解整体学情；家长查阅空间，就能了解孩子的学习轨迹和德育评价……随着教育信息化进程的加速，越来越多教育类APP在中小学教学场景中现身。其教学效果究竟如何？与现有的教学体系如何互补互助，提高学生的学习兴趣和效率？',
            link: 'http://www.chinanews.com/life/2017/12-06/8393977.shtml'
        },
        {
            imgUrl: 'newspic13.jpg',
            title: '中国青年网：一起作业亮相中韩经贸合作交流会 牵手韩国教育公司e-future',
            content: ' 12月14日，“2017中韩经贸合作交流会”在北京国家会议中心举办。韩国演员宋慧乔、EXO-CBX，以及众多中韩企业代表出席了本次会议。作为国内在线教育的领先代表，一起作业受邀出席大会，并成功签约韩国教育公司e-future，双方在资源共享、内容定制等方面达成深度战略合作。',
            link: 'http://finance.youth.cn/finance_cyxfgsxw/201712/t20171218_11167082.htm'
        },
        {
            imgUrl: 'newspic14.jpg',
            title: '搜狐教育：一起作业，那支6年“不合群”的小快艇',
            content: '上次去一起作业拜访是冬末初春，我在会客室等待的时候，刘畅正在旁边小屋和公司的某个人聊天。晚上九点多，北京望京绿地B座的16层到19层都还亮着灯，一起作业联合创始人肖盾带我参观了三层楼。很多人还在办公室里——当然我并不知道他们还在具体忙些什么。今年10月11日那天，突然想起来，这个2015年2月18日（农历大年三十）宣布完成1亿美金D轮融资的公司，现在怎么样了？于是我跟肖盾约了采访。',
            link: 'http://www.sohu.com/a/205597564_115563'
        },
        {
            imgUrl: 'newspic15.jpg',
            title: '中国新闻网：在线作业用户报告：34.3%学生认为使用后提升学习成绩',
            content: '“有66.2%的学生认为在线作业提升了学习兴趣，34.3%的学生认为使用在线作业平台能够提升自己的学习成绩”，“69.7%的教师使用在线作业平台后，布置和检查作业的时间有所减少”，“87.4%的家长对孩子使用在线作业平台持支持态度”……这些数据出自近日中国教育学会第三十次学术年会微论坛上发布的《2017年中国中小学在线作业用户洞察报告》(以下简称《洞察报告》)。',
            link: 'http://www.chinanews.com/business/2017/11-23/8383749.shtml'
        },
        {
            imgUrl: 'newspic16.jpg',
            title: '凤凰科技：一起作业肖盾GES大会现场：这是教育信息化最好的时代',
            content: '11月29日，一起作业联合创始人肖盾受邀出席首届GES未来教育大会(以下简称“GES大会”)，参加“学校教育信息化”分论坛，与好未来高级副总裁于莉、Classdojo联合创始人兼首席执行官Sam Chaudhary、ThinkCERCA创始人兼首席执行官Eileen Murphy Buckley、互联网教育智能技术与应用国家工程实验室主任黄荣怀、中国教育学会副会长、上海教育协会会长尹后庆一起，共同就教育信息化对于学生、学校乃至社会的影响，以及未来学校教育信息化的趋势，民办教育如何推动教育信息化发展等话题展开了讨论。',
            link: 'http://tech.ifeng.com/a/20171201/44786867_0.shtml'
        },
        {
            imgUrl: 'newspic08.jpg',
            title: '中国教育报：一起科技：用科技创新点亮“教育公平”未来',
            content: '日前，第三届全国基础教育信息化应用展示交流活动在北京全国农业展览馆举行。一起科技作为在线教育模块的重要代表亮相该活动。一起科技更为外人熟知的是它的子品牌“一起作业”，这是一家被外界称为全球知名的基础教育智能学习平台，目前拥有6000万的用户，据统计每4个中小学生中就有一个他们的用户。今年3月20日，“一起作业”宣布品牌升级为“一起”，建立了以“一起作业”“一起学”“一起公益”三个子品牌为主的品牌矩阵。',
            link: 'http://paper.jyb.cn/zgjyb/html/2018-05/08/content_498698.htm?div=-1'
        },
        {
            imgUrl: 'newspic09.jpg',
            title: '中国网：哈尔滨141中学教师李爱华：一起教育科技让学生和学习“两情相悦”',
            content: '这是一个变革力空前的年代，信息化技术同样带来了教育的深刻变革。在教育信息化路上，总有一些人敢于挑战固有思维，探索新的世界，总有一些人敢于突破自己，革新自我。他们或带来了教育的新思路新方法，或带来了高效、个性、有趣的课堂，或助力了孩子快乐学习，或为自己的成长打开了一扇窗。',
            link: 'http://science.china.com.cn/2018-07/13/content_40420260.htm'
        },
        {
            imgUrl: 'newspic10.jpg',
            title: '北京晨报：28年教龄的乡村校长：科技让优质资源再回到农村',
            content: '“用一句话来形容我对一起作业的感受，那就是‘见到你就爱上你’”，在刚刚结束的一起科技新春发布会上，山东省肥城市王瓜店镇中心小学的校长张益强说。发布会上，张校长用朴素又十分有力量的语言，讲述了一起作业给孩子学习带来的帮助、对老师学生家长三方之间互动的促进作用、以及给乡村教育带来的巨大改变。',
            link: 'http://www.morningpost.com.cn/2018/0330/1699215.shtml'
        },
        {
            imgUrl: 'newspic11.jpg',
            title: '中国新闻网：一起作业与剑桥达成合作 首发英语教材《POWER UP》',
            content: '近日，剑桥大学出版社与一起作业在北京签署战略合作协议。一起作业联合创始人肖盾、剑桥大学出版社首席执行官Peter Phillips作为签约代表出席了会议，并共同宣布将一起推出与剑桥英语考试适配的教材《POWER UP》，通过一起作业平台面向K12阶段学生独家全球首发，并继续加强国内外优质教学资源互通，以及学习理论研究与应用等方面的合作，共同成长、鼎力赋能国际教育。',
            link: 'http://www.chinanews.com/cul/2018/03-14/8467589.shtml'
        },
    ];
    var pageList = newsDataList.slice(0,10); // 初始列表
    var pageNum = Math.ceil((newsDataList.length)/10); // 总页数
    var totalCount = newsDataList.length; // 条目总数
    var flag = true; // 是否首次进入页面
    var newsModal = function () {
        var self = this;
        $.extend(self, {
            imgCdnHeader: ko.observable(typeof imgCdnHeader == "undefined" ? "" : imgCdnHeader),
            newsDataList: ko.observableArray(pageList),
        });
        function initPagation() {
            $('#pagger').paging({
                initPageNo: 1, // 初始页码
                totalPages: pageNum, //总页数
                totalCount: '共' + totalCount + '条数据', // 条目总数
                slideSpeed: 600, // 缓动速度。单位毫秒
                jump: false, //是否跳转
                callback: function(page) {
                    /* 首页、最后一页置灰样式 */
                    if (page === 1) {
                        // 第一页
                        $('.first-page,.pre-page').attr('disabled','disabled');
                        $('.next-page,.last-page').removeAttr('disabled');
                    } else if (page === pageNum) {
                        // 最后一页
                        $('.first-page,.pre-page').removeAttr('disabled');
                        $('.next-page,.last-page').attr('disabled','disabled');
                    } else {
                        $('.last-page,.next-page,.pre-page,.first-page').removeAttr('disabled');
                    }
                    /* 点击页码 回滚到新闻头部 */
                    if (flag) {
                        flag = false;
                    } else {
                        $("html,body").animate({scrollTop:$('.md-banner').height()}, 0);
                    }
                    pageList = newsDataList.slice((10*(page-1)),10*page);
                    self.newsDataList(pageList);
                }
            });
        }

        initPagation();
    };

    ko.applyBindings(new newsModal(), document.getElementById('newsModal'));
});