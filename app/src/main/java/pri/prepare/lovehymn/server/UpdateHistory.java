package pri.prepare.lovehymn.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;

import java.util.ArrayList;

import pri.prepare.lovehymn.client.SettingDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class UpdateHistory {
    public static final int MIN_RES_INDEX = 190;

    /**
     * 是否为内测版
     *
     * @return
     */
    public static boolean isTestMode() {
        return VERSION_HISTORY[0].contains(" temp ");
    }

    public static final String[] VERSION_HISTORY = {
            "23/12/17 1.8.5 横屏时改为默认隐藏状态栏;标题过长时跑马灯显示(而非原先的换行显示);{修复3处蓝版pdf或歌词错误}",
            "23/07/31 1.8.4 {修复十几处蓝版pdf或歌词错误},没别的了",
            "23/03/24 temp 修复一些细节",
            "23/03/23 1.8.3 启动页美化",
            "23/03/20 temp {修复18处蓝版pdf}",
            "23/02/02 temp 足迹详情完善",
            "23/02/02 1.8.2 足迹详情(未完成);提示优化",
            "23/01/22 temp 修复横屏pdf比例显示bug",
            "22/12/25 1.8.1 三旧一新错误修复;解压完成后提示时间加长到一分钟,避免未注意而无法判断是否解压完成(可以点击提示快速关闭);{修复60多处的蓝版pdf错误}",
            "22/11/23 1.8.0 加载时屏幕常亮;广告方式修改;一些文字资料修改;打开播放列表时自动播放当前MP3;加一个开关解决某些情况下打开播放列表错误的bug",
            "22/10/06 1.7.9 修复三旧一新错误",
            "22/09/28 1.7.8 {修复几处蓝版pdf},资源和三旧一新错误;加载异常提示信息修改;修复附加诗歌的错误(会影响查看足迹等功能)",
            "22/08/11 1.7.7 取消加载pdf失败时的按钮提示(该按钮有时会错误显示,影响阅读)",
            "22/08/10 temp 新增网页版三旧一新功能(从三旧一新设置中修改);相同诗歌的资源互相引用(一些诗歌被多个诗歌本收录,MP3资源可以共用);Q207改为'同我唱一首锡安之歌'",
            "22/08/06 temp 播放音频时,若媒体音量为0,修改媒体音量;三旧一新的细节调整;{发布pdf附加包V4}",
            "22/07/30 1.7.6 修复一个加载MP3列表异常的bug;打开MP3播放列表时停止主界面的MP3播放",
            "22/07/24 temp 完善教程和设置UI小修改",
            "22/07/22 temp 修复三旧一新和备注功能的小bug;完善第一次使用流程",
            "22/07/20 temp 修复三旧一新播放的bug;修复一个通知细节",
            "22/07/18 temp 修复严重错误:错把以斯帖记当以斯拉记,导致的三旧一新和相关经节错误;新增个人备注功能",
            "22/07/17 temp 优化三旧一新细节;几个小bug修复",
            "22/07/15 1.7.5 资源文件整理,解决{华为手机加载}困难的问题",
            "22/07/14 temp 三旧一新细节调整{(可以在桌面长按app图标或者设置-小工具进入)};资源文件整理,隐藏部分资源文件",
            "22/07/09 1.7.4 完善三旧一新小工具;高亮重要更新内容;异常情况提示;默认播放列表设为所有诗歌",
            "22/07/06 temp 完善三旧一新小工具",
            "22/07/05 temp 完善三旧一新小工具;修复有诗歌的标签删除bug",
            "22/07/04 temp 初步完成{三旧一新小工具}",
            "22/07/03 1.7.3 增加第一次使用时的设置引导",
            "22/07/01 temp 一些细节修改",
            "22/06/19 temp 一些细节修改",
            "22/06/05 1.7.2 优化解压附加包失败时的处理办法;整理代码",
            "22/06/04 temp 设置中增加小工具:简易时间表制作",
            "22/06/03 temp 修复在某些情况下加载标签资源出错的bug;修复一些地方的附加诗歌的顺序问题(重新加载起效);修复加载资源时无法拖动说明的bug",
            "22/05/28 1.7.1 一些文字描述修改;一些代码整理;同谱诗歌功能优化;目录的搜索功能优化",
            "22/05/21 temp 添加桌面长按功能:最近播放列表;修复同谱诗歌错误(建议清空app数据或重装来删除错误数据)",
            "22/05/17 temp 加一些说明",
            "22/05/15 temp 修改一些文本",
            "22/05/14 1.7.0 修复一些细节问题;修复搜索时排序错误;自动足迹默认开启",
            "22/05/08 1.6.6 修复从后台打开app丢失pdf进度的bug;优化自动足迹;修改广告配置",
            "22/05/07 temp 修复一个严重bug:搜索时可能不显示结果;主题设置移动到其他设置中",
            "22/05/06 temp 优化播放列表细节",
            "22/05/05 temp 优化播放列表细节",
            "22/05/04 temp 更新检测服务优化;新增标签组(相关功能未完成)",
            "22/05/01 temp 优化搜索体验",
            "22/04/30 temp 一些文字描述修改;新功能:播放列表的标签弹窗10秒后自动关闭;播放列表增加所有MP3和长按功能",
            "22/04/28 1.6.5 一些文字描述修改;目录bug修复",
            "22/04/27 temp 一些文字描述修改",
            "22/04/23 temp 调整统计和播放列表细节;新增唱诗人mp3附加包(见网盘分享)",
            "22/04/22 temp 调整播放列表细节",
            "22/04/21 temp 修复mp3排序问题;调整mp3播放页细节;修复快捷删除可能造成未彻底删除bug",
            "22/04/19 1.6.4 播放列表(诗歌本)增加添加到标签功能",
            "22/04/18 temp {降低app版本需求},最低至{安卓8.0};修复播放列表一个排序错误",
            "22/04/17 temp 增加一个足迹统计项;优化目录输入法逻辑;增加标签快捷删除(当该标签下没有诗歌时)",
            "22/04/09 1.6.3 增加标签和足迹的统计功能;完善自动足迹功能",
            "22/04/04 1.6.2 修复一个播放列表的歌词bug",
            "22/03/22 temp 增强稳定性;修复一个播放列表上一首下一首bug",
            "22/03/21 temp 修改缓存策略",
            "22/03/16 temp 修复自动足迹自动刷新失败问题",
            "22/03/10 1.6.1 新增自动足迹功能(在标签足迹设置中开启);全新整理的pdf:{pdf附加包V3}",
            "22/03/07 temp 细节调整",
            "22/03/02 temp 长按标签显示标签下的所有诗歌",
            "22/02/21 temp 修复整理资源文件造成的显示bug",
            "22/02/14 temp 修改设置图标;增加手势操作类型;修复主页循环模式不能保存的bug",
            "22/02/11 1.6.0 增加手势设置和手势操作",
            "22/02/10 temp 修复一处大本白版索引错误;强化删除缓存功能;修复一个特殊序号诗歌的显示问题;提升兼容性;优化足迹显示;修复自定义pdf(新功能)可能造成搜索异常的bug;优化目录显示",
            "22/01/28 1.5.9 完善细节;调整其他设置;优化搜索速度",
            "22/01/26 temp 完善细节;整理代码",
            "22/01/25 temp 统一其他设置与标签足迹设置UI;修复异常资源的bug,增强稳定性",
            "22/01/24 temp 修复分享及白版bug;用{双指滑动代替三指滑动}",
            "22/01/21 temp 其他设置中增加功能:清空缓存;完善新增功能",
            "22/01/20 1.5.8 整理res信息;删除一些基本用不到的配置",
            "22/01/19 temp 优化提示信息;新增书名‘其它’,用于用户{自定义的诗歌}(或其他pdf),整个自定义功能设计中",
            "22/01/18 temp 修复无对应信息的pdf(如果存在)造成的一些异常情况",
            "22/01/14 temp 增加一些不兼容时的提示,方便出来问题",
            "22/01/13 1.5.7 完善过渡动画;清除app数据功能移动到其他设置",
            "22/01/12 temp 优化播放列表界面;完善过渡动画",
            "22/01/11 temp 优化播放器界面并添加按钮：跳转到pdf",
            "22/01/10 temp 修复播放器bug并优化该界面",
            "22/01/07 temp 完善播放器歌词界面",
            "22/01/06 1.5.6 一本诗歌本支持多个白版pdf;修改主页‘随机播放’->'猜你喜欢',暂时未考虑好该按钮改什么;修改主页按钮布局",
            "21/12/27 temp 完善字段;修复通过收藏夹和历史记录跳转时未更新标题栏bug",
            "21/12/23 temp 完善作者资料,算是基本完成了;增加清除app数据按钮,方便情况数据(哎,版本升级造成的错误数据处理一直做不好);" +
                    "修改dal的查询实现方法,减小升级的bug几率;修改MP3默认循环模式为随机播放;完善提示和常见问题",
            "21/12/22 temp 完善作者资料",
            "21/12/21 temp 完善作者资料",
            "21/12/20 temp 补充细节;重构常见问题;优化新增的pdf附加包逻辑;修改第一次加载界面的说明:统一使用小贴士",
            "21/12/17 1.5.5 重构小贴士和帮助文档",
            "21/12/15 temp 增加新功能:生僻字字典;修改设置布局",
            "21/12/14 temp 优化pdf切换过度效果;修复MP3播放的初始播放按钮bug;增加白板诗歌锁的功能;加个升级提示框",
            "21/12/13 temp MP3初始播放按钮改为:随机;足迹细节调整;添加设置:默认显示工具栏(左右滑动时工具栏会影响看第一段歌词,找歌时很讨厌);" +
                    "MP3播放器增加歌词功能;修复MP3播放器偶尔加载MP3异常中断的bug",
            "21/12/11 1.5.4 发布新版本;{pdf也支持附加包}",
            "21/12/10 temp 完善MP3功能:图标 修复bug;调整细节;通过异步加载优化用户体验",
            "21/12/08 temp 完善MP3功能:排序,上一首下一首",
            "21/12/08 temp 优化搜索界面,完善MP3功能",
            "21/12/07 1.5.3 增加{MP3播放列表功能}",
            "21/12/05 temp 细节调整;下一个版本计划强化MP3功能,目前先做稳定版",
            "21/12/03 temp 新增一些作者简介",
            "21/12/01 temp 修改标签管理页的下拉框样式;在标题栏添加足迹标记",
            "21/11/29 1.5.2 终于整理好青年诗歌的歌词了,目前六本诗歌本都有歌词,都可以歌词搜索了;修改标签备份逻辑:每次修改自动备份到储存卡",
            "21/11/28 temp 降低长按误识别率;足迹添加提示:X首诗歌留下了足迹;修复个别情况下展示歌词过短的bug",
            "21/11/27 temp 修复约8个pdf错误;修复附加诗歌的下一首bug",
            "21/11/21 1.5.1 尝试解决启动闪退;修复少量‘新歌颂咏’歌词的错误;添加足迹功能",
            "21/11/18 temp 替换一个pdf;修复大量‘新歌颂咏’歌词的错误;完善分享MP3功能(因微信限制,只能加到收藏中,再分享到群里);删除一个其他设置",
            "21/11/14 temp 修复一些资源错误;增加‘唱诗人’歌词;增加一提示;完善收藏/历史界面;更新介绍图片",
            "21/11/08 temp 略微美化了下收藏/历史界面",
            "21/11/07 temp 修复一个空指针错误;标签页优化排版;缩短长按时间",
            "21/11/06 1.5.0 修复bug:拖动进度条",
            "21/11/04 temp 调整新界面(初步完成);取消长按目录功能",
            "21/08/10 1.4.9 修复长按bug;优化按旋律搜索。P.S.后续又没空了,下一个版本不知会啥时候了",
            "21/08/08 temp 美化,继续美化;优化长按逻辑",
            "21/08/04 temp 继续美化标签",
            "21/08/04 temp 风格统一了下",
            "21/08/02 1.4.8 稍稍美化了标签管理页,发布个新版本吧",
            "21/08/01 temp 添加长按功能,替代之前点击标题栏,并加入分享快捷键;美化收藏标签界面",
            "21/07/31 1.4.7 完成标签编辑功能",
            "21/07/30 temp 基本完成标签修改功能",
            "21/07/28 temp 完善经节显示",
            "21/07/27 temp 修改默认主题为大图标,优化加载界面,修复资源错误",
            "21/06/27 1.4.6 修复第一次打开的工具栏闪动bug;加入标签管理(初步)",
            "21/06/26 temp 优化标签dialog:样式与动画",
            "21/06/23 temp 完善标签;使用databinding框架",
            "21/06/23 temp 完善标签",
            "21/06/22 1.4.5 增加更新内容显示(越来越像个app了);初步添加标签功能",
            "21/06/19 1.4.4 优化下载地址,将其他设置的按钮加到设置中,重构代码",
            "21/06/19 1.4.3 优化加载的提示,异性屏检测,修复息屏后打开的闪退bug",
            "21/06/18 1.4.2 加入启动自建",
            "21/06/17 1.4.1 日志优化",
            "21/06/16 1.4.0 优化加载资源时的提示;修改版本号,发布",
            "21/06/13 temp 修复bug",
            "21/06/12 1.3.12 删除停止按钮",
            "21/06/11 1.3.11 完善启动页",
            "21/06/10 1.3.10 增加启动页,将第一次打开的加载放在该页面,并加入说明(计划)",
            "21/06/07 1.3.9 增加特殊设置:状态栏的显示与隐藏;儿童诗歌序号处理",
            "21/06/06 1.3.8 修复bug;优化提示;竖屏显示状态栏;增加特殊设置页面,放一些特殊情况下才使用的功能(有些甚至是开发者功能)",
            "21/06/05 1.3.7 优化加载速度;修复几首青年诗歌序号错误",
            "21/06/04 temp 修复安卓11的大边框bug",
            "21/06/02 temp 大本60首后的背景及作者资料",
            "21/06/02 temp 完善细节",
            "21/06/01 1.3.6 搜索结果高亮;图标修改",
            "21/05/30 1.3.5 细节调整;增加大图标主题,横屏显示时间,搜索效率优化",
            "21/05/29 1.3.4 增加切换主题功能:增加透明主题,彩色主题",
            "21/05/29 1.3.3 修复一个空指针错误",
            "21/05/27 1.3.2 加入随机播放按钮;调整按钮大小与样式",
            "21/05/25 1.3.1 加入一些异常处理",
            "21/05/25 temp 一些细节完善",
            "21/05/23 temp 隐藏两个设置,并同步到说明文档中,发现一处歌词乱码,优化搜索结果显示,打开时恢复上次阅读进度,删除以前的错误数据,修复更新检查在其他手机上异常(调整识别),优化加载提示,优化MP3播放按钮逻辑",
            "21/05/22 temp 完善说明;自动扫描并加载资源包,修复儿童诗歌搜索bug,修复第一次打开未能加载附加包bug,强化自动扫描功能",
            "21/05/19 temp 获取更新地址功能又能用了,但没之前的好用,哎",
            "21/05/19 temp 自动删除已导入的附加包",
            "21/05/18 temp 完善经节显示",
            "21/05/16 temp 经节识别bug修复;更新蓝版资源(2016版);整理了儿童诗歌,新歌颂咏歌词",
            "21/05/15 temp 设置界面完善;增加收藏夹功能;重新打开ap mp3p播放停止bug修复",
            "21/05/14 temp 设置界面调整",
            "21/05/13 temp 修改搜索界面UI;修复一搜索bug",
            "21/05/11 temp 修复按旋律和完整序号搜索bug",
            "21/05/09 temp 完善历史记录;智能补全同谱诗歌;旋律搜索;设置:手动同步资源",
            "21/05/07 temp 完成历史记录的上一首、下一首",
            "21/05/05 1.3 准备发布1.3版;调整设置界面排版",
            "21/05/05 temp 初步完成经节显示",
            "21/05/03 temp 初步完成同谱诗歌链接",
            "21/05/01 temp 加入多关键字搜索:已空格隔开,例'倪柝声 十字架';修复统计bug",
            "21/04/29 temp 修复按序号搜索bug;修复提示bug;修改图标,还是不满意",
            "21/04/24 temp 终于把作者和内容正确显示了:现支持多个词作者和曲作者;添加新的配置路径",
            "21/04/18 temp 终于运行起来了,bug还很多,修改底层不容易",
            "21/04/09 temp 底层结构大调整:为了支持多个作者及圣经节(又是一个大工程)",
            "21/04/05 1.2 整理MP3;修复一些资源错误;修改统计逻辑;修复工具栏显示bug;修改结构:词作者,曲作者",
            "21/03/28 1.1 将蓝版塞进安装包;将MP3和白版改为附加包;删除一些过期代码;修复排序bug;调整一个日志",
            "21/03/25 1.03 青年诗歌 新歌颂咏 儿童诗歌白版对照资源整理完毕;修复一个下一首bug",
            "21/03/22 1.02 大本、补充本白版资源对照整理完毕",
            "21/03/21 1.01 增加白版",
            "21/03/20 1.0 正式发布",
            "21/03/20 0.996 整理说明文档;修复作者其他诗歌bug;替换部分蓝版诗歌",
            "21/03/19 0.995 资源整理(大本1-60的思路,经节等);统计优化",
            "21/03/19 0.994 修复懒加载bug",
            "21/03/18 0.993 修复儿童诗歌上一首下一首错误;优化搜索结果;完善说明;整理 唱诗人,新歌颂咏标题;增加提示",
            "21/03/16 0.992 修改搜索分页逻辑;重复bug(可能修复了)",
            "21/03/14 0.991 搜索时如果在某个诗歌本的文件夹里,那么只会搜索该诗歌本的诗歌;优化搜索速度;优化搜索结果显示",
            "21/03/13 0.99 搜索时下拉自动加载更多搜索结果;加入手势:左右滑动上一首/下一首;修复bug:诗歌序号带-2的处理;资源及代码整理,为正式版做准备",
            "21/03/11 0.98 完成资源下载功能的ui界面",
            "21/03/06 0.97 通过延时加载减少卡顿感",
            "21/03/05 0.96 给详情按钮不同的图标",
            "21/03/05 0.95 给附歌单独的目录(然后文件夹结构又改了...)",
            "21/03/04 0.94 二级目录优化;代码整理;添加歌词显示",
            "21/03/03 0.93 修改主界面的颜色,图标等;二级目录初步",
            "21/03/02 0.92 操作逻辑改为点击中间显示菜单",
            "21/02/28 0.90 UI逻辑大调整;风格是改过来了,配色很垃圾...",
            "21/02/27 0.81 UI修改:详情界面同一风格;音乐播放基础功能",
            "21/02/26 0.80 UI修改:搜素界面;设置界面",
            "21/02/26 0.79 UI风格变化(圆润)目录界面(除搜索)",
            "21/02/25 0.78 数字的搜索优化(数据库加表)",
            "21/02/24 0.77 时间和分享bug修复;整理MP3资料;MP3初步处理",
            "21/02/24 0.76 优化加载资源文件夹的用户体验(丝滑);搜索优化;说明书第二版",
            "21/02/23 0.75 显示时间;搜索优化",
            "21/02/23 0.74 日志完善;特殊处理资源文件嵌套导致找不到资源的bug",
            "21/02/22 0.72 搜索结果排序;资源乱码bug修复;修复找不到文件时的显示异常;添加资源统计;添加‘感谢’;完善资源统计;添加历史;显示优化",
            "21/02/21 0.7 同一序号多首诗歌特殊处理;添加了日志功能",
            "21/02/19 0.6 重新载入资源文件bug;说明的逻辑修改;大本补充本的标题歌词收集完毕",
            "21/02/18 Alpha0.5 发现一个bug,资源文件编码造成的,不予解决;自动搜索资源文件",
            "21/02/17 Alpha0.4 精简了界面;修改了分享功能位置;修复实体重复添加的bug",
            "21/02/16 Alpha0.3 附歌的上一首下一首特殊处理;按钮自动隐藏优化;文件名显示优化;分享到微信功能;添加说明;添加复制‘赏析’等功能;光滑的渐隐效果;作者简介;作者的其他诗歌",
            "21/02/15 Alpha0.2 添加动态关键字;增加左右按钮(附加的诗歌还未特殊处理)",
            "Alpha0.1 有个雏形了"
    };
    private static final String[] ASK_ANSWER = {
            //"问:\r\n答:",
            "问:设置里一直提示获取不到下载地址怎么办？\r\n答:可能是wifi造成的,换成数据试试",
            "问:app解压压缩包失败怎么办?\r\n答:请尝试到文件管理器中手动解压压缩包,并移动到'诗歌蓝版'的相应位置;如果文件管理器也解压失败,尝试换个解压app或者用电脑解压",
            //"问:搜索很慢怎么办?\r\n答:第一次搜索时搜索结果的诗歌会去建立诗歌和资源的对应关系,因此比较慢,第二次搜索同样关键字就快了",
            "问:诗人介绍怎么那么多英文和奇怪翻译?\r\n答:很多是从英文网站https://hymnary.org/谷歌翻译来的,如果你有合适的诗人介绍资源或网站,请告诉作者",
            "问:下载链接怎么失效了?\r\n答:可能是链接又被百度封了,请联系作者来解决",
            "问:诗歌标题变成数字怎么办?\r\n答:可能是版本升级造成的错误。请通过'设置'-'其他设置'-'清除app数据'重置",
            "问:手机的状态栏可以隐藏吗?\r\n答:如果你的手机是挖空/水滴/刘海屏,不建议隐藏;如果是非全面屏或弹出式前摄/屏下摄像头,可以在其他设置中隐藏状态栏,以获取更好的阅读体验",
            "问:歌词显示异常怎么办?\r\n答:可能是版本升级造成的错误。请通过'设置'-'其他设置'-'清除app数据'重置",
            "问:百度网盘下载太慢,能不能换其他网盘?\r\n答:暂时还是使用百度网盘,没有修改网盘的计划",
            "问:为什么搜索下拉会一直'加载中...'?\r\n答:可能是下拉太快导致的bug,可以往回拉一些,重新下拉进行加载",
            "问:mp3哪里下载?\r\n答:加作者的百度网盘好友私发(参考'设置'-'说明文档'-'加百度网盘好友教程')",
            "问:白版去哪了?\r\n答:现在长按pdf会显示白版,分享,历史等内容,可参考'" + SettingDialog.ALL_READ + "'-'使用说明'"
    };

    public static String[] getAskAnswer() {
        int[] inds = Tool.randomList(ASK_ANSWER.length);
        String[] res = new String[ASK_ANSWER.length];
        for (int i = 0; i < res.length; i++)
            res[i] = ASK_ANSWER[inds[i]];
        return res;
    }

    public static final String WELCOME = "欢迎使用";

    /**
     * 获取更新信息
     */
    public static String[] getUpdate(Context context) throws PackageManager.NameNotFoundException {
        String currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        String lastVersion = Setting.getValueS(Setting.LAST_VERSION_NAME);
        if (lastVersion.length() == 0) {
            Setting.updateSetting(Setting.LAST_VERSION_NAME, currentVersion);
            return new String[]{WELCOME, "欢迎使用唱诗歌app，如果你是第一次使用，请阅读'设置'-'" + SettingDialog.ALL_READ + "'-'使用说明'，以了解更多的隐藏功能"};
        }
        if (lastVersion.equals(currentVersion))
            return new String[0];
        ArrayList<String> res = new ArrayList<>();
        for (String s : VERSION_HISTORY) {
            String t = getVersionStr(s);
            if (t.equals(lastVersion)) {
                Setting.updateSetting(Setting.LAST_VERSION_NAME, currentVersion);
                res.add("{注意：}");
                res.add("如果更新中有{蓝版pdf}更新，可以清除数据重新加载，使{蓝版pdf}得更新");
                res.add("如果更新后无法搜索，点击详情无反应，则需要清除数据重新加载");
                return new String[]{"更新历史", String.join("\r\n\r\n", res)};
            }
            res.add(s);
        }
        throw new RuntimeException("更新历史异常");
    }

    private static String getVersionStr(String s) {
        String[] va = s.split(" ");
        if (va.length <= 2)
            throw new RuntimeException("更新历史异常");
        if (!va[1].equals("temp")) {
            return va[1];
        }
        return "";
    }
}
