# Python: опции и перенос

[К практике](../README.md) · [Среда](../environment/README.md)

- [L07: ограничения опций](../exercises/l07-options/README.md) — [options.py](options.py), реализовать validate; [эталон](../solutions/l07.md).
- [L14: нормализация миграции](../exercises/l14-migration/README.md) — [migrate.py](migrate.py), изучить обвязку CSV и реализовать normalize; [эталон](../solutions/l14.md).

Python 3.15.2 запускается одноразовым контейнером. Зависимостей pip нет; Decimal, csv, json и pathlib входят в стандартную библиотеку. Источники читаются из репозитория, результаты L14 пишутся в /artifacts. PYTHONDONTWRITEBYTECODE исключает кэш рядом с исходниками. Заготовки намеренно незавершены. Эталон migrate.py переиспользует файловую обвязку заготовки, чтобы разбор концентрировался на предметном преобразовании.
