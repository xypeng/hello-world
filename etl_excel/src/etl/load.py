from __future__ import annotations

from typing import Dict, Any, List
from sqlalchemy import create_engine, text
from datetime import datetime


def _sqlite_type_of(col_type: str) -> str:
    t = (col_type or "string").lower()
    if t in ("int", "integer", "bool", "boolean"):
        return "INTEGER"
    if t in ("float", "double", "number"):
        return "REAL"
    if t in ("date", "datetime"):
        return "TEXT"
    return "TEXT"


def _ensure_table(engine, table_name: str, schema_cfg: Dict[str, Any]):
    # Create table if not exists (SQLite)
    columns = schema_cfg.get("columns", {})
    pk = schema_cfg.get("primary_key", []) or []
    col_defs = []
    for col, cfg in columns.items():
        sql_type = _sqlite_type_of((cfg or {}).get("type", "string"))
        col_defs.append(f"{col} {sql_type}")
    pk_sql = f", PRIMARY KEY ({','.join(pk)})" if pk else ""
    ddl = f"CREATE TABLE IF NOT EXISTS {table_name} (" + ", ".join(col_defs) + pk_sql + ")"
    with engine.begin() as conn:
        conn.execute(text(ddl))


def load_records_to_db(records: List[Dict[str, Any]], *, db_cfg: Dict[str, Any], schema_cfg: Dict[str, Any],
                       table_name: str, logger=None) -> None:
    database_url = db_cfg["database_url"]
    mode = db_cfg.get("mode", "append")
    echo_sql = db_cfg.get("echo_sql", False)

    engine = create_engine(database_url, echo=echo_sql, future=True)

    if mode not in ("append", "replace", "upsert"):
        raise ValueError(f"未知写入模式: {mode}")

    # Only SQLite fully implemented here
    if not database_url.startswith("sqlite:"):
        raise NotImplementedError("当前示例实现仅对 SQLite 提供内置建表与写入。其他数据库请按方言实现。")

    # create table if needed
    _ensure_table(engine, table_name, schema_cfg)

    columns = list(schema_cfg.get("columns", {}).keys())
    # normalize/serialize values
    def serialize(val):
        if isinstance(val, datetime):
            return val.isoformat(sep=" ")
        if isinstance(val, bool):
            return 1 if val else 0
        return val

    if mode == "replace":
        with engine.begin() as conn:
            conn.execute(text(f"DELETE FROM {table_name}"))

    if mode in ("append", "replace"):
        placeholders = ",".join([":" + c for c in columns])
        sql = f"INSERT INTO {table_name} (" + ",".join(columns) + ") VALUES (" + placeholders + ")"
        with engine.begin() as conn:
            for rec in records:
                payload = {c: serialize(rec.get(c)) for c in columns}
                conn.execute(text(sql), payload)
        return

    if mode == "upsert":
        primary_keys: List[str] = schema_cfg.get("primary_key", [])
        if not primary_keys:
            raise ValueError("upsert 模式需要在 schema.primary_key 中指定主键")
        # SQLite: INSERT OR REPLACE
        placeholders = ",".join([":" + c for c in columns])
        sql = f"INSERT OR REPLACE INTO {table_name} (" + ",".join(columns) + ") VALUES (" + placeholders + ")"
        with engine.begin() as conn:
            for rec in records:
                payload = {c: serialize(rec.get(c)) for c in columns}
                conn.execute(text(sql), payload)
        return

