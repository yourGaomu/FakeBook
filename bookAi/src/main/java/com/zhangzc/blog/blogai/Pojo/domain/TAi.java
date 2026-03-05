package com.zhangzc.blog.blogai.Pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI对话用户表
 * @TableName t_ai
 */
@TableName(value ="t_ai")
@Data
public class TAi implements Serializable {
    /**
     * 主键，自增，从1开始，唯一
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户QQ号，唯一
     */
    @TableField(value = "qq")
    private String qq;

    /**
     * 权限，0=游客，1=认证成功，默认0
     */
    @TableField(value = "role")
    private Integer role;

    /**
     * 对话次数，默认100，非负
     */
    @TableField(value = "chat_count")
    private Integer chatCount;

    /**
     * 是否禁止，0=不禁止，1=被禁止，默认0
     */
    @TableField(value = "is_banned")
    private Integer isBanned;

    /**
     * 用户最近一次请求的IP地址，支持IPv4和IPv6
     */
    @TableField(value = "ip_address")
    private String ipAddress;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

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
        TAi other = (TAi) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getQq() == null ? other.getQq() == null : this.getQq().equals(other.getQq()))
            && (this.getRole() == null ? other.getRole() == null : this.getRole().equals(other.getRole()))
            && (this.getChatCount() == null ? other.getChatCount() == null : this.getChatCount().equals(other.getChatCount()))
            && (this.getIsBanned() == null ? other.getIsBanned() == null : this.getIsBanned().equals(other.getIsBanned()))
            && (this.getIpAddress() == null ? other.getIpAddress() == null : this.getIpAddress().equals(other.getIpAddress()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getQq() == null) ? 0 : getQq().hashCode());
        result = prime * result + ((getRole() == null) ? 0 : getRole().hashCode());
        result = prime * result + ((getChatCount() == null) ? 0 : getChatCount().hashCode());
        result = prime * result + ((getIsBanned() == null) ? 0 : getIsBanned().hashCode());
        result = prime * result + ((getIpAddress() == null) ? 0 : getIpAddress().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", qq=").append(qq);
        sb.append(", role=").append(role);
        sb.append(", chatCount=").append(chatCount);
        sb.append(", isBanned=").append(isBanned);
        sb.append(", ipAddress=").append(ipAddress);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}