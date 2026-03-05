ALTER TABLE `t_llm_model` 
ADD COLUMN `is_net` INT DEFAULT 0 COMMENT '是否原生支持联网 0-否 1-是' AFTER `updated_at`;

-- 将现有模型标记为需要插件（默认就是0，这里为了演示）
-- UPDATE `t_llm_model` SET `is_net` = 0;

-- 如果你已经知道某些模型支持联网（比如 id=1 的模型），可以手动更新
-- UPDATE `t_llm_model` SET `is_net` = 1 WHERE `id` = 1;
