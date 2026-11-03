# Запуск контейнерной среды

[К практике](../README.md) · [Задания](../exercises/README.md) · [Предварительные знания](../../docs/prerequisites.md) · [Источники версий](../../references/sources.md)

## Состав и требования

На хосте нужны Docker с Compose v2+ и редактор файлов. Для Windows 11 используйте Docker Desktop в режиме Linux containers с backend WSL 2; Ubuntu в WSL подходит для команд Bash. В Linux достаточно Docker Engine с Compose plugin. Java, Python, psql и сервер PostgreSQL на хост устанавливать не нужно. Docker должен быть запущен; возможность установки Docker и принятие его условий определяет владелец компьютера.

Стенд проверен 2026-11-03: Docker Desktop 4.96.3, Engine 29.9.2, Compose 5.6.1, linux/amd64. Воспроизводимые компоненты: PostgreSQL 18.6, Python 3.15.2, Temurin Java 25.0.5 LTS, pgJDBC 42.7.14. Образы закреплены digest в [compose.yaml](../../compose.yaml) и [Dockerfile](../java/Dockerfile), драйвер — версией и SHA-256. Java 25 выбрана как актуальная LTS-ветка; код не зависит от краткосрочного JDK 27. Наличие образов для другой архитектуры следует проверить на своей машине; ARM в этой работе не запускался.

Практический ориентир для ресурсов Docker: 2 CPU, 4 GB RAM и 5 GB свободного места; это рекомендация для малого стенда, а не измеренная минимальная конфигурация. Интернет нужен при первой загрузке образов и сборке Java-образа. Python использует только стандартную библиотеку, Maven и pip не нужны. Для повторной работы готовых образов интернет не требуется.

Сервисы: db (постоянный процесс), python/java (одноразовые контейнеры профиля tools). Исходники смонтированы только для чтения; `practice/output` смонтирован для записи как `/artifacts`. БД хранится в именованном томе pgdata. Порты на хост не публикуются. Внутренний адрес `db:5432`, база `plm`, пользователь `plm_owner`, учебный пароль `training-only`. Это общедоступные реквизиты локального стенда, не производственные секреты. GUI и настоящая коммерческая PLM-система не входят в стенд.

[Java-ветка J01–J12](../java/advanced/README.md) использует те же образы и драйвер без дополнительных зависимостей. Общий reset включает `java/advanced/schema.sql` в свою транзакцию: исходные наборы L01–L14 сохраняются, добавляются таблицы `java_*` и независимые fixtures. Lab.java задаёт statement_timeout=10s и lock_timeout=4s; отказ по времени не следует трактовать как успешную частичную операцию. JDK source-file mode компилирует необходимые соседние исходники без файлов `.class` на хосте.

## Быстрый старт в Windows PowerShell

Откройте PowerShell в корне скачанного проекта, где расположен compose.yaml:

```powershell
docker version
docker compose version
powershell -NoProfile -ExecutionPolicy Bypass -File practice/environment/lab.ps1 up
powershell -NoProfile -ExecutionPolicy Bypass -File practice/environment/lab.ps1 reset
powershell -NoProfile -ExecutionPolicy Bypass -File practice/environment/lab.ps1 psql
```

`Bypass` относится только к дочернему процессу, постоянная политика Windows не изменяется. Если корпоративная политика запрещает запуск сценариев, используйте прямые команды Docker ниже либо Bash в разрешённой Ubuntu. Скрипт PowerShell сохранён в UTF-8 с BOM: Windows PowerShell 5.1 корректно читает русские сообщения и комментарии без изменения системной кодировки.

В psql выполните `SELECT id,name FROM lab.item ORDER BY id;` и `SELECT count(*) FROM lab.base_tree;`. Ожидаются 9 изделий и 9 путей базового состава. `\q` завершает сессию. Далее откройте [L01](../exercises/l01-identity/README.md).

## Быстрый старт в Ubuntu/WSL или Linux

В Docker Desktop включите интеграцию с нужным дистрибутивом WSL. Перейдите в корень репозитория; пример пути Windows — `/mnt/d/Projects/plm-training-course` (на своей машине используйте свой путь). Скрипт запускается через bash, chmod не требуется:

```bash
docker version
docker compose version
bash practice/environment/lab.sh up
bash practice/environment/lab.sh reset
bash practice/environment/lab.sh psql
```

Не запускайте одновременно две копии курса с одним Compose-именем. По умолчанию оно plm-course; для отдельного стенда установите `$env:PLM_PROJECT_NAME='plm-course-alice'` в PowerShell или `export PLM_PROJECT_NAME=plm-course-alice` в Bash. Используйте одно и то же значение во всех терминалах и при уборке.

## Команды работы

| Действие | Результат |
| --- | --- |
| up | Запустить db и дождаться healthcheck |
| reset | Атомарно пересоздать только схему lab и загрузить seed; удалить прежние результаты SQL |
| psql | Интерактивный клиент в контейнере db |
| sql practice/путь.sql | Выполнить один файл с ON_ERROR_STOP, относительные SQL-включения работают внутри контейнера |
| python practice/путь.py | Одноразовый Python, исходник читается с хоста |
| java practice/путь.java аргументы | Собрать образ с JDBC и запустить Java source-file |
| backup | Согласованный pg_dump в practice/output/plm.dump |
| restore | Пересоздать отдельную plm_restore и загрузить dump; plm не менять |
| down | Удалить контейнеры/сеть, сохранить том данных и output |
| clean | Удалить контейнеры, сеть, том, локально собранный Java-образ и файлы output, кроме .gitkeep |

PowerShell-префикс: `powershell -NoProfile -ExecutionPolicy Bypass -File practice/environment/lab.ps1`. Bash-префикс: `bash practice/environment/lab.sh`. Скрипты проверяют ненулевые коды Docker. Заготовки намеренно содержат TODO и незавершённые методы; ошибка NotImplementedError/UnsupportedOperationException до реализации ожидаема.

Прямые команды, если скрипт недоступен:

```text
docker compose up -d --wait db
docker compose exec -T db psql -X -U plm_owner -d plm -v ON_ERROR_STOP=1 -f /workspace/practice/postgresql/schema/reset.sql
docker compose exec db psql -X -U plm_owner -d plm
docker compose run --rm python practice/python/options.py
docker compose build java
docker compose run --rm java practice/java/EventConsumer.java EVT-1 MBOM:DM:P1 1 BASE-01
docker compose --profile tools down --volumes --remove-orphans --rmi local
```

Compose сам запускает одноразовый сервис из профиля при явном `run`; обычный up поднимает только db. SQL использует UTF-8. Не передавайте SQL через PowerShell-конвейер `Get-Content | docker ...`: версии PowerShell отличаются обработкой кодировки. Используйте `-f` и файлы внутри смонтированного репозитория. Дамп создаётся внутри контейнера, поэтому бинарное перенаправление PowerShell тоже не требуется.

## Сброс, ошибки и уборка

Перед каждым заданием делайте reset. Это удаляет изменения в lab, но не Python/Java-исходники, output или plm_restore. Ошибка DDL внутри reset откатывает транзакцию; сообщение об ошибке нельзя игнорировать. Backup/restore не восстанавливают незаписанные исходники редактора. Для сохранения своей попытки скопируйте заготовки средствами редактора; при переносе используйте отдельные имена в practice/ и обновляйте путь запуска.

Если db не стал healthy, выполните `docker compose logs db` и `docker compose ps`; проверьте режим Linux containers и свободное место. Если виден старый набор, выполните reset: инициализация не привязана к docker-entrypoint-initdb.d и всегда запускается явно. Ошибка разрешения Docker API означает, что CLI не имеет доступа к Docker Desktop; исправляется на хосте. При ошибке скачивания проверьте доступ к Docker Hub и jdbc.postgresql.org, не отключая проверку TLS или контрольную сумму. Digest не следует заменять на latest ради устранения ошибки.

После работы сохраните нужные результаты вне output и выполните clean. Команда безвозвратно удаляет учебные данные именно выбранного Compose-проекта и содержимое output. При совместном использовании одной рабочей копии output общий для всех имён проектов. Скрипты не вызывают docker system prune и не трогают посторонние контейнеры/тома. Базовые официальные образы остаются в общем кэше Docker; при необходимости удалите только перечисленные в compose/Dockerfile образы, убедившись, что они не используются. Build cache принадлежит Docker и может быть общим с другими проектами, поэтому глобальная очистка не запускается.

[Протокол проверки автора](verification.md) · [Критерии самостоятельной оценки](../../docs/assessment.md)
