# L13. Java-потребитель: повтор, конфликт и откат

[Каталог заданий](../README.md) · [Глоссарий](../../../references/glossary.md)

## Постановка задачи

ERP получает учебное событие о выпуске MBOM. Доставка может повториться после утраты подтверждения. Реализуйте consume в EventConsumer.java, используя JDBC и транзакцию PostgreSQL; исходная CLI-обвязка готова. Payload упрощён до неизменяемой строки учебного содержимого (BASE-01, BASE-02); настоящий комплект BOM не сериализуется. Потребитель не перечитывает текущую базовую линию по этому имени.

1. Контракт: eventId устойчив, aggregate определяет объект, version начинается с 1 и растёт строго на 1, payload — точная строка снимка. Одинаковый ключ с тем же aggregate/version/payload возвращает сохранённый результат. С тем же ключом и другим содержимым — KEY_CONFLICT без изменений.
2. Новое событие применимо только при version=current+1, отсутствие агрегата означает current=0. Разрыв или устаревшая версия отклоняются; они не превращаются в успешный повтор по совпадению содержимого.
3. Согласуйте запись erp_definition и inbox в одной транзакции. Флаг fail вызывает искусственный сбой после эффекта, но до inbox; обе записи должны откатиться. Все запросы с данными выполняйте через PreparedStatement.
4. Изучите проблему одновременной обработки отсутствующего агрегата и одного eventId для разных агрегатов. Для упражнения допустима общая блокировка двух таблиц, как в эталоне; обоснуйте её ограничение по производительности.
5. Вручную выполните последовательность запусков ниже. Прочитайте SQL-контроль состояния. Outbox отправителя уже содержит EVT-1, но сетевой транспорт здесь моделируется CLI: флаг отправки и автоматическая доставка не реализованы.
6. Объясните, почему локальный APPLIED подтверждает только учебное применение пакета в получателе, а не запуск производства и не проверку содержимого реальной MBOM.

## Предварительные условия

Прочитайте [теорию темы 17](../../../course/17-apis-and-enterprise-integration/theory.md) и связанные подтемы ниже. Уровень: Java выше среднего, JDBC и уверенный SQL. Поднимите [контейнерную среду](../../environment/README.md).

Перед началом выполните `reset`: это возвращает общую исходную базу ДМ-01 и удаляет результаты предыдущего задания в схеме lab. Все практикумы независимы по данным; проходите их в порядке номеров для последовательного освоения. Не выполняйте reset посреди практикума. Файлы в practice/output reset не удаляет.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File practice/environment/lab.ps1 reset
```

Команды в заданиях выполняются из корня репозитория. Для Ubuntu: `bash practice/environment/lab.sh reset`. Редактируйте заготовку на хосте любым редактором; SQL и код исполняйте в контейнерах.

## Заготовки и запуск

[SQL-заготовка L13](../../postgresql/queries/l13.sql). Выполните после заполнения:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File practice/environment/lab.ps1 sql practice/postgresql/queries/l13.sql
```

В Ubuntu: `bash practice/environment/lab.sh sql practice/postgresql/queries/l13.sql`. Для интерактивных опытов используйте действие `psql`; после ожидаемого SQL-отказа внутри транзакции выполните ROLLBACK. Скрипт sql останавливается на неожиданной ошибке.

Последовательность ручных запусков из корня (сначала собственная реализация):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File practice/environment/lab.ps1 java practice/java/EventConsumer.java EVT-1 MBOM:DM:P1 1 BASE-01
```

Повторите её, затем замените payload на OTHER для конфликта. Для второго события используйте аргументы `EVT-2 MBOM:DM:P1 2 BASE-02 fail`, затем ту же команду без `fail`. Для разрыва — `EVT-4 MBOM:DM:P1 4 BASE-04`. Ожидаемые исключения Java возвращают ненулевой код; после каждого откройте SQL-контроль, чтобы убедиться в отсутствии частичного эффекта.

В Ubuntu: `bash practice/environment/lab.sh java practice/java/EventConsumer.java ...`. Для эталона замените путь на `practice/solutions/EventConsumer.java`; оба файла имеют один класс и запускаются по одному как Java source-file. Java-классы на хосте не компилируются. Для проверки параллельного повтора сначала reset, затем запустите первую команду одновременно в двух терминалах.

[Java-заготовка consume](../../java/EventConsumer.java).

## Критерии самостоятельной оценки

Первый EVT-1 → APPLIED, точный повтор → REPLAY:APPLIED. Иное содержимое того же ключа даёт KEY_CONFLICT. Сбой EVT-2/version=2 оставляет erp version=1 и один inbox. Повтор EVT-2 без fail даёт APPLIED и version=2; новая version=4 отклоняется без эффекта. Два параллельных одинаковых EVT-1 дают один предметный эффект. Все отказы оставляют согласованные таблицы.

Сохраните собственный SQL/код, результаты и краткое объяснение оснований решения. Сравнивайте предметный смысл, точные ключи, количества и отказы; порядок строк без ORDER BY и времена выполнения могут отличаться. Автоматического оценивания, баллов и обязательной сдачи нет.

## Сверка после самостоятельного выполнения

[Эталон L13 с объяснением](../../solutions/l13.md) открывайте после собственной попытки. Перед запуском эталона снова сделайте reset, чтобы результаты вашего DDL или изменений не влияли на сравнение.

## Связь с учебными материалами

Основная тема практического покрытия — 17. Связанные темы 16 и 20: контракт обмена и ручные сценарии отказов. Настоящие CAD/CAM/CAE, сетевой API и автоматический тестовый контроль в задании отсутствуют.

- [Сервисная модель, API и контракты данных (навигация/подтема)](../../../course/17-apis-and-enterprise-integration/01-service-contracts/README.md)
- [События, идемпотентность, повторы и обработка ошибок (навигация/подтема)](../../../course/17-apis-and-enterprise-integration/02-events-and-reliability/README.md)
- [Обмен с ERP/MES и владение данными (навигация/подтема)](../../../course/17-apis-and-enterprise-integration/03-erp-mes-exchange/README.md)
- [Адаптеры и прикладная логика на Python и Java (навигация/подтема)](../../../course/17-apis-and-enterprise-integration/04-python-java-adapters/README.md)
- [17. API, расширения и интеграция предприятия (навигация/подтема)](../../../course/17-apis-and-enterprise-integration/README.md)
- [Практика к теме 17 (практика)](../../../course/17-apis-and-enterprise-integration/practice.md)
- [API, расширения и интеграция предприятия (теория)](../../../course/17-apis-and-enterprise-integration/theory.md)
- [Связи CAD-моделей, чертежей и объектов изделия (навигация/подтема)](../../../course/16-cad-cam-cae-integration/01-cad-associations/README.md)
- [Зависимости CAD-сборок, метаданные и синхронизация EBOM (навигация/подтема)](../../../course/16-cad-cam-cae-integration/02-cad-dependencies/README.md)
- [Расчётные модели, условия, результаты и прослеживаемость CAE (навигация/подтема)](../../../course/16-cad-cam-cae-integration/03-cae-data/README.md)
- [CAM-проекты, управляющие программы и производственный контекст (навигация/подтема)](../../../course/16-cad-cam-cae-integration/04-cam-data/README.md)
- [Нейтральные форматы обмена и границы адаптеров (навигация/подтема)](../../../course/16-cad-cam-cae-integration/05-exchange-and-adapters/README.md)
- [16. Интеграция с CAD/CAM/CAE (навигация/подтема)](../../../course/16-cad-cam-cae-integration/README.md)
- [Практика к теме 16 (практика)](../../../course/16-cad-cam-cae-integration/practice.md)
- [Интеграция с CAD/CAM/CAE (теория)](../../../course/16-cad-cam-cae-integration/theory.md)
- [Инварианты модели и предметные сценарии проверки (навигация/подтема)](../../../course/20-verification-and-testing/01-invariants-and-scenarios/README.md)
- [Проверка составов, правил конфигурации и применяемости (навигация/подтема)](../../../course/20-verification-and-testing/02-configuration-tests/README.md)
- [Проверка прав, процессов и интеграционных контрактов (навигация/подтема)](../../../course/20-verification-and-testing/03-security-and-integration-tests/README.md)
- [20. Проверка моделей и тестирование PLM/PDM-решений (навигация/подтема)](../../../course/20-verification-and-testing/README.md)
- [Практика к теме 20 (практика)](../../../course/20-verification-and-testing/practice.md)
- [Проверка моделей и тестирование PLM/PDM-решений (теория)](../../../course/20-verification-and-testing/theory.md)
