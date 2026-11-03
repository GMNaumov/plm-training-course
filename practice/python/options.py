"""L07: дополните validate; запускать из корня репозитория в python-контейнере."""
import json
from pathlib import Path


def validate(config: dict) -> tuple[str, list[str]]:
    """Вернуть INVALID, INCOMPLETE либо VALID и объяснения нарушений.

    Все ключи обязательны. None означает незавершённый выбор.
    REDUNDANT требует DUAL; DUAL требует FW-B.
    Сначала выявляются нарушения известных значений; затем неполнота.
    """
    raise NotImplementedError("Реализуйте предметные правила L07")


if __name__ == "__main__":
    configs = json.loads(Path("practice/datasets/options.json").read_text(encoding="utf-8"))
    for config in configs:
        print(config["id"], *validate(config))
