-- Project管理小助手数据库初始化脚本
-- 作者: timelordtty

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    review_period INT,
    online_date DATE,
    registration_period INT,
    registration_end_date DATE,
    earliest_review_date DATE,
    expected_review_time TIMESTAMP,
    expert_review_time TIMESTAMP,
    remark VARCHAR(1000)
);

-- 文本替换规则表
CREATE TABLE IF NOT EXISTS text_replacement_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_text VARCHAR(255) NOT NULL,
    replacement_text VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文档模板表
CREATE TABLE IF NOT EXISTS document_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- WORD, EXCEL
    file_path VARCHAR(500) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 字段定义表
CREATE TABLE IF NOT EXISTS field_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT,
    field_name VARCHAR(255) NOT NULL,
    field_type VARCHAR(50) NOT NULL, -- TEXT, NUMBER, DATE, etc.
    default_value VARCHAR(500),
    is_list BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (template_id) REFERENCES document_template(id) ON DELETE CASCADE
);

-- 初始化部分替换规则
INSERT INTO text_replacement_rule (original_text, replacement_text) VALUES
('我很你', '我爱你'),
('好对你', '好喜欢你'),
('烦死了', '真开心'); 