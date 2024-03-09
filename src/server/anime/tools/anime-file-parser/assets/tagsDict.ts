const dict = {
  group: [
    { from: /DMHY/gi, to: "動漫花園" },
    { from: /SHIGURE/gi, to: "時雨初空" },
    { from: /HYSUB/gi, to: "幻樱字幕组" },
    { from: /BeanSub/gi, to: "豌豆字幕组" },
    { from: /FZSD/gi, to: "风之圣殿" },
    { from: /FLsonw|FLsnow/gi, to: "雪飘工作室" },
    { from: /WMSUB/gi, to: "风车字幕组" },
    { from: /DBD-Raws/gi, to: "DBD 制作组" },
    { from: /(\[|)UHA-WING(S|)/gi, to: "悠哈璃羽字幕社" },
    { from: /SweetSub/gi, to: "SweetSub 字幕组" },
    { from: /MMSUB/gi, to: "MMSUB" },
    { from: /LoliHouse/gi, to: "LoliHouse 压制组" },
    { from: /SFEO-Raws/gi, to: "SFEO-Raws 压制组" },
    { from: /SumiSora/gi, to: "澄空学园" },
    { from: /YUI-7/gi, to: "西农YUI汉化组" },
    { from: /Skymoon-Raws/gi, to: "天月動漫&發佈組" },
    { from: /PCSUB/gi, to: "波洛咖啡厅" },
    { from: /Kamigami/gi, to: "诸神字幕组" },
    { from: /Mabors( |-|){0,1}(|Sub)+/gi, to: "幻之字幕组" },
    { from: /DHR\ feat\.nobady98|DHR/gi, to: "DHR动研字幕组" },
    { from: /DMG/gi, to: "动漫国字幕组" },
    { from: /FZsub/gi, to: "F宅字幕组" },
    { from: /Xrip/gi, to: "Xrip 压制组" },
    { from: /A\.I\.R\.nesSub/gi, to: "A.I.R.nesSub 字幕组" },
    {
      from: /NC-Raws|まひろ🍥|神楽坂 まひろ|推しの子/gi,
      to: "NC-Raws 搬运组",
    },
    { from: /NaN-Raws/gi, to: "NaN-Raws 搬运组" },
    { from: /Lilith-Raws/gi, to: "Lilith-Raws 搬运组" },
    { from: /云光字幕组/gi, to: "云光字幕组" },
    { from: /CASO/gi, to: "华盟字幕组" },
    { from: /Comicat(|Sub)+/gi, to: "漫猫字幕组" },
    { from: /KissSub/gi, to: "爱恋字幕组" },
    { from: /JYFanSub|KTXP|JYSUB|JYFANSUB/gi, to: "极影字幕社" },
    {
      from: /Nekomoe\ kissaten|Nekomoe\ kissatten|Nekomoenai.sub/gi,
      to: "喵萌奶茶屋",
    },
    { from: /Mmch\.sub/gi, to: "喵萌茶会字幕组" },
    { from: /SakuratoSub|sakurato\.sub|Sakurato/gi, to: "桜都字幕组" },
    { from: /STYHSub/gi, to: "霜庭云花字幕组" },
    { from: /Airota-Anonymous|Airota|Ariota/gi, to: "千夏字幕组" },
    { from: /SZW/gi, to: "森之屋动漫" },
    { from: /SAIO-Raws/gi, to: "SAIO-Raws 压制组" },
    { from: /VCB-Studio|VCB-S/gi, to: "VCB-Studio 压制组" },
    { from: /熔岩番剧库|LavaAnimeLib/gi, to: "熔岩番剧库压制" },
    { from: /异域-11番小队/gi, to: "异域-11番小队" },
    { from: /XKSub/gi, to: "星空字幕组" },
    { from: /ZeroSub/gi, to: "ZeroSub" },
    { from: /CoolComic404/gi, to: "酷漫404" },
    { from: /orion origin/gi, to: "猎户不鸽发布组" },
    { from: /MCE/gi, to: "MCE 汉化组" },
    { from: /LavaAnime/gi, to: "熔岩动画" },
    { from: /igsub/gi, to: "爱咕字幕组" },
    { from: /GST/gi, to: "GST 搬运组" },
    { from: /LxyLab/gi, to: "LxyLab 字幕组" },
    { from: /LKSUB/gi, to: "轻之国度字幕组" },
    { from: /Henshin/gi, to: "Henshin 压制组" },
    { from: /LPSub/gi, to: "离谱Sub" },
    { from: /(MingYSub|MingY)/gi, to: "MingYSub" },
    { from: /WOLF|WOLF字幕组\&RH/gi, to: "WOLF 字幕组" },
    { from: /Hakugetsu/gi, to: "白月字幕组" },
    { from: /夏沐字幕组/gi, to: "夏沐字幕组" },
    { from: /KyokuSai/gi, to: "极彩字幕组" },
    { from: /Meowskers/gi, to: "喵鲁字幕组" },
    { from: /Amor字幕组/gi, to: "Amor 字幕组" },
    { from: /POPGO/gi, to: "漫游字幕组" },
    { from: /Moozzi2/gi, to: "Moozzi2 压制组" },
    { from: /mawen1250/gi, to: "mawen1250 压制组" },
    { from: /ANK-Raws|ANK/gi, to: "ANK-Raws 压制组" },
    { from: /Snow-Raws/gi, to: "花園壓制組" },
    { from: /Haruhana/gi, to: "拨雪寻春字幕组" },
    { from: /RATH/gi, to: "拉斯观测组" },
    { from: /SBSUB/gi, to: "银色子弹字幕组" },
    { from: /织梦字幕组/gi, to: "织梦字幕组" },
    { from: /Billion Meta Lab/gi, to: "亿次研同好会" },
    { from: /LavaAnimeSub/gi, to: "熔岩动画Sub" },
    { from: /Kitaujisub/gi, to: "北宇治字幕组" },
    { from: /RHD/gi, to: "RHD字幕组" },
    { from: /CE/gi, to: "CE家族社" },
    { from: /MakariHoshiyume|MakariTsukiyo|Makari/gi, to: "茉语星梦" },
    { from: /HYO/gi, to: "花園舊社" },
    { from: /HaSub/gi, to: "花语字幕组" },
    { from: /Haretahoo/gi, to: "Haretahoo 字幕组" },
    { from: /SGS/gi, to: "SGS曙光社" },
    { from: /LF2/gi, to: "✿小花花同盟戰線✿" },
    { from: /HKG/gi, to: "HKG字幕組" },
    { from: /I\.G/gi, to: "元古I.G部落" },
    { from: /JOJO/gi, to: "JOJO的奇妙冒险吧" },
    { from: /TSDM/gi, to: "天使动漫自购 / TSDM 字幕组" },
    { from: /Romanticat/gi, to: "猫恋汉化组" },
    { from: /FreeSub/gi, to: "自由字幕组" },
    { from: /ACG7/gi, to: "雷姆利亚" },
    { from: /Lamune/gi, to: "波子汽水汉化组" },
    { from: /SC-OL/gi, to: "雪酷字幕组" },
    { from: /AcgmTHK|TUcaptions(|\(THK\))+/gi, to: "TUcaptions 字幕社" },
    { from: /EMD/gi, to: "恶魔岛字幕组" },
    { from: /NTR/gi, to: "NTR字幕组" },
    { from: /TSRJ/gi, to: "間人字幕組" },
    { from: /STK/gi, to: "生徒会字幕组" },
    { from: /Shirokoi/gi, to: "白恋动漫萝卜部" },
    { from: /DHR-Raws/gi, to: "DHR动研压制组" },
    { from: /DHRx幻之/gi, to: "DHR动研字幕组&幻之字幕组" },
    { from: /Dymy/gi, to: "Dymy字幕組" },
    { from: /7³ACG/gi, to: "7³ACG压制组" },
    { from: /Ohys-Raws/gi, to: "Ohys-Raws 搬运组" },
    { from: /Leopard-Raws/gi, to: "Leopard-Raws 搬运组" },
  ],
  source: [
    { from: /TVRip|TV/gi, to: "TV放送源" },
    { from: /WEBRip|WEB/gi, to: "流媒体源" },
    { from: /DL/g, to: "下载源" },
    { from: /BDRip|BD/gi, to: "蓝光盘源" },
    { from: /DVDRip|DVD/gi, to: "DVD 源" },
    { from: /ATX|AT-X/gi, to: "AT-X 放送源" },
    { from: /NTV/gi, to: "NTV (日本电视台) 放送源" },
    { from: /BS11/gi, to: "BS11 日本BS放送源" },
    { from: /WOWOW/gi, to: "WOWOW TV放送源" },
    { from: /MX/gi, to: "TOKYO MX TV放送源" },
    { from: /BSFUJI/gi, to: "BS富士 放送源" },
    { from: /TBS/gi, to: "TBS 放送源" },
    { from: /ABC/gi, to: "朝日放送源" },
    { from: /BSN/gi, to: "BS日本 TV放送源" },
    { from: /YTV/gi, to: "加拿大 YTV 放送源" },
    { from: /CX/gi, to: "富士电视台 放送源" },
    { from: /NHKG/gi, to: "NHK综合频道 放送源" },
    { from: /TX/gi, to: "东京电视台 放送源" },
    { from: /BSJ/gi, to: "BS东视 放送源" },
    { from: /MBS/gi, to: "MBS TV放送源" },
    { from: /Baha/gi, to: "巴哈姆特动画疯" },
    { from: /B-Global/gi, to: "哔哩哔哩国际源" },
    { from: /CR/gi, to: "Crunchyroll 源" },
    { from: /Sentai/gi, to: "Hidive 源" },
    { from: /BiliBili|BL/gi, to: "哔哩哔哩动画源" },
    { from: /(Acfun|AC)/gi, to: "Acfun 源" },
    { from: /(netflix|NF)/gi, to: "Netflix 源" },
  ],
  quality: [
    { from: /(1080P|1920(X|×|\*)(1080|816))/gi, to: "1080P" },
    { from: /1440(X|×|\*)1080/gi, to: "1080P(3:4)" },
    { from: /2160P|3840(X|×|\*)2160/gi, to: "2160P(4K)" },
    { from: /1440P|2560(X|×|\*)1440/gi, to: "1440P(2K)" },
    { from: /(720P|1280(X|×|\*)720)/gi, to: "720P" },
    { from: /(960(X|×|\*)720)/gi, to: "720P(3:4)" },
    { from: /(480P|(864|640)(X|×|\*)480)/gi, to: "480P" },
    { from: /(810P)/gi, to: "810P" },
    { from: /60fps/gi, to: "60FPS" },
    { from: /(AVC|x(\.){0,1}264|H(\.){0,1}264)/gi, to: "AVC" },
    {
      from: /(HEVC|x(\.){0,1}265|H(\.){0,1}265)/gi,
      to: "HEVC",
      noBrowser: true,
    },
    { from: /Ma10p/gi, to: "HEVC 10bit", noBrowser: true },
    { from: /Main10/gi, to: "Profile Main10", noBrowser: true },
    { from: /8Bits|8Bit/gi, to: "8bit 色深" },
    { from: /Hi10p|10Bits|10Bit/gi, to: "10位色", noBrowser: true },
    { from: /yuv420p10/gi, to: "YUV-4:2:0 10位色", noBrowser: true },
    { from: /yuv420p8/gi, to: "YUV-4:2:0 8位色" },
    { from: /(\d(AAC|ACC))|((AAC|ACC)(x|×)\d)|(ACC|AAC)/gi, to: "AAC" },
    { from: /FLAC(x|×)\d|\dFLAC|FLAC/gi, to: "Flac" },
    { from: /OPUS(x|×)\d|\dOPUS|OPUS/gi, to: "OPUS" },
    { from: /AC3/gi, to: "杜比数码环绕声", noBrowser: true },
    // { from: /(((x|h)(\.){0,1}264)|(AVC|HEVC))(_|-)(AAC|ACC)/gi, to: "AVC ACC" },
    { from: /mp4/gi, to: "MP4" },
    { from: /mkv/gi, to: "MKV", noBrowser: true },
  ],
  subtitle: [
    { from: /LavaASS/gi, to: "番剧库内封(已弃用技术)" },
    { from: /(GB_CN|GB|SC|CHS|CH(?=[^T])|简体中文|简体|简中)/gi, to: "简中" },
    { from: /(BIG5|TC|CHT)/gi, to: "繁中" },
    {
      from: /JPSC|(CHS|SC|GB)(&|,|_|\+)(JPN|JP|JAP)|简体双语/gi,
      to: "简日双语",
    },
    {
      from: /JSTC|(CHT|TC|BIG5)(&|,|_|\+)(JPN|JP|JAP)|繁日双语|繁日雙語/gi,
      to: "繁日双语",
    },
    {
      from: /(CHS|SC|GB)(&|,|_|\+)(CHT|TC|BIG5)(&|,|_|\+)(JPN|JP|JAP)|简繁日|简繁日多语|简繁日三语/gi,
      to: "简繁日多语",
    },
    { from: /CN|ZH/gi, to: "中文" },
    { from: /ENG|EN/gi, to: "英语" },
    { from: /(JPN|JAP|JP)/gi, to: "日语" },
    { from: /TH/gi, to: "泰语" },
    { from: /V2/gi, to: "第2版" },
    { from: /V3/gi, to: "第3版" },
    // { from: /Final/ig, to: "最终版本" },
    { from: /ASS(x|×)\d/gi, to: "多语言特效字幕" },
    { from: /ASS|SSA/gi, to: "特效字幕" },
    { from: /SRT(x|×)\d/gi, to: "多语言SRT字幕" },
    { from: /SRT/gi, to: "SRT字幕" },
    { from: /Sub/gi, to: "字幕" },
  ],
  other: [
    { from: /SP/gi, to: "SP" },
    { from: /OVA/gi, to: "OVA" },
    { from: /OAD/gi, to: "OAD" },
    { from: /Movie/gi, to: "剧场版" },
    { from: /Subtitles|Subtitle|Subs/gi, to: "外挂字幕合集包" },
    { from: /Fonts|Font/gi, to: "字体合集包" },
    { from: /NCOP/gi, to: "素材 - 无字 OP" },
    { from: /NCOP1/gi, to: "素材 - 无字 OP 1" },
    { from: /NCOP2/gi, to: "素材 - 无字 OP 2" },
    { from: /NCOP3/gi, to: "素材 - 无字 OP 3" },
    { from: /NCOP4/gi, to: "素材 - 无字 OP 4" },
    { from: /NCOP5/gi, to: "素材 - 无字 OP 5" },
    { from: /NCOP6/gi, to: "素材 - 无字 OP 6" },
    { from: /NCED/gi, to: "素材 - 无字 ED" },
    { from: /NCED1/gi, to: "素材 - 无字 ED 1" },
    { from: /NCED2/gi, to: "素材 - 无字 ED 2" },
    { from: /NCED3/gi, to: "素材 - 无字 ED 3" },
    { from: /NCED4/gi, to: "素材 - 无字 ED 4" },
    { from: /NCED5/gi, to: "素材 - 无字 ED 5" },
    { from: /NCED6/gi, to: "素材 - 无字 ED 6" },
  ],
  format: [
    // 此中的正则会单独对整个文件名匹配一次，不会和其他的冲突
    { from: /ass|ssa/gi, to: "ASS外挂字幕", type: "subtitle" },
    { from: /srt/gi, to: "SRT外挂字幕", type: "subtitle" },
    { from: /mp4/gi, to: "MP4视频", type: "video" },
    { from: /mkv/gi, to: "MKV视频", type: "video" },
    { from: /(png|jpg|jpeg|gif|bmp|tif|svg)/gi, to: "图片", type: "image" },
    { from: /torrent/gi, to: "种子文件", type: "torrent" },
    { from: /txt/gi, to: "TXT文档", type: "document" },
    { from: /(pdf|docx|doc)/gi, to: "文档", type: "document" },
    { from: /(mp3)/gi, to: "MP3音乐", type: "music" },
    { from: /(flac)/gi, to: "FLAC无损音乐", type: "music" },
    { from: /(zip|rar|7z)/gi, to: "压缩文件", type: "archive" },
  ],
  delete: [
    /招募(翻译|时轴后期|后期|时轴)/gi,
    /(\d{1,2}|一|四|七|十)月(新|){0,1}番/gi,
    /[A-F\d]{8}/gi, // CRC32 校验码
    /(new-ani.me)/gi,
  ],
};

export default dict;
