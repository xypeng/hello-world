## Excel 批量导入至同一数据表 - 代码框架

本框架用于将多个字段不一致的 Excel 文件，统一映射并导入到同一个数据库表中。支持：

- 字段名标准化（大小写、去空格、中文等处理）
- 字段同义词映射（通过 YAML 配置）
- 类型转换（string/int/float/bool/date/datetime）
- 缺失字段填充默认值
- 插入/替换（append/replace），可选主键去重（upsert 预留）
- 命令行批处理目录或单文件

### 目录结构

```
etl_excel/
  config/
    db.yaml            # 默认数据库及写入策略
    schema.yaml        # 目标表字段与类型定义
    mappings/
      default.yaml     # 字段同义词与默认值
  src/
    main.py            # 命令行入口
    etl/
      __init__.py
      config.py        # 配置加载
      io.py            # Excel 读取
      transform.py     # 标准化、映射、类型转换
      load.py          # 数据库写入
      utils.py         # 日志与工具
  requirements.txt
```

### 安装依赖

```bash
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
```

默认使用 SQLite，零依赖驱动。若需连接其他数据库（例如 Postgres/MySQL），请在 `db.yaml` 中配置 `database_url` 并安装相应驱动。

### 配置说明

- `config/db.yaml`：数据库连接与写入策略
- `config/schema.yaml`：目标表字段与类型，包含是否主键、默认值
- `config/mappings/default.yaml`：来源字段到目标字段的同义词映射与默认值

### 快速开始

```bash
python src/main.py \
  --input /path/to/excel_or_dir \
  --schema config/schema.yaml \
  --mapping config/mappings/default.yaml \
  --db config/db.yaml \
  --table my_table \
  --mode append \
  --recursive
```

常用参数：

- `--input`：Excel 文件或目录
- `--recursive`：递归扫描目录
- `--db`：数据库配置文件，或使用 `--db-url` 直接传入
- `--schema`：目标表字段定义
- `--mapping`：字段同义词与默认值
- `--table`：目标表名（覆盖配置）
- `--mode`：append|replace（追加或替换表）
- `--dry-run`：只做转换与预览，不写入数据库
- `--preview`：打印预览行数（默认 5）

### 自定义映射

`config/mappings/default.yaml` 支持为目标字段定义多个来源同义词。例如：

```yaml
column_synonyms:
  name: [姓名, 名称, 用户名, name, user_name]
  age: [年龄, 年紀, age]
  created_at: [创建时间, 建立时间, create_time, created_at]

defaults:
  country: CN
```

### 运行示例（只预览不入库）

```bash
python src/main.py --input ./samples --schema config/schema.yaml --mapping config/mappings/default.yaml --db config/db.yaml --table my_table --dry-run --preview 10 --recursive
```

### 注意

- 不同 Excel 的 sheet 名可能不同，默认读取第一个 sheet，可通过 `--sheet` 指定。
- 若开启 `replace`，将替换整张表。
- 如需 upsert（按主键去重更新），可在 `schema.yaml` 声明主键并在 `--mode upsert` 下启用（示例实现预留）。

