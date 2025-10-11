package com.zhangzc.bookcommentbiz.Pojo.Domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @TableName t_comment
 */
@TableName(value ="t_comment")
@Data
public class TComment {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的笔记ID
     */
    @TableField(value = "note_id")
    private Long noteId;

    /**
     * 发布者用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 评论内容UUID
     */
    @TableField(value = "content_uuid")
    private String contentUuid;

    /**
     * 内容是否为空(0：不为空 1：为空)
     */
    @TableField(value = "is_content_empty")
    private Boolean isContentEmpty;

    /**
     * 评论附加图片URL
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 级别(1：一级评论 2：二级评论)
     */
    @TableField(value = "level")
    private Integer level;

    /**
     * 评论被回复次数，仅一级评论需要
     */
    @TableField(value = "reply_total")
    private Long replyTotal;

    /**
     * 评论被点赞次数
     */
    @TableField(value = "like_total")
    private Long likeTotal;

    /**
     * 父ID (若是对笔记的评论，则此字段存储笔记ID; 若是二级评论，则此字段存储一级评论的ID)
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 回复哪个的评论 (0表示是对笔记的评论，若是对他人评论的回复，则存储回复评论的ID)
     */
    @TableField(value = "reply_comment_id")
    private Long replyCommentId;

    /**
     * 回复的哪个用户, 存储用户ID
     */
    @TableField(value = "reply_user_id")
    private Long replyUserId;

    /**
     * 是否置顶(0：不置顶 1：置顶)
     */
    @TableField(value = "is_top")
    private Integer isTop;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 二级评论总数（只有一级评论才需要统计）
     */
    @TableField(value = "child_comment_total")
    private Long childCommentTotal;

    /**
     * 评论热度
     */
    @TableField(value = "heat")
    private BigDecimal heat;

    /**
     * 最早回复的评论ID (只有一级评论需要)
     */
    @TableField(value = "first_reply_comment_id")
    private Long firstReplyCommentId;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TComment other = (TComment) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getNoteId() == null ? other.getNoteId() == null : this.getNoteId().equals(other.getNoteId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getContentUuid() == null ? other.getContentUuid() == null : this.getContentUuid().equals(other.getContentUuid()))
            && (this.getIsContentEmpty() == null ? other.getIsContentEmpty() == null : this.getIsContentEmpty().equals(other.getIsContentEmpty()))
            && (this.getImageUrl() == null ? other.getImageUrl() == null : this.getImageUrl().equals(other.getImageUrl()))
            && (this.getLevel() == null ? other.getLevel() == null : this.getLevel().equals(other.getLevel()))
            && (this.getReplyTotal() == null ? other.getReplyTotal() == null : this.getReplyTotal().equals(other.getReplyTotal()))
            && (this.getLikeTotal() == null ? other.getLikeTotal() == null : this.getLikeTotal().equals(other.getLikeTotal()))
            && (this.getParentId() == null ? other.getParentId() == null : this.getParentId().equals(other.getParentId()))
            && (this.getReplyCommentId() == null ? other.getReplyCommentId() == null : this.getReplyCommentId().equals(other.getReplyCommentId()))
            && (this.getReplyUserId() == null ? other.getReplyUserId() == null : this.getReplyUserId().equals(other.getReplyUserId()))
            && (this.getIsTop() == null ? other.getIsTop() == null : this.getIsTop().equals(other.getIsTop()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getChildCommentTotal() == null ? other.getChildCommentTotal() == null : this.getChildCommentTotal().equals(other.getChildCommentTotal()))
            && (this.getHeat() == null ? other.getHeat() == null : this.getHeat().equals(other.getHeat()))
            && (this.getFirstReplyCommentId() == null ? other.getFirstReplyCommentId() == null : this.getFirstReplyCommentId().equals(other.getFirstReplyCommentId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getNoteId() == null) ? 0 : getNoteId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getContentUuid() == null) ? 0 : getContentUuid().hashCode());
        result = prime * result + ((getIsContentEmpty() == null) ? 0 : getIsContentEmpty().hashCode());
        result = prime * result + ((getImageUrl() == null) ? 0 : getImageUrl().hashCode());
        result = prime * result + ((getLevel() == null) ? 0 : getLevel().hashCode());
        result = prime * result + ((getReplyTotal() == null) ? 0 : getReplyTotal().hashCode());
        result = prime * result + ((getLikeTotal() == null) ? 0 : getLikeTotal().hashCode());
        result = prime * result + ((getParentId() == null) ? 0 : getParentId().hashCode());
        result = prime * result + ((getReplyCommentId() == null) ? 0 : getReplyCommentId().hashCode());
        result = prime * result + ((getReplyUserId() == null) ? 0 : getReplyUserId().hashCode());
        result = prime * result + ((getIsTop() == null) ? 0 : getIsTop().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getChildCommentTotal() == null) ? 0 : getChildCommentTotal().hashCode());
        result = prime * result + ((getHeat() == null) ? 0 : getHeat().hashCode());
        result = prime * result + ((getFirstReplyCommentId() == null) ? 0 : getFirstReplyCommentId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", noteId=").append(noteId);
        sb.append(", userId=").append(userId);
        sb.append(", contentUuid=").append(contentUuid);
        sb.append(", isContentEmpty=").append(isContentEmpty);
        sb.append(", imageUrl=").append(imageUrl);
        sb.append(", level=").append(level);
        sb.append(", replyTotal=").append(replyTotal);
        sb.append(", likeTotal=").append(likeTotal);
        sb.append(", parentId=").append(parentId);
        sb.append(", replyCommentId=").append(replyCommentId);
        sb.append(", replyUserId=").append(replyUserId);
        sb.append(", isTop=").append(isTop);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", childCommentTotal=").append(childCommentTotal);
        sb.append(", heat=").append(heat);
        sb.append(", firstReplyCommentId=").append(firstReplyCommentId);
        sb.append("]");
        return sb.toString();
    }
}