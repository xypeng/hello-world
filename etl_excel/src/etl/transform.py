from __future__ import annotations

from typing import Dict, Any, List
import re
from datetime import datetime, date
from dateutil import parser as date_parser


def normalize_column_name(name: str) -> str:
    if name is None:
        return ""
    s = str(name).strip()
    s = re.sub(r"\s+", " ", s)
    return s


def build_source_to_target_map(mapping_cfg: Dict[str, Any]) -> Dict[str, str]:
    synonyms = mapping_cfg.get("column_synonyms", {})
    source_to_target: Dict[str, str] = {}
    for target, alts in synonyms.items():
        for alias in alts or []:
            source_to_target[normalize_column_name(alias).lower()] = target
        # include target itself
        source_to_target[normalize_column_name(target).lower()] = target
    return source_to_target


def cast_value(value: Any, target_type: str) -> Any:
    t = (target_type or "string").lower()
    if value is None or (isinstance(value, str) and value.strip() == ""):
        return None
    try:
        if t in ("string", "str", "text"):
            return str(value)
        if t in ("int", "integer"):
            return int(float(value))
        if t in ("float", "double", "number"):
            return float(value)
        if t in ("bool", "boolean"):
            if isinstance(value, bool):
                return value
            s = str(value).strip().lower()
            return s in ("1", "true", "yes", "y", "t")
        if t in ("date", "datetime"):
            if isinstance(value, (datetime, date)):
                dt = datetime(value.year, value.month, value.day) if isinstance(value, date) and not isinstance(value, datetime) else value
            else:
                dt = date_parser.parse(str(value))
            return dt
    except Exception:
        return None
    return value


def transform_single_records(records: List[Dict[str, Any]], *, schema_cfg: Dict[str, Any], mapping_cfg: Dict[str, Any]) -> List[Dict[str, Any]]:
    lookup = build_source_to_target_map(mapping_cfg)
    target_columns = list(schema_cfg.get("columns", {}).keys())
    defaults = {**(schema_cfg.get("defaults", {}) or {}), **(mapping_cfg.get("defaults", {}) or {})}

    transformed: List[Dict[str, Any]] = []
    for rec in records:
        out: Dict[str, Any] = {col: None for col in target_columns}
        # map fields
        for src_key, value in rec.items():
            key = normalize_column_name(src_key).lower()
            tgt = lookup.get(key)
            if tgt in out:
                out[tgt] = value
        # defaults
        for col, default_val in defaults.items():
            if out.get(col) in (None, ""):
                out[col] = default_val
        # cast per schema
        for col, col_cfg in (schema_cfg.get("columns", {}) or {}).items():
            if isinstance(col_cfg, dict) and "type" in col_cfg:
                out[col] = cast_value(out.get(col), col_cfg["type"])
        transformed.append(out)
    return transformed


def transform_files_to_records(files: List[str], *, sheet_name: str | None,
                               schema_cfg: Dict[str, Any], mapping_cfg: Dict[str, Any], logger=None) -> List[Dict[str, Any]]:
    from .io import read_excel_rows
    combined: List[Dict[str, Any]] = []
    for f in files:
        try:
            raw_records = read_excel_rows(f, sheet_name=sheet_name)
            tf_records = transform_single_records(raw_records, schema_cfg=schema_cfg, mapping_cfg=mapping_cfg)
            combined.extend(tf_records)
        except Exception as exc:
            if logger:
                logger.warning(f"读取或转换失败: {f}: {exc}")
            else:
                print(f"读取或转换失败: {f}: {exc}")
    return combined

