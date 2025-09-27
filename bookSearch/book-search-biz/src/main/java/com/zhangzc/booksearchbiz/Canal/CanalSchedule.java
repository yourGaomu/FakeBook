package com.zhangzc.booksearchbiz.Canal;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.common.collect.Maps;
import com.zhangzc.booksearchbiz.Mapper.Es.SearchNoteMapper;
import com.zhangzc.booksearchbiz.Mapper.Es.SearchUserMapper;
import com.zhangzc.booksearchbiz.Mapper.Mp.SelectMapper;
import com.zhangzc.booksearchbiz.Pojo.CanalResultVo.ValueAndFlagVo;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteRspVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserRspVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private final SearchNoteMapper searchNoteMapper;
    private final SearchUserMapper searchUserMapper;
    private final SelectMapper selectMapper;

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
            case "t_note" -> handleNoteEvent(columnMap, eventType); // 笔记表
            case "t_user" -> handleUserEvent(columnMap, eventType); // 用户表
            case "t_user_count" -> handleUserCountEvent(columnMap, eventType); // 用户计数表
            case "t_note_count" -> handleNoteCountEvent(columnMap, eventType); // 笔记计数表
            default -> log.warn("Table: {} not support", table);
        }
    }

    private void handleNoteCountEvent(Map<String, ValueAndFlagVo> columnMap, CanalEntry.EventType eventType) {
        if (columnMap == null || columnMap.isEmpty()) {
            //删除不用管
            return;
        }
        Long noteId = Long.parseLong(columnMap.get("note_id").getValue().toString());
        //根据不同的消息进行处理
        syncNoteCount2Index(noteId, eventType, columnMap);
    }

    private void syncNoteCount2Index(Long noteId, CanalEntry.EventType eventType, Map<String, ValueAndFlagVo> columnMap) {
        switch (eventType) {
            case INSERT -> {
                //是否存在记录
                boolean flag = exitSearchNoteRspVo(noteId);
                if (flag) {
                    SearchNoteRspVO searchUserRspVoByUserId = getSearchNoteRspVoByNoteId(noteId);
                    Integer insert = searchNoteMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                }
            }
            case UPDATE -> {
                //是否存在记录
                boolean flag = exitSearchNoteRspVo(noteId);
                if (flag) {
                    SearchNoteRspVO searchUserRspVoByUserId = getSearchNoteRspVoByNoteId(noteId);
                    Integer insert = searchNoteMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                } else {
                    //Es中有对应的数据需要进行更新
                    Map<String, Object> searchUserRspVoByUpdateColumn = getSearchNoteRspVoByUpdateColumn(columnMap);
                    SearchNoteRspVO searchNoteRspVO = BeanUtil.copyProperties(searchUserRspVoByUpdateColumn, SearchNoteRspVO.class);
                    searchNoteRspVO.setNoteId(noteId);
                    searchNoteMapper.updateById(searchNoteRspVO);
                }
            }
            default -> log.warn("EventType: {} not support", eventType);
        }

        //有数据存在了不需要操作因为只是添加操作
    }

    private Map<String, Object> getSearchNoteRspVoByUpdateColumn(Map<String, ValueAndFlagVo> columnMap) {
        Map<String, Object> map = Maps.newHashMap();
        columnMap.forEach((key, value) -> {
            if (value.getUpdated()) {
                //将需要更新的字段放入进去
                map.put(key, value.getValue());
            }
        });
        return map;
    }

    private SearchNoteRspVO getSearchNoteRspVoByNoteId(Long noteId) {
        List<Map<String, Object>> maps = selectMapper.selectEsNoteIndexData(noteId);
        Map<String, Object> stringObjectMap = maps.get(0);
        //获取封面中的第一个地址作为封面
        stringObjectMap.put("cover", stringObjectMap.get("img_uris").toString().split(",")[0]);
        stringObjectMap.put("highlightTitle", null);
        return BeanUtil.copyProperties(stringObjectMap, SearchNoteRspVO.class);

    }

    private boolean exitSearchNoteRspVo(Long noteId) {
        LambdaEsQueryWrapper<SearchNoteRspVO> lambdaEsQueryWrapper = new LambdaEsQueryWrapper<>();
        lambdaEsQueryWrapper.eq(SearchNoteRspVO::getNoteId, noteId);
        SearchNoteRspVO searchUserRspVO = searchNoteMapper.selectOne(lambdaEsQueryWrapper);
        return Objects.isNull(searchUserRspVO);
    }

    private void handleUserCountEvent(Map<String, ValueAndFlagVo> columnMap, CanalEntry.EventType eventType) {
        if (columnMap == null || columnMap.isEmpty()) {
            //删除不用管
            return;
        }
        Long userId = Long.parseLong(columnMap.get("user_id").getValue().toString());
        //根据不同的消息进行处理
        syncUserCount2Index(userId, eventType, columnMap);

    }

    private void syncUserCount2Index(Long userId, CanalEntry.EventType eventType, Map<String, ValueAndFlagVo> columnMap) {
        switch (eventType) {
            case INSERT -> {
                //是否存在记录
                boolean flag = exitSearchUserRspVo(userId);
                if (flag) {
                    SearchUserRspVO searchUserRspVoByUserId = getSearchUserRspVoByUserId(userId);
                    Integer insert = searchUserMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                }
            }
            case UPDATE -> {
                //是否存在记录
                boolean flag = exitSearchUserRspVo(userId);
                if (flag) {
                    SearchUserRspVO searchUserRspVoByUserId = getSearchUserRspVoByUserId(userId);
                    Integer insert = searchUserMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                } else {
                    //Es中有对应的数据需要进行更新
                    Map<String, Object> searchUserRspVoByUpdateColumn = getSearchUserRspVoByUpdateColumn(columnMap);
                    SearchUserRspVO searchUserRspVO = BeanUtil.copyProperties(searchUserRspVoByUpdateColumn, SearchUserRspVO.class);
                    searchUserRspVO.setUserId(userId);
                    searchUserMapper.updateById(searchUserRspVO);
                }
            }
            default -> log.warn("EventType: {} not support", eventType);
        }

        //有数据存在了不需要操作因为只是添加操作
    }

    private Map<String, Object> getSearchUserRspVoByUpdateColumn(Map<String, ValueAndFlagVo> columnMap) {
        Map<String, Object> map = Maps.newHashMap();
        columnMap.forEach((key, value) -> {
            if (value.getUpdated()) {
                //将需要更新的字段放入进去
                map.put(key, value.getValue());
            }
        });
        return map;
    }

    private boolean exitSearchUserRspVo(Long userId) {
        LambdaEsQueryWrapper<SearchUserRspVO> lambdaEsQueryWrapper = new LambdaEsQueryWrapper();
        lambdaEsQueryWrapper.eq(SearchUserRspVO::getUserId, userId);
        SearchUserRspVO searchUserRspVO = searchUserMapper.selectOne(lambdaEsQueryWrapper);
        return Objects.isNull(searchUserRspVO);
    }

    private SearchUserRspVO getSearchUserRspVoByUserId(Long userId) {
        SearchUserRspVO searchUserRspVO = new SearchUserRspVO();
        searchUserRspVO.setUserId(userId);
        //查询用户信息
        List<Map<String, Object>> maps = selectMapper.selectEsUserIndexData(userId);
        Map<String, Object> stringObjectMap = maps.get(0);
        //开始赋值
        searchUserRspVO.setAvatar(stringObjectMap.get("avatar").toString());
        searchUserRspVO.setFansTotal(Integer.parseInt(stringObjectMap.get("fans_total").toString()));
        searchUserRspVO.setNoteTotal(Integer.parseInt(stringObjectMap.get("note_total").toString()));
        searchUserRspVO.setXiaohashuId(stringObjectMap.get("xiaohashu_id").toString());
        searchUserRspVO.setNickname(stringObjectMap.get("nickname").toString());
        return searchUserRspVO;
    }

    /**
     * 处理笔记表事件
     *
     * @param columnMap
     * @param eventType
     */
    private void handleNoteEvent(Map<String, ValueAndFlagVo> columnMap, CanalEntry.EventType eventType) throws Exception {
        if (columnMap == null || columnMap.isEmpty()) {
            //删除不用管
            return;
        }
        Long noteId = Long.parseLong(columnMap.get("id").getValue().toString());
        //根据不同的消息进行处理
        syncNote2Index(noteId, eventType, columnMap);
    }

    private void syncNote2Index(Long noteId, CanalEntry.EventType eventType, Map<String, ValueAndFlagVo> columnMap) {
        switch (eventType) {
            case INSERT -> {
                //是否存在记录
                boolean flag = exitSearchNoteRspVo(noteId);
                if (flag) {
                    SearchNoteRspVO searchUserRspVoByUserId = getSearchNoteRspVoByNoteId(noteId);
                    Integer insert = searchNoteMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                }
            }
            case UPDATE -> {
                //是否存在记录
                boolean flag = exitSearchNoteRspVo(noteId);
                if (flag) {
                    SearchNoteRspVO searchUserRspVoByUserId = getSearchNoteRspVoByNoteId(noteId);
                    Integer insert = searchNoteMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                } else {
                    //Es中有对应的数据需要进行更新
                    Map<String, Object> searchUserRspVoByUpdateColumn = getSearchNoteRspVoByUpdateColumn(columnMap);
                    SearchNoteRspVO searchNoteRspVO = BeanUtil.copyProperties(searchUserRspVoByUpdateColumn, SearchNoteRspVO.class);
                    searchNoteRspVO.setNoteId(noteId);
                    searchNoteMapper.updateById(searchNoteRspVO);
                }
            }
            default -> log.warn("EventType: {} not support", eventType);
        }

        //有数据存在了不需要操作因为只是添加操作
    }

    /**
     * 处理用户表事件
     *
     * @param columnMap
     * @param eventType
     */
    private void handleUserEvent(Map<String, ValueAndFlagVo> columnMap, CanalEntry.EventType eventType) throws Exception {
        if (columnMap == null || columnMap.isEmpty()) {
            //删除不用管
            return;
        }
        Long userId = Long.parseLong(columnMap.get("id").getValue().toString());
        //根据不同的消息进行处理
        syncUser2Index(userId, eventType, columnMap);
    }

    private void syncUser2Index(Long userId, CanalEntry.EventType eventType, Map<String, ValueAndFlagVo> columnMap) {
        switch (eventType) {
            case INSERT -> {
                //是否存在记录
                boolean flag = exitSearchUserRspVo(userId);
                if (flag) {
                    SearchUserRspVO searchUserRspVoByUserId = getSearchUserRspVoByUserId(userId);
                    Integer insert = searchUserMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                }
            }
            case UPDATE -> {
                //是否存在记录
                boolean flag = exitSearchUserRspVo(userId);
                if (flag) {
                    SearchUserRspVO searchUserRspVoByUserId = getSearchUserRspVoByUserId(userId);
                    Integer insert = searchUserMapper.insert(searchUserRspVoByUserId);
                    System.out.println(insert + "条数据已经加载进去了");
                } else {
                    //Es中有对应的数据需要进行更新
                    Map<String, Object> searchUserRspVoByUpdateColumn = getSearchUserRspVoByUpdateColumn(columnMap);
                    SearchUserRspVO searchUserRspVO = BeanUtil.copyProperties(searchUserRspVoByUpdateColumn, SearchUserRspVO.class);
                    searchUserRspVO.setUserId(userId);
                    searchUserMapper.updateById(searchUserRspVO);
                }
            }
            default -> log.warn("EventType: {} not support", eventType);
        }
    }

}

