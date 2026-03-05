package com.zhangzc.sharethingchatimpl.enums;

public enum UserActionEnum {
    ARTICLE_READ(1, "文章阅读", "用户阅读作者发布的文章"),
    LIKE(2, "点赞", "用户给作者的文章点赞"),
    COMMENT(3, "评论", "用户给作者的文章评论"),
    COLLECT(4, "收藏", "用户收藏作者的文章"),
    SHARE(5, "分享", "用户分享作者的文章到外部平台"),
    COMPLETE_READ(6, "完整阅读", "用户滚动至帖子底部，完成阅读"),
    REPLY_COMMENT(7, "回复评论", "用户回复其他用户的评论"),
    FORWARD_IN_SITE(8, "站内转发", "用户将帖子转发至站内动态、圈子或主页"),
    FOLLOW_AUTHOR(9, "关注作者", "用户因阅读本帖而关注作者"),
    ADMIRE(10, "赞赏", "用户免费点击赞赏按钮表达认可"),
    TIP(11, "打赏", "用户使用虚拟币或现金对作者进行打赏"),
    PUBLISH_WORK(12, "发布作品", "用户成功发布一篇新帖子或原创内容");

    private final int id;
    private final String actionName;
    private final String description;

    UserActionEnum(int id, String actionName, String description) {
        this.id = id;
        this.actionName = actionName;
        this.description = description;
    }

    // 根据ID查找枚举
    public static UserActionEnum valueOfId(int id) {
        for (UserActionEnum userAction : values()) {
            if (userAction.getId() == id) {
                return userAction;
            }
        }
        throw new IllegalArgumentException("No matching enum found for id: " + id);
    }

    public static UserActionEnum valueOfActionName(String actionName) {
        for (UserActionEnum userAction : values()) {
            if (userAction.getActionName().equals(actionName)) {
                return userAction;
            }
        }
        throw new IllegalArgumentException("No matching enum found for actionName: " + actionName);
    }

    public int getId() {
        return id;
    }

    public String getActionName() {
        return actionName;
    }

    public String getDescription() {
        return description;
    }
}