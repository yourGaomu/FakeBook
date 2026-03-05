package com.zhangzc.kafkaspringbootstart.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Kafka发送/消费记录存储表
 * @TableName kafka_record
 */
@TableName(value ="kafka_record")
@Data
public class KafkaRecord implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Kafka消息唯一标识（UUID）
     */
    @TableField(value = "message_id")
    private String messageId;

    /**
     * Kafka主题名称
     */
    @TableField(value = "topic")
    private String topic;

    /**
     * Kafka分区号
     */
    @TableField(value = "partition")
    private Integer partition;

    /**
     * Kafka偏移量
     */
    @TableField(value = "offset")
    private Long offset;

    /**
     * 消息内容（支持长文本）
     */
    @TableField(value = "content")
    private String content;

    /**
     * 记录类型：SEND-发送，CONSUME-消费
     */
    @TableField(value = "type")
    private String type;

    /**
     * 状态：SUCCESS-成功，FAIL-失败
     */
    @TableField(value = "status")
    private String status;

    /**
     * 记录创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

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
        KafkaRecord other = (KafkaRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMessageId() == null ? other.getMessageId() == null : this.getMessageId().equals(other.getMessageId()))
            && (this.getTopic() == null ? other.getTopic() == null : this.getTopic().equals(other.getTopic()))
            && (this.getPartition() == null ? other.getPartition() == null : this.getPartition().equals(other.getPartition()))
            && (this.getOffset() == null ? other.getOffset() == null : this.getOffset().equals(other.getOffset()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMessageId() == null) ? 0 : getMessageId().hashCode());
        result = prime * result + ((getTopic() == null) ? 0 : getTopic().hashCode());
        result = prime * result + ((getPartition() == null) ? 0 : getPartition().hashCode());
        result = prime * result + ((getOffset() == null) ? 0 : getOffset().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", messageId=").append(messageId);
        sb.append(", topic=").append(topic);
        sb.append(", partition=").append(partition);
        sb.append(", offset=").append(offset);
        sb.append(", content=").append(content);
        sb.append(", type=").append(type);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}