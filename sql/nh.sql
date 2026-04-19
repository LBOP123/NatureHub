create table QRTZ_CALENDARS
(
    sched_name    varchar(120) not null comment '调度名称',
    calendar_name varchar(200) not null comment '日历名称',
    calendar      blob         not null comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
)
    comment '日历信息表';

create table QRTZ_FIRED_TRIGGERS
(
    sched_name        varchar(120) not null comment '调度名称',
    entry_id          varchar(95)  not null comment '调度器实例id',
    trigger_name      varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group     varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    instance_name     varchar(200) not null comment '调度器实例名',
    fired_time        bigint       not null comment '触发的时间',
    sched_time        bigint       not null comment '定时器制定的时间',
    priority          int          not null comment '优先级',
    state             varchar(16)  not null comment '状态',
    job_name          varchar(200) null comment '任务名称',
    job_group         varchar(200) null comment '任务组名',
    is_nonconcurrent  varchar(1)   null comment '是否并发',
    requests_recovery varchar(1)   null comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
)
    comment '已触发的触发器表';

create table QRTZ_JOB_DETAILS
(
    sched_name        varchar(120) not null comment '调度名称',
    job_name          varchar(200) not null comment '任务名称',
    job_group         varchar(200) not null comment '任务组名',
    description       varchar(250) null comment '相关介绍',
    job_class_name    varchar(250) not null comment '执行任务类名称',
    is_durable        varchar(1)   not null comment '是否持久化',
    is_nonconcurrent  varchar(1)   not null comment '是否并发',
    is_update_data    varchar(1)   not null comment '是否更新数据',
    requests_recovery varchar(1)   not null comment '是否接受恢复执行',
    job_data          blob         null comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
)
    comment '任务详细信息表';

create table QRTZ_LOCKS
(
    sched_name varchar(120) not null comment '调度名称',
    lock_name  varchar(40)  not null comment '悲观锁名称',
    primary key (sched_name, lock_name)
)
    comment '存储的悲观锁信息表';

create table QRTZ_PAUSED_TRIGGER_GRPS
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
)
    comment '暂停的触发器表';

create table QRTZ_SCHEDULER_STATE
(
    sched_name        varchar(120) not null comment '调度名称',
    instance_name     varchar(200) not null comment '实例名称',
    last_checkin_time bigint       not null comment '上次检查时间',
    checkin_interval  bigint       not null comment '检查间隔时间',
    primary key (sched_name, instance_name)
)
    comment '调度器状态表';

create table QRTZ_TRIGGERS
(
    sched_name     varchar(120) not null comment '调度名称',
    trigger_name   varchar(200) not null comment '触发器的名字',
    trigger_group  varchar(200) not null comment '触发器所属组的名字',
    job_name       varchar(200) not null comment 'qrtz_job_details表job_name的外键',
    job_group      varchar(200) not null comment 'qrtz_job_details表job_group的外键',
    description    varchar(250) null comment '相关介绍',
    next_fire_time bigint       null comment '上一次触发时间（毫秒）',
    prev_fire_time bigint       null comment '下一次触发时间（默认为-1表示不触发）',
    priority       int          null comment '优先级',
    trigger_state  varchar(16)  not null comment '触发器状态',
    trigger_type   varchar(8)   not null comment '触发器的类型',
    start_time     bigint       not null comment '开始时间',
    end_time       bigint       null comment '结束时间',
    calendar_name  varchar(200) null comment '日程表名称',
    misfire_instr  smallint     null comment '补偿执行的策略',
    job_data       blob         null comment '存放持久化job对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_TRIGGERS_ibfk_1
        foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS (sched_name, job_name, job_group)
)
    comment '触发器详细信息表';

create table QRTZ_BLOB_TRIGGERS
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_name  varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    blob_data     blob         null comment '存放持久化Trigger对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_BLOB_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment 'Blob类型的触发器表';

create table QRTZ_CRON_TRIGGERS
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    cron_expression varchar(200) not null comment 'cron表达式',
    time_zone_id    varchar(80)  null comment '时区',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_CRON_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment 'Cron类型的触发器表';

create table QRTZ_SIMPLE_TRIGGERS
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    repeat_count    bigint       not null comment '重复的次数统计',
    repeat_interval bigint       not null comment '重复的间隔时间',
    times_triggered bigint       not null comment '已经触发的次数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_SIMPLE_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment '简单触发器的信息表';

create table QRTZ_SIMPROP_TRIGGERS
(
    sched_name    varchar(120)   not null comment '调度名称',
    trigger_name  varchar(200)   not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200)   not null comment 'qrtz_triggers表trigger_group的外键',
    str_prop_1    varchar(512)   null comment 'String类型的trigger的第一个参数',
    str_prop_2    varchar(512)   null comment 'String类型的trigger的第二个参数',
    str_prop_3    varchar(512)   null comment 'String类型的trigger的第三个参数',
    int_prop_1    int            null comment 'int类型的trigger的第一个参数',
    int_prop_2    int            null comment 'int类型的trigger的第二个参数',
    long_prop_1   bigint         null comment 'long类型的trigger的第一个参数',
    long_prop_2   bigint         null comment 'long类型的trigger的第二个参数',
    dec_prop_1    decimal(13, 4) null comment 'decimal类型的trigger的第一个参数',
    dec_prop_2    decimal(13, 4) null comment 'decimal类型的trigger的第二个参数',
    bool_prop_1   varchar(1)     null comment 'Boolean类型的trigger的第一个参数',
    bool_prop_2   varchar(1)     null comment 'Boolean类型的trigger的第二个参数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_SIMPROP_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment '同步机制的行锁表';

create index sched_name
    on QRTZ_TRIGGERS (sched_name, job_name, job_group);

create table bio_recognition
(
    id                 bigint auto_increment comment '主键ID'
        primary key,
    user_id            bigint                               null comment '用户ID',
    username           varchar(50)                          null comment '用户名',
    image_url          varchar(500)                         not null comment '图片URL',
    recognition_result varchar(200)                         null comment '识别结果（物种名称）',
    recognition_type   tinyint(1)                           null comment '识别类型【字典编码：bio_recognition_type】 1-植物 2-动物 3-其他',
    confidence         decimal(5, 2)                        null comment '置信度（0-100）',
    status             tinyint(1) default 1                 null comment '识别状态【字典编码：bio_recognition_status】 1-成功 0-失败',
    baike_info         text                                 null comment '百科信息（JSON格式）',
    all_results        text                                 null comment '所有识别结果（JSON格式）',
    is_shared          tinyint(1) default 0                 null comment '是否已分享到社群【字典编码：sys_yes_no】 0-否 1-是',
    shared_topic_id    bigint                               null comment '关联的社群话题ID',
    remark             varchar(500)                         null comment '备注',
    ip_address         varchar(50)                          null comment 'IP地址',
    create_time        datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '生物识别记录表';

create index idx_create_time
    on bio_recognition (create_time);

create index idx_recognition_result
    on bio_recognition (recognition_result);

create index idx_status
    on bio_recognition (status);

create index idx_user_id
    on bio_recognition (user_id);

create index idx_username
    on bio_recognition (username);

create table gen_table
(
    table_id          bigint auto_increment comment '编号'
        primary key,
    table_name        varchar(200) default ''     null comment '表名称',
    table_comment     varchar(500) default ''     null comment '表描述',
    sub_table_name    varchar(64)                 null comment '关联子表的表名',
    sub_table_fk_name varchar(64)                 null comment '子表关联的外键名',
    class_name        varchar(100) default ''     null comment '实体类名称',
    tpl_category      varchar(200) default 'crud' null comment '使用的模板（crud单表操作 tree树表操作）',
    tpl_web_type      varchar(30)  default ''     null comment '前端模板类型（element-ui模版 element-plus模版）',
    package_name      varchar(100)                null comment '生成包路径',
    module_name       varchar(30)                 null comment '生成模块名',
    business_name     varchar(30)                 null comment '生成业务名',
    function_name     varchar(50)                 null comment '生成功能名',
    function_author   varchar(50)                 null comment '生成功能作者',
    gen_type          char         default '0'    null comment '生成代码方式（0zip压缩包 1自定义路径）',
    gen_path          varchar(200) default '/'    null comment '生成路径（不填默认项目路径）',
    options           varchar(1000)               null comment '其它生成选项',
    create_by         varchar(64)  default ''     null comment '创建者',
    create_time       datetime                    null comment '创建时间',
    update_by         varchar(64)  default ''     null comment '更新者',
    update_time       datetime                    null comment '更新时间',
    remark            varchar(500)                null comment '备注'
)
    comment '代码生成业务表';

create table gen_table_column
(
    column_id      bigint auto_increment comment '编号'
        primary key,
    table_id       bigint                    null comment '归属表编号',
    column_name    varchar(200)              null comment '列名称',
    column_comment varchar(500)              null comment '列描述',
    column_type    varchar(100)              null comment '列类型',
    java_type      varchar(500)              null comment 'JAVA类型',
    java_field     varchar(200)              null comment 'JAVA字段名',
    is_pk          char                      null comment '是否主键（1是）',
    is_increment   char                      null comment '是否自增（1是）',
    is_required    char                      null comment '是否必填（1是）',
    is_insert      char                      null comment '是否为插入字段（1是）',
    is_edit        char                      null comment '是否编辑字段（1是）',
    is_list        char                      null comment '是否列表字段（1是）',
    is_query       char                      null comment '是否查询字段（1是）',
    query_type     varchar(200) default 'EQ' null comment '查询方式（等于、不等于、大于、小于、范围）',
    html_type      varchar(200)              null comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
    dict_type      varchar(200) default ''   null comment '字典类型',
    sort           int                       null comment '排序',
    create_by      varchar(64)  default ''   null comment '创建者',
    create_time    datetime                  null comment '创建时间',
    update_by      varchar(64)  default ''   null comment '更新者',
    update_time    datetime                  null comment '更新时间'
)
    comment '代码生成业务表字段';

create table mark3d_task
(
    id                  bigint auto_increment comment '主键ID'
        primary key,
    user_id             bigint                               null comment '用户ID',
    username            varchar(50)                          null comment '用户名',
    task_name           varchar(200)                         null comment '任务名称',
    front_image_url     varchar(500)                         not null comment '输入图片URL（七牛云）',
    back_image_url      varchar(500)                         null comment '背面图片URL（七牛云，预留）',
    top_image_url       varchar(500)                         null comment '上面图片URL（七牛云，预留）',
    side_image_url      varchar(500)                         null comment '侧面图片URL（七牛云，预留）',
    meshy_task_id       varchar(100)                         null comment '腾讯云混元3D JobId',
    task_status         char       default '1'               not null comment '任务状态（字典：mark3d_task_status）1-等待中 2-生成中 3-成功 4-失败',
    progress            int        default 0                 null comment '生成进度（0-100）',
    model_url_glb       varchar(500)                         null comment 'GLB模型地址（腾讯云临时，24小时有效）',
    model_url_glb_qiniu varchar(500)                         null comment 'GLB模型地址（七牛云持久化，永久有效）',
    model_url_obj       varchar(500)                         null comment 'OBJ模型地址（腾讯云临时）',
    thumbnail_url       varchar(500)                         null comment '预览图URL（腾讯云临时）',
    thumbnail_qiniu     varchar(500)                         null comment '预览图URL（七牛云持久化，永久有效）',
    error_message       varchar(500)                         null comment '失败错误信息',
    remark              varchar(500)                         null comment '备注',
    is_public           tinyint(1) default 0                 not null comment '是否公开展示（0-不公开 1-公开）',
    create_time         datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time         datetime                             null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '3D标注任务记录表';

create index idx_create_time
    on mark3d_task (create_time);

create index idx_is_public
    on mark3d_task (is_public);

create index idx_meshy_task_id
    on mark3d_task (meshy_task_id);

create index idx_task_status
    on mark3d_task (task_status);

create index idx_user_id
    on mark3d_task (user_id);

create index idx_username
    on mark3d_task (username);

create table nh_community_category
(
    category_id   bigint auto_increment comment '板块ID'
        primary key,
    category_code varchar(50)                           not null comment '板块代码（唯一标识）',
    category_name varchar(100)                          not null comment '板块名称',
    category_icon varchar(100)                          null comment '板块图标',
    category_desc varchar(500)                          null comment '板块描述',
    sort_order    int         default 0                 null comment '排序顺序',
    status        char        default '0'               null comment '状态：0-正常，1-停用',
    create_by     varchar(64) default ''                null comment '创建者',
    create_time   datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by     varchar(64) default ''                null comment '更新者',
    update_time   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    remark        varchar(500)                          null comment '备注',
    constraint uk_category_code
        unique (category_code)
)
    comment '社群板块分类表';

create index idx_sort
    on nh_community_category (sort_order);

create index idx_status
    on nh_community_category (status);

create table nh_community_collect
(
    collect_id  bigint auto_increment comment '收藏ID'
        primary key,
    user_id     bigint   not null comment '用户ID',
    topic_id    bigint   not null comment '话题ID',
    create_time datetime null comment '创建时间',
    constraint uk_user_topic
        unique (user_id, topic_id)
)
    comment '用户收藏表';

create index idx_topic_id
    on nh_community_collect (topic_id);

create table nh_community_comment
(
    comment_id    bigint auto_increment comment '评论ID'
        primary key,
    topic_id      bigint                  not null comment '话题ID',
    user_id       bigint                  not null comment '评论用户ID',
    user_name     varchar(64)             null comment '评论用户名',
    parent_id     bigint      default 0   null comment '父评论ID（0表示一级评论）',
    reply_to_id   bigint                  null comment '回复的用户ID',
    reply_to_name varchar(64)             null comment '回复的用户名',
    content       text                    not null comment '评论内容',
    like_count    int         default 0   null comment '点赞数',
    status        char        default '0' null comment '状态：0-正常，1-已删除，2-已屏蔽',
    create_by     varchar(64) default ''  null comment '创建者',
    create_time   datetime                null comment '创建时间',
    update_by     varchar(64) default ''  null comment '更新者',
    update_time   datetime                null comment '更新时间'
)
    comment '话题评论表';

create index idx_comment_time
    on nh_community_comment (create_time);

create index idx_comment_topic
    on nh_community_comment (topic_id, status);

create index idx_comment_user
    on nh_community_comment (user_id);

create index idx_parent_id
    on nh_community_comment (parent_id);

create index idx_topic_id
    on nh_community_comment (topic_id);

create index idx_user_id
    on nh_community_comment (user_id);

create table nh_community_like
(
    like_id     bigint auto_increment comment '点赞ID'
        primary key,
    user_id     bigint      not null comment '用户ID',
    target_type varchar(20) not null comment '目标类型（topic话题/comment评论）',
    target_id   bigint      not null comment '目标ID',
    create_time datetime    null comment '创建时间',
    constraint uk_user_target
        unique (user_id, target_type, target_id)
)
    comment '用户点赞表';

create index idx_target
    on nh_community_like (target_type, target_id);

create table nh_community_report
(
    report_id        bigint auto_increment comment '举报ID'
        primary key,
    report_type      varchar(20)                           not null comment '举报类型：0-话题，1-评论',
    target_id        bigint                                not null comment '被举报对象ID',
    target_title     varchar(500)                          null comment '被举报对象标题/内容摘要',
    target_user_id   bigint                                null comment '被举报用户ID',
    target_user_name varchar(64)                           null comment '被举报用户名',
    reporter_id      bigint      default 0                 null comment '举报人ID',
    reporter_name    varchar(64)                           null comment '举报人名称',
    reason           varchar(20)                           not null comment '举报原因：0-垃圾广告，1-辱骂攻击，2-违法违规，3-虚假信息，4-其他',
    description      varchar(1000)                         null comment '举报详细描述',
    status           varchar(20) default '0'               null comment '处理状态：0-待处理，1-已处理，2-已忽略',
    handle_result    varchar(20)                           null comment '处理结果：0-删除内容，1-禁言用户，2-警告，3-忽略',
    handle_remark    varchar(500)                          null comment '处理备注',
    handle_time      datetime                              null comment '处理时间',
    handle_by        varchar(64)                           null comment '处理人',
    create_by        varchar(64) default ''                null comment '创建者',
    create_time      datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by        varchar(64) default ''                null comment '更新者',
    update_time      datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '社群举报表';

create index idx_create_time
    on nh_community_report (create_time);

create index idx_reporter
    on nh_community_report (reporter_id);

create index idx_status
    on nh_community_report (status);

create index idx_target
    on nh_community_report (report_type, target_id);

create table nh_community_topic
(
    topic_id      bigint auto_increment comment '话题ID'
        primary key,
    user_id       bigint                            not null comment '发布用户ID',
    user_name     varchar(64)                       null comment '发布用户名',
    category      varchar(50)                       not null comment '话题分类（species_science物种科普/field_explore野外探索/identify_help鉴定求助）',
    source_type   varchar(50)    default 'original' null comment '来源类型: original-原创, observation-观察记录, identification-物种鉴定, survey-野外调查, diary-观察日志',
    source_id     bigint                            null comment '来源记录ID',
    title         varchar(200)                      not null comment '话题标题',
    content       text                              not null comment '话题内容',
    images        text                              null comment '图片路径（JSON数组）',
    tags          varchar(500)                      null comment '标签（逗号分隔）',
    view_count    int            default 0          null comment '浏览次数',
    like_count    int            default 0          null comment '点赞数',
    comment_count int            default 0          null comment '评论数',
    collect_count int            default 0          null comment '收藏数',
    share_count   int            default 0          null comment '转发数',
    hot_score     decimal(10, 2) default 0.00       null comment '热度分数',
    is_top        char           default '0'        null comment '是否置顶：0-否，1-是',
    is_essence    char           default '0'        null comment '是否精华：0-否，1-是',
    status        char           default '0'        null comment '状态：0-正常，1-关闭',
    create_by     varchar(64)    default ''         null comment '创建者',
    create_time   datetime                          null comment '创建时间',
    update_by     varchar(64)    default ''         null comment '更新者',
    update_time   datetime                          null comment '更新时间',
    remark        varchar(500)                      null comment '备注',
    audit_status  varchar(16)                       null comment '审核状态：0待审核 1通过 2拒绝',
    audit_remark  varchar(500)                      null comment '审核备注',
    audit_time    datetime                          null comment '审核时间',
    audit_by      varchar(64)                       null comment '审核人'
)
    comment '社群话题表';

create index idx_category
    on nh_community_topic (category);

create index idx_create_time
    on nh_community_topic (create_time);

create index idx_hot_score
    on nh_community_topic (hot_score);

create index idx_source
    on nh_community_topic (source_type, source_id);

create index idx_user_id
    on nh_community_topic (user_id);

create table nh_diary_record_relation
(
    id          bigint auto_increment comment 'ID'
        primary key,
    diary_id    bigint   not null comment '日志ID',
    record_id   bigint   not null comment '观察记录ID',
    create_time datetime null comment '创建时间',
    constraint uk_diary_record
        unique (diary_id, record_id)
)
    comment '日志关联观察记录表';

create index idx_diary_id
    on nh_diary_record_relation (diary_id);

create index idx_record_id
    on nh_diary_record_relation (record_id);

create table nh_field_survey
(
    survey_id       bigint auto_increment comment '调查ID'
        primary key,
    user_id         bigint                  not null comment '用户ID',
    title           varchar(200)            not null comment '调查标题',
    survey_date     date                    not null comment '调查日期',
    start_time      datetime                null comment '开始时间',
    end_time        datetime                null comment '结束时间',
    location        varchar(200)            null comment '调查地点',
    route_info      text                    null comment '路线信息(JSON格式，包含经纬度坐标数组)',
    weather         varchar(100)            null comment '天气情况',
    temperature     varchar(50)             null comment '温度',
    habitat_type    tinyint(1)              null comment '生境类型【字典编码：nh_habitat_type】 1-森林 2-草地 3-湿地 4-河流 5-湖泊 6-海洋 7-山地 8-农田 9-城市 10-其他',
    survey_method   tinyint(1)              null comment '调查方法【字典编码：nh_survey_method】 1-样线法 2-样方法 3-定点观察 4-随机游走 5-陷阱法 6-网捕法 7-声音记录 8-红外相机 9-综合调查 10-其他',
    team_members    text                    null comment '团队成员(JSON)',
    species_count   int         default 0   null comment '发现物种数',
    species_list    text                    null comment '物种列表(JSON)',
    description     text                    null comment '调查描述',
    findings        text                    null comment '主要发现',
    images          text                    null comment '图片列表(JSON)',
    attachments     text                    null comment '附件列表(JSON)',
    is_shared       tinyint(1)  default 0   null comment '是否已分享到社群【字典编码：nh_yes_no】 0-否 1-是',
    shared_topic_id bigint                  null comment '关联的社群话题ID',
    audit_status    tinyint(1)  default 0   null comment '审核状态【字典编码：nh_audit_status】 0-草稿 1-待审核 2-已通过 3-已驳回',
    audit_time      datetime                null comment '审核时间',
    audit_by        varchar(64)             null comment '审核人',
    audit_remark    varchar(500)            null comment '审核备注',
    del_flag        char        default '0' null comment '删除标志: 0-正常, 2-删除',
    create_by       varchar(64) default ''  null comment '创建者',
    create_time     datetime                null comment '创建时间',
    update_by       varchar(64) default ''  null comment '更新者',
    update_time     datetime                null comment '更新时间',
    remark          varchar(500)            null comment '备注',
    topic_id        bigint                  null
)
    comment '野外调查记录表';

create index idx_audit_status
    on nh_field_survey (audit_status);

create index idx_create_time
    on nh_field_survey (create_time);

create index idx_survey_date
    on nh_field_survey (survey_date);

create index idx_user_id
    on nh_field_survey (user_id);

create table nh_identification_vote
(
    vote_id           bigint auto_increment comment '投票ID'
        primary key,
    identification_id bigint                                not null comment '鉴定请求ID',
    user_id           bigint                                not null comment '投票用户ID',
    user_name         varchar(50)                           null comment '用户名',
    user_type         char                                  not null comment '用户类型：0=探索者,1=鉴定者',
    vote_type         char                                  not null comment '投票类型：0=同意,1=不同意',
    vote_weight       int                                   not null comment '票权重',
    vote_time         datetime                              not null comment '投票时间',
    del_flag          char        default '0'               null comment '删除标志',
    create_by         varchar(64) default ''                null comment '创建者',
    create_time       datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by         varchar(64) default ''                null comment '更新者',
    update_time       datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_identification_user
        unique (identification_id, user_id)
)
    comment '鉴定投票记录表';

create index idx_identification_id
    on nh_identification_vote (identification_id);

create index idx_user_id
    on nh_identification_vote (user_id);

create table nh_identifier_application
(
    id            bigint auto_increment
        primary key,
    user_id       bigint                                 not null,
    user_name     varchar(64)                            not null,
    nick_name     varchar(64)  default ''                null,
    real_name     varchar(50)  default ''                null,
    expertise     varchar(500)                           not null,
    bio           text                                   null,
    qualification varchar(500) default ''                null,
    experience    text                                   null,
    status        tinyint(1)   default 0                 null comment '0=待审核,1=已通过,2=已拒绝',
    reject_reason varchar(500) default ''                null,
    review_time   datetime                               null,
    review_by     varchar(64)  default ''                null,
    create_time   datetime     default CURRENT_TIMESTAMP null,
    update_time   datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '鉴定者申请表';

create index idx_create_time
    on nh_identifier_application (create_time);

create index idx_status
    on nh_identifier_application (status);

create index idx_user_id
    on nh_identifier_application (user_id);

create table nh_observation_diary
(
    diary_id         bigint auto_increment comment '日志ID'
        primary key,
    user_id          bigint                  not null comment '用户ID',
    title            varchar(200)            not null comment '日志标题',
    observation_date datetime                not null comment '观察日期',
    location         varchar(500)            null comment '观察地点',
    route_info       text                    null comment '路线信息',
    weather          varchar(100)            null comment '天气情况',
    temperature      varchar(50)             null comment '温度',
    species_found    text                    null comment '发现物种（JSON数组）',
    content          text                    null comment '日志内容/心得体会',
    images           text                    null comment '图片路径（JSON数组）',
    visibility       char        default '0' not null comment '可见性（0私密 1公开）',
    is_archived      char        default '0' null comment '是否归档（0否 1是）',
    tags             varchar(500)            null comment '标签',
    observation_ids  varchar(500)            null comment '关联观察记录ID（逗号分隔）',
    is_shared        varchar(1)  default '0' null comment '是否已分享到社群: 0-否, 1-是',
    shared_topic_id  bigint                  null comment '关联的社群话题ID',
    create_by        varchar(64) default ''  null comment '创建者',
    create_time      datetime                null comment '创建时间',
    update_by        varchar(64) default ''  null comment '更新者',
    update_time      datetime                null comment '更新时间',
    remark           varchar(500)            null comment '备注',
    record_count     int         default 0   null comment '关联的观察记录数'
)
    comment '个人观察日志表';

create index idx_is_archived
    on nh_observation_diary (is_archived);

create index idx_observation_date
    on nh_observation_diary (observation_date);

create index idx_record_count
    on nh_observation_diary (record_count);

create index idx_user_id
    on nh_observation_diary (user_id);

create index idx_visibility
    on nh_observation_diary (visibility);

create table nh_observation_diary_backup
(
    diary_id    bigint      default 0   not null comment '日志ID',
    user_id     bigint                  not null comment '用户ID',
    title       varchar(200)            not null comment '标题',
    content     text                    not null comment '日志内容',
    diary_date  date                    null comment '日志日期',
    location    varchar(200)            null comment '地点',
    weather     varchar(50)             null comment '天气',
    temperature varchar(20)             null comment '温度',
    images      text                    null comment '图片URL列表（JSON数组）',
    videos      text                    null comment '视频URL列表（JSON数组）',
    tags        varchar(500)            null comment '标签（逗号分隔）',
    visibility  char        default '0' null comment '可见性（0私密 1公开）',
    is_archived char        default '0' null comment '是否归档（0否 1是）',
    view_count  int         default 0   null comment '浏览次数',
    like_count  int         default 0   null comment '点赞数',
    del_flag    char        default '0' null comment '删除标志',
    create_by   varchar(64) default ''  null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64) default ''  null comment '更新者',
    update_time datetime                null comment '更新时间',
    remark      varchar(500)            null comment '备注'
);

create table nh_observation_record
(
    record_id        bigint auto_increment comment '观察记录ID'
        primary key,
    user_id          bigint                  not null comment '用户ID',
    title            varchar(200)            not null comment '标题',
    observation_time datetime                not null comment '观察时间',
    location         varchar(200)            not null comment '观察地点',
    latitude         decimal(10, 6)          null comment '纬度',
    longitude        decimal(10, 6)          null comment '经度',
    species_type     tinyint                 not null comment '物种类型 0=植物,1=动物,2=真菌,3=其他',
    species_name     varchar(100)            not null comment '物种名称',
    habitat          varchar(500)            null comment '生境描述',
    description      text                    not null comment '详细描述',
    images           text                    null comment '图片URL列表（JSON数组）',
    videos           text                    null comment '视频URL列表（JSON数组）',
    audit_status     tinyint     default 0   null comment '审核状态 0=草稿,1=待审核,2=已通过,3=已驳回',
    submit_time      datetime                null comment '提交审核时间',
    review_time      datetime                null comment '审核时间',
    reviewer_id      bigint                  null comment '审核人ID',
    review_comment   varchar(500)            null comment '审核意见',
    reject_reason    varchar(500)            null comment '驳回原因',
    specimen_id      bigint                  null comment '关联的标本ID（审核通过后生成）',
    is_shared        tinyint     default 0   null comment '是否已分享 0=否,1=是',
    shared_topic_id  bigint                  null comment '关联的社群话题ID',
    del_flag         char        default '0' null comment '删除标志（0存在 2删除）',
    create_by        varchar(64) default ''  null comment '创建者',
    create_time      datetime                null comment '创建时间',
    update_by        varchar(64) default ''  null comment '更新者',
    update_time      datetime                null comment '更新时间',
    remark           varchar(500)            null comment '备注',
    topic_id         bigint                  null
)
    comment '观察记录表';

create index idx_observation_time
    on nh_observation_record (observation_time);

create index idx_review_status
    on nh_observation_record (audit_status);

create index idx_species_type
    on nh_observation_record (species_type);

create index idx_user_id
    on nh_observation_record (user_id);

create table nh_species_identification
(
    identification_id   bigint auto_increment comment '鉴定ID'
        primary key,
    user_id             bigint                  not null comment '用户ID',
    title               varchar(200)            not null comment '标题',
    description         text                    null comment '详细描述',
    images              text                    null comment '图片URL列表(JSON)',
    observation_time    datetime                null comment '观察时间',
    location            varchar(200)            null comment '观察地点',
    latitude            decimal(10, 7)          null comment '纬度',
    longitude           decimal(10, 7)          null comment '经度',
    habitat             text                    null comment '生境描述',
    features            text                    null comment '特征描述',
    status              tinyint     default 0   null comment '鉴定状态【字典编码：nh_identification_status】0=待鉴定,1=已回答,2=已解决',
    best_answer_id      bigint                  null comment '最佳答案ID',
    answer_count        int         default 0   null comment '回答数量',
    view_count          int         default 0   null comment '浏览次数',
    is_shared           tinyint     default 0   null comment '是否分享【字典编码：nh_yes_no】0=否,1=是',
    shared_topic_id     bigint                  null comment '关联的社群话题ID',
    audit_status        tinyint     default 0   null comment '审核状态【字典编码：nh_audit_status】0=草稿,1=待审核,2=已通过,3=已驳回',
    audit_time          datetime                null comment '审核时间',
    audit_by            varchar(64)             null comment '审核人',
    audit_remark        varchar(500)            null comment '审核备注',
    del_flag            char        default '0' null comment '删除标志: 0-正常, 2-删除',
    create_by           varchar(64) default ''  null comment '创建者',
    create_time         datetime                null comment '创建时间',
    update_by           varchar(64) default ''  null comment '更新者',
    update_time         datetime                null comment '更新时间',
    remark              varchar(500)            null comment '备注',
    topic_id            bigint                  null,
    ai_species_name     varchar(200)            null comment 'AI识别的物种名称',
    ai_score            decimal(10, 7)          null comment 'AI识别的置信分数',
    ai_type             varchar(20)             null comment 'AI识别类型（animal/plant）',
    baike_url           varchar(500)            null comment '百度百科URL',
    baike_image_url     varchar(500)            null comment '百科图片URL',
    baike_description   text                    null comment '百科描述',
    vote_agree_score    int         default 0   null comment '同意总积分',
    vote_disagree_score int         default 0   null comment '不同意总积分',
    vote_status         char        default '0' null comment '投票状态：0=未开始,1=进行中,2=已结束',
    vote_result         char        default '2' null
)
    comment '物种鉴定求助表';

create table nh_identification_answer
(
    answer_id         bigint auto_increment comment '回答ID'
        primary key,
    identification_id bigint              not null comment '鉴定ID',
    user_id           bigint              not null comment '回答用户ID',
    user_name         varchar(64)         null comment '回答用户名',
    content           text                not null comment '回答内容',
    species_name      varchar(200)        null comment '物种名称',
    confidence        varchar(20)         null comment '置信度: high-高, medium-中, low-低',
    reference         text                null comment '参考资料',
    images            text                null comment '参考图片(JSON)',
    is_best           tinyint default 0   null comment '是否最佳答案【字典编码：nh_yes_no】0=否,1=是',
    like_count        int     default 0   null comment '点赞数',
    del_flag          char    default '0' null comment '删除标志: 0-正常, 2-删除',
    create_time       datetime            null comment '创建时间',
    update_time       datetime            null comment '更新时间',
    constraint nh_identification_answer_ibfk_1
        foreign key (identification_id) references nh_species_identification (identification_id)
            on delete cascade
)
    comment '鉴定回答表';

create index idx_create_time
    on nh_identification_answer (create_time);

create index idx_identification_id
    on nh_identification_answer (identification_id);

create index idx_is_best
    on nh_identification_answer (is_best);

create index idx_user_id
    on nh_identification_answer (user_id);

create index idx_audit_status
    on nh_species_identification (audit_status);

create index idx_create_time
    on nh_species_identification (create_time);

create index idx_status
    on nh_species_identification (status);

create index idx_user_id
    on nh_species_identification (user_id);

create table nh_specimen
(
    specimen_id      bigint auto_increment comment '标本ID'
        primary key,
    record_id        bigint                  not null comment '来源观察记录ID',
    user_id          bigint                  not null comment '贡献用户ID',
    specimen_code    varchar(50)             null comment '标本编号',
    title            varchar(200)            not null comment '标题',
    species_type     varchar(50)             not null comment '物种类型',
    species_name     varchar(100)            not null comment '物种名称',
    scientific_name  varchar(200)            null comment '学名（管理员补充）',
    family           varchar(100)            null comment '科',
    genus            varchar(100)            null comment '属',
    protection_level varchar(50)             null comment '保护级别',
    observation_time datetime                not null comment '观察时间',
    location         varchar(200)            not null comment '地点',
    latitude         decimal(10, 6)          null comment '纬度',
    longitude        decimal(10, 6)          null comment '经度',
    habitat          varchar(500)            null comment '生境',
    description      text                    null comment '描述',
    images           text                    null comment '图片',
    videos           text                    null comment '视频',
    quality_score    int                     null comment '质量评分（0-100）',
    is_public        char        default '1' null comment '是否公开（0否 1是）',
    view_count       int         default 0   null comment '浏览次数',
    del_flag         char        default '0' null comment '删除标志',
    create_by        varchar(64) default ''  null comment '创建者',
    create_time      datetime                null comment '创建时间',
    update_by        varchar(64) default ''  null comment '更新者',
    update_time      datetime                null comment '更新时间',
    remark           varchar(500)            null comment '备注',
    constraint uk_record_id
        unique (record_id)
)
    comment '标本管理表';

create index idx_species_type
    on nh_specimen (species_type);

create index idx_specimen_code
    on nh_specimen (specimen_code);

create index idx_user_id
    on nh_specimen (user_id);

create table qa_conversation
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    user_id         bigint                               null comment '用户ID',
    username        varchar(50)                          null comment '用户名',
    title           varchar(200)                         not null comment '对话标题',
    qa_type         tinyint(1) default 1                 null comment '问答类型：1-普通问答，2-知识图谱问答',
    related_species varchar(200)                         null comment '相关物种',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'AI问答对话会话表';

create index idx_qa_type
    on qa_conversation (qa_type);

create index idx_user_id
    on qa_conversation (user_id);

create index idx_username
    on qa_conversation (username);

create table qa_history
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    user_id         bigint                               null comment '用户ID',
    conversation_id bigint                               null comment '对话ID',
    username        varchar(50)                          null comment '用户名',
    question        text                                 not null comment '用户问题',
    answer          text                                 null comment 'AI回答',
    qa_type         tinyint(1) default 1                 null comment '问答类型：1-普通问答，2-知识图谱问答',
    related_species varchar(200)                         null comment '相关物种',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    ip_address      varchar(50)                          null comment 'IP地址'
)
    comment 'AI问答历史记录表';

create index idx_conversation_id
    on qa_history (conversation_id);

create index idx_create_time
    on qa_history (create_time);

create index idx_qa_type
    on qa_history (qa_type);

create index idx_related_species
    on qa_history (related_species);

create index idx_user_id
    on qa_history (user_id);

create index idx_username
    on qa_history (username);

create table species_details
(
    detail_id           bigint auto_increment comment '详情ID'
        primary key,
    taxon_id            bigint                  not null comment '关联物种分类表taxon_id',
    morphology          text                    null comment '形态特征描述',
    habitat             varchar(500) default '' null comment '生境描述',
    distribution        varchar(500) default '' null comment '分布区域',
    conservation_status varchar(100) default '' null comment '保护状态',
    national_protection varchar(50)  default '' null comment '国家保护级别',
    ecological_role     varchar(500) default '' null comment '生态作用',
    update_by           varchar(64)  default '' null comment '更新者',
    update_time         datetime                null comment '更新时间',
    constraint uk_taxon_id
        unique (taxon_id)
)
    comment '物种详细信息表';

create index idx_conservation
    on species_details (conservation_status);

create table species_media
(
    media_id      bigint auto_increment comment '媒体ID（主键）'
        primary key,
    taxon_id      bigint                   not null comment '关联物种分类表taxon_id',
    media_type    char                     not null comment '媒体类型（1-图片，2-视频，3-音频）',
    file_url      varchar(255)             not null comment '媒体文件URL',
    thumbnail_url varchar(255) default ''  null comment '缩略图URL（仅图片）',
    is_standard   char         default '0' null comment '是否标准图鉴（1-是，0-否）',
    display_order tinyint      default 0   null comment '展示顺序（数字越小越靠前）',
    uploaded_by   varchar(64)  default ''  null comment '上传者（同create_by，可保留便于理解）',
    create_by     varchar(64)  default ''  null comment '创建者',
    create_time   datetime                 null comment '上传时间（同create_time）',
    update_by     varchar(64)  default ''  null comment '更新者',
    update_time   datetime                 null comment '更新时间',
    remark        varchar(500) default ''  null comment '备注（如“正面照”“栖息地视频”）'
)
    comment '物种多媒体资源表';

create index idx_is_standard
    on species_media (is_standard);

create index idx_taxon_media
    on species_media (taxon_id, media_type);

create table species_tag_rel
(
    rel_id      bigint auto_increment comment '关联ID（主键）'
        primary key,
    taxon_id    bigint                  not null comment '关联物种分类表taxon_id',
    tag_id      bigint                  not null comment '关联物种标签表tag_id',
    create_by   varchar(64)  default '' null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64)  default '' null comment '更新者',
    update_time datetime                null comment '更新时间',
    remark      varchar(200) default '' null comment '关联备注（如“2024年新增该标签关联”）',
    constraint uk_taxon_tag
        unique (taxon_id, tag_id)
)
    comment '物种与标签的关联表';

create index idx_tag_rel
    on species_tag_rel (tag_id);

create index idx_taxon_rel
    on species_tag_rel (taxon_id);

create table species_tags
(
    tag_id       bigint auto_increment comment '标签ID'
        primary key,
    tag_name     varchar(100)            not null comment '标签名称（唯一，如“入侵物种”“药用植物”）',
    tag_category varchar(100) default '' null comment '标签类别（如“功能标签”“生态标签”）',
    create_by    varchar(64)  default '' null comment '创建者',
    create_time  datetime                null comment '创建时间',
    update_by    varchar(64)  default '' null comment '更新者',
    update_time  datetime                null comment '更新时间',
    remark       varchar(200) default '' null comment '备注',
    constraint uk_tag_name
        unique (tag_name)
)
    comment '物种标签表';

create index idx_tag_category
    on species_tags (tag_category);

create table species_taxa
(
    taxon_id        bigint auto_increment comment '分类单元ID'
        primary key,
    scientific_name varchar(255)             not null comment '物种学名',
    chinese_name    varchar(255) default ''  null comment '物种中文名',
    common_names    varchar(500) default ''  null comment '俗名',
    rank_level      tinyint                  not null comment '分类阶元级别',
    parent_taxon_id bigint       default 0   null comment '父分类单元ID',
    taxon_code      varchar(100) default ''  null comment '分类单元编码',
    is_valid        char         default '1' null comment '是否有效（1-有效，0-无效）',
    synonyms        varchar(500) default ''  null comment '同义词',
    create_by       varchar(64)  default ''  null comment '创建者',
    create_time     datetime                 null comment '创建时间',
    update_by       varchar(64)  default ''  null comment '更新者',
    update_time     datetime                 null comment '更新时间',
    remark          varchar(500) default ''  null comment '备注',
    constraint uk_scientific_name
        unique (scientific_name),
    constraint uk_taxon_code
        unique (taxon_code)
)
    comment '物种分类基础表';

create index idx_parent_taxon
    on species_taxa (parent_taxon_id);

create index idx_rank_level
    on species_taxa (rank_level);

create table species_video_task
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    user_id       bigint                             null comment '用户ID',
    username      varchar(50)                        null comment '用户名',
    content       text                               not null comment '知识库返回的科普内容（作为视频生成prompt）',
    task_status   char     default '1'               not null comment '任务状态：1-等待中 2-生成中 3-成功 4-失败',
    video_url     varchar(500)                       null comment '视频地址（七牛云持久化）',
    error_message varchar(500)                       null comment '失败错误信息',
    remark        varchar(500)                       null comment '备注（问题标题）',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime                           null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '物种科普视频生成任务记录表';

create index idx_create_time
    on species_video_task (create_time);

create index idx_task_status
    on species_video_task (task_status);

create index idx_user_id
    on species_video_task (user_id);

create index idx_username
    on species_video_task (username);

create table sys_config
(
    config_id    int auto_increment comment '参数主键'
        primary key,
    config_name  varchar(100) default ''  null comment '参数名称',
    config_key   varchar(100) default ''  null comment '参数键名',
    config_value varchar(500) default ''  null comment '参数键值',
    config_type  char         default 'N' null comment '系统内置（Y是 N否）',
    create_by    varchar(64)  default ''  null comment '创建者',
    create_time  datetime                 null comment '创建时间',
    update_by    varchar(64)  default ''  null comment '更新者',
    update_time  datetime                 null comment '更新时间',
    remark       varchar(500)             null comment '备注'
)
    comment '参数配置表';

create table sys_dept
(
    dept_id     bigint auto_increment comment '部门id'
        primary key,
    parent_id   bigint      default 0   null comment '父部门id',
    ancestors   varchar(50) default ''  null comment '祖级列表',
    dept_name   varchar(30) default ''  null comment '部门名称',
    order_num   int         default 0   null comment '显示顺序',
    leader      varchar(20)             null comment '负责人',
    phone       varchar(11)             null comment '联系电话',
    email       varchar(50)             null comment '邮箱',
    status      char        default '0' null comment '部门状态（0正常 1停用）',
    del_flag    char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by   varchar(64) default ''  null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64) default ''  null comment '更新者',
    update_time datetime                null comment '更新时间'
)
    comment '部门表';

create table sys_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int          default 0   null comment '字典排序',
    dict_label  varchar(100) default ''  null comment '字典标签',
    dict_value  varchar(100) default ''  null comment '字典键值',
    dict_type   varchar(100) default ''  null comment '字典类型',
    css_class   varchar(100)             null comment '样式属性（其他样式扩展）',
    list_class  varchar(100)             null comment '表格回显样式',
    is_default  char         default 'N' null comment '是否默认（Y是 N否）',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '字典数据表';

create table sys_dict_type
(
    dict_id     bigint auto_increment comment '字典主键'
        primary key,
    dict_name   varchar(100) default ''  null comment '字典名称',
    dict_type   varchar(100) default ''  null comment '字典类型',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注',
    constraint dict_type
        unique (dict_type)
)
    comment '字典类型表';

create table sys_job
(
    job_id          bigint auto_increment comment '任务ID',
    job_name        varchar(64)  default ''        not null comment '任务名称',
    job_group       varchar(64)  default 'DEFAULT' not null comment '任务组名',
    invoke_target   varchar(500)                   not null comment '调用目标字符串',
    cron_expression varchar(255) default ''        null comment 'cron执行表达式',
    misfire_policy  varchar(20)  default '3'       null comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent      char         default '1'       null comment '是否并发执行（0允许 1禁止）',
    status          char         default '0'       null comment '状态（0正常 1暂停）',
    create_by       varchar(64)  default ''        null comment '创建者',
    create_time     datetime                       null comment '创建时间',
    update_by       varchar(64)  default ''        null comment '更新者',
    update_time     datetime                       null comment '更新时间',
    remark          varchar(500) default ''        null comment '备注信息',
    primary key (job_id, job_name, job_group)
)
    comment '定时任务调度表';

create table sys_job_log
(
    job_log_id     bigint auto_increment comment '任务日志ID'
        primary key,
    job_name       varchar(64)               not null comment '任务名称',
    job_group      varchar(64)               not null comment '任务组名',
    invoke_target  varchar(500)              not null comment '调用目标字符串',
    job_message    varchar(500)              null comment '日志信息',
    status         char          default '0' null comment '执行状态（0正常 1失败）',
    exception_info varchar(2000) default ''  null comment '异常信息',
    create_time    datetime                  null comment '创建时间'
)
    comment '定时任务调度日志表';

create table sys_logininfor
(
    info_id        bigint auto_increment comment '访问ID'
        primary key,
    user_name      varchar(50)  default ''  null comment '用户账号',
    ipaddr         varchar(128) default ''  null comment '登录IP地址',
    login_location varchar(255) default ''  null comment '登录地点',
    browser        varchar(50)  default ''  null comment '浏览器类型',
    os             varchar(50)  default ''  null comment '操作系统',
    status         char         default '0' null comment '登录状态（0成功 1失败）',
    msg            varchar(255) default ''  null comment '提示消息',
    login_time     datetime                 null comment '访问时间'
)
    comment '系统访问记录';

create index idx_sys_logininfor_lt
    on sys_logininfor (login_time);

create index idx_sys_logininfor_s
    on sys_logininfor (status);

create table sys_menu
(
    menu_id     bigint auto_increment comment '菜单ID'
        primary key,
    menu_name   varchar(50)              not null comment '菜单名称',
    parent_id   bigint       default 0   null comment '父菜单ID',
    order_num   int          default 0   null comment '显示顺序',
    path        varchar(200) default ''  null comment '路由地址',
    component   varchar(255)             null comment '组件路径',
    query       varchar(255)             null comment '路由参数',
    route_name  varchar(50)  default ''  null comment '路由名称',
    is_frame    int          default 1   null comment '是否为外链（0是 1否）',
    is_cache    int          default 0   null comment '是否缓存（0缓存 1不缓存）',
    menu_type   char         default ''  null comment '菜单类型（M目录 C菜单 F按钮）',
    visible     char         default '0' null comment '菜单状态（0显示 1隐藏）',
    status      char         default '0' null comment '菜单状态（0正常 1停用）',
    perms       varchar(100)             null comment '权限标识',
    icon        varchar(100) default '#' null comment '菜单图标',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500) default ''  null comment '备注'
)
    comment '菜单权限表';

create table sys_notice
(
    notice_id      int auto_increment comment '公告ID'
        primary key,
    notice_title   varchar(50)             not null comment '公告标题',
    notice_type    char                    not null comment '公告类型（1通知 2公告）',
    notice_content longblob                null comment '公告内容',
    status         char        default '0' null comment '公告状态（0正常 1关闭）',
    create_by      varchar(64) default ''  null comment '创建者',
    create_time    datetime                null comment '创建时间',
    update_by      varchar(64) default ''  null comment '更新者',
    update_time    datetime                null comment '更新时间',
    remark         varchar(255)            null comment '备注'
)
    comment '通知公告表';

create table sys_oper_log
(
    oper_id        bigint auto_increment comment '日志主键'
        primary key,
    title          varchar(50)   default '' null comment '模块标题',
    business_type  int           default 0  null comment '业务类型（0其它 1新增 2修改 3删除）',
    method         varchar(200)  default '' null comment '方法名称',
    request_method varchar(10)   default '' null comment '请求方式',
    operator_type  int           default 0  null comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' null comment '操作人员',
    dept_name      varchar(50)   default '' null comment '部门名称',
    oper_url       varchar(255)  default '' null comment '请求URL',
    oper_ip        varchar(128)  default '' null comment '主机地址',
    oper_location  varchar(255)  default '' null comment '操作地点',
    oper_param     varchar(2000) default '' null comment '请求参数',
    json_result    varchar(2000) default '' null comment '返回参数',
    status         int           default 0  null comment '操作状态（0正常 1异常）',
    error_msg      varchar(2000) default '' null comment '错误消息',
    oper_time      datetime                 null comment '操作时间',
    cost_time      bigint        default 0  null comment '消耗时间'
)
    comment '操作日志记录';

create index idx_sys_oper_log_bt
    on sys_oper_log (business_type);

create index idx_sys_oper_log_ot
    on sys_oper_log (oper_time);

create index idx_sys_oper_log_s
    on sys_oper_log (status);

create table sys_post
(
    post_id     bigint auto_increment comment '岗位ID'
        primary key,
    post_code   varchar(64)            not null comment '岗位编码',
    post_name   varchar(50)            not null comment '岗位名称',
    post_sort   int                    not null comment '显示顺序',
    status      char                   not null comment '状态（0正常 1停用）',
    create_by   varchar(64) default '' null comment '创建者',
    create_time datetime               null comment '创建时间',
    update_by   varchar(64) default '' null comment '更新者',
    update_time datetime               null comment '更新时间',
    remark      varchar(500)           null comment '备注'
)
    comment '岗位信息表';

create table sys_role
(
    role_id             bigint auto_increment comment '角色ID'
        primary key,
    role_name           varchar(30)             not null comment '角色名称',
    role_key            varchar(100)            not null comment '角色权限字符串',
    role_sort           int                     not null comment '显示顺序',
    data_scope          char        default '1' null comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
    menu_check_strictly tinyint(1)  default 1   null comment '菜单树选择项是否关联显示',
    dept_check_strictly tinyint(1)  default 1   null comment '部门树选择项是否关联显示',
    status              char                    not null comment '角色状态（0正常 1停用）',
    del_flag            char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by           varchar(64) default ''  null comment '创建者',
    create_time         datetime                null comment '创建时间',
    update_by           varchar(64) default ''  null comment '更新者',
    update_time         datetime                null comment '更新时间',
    remark              varchar(500)            null comment '备注'
)
    comment '角色信息表';

create table sys_role_dept
(
    role_id bigint not null comment '角色ID',
    dept_id bigint not null comment '部门ID',
    primary key (role_id, dept_id)
)
    comment '角色和部门关联表';

create table sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID',
    primary key (role_id, menu_id)
)
    comment '角色和菜单关联表';

create table sys_user
(
    user_id         bigint auto_increment comment '用户ID'
        primary key,
    dept_id         bigint                    null comment '部门ID',
    user_name       varchar(30)               not null comment '用户账号',
    nick_name       varchar(30)               not null comment '用户昵称',
    user_type       varchar(2)   default '00' null comment '用户类型（00系统用户）',
    email           varchar(50)  default ''   null comment '用户邮箱',
    phonenumber     varchar(11)  default ''   null comment '手机号码',
    sex             char         default '0'  null comment '用户性别（0男 1女 2未知）',
    avatar          varchar(100) default ''   null comment '头像地址',
    password        varchar(100) default ''   null comment '密码',
    status          char         default '0'  null comment '账号状态（0正常 1停用）',
    del_flag        char         default '0'  null comment '删除标志（0代表存在 2代表删除）',
    login_ip        varchar(128) default ''   null comment '最后登录IP',
    login_date      datetime                  null comment '最后登录时间',
    pwd_update_date datetime                  null comment '密码最后更新时间',
    create_by       varchar(64)  default ''   null comment '创建者',
    create_time     datetime                  null comment '创建时间',
    update_by       varchar(64)  default ''   null comment '更新者',
    update_time     datetime                  null comment '更新时间',
    remark          varchar(500)              null comment '备注'
)
    comment '用户信息表';

create table sys_user_post
(
    user_id bigint not null comment '用户ID',
    post_id bigint not null comment '岗位ID',
    primary key (user_id, post_id)
)
    comment '用户与岗位关联表';

create table sys_user_role
(
    user_id bigint not null comment '用户ID',
    role_id bigint not null comment '角色ID',
    primary key (user_id, role_id)
)
    comment '用户和角色关联表';

create definer = root@`%` view v_user_observation_stats as
select `u`.`user_id`                                                                           AS `user_id`,
       `u`.`nick_name`                                                                         AS `nick_name`,
       count(distinct `r`.`record_id`)                                                         AS `total_records`,
       count(distinct (case when (`r`.`review_status` = 'approved') then `r`.`record_id` end)) AS `approved_records`,
       count(distinct (case when (`r`.`review_status` = 'pending') then `r`.`record_id` end))  AS `pending_records`,
       count(distinct (case when (`r`.`review_status` = 'rejected') then `r`.`record_id` end)) AS `rejected_records`,
       count(distinct `d`.`diary_id`)                                                          AS `total_diaries`,
       count(distinct `s`.`specimen_id`)                                                       AS `total_specimens`,
       count(distinct `r`.`species_type`)                                                      AS `species_types_count`
from (((`ry-vue`.`sys_user` `u` left join `ry-vue`.`nh_observation_record` `r`
        on (((`u`.`user_id` = `r`.`user_id`) and (`r`.`del_flag` = '0')))) left join `ry-vue`.`nh_observation_diary` `d`
       on (((`u`.`user_id` = `d`.`user_id`) and (`d`.`del_flag` = '0')))) left join `ry-vue`.`nh_specimen` `s`
      on (((`u`.`user_id` = `s`.`user_id`) and (`s`.`del_flag` = '0'))))
where (`u`.`del_flag` = '0')
group by `u`.`user_id`, `u`.`nick_name`;

create table QRTZ_CALENDARS
(
    sched_name    varchar(120) not null comment '调度名称',
    calendar_name varchar(200) not null comment '日历名称',
    calendar      blob         not null comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
)
    comment '日历信息表';

create table QRTZ_FIRED_TRIGGERS
(
    sched_name        varchar(120) not null comment '调度名称',
    entry_id          varchar(95)  not null comment '调度器实例id',
    trigger_name      varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group     varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    instance_name     varchar(200) not null comment '调度器实例名',
    fired_time        bigint       not null comment '触发的时间',
    sched_time        bigint       not null comment '定时器制定的时间',
    priority          int          not null comment '优先级',
    state             varchar(16)  not null comment '状态',
    job_name          varchar(200) null comment '任务名称',
    job_group         varchar(200) null comment '任务组名',
    is_nonconcurrent  varchar(1)   null comment '是否并发',
    requests_recovery varchar(1)   null comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
)
    comment '已触发的触发器表';

create table QRTZ_JOB_DETAILS
(
    sched_name        varchar(120) not null comment '调度名称',
    job_name          varchar(200) not null comment '任务名称',
    job_group         varchar(200) not null comment '任务组名',
    description       varchar(250) null comment '相关介绍',
    job_class_name    varchar(250) not null comment '执行任务类名称',
    is_durable        varchar(1)   not null comment '是否持久化',
    is_nonconcurrent  varchar(1)   not null comment '是否并发',
    is_update_data    varchar(1)   not null comment '是否更新数据',
    requests_recovery varchar(1)   not null comment '是否接受恢复执行',
    job_data          blob         null comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
)
    comment '任务详细信息表';

create table QRTZ_LOCKS
(
    sched_name varchar(120) not null comment '调度名称',
    lock_name  varchar(40)  not null comment '悲观锁名称',
    primary key (sched_name, lock_name)
)
    comment '存储的悲观锁信息表';

create table QRTZ_PAUSED_TRIGGER_GRPS
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
)
    comment '暂停的触发器表';

create table QRTZ_SCHEDULER_STATE
(
    sched_name        varchar(120) not null comment '调度名称',
    instance_name     varchar(200) not null comment '实例名称',
    last_checkin_time bigint       not null comment '上次检查时间',
    checkin_interval  bigint       not null comment '检查间隔时间',
    primary key (sched_name, instance_name)
)
    comment '调度器状态表';

create table QRTZ_TRIGGERS
(
    sched_name     varchar(120) not null comment '调度名称',
    trigger_name   varchar(200) not null comment '触发器的名字',
    trigger_group  varchar(200) not null comment '触发器所属组的名字',
    job_name       varchar(200) not null comment 'qrtz_job_details表job_name的外键',
    job_group      varchar(200) not null comment 'qrtz_job_details表job_group的外键',
    description    varchar(250) null comment '相关介绍',
    next_fire_time bigint       null comment '上一次触发时间（毫秒）',
    prev_fire_time bigint       null comment '下一次触发时间（默认为-1表示不触发）',
    priority       int          null comment '优先级',
    trigger_state  varchar(16)  not null comment '触发器状态',
    trigger_type   varchar(8)   not null comment '触发器的类型',
    start_time     bigint       not null comment '开始时间',
    end_time       bigint       null comment '结束时间',
    calendar_name  varchar(200) null comment '日程表名称',
    misfire_instr  smallint     null comment '补偿执行的策略',
    job_data       blob         null comment '存放持久化job对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_TRIGGERS_ibfk_1
        foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS (sched_name, job_name, job_group)
)
    comment '触发器详细信息表';

create table QRTZ_BLOB_TRIGGERS
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_name  varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    blob_data     blob         null comment '存放持久化Trigger对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_BLOB_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment 'Blob类型的触发器表';

create table QRTZ_CRON_TRIGGERS
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    cron_expression varchar(200) not null comment 'cron表达式',
    time_zone_id    varchar(80)  null comment '时区',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_CRON_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment 'Cron类型的触发器表';

create table QRTZ_SIMPLE_TRIGGERS
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    repeat_count    bigint       not null comment '重复的次数统计',
    repeat_interval bigint       not null comment '重复的间隔时间',
    times_triggered bigint       not null comment '已经触发的次数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_SIMPLE_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment '简单触发器的信息表';

create table QRTZ_SIMPROP_TRIGGERS
(
    sched_name    varchar(120)   not null comment '调度名称',
    trigger_name  varchar(200)   not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200)   not null comment 'qrtz_triggers表trigger_group的外键',
    str_prop_1    varchar(512)   null comment 'String类型的trigger的第一个参数',
    str_prop_2    varchar(512)   null comment 'String类型的trigger的第二个参数',
    str_prop_3    varchar(512)   null comment 'String类型的trigger的第三个参数',
    int_prop_1    int            null comment 'int类型的trigger的第一个参数',
    int_prop_2    int            null comment 'int类型的trigger的第二个参数',
    long_prop_1   bigint         null comment 'long类型的trigger的第一个参数',
    long_prop_2   bigint         null comment 'long类型的trigger的第二个参数',
    dec_prop_1    decimal(13, 4) null comment 'decimal类型的trigger的第一个参数',
    dec_prop_2    decimal(13, 4) null comment 'decimal类型的trigger的第二个参数',
    bool_prop_1   varchar(1)     null comment 'Boolean类型的trigger的第一个参数',
    bool_prop_2   varchar(1)     null comment 'Boolean类型的trigger的第二个参数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint QRTZ_SIMPROP_TRIGGERS_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment '同步机制的行锁表';

create index sched_name
    on QRTZ_TRIGGERS (sched_name, job_name, job_group);

create table bio_recognition
(
    id                 bigint auto_increment comment '主键ID'
        primary key,
    user_id            bigint                               null comment '用户ID',
    username           varchar(50)                          null comment '用户名',
    image_url          varchar(500)                         not null comment '图片URL',
    recognition_result varchar(200)                         null comment '识别结果（物种名称）',
    recognition_type   tinyint(1)                           null comment '识别类型【字典编码：bio_recognition_type】 1-植物 2-动物 3-其他',
    confidence         decimal(5, 2)                        null comment '置信度（0-100）',
    status             tinyint(1) default 1                 null comment '识别状态【字典编码：bio_recognition_status】 1-成功 0-失败',
    baike_info         text                                 null comment '百科信息（JSON格式）',
    all_results        text                                 null comment '所有识别结果（JSON格式）',
    is_shared          tinyint(1) default 0                 null comment '是否已分享到社群【字典编码：sys_yes_no】 0-否 1-是',
    shared_topic_id    bigint                               null comment '关联的社群话题ID',
    remark             varchar(500)                         null comment '备注',
    ip_address         varchar(50)                          null comment 'IP地址',
    create_time        datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '生物识别记录表';

create index idx_create_time
    on bio_recognition (create_time);

create index idx_recognition_result
    on bio_recognition (recognition_result);

create index idx_status
    on bio_recognition (status);

create index idx_user_id
    on bio_recognition (user_id);

create index idx_username
    on bio_recognition (username);

create table gen_table
(
    table_id          bigint auto_increment comment '编号'
        primary key,
    table_name        varchar(200) default ''     null comment '表名称',
    table_comment     varchar(500) default ''     null comment '表描述',
    sub_table_name    varchar(64)                 null comment '关联子表的表名',
    sub_table_fk_name varchar(64)                 null comment '子表关联的外键名',
    class_name        varchar(100) default ''     null comment '实体类名称',
    tpl_category      varchar(200) default 'crud' null comment '使用的模板（crud单表操作 tree树表操作）',
    tpl_web_type      varchar(30)  default ''     null comment '前端模板类型（element-ui模版 element-plus模版）',
    package_name      varchar(100)                null comment '生成包路径',
    module_name       varchar(30)                 null comment '生成模块名',
    business_name     varchar(30)                 null comment '生成业务名',
    function_name     varchar(50)                 null comment '生成功能名',
    function_author   varchar(50)                 null comment '生成功能作者',
    gen_type          char         default '0'    null comment '生成代码方式（0zip压缩包 1自定义路径）',
    gen_path          varchar(200) default '/'    null comment '生成路径（不填默认项目路径）',
    options           varchar(1000)               null comment '其它生成选项',
    create_by         varchar(64)  default ''     null comment '创建者',
    create_time       datetime                    null comment '创建时间',
    update_by         varchar(64)  default ''     null comment '更新者',
    update_time       datetime                    null comment '更新时间',
    remark            varchar(500)                null comment '备注'
)
    comment '代码生成业务表';

create table gen_table_column
(
    column_id      bigint auto_increment comment '编号'
        primary key,
    table_id       bigint                    null comment '归属表编号',
    column_name    varchar(200)              null comment '列名称',
    column_comment varchar(500)              null comment '列描述',
    column_type    varchar(100)              null comment '列类型',
    java_type      varchar(500)              null comment 'JAVA类型',
    java_field     varchar(200)              null comment 'JAVA字段名',
    is_pk          char                      null comment '是否主键（1是）',
    is_increment   char                      null comment '是否自增（1是）',
    is_required    char                      null comment '是否必填（1是）',
    is_insert      char                      null comment '是否为插入字段（1是）',
    is_edit        char                      null comment '是否编辑字段（1是）',
    is_list        char                      null comment '是否列表字段（1是）',
    is_query       char                      null comment '是否查询字段（1是）',
    query_type     varchar(200) default 'EQ' null comment '查询方式（等于、不等于、大于、小于、范围）',
    html_type      varchar(200)              null comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
    dict_type      varchar(200) default ''   null comment '字典类型',
    sort           int                       null comment '排序',
    create_by      varchar(64)  default ''   null comment '创建者',
    create_time    datetime                  null comment '创建时间',
    update_by      varchar(64)  default ''   null comment '更新者',
    update_time    datetime                  null comment '更新时间'
)
    comment '代码生成业务表字段';

create table mark3d_task
(
    id                  bigint auto_increment comment '主键ID'
        primary key,
    user_id             bigint                               null comment '用户ID',
    username            varchar(50)                          null comment '用户名',
    task_name           varchar(200)                         null comment '任务名称',
    front_image_url     varchar(500)                         not null comment '输入图片URL（七牛云）',
    back_image_url      varchar(500)                         null comment '背面图片URL（七牛云，预留）',
    top_image_url       varchar(500)                         null comment '上面图片URL（七牛云，预留）',
    side_image_url      varchar(500)                         null comment '侧面图片URL（七牛云，预留）',
    meshy_task_id       varchar(100)                         null comment '腾讯云混元3D JobId',
    task_status         char       default '1'               not null comment '任务状态（字典：mark3d_task_status）1-等待中 2-生成中 3-成功 4-失败',
    progress            int        default 0                 null comment '生成进度（0-100）',
    model_url_glb       varchar(500)                         null comment 'GLB模型地址（腾讯云临时，24小时有效）',
    model_url_glb_qiniu varchar(500)                         null comment 'GLB模型地址（七牛云持久化，永久有效）',
    model_url_obj       varchar(500)                         null comment 'OBJ模型地址（腾讯云临时）',
    thumbnail_url       varchar(500)                         null comment '预览图URL（腾讯云临时）',
    thumbnail_qiniu     varchar(500)                         null comment '预览图URL（七牛云持久化，永久有效）',
    error_message       varchar(500)                         null comment '失败错误信息',
    remark              varchar(500)                         null comment '备注',
    is_public           tinyint(1) default 0                 not null comment '是否公开展示（0-不公开 1-公开）',
    create_time         datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time         datetime                             null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '3D标注任务记录表';

create index idx_create_time
    on mark3d_task (create_time);

create index idx_is_public
    on mark3d_task (is_public);

create index idx_meshy_task_id
    on mark3d_task (meshy_task_id);

create index idx_task_status
    on mark3d_task (task_status);

create index idx_user_id
    on mark3d_task (user_id);

create index idx_username
    on mark3d_task (username);

create table nh_community_category
(
    category_id   bigint auto_increment comment '板块ID'
        primary key,
    category_code varchar(50)                           not null comment '板块代码（唯一标识）',
    category_name varchar(100)                          not null comment '板块名称',
    category_icon varchar(100)                          null comment '板块图标',
    category_desc varchar(500)                          null comment '板块描述',
    sort_order    int         default 0                 null comment '排序顺序',
    status        char        default '0'               null comment '状态：0-正常，1-停用',
    create_by     varchar(64) default ''                null comment '创建者',
    create_time   datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by     varchar(64) default ''                null comment '更新者',
    update_time   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    remark        varchar(500)                          null comment '备注',
    constraint uk_category_code
        unique (category_code)
)
    comment '社群板块分类表';

create index idx_sort
    on nh_community_category (sort_order);

create index idx_status
    on nh_community_category (status);

create table nh_community_collect
(
    collect_id  bigint auto_increment comment '收藏ID'
        primary key,
    user_id     bigint   not null comment '用户ID',
    topic_id    bigint   not null comment '话题ID',
    create_time datetime null comment '创建时间',
    constraint uk_user_topic
        unique (user_id, topic_id)
)
    comment '用户收藏表';

create index idx_topic_id
    on nh_community_collect (topic_id);

create table nh_community_comment
(
    comment_id    bigint auto_increment comment '评论ID'
        primary key,
    topic_id      bigint                  not null comment '话题ID',
    user_id       bigint                  not null comment '评论用户ID',
    user_name     varchar(64)             null comment '评论用户名',
    parent_id     bigint      default 0   null comment '父评论ID（0表示一级评论）',
    reply_to_id   bigint                  null comment '回复的用户ID',
    reply_to_name varchar(64)             null comment '回复的用户名',
    content       text                    not null comment '评论内容',
    like_count    int         default 0   null comment '点赞数',
    status        char        default '0' null comment '状态：0-正常，1-已删除，2-已屏蔽',
    create_by     varchar(64) default ''  null comment '创建者',
    create_time   datetime                null comment '创建时间',
    update_by     varchar(64) default ''  null comment '更新者',
    update_time   datetime                null comment '更新时间'
)
    comment '话题评论表';

create index idx_comment_time
    on nh_community_comment (create_time);

create index idx_comment_topic
    on nh_community_comment (topic_id, status);

create index idx_comment_user
    on nh_community_comment (user_id);

create index idx_parent_id
    on nh_community_comment (parent_id);

create index idx_topic_id
    on nh_community_comment (topic_id);

create index idx_user_id
    on nh_community_comment (user_id);

create table nh_community_like
(
    like_id     bigint auto_increment comment '点赞ID'
        primary key,
    user_id     bigint      not null comment '用户ID',
    target_type varchar(20) not null comment '目标类型（topic话题/comment评论）',
    target_id   bigint      not null comment '目标ID',
    create_time datetime    null comment '创建时间',
    constraint uk_user_target
        unique (user_id, target_type, target_id)
)
    comment '用户点赞表';

create index idx_target
    on nh_community_like (target_type, target_id);

create table nh_community_report
(
    report_id        bigint auto_increment comment '举报ID'
        primary key,
    report_type      varchar(20)                           not null comment '举报类型：0-话题，1-评论',
    target_id        bigint                                not null comment '被举报对象ID',
    target_title     varchar(500)                          null comment '被举报对象标题/内容摘要',
    target_user_id   bigint                                null comment '被举报用户ID',
    target_user_name varchar(64)                           null comment '被举报用户名',
    reporter_id      bigint      default 0                 null comment '举报人ID',
    reporter_name    varchar(64)                           null comment '举报人名称',
    reason           varchar(20)                           not null comment '举报原因：0-垃圾广告，1-辱骂攻击，2-违法违规，3-虚假信息，4-其他',
    description      varchar(1000)                         null comment '举报详细描述',
    status           varchar(20) default '0'               null comment '处理状态：0-待处理，1-已处理，2-已忽略',
    handle_result    varchar(20)                           null comment '处理结果：0-删除内容，1-禁言用户，2-警告，3-忽略',
    handle_remark    varchar(500)                          null comment '处理备注',
    handle_time      datetime                              null comment '处理时间',
    handle_by        varchar(64)                           null comment '处理人',
    create_by        varchar(64) default ''                null comment '创建者',
    create_time      datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by        varchar(64) default ''                null comment '更新者',
    update_time      datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '社群举报表';

create index idx_create_time
    on nh_community_report (create_time);

create index idx_reporter
    on nh_community_report (reporter_id);

create index idx_status
    on nh_community_report (status);

create index idx_target
    on nh_community_report (report_type, target_id);

create table nh_community_topic
(
    topic_id      bigint auto_increment comment '话题ID'
        primary key,
    user_id       bigint                            not null comment '发布用户ID',
    user_name     varchar(64)                       null comment '发布用户名',
    category      varchar(50)                       not null comment '话题分类（species_science物种科普/field_explore野外探索/identify_help鉴定求助）',
    source_type   varchar(50)    default 'original' null comment '来源类型: original-原创, observation-观察记录, identification-物种鉴定, survey-野外调查, diary-观察日志',
    source_id     bigint                            null comment '来源记录ID',
    title         varchar(200)                      not null comment '话题标题',
    content       text                              not null comment '话题内容',
    images        text                              null comment '图片路径（JSON数组）',
    tags          varchar(500)                      null comment '标签（逗号分隔）',
    view_count    int            default 0          null comment '浏览次数',
    like_count    int            default 0          null comment '点赞数',
    comment_count int            default 0          null comment '评论数',
    collect_count int            default 0          null comment '收藏数',
    share_count   int            default 0          null comment '转发数',
    hot_score     decimal(10, 2) default 0.00       null comment '热度分数',
    is_top        char           default '0'        null comment '是否置顶：0-否，1-是',
    is_essence    char           default '0'        null comment '是否精华：0-否，1-是',
    status        char           default '0'        null comment '状态：0-正常，1-关闭',
    create_by     varchar(64)    default ''         null comment '创建者',
    create_time   datetime                          null comment '创建时间',
    update_by     varchar(64)    default ''         null comment '更新者',
    update_time   datetime                          null comment '更新时间',
    remark        varchar(500)                      null comment '备注',
    audit_status  varchar(16)                       null comment '审核状态：0待审核 1通过 2拒绝',
    audit_remark  varchar(500)                      null comment '审核备注',
    audit_time    datetime                          null comment '审核时间',
    audit_by      varchar(64)                       null comment '审核人'
)
    comment '社群话题表';

create index idx_category
    on nh_community_topic (category);

create index idx_create_time
    on nh_community_topic (create_time);

create index idx_hot_score
    on nh_community_topic (hot_score);

create index idx_source
    on nh_community_topic (source_type, source_id);

create index idx_user_id
    on nh_community_topic (user_id);

create table nh_diary_record_relation
(
    id          bigint auto_increment comment 'ID'
        primary key,
    diary_id    bigint   not null comment '日志ID',
    record_id   bigint   not null comment '观察记录ID',
    create_time datetime null comment '创建时间',
    constraint uk_diary_record
        unique (diary_id, record_id)
)
    comment '日志关联观察记录表';

create index idx_diary_id
    on nh_diary_record_relation (diary_id);

create index idx_record_id
    on nh_diary_record_relation (record_id);

create table nh_field_survey
(
    survey_id       bigint auto_increment comment '调查ID'
        primary key,
    user_id         bigint                  not null comment '用户ID',
    title           varchar(200)            not null comment '调查标题',
    survey_date     date                    not null comment '调查日期',
    start_time      datetime                null comment '开始时间',
    end_time        datetime                null comment '结束时间',
    location        varchar(200)            null comment '调查地点',
    route_info      text                    null comment '路线信息(JSON格式，包含经纬度坐标数组)',
    weather         varchar(100)            null comment '天气情况',
    temperature     varchar(50)             null comment '温度',
    habitat_type    tinyint(1)              null comment '生境类型【字典编码：nh_habitat_type】 1-森林 2-草地 3-湿地 4-河流 5-湖泊 6-海洋 7-山地 8-农田 9-城市 10-其他',
    survey_method   tinyint(1)              null comment '调查方法【字典编码：nh_survey_method】 1-样线法 2-样方法 3-定点观察 4-随机游走 5-陷阱法 6-网捕法 7-声音记录 8-红外相机 9-综合调查 10-其他',
    team_members    text                    null comment '团队成员(JSON)',
    species_count   int         default 0   null comment '发现物种数',
    species_list    text                    null comment '物种列表(JSON)',
    description     text                    null comment '调查描述',
    findings        text                    null comment '主要发现',
    images          text                    null comment '图片列表(JSON)',
    attachments     text                    null comment '附件列表(JSON)',
    is_shared       tinyint(1)  default 0   null comment '是否已分享到社群【字典编码：nh_yes_no】 0-否 1-是',
    shared_topic_id bigint                  null comment '关联的社群话题ID',
    audit_status    tinyint(1)  default 0   null comment '审核状态【字典编码：nh_audit_status】 0-草稿 1-待审核 2-已通过 3-已驳回',
    audit_time      datetime                null comment '审核时间',
    audit_by        varchar(64)             null comment '审核人',
    audit_remark    varchar(500)            null comment '审核备注',
    del_flag        char        default '0' null comment '删除标志: 0-正常, 2-删除',
    create_by       varchar(64) default ''  null comment '创建者',
    create_time     datetime                null comment '创建时间',
    update_by       varchar(64) default ''  null comment '更新者',
    update_time     datetime                null comment '更新时间',
    remark          varchar(500)            null comment '备注',
    topic_id        bigint                  null
)
    comment '野外调查记录表';

create index idx_audit_status
    on nh_field_survey (audit_status);

create index idx_create_time
    on nh_field_survey (create_time);

create index idx_survey_date
    on nh_field_survey (survey_date);

create index idx_user_id
    on nh_field_survey (user_id);

create table nh_identification_vote
(
    vote_id           bigint auto_increment comment '投票ID'
        primary key,
    identification_id bigint                                not null comment '鉴定请求ID',
    user_id           bigint                                not null comment '投票用户ID',
    user_name         varchar(50)                           null comment '用户名',
    user_type         char                                  not null comment '用户类型：0=探索者,1=鉴定者',
    vote_type         char                                  not null comment '投票类型：0=同意,1=不同意',
    vote_weight       int                                   not null comment '票权重',
    vote_time         datetime                              not null comment '投票时间',
    del_flag          char        default '0'               null comment '删除标志',
    create_by         varchar(64) default ''                null comment '创建者',
    create_time       datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by         varchar(64) default ''                null comment '更新者',
    update_time       datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_identification_user
        unique (identification_id, user_id)
)
    comment '鉴定投票记录表';

create index idx_identification_id
    on nh_identification_vote (identification_id);

create index idx_user_id
    on nh_identification_vote (user_id);

create table nh_identifier_application
(
    id            bigint auto_increment
        primary key,
    user_id       bigint                                 not null,
    user_name     varchar(64)                            not null,
    nick_name     varchar(64)  default ''                null,
    real_name     varchar(50)  default ''                null,
    expertise     varchar(500)                           not null,
    bio           text                                   null,
    qualification varchar(500) default ''                null,
    experience    text                                   null,
    status        tinyint(1)   default 0                 null comment '0=待审核,1=已通过,2=已拒绝',
    reject_reason varchar(500) default ''                null,
    review_time   datetime                               null,
    review_by     varchar(64)  default ''                null,
    create_time   datetime     default CURRENT_TIMESTAMP null,
    update_time   datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '鉴定者申请表';

create index idx_create_time
    on nh_identifier_application (create_time);

create index idx_status
    on nh_identifier_application (status);

create index idx_user_id
    on nh_identifier_application (user_id);

create table nh_observation_diary
(
    diary_id         bigint auto_increment comment '日志ID'
        primary key,
    user_id          bigint                  not null comment '用户ID',
    title            varchar(200)            not null comment '日志标题',
    observation_date datetime                not null comment '观察日期',
    location         varchar(500)            null comment '观察地点',
    route_info       text                    null comment '路线信息',
    weather          varchar(100)            null comment '天气情况',
    temperature      varchar(50)             null comment '温度',
    species_found    text                    null comment '发现物种（JSON数组）',
    content          text                    null comment '日志内容/心得体会',
    images           text                    null comment '图片路径（JSON数组）',
    visibility       char        default '0' not null comment '可见性（0私密 1公开）',
    is_archived      char        default '0' null comment '是否归档（0否 1是）',
    tags             varchar(500)            null comment '标签',
    observation_ids  varchar(500)            null comment '关联观察记录ID（逗号分隔）',
    is_shared        varchar(1)  default '0' null comment '是否已分享到社群: 0-否, 1-是',
    shared_topic_id  bigint                  null comment '关联的社群话题ID',
    create_by        varchar(64) default ''  null comment '创建者',
    create_time      datetime                null comment '创建时间',
    update_by        varchar(64) default ''  null comment '更新者',
    update_time      datetime                null comment '更新时间',
    remark           varchar(500)            null comment '备注',
    record_count     int         default 0   null comment '关联的观察记录数'
)
    comment '个人观察日志表';

create index idx_is_archived
    on nh_observation_diary (is_archived);

create index idx_observation_date
    on nh_observation_diary (observation_date);

create index idx_record_count
    on nh_observation_diary (record_count);

create index idx_user_id
    on nh_observation_diary (user_id);

create index idx_visibility
    on nh_observation_diary (visibility);

create table nh_observation_diary_backup
(
    diary_id    bigint      default 0   not null comment '日志ID',
    user_id     bigint                  not null comment '用户ID',
    title       varchar(200)            not null comment '标题',
    content     text                    not null comment '日志内容',
    diary_date  date                    null comment '日志日期',
    location    varchar(200)            null comment '地点',
    weather     varchar(50)             null comment '天气',
    temperature varchar(20)             null comment '温度',
    images      text                    null comment '图片URL列表（JSON数组）',
    videos      text                    null comment '视频URL列表（JSON数组）',
    tags        varchar(500)            null comment '标签（逗号分隔）',
    visibility  char        default '0' null comment '可见性（0私密 1公开）',
    is_archived char        default '0' null comment '是否归档（0否 1是）',
    view_count  int         default 0   null comment '浏览次数',
    like_count  int         default 0   null comment '点赞数',
    del_flag    char        default '0' null comment '删除标志',
    create_by   varchar(64) default ''  null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64) default ''  null comment '更新者',
    update_time datetime                null comment '更新时间',
    remark      varchar(500)            null comment '备注'
);

create table nh_observation_record
(
    record_id        bigint auto_increment comment '观察记录ID'
        primary key,
    user_id          bigint                  not null comment '用户ID',
    title            varchar(200)            not null comment '标题',
    observation_time datetime                not null comment '观察时间',
    location         varchar(200)            not null comment '观察地点',
    latitude         decimal(10, 6)          null comment '纬度',
    longitude        decimal(10, 6)          null comment '经度',
    species_type     tinyint                 not null comment '物种类型 0=植物,1=动物,2=真菌,3=其他',
    species_name     varchar(100)            not null comment '物种名称',
    habitat          varchar(500)            null comment '生境描述',
    description      text                    not null comment '详细描述',
    images           text                    null comment '图片URL列表（JSON数组）',
    videos           text                    null comment '视频URL列表（JSON数组）',
    audit_status     tinyint     default 0   null comment '审核状态 0=草稿,1=待审核,2=已通过,3=已驳回',
    submit_time      datetime                null comment '提交审核时间',
    review_time      datetime                null comment '审核时间',
    reviewer_id      bigint                  null comment '审核人ID',
    review_comment   varchar(500)            null comment '审核意见',
    reject_reason    varchar(500)            null comment '驳回原因',
    specimen_id      bigint                  null comment '关联的标本ID（审核通过后生成）',
    is_shared        tinyint     default 0   null comment '是否已分享 0=否,1=是',
    shared_topic_id  bigint                  null comment '关联的社群话题ID',
    del_flag         char        default '0' null comment '删除标志（0存在 2删除）',
    create_by        varchar(64) default ''  null comment '创建者',
    create_time      datetime                null comment '创建时间',
    update_by        varchar(64) default ''  null comment '更新者',
    update_time      datetime                null comment '更新时间',
    remark           varchar(500)            null comment '备注',
    topic_id         bigint                  null
)
    comment '观察记录表';

create index idx_observation_time
    on nh_observation_record (observation_time);

create index idx_review_status
    on nh_observation_record (audit_status);

create index idx_species_type
    on nh_observation_record (species_type);

create index idx_user_id
    on nh_observation_record (user_id);

create table nh_species_identification
(
    identification_id   bigint auto_increment comment '鉴定ID'
        primary key,
    user_id             bigint                  not null comment '用户ID',
    title               varchar(200)            not null comment '标题',
    description         text                    null comment '详细描述',
    images              text                    null comment '图片URL列表(JSON)',
    observation_time    datetime                null comment '观察时间',
    location            varchar(200)            null comment '观察地点',
    latitude            decimal(10, 7)          null comment '纬度',
    longitude           decimal(10, 7)          null comment '经度',
    habitat             text                    null comment '生境描述',
    features            text                    null comment '特征描述',
    status              tinyint     default 0   null comment '鉴定状态【字典编码：nh_identification_status】0=待鉴定,1=已回答,2=已解决',
    best_answer_id      bigint                  null comment '最佳答案ID',
    answer_count        int         default 0   null comment '回答数量',
    view_count          int         default 0   null comment '浏览次数',
    is_shared           tinyint     default 0   null comment '是否分享【字典编码：nh_yes_no】0=否,1=是',
    shared_topic_id     bigint                  null comment '关联的社群话题ID',
    audit_status        tinyint     default 0   null comment '审核状态【字典编码：nh_audit_status】0=草稿,1=待审核,2=已通过,3=已驳回',
    audit_time          datetime                null comment '审核时间',
    audit_by            varchar(64)             null comment '审核人',
    audit_remark        varchar(500)            null comment '审核备注',
    del_flag            char        default '0' null comment '删除标志: 0-正常, 2-删除',
    create_by           varchar(64) default ''  null comment '创建者',
    create_time         datetime                null comment '创建时间',
    update_by           varchar(64) default ''  null comment '更新者',
    update_time         datetime                null comment '更新时间',
    remark              varchar(500)            null comment '备注',
    topic_id            bigint                  null,
    ai_species_name     varchar(200)            null comment 'AI识别的物种名称',
    ai_score            decimal(10, 7)          null comment 'AI识别的置信分数',
    ai_type             varchar(20)             null comment 'AI识别类型（animal/plant）',
    baike_url           varchar(500)            null comment '百度百科URL',
    baike_image_url     varchar(500)            null comment '百科图片URL',
    baike_description   text                    null comment '百科描述',
    vote_agree_score    int         default 0   null comment '同意总积分',
    vote_disagree_score int         default 0   null comment '不同意总积分',
    vote_status         char        default '0' null comment '投票状态：0=未开始,1=进行中,2=已结束',
    vote_result         char        default '2' null
)
    comment '物种鉴定求助表';

create table nh_identification_answer
(
    answer_id         bigint auto_increment comment '回答ID'
        primary key,
    identification_id bigint              not null comment '鉴定ID',
    user_id           bigint              not null comment '回答用户ID',
    user_name         varchar(64)         null comment '回答用户名',
    content           text                not null comment '回答内容',
    species_name      varchar(200)        null comment '物种名称',
    confidence        varchar(20)         null comment '置信度: high-高, medium-中, low-低',
    reference         text                null comment '参考资料',
    images            text                null comment '参考图片(JSON)',
    is_best           tinyint default 0   null comment '是否最佳答案【字典编码：nh_yes_no】0=否,1=是',
    like_count        int     default 0   null comment '点赞数',
    del_flag          char    default '0' null comment '删除标志: 0-正常, 2-删除',
    create_time       datetime            null comment '创建时间',
    update_time       datetime            null comment '更新时间',
    constraint nh_identification_answer_ibfk_1
        foreign key (identification_id) references nh_species_identification (identification_id)
            on delete cascade
)
    comment '鉴定回答表';

create index idx_create_time
    on nh_identification_answer (create_time);

create index idx_identification_id
    on nh_identification_answer (identification_id);

create index idx_is_best
    on nh_identification_answer (is_best);

create index idx_user_id
    on nh_identification_answer (user_id);

create index idx_audit_status
    on nh_species_identification (audit_status);

create index idx_create_time
    on nh_species_identification (create_time);

create index idx_status
    on nh_species_identification (status);

create index idx_user_id
    on nh_species_identification (user_id);

create table nh_specimen
(
    specimen_id      bigint auto_increment comment '标本ID'
        primary key,
    record_id        bigint                  not null comment '来源观察记录ID',
    user_id          bigint                  not null comment '贡献用户ID',
    specimen_code    varchar(50)             null comment '标本编号',
    title            varchar(200)            not null comment '标题',
    species_type     varchar(50)             not null comment '物种类型',
    species_name     varchar(100)            not null comment '物种名称',
    scientific_name  varchar(200)            null comment '学名（管理员补充）',
    family           varchar(100)            null comment '科',
    genus            varchar(100)            null comment '属',
    protection_level varchar(50)             null comment '保护级别',
    observation_time datetime                not null comment '观察时间',
    location         varchar(200)            not null comment '地点',
    latitude         decimal(10, 6)          null comment '纬度',
    longitude        decimal(10, 6)          null comment '经度',
    habitat          varchar(500)            null comment '生境',
    description      text                    null comment '描述',
    images           text                    null comment '图片',
    videos           text                    null comment '视频',
    quality_score    int                     null comment '质量评分（0-100）',
    is_public        char        default '1' null comment '是否公开（0否 1是）',
    view_count       int         default 0   null comment '浏览次数',
    del_flag         char        default '0' null comment '删除标志',
    create_by        varchar(64) default ''  null comment '创建者',
    create_time      datetime                null comment '创建时间',
    update_by        varchar(64) default ''  null comment '更新者',
    update_time      datetime                null comment '更新时间',
    remark           varchar(500)            null comment '备注',
    constraint uk_record_id
        unique (record_id)
)
    comment '标本管理表';

create index idx_species_type
    on nh_specimen (species_type);

create index idx_specimen_code
    on nh_specimen (specimen_code);

create index idx_user_id
    on nh_specimen (user_id);

create table qa_conversation
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    user_id         bigint                               null comment '用户ID',
    username        varchar(50)                          null comment '用户名',
    title           varchar(200)                         not null comment '对话标题',
    qa_type         tinyint(1) default 1                 null comment '问答类型：1-普通问答，2-知识图谱问答',
    related_species varchar(200)                         null comment '相关物种',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'AI问答对话会话表';

create index idx_qa_type
    on qa_conversation (qa_type);

create index idx_user_id
    on qa_conversation (user_id);

create index idx_username
    on qa_conversation (username);

create table qa_history
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    user_id         bigint                               null comment '用户ID',
    conversation_id bigint                               null comment '对话ID',
    username        varchar(50)                          null comment '用户名',
    question        text                                 not null comment '用户问题',
    answer          text                                 null comment 'AI回答',
    qa_type         tinyint(1) default 1                 null comment '问答类型：1-普通问答，2-知识图谱问答',
    related_species varchar(200)                         null comment '相关物种',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    ip_address      varchar(50)                          null comment 'IP地址'
)
    comment 'AI问答历史记录表';

create index idx_conversation_id
    on qa_history (conversation_id);

create index idx_create_time
    on qa_history (create_time);

create index idx_qa_type
    on qa_history (qa_type);

create index idx_related_species
    on qa_history (related_species);

create index idx_user_id
    on qa_history (user_id);

create index idx_username
    on qa_history (username);

create table species_details
(
    detail_id           bigint auto_increment comment '详情ID'
        primary key,
    taxon_id            bigint                  not null comment '关联物种分类表taxon_id',
    morphology          text                    null comment '形态特征描述',
    habitat             varchar(500) default '' null comment '生境描述',
    distribution        varchar(500) default '' null comment '分布区域',
    conservation_status varchar(100) default '' null comment '保护状态',
    national_protection varchar(50)  default '' null comment '国家保护级别',
    ecological_role     varchar(500) default '' null comment '生态作用',
    update_by           varchar(64)  default '' null comment '更新者',
    update_time         datetime                null comment '更新时间',
    constraint uk_taxon_id
        unique (taxon_id)
)
    comment '物种详细信息表';

create index idx_conservation
    on species_details (conservation_status);

create table species_media
(
    media_id      bigint auto_increment comment '媒体ID（主键）'
        primary key,
    taxon_id      bigint                   not null comment '关联物种分类表taxon_id',
    media_type    char                     not null comment '媒体类型（1-图片，2-视频，3-音频）',
    file_url      varchar(255)             not null comment '媒体文件URL',
    thumbnail_url varchar(255) default ''  null comment '缩略图URL（仅图片）',
    is_standard   char         default '0' null comment '是否标准图鉴（1-是，0-否）',
    display_order tinyint      default 0   null comment '展示顺序（数字越小越靠前）',
    uploaded_by   varchar(64)  default ''  null comment '上传者（同create_by，可保留便于理解）',
    create_by     varchar(64)  default ''  null comment '创建者',
    create_time   datetime                 null comment '上传时间（同create_time）',
    update_by     varchar(64)  default ''  null comment '更新者',
    update_time   datetime                 null comment '更新时间',
    remark        varchar(500) default ''  null comment '备注（如“正面照”“栖息地视频”）'
)
    comment '物种多媒体资源表';

create index idx_is_standard
    on species_media (is_standard);

create index idx_taxon_media
    on species_media (taxon_id, media_type);

create table species_tag_rel
(
    rel_id      bigint auto_increment comment '关联ID（主键）'
        primary key,
    taxon_id    bigint                  not null comment '关联物种分类表taxon_id',
    tag_id      bigint                  not null comment '关联物种标签表tag_id',
    create_by   varchar(64)  default '' null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64)  default '' null comment '更新者',
    update_time datetime                null comment '更新时间',
    remark      varchar(200) default '' null comment '关联备注（如“2024年新增该标签关联”）',
    constraint uk_taxon_tag
        unique (taxon_id, tag_id)
)
    comment '物种与标签的关联表';

create index idx_tag_rel
    on species_tag_rel (tag_id);

create index idx_taxon_rel
    on species_tag_rel (taxon_id);

create table species_tags
(
    tag_id       bigint auto_increment comment '标签ID'
        primary key,
    tag_name     varchar(100)            not null comment '标签名称（唯一，如“入侵物种”“药用植物”）',
    tag_category varchar(100) default '' null comment '标签类别（如“功能标签”“生态标签”）',
    create_by    varchar(64)  default '' null comment '创建者',
    create_time  datetime                null comment '创建时间',
    update_by    varchar(64)  default '' null comment '更新者',
    update_time  datetime                null comment '更新时间',
    remark       varchar(200) default '' null comment '备注',
    constraint uk_tag_name
        unique (tag_name)
)
    comment '物种标签表';

create index idx_tag_category
    on species_tags (tag_category);

create table species_taxa
(
    taxon_id        bigint auto_increment comment '分类单元ID'
        primary key,
    scientific_name varchar(255)             not null comment '物种学名',
    chinese_name    varchar(255) default ''  null comment '物种中文名',
    common_names    varchar(500) default ''  null comment '俗名',
    rank_level      tinyint                  not null comment '分类阶元级别',
    parent_taxon_id bigint       default 0   null comment '父分类单元ID',
    taxon_code      varchar(100) default ''  null comment '分类单元编码',
    is_valid        char         default '1' null comment '是否有效（1-有效，0-无效）',
    synonyms        varchar(500) default ''  null comment '同义词',
    create_by       varchar(64)  default ''  null comment '创建者',
    create_time     datetime                 null comment '创建时间',
    update_by       varchar(64)  default ''  null comment '更新者',
    update_time     datetime                 null comment '更新时间',
    remark          varchar(500) default ''  null comment '备注',
    constraint uk_scientific_name
        unique (scientific_name),
    constraint uk_taxon_code
        unique (taxon_code)
)
    comment '物种分类基础表';

create index idx_parent_taxon
    on species_taxa (parent_taxon_id);

create index idx_rank_level
    on species_taxa (rank_level);

create table species_video_task
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    user_id       bigint                             null comment '用户ID',
    username      varchar(50)                        null comment '用户名',
    content       text                               not null comment '知识库返回的科普内容（作为视频生成prompt）',
    task_status   char     default '1'               not null comment '任务状态：1-等待中 2-生成中 3-成功 4-失败',
    video_url     varchar(500)                       null comment '视频地址（七牛云持久化）',
    error_message varchar(500)                       null comment '失败错误信息',
    remark        varchar(500)                       null comment '备注（问题标题）',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime                           null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '物种科普视频生成任务记录表';

create index idx_create_time
    on species_video_task (create_time);

create index idx_task_status
    on species_video_task (task_status);

create index idx_user_id
    on species_video_task (user_id);

create index idx_username
    on species_video_task (username);

create table sys_config
(
    config_id    int auto_increment comment '参数主键'
        primary key,
    config_name  varchar(100) default ''  null comment '参数名称',
    config_key   varchar(100) default ''  null comment '参数键名',
    config_value varchar(500) default ''  null comment '参数键值',
    config_type  char         default 'N' null comment '系统内置（Y是 N否）',
    create_by    varchar(64)  default ''  null comment '创建者',
    create_time  datetime                 null comment '创建时间',
    update_by    varchar(64)  default ''  null comment '更新者',
    update_time  datetime                 null comment '更新时间',
    remark       varchar(500)             null comment '备注'
)
    comment '参数配置表';

create table sys_dept
(
    dept_id     bigint auto_increment comment '部门id'
        primary key,
    parent_id   bigint      default 0   null comment '父部门id',
    ancestors   varchar(50) default ''  null comment '祖级列表',
    dept_name   varchar(30) default ''  null comment '部门名称',
    order_num   int         default 0   null comment '显示顺序',
    leader      varchar(20)             null comment '负责人',
    phone       varchar(11)             null comment '联系电话',
    email       varchar(50)             null comment '邮箱',
    status      char        default '0' null comment '部门状态（0正常 1停用）',
    del_flag    char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by   varchar(64) default ''  null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64) default ''  null comment '更新者',
    update_time datetime                null comment '更新时间'
)
    comment '部门表';

create table sys_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int          default 0   null comment '字典排序',
    dict_label  varchar(100) default ''  null comment '字典标签',
    dict_value  varchar(100) default ''  null comment '字典键值',
    dict_type   varchar(100) default ''  null comment '字典类型',
    css_class   varchar(100)             null comment '样式属性（其他样式扩展）',
    list_class  varchar(100)             null comment '表格回显样式',
    is_default  char         default 'N' null comment '是否默认（Y是 N否）',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '字典数据表';

create table sys_dict_type
(
    dict_id     bigint auto_increment comment '字典主键'
        primary key,
    dict_name   varchar(100) default ''  null comment '字典名称',
    dict_type   varchar(100) default ''  null comment '字典类型',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注',
    constraint dict_type
        unique (dict_type)
)
    comment '字典类型表';

create table sys_job
(
    job_id          bigint auto_increment comment '任务ID',
    job_name        varchar(64)  default ''        not null comment '任务名称',
    job_group       varchar(64)  default 'DEFAULT' not null comment '任务组名',
    invoke_target   varchar(500)                   not null comment '调用目标字符串',
    cron_expression varchar(255) default ''        null comment 'cron执行表达式',
    misfire_policy  varchar(20)  default '3'       null comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent      char         default '1'       null comment '是否并发执行（0允许 1禁止）',
    status          char         default '0'       null comment '状态（0正常 1暂停）',
    create_by       varchar(64)  default ''        null comment '创建者',
    create_time     datetime                       null comment '创建时间',
    update_by       varchar(64)  default ''        null comment '更新者',
    update_time     datetime                       null comment '更新时间',
    remark          varchar(500) default ''        null comment '备注信息',
    primary key (job_id, job_name, job_group)
)
    comment '定时任务调度表';

create table sys_job_log
(
    job_log_id     bigint auto_increment comment '任务日志ID'
        primary key,
    job_name       varchar(64)               not null comment '任务名称',
    job_group      varchar(64)               not null comment '任务组名',
    invoke_target  varchar(500)              not null comment '调用目标字符串',
    job_message    varchar(500)              null comment '日志信息',
    status         char          default '0' null comment '执行状态（0正常 1失败）',
    exception_info varchar(2000) default ''  null comment '异常信息',
    create_time    datetime                  null comment '创建时间'
)
    comment '定时任务调度日志表';

create table sys_logininfor
(
    info_id        bigint auto_increment comment '访问ID'
        primary key,
    user_name      varchar(50)  default ''  null comment '用户账号',
    ipaddr         varchar(128) default ''  null comment '登录IP地址',
    login_location varchar(255) default ''  null comment '登录地点',
    browser        varchar(50)  default ''  null comment '浏览器类型',
    os             varchar(50)  default ''  null comment '操作系统',
    status         char         default '0' null comment '登录状态（0成功 1失败）',
    msg            varchar(255) default ''  null comment '提示消息',
    login_time     datetime                 null comment '访问时间'
)
    comment '系统访问记录';

create index idx_sys_logininfor_lt
    on sys_logininfor (login_time);

create index idx_sys_logininfor_s
    on sys_logininfor (status);

create table sys_menu
(
    menu_id     bigint auto_increment comment '菜单ID'
        primary key,
    menu_name   varchar(50)              not null comment '菜单名称',
    parent_id   bigint       default 0   null comment '父菜单ID',
    order_num   int          default 0   null comment '显示顺序',
    path        varchar(200) default ''  null comment '路由地址',
    component   varchar(255)             null comment '组件路径',
    query       varchar(255)             null comment '路由参数',
    route_name  varchar(50)  default ''  null comment '路由名称',
    is_frame    int          default 1   null comment '是否为外链（0是 1否）',
    is_cache    int          default 0   null comment '是否缓存（0缓存 1不缓存）',
    menu_type   char         default ''  null comment '菜单类型（M目录 C菜单 F按钮）',
    visible     char         default '0' null comment '菜单状态（0显示 1隐藏）',
    status      char         default '0' null comment '菜单状态（0正常 1停用）',
    perms       varchar(100)             null comment '权限标识',
    icon        varchar(100) default '#' null comment '菜单图标',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500) default ''  null comment '备注'
)
    comment '菜单权限表';

create table sys_notice
(
    notice_id      int auto_increment comment '公告ID'
        primary key,
    notice_title   varchar(50)             not null comment '公告标题',
    notice_type    char                    not null comment '公告类型（1通知 2公告）',
    notice_content longblob                null comment '公告内容',
    status         char        default '0' null comment '公告状态（0正常 1关闭）',
    create_by      varchar(64) default ''  null comment '创建者',
    create_time    datetime                null comment '创建时间',
    update_by      varchar(64) default ''  null comment '更新者',
    update_time    datetime                null comment '更新时间',
    remark         varchar(255)            null comment '备注'
)
    comment '通知公告表';

create table sys_oper_log
(
    oper_id        bigint auto_increment comment '日志主键'
        primary key,
    title          varchar(50)   default '' null comment '模块标题',
    business_type  int           default 0  null comment '业务类型（0其它 1新增 2修改 3删除）',
    method         varchar(200)  default '' null comment '方法名称',
    request_method varchar(10)   default '' null comment '请求方式',
    operator_type  int           default 0  null comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' null comment '操作人员',
    dept_name      varchar(50)   default '' null comment '部门名称',
    oper_url       varchar(255)  default '' null comment '请求URL',
    oper_ip        varchar(128)  default '' null comment '主机地址',
    oper_location  varchar(255)  default '' null comment '操作地点',
    oper_param     varchar(2000) default '' null comment '请求参数',
    json_result    varchar(2000) default '' null comment '返回参数',
    status         int           default 0  null comment '操作状态（0正常 1异常）',
    error_msg      varchar(2000) default '' null comment '错误消息',
    oper_time      datetime                 null comment '操作时间',
    cost_time      bigint        default 0  null comment '消耗时间'
)
    comment '操作日志记录';

create index idx_sys_oper_log_bt
    on sys_oper_log (business_type);

create index idx_sys_oper_log_ot
    on sys_oper_log (oper_time);

create index idx_sys_oper_log_s
    on sys_oper_log (status);

create table sys_post
(
    post_id     bigint auto_increment comment '岗位ID'
        primary key,
    post_code   varchar(64)            not null comment '岗位编码',
    post_name   varchar(50)            not null comment '岗位名称',
    post_sort   int                    not null comment '显示顺序',
    status      char                   not null comment '状态（0正常 1停用）',
    create_by   varchar(64) default '' null comment '创建者',
    create_time datetime               null comment '创建时间',
    update_by   varchar(64) default '' null comment '更新者',
    update_time datetime               null comment '更新时间',
    remark      varchar(500)           null comment '备注'
)
    comment '岗位信息表';

create table sys_role
(
    role_id             bigint auto_increment comment '角色ID'
        primary key,
    role_name           varchar(30)             not null comment '角色名称',
    role_key            varchar(100)            not null comment '角色权限字符串',
    role_sort           int                     not null comment '显示顺序',
    data_scope          char        default '1' null comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
    menu_check_strictly tinyint(1)  default 1   null comment '菜单树选择项是否关联显示',
    dept_check_strictly tinyint(1)  default 1   null comment '部门树选择项是否关联显示',
    status              char                    not null comment '角色状态（0正常 1停用）',
    del_flag            char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by           varchar(64) default ''  null comment '创建者',
    create_time         datetime                null comment '创建时间',
    update_by           varchar(64) default ''  null comment '更新者',
    update_time         datetime                null comment '更新时间',
    remark              varchar(500)            null comment '备注'
)
    comment '角色信息表';

create table sys_role_dept
(
    role_id bigint not null comment '角色ID',
    dept_id bigint not null comment '部门ID',
    primary key (role_id, dept_id)
)
    comment '角色和部门关联表';

create table sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID',
    primary key (role_id, menu_id)
)
    comment '角色和菜单关联表';

create table sys_user
(
    user_id         bigint auto_increment comment '用户ID'
        primary key,
    dept_id         bigint                    null comment '部门ID',
    user_name       varchar(30)               not null comment '用户账号',
    nick_name       varchar(30)               not null comment '用户昵称',
    user_type       varchar(2)   default '00' null comment '用户类型（00系统用户）',
    email           varchar(50)  default ''   null comment '用户邮箱',
    phonenumber     varchar(11)  default ''   null comment '手机号码',
    sex             char         default '0'  null comment '用户性别（0男 1女 2未知）',
    avatar          varchar(100) default ''   null comment '头像地址',
    password        varchar(100) default ''   null comment '密码',
    status          char         default '0'  null comment '账号状态（0正常 1停用）',
    del_flag        char         default '0'  null comment '删除标志（0代表存在 2代表删除）',
    login_ip        varchar(128) default ''   null comment '最后登录IP',
    login_date      datetime                  null comment '最后登录时间',
    pwd_update_date datetime                  null comment '密码最后更新时间',
    create_by       varchar(64)  default ''   null comment '创建者',
    create_time     datetime                  null comment '创建时间',
    update_by       varchar(64)  default ''   null comment '更新者',
    update_time     datetime                  null comment '更新时间',
    remark          varchar(500)              null comment '备注'
)
    comment '用户信息表';

create table sys_user_post
(
    user_id bigint not null comment '用户ID',
    post_id bigint not null comment '岗位ID',
    primary key (user_id, post_id)
)
    comment '用户与岗位关联表';

create table sys_user_role
(
    user_id bigint not null comment '用户ID',
    role_id bigint not null comment '角色ID',
    primary key (user_id, role_id)
)
    comment '用户和角色关联表';

create definer = root@`%` view v_user_observation_stats as
select `u`.`user_id`                                                                           AS `user_id`,
       `u`.`nick_name`                                                                         AS `nick_name`,
       count(distinct `r`.`record_id`)                                                         AS `total_records`,
       count(distinct (case when (`r`.`review_status` = 'approved') then `r`.`record_id` end)) AS `approved_records`,
       count(distinct (case when (`r`.`review_status` = 'pending') then `r`.`record_id` end))  AS `pending_records`,
       count(distinct (case when (`r`.`review_status` = 'rejected') then `r`.`record_id` end)) AS `rejected_records`,
       count(distinct `d`.`diary_id`)                                                          AS `total_diaries`,
       count(distinct `s`.`specimen_id`)                                                       AS `total_specimens`,
       count(distinct `r`.`species_type`)                                                      AS `species_types_count`
from (((`ry-vue`.`sys_user` `u` left join `ry-vue`.`nh_observation_record` `r`
        on (((`u`.`user_id` = `r`.`user_id`) and (`r`.`del_flag` = '0')))) left join `ry-vue`.`nh_observation_diary` `d`
       on (((`u`.`user_id` = `d`.`user_id`) and (`d`.`del_flag` = '0')))) left join `ry-vue`.`nh_specimen` `s`
      on (((`u`.`user_id` = `s`.`user_id`) and (`s`.`del_flag` = '0'))))
where (`u`.`del_flag` = '0')
group by `u`.`user_id`, `u`.`nick_name`;

