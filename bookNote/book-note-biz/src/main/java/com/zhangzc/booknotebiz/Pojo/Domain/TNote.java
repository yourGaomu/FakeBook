package com.zhangzc.booknotebiz.Pojo.Domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 笔记表
 * @TableName t_note
 */
@TableName(value ="t_note")
@Data
@Builder
public class TNote implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 内容是否为空(0：不为空 1：空)
     */
    @TableField(value = "is_content_empty")
    private Boolean isContentEmpty;

    /**
     * 发布者ID
     */
    @TableField(value = "creator_id")
    private Long creatorId;

    /**
     * 话题ID
     */
    @TableField(value = "topic_id")
    private Long topicId;

    /**
     * 话题名称
     */
    @TableField(value = "topic_name")
    private String topicName;

    /**
     * 是否置顶(0：未置顶 1：置顶)
     */
    @TableField(value = "is_top")
    private Boolean isTop;

    /**
     * 类型(0：图文 1：视频)
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 笔记图片链接(逗号隔开)
     */
    @TableField(value = "img_uris")
    private String imgUris;

    /**
     * 视频链接
     */
    @TableField(value = "video_uri")
    private String videoUri;

    /**
     * 可见范围(0：公开,所有人可见 1：仅对自己可见)
     */
    @TableField(value = "visible")
    private Integer visible;

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
     * 状态(0：待审核 1：正常展示 2：被删除(逻辑删除) 3：被下架)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 笔记内容UUID
     */
    @TableField(value = "content_uuid")
    private String contentUuid;

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
        TNote other = (TNote) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getIsContentEmpty() == null ? other.getIsContentEmpty() == null : this.getIsContentEmpty().equals(other.getIsContentEmpty()))
            && (this.getCreatorId() == null ? other.getCreatorId() == null : this.getCreatorId().equals(other.getCreatorId()))
            && (this.getTopicId() == null ? other.getTopicId() == null : this.getTopicId().equals(other.getTopicId()))
            && (this.getTopicName() == null ? other.getTopicName() == null : this.getTopicName().equals(other.getTopicName()))
            && (this.getIsTop() == null ? other.getIsTop() == null : this.getIsTop().equals(other.getIsTop()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getImgUris() == null ? other.getImgUris() == null : this.getImgUris().equals(other.getImgUris()))
            && (this.getVideoUri() == null ? other.getVideoUri() == null : this.getVideoUri().equals(other.getVideoUri()))
            && (this.getVisible() == null ? other.getVisible() == null : this.getVisible().equals(other.getVisible()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getContentUuid() == null ? other.getContentUuid() == null : this.getContentUuid().equals(other.getContentUuid()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getIsContentEmpty() == null) ? 0 : getIsContentEmpty().hashCode());
        result = prime * result + ((getCreatorId() == null) ? 0 : getCreatorId().hashCode());
        result = prime * result + ((getTopicId() == null) ? 0 : getTopicId().hashCode());
        result = prime * result + ((getTopicName() == null) ? 0 : getTopicName().hashCode());
        result = prime * result + ((getIsTop() == null) ? 0 : getIsTop().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getImgUris() == null) ? 0 : getImgUris().hashCode());
        result = prime * result + ((getVideoUri() == null) ? 0 : getVideoUri().hashCode());
        result = prime * result + ((getVisible() == null) ? 0 : getVisible().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getContentUuid() == null) ? 0 : getContentUuid().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", title=").append(title);
        sb.append(", isContentEmpty=").append(isContentEmpty);
        sb.append(", creatorId=").append(creatorId);
        sb.append(", topicId=").append(topicId);
        sb.append(", topicName=").append(topicName);
        sb.append(", isTop=").append(isTop);
        sb.append(", type=").append(type);
        sb.append(", imgUris=").append(imgUris);
        sb.append(", videoUri=").append(videoUri);
        sb.append(", visible=").append(visible);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", status=").append(status);
        sb.append(", contentUuid=").append(contentUuid);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}