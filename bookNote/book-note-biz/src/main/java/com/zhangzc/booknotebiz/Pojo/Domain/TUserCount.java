package com.zhangzc.booknotebiz.Pojo.Domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 用户计数表
 * @TableName t_user_count
 */
@TableName(value ="t_user_count")
@Data
public class TUserCount implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 粉丝总数
     */
    @TableField(value = "fans_total")
    private Long fansTotal;

    /**
     * 关注总数
     */
    @TableField(value = "following_total")
    private Long followingTotal;

    /**
     * 发布笔记总数
     */
    @TableField(value = "note_total")
    private Long noteTotal;

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
        TUserCount other = (TUserCount) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getFansTotal() == null ? other.getFansTotal() == null : this.getFansTotal().equals(other.getFansTotal()))
            && (this.getFollowingTotal() == null ? other.getFollowingTotal() == null : this.getFollowingTotal().equals(other.getFollowingTotal()))
            && (this.getNoteTotal() == null ? other.getNoteTotal() == null : this.getNoteTotal().equals(other.getNoteTotal()))
            && (this.getLikeTotal() == null ? other.getLikeTotal() == null : this.getLikeTotal().equals(other.getLikeTotal()))
            && (this.getCollectTotal() == null ? other.getCollectTotal() == null : this.getCollectTotal().equals(other.getCollectTotal()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getFansTotal() == null) ? 0 : getFansTotal().hashCode());
        result = prime * result + ((getFollowingTotal() == null) ? 0 : getFollowingTotal().hashCode());
        result = prime * result + ((getNoteTotal() == null) ? 0 : getNoteTotal().hashCode());
        result = prime * result + ((getLikeTotal() == null) ? 0 : getLikeTotal().hashCode());
        result = prime * result + ((getCollectTotal() == null) ? 0 : getCollectTotal().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", fansTotal=").append(fansTotal);
        sb.append(", followingTotal=").append(followingTotal);
        sb.append(", noteTotal=").append(noteTotal);
        sb.append(", likeTotal=").append(likeTotal);
        sb.append(", collectTotal=").append(collectTotal);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}