package com.zhangzc.bookcommentbiz.Canal;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.common.collect.Maps;
import com.zhangzc.bookcommentbiz.Pojo.Domain.TComment;
import com.zhangzc.bookcommentbiz.Pojo.Vo.ValueAndFlagVo;
import com.zhangzc.bookcommentbiz.Service.CommentService;
import com.zhangzc.bookcommentbiz.Service.TCommentService;
import com.zhangzc.bookcommentbiz.Utils.HeatCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class CanalSchedule implements Runnable {


    private final CanalProperties canalProperties;
    private final CanalConnector canalConnector;
    private final TCommentService tCommentService;

    @Override
    @Scheduled(fixedDelay = 100) // 每隔 100ms 被执行一次
    public void run() {
        // 初始化批次 ID，-1 表示未开始或未获取到数据
        long batchId = -1;
        try {
            // 从 canalConnector 获取批量消息，返回的数据量由 batchSize 控制，若不足，则拉取已有的
            Message message = canalConnector.getWithoutAck(canalProperties.getBatchSize());

            // 获取当前拉取消息的批次 ID
            batchId = message.getId();

            // 获取当前批次中的数据条数
            long size = message.getEntries().size();
            if (batchId == -1 || size == 0) {
                try {
                    // 拉取数据为空，休眠 1s, 防止频繁拉取
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
            } else {
                // 如果当前批次有数据，处理这批次数据
                processEntry(message.getEntries());
            }

            // 对当前批次的消息进行 ack 确认，表示该批次的数据已经被成功消费
            canalConnector.ack(batchId);
        } catch (Exception e) {
            log.error("消费 Canal 批次数据异常", e);
            // 如果出现异常，需要进行数据回滚，以便重新消费这批次的数据
            canalConnector.rollback(batchId);
        }
    }

    /**
     * 处理这一批次数据
     *
     * @param entrys
     */
    private void processEntry(List<CanalEntry.Entry> entrys) throws Exception {
        // 循环处理批次数据
        for (CanalEntry.Entry entry : entrys) {
            // 只处理 ROWDATA 行数据类型的 Entry，忽略事务等其他类型
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                // 获取事件类型（如：INSERT、UPDATE、DELETE 等等）
                CanalEntry.EventType eventType = entry.getHeader().getEventType();
                // 获取数据库名称
                String database = entry.getHeader().getSchemaName();
                // 获取表名称
                String table = entry.getHeader().getTableName();

                // 解析出 RowChange 对象，包含 RowData 和事件相关信息
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                // 遍历所有行数据（RowData）
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    // 获取行中所有列的最新值（AfterColumns）
                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();

                    // 将列数据解析为 Map，方便后续处理
                    Map<String, ValueAndFlagVo> columnMap = parseColumns2Map(columns);

                    log.info("EventType: {}, Database: {}, Table: {}, Columns: {}", eventType, database, table, columnMap);

                    // 处理事件
                    processEvent(columnMap, table, eventType);
                }
            }
        }
    }


    /**
     * 将列数据解析为 Map
     *
     * @param columns
     * @return
     */
    private Map<String, ValueAndFlagVo> parseColumns2Map(List<CanalEntry.Column> columns) {
        Map<String, ValueAndFlagVo> map = Maps.newHashMap();
        columns.forEach(column -> {
            if (Objects.isNull(column)) return;
            map.put(column.getName(), ValueAndFlagVo
                    .builder()
                    .value(column.getValue())
                    .updated(column.getUpdated())
                    .build());
        });
        return map;
    }

    /**
     * 处理事件
     *
     * @param columnMap
     * @param table
     * @param eventType
     */
    private void processEvent(Map<String, ValueAndFlagVo> columnMap, String table, CanalEntry.EventType eventType) throws Exception {
        switch (table) {
            //评论表被点赞
            case "t_comment_like" -> handleCommentEvent(columnMap, eventType);//评论表
            //评论表被回复
            case "t_comment" -> handleCommentEventTwo(columnMap, eventType);
            default -> log.warn("Table: {} not support", table);
        }
    }

    private void handleCommentEventTwo(Map<String, ValueAndFlagVo> columnMap, CanalEntry.EventType eventType) {
    }

    private void handleCommentEvent(Map<String, ValueAndFlagVo> columnMap, CanalEntry.EventType eventType) {
        //当该表进行更新操作时，更新其热力值
        switch (eventType) {
            case INSERT -> {
                //点赞表
                //更新热力值
                Long commentId = Long.parseLong(columnMap.get("comment_id").getValue().toString());
                //查询这个评论的一级评论
                Long firstCommentId = getFirstComment(commentId);
                if(firstCommentId != null){
                    //查询这个一级评论
                    TComment one = tCommentService.lambdaQuery().eq(TComment::getId, firstCommentId).one();
                    //获取回复数
                    Long replyTotal = one.getReplyTotal();
                    //获取点赞数
                    Long likeTotal = one.getLikeTotal();
                    //计算热力值
                    BigDecimal heat = HeatCalculator.calculateHeat(likeTotal, replyTotal);
                    //更新热力值
                    one.setHeat(heat);
                    //更新点赞数
                    one.setLikeTotal(likeTotal + 1);
                    tCommentService.updateById(one);
                }
            }
        }
    }

    private Long getFirstComment(Long commentId) {
        TComment one = tCommentService.lambdaQuery().eq(TComment::getId, commentId).one();
        Long parentId = one.getParentId();
        boolean isCommentParentId = parentId.toString().matches("^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$")
                || parentId.toString().matches("^[0-9a-fA-F]{32}$");
        if (isCommentParentId) {
            return parentId;
        }
        return null;
    }

}

