import sys
import os
import click
from etl.config import load_db_config, load_schema_config, load_mapping_config
from etl.io import discover_excel_files
from etl.transform import transform_files_to_records
from etl.load import load_records_to_db
from etl.utils import get_logger


@click.command(context_settings={"help_option_names": ["-h", "--help"]})
@click.option("--input", "input_path", required=True, type=click.Path(exists=True, path_type=str), help="Excel 文件或目录")
@click.option("--recursive", is_flag=True, help="递归扫描目录")
@click.option("--sheet", default=None, help="读取的 sheet 名称（默认第一个）")
@click.option("--db", "db_cfg_path", required=True, type=click.Path(exists=True, path_type=str), help="数据库配置 YAML")
@click.option("--db-url", default=None, help="直接传入数据库 URL，覆盖配置文件")
@click.option("--schema", "schema_path", required=True, type=click.Path(exists=True, path_type=str), help="目标表 schema 配置 YAML")
@click.option("--mapping", "mapping_path", required=True, type=click.Path(exists=True, path_type=str), help="字段映射配置 YAML")
@click.option("--table", default=None, help="目标表名（覆盖配置）")
@click.option("--mode", type=click.Choice(["append", "replace", "upsert"]), default=None, help="写入模式")
@click.option("--chunksize", type=int, default=None, help="批量写入 chunk 大小")
@click.option("--dry-run", is_flag=True, help="只做转换与预览，不写库")
@click.option("--preview", type=int, default=5, help="预览前 N 行")
def cli(input_path: str, recursive: bool, sheet: str | None, db_cfg_path: str, db_url: str | None,
        schema_path: str, mapping_path: str, table: str | None, mode: str | None, chunksize: int | None,
        dry_run: bool, preview: int):
    logger = get_logger()

    db_cfg = load_db_config(db_cfg_path, override_url=db_url, override_table=table, override_mode=mode, override_chunksize=chunksize)
    schema_cfg = load_schema_config(schema_path)
    mapping_cfg = load_mapping_config(mapping_path)

    target_table = table or schema_cfg["table"] or db_cfg["table"]
    if not target_table:
        raise click.UsageError("必须提供目标表名 --table 或在配置中声明 table")

    files = discover_excel_files(input_path, recursive=recursive)
    if not files:
        click.echo("未发现 Excel 文件")
        sys.exit(1)

    records = transform_files_to_records(files, sheet_name=sheet, schema_cfg=schema_cfg, mapping_cfg=mapping_cfg, logger=logger)

    click.echo(f"发现文件数: {len(files)}，合并行数: {len(records)}")
    if preview > 0 and records:
        from tabulate import tabulate
        head = records[:preview]
        click.echo(tabulate(head, headers="keys", tablefmt="psql", showindex=False))

    if dry_run:
        click.echo("dry-run 模式，未写入数据库")
        return

    load_records_to_db(records, db_cfg=db_cfg, schema_cfg=schema_cfg, table_name=target_table, logger=logger)
    click.echo(f"写入完成 -> {db_cfg['database_url']} 表: {target_table}")


if __name__ == "__main__":
    cli()

