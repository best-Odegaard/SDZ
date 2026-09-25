/**
 * 赛搭子 - 本地 Mock 数据层
 *
 * 用途：前端界面开发阶段脱离 saidazi-server 独立预览。
 * 切换：修改 utils/request.js 中的 USE_MOCK 开关即可全局启用/关闭。
 *
 * 设计约定：
 *   - 所有 mock 函数的返回结构必须与 saidazi-server 真实接口保持一致，
 *     这样在 USE_MOCK 关闭后，页面代码无需任何修改。
 *   - 字段命名严格对应后端实体（驼峰）：regEndTime / matchTime / needCount ...
 */

/* ==================== 工具 ==================== */

const DAY = 86400000

/** 相对今天偏移 n 天的 ISO 时间字符串 */
function dayOffset(n, hour = 0) {
  const d = new Date(Date.now() + n * DAY)
  d.setHours(hour, 0, 0, 0)
  return fmt(d)
}

function fmt(d) {
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:00`
}

/** 模拟网络延迟 */
function delay(data, ms = 260) {
  return new Promise((resolve) => setTimeout(() => resolve(data), ms))
}

/** 分页切片，返回后端风格 { list, total, page, size } */
function paginate(arr, page = 1, size = 10) {
  const p = Number(page) || 1
  const s = Number(size) || 10
  return {
    list: arr.slice((p - 1) * s, p * s),
    total: arr.length,
    page: p,
    size: s,
  }
}

/** 仅保留真正有值的查询条件（避免 undefined 参与过滤） */
function pickQuery(params = {}) {
  const q = { ...params }
  Object.keys(q).forEach((k) => {
    if (q[k] === null || q[k] === undefined || q[k] === '') delete q[k]
  })
  return q
}

/* ==================== 当前登录用户 ==================== */

export const currentUser = {
  id: 1,
  openId: 'mock_openid_001',
  nickname: '林小赛',
  avatarUrl: '',
  phone: '138****6688',
  college: '计算机学院',
  major: '软件工程',
  grade: '大三',
  skills: ['Java', 'Vue', '算法', '数据分析'],
  interests: ['学科竞赛', '创新创业'],
  role: 0,
}

/* ==================== 赛事 ==================== */

export const competitions = [
  {
    id: 1,
    title: '2026 全国大学生数学建模竞赛',
    type: 0,
    category: '学科竞赛',
    difficulty: 3,
    collegeTag: '计算机学院',
    organizer: '中国工业与应用数学学会',
    regStartTime: dayOffset(-12, 9),
    regEndTime: dayOffset(6, 18),
    matchTime: dayOffset(28, 9),
    description:
      '全国大学生数学建模竞赛创办于 1992 年，每年一届，是目前国内规模最大的基础性学科竞赛。\n\n【参赛要求】\n3 人一组，需具备建模、编程、论文写作三项能力分工，鼓励跨专业组队。\n\n【奖项设置】\n全国一等奖、二等奖；赛区一等奖、二等奖、三等奖。',
    regInfo: '登录官网 https://www.mcm.edu.cn 注册报名，需缴纳报名费 100 元/队。\n报名截止前可修改队员信息，截止后不可变更。',
    coverUrl: '',
    status: 1,
    viewCount: 1286,
    favoriteCount: 342,
  },
  {
    id: 2,
    title: '"互联网+" 大学生创新创业大赛（校赛）',
    type: 0,
    category: '创新创业',
    difficulty: 2,
    collegeTag: '创新创业学院',
    organizer: '校创新创业学院',
    regStartTime: dayOffset(-20, 9),
    regEndTime: dayOffset(-2, 18),
    matchTime: dayOffset(15, 9),
    description:
      '中国国际大学生创新大赛校级选拔赛，分高教主赛道、青年红色筑梦之旅赛道。\n\n【参赛要求】\n需提交商业计划书 + 路演 PPT，团队 3-8 人，鼓励跨学院组队。',
    regInfo: '校内平台报名：登录教务系统 → 竞赛报名 → 选择本赛事。',
    coverUrl: '',
    status: 1,
    viewCount: 964,
    favoriteCount: 208,
  },
  {
    id: 3,
    title: '中国大学生计算机设计大赛',
    type: 1,
    category: '学科竞赛',
    difficulty: 3,
    collegeTag: '',
    organizer: '教育部高等学校计算机类专业教学指导委员会',
    regStartTime: dayOffset(3, 9),
    regEndTime: dayOffset(25, 18),
    matchTime: dayOffset(60, 9),
    description:
      '大赛分软件应用与开发、微课与教学辅助、物联网应用、大数据应用等 11 个大类。\n\n【参赛要求】\n每队 1-3 人，需有指导教师，作品须为原创。',
    regInfo: '官网 https://jsjds.blcu.edu.cn 在线报名，由学校统一组织。',
    coverUrl: '',
    status: 1,
    viewCount: 742,
    favoriteCount: 156,
  },
  {
    id: 4,
    title: '校园十佳歌手大赛',
    type: 0,
    category: '文艺活动',
    difficulty: 1,
    collegeTag: '艺术学院',
    organizer: '校学生会文艺部',
    regStartTime: dayOffset(-3, 9),
    regEndTime: dayOffset(9, 20),
    matchTime: dayOffset(20, 19),
    description: '一年一度的校园歌手大赛，面向全校本硕博学生，不限唱法、不限语种。',
    regInfo: '关注「校学生会」公众号，后台回复「歌手大赛」获取报名表。',
    coverUrl: '',
    status: 1,
    viewCount: 1534,
    favoriteCount: 412,
  },
  {
    id: 5,
    title: '蓝桥杯全国软件和信息技术专业人才大赛',
    type: 1,
    category: '学科竞赛',
    difficulty: 2,
    collegeTag: '',
    organizer: '工业和信息化部人才交流中心',
    regStartTime: dayOffset(8, 9),
    regEndTime: dayOffset(38, 18),
    matchTime: dayOffset(75, 9),
    description:
      '分为 C/C++、Java、Python 等程序设计组别，以及嵌入式、单片机等电子类组别。\n\n【参赛要求】\n个人赛，需通过校内选拔或直接官网报名。',
    regInfo: '官网 https://dasai.lanqiao.cn 注册报名，报名费 300 元/人。',
    coverUrl: '',
    status: 1,
    viewCount: 2108,
    favoriteCount: 587,
  },
  {
    id: 6,
    title: '全国大学生电子设计竞赛',
    type: 1,
    category: '学科竞赛',
    difficulty: 3,
    collegeTag: '',
    organizer: '教育部高等教育司',
    regStartTime: dayOffset(-45, 9),
    regEndTime: dayOffset(-15, 18),
    matchTime: dayOffset(-8, 8),
    description: '面向电子信息类专业的全国性赛事，采用「半封闭、相对集中」的竞赛方式，为期四天三夜。',
    regInfo: '由学校统一组队报名，需具备模电/数电/单片机基础。',
    coverUrl: '',
    status: 2,
    viewCount: 863,
    favoriteCount: 97,
  },
  {
    id: 7,
    title: '校运动会篮球联赛',
    type: 0,
    category: '体育竞技',
    difficulty: 1,
    collegeTag: '体育学院',
    organizer: '校体育运动委员会',
    regStartTime: dayOffset(-5, 9),
    regEndTime: dayOffset(4, 18),
    matchTime: dayOffset(12, 15),
    description: '各学院组队参赛，5v5 全场制，采用小组循环 + 淘汰赛赛制。',
    regInfo: '以学院为单位统一报名，联系人：体育学院学生会。',
    coverUrl: '',
    status: 1,
    viewCount: 654,
    favoriteCount: 121,
  },
  {
    id: 8,
    title: 'ACM-ICPC 国际大学生程序设计竞赛（亚洲区）',
    type: 1,
    category: '学科竞赛',
    difficulty: 3,
    collegeTag: '',
    organizer: 'ICPC Foundation',
    regStartTime: dayOffset(15, 9),
    regEndTime: dayOffset(50, 18),
    matchTime: dayOffset(95, 9),
    description: '全球最具影响力的大学生程序设计竞赛，3 人一队，5 小时内解决 8-13 道算法题。',
    regInfo: '官网 https://icpc.global 注册，需通过网络预选赛获得现场赛资格。',
    coverUrl: '',
    status: 1,
    viewCount: 1120,
    favoriteCount: 289,
  },
]

/* ==================== 组队 ==================== */

export const teams = [
  {
    id: 1,
    competitionId: 1,
    competitionTitle: '2026 全国大学生数学建模竞赛',
    userId: 2,
    publisher: {
      id: 2,
      nickname: '陈数模',
      avatarUrl: '',
      college: '数学学院',
      major: '应用数学',
      grade: '大三',
      skills: ['数学建模', 'MATLAB', '论文写作'],
    },
    title: '数模国赛缺一个负责编程的队友',
    needCount: 1,
    currentCount: 2,
    skillsRequired: ['Python', 'MATLAB', '算法'],
    description:
      '目前已有两人：我负责建模与论文，另一位同学负责数据预处理。\n还差一位编程能力强的同学负责算法实现与结果可视化。\n\n希望你有过建模经验，能连续投入赛前两周的集训时间。',
    contact: '微信：chen_model_2026',
    requireResume: 1,
    remark: '优先考虑有获奖经历的同学',
    status: 0,
    createTime: dayOffset(-2, 14),
  },
  {
    id: 2,
    competitionId: 3,
    competitionTitle: '中国大学生计算机设计大赛',
    userId: 3,
    publisher: {
      id: 3,
      nickname: '王前端',
      avatarUrl: '',
      college: '计算机学院',
      major: '软件工程',
      grade: '大四',
      skills: ['Vue', 'TypeScript', 'Node.js'],
    },
    title: '计设赛招前端 + UI 设计，已有后端',
    needCount: 2,
    currentCount: 1,
    skillsRequired: ['Vue', 'UI设计', '前端'],
    description:
      '参赛作品：面向校园的二手交易平台。\n后端接口已基本完成，现在需要一位前端同学和一位 UI 设计同学把界面做出来。\n\n赛期约一个月，每周线上同步一次进度。',
    contact: 'QQ：88266331',
    requireResume: 1,
    remark: '',
    status: 0,
    createTime: dayOffset(-1, 20),
  },
  {
    id: 3,
    competitionId: 5,
    competitionTitle: '蓝桥杯全国软件和信息技术专业人才大赛',
    userId: 4,
    publisher: {
      id: 4,
      nickname: '李算法',
      avatarUrl: '',
      college: '计算机学院',
      major: '计算机科学与技术',
      grade: '大二',
      skills: ['C++', '算法', '数据结构'],
    },
    title: '组队刷题冲蓝桥杯省一，求搭子',
    needCount: 3,
    currentCount: 2,
    skillsRequired: ['C++', 'Java', '算法'],
    description: '打算组一个 4 人刷题小组，每周固定晚上一起做往年真题 + 复盘。\n不要求基础多好，能坚持就行。',
    contact: '微信群：蓝桥杯冲刺小组',
    requireResume: 0,
    remark: '主要是互相监督，不强制参加',
    status: 0,
    createTime: dayOffset(-4, 21),
  },
  {
    id: 4,
    competitionId: 2,
    competitionTitle: '"互联网+" 大学生创新创业大赛（校赛）',
    userId: 5,
    publisher: {
      id: 5,
      nickname: '赵创业',
      avatarUrl: '',
      college: '经济管理学院',
      major: '工商管理',
      grade: '大三',
      skills: ['商业计划书', '市场调研', '路演'],
    },
    title: '互联网+校赛，项目已立项，招技术合伙人',
    needCount: 2,
    currentCount: 3,
    skillsRequired: ['产品设计', 'Flutter', '后端'],
    description:
      '项目方向：校园共享自习室预约系统。已拿到学院孵化支持，缺技术人员落地产品。\n\n希望你能长期投入，后续可一起走省赛。',
    contact: '手机：150****2233',
    requireResume: 1,
    remark: '有股权激励意向可详谈',
    status: 1,
    createTime: dayOffset(-9, 11),
  },
  {
    id: 5,
    competitionId: 7,
    competitionTitle: '校运动会篮球联赛',
    userId: 6,
    publisher: {
      id: 6,
      nickname: '周中锋',
      avatarUrl: '',
      college: '机械工程学院',
      major: '机械设计',
      grade: '大二',
      skills: ['篮球', '团队协作'],
    },
    title: '机械学院篮球队缺 2 人，内线优先',
    needCount: 2,
    currentCount: 5,
    skillsRequired: ['篮球'],
    description: '每周训练两次，比赛在两周后开始。缺内线球员，身高 175+ 优先。',
    contact: '加群：机械篮球 2026',
    requireResume: 0,
    remark: '',
    status: 0,
    createTime: dayOffset(-1, 10),
  },
  {
    id: 6,
    competitionId: 1,
    competitionTitle: '2026 全国大学生数学建模竞赛',
    userId: 7,
    publisher: {
      id: 7,
      nickname: '孙论文',
      avatarUrl: '',
      college: '物理学院',
      major: '应用物理',
      grade: '大四',
      skills: ['LaTeX', '论文写作', '数据分析'],
    },
    title: '找两位队友一起打数模，我负责写论文',
    needCount: 2,
    currentCount: 1,
    skillsRequired: ['数学建模', 'Python'],
    description: '去年拿过赛区二等奖，今年想冲国奖。我负责论文与排版，需要建模和编程各一人。',
    contact: '邮箱：sun@example.edu',
    requireResume: 1,
    remark: '希望有完整参赛经历',
    status: 0,
    createTime: dayOffset(-6, 16),
  },
]

/* ==================== 简历 ==================== */

export const resume = {
  id: 1,
  userId: 1,
  name: '林小赛',
  collegeMajor: '计算机学院 · 软件工程',
  grade: '大三',
  skills: ['Java', 'Vue', '算法', '数据分析', 'MySQL'],
  experience:
    '2025 年 全国大学生数学建模竞赛 赛区二等奖（负责编程与可视化）\n2025 年 校级程序设计大赛 一等奖\n2024 年 参与校级科研训练项目「校园二手交易平台」后端开发',
  introduction:
    '计算机学院软件工程专业大三学生，主要方向是后端开发与算法。\n喜欢把复杂问题拆解成可执行的步骤，也享受和队友一起把项目做出来的过程。\n平时会写技术博客记录踩坑，目前正在准备蓝桥杯与数模国赛。',
  attachmentUrl: '',
  attachmentName: '林小赛-简历.pdf',
  status: 1,
  updateTime: dayOffset(-3, 15),
}

/** 收到的简历（申请人） */
export const receivedResumes = [
  {
    id: 11,
    teamId: 1,
    teamTitle: '数模国赛缺一个负责编程的队友',
    applicantId: 11,
    applicant: {
      id: 11,
      nickname: '黄程序',
      avatarUrl: '',
      college: '计算机学院',
      major: '人工智能',
      grade: '大三',
    },
    matchScore: 86.5,
    status: 0,
    createTime: dayOffset(-1, 19),
    resume: {
      name: '黄程序',
      collegeMajor: '计算机学院 · 人工智能',
      grade: '大三',
      skills: ['Python', 'PyTorch', '算法', '数据分析'],
      experience: '2025 年 数模国赛 赛区三等奖；2025 年 天池算法赛 前 5%',
      introduction: '熟悉 Python 科学计算栈，做过 3 次建模竞赛，能独立完成算法实现与图表输出。',
      attachmentUrl: '',
      attachmentName: '黄程序-简历.pdf',
    },
  },
  {
    id: 12,
    teamId: 1,
    teamTitle: '数模国赛缺一个负责编程的队友',
    applicantId: 12,
    applicant: {
      id: 12,
      nickname: '吴统计',
      avatarUrl: '',
      college: '数学学院',
      major: '统计学',
      grade: '大四',
    },
    matchScore: 72.0,
    status: 0,
    createTime: dayOffset(-2, 10),
    resume: {
      name: '吴统计',
      collegeMajor: '数学学院 · 统计学',
      grade: '大四',
      skills: ['R', 'SPSS', '数学建模'],
      experience: '连续两年参加数模校赛，获校内一等奖',
      introduction: '统计基础扎实，擅长数据处理与模型检验，编程以 R 为主。',
      attachmentUrl: '',
      attachmentName: '',
    },
  },
  {
    id: 13,
    teamId: 1,
    teamTitle: '数模国赛缺一个负责编程的队友',
    applicantId: 13,
    applicant: {
      id: 13,
      nickname: '郑可视化',
      avatarUrl: '',
      college: '计算机学院',
      major: '数字媒体技术',
      grade: '大二',
    },
    matchScore: 64.5,
    status: 1,
    createTime: dayOffset(-5, 13),
    resume: {
      name: '郑可视化',
      collegeMajor: '计算机学院 · 数字媒体技术',
      grade: '大二',
      skills: ['Python', 'ECharts', 'UI设计'],
      experience: '校级数据可视化比赛二等奖',
      introduction: '擅长把数据做成好看的图表，代码能力一般但学习快。',
      attachmentUrl: '',
      attachmentName: '',
    },
  },
]

/* ==================== 帖子 / 社区 ==================== */

export const posts = [
  {
    id: 1,
    userId: 21,
    author: { id: 21, nickname: '数模老油条', avatarUrl: '', college: '数学学院', grade: '研一' },
    title: '数模国赛拿了国二，分享一下我们队的备赛方法',
    category: 2,
    content:
      '先说结论：数模不是靠临场发挥，是靠赛前把「模板」准备好。\n\n一、分工要明确\n建模、编程、论文三个人，但不要完全割裂。我们队的做法是：建模的同学也要会写代码，编程的同学也要理解模型。\n\n二、赛前准备清单\n1. 论文 LaTeX 模板（摘要页、目录、图表格式全部定好）\n2. 常用算法代码库（灰色预测、层次分析、遗传算法、模拟退火）\n3. 数据可视化脚本（matplotlib 风格统一）\n\n三、写作节奏\n第一天定题 + 建模框架；第二天跑通模型出结果；第三天全力写论文 + 摘要反复打磨。\n摘要至少改 10 遍，评委第一眼看的就是摘要。\n\n四、踩过的坑\n- 不要把时间花在追求模型的复杂度上，能自圆其说比模型高级更重要\n- 图表一定要有编号和标题，论文格式分扣起来很亏\n- 第三天千万别再改模型了，来不及',
    images: [],
    allowComment: 1,
    likeCount: 128,
    commentCount: 2,
    liked: false,
    status: 1,
    createTime: dayOffset(-3, 20),
  },
  {
    id: 2,
    userId: 22,
    author: { id: 22, nickname: '蓝桥小萌新', avatarUrl: '', college: '计算机学院', grade: '大二' },
    title: '求问：蓝桥杯省赛用 Java 还是 C++ 更有优势？',
    category: 1,
    content:
      '目前 Java 和 C++ 都学过一点，Java 更熟一些（写过 Spring Boot 项目），C++ 只学到 STL 的基础。\n\n想问问有经验的同学，蓝桥杯省赛用哪个语言更容易拿奖？\n听说 C++ 运行快不容易超时，但是 Java 写起来舒服，纠结中……',
    images: [],
    allowComment: 1,
    likeCount: 34,
    commentCount: 3,
    liked: true,
    status: 1,
    createTime: dayOffset(-1, 16),
  },
  {
    id: 3,
    userId: 23,
    author: { id: 23, nickname: '资料搬运工', avatarUrl: '', college: '计算机学院', grade: '大三' },
    title: '【资料分享】整理了一份算法竞赛常用模板合集',
    category: 3,
    content:
      '花了两周时间把常用的算法模板整理成一个仓库，包含：\n\n- 基础：排序、二分、前缀和、差分\n- 图论：最短路（Dijkstra/SPFA/Floyd）、最小生成树、拓扑排序\n- 动态规划：背包、区间 DP、树形 DP、状态压缩\n- 数据结构：并查集、线段树、树状数组、单调栈\n- 字符串：KMP、字典树、字符串哈希\n\n每个模板都附了例题链接和复杂度说明。\n需要的同学评论区留言，我私发链接。',
    images: [],
    allowComment: 1,
    likeCount: 256,
    commentCount: 1,
    liked: false,
    status: 1,
    createTime: dayOffset(-6, 11),
  },
  {
    id: 4,
    userId: 24,
    author: { id: 24, nickname: '路人甲', avatarUrl: '', college: '外国语学院', grade: '大一' },
    title: '大一新生想参加竞赛，但是不知道从哪里开始',
    category: 1,
    content:
      '刚入学，看到学长学姐都在打各种竞赛，有点心动但是完全没头绪。\n\n我是文科专业的，是不是基本没有能参加的竞赛？\n如果有的话，希望能推荐一些门槛低、适合新手的。谢谢大家！',
    images: [],
    allowComment: 1,
    likeCount: 47,
    commentCount: 2,
    liked: false,
    status: 1,
    createTime: dayOffset(-2, 22),
  },
  {
    id: 5,
    userId: 25,
    author: { id: 25, nickname: '吃瓜群众', avatarUrl: '', college: '艺术学院', grade: '大二' },
    title: '今年的十佳歌手大赛报名好像提前了？',
    category: 4,
    content: '刚看到公众号推文，报名截止比去年早了一周。想参加的同学记得别错过时间。',
    images: [],
    allowComment: 1,
    likeCount: 19,
    commentCount: 0,
    liked: false,
    status: 1,
    createTime: dayOffset(0, 9),
  },
  {
    id: 6,
    userId: 26,
    author: { id: 26, nickname: '挑战杯老兵', avatarUrl: '', college: '经济管理学院', grade: '大四' },
    title: '挑战杯 VS 互联网+，到底该打哪个？',
    category: 4,
    content:
      '两个都参加过，简单说说区别：\n\n挑战杯更看重学术性和成果的深度，评委偏学术背景；\n互联网+更看重商业价值和落地可行性，评委里投资人比较多。\n\n如果是技术型项目（有论文/专利），建议打挑战杯；\n如果是产品型项目（有用户/营收数据），建议打互联网+。\n\n当然两个都报也行，材料可以复用一部分。',
    images: [],
    allowComment: 1,
    likeCount: 93,
    commentCount: 0,
    liked: false,
    status: 1,
    createTime: dayOffset(-8, 15),
  },
]

export const comments = {
  1: [
    {
      id: 101,
      postId: 1,
      userId: 31,
      user: { id: 31, nickname: '准国奖选手', avatarUrl: '' },
      content: '摘要改 10 遍这条太真实了，我们去年就是摘要写得潦草被扣分。',
      createTime: dayOffset(-3, 21),
    },
    {
      id: 102,
      postId: 1,
      userId: 32,
      user: { id: 32, nickname: '匿名同学', avatarUrl: '' },
      content: '请问代码库是赛前自己整理还是直接用网上的模板？',
      createTime: dayOffset(-2, 10),
    },
  ],
  2: [
    {
      id: 201,
      postId: 2,
      userId: 33,
      user: { id: 33, nickname: 'C++选手', avatarUrl: '' },
      content: '省赛难度不高，Java 完全够用，不会超时。选你熟悉的就行。',
      createTime: dayOffset(-1, 17),
    },
    {
      id: 202,
      postId: 2,
      userId: 34,
      user: { id: 34, nickname: '双语言选手', avatarUrl: '' },
      content: '我两个都打过，建议 Java。蓝桥杯 Java 组竞争反而小一些。',
      createTime: dayOffset(-1, 18),
    },
    {
      id: 203,
      postId: 2,
      userId: 35,
      user: { id: 35, nickname: '路人乙', avatarUrl: '' },
      content: '同问，蹲一个答案。',
      createTime: dayOffset(0, 8),
    },
  ],
  3: [
    {
      id: 301,
      postId: 3,
      userId: 36,
      user: { id: 36, nickname: '求模板', avatarUrl: '' },
      content: '求一份！谢谢楼主，已关注。',
      createTime: dayOffset(-5, 14),
    },
  ],
  4: [
    {
      id: 401,
      postId: 4,
      userId: 37,
      user: { id: 37, nickname: '热心学长', avatarUrl: '' },
      content: '文科专业可以参加「互联网+」的红旅赛道，还有各类征文、演讲比赛。',
      createTime: dayOffset(-2, 23),
    },
    {
      id: 402,
      postId: 4,
      userId: 38,
      user: { id: 38, nickname: '外院学姐', avatarUrl: '' },
      content: '推荐先参加校级的比赛练手，比如外语演讲比赛、创新创业校赛，门槛很低。',
      createTime: dayOffset(-1, 9),
    },
  ],
  5: [],
  6: [],
}

/* ==================== 往届成果 ==================== */

export const achievements = {
  1: [
    {
      id: 1,
      competitionId: 1,
      teamName: '求是队',
      award: '全国二等奖',
      introduction: '针对城市交通流量预测问题，构建了基于时空图神经网络的组合模型，论文获评优秀论文。',
      members: '张一鸣、李思远、王雨桐',
      year: '2025',
    },
    {
      id: 2,
      competitionId: 1,
      teamName: '启航队',
      award: '赛区一等奖',
      introduction: '以「光伏板清洁机器人路径规划」为题，采用改进蚁群算法完成最优路径求解。',
      members: '陈昊、刘子涵、周欣然',
      year: '2024',
    },
  ],
  3: [
    {
      id: 3,
      competitionId: 3,
      teamName: '码上开花',
      award: '国家级三等奖',
      introduction: '作品《校园共享自习室预约系统》，实现座位实时可视化与智能推荐。',
      members: '孙浩、郑雪、马腾',
      year: '2025',
    },
  ],
}

/* ==================== 收藏 ==================== */

export let favorites = [
  { id: 1, userId: 1, competitionId: 1, createTime: dayOffset(-4, 20), competition: null },
  { id: 2, userId: 1, competitionId: 5, createTime: dayOffset(-1, 12), competition: null },
]

/* ==================== 提醒 ==================== */

export let reminders = [
  {
    id: 1,
    userId: 1,
    competitionId: 1,
    competitionTitle: '2026 全国大学生数学建模竞赛',
    remindTime: dayOffset(6, 9),
    remindType: 2,
    status: 0,
    isPushed: 0,
    createTime: dayOffset(-4, 20),
  },
  {
    id: 2,
    userId: 1,
    competitionId: 3,
    competitionTitle: '中国大学生计算机设计大赛',
    remindTime: dayOffset(3, 9),
    remindType: 1,
    status: 0,
    isPushed: 0,
    createTime: dayOffset(-2, 11),
  },
  {
    id: 3,
    userId: 1,
    competitionId: 5,
    competitionTitle: '蓝桥杯全国软件和信息技术专业人才大赛',
    remindTime: dayOffset(8, 9),
    remindType: 1,
    status: 1,
    isPushed: 1,
    createTime: dayOffset(-10, 9),
  },
]

/* ==================== 我的组队 / 我的申请 ==================== */

export const myPublishedTeams = [teams[0]]
export const myAppliedTeams = [
  {
    id: 1,
    teamId: 2,
    teamTitle: '计设赛招前端 + UI 设计，已有后端',
    competitionTitle: '中国大学生计算机设计大赛',
    matchScore: 78.5,
    status: 0,
    createTime: dayOffset(-1, 21),
  },
  {
    id: 2,
    teamId: 3,
    teamTitle: '组队刷题冲蓝桥杯省一，求搭子',
    competitionTitle: '蓝桥杯全国软件和信息技术专业人才大赛',
    matchScore: 91.0,
    status: 1,
    createTime: dayOffset(-3, 19),
  },
  {
    id: 3,
    teamId: 6,
    teamTitle: '找两位队友一起打数模，我负责写论文',
    competitionTitle: '2026 全国大学生数学建模竞赛',
    matchScore: 68.0,
    status: 2,
    createTime: dayOffset(-5, 14),
  },
]

/* ==================== 智能匹配结果 ==================== */

export const matchTeams = [
  { ...teams[0], matchScore: 92.5, matchTags: ['Python', '算法'] },
  { ...teams[5], matchScore: 85.0, matchTags: ['数学建模'] },
  { ...teams[2], matchScore: 74.5, matchTags: ['算法'] },
]

export const matchMates = [
  {
    userId: 41,
    nickname: '黄程序',
    avatarUrl: '',
    college: '计算机学院',
    major: '人工智能',
    grade: '大三',
    skills: ['Python', 'PyTorch', '算法', '数据分析'],
    matchScore: 88.0,
    matchTags: ['Python', '算法', '数据分析'],
    competitionTitle: '2026 全国大学生数学建模竞赛',
  },
  {
    userId: 42,
    nickname: '陈数模',
    avatarUrl: '',
    college: '数学学院',
    major: '应用数学',
    grade: '大三',
    skills: ['数学建模', 'MATLAB', '论文写作'],
    matchScore: 76.5,
    matchTags: ['数学建模'],
    competitionTitle: '2026 全国大学生数学建模竞赛',
  },
  {
    userId: 43,
    nickname: '王前端',
    avatarUrl: '',
    college: '计算机学院',
    major: '软件工程',
    grade: '大四',
    skills: ['Vue', 'TypeScript', 'Node.js'],
    matchScore: 69.0,
    matchTags: [],
    competitionTitle: '中国大学生计算机设计大赛',
  },
]

/* ==================== 统计 ==================== */

export const myStats = {
  teamCount: 4,
  postCount: 3,
  favoriteCount: 12,
  achievementCount: 2,
}

/* ==================== Mock 查询接口 ==================== */

export const mockApi = {
  /* ---------- 用户 ---------- */
  userInfo() {
    return delay({ ...currentUser })
  },

  /* ---------- 赛事 ---------- */
  competitionList(params = {}) {
    const q = pickQuery(params)
    let rows = competitions.slice()
    if (q.type !== undefined) rows = rows.filter((r) => Number(r.type) === Number(q.type))
    if (q.status !== undefined) rows = rows.filter((r) => Number(r.status) === Number(q.status))
    if (q.category) rows = rows.filter((r) => r.category === q.category)
    if (q.difficulty) rows = rows.filter((r) => Number(r.difficulty) === Number(q.difficulty))
    if (q.collegeTag) rows = rows.filter((r) => r.collegeTag === q.collegeTag)
    rows.sort((a, b) => b.id - a.id)
    return delay(paginate(rows, q.page, q.size))
  },

  competitionDetail(id) {
    const item = competitions.find((c) => c.id === Number(id))
    if (!item) return Promise.reject({ code: 404, message: '赛事不存在' })
    return delay({ ...item })
  },

  competitionSearch(keyword) {
    const kw = String(keyword || '').trim()
    if (!kw) return delay([])
    const rows = competitions.filter(
      (c) => c.title.includes(kw) || (c.organizer || '').includes(kw) || (c.category || '').includes(kw)
    )
    return delay(rows)
  },

  /** 赛事日历：按期数返回 { 'YYYY-MM-DD': [{...}] } */
  competitionCalendar(month) {
    const target = month || fmt(new Date()).slice(0, 7)
    const map = {}
    competitions.forEach((c) => {
      const nodes = [
        { date: (c.regStartTime || '').slice(0, 10), type: '报名开始', title: c.title, id: c.id },
        { date: (c.regEndTime || '').slice(0, 10), type: '报名截止', title: c.title, id: c.id },
        { date: (c.matchTime || '').slice(0, 10), type: '比赛', title: c.title, id: c.id },
      ]
      nodes.forEach((n) => {
        if (!n.date || !n.date.startsWith(target)) return
        if (!map[n.date]) map[n.date] = []
        map[n.date].push(n)
      })
    })
    return delay({ month: target, days: map })
  },

  achievementList(competitionId) {
    return delay(achievements[Number(competitionId)] || [])
  },

  /* ---------- 收藏 ---------- */
  favoriteList() {
    const rows = favorites.map((f) => ({
      ...f,
      competition: competitions.find((c) => c.id === f.competitionId) || null,
    }))
    return delay(rows)
  },

  favoriteCheck(competitionId) {
    return delay(favorites.some((f) => f.competitionId === Number(competitionId)))
  },

  favoriteAdd(competitionId) {
    const cid = Number(competitionId)
    if (!favorites.some((f) => f.competitionId === cid)) {
      favorites = [
        ...favorites,
        { id: Date.now(), userId: 1, competitionId: cid, createTime: fmt(new Date()), competition: null },
      ]
    }
    return delay(true)
  },

  favoriteRemove(competitionId) {
    const cid = Number(competitionId)
    favorites = favorites.filter((f) => f.competitionId !== cid)
    return delay(true)
  },

  /* ---------- 提醒 ---------- */
  reminderList() {
    return delay(reminders.slice())
  },

  reminderAdd(data = {}) {
    const c = competitions.find((x) => x.id === Number(data.competitionId))
    const item = {
      id: Date.now(),
      userId: 1,
      competitionId: Number(data.competitionId),
      competitionTitle: c ? c.title : '未知赛事',
      remindTime: data.remindTime || dayOffset(1, 9),
      remindType: Number(data.remindType) || 1,
      status: 0,
      isPushed: 0,
      createTime: fmt(new Date()),
    }
    reminders = [...reminders, item]
    return delay(item)
  },

  reminderRemove(id) {
    reminders = reminders.filter((r) => r.id !== Number(id))
    return delay(true)
  },

  /* ---------- 组队 ---------- */
  teamList(params = {}) {
    const q = pickQuery(params)
    let rows = teams.slice()
    if (q.competitionId) rows = rows.filter((t) => t.competitionId === Number(q.competitionId))
    if (q.status !== undefined) rows = rows.filter((t) => Number(t.status) === Number(q.status))
    if (q.skill) {
      const kw = String(q.skill).toLowerCase()
      rows = rows.filter(
        (t) =>
          t.title.toLowerCase().includes(kw) ||
          (t.skillsRequired || []).some((s) => s.toLowerCase().includes(kw))
      )
    }
    if (q.grade) rows = rows.filter((t) => t.publisher.grade === q.grade)
    rows.sort((a, b) => b.id - a.id)
    return delay(paginate(rows, q.page, q.size))
  },

  teamDetail(id) {
    const item = teams.find((t) => t.id === Number(id))
    if (!item) return Promise.reject({ code: 404, message: '组队不存在' })
    return delay({
      ...item,
      // 模拟队友列表（队长 + 已加入成员）
      members: [
        { userId: item.userId, nickname: item.publisher.nickname, role: '队长', ...item.publisher },
        { userId: item.userId + 100, nickname: '已加入的同学', role: '队员', college: '计算机学院', grade: '大二' },
      ].slice(0, item.currentCount),
    })
  },

  teamApply(data = {}) {
    return delay({ id: Date.now(), ...data, status: 0 })
  },

  teamPublish(data = {}) {
    return delay({ id: Date.now(), ...data, status: 0 })
  },

  myPublish() {
    return delay(myPublishedTeams.slice())
  },

  myApply() {
    return delay(myAppliedTeams.slice())
  },

  receivedApplications() {
    return delay(receivedResumes.slice())
  },

  processApplication(data = {}) {
    const target = receivedResumes.find((r) => r.id === Number(data.applicationId))
    if (target) target.status = Number(data.status)
    return delay(true)
  },

  /* ---------- 智能匹配 ---------- */
  matchTeams() {
    return delay(matchTeams.slice())
  },

  matchMates() {
    return delay(matchMates.slice())
  },

  /* ---------- 简历 ---------- */
  resumeMy() {
    return delay({ ...resume })
  },

  resumeSave(data = {}) {
    Object.assign(resume, data, { updateTime: fmt(new Date()) })
    return delay({ ...resume })
  },

  resumeByUserId() {
    return delay({ ...resume })
  },

  sentHistory() {
    return delay(
      myAppliedTeams.map((a) => ({
        id: a.id,
        teamTitle: a.teamTitle,
        competitionTitle: a.competitionTitle,
        status: a.status,
        sendTime: a.createTime,
      }))
    )
  },

  /* ---------- 帖子 ---------- */
  postList(params = {}) {
    const q = pickQuery(params)
    let rows = posts.filter((p) => p.status === 1)
    if (q.category) rows = rows.filter((p) => Number(p.category) === Number(q.category))
    if (q.keyword) rows = rows.filter((p) => p.title.includes(q.keyword))
    rows.sort((a, b) => b.id - a.id)
    return delay(paginate(rows, q.page, q.size))
  },

  postDetail(id) {
    const item = posts.find((p) => p.id === Number(id))
    if (!item) return Promise.reject({ code: 404, message: '帖子不存在' })
    return delay({ ...item })
  },

  postMy() {
    return delay(posts.filter((p) => p.userId <= 30).slice(0, 3))
  },

  postPublish(data = {}) {
    const item = {
      id: Date.now(),
      userId: 1,
      author: { id: 1, nickname: currentUser.nickname, avatarUrl: '', college: currentUser.college, grade: currentUser.grade },
      title: data.title || '',
      category: Number(data.category) || 4,
      content: data.content || '',
      images: data.images || [],
      allowComment: 1,
      likeCount: 0,
      commentCount: 0,
      liked: false,
      status: 1,
      createTime: fmt(new Date()),
    }
    posts.unshift(item)
    return delay(item)
  },

  postLike(id) {
    const item = posts.find((p) => p.id === Number(id))
    if (item) {
      item.liked = !item.liked
      item.likeCount += item.liked ? 1 : -1
    }
    return delay(item ? { liked: item.liked, likeCount: item.likeCount } : {})
  },

  postComments(id) {
    return delay(comments[Number(id)] || [])
  },

  postComment(id, data = {}) {
    const pid = Number(id)
    if (!comments[pid]) comments[pid] = []
    const item = {
      id: Date.now(),
      postId: pid,
      userId: 1,
      user: { id: 1, nickname: currentUser.nickname, avatarUrl: '' },
      content: data.content || '',
      createTime: fmt(new Date()),
    }
    comments[pid].push(item)
    const post = posts.find((p) => p.id === pid)
    if (post) post.commentCount = comments[pid].length
    return delay(item)
  },

  /* ---------- 个人统计 ---------- */
  myStats() {
    return delay({ ...myStats })
  },
}

export default mockApi
