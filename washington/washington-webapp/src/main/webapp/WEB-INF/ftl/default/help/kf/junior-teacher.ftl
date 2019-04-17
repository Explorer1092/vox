<#import "module.ftl" as com>
<@com.page title="老师" type="teacher">
    <@app.css href="public/skin/helpkf/css/skin.css" />
<!--//start-->
<div class="t-helpCenter-main">
    <div class="nav">
        <ul>
            <li class="title">问题帮助</li>
            <li class="js-clickLeftTypeBtn" data-type="认证问题"><a href="javascript:void(0);">认证问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="账号密码&绑定"><a href="javascript:void(0);">账号密码&绑定</a></li>
            <li class="js-clickLeftTypeBtn" data-type="作业相关问题"><a href="javascript:void(0);">组作业相关问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="班级问题"><a href="javascript:void(0);">班级问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="中学英语学豆与学习用品"><a href="javascript:void(0);">中学英语学豆与学习用品</a></li>
            <li class="js-clickLeftTypeBtn" data-type="其他"><a href="javascript:void(0);">其他</a></li>
            <#--<li class="js-clickLeftTypeBtn textColor" data-type="投诉建议"><a href="javascript:void(0);">投诉建议</a></li>-->
            <li class="js-clickLeftTypeBtn textColor" data-type="联系客服"><a href="javascript:void(0);">联系客服</a></li>
        </ul>
    </div>
    <div id="detailsItems"></div>
</div>
<!--end//-->
<script type="text/javascript">
    publicTel = "<@ftlmacro.hotline phoneType = "junior"/>";
    detailItem = {
        "认证问题": [{
            name : "如何才能认证？",
            content: "请您点击左上角老师头像，点击【我的认证】，查看认证条件是否全部满足，满足条件后，系统会在一天内自动匹配帮您认证通过。您也可以致电客服4001601717进行人工认证。",
            tel : false,
            server : false
        }],
        "账号密码&绑定" : [{
            name : "老师如何找回学生密码？",
            content: "您可以在【我的班级】标签下，点击【学生详情】找到要修改密码的学生名字。</br>" +
                "1.学生未绑定手机请您直接点击【重置密码】，输入两次新密码，点击【确定】即可。</br>" +
                "2.学生已绑定手机请您点击【重置密码】，密码会以短信形式发送到学生绑定的手机号码上。</br>" +
                "如果学生已绑定手机但是号码已停用或者不方便接收短信，请学生联系客服4001601717进行密码找回。",
            tel : false,
            server : false
        },{
            name : "老师忘记密码怎么办？",
            content: "1.已绑定手机号码：请您单击左侧您的头像-【账号安全】-【修改密码】按提示输入绑定的手机号码就可以找回密码。</br>" +
                "2.没有绑定手机号码：请您致电4001601717联系客服，客服会第一时间帮您找回密码。</br>",
            tel : false,
            server : false
        },{
            name : "怎么修改绑定的手机号码？",
            content: "请您单击右侧您的头像，点击【个人中心】-【账号安全】-【更换手机】。",
            tel : false,
            server : false
        },{
            name : "怎么修改密码？",
            content: "请您单击右侧您的头像，点击【个人中心】-【账号安全】-【修改密码】。",
            tel : false,
            server : false
        },{
            name : "如何修改头像？",
            content: "暂时不能更换头像，请您随时关注。",
            tel : false,
            server : false
        }],
        "作业相关问题" : [{
            name : "为什么不能检查作业？",
            content: "如果作业是当天到期或学生已经全部完成，请您点击【在线作业】-【作业报告】-【检查作业】，但是如果作业不是当天到期，是不能检查的。您可以调整布置作业完成时间到今天，即可检查。",
            tel : false,
            server : false
        },{
            name : "怎么调整作业？",
            content: "作业目前只能调整时间无法调整内容，请您点击【在线作业】-【检查作业】，点击右上角【调整】进行调整，已经到期或已检查的作业是不能调整的哦。",
            tel : false,
            server : false
        },{
            name : "怎么删除作业？",
            content: "请您点击【在线作业】-【检查作业】进行删除，只能删除未到期且未检查的作业。",
            tel : false,
            server : false
        },{
            name : "检查完作业怎么查看学生的完成情况？",
            content: "请您点击【在线作业】-【检查作业】-【检查】即可看到学生的完成情况。",
            tel : false,
            server : false
        },{
            name : "我有三个班，给其中一个班发布了作业，怎么给剩下的班级发布作业？",
            content: "请您点击首页下方【在线作业】-【发布作业】可以给相应的班级布置作业。",
            tel : false,
            server : false
        },{
            name : "学期结束了，我能查看本学期的总结吗？",
            content: "请您点击【在线作业】-【学情】即可看到本学期的总结。",
            tel : false,
            server : false
        },{
            name : "怎么批量发布作业？",
            content: "请您登陆账号后，在左侧菜单中点击【在线作业】-【发布作业】，选择年级，勾选班级时，在【全部】前划勾，即可选择同年级所有班级，根据您要发布的单元、题型进行点选，点击【发布】，选择截止时间，确认无误后点击【确认发布】即可。",
            tel : false,
            server : false
        },{
            name : "布置了作业，为什么学生说没看到相应作业？",
            content: "老师出现这种问题有两种情况：</br>" +
                "1、请点击【我的班级】-【学生详情】，核实学生登录账号和您班级中的账号是否一致，如果不一致，请告知孩子使用您班级中显示的正确账号登录。</br>" +
                "2、请点击【在线作业】-【检查作业】，看一下您布置的作业是否存在，可能是您不小心点击了删除，删除后，学生端就不会显示该作业了。",
            tel : false,
            server : false
        },{
            name : "学生说做了作业，为什么中显示没有做？",
            content: "请您点击【我的班级】-【学生管理】，核实学生是否存在两个账号，如果学生有两个账号，请删除其中不经常使用的账号，避免数据出现问题。",
            tel : false,
            server : false
        }],
        "班级问题" : [{
            name : "我加错班级了，怎么修改？",
            content: "请您登陆账号后，在左侧菜单中点击【班级管理】，再点击右上角【创建/退出班级】找到所加班级，在现有班级中选择不教的班级点击【不教了】即可。",
            tel : false,
//            problem : "question_class",
            server : false
        }, {
            name : "怎么接管其他老师班级？ ",
            content: "请您登陆账号后，在左侧菜单中点击【班级管理】，再点击右上角【加入班级】，根据年级、班级、任课老师姓名选择您要加入的班级，选中该班之后点击【申请加入】。等对方老师登陆账号之后，同意您的转班申请，班级就会转到您的名下了。",
            tel : false,
            server : false
        }, {
            name : "怎么把我的班转给其他老师？",
            content: "请您登陆账号后，在左侧菜单中点击【班级管理】选择要转让的班级，点击您名字下方的【转让班级】，选择要转给的老师，点击【转给TA】；等对方老师登陆账号，同意您的申请之后，您的班即可转给该老师。如果您要转给的老师还未注册，请您填写新老师姓名、手机号邀请该老师注册后完成转班。",
            tel : false,
            server : false
        },{
            name : "怎么接手别人转给我的班？",
            content: "请您登陆账号后，在左侧菜单中点击【班级管理】，您在粉色框中会看到转让申请，点击【允许】，班级即可转给您。",
            tel : false,
            server : false
        },{
            name : "我如何取消转让班级的申请？",
            content: "如果对方还没有同意您的转让申请，您可以在【班级管理】中的【我的申请】，选中要取消的申请，单击后边的【取消】，就可以取消申请了，但是您会从班级内退出，需要点击【创建新班级】 选择退出的班级，加入即可。如果对方已经同意了您的申请，将无法进行此次操作。",
            tel : false,
            server : false
        },{
            name : "怎么添加班级？",
            content: "请您登陆账号后，在左侧菜单中点击【班级管理】，然后点击【创建/退出班级】，选择相应的年级、班级即可添加。如果没有找到您的班级，可以联系人工客服为您添加。",
            tel : false,
            problem : "question_class",
            server : false,
        },{
            name : "我怎么加入其他老师创建好的班级？",
            content: "请您登陆账号后，在左侧菜单中点击【班级管理】，再点击右上角【加入班级】，根据年级、班级、任课老师姓名选择您要加入的班级，选中该班之后点击【申请加入】。等对方老师登陆账号之后，同意您的申请，即可加入该老师的班级。（申请发送后当面提醒老师可以更快通过申请噢！）",
            tel : false,
            server : false
        },{
            name : "我创建班级的时候，选择好年级和班级后，发现学校已经有人创建了这个班，怎么办？",
            content: "请您核实这个班级的拥有者是不是您所教班级的其他任课老师。</br>" +
                "如果是，正常选择年级，班级，点击【加入】。</br>" +
                "如果不是，点击【不加入】-【创建班级】，选择班级人数，即可创建新的班级。",
            tel : false,
            server : false
        },{
            name : "我如何删除学生？",
            content: "请在【班级管理】标签下，点击【学生详情】找到要删除的学生名字，点击后方【删除】即可。",
            tel : false,
            server : false
        },{
            name : "总有捣乱的学生莫名其妙的加入到我们班级中来，我要怎么处理？",
            content: "请您登陆账号后，在左侧菜单中点击【班级管理】，选择对应班级点击【学生详情】，在右上角【学生加入班级】勾选【不允许】学生加入，即可禁止学生加入所在班级。",
            tel : false,
            server : false
        },
        {
            name : "我如何查看班级学生的信息？",
            content: "请在【班级管理】标签下，点击【学生详情】，都可以看到该班级的学生姓名、学号、家长手机和对应的操作栏。",
            tel : false,
            server : false
        },
        {
            name : "我如何添加学生？",
            content: "请您告诉学生您正确的账号或者手机号，学生注册学生APP之后，点击【注册】输入号码选择正确的班级即可。",
            tel : false,
            server : false
        }],
        "中学英语学豆与学习用品": [{
            name : "如何获得学豆？",
            content: "您可以通过发布并检查作业的方式获得学豆，更多规则请您点击【学豆】图标进行查看。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "学豆丢失怎么办？",
            content: "请您点击学豆数量，查看详细的学豆使用明细。如果发现异常减少情况，请您拨打4001601717，会有客服人员了解您的情况并帮您解决问题。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "学生的学习用品到了，为什么老师的学习用品还没有到？",
            content: "非常抱歉给您带来的不便，由于老师的学习用品与学生的学习用品是分开配送发放的，到货时间会有几天的延迟，建议您再耐心等待几天。最晚20号左右可以到达。如果20号之后还没有收到，您可以致电客服进行解决。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "怎么修改学习用品中心的收货地址？",
            content: "请您点击您的头像，选择我的资料，可以填写收货地址哦。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "为什么班里有些学生学习用品收到了，有些没有收到？",
            content: "请先确认没有收到学习用品的孩子的兑换时间，当月兑换的要下月发货，如果是上月兑换请联系客服帮学生处理。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "现在兑换的学习用品什么时候到？",
            content: "您好，学习用品兑换资格：只有通过认证的老师才能兑换学习用品，未认证老师的订单视为无效订单不予寄送。</br>" +
                "您在本月兑换的学习用品，将于下个月20日左右寄到学校，6、7、8月兑换的学习用品集中在9月寄送，12、1、2月兑换的学习用品集中在3月寄送。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "兑换的学习用品不想要了怎么办？",
            content: "如果您是当月兑换的学习用品，请您点击【我的学习用品】点击学习用品后边的叉号，即可取消学习用品。（如果不是当月兑换的学习用品，是不能退货的。）",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "兑换的学习用品有问题怎么办？",
            content: "请您将有问题的学习用品拍照，将图片发给在线客服工作人员，工作人员会根据您的情况做出相应处理。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "以后还会增加学习用品种类吗？",
            content: "我们后期会专门做大数据分析，根据老师的兑换情况随时调整学习用品的种类和数量。",
            tel : false,
            problem : "",
            server : false
        },
        {
            name : "为什么很多学习用品库存紧张？",
            content: "各个学习用品的供应商不同库存不同，由于库存物流等原因补货较慢，建议您兑换其他种类学习用品。",
            tel : false,
            problem : "",
            server : false
        }],
        "其他" : [{
            name : "我教的学科更换了，能调整吗？",
            content: "老师的任教学科是可以调整的，请您联系客服，客服人员会协助您进行学科更换。",
            tel : false,
            problem : "question_other",
            server : false
        },{
            name : "怎么制作听力材料？",
            content: "请您登陆账号后点击【听力材料】，点击右上角的制作听力材料，根据提示选择您需要制作的听力材料即可。",
            tel : false,
            problem : "question_other",
            server : false
        },
        {
            name : "如何修改制作完成的听力材料？",
            content: "请您登陆账号后点击【听力材料】，找到您制作的题目，点击后面的【编辑】，即可修改您设置好的内容。",
            tel : false,
            problem : "question_other",
            server : false
        },
        {
            name : "我的学校信息错了，怎么修改？",
            content: "如果您需要修改学校信息，请您致电4001601717联系客服，客服人员会协助您修改学校信息。",
            tel : false,
            problem : "question_other",
            server : false
        }],
        /*"投诉建议" : [{
            name : "投诉建议",
            content: "",
            tel : false,
            problem : "question_advice",
            server : false
        }],*/
        "联系客服" : [{
            name : "联系客服",
            content: "",
            tel : false,
            problem : "question_other",
            server : true
        }]
    };
</script>
</@com.page>