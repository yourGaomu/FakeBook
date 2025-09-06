package com.zhangzc.bookcountbiz.Domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 笔记计数表
 * @TableName t_note_count
 */
@TableName(value ="t_note_count")
@Data
public class TNoteCount implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    @TableField(value = "note_id")
    private Long noteId;

    /**
     * 获得点赞总数
     */
    @TableField(value = "like_total")
    private Long likeTotal;

    /**
     * 获得收藏总数
     */
    @TableField(value = "collect_total")
    private Long collectTotal;

    /**
     * 被评论总数
     */
    @TableField(value = "comment_total")
    private Long commentTotal;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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
        TNoteCount other = (TNoteCount) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getNoteId() == null ? other.getNoteId() == null : this.getNoteId().equals(other.getNoteId()))
            && (this.getLikeTotal() == null ? other.getLikeTotal() == null : this.getLikeTotal().equals(other.getLikeTotal()))
            && (this.getCollectTotal() == null ? other.getCollectTotal() == null : this.getCollectTotal().equals(other.getCollectTotal()))
            && (this.getCommentTotal() == null ? other.getCommentTotal() == null : this.getCommentTotal().equals(other.getCommentTotal()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getNoteId() == null) ? 0 : getNoteId().hashCode());
        result = prime * result + ((getLikeTotal() == null) ? 0 : getLikeTotal().hashCode());
        result = prime * result + ((getCollectTotal() == null) ? 0 : getCollectTotal().hashCode());
        result = prime * result + ((getCommentTotal() == null) ? 0 : getCommentTotal().hashCode());
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
        sb.append(", likeTotal=").append(likeTotal);
        sb.append(", collectTotal=").append(collectTotal);
        sb.append(", commentTotal=").append(commentTotal);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}