"""L07: логическая допустимость не подтверждает выпущенную конструкцию."""
import json
from pathlib import Path


def validate(config: dict) -> tuple[str, list[str]]:
    keys = ("redundant", "controller", "firmware")
    errors = []
    r, c, f = (config.get(k) for k in keys)
    if r is not None and type(r) is not bool:
        errors.append("redundant должен быть bool")
    if c is not None and c not in ("SINGLE", "DUAL"):
        errors.append("Неизвестный controller")
    if f is not None and f not in ("FW-A", "FW-B"):
        errors.append("Неизвестная firmware")
    if r is True and c is not None and c != "DUAL":
        errors.append("REDUNDANT требует DUAL")
    if c == "DUAL" and f is not None and f != "FW-B":
        errors.append("DUAL требует FW-B")
    if errors:
        return "INVALID", errors
    if any(config.get(k) is None for k in keys):
        return "INCOMPLETE", ["Не завершён выбор опций"]
    return "VALID", []


if __name__ == "__main__":
    for config in json.loads(Path("practice/datasets/options.json").read_text(encoding="utf-8")):
        print(config["id"], *validate(config))
