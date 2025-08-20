from __future__ import annotations

import yaml
from typing import Any, Dict


def _read_yaml(path: str) -> Dict[str, Any]:
    with open(path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f) or {}


def load_db_config(path: str, *, override_url: str | None = None, override_table: str | None = None,
                   override_mode: str | None = None, override_chunksize: int | None = None) -> Dict[str, Any]:
    cfg = _read_yaml(path)
    if override_url:
        cfg["database_url"] = override_url
    if override_table:
        cfg["table"] = override_table
    if override_mode:
        cfg["mode"] = override_mode
    if override_chunksize is not None:
        cfg["chunksize"] = override_chunksize
    cfg.setdefault("echo_sql", False)
    cfg.setdefault("mode", "append")
    cfg.setdefault("chunksize", 1000)
    return cfg


def load_schema_config(path: str) -> Dict[str, Any]:
    cfg = _read_yaml(path)
    cfg.setdefault("table", None)
    cfg.setdefault("primary_key", [])
    cfg.setdefault("columns", {})
    return cfg


def load_mapping_config(path: str) -> Dict[str, Any]:
    cfg = _read_yaml(path)
    cfg.setdefault("column_synonyms", {})
    cfg.setdefault("defaults", {})
    return cfg

