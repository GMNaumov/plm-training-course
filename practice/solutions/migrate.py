"""L14: эталон; CSV-обвязка используется из заготовки, оценивания нет."""
import json
import sys
from decimal import Decimal, InvalidOperation
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "python"))
import migrate


def normalize(row: dict) -> dict:
    code = row["item_code"].strip().upper()
    if not row["source_id"].strip() or not code:
        raise ValueError("EMPTY_ID")
    if not row["mass"].strip():
        raise ValueError("UNKNOWN_MASS")
    if row["unit"] not in ("g", "kg"):
        raise ValueError("UNSUPPORTED_MASS_UNIT")
    try:
        mass = Decimal(row["mass"])
    except InvalidOperation as exc:
        raise ValueError("INVALID_NUMBER") from exc
    if not mass.is_finite() or mass <= 0:
        raise ValueError("NON_POSITIVE_OR_NON_FINITE")
    if row["unit"] == "g":
        mass /= Decimal(1000)
    return {"source_id": row["source_id"], "item_code": code,
            "mass_kg": format(mass, "f"), "rule_version": migrate.RULE,
            "original": json.dumps(row, ensure_ascii=False, sort_keys=True)}


if __name__ == "__main__":
    migrate.normalize = normalize
    migrate.run()
