"""L14: заполните normalize; файловая обвязка предоставлена."""
import csv
import json
from pathlib import Path

RULE = "mass-v1"


def normalize(row: dict) -> dict:
    """Вернуть source_id, item_code, mass_kg, rule_version, original.

    item_code: удалить крайние пробелы и перевести в верхний регистр; g → kg. Масса — конечное положительное Decimal.
    При неизвестном значении/единице выбросить ValueError с причиной.
    original: все исходные поля в JSON, без потери пробелов и исходной единицы.
    """
    raise NotImplementedError("Реализуйте нормализацию L14")


def run():
    output = Path("/artifacts")
    output.mkdir(exist_ok=True)
    with Path("practice/datasets/legacy.csv").open(encoding="utf-8", newline="") as source, \
         (output / "accepted.csv").open("w", encoding="utf-8", newline="") as good, \
         (output / "rejected.csv").open("w", encoding="utf-8", newline="") as bad:
        accepted = csv.DictWriter(good, fieldnames=["source_id", "item_code", "mass_kg", "rule_version", "original"])
        rejected = csv.DictWriter(bad, fieldnames=["source_id", "reason", "original"])
        accepted.writeheader()
        rejected.writeheader()
        counts = [0, 0]
        for row in csv.DictReader(source):
            try:
                accepted.writerow(normalize(row))
                counts[0] += 1
            except ValueError as exc:
                rejected.writerow({"source_id": row["source_id"], "reason": str(exc), "original": json.dumps(row, ensure_ascii=False, sort_keys=True)})
                counts[1] += 1
    print(f"Принято: {counts[0]}; отклонено: {counts[1]}; правило: {RULE}")


if __name__ == "__main__":
    run()
