create table im_character_avatar
(
    id                      bigint auto_increment comment '主键'
        primary key,
    template_character_id   bigint                               null comment '模板人物id',
    template_character_name varchar(50)                          not null comment '模板人物名称',
    name                    varchar(50)                          not null comment '头像名称',
    avatar                  varchar(1000)                        not null comment '头像链接',
    level                   tinyint  default 0                   not null comment '头像等级',
    status                  varchar(2)                           null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    create_time             datetime default current_timestamp() not null comment '创建时间',
    update_time             datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    create_by               bigint                               null comment '创建人',
    update_by               bigint                               null comment '更新人',
    deleted                 tinyint  default 0                   not null comment '是否删除：0-否；1-是',
    constraint idx_1
        unique (template_character_name, name)
)
    comment '模板人物头像配置表';

create table im_character_emo
(
    id                bigint auto_increment comment 'id'
        primary key,
    template_group_id bigint                                 not null comment '群聊模板id',
    character_id      bigint                                 not null comment '角色id',
    character_name    varchar(50)                            not null comment '角色名称',
    name              varchar(100)                           null comment '名称',
    url               varchar(1500)                          null comment '表情包图片链接',
    status            varchar(2) default '0'                 not null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    create_time       datetime   default current_timestamp() not null comment '创建时间',
    update_time       datetime                               null comment '更新时间',
    create_by         bigint                                 not null comment '创建人',
    update_by         bigint                                 null comment '更新人',
    deleted           tinyint(1) default 0                   not null comment '是否删除：0-否；1-是'
)
    comment '角色表情包表';

create index idx_1
    on im_character_emo (character_id);

create table im_character_word
(
    id                bigint auto_increment comment 'id'
        primary key,
    template_group_id bigint                                 not null comment '群聊模板id',
    character_id      bigint                                 not null comment '角色id',
    character_name    varchar(50)                            not null comment '角色名称',
    word              varchar(100)                           not null comment '台词',
    voice             varchar(1000)                          null comment '语音',
    status            varchar(2) default '0'                 not null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    create_time       datetime   default current_timestamp() not null comment '创建时间',
    update_time       datetime                               null comment '更新时间',
    create_by         bigint                                 not null comment '创建人',
    update_by         bigint                                 null comment '更新人',
    deleted           tinyint(1) default 0                   not null comment '是否删除：0-否；1-是'
)
    comment '角色台词表';

create index idx_1
    on im_character_word (character_id);

create table im_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int(4)       default 0   null comment '字典排序',
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


create table im_friend
(
    id                bigint auto_increment comment 'id'
        primary key,
    user_id           bigint                                   not null comment '用户id',
    friend_id         bigint                                   not null comment '好友id',
    friend_nick_name  varchar(255)                             not null comment '好友昵称',
    friend_head_image varchar(255) default ''                  null comment '好友头像',
    friend_remark     varchar(50)                              null comment '好友备注',
    created_time      datetime     default current_timestamp() null comment '创建时间'
)
    comment '好友表';

create index idx_friend_id
    on im_friend (friend_id);

create index idx_user_id
    on im_friend (user_id);

create table im_group
(
    id                bigint auto_increment comment 'id'
        primary key,
    name              varchar(255)                              not null comment '群名字',
    owner_id          bigint                                    not null comment '群主id',
    head_image        varchar(255)  default ''                  null comment '群头像',
    head_image_thumb  varchar(255)  default ''                  null comment '群头像缩略图',
    group_type        tinyint(1)    default 0                   not null comment '群聊类型',
    is_template       tinyint(1)    default 0                   not null comment '是否模板群聊（0：否；1：是）',
    template_group_id bigint                                    null comment '模板群聊id',
    notice            varchar(1024) default ''                  null comment '群公告',
    remark            varchar(255)  default ''                  null comment '群备注',
    is_banned         smallint(1)   default 0                   not null comment '禁止发言（1：是；0否）',
    ban_type          varchar(50)                               null comment '被禁止发言类型（admin：管理员禁止；master：群主禁止）',
    ban_expire_time   datetime                                  null comment '禁止发言失效时间',
    deleted           tinyint(1)    default 0                   not null comment '是否已删除',
    created_time      datetime      default current_timestamp() null comment '创建时间',
    switch_time       datetime                                  null comment '切换模板群聊时间'
)
    comment '群';

create table im_group_member
(
    id                    bigint auto_increment comment 'id'
        primary key,
    group_id              bigint                                    not null comment '群id',
    user_id               bigint                                    not null comment '用户id',
    alias_name            varchar(255)  default ''                  not null comment '群别名',
    alias_name_prefix     varchar(10)                               null comment '昵称前缀',
    alias_name_suffix     varchar(10)                               null comment '昵称后缀',
    nickname              varchar(10)                               null comment '用户昵称',
    head_image            varchar(1000) default ''                  null comment '用户头像',
    head_image_def        varchar(1000)                             null comment '自定义头像',
    is_template           tinyint(1)    default 0                   not null comment '是否模板人物',
    template_character_id bigint        default 0                   not null comment '模板人物id',
    character_avatar_id   bigint                                    null comment '模板人物头像id',
    avatar_alias          varchar(50)                               null comment '头像别名',
    remark                varchar(255)  default ''                  null comment '备注',
    quit                  tinyint(1)    default 0                   not null comment '是否已退出',
    quit_time             datetime                                  null comment '退出时间',
    show_nickname         tinyint(1)    default 0                   not null comment '是否展示群成员昵称（0：否；1：是）',
    is_banned             smallint(1)   default 0                   not null comment '禁止发言（1：是；0否）',
    ban_type              varchar(50)                               null comment '被禁止发言类型（admin：管理员禁止；master：群主禁止）',
    ban_expire_time       datetime                                  null comment '禁止发言失效时间',
    created_time          datetime      default current_timestamp() null comment '创建时间',
    switch_time           datetime                                  null comment '模板人物切换时间',
    constraint unique_idx_1
        unique (group_id, user_id)
)
    comment '群成员表';

create index idx_group_id
    on im_group_member (group_id);

create index idx_user_id
    on im_group_member (user_id);

create table im_group_message
(
    id             bigint auto_increment comment 'id'
        primary key,
    group_id       bigint                                    not null comment '群id',
    send_id        bigint                                    not null comment '发送用户id',
    send_nick_name varchar(200)  default ''                  null comment '发送用户昵称',
    recv_ids       varchar(1000) default ''                  null comment '接收用户id,逗号分隔，为空表示发给所有成员',
    content        text                                      null comment '发送内容',
    quote_id       bigint                                    null comment '引用消息id',
    quote_msg      text                                      null comment '引用消息',
    at_user_ids    varchar(1000)                             null comment '@的用户id列表',
    receipt        tinyint       default 0                   not null comment '是否回执消息',
    receipt_ok     tinyint       default 0                   not null comment '回执消息是否完成',
    type           tinyint(1)                                not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 10:系统提示',
    status         tinyint(1)    default 0                   null comment '状态 0:未发出 1:已送达  2:撤回 3:已读',
    send_time      datetime      default current_timestamp() null comment '发送时间'
)
    comment '群消息表';

create index idx_group_id
    on im_group_message (group_id);

create table im_group_msg_read_position
(
    group_id     bigint                               not null comment '群聊id',
    user_id      bigint                               not null comment '用户id',
    group_msg_id bigint                               not null comment '群聊消息id',
    create_time  datetime default current_timestamp() null comment '创建时间',
    update_time  datetime default current_timestamp() null on update current_timestamp() comment '更新时间',
    constraint idx1
        unique (group_id, user_id)
)
    comment '记录用户群聊消息的已读位置表';

create table im_media_material
(
    id               int auto_increment comment '主键'
        primary key,
    title            varchar(100)                         not null comment '素材名称',
    description      varchar(200)                         null comment '素材简介',
    url              varchar(1000)                        not null comment '素材链接',
    cover_image      varchar(1000)                        null comment '封面图片',
    type             varchar(10)                          not null comment '素材类型',
    format           varchar(10)                          not null comment '格式',
    display_duration int                                  not null comment '素材展示时长（单位秒）',
    sort             int                                  not null comment '排序号',
    status           tinyint  default 1                   not null comment '状态（1：启用；0：停用）',
    start_time       datetime                             null comment '素材展示开始时间',
    end_time         datetime                             null comment '素材展示结束时间',
    create_time      datetime default current_timestamp() not null comment '创建时间',
    update_time      datetime default current_timestamp() not null on update current_timestamp() comment '更新时间'
)
    comment '媒体素材表';

create index idx1
    on im_media_material (sort);

create table im_music
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                                 not null comment '用户id',
    singer      varchar(100)                           null comment '歌手',
    name        varchar(100)                           not null comment '歌曲名称',
    url         varchar(255)                           not null comment '歌曲链接',
    cover       varchar(1000)                          null comment '封面图片',
    duration    int                                    null comment '歌曲时长',
    play_count  int        default 0                   not null comment '播放次数',
    category    varchar(10)                            not null comment '分类（private：个人，group：群聊，region：地区）',
    group_id    bigint                                 null comment '群id',
    region_code varchar(50)                            null comment '地区编码',
    deleted     tinyint(1) default 0                   not null comment '是否删除（0：否；1：是）',
    create_time datetime   default current_timestamp() not null comment '创建时间',
    update_time datetime   default current_timestamp() null on update current_timestamp() comment '更新时间'
)
    comment '音乐表';

create table im_music_star
(
    id          bigint               not null comment '主键'
        primary key,
    user_id     bigint               not null comment '用户id',
    music_id    bigint               not null comment '歌曲id',
    create_time datetime             not null comment '创建时间',
    update_time datetime             null comment '更新时间',
    deleted     tinyint(1) default 0 not null comment '是否删除（1：是；0：否）'
)
    comment '音乐点赞表';

create table im_picture
(
    id           int auto_increment comment '主键'
        primary key,
    picture_name varchar(100)                           not null comment '图片名称',
    category     varchar(20)                            not null comment '图片类型',
    format       varchar(20)                            not null comment '格式',
    url          varchar(1000)                          null comment '图片链接',
    origin       varchar(100)                           not null comment '来源',
    origin_id    varchar(50)                            null comment '图片来源id',
    status       tinyint(1) default 1                   not null comment '状态（1：有效；0：失效）',
    create_time  datetime   default current_timestamp() null comment '创建时间',
    update_time  datetime   default current_timestamp() null on update current_timestamp() comment '更新时间',
    constraint idx1
        unique (origin_id)
)
    comment '图片表';

create table im_private_message
(
    id        bigint auto_increment comment 'id'
        primary key,
    send_id   bigint                               not null comment '发送用户id',
    recv_id   bigint                               not null comment '接收用户id',
    content   text                                 null comment '发送内容',
    quote_id  bigint                               null comment '引用消息id',
    quote_msg text                                 null comment '引用消息',
    type      tinyint(1)                           not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 10:系统提示',
    status    tinyint(1)                           not null comment '状态 0:未送达 1:送达 2:撤回 3:已读',
    send_time datetime default current_timestamp() null comment '发送时间'
)
    comment '私聊消息';

create index idx_send_recv_id
    on im_private_message (send_id, recv_id);

create table im_push_task
(
    seq_no      bigint auto_increment comment '序列号'
        primary key,
    pusher_id   bigint(50)           not null comment '推送者id',
    recv_range  varchar(10)          not null comment '接收者范围（all：全部用户；part：部分用户）',
    sys_msg_id  bigint               not null comment '系统消息id',
    title       varchar(100)         not null comment '消息标题',
    send_time   datetime             not null comment '推送时间',
    push_status tinyint(1) default 0 not null comment '推送状态（0：未推送；1：已推送）',
    status      tinyint(1) default 0 not null comment '状态 0:未发出 1:已送达  2:撤回 3:已读',
    create_by   bigint               not null comment '创建者',
    create_time datetime             not null comment '创建时间',
    deleted     tinyint(1) default 0 not null comment '是否删除'
)
    comment '推送任务表';

create index idx_sys_msg_id
    on im_push_task (sys_msg_id);

create table im_push_task_receiver
(
    id      bigint auto_increment comment 'id'
        primary key,
    seq_no  bigint not null comment '推送消息序列号',
    user_id bigint not null comment '用户id'
)
    comment '推送任务接收人';

create index idx_seq_no
    on im_push_task_receiver (seq_no);

create index idx_user_id
    on im_push_task_receiver (user_id);

create table im_pusher
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(50)          not null comment '推送主体名称',
    head_image  varchar(1000)        not null comment '推送主体头像',
    create_by   bigint               not null comment '创建人',
    create_time datetime             not null comment '创建时间',
    update_by   bigint               null comment '更新人',
    update_time datetime             null comment '更新时间',
    deleted     tinyint(1) default 0 not null comment '是否删除'
)
    comment '推送主体表';

create table im_region
(
    code             varchar(100)         not null comment '地区编码'
        primary key,
    parent_code      varchar(100)         not null comment '上级地区编码',
    level            tinyint(1)           not null comment '地区级别(0：地球；1：国家；2：省；3：市；4：县/区；5：镇/乡；6：村)',
    name             varchar(200)         not null comment '地区名称',
    leader_call_name varchar(50)          null comment '地区领导称呼',
    latitude         varchar(100)         null comment '纬度',
    longitude        varchar(100)         null comment '经度',
    deleted          tinyint(1) default 0 not null comment '是否删除',
    is_banned        tinyint(1) default 0 not null comment '是否被禁止发言',
    ban_type         varchar(50)          null comment '被禁止发言类型（admin：管理员禁止）',
    ban_expire_time  datetime             null comment '禁止发言失效时间',
    create_by        bigint               null comment '创建人用户id',
    create_time      datetime             null comment '创建时间',
    update_by        bigint               null comment '更新人用户id',
    update_time      datetime             null comment '更新时间'
)
    comment '地区表';

create index pcode_idx
    on im_region (parent_code);

create table im_region_group
(
    id                bigint auto_increment comment 'id'
        primary key,
    code              varchar(100)         not null comment '地区编码',
    region_group_name varchar(200)         not null comment '地区群聊名称',
    num               varchar(20)          not null comment '地区群聊编号',
    leader_id         bigint               null comment '地区群聊群主id',
    effective_time    datetime             null comment '群主生效时间',
    expiration_time   datetime             null comment '群主失效时间',
    deleted           tinyint(1) default 0 not null comment '是否删除',
    is_banned         tinyint(1) default 0 not null comment '是否被禁止发言',
    ban_type          varchar(50)          null comment '被禁止发言类型（admin：管理员禁止）',
    ban_expire_time   datetime             null comment '禁止发言失效时间',
    remark            varchar(200)         null comment '备注',
    create_by         bigint               null comment '创建人用户id',
    create_time       datetime             null comment '创建时间',
    update_by         bigint               null comment '更新人用户id',
    update_time       datetime             null comment '更新时间',
    constraint unique_1
        unique (code, num)
)
    comment '地区群聊表';

create table im_region_group_member
(
    id              bigint auto_increment comment 'id'
        primary key,
    region_group_id bigint               not null comment '地区群聊id',
    code            varchar(50)          null comment '地区编码',
    user_id         bigint               not null comment '用户id',
    alias_name      varchar(255)         null comment '用户聊天名称',
    head_image      varchar(1000)        null comment '用户头像',
    quit            tinyint(1)           null comment '是否退出',
    quit_time       datetime             null comment '退出时间',
    is_banned       tinyint(1) default 0 not null comment '是否被禁止发言',
    ban_type        varchar(50)          null comment '被禁止发言类型（admin：管理员禁止）',
    ban_expire_time datetime             null comment '禁止发言失效时间',
    remark          varchar(200)         null comment '备注',
    create_by       bigint               null comment '创建人用户id',
    create_time     datetime             null comment '创建时间',
    update_by       bigint               null comment '更新人用户id',
    update_time     datetime             null comment '更新时间',
    constraint unique_1
        unique (region_group_id, user_id)
)
    comment '地区群聊常驻用户表';

create table im_region_group_message
(
    id              bigint auto_increment comment 'id'
        primary key,
    region_group_id bigint                                   not null comment '地区群聊id',
    send_id         bigint                                   not null comment '发送用户id',
    send_nick_name  varchar(200) default ''                  null comment '发送用户昵称',
    recv_ids        varchar(1000)                            null comment '接收用户id,逗号分隔，为空表示发给所有成员',
    content         text                                     null comment '发送内容',
    quote_id        bigint                                   null comment '引用消息id',
    quote_msg       text                                     null comment '引用消息',
    at_user_ids     varchar(1000)                            null comment '@的用户id列表',
    receipt         tinyint      default 0                   not null comment '是否回执消息',
    receipt_ok      tinyint      default 0                   not null comment '回执消息是否完成',
    type            tinyint(1)                               not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 10:系统提示',
    status          tinyint(1)   default 0                   null comment '状态 0:正常  2:撤回',
    send_time       datetime     default current_timestamp() null comment '发送时间'
)
    comment '地区群聊消息表';

create index idx_group_id
    on im_region_group_message (region_group_id);

create table im_system_message
(
    id          bigint auto_increment comment 'id'
        primary key,
    title       varchar(50)          not null comment '标题',
    cover_url   varchar(1000)        null comment '封面图片',
    intro       varchar(255)         null comment '简述',
    content     text                 null comment '内容',
    extern_link varchar(2000)        null comment '外部链接',
    type        tinyint(1)           not null comment '消息类型 0:文字 1:图片 2:文件 3:音频 4:视频 9:富文本 ',
    create_time datetime             not null comment '创建时间',
    create_by   bigint               not null comment '创建者',
    update_time datetime             null comment '更新时间',
    update_by   bigint               null comment '更新者',
    deleted     tinyint(1) default 0 not null comment '是否删除'
)
    comment '系统消息表';

create table im_talk
(
    id             int auto_increment comment '主键'
        primary key,
    user_id        bigint               not null comment '用户id',
    character_id   bigint               null comment '角色人物id',
    avatar_id      bigint               null comment '角色头像id',
    nick_name      varchar(50)          null comment '用户昵称',
    avatar         varchar(1000)        null comment '用户头像',
    content        varchar(200)         not null comment '内容',
    files          text                 null comment '文件列表',
    scope          tinyint(1)           not null comment '公布范围（1：私密；2：好友可见；9：公开）',
    category       varchar(10)          not null comment '分类（private：个人，group：群聊，region：地区）',
    group_id       bigint               null comment '群聊id',
    region_code    varchar(50)          null comment '地区编码',
    group_visible  tinyint(1) default 0 not null comment '群聊空间可见',
    region_visible tinyint(1) default 0 not null comment '地区空间可见',
    address        varchar(200)         null comment '发布地址',
    create_time    datetime             not null comment '创建时间',
    create_by      bigint               not null comment '创建人',
    update_time    datetime             null comment '更新时间',
    update_by      bigint               null comment '更新人',
    deleted        tinyint(1) default 0 not null comment '是否删除（1：是；0：否）'
)
    comment '说说表';

create index idx_1
    on im_talk (user_id);

create table im_talk_comment
(
    id                  bigint               not null comment '主键'
        primary key,
    talk_id             bigint               not null comment '动态id',
    user_id             bigint               not null comment '用户id',
    user_nickname       varchar(50)          not null comment '用户昵称',
    user_avatar         varchar(1000)        null comment '用户头像',
    content             varchar(100)         not null comment '评论内容',
    character_id        bigint               null comment '角色id',
    avatar_id           bigint               null comment '角色头像id',
    reply_comment_id    bigint               null comment '回复的评论id',
    reply_user_id       bigint               null comment '回复用户id',
    reply_user_character_id       bigint     null comment '回复用户角色id',
    reply_user_avatar   varchar(1000)        null comment '回复用户头像',
    reply_user_nickname varchar(50)          null comment '回复用户昵称',
    ip                  varchar(200)         null comment 'IP地址',
    ip_address          varchar(200)         null comment 'IP来源',
    deleted             tinyint(1) default 0 not null comment '是否删除（0：否；1：是）',
    create_time         datetime             not null comment '创建时间',
    create_by           bigint               not null comment '创建人',
    update_time         datetime             null comment '更新时间',
    update_by           bigint               null comment '更新人'
)
    comment '动态评论表';

create index idx_1
    on im_talk_comment (talk_id);

create index idx_2
    on im_talk_comment (user_id);

create table im_talk_notify
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint               not null comment '接收提醒的用户ID',
    talk_id     bigint               not null comment '动态id',
    comment_id  bigint               null comment '评论id',
    star_id     bigint               null comment '点赞id',
    action_type tinyint(1)           not null comment '提醒类型（点赞、评论、@提及）',
    category    varchar(100)         not null comment '分类',
    group_id    bigint               null comment '群聊id',
    region_code varchar(50)          null comment '地区编码',
    is_read     tinyint(1) default 0 not null comment '是否已读',
    deleted     tinyint(1) default 0 not null comment '删除标识',
    create_time datetime             not null comment '提醒生成的时间',
    update_time datetime             null comment '更新时间'
)
    comment '朋友圈动态通知表';

create index idx_1
    on im_talk_notify (user_id);

create table im_talk_star
(
    id           bigint auto_increment comment '主键'
        primary key,
    talk_id      bigint               not null comment '动态id',
    user_id      bigint               not null comment '用户id',
    nickname     varchar(50)          null comment '用户昵称',
    avatar       varchar(1000)        null comment '用户头像',
    character_id bigint               null comment '角色id',
    avatar_id    bigint               null comment '角色头像id',
    create_time  datetime             not null comment '创建时间',
    create_by    bigint               not null comment '创建人',
    update_time  datetime             null comment '更新时间',
    update_by    bigint               null comment '更新人',
    deleted      tinyint(1) default 0 not null comment '是否删除（0：否；1：是）',
    constraint idx1
        unique (talk_id, user_id)
)
    comment '动态赞星表';

create table im_template_character
(
    id                bigint auto_increment comment '主键'
        primary key,
    template_group_id bigint                                 not null comment '模板群聊id',
    name              varchar(50)                            not null comment '人物名称',
    description       varchar(200)                           null comment '人物描述',
    avatar            varchar(500)                           not null comment '人物头像',
    create_time       datetime   default current_timestamp() not null comment '创建时间',
    update_time       datetime   default current_timestamp() not null on update current_timestamp() comment '更新时间',
    create_by         bigint                                 null comment '创建人',
    update_by         bigint                                 null comment '更新人',
    status            varchar(2)                             null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    deleted           tinyint(1) default 0                   not null comment '是否删除：0-否；1-是',
    constraint idx_1
        unique (template_group_id, name)
)
    comment '模板群的模板人物';

create table im_template_group
(
    id          bigint auto_increment comment '主键'
        primary key,
    group_name  varchar(20)                            not null comment '群名称',
    description varchar(200)                           null comment '描述',
    avatar      varchar(1000)                          null comment '群头像',
    count       int        default 0                   not null comment '群人数',
    status      varchar(2)                             null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    create_time datetime   default current_timestamp() not null comment '创建时间',
    update_time datetime   default current_timestamp() not null on update current_timestamp() comment '更新时间',
    create_by   bigint                                 null comment '创建人',
    update_by   bigint                                 null comment '更新人',
    deleted     tinyint(1) default 0                   not null comment '是否删除：0-否；1-是'
)
    comment '模板群';

create table im_user
(
    id               bigint auto_increment comment 'id'
        primary key,
    user_name        varchar(255)                              not null comment '用户名',
    nick_name        varchar(255)                              not null comment '用户昵称',
    email            varchar(100)  default ''                  not null comment '用户邮箱',
    head_image       varchar(255)  default ''                  null comment '用户头像',
    head_image_thumb varchar(255)  default ''                  null comment '用户头像缩略图',
    password         varchar(255)                              not null comment '密码(明文)',
    sex              tinyint(1)    default 0                   null comment '性别 0:男 1::女',
    type             smallint(1)   default 1                   null comment '用户类型 1:普通用户 2:审核账户',
    signature        varchar(1024) default ''                  null comment '个性签名',
    qq_open_id       varchar(255)                              null comment 'QQ开放id',
    qq_access_token  varchar(255)                              null comment 'QQ访问令牌',
    login_type       tinyint(1)                                null comment '登录类型',
    is_disable       tinyint(1)    default 0                   not null comment '是否禁用',
    ip_address       varchar(50)                               null comment '用户登录IP',
    ip_source        varchar(50)                               null comment 'IP来源',
    province         varchar(50)                               null comment '省份',
    city             varchar(50)                               null comment '城市',
    is_banned        smallint(1)   default 0                   not null comment '禁止发言（1：是；0：否）',
    auto_play        tinyint(1)    default 0                   not null comment '语音自动播放（1：是；0：否）',
    sound_play       tinyint(1)    default 0                   not null comment '消息提示音（1：开启；0：关闭）',
    last_login_time  datetime      default current_timestamp() null on update current_timestamp() comment '最后登录时间',
    create_time      datetime      default current_timestamp() null comment '创建时间',
    update_time      datetime      default current_timestamp() null on update current_timestamp() comment '更新时间',
    constraint idx_email
        unique (email),
    constraint idx_user_name
        unique (user_name)
)
    comment '用户表';

create index idx_nick_name
    on im_user (nick_name);

create table im_visitor
(
    id               int auto_increment comment '主键'
        primary key,
    ip               varchar(50)                          null comment 'IP地址',
    nation           varchar(50)                          null comment '国家',
    pro              varchar(50)                          null comment '省',
    city             varchar(50)                          null comment '市',
    browser          varchar(100)                         null comment '浏览器',
    operating_system varchar(100)                         null comment '操作系统',
    addr             varchar(200)                         null comment '地址',
    domain           varchar(200)                         null comment '域名',
    location_info    varchar(1000)                        null comment '位置信息json结果',
    create_time      datetime default current_timestamp() not null comment '创建时间'
)
    comment '访客信息表';


create table im_sensitive_word
(
    id          bigint auto_increment comment 'id'
        primary key,
    content     varchar(64)                          not null comment '敏感词内容',
    enabled     tinyint  default 0                   null comment '是否启用 0:未启用 1:启用',
    creator     bigint                               null comment '创建者',
    create_time datetime default current_timestamp() null comment '创建时间'
)
    comment '敏感词';

create table t_hero_info
(
    id               int auto_increment comment 'id'
        primary key,
    hero_name        varchar(50)          not null comment '英雄名称',
    hero_detail_url  varchar(1500)        null comment '英雄详情链接',
    hero_profile_url varchar(1500)        null comment '英雄头像链接',
    hero_id          varchar(50)          null comment '英雄id',
    is_crawl         tinyint(1) default 0 not null comment '是否爬取',
    category         varchar(50)          not null comment '类别',
    create_time      datetime             null comment '创建时间',
    update_time      datetime             null comment '更新时间',
    constraint idx_1
        unique (hero_name, category)
)
    comment '英雄信息表';

create table t_hero_skin
(
    id               int auto_increment
        primary key,
    hero_id          varchar(50)          not null comment '英雄id',
    hero_name        varchar(50)          not null comment '英雄名称',
    skin_name        varchar(50)          not null comment '皮肤名称',
    skin_url         varchar(1500)        null comment '皮肤图片链接',
    skin_profile_url varchar(1500)        null comment '皮肤头像链接',
    category         varchar(50)          not null comment '类别',
    is_crawl         tinyint(1) default 0 not null comment '是否爬取',
    create_time      datetime             null comment '创建时间',
    update_time      datetime             null comment '更新时间',
    constraint idx_1
        unique (hero_id, category, skin_name)
)
    comment '英雄皮肤数据表';

create table t_hero_word
(
    id          int auto_increment comment 'id'
        primary key,
    hero_id     varchar(50)          not null comment '英雄id',
    hero_name   varchar(50)          not null comment '英雄名称',
    category    varchar(20)          not null comment '类别',
    word        varchar(200)         null comment '台词',
    voice_url   varchar(1500)        null comment '台词语音',
    is_crawl    tinyint(1) default 0 not null comment '是否爬取',
    create_time datetime             null comment '创建时间',
    update_time datetime             null comment '更新时间'
)
    comment '英雄台词表';