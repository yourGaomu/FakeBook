package com.zhangzc.blog.blogai.Tools;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlTool {


    // 白名单表 (请根据实际情况调整)
    private static final Set<String> ALLOWED_TABLES = Set.of(
            "search_history",
            "t_channel",
            "t_channel_topic_rel",
            "t_fans",
            "t_following",
            "t_note",
            "t_note_count",
            "t_note_like",
            "t_note_collection",
            "t_note_comment",
            "t_note_content",
            "t_topic",
            "t_user_count",
            "t_user_role_rel",
            "t_role"
    );

    @Tool("执行 SQL 查询语句并返回结果。注意：只能执行 SELECT 查询，且仅限于查询业务数据表。")
    public List<Map<String, Object>> executeQuery(@P("需要执行的 SQL 查询语句") String sql) {


        log.info("LLM Request to execute SQL: {}", sql);

        // 1. 基础校验
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL 不能为空");
        }

        // 2. JSqlParser 解析与校验
        try {
            // 解析 SQL
            Statement statement = CCJSqlParserUtil.parse(sql);

            // 2.1 校验是否为 SELECT 语句
            if (!(statement instanceof Select)) {
                throw new IllegalArgumentException("安全警告：只允许执行 SELECT 查询语句，禁止 " + statement.getClass().getSimpleName() + " 操作");
            }

            // 2.2 提取表名并校验白名单
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(statement);

            for (String tableName : tableList) {
                // 移除可能的引号 (比如 MySQL 的 `t_article`)
                String cleanTableName = tableName.
                        replace("`", "")
                        .replace("\"", "")
                        .replace("'", "");

                // 忽略大小写比较
                if (!ALLOWED_TABLES.contains(cleanTableName.toLowerCase())) {
                    throw new IllegalArgumentException("安全警告：访问了未授权的表: " + cleanTableName);
                }
            }

        } catch (JSQLParserException e) {
            log.error("SQL Parse Error", e);
            throw new RuntimeException("SQL 解析失败，请检查语法: " + e.getMessage());
        }

        try {
            // 3. 执行查询
            // 简单防范：如果 SQL 没有 LIMIT，强行加上 LIMIT 20 防止数据量过大
            String finalSql = sql;
//            if (!finalSql.toUpperCase().contains("LIMIT")) {
//                finalSql += " LIMIT 20";
//            }

            return SqlRunner.db().selectList(finalSql);
        } catch (Exception e) {
            log.error("SQL Execution Error", e);
            throw new RuntimeException("SQL 执行失败: " + e.getMessage());
        }
    }

    @Tool("获取数据库中指定表的结构定义（DDL），用于编写正确的 SQL")
    public String getTableSchema(@P("表名，例如 t_article") String tableName) {
        if (tableName == null) return "Table name cannot be null";

        // 1. Clean and validate table name
        String cleanName = tableName.replace("`", "").replace("\"", "").replace("'", "").toLowerCase();

        if (!ALLOWED_TABLES.contains(cleanName)) {
            return "Unauthorized table: " + cleanName + ". Available tables: " + String.join(", ", ALLOWED_TABLES);
        }

        try {
            // 2. Execute SHOW CREATE TABLE
            String sql = "SHOW CREATE TABLE " + cleanName;
            List<Map<String, Object>> result = SqlRunner.db().selectList(sql);

            if (result != null && !result.isEmpty()) {
                Map<String, Object> row = result.get(0);
                if (row.containsKey("Create Table")) {
                    return (String) row.get("Create Table");
                }
                return row.values().stream().skip(1).findFirst().map(Object::toString).orElse("Schema not found");
            }
            return "Table not found: " + cleanName;
        } catch (Exception e) {
            log.error("Failed to get table schema for {}", cleanName, e);
            return "Error retrieving schema: " + e.getMessage();
        }
    }


    @Tool("获取有哪些数据库存在本项目之中")
    public Set<String> getTableNames() {
        return ALLOWED_TABLES;
    }
}
