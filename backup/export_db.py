#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
数据库导出脚本
导出 code_platform 数据库的所有表结构和数据
"""

import pymysql
import os
from datetime import datetime

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'quanyu',
    'password': '123456',
    'database': 'code_platform',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

def export_database():
    """导出数据库"""
    # 生成文件名
    date_str = datetime.now().strftime('%Y-%m-%d')
    timestamp = datetime.now().strftime('%Y-%m-%d_%H-%M-%S')
    output_dir = '.'
    filename = f'db-{date_str}.sql'
    filepath = os.path.join(output_dir, filename)
    
    # 确保目录存在
    os.makedirs(output_dir, exist_ok=True)
    
    print(f'开始导出数据库 code_platform...')
    print(f'导出文件：{filepath}')
    
    try:
        # 连接数据库
        connection = pymysql.connect(**DB_CONFIG)
        cursor = connection.cursor()
        
        # 获取所有表
        cursor.execute("SHOW TABLES")
        tables = [table[f'Tables_in_{DB_CONFIG["database"]}'] for table in cursor.fetchall()]
        
        print(f'找到 {len(tables)} 张表')
        
        # 构建 SQL 内容
        sql_content = []
        sql_content.append(f'-- 数据库导出脚本')
        sql_content.append(f'-- 数据库：code_platform')
        sql_content.append(f'-- 导出时间：{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}')
        sql_content.append(f'-- 导出工具：Python + PyMySQL')
        sql_content.append('')
        sql_content.append('SET NAMES utf8mb4;')
        sql_content.append('SET FOREIGN_KEY_CHECKS = 0;')
        sql_content.append('')
        sql_content.append('USE `code_platform`;')
        sql_content.append('')
        
        for table in tables:
            print(f'  导出表：{table}')
            
            # 导出表结构
            cursor.execute(f'SHOW CREATE TABLE `{table}`')
            create_stmt = cursor.fetchone()
            sql_content.append(f'-- ----------------------------')
            sql_content.append(f'-- 表结构：{table}')
            sql_content.append(f'-- ----------------------------')
            sql_content.append(f'DROP TABLE IF EXISTS `{table}`;')
            sql_content.append(f'{create_stmt["Create Table"]};')
            sql_content.append('')
            
            # 导出表数据
            cursor.execute(f'SELECT * FROM `{table}`')
            rows = cursor.fetchall()
            
            if rows:
                sql_content.append(f'-- ----------------------------')
                sql_content.append(f'-- 表数据：{table} (共 {len(rows)} 条)')
                sql_content.append(f'-- ----------------------------')
                
                # 获取列名
                columns = list(rows[0].keys())
                
                for row in rows:
                    values = []
                    for col in columns:
                        value = row[col]
                        if value is None:
                            values.append('NULL')
                        elif isinstance(value, (int, float)):
                            values.append(str(value))
                        elif isinstance(value, datetime):
                            values.append(f"'{value.strftime('%Y-%m-%d %H:%M:%S')}'")
                        else:
                            # 转义字符串
                            escaped = str(value).replace("'", "''").replace("\\", "\\\\")
                            values.append(f"'{escaped}'")
                    
                    sql_content.append(f'INSERT INTO `{table}` (`{",`".join(columns)}`) VALUES ({", ".join(values)});')
                
                sql_content.append('')
        
        sql_content.append('SET FOREIGN_KEY_CHECKS = 1;')
        sql_content.append('')
        sql_content.append('-- 导出完成')
        
        # 写入文件
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(sql_content))
        
        print(f'')
        print(f'✓ 导出完成！')
        print(f'  文件：{filepath}')
        print(f'  大小：{os.path.getsize(filepath) / 1024:.2f} KB')
        
        # 同时创建一个带时间戳的备份
        timestamp_filename = f'db-{timestamp}.sql'
        timestamp_filepath = os.path.join(output_dir, timestamp_filename)
        with open(timestamp_filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(sql_content))
        print(f'  时间戳备份：{timestamp_filepath}')
        
        cursor.close()
        connection.close()
        
    except pymysql.Error as e:
        print(f'✗ 数据库错误：{e}')
        raise
    except Exception as e:
        print(f'✗ 导出失败：{e}')
        raise

if __name__ == '__main__':
    export_database()
