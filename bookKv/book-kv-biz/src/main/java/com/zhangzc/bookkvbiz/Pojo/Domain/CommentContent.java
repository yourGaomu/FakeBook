package com.zhangzc.bookkvbiz.Pojo.Domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 存储笔记的评论内容
 * @TableName comment_content
 */
@TableName(value ="comment_content")
@Data
public class CommentContent {
    /**
     * 评论唯一ID，UUID格式
     */
    @TableId(value = "content_id")
    private String contentId;

    /**
     * 笔记ID，关联笔记表
     */
    @TableField(value = "note_id")
    private Long noteId;

    /**
     * 发布年月，格式为YYYY-MM
     */
    @TableField(value = "year_month")
    private String yearMonth;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

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
        CommentContent other = (CommentContent) that;
        return (this.getContentId() == null ? other.getContentId() == null : this.getContentId().equals(other.getContentId()))
            && (this.getNoteId() == null ? other.getNoteId() == null : this.getNoteId().equals(other.getNoteId()))
            && (this.getYearMonth() == null ? other.getYearMonth() == null : this.getYearMonth().equals(other.getYearMonth()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getContentId() == null) ? 0 : getContentId().hashCode());
        result = prime * result + ((getNoteId() == null) ? 0 : getNoteId().hashCode());
        result = prime * result + ((getYearMonth() == null) ? 0 : getYearMonth().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", contentId=").append(contentId);
        sb.append(", noteId=").append(noteId);
        sb.append(", yearMonth=").append(yearMonth);
        sb.append(", content=").append(content);
        sb.append("]");
        return sb.toString();
    }
}