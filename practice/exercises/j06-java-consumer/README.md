# J06. Конкурентный получатель без блокировки целой таблицы

[Все задания](../README.md) · [Java-ветка](../../java/advanced/README.md) · [Теория интеграций](../../../course/17-apis-and-enterprise-integration/theory.md) · [Глоссарий](../../../references/glossary.md)

## Задача и предварительные условия

Прочитайте L13 и J05. Нужны READ COMMITTED, UNIQUE и блокировки строк. Расширьте потребителя: независимые агрегаты должны продвигаться параллельно, а самый первый приём и глобальный eventId остаются защищёнными. Простого SELECT FOR UPDATE отсутствующей строки недостаточно.

Перед самостоятельным сценарием — reset. `java_event` играет роль inbox, `java_target` — проекции получателя. Они не связаны с inbox/erp_definition L13, поэтому старые упражнения не становятся скрытым предусловием.

## Протокол реализации

В [OrderedConsumer.java](../../java/advanced/OrderedConsumer.java) реализуйте `apply`:

1. Проверьте непустые идентификаторы, payload и version ≥ 1. Payload — непрозрачная строка, сравнение точное, семантика JSON не заявляется.
2. Зарезервируйте eventId уникальным INSERT с результатом PENDING внутри незавершённой транзакции. Конкурентный такой же INSERT должен дождаться первого результата.
3. При занятом eventId прочитайте договор новым statement READ COMMITTED. Одинаковые aggregate/version/payload возвращают REPLAY:APPLIED; разные — EVENT_CONFLICT.
4. Создайте строку агрегата версии 0 через ON CONFLICT DO NOTHING и заблокируйте её. Допустима только следующая версия current+1. Пропуск и устаревшая версия с новым eventId — отказ.
5. Измените проекцию и квитанцию на APPLIED одной транзакцией. Искусственный fail между этими действиями должен откатить и эффект, и резервирование.

```text
docker compose run --rm java practice/java/advanced/OrderedConsumer.java E1 MBOM:DM:P1 1 BASE-01
docker compose run --rm java practice/java/advanced/OrderedConsumer.java E1 MBOM:DM:P1 1 BASE-01
docker compose run --rm java practice/java/advanced/OrderedConsumer.java E1 MBOM:DM:P1 1 OTHER
docker compose run --rm java practice/java/advanced/OrderedConsumer.java E3 MBOM:DM:P1 3 GAP
docker compose run --rm java practice/java/advanced/OrderedConsumer.java E2 MBOM:DM:P1 2 NEXT fail
docker compose run --rm java practice/java/advanced/OrderedConsumer.java E2 MBOM:DM:P1 2 NEXT
```

## Ожидаемые результаты и самооценка

Результаты последовательно: APPLIED, REPLAY:APPLIED, EVENT_CONFLICT, VERSION_GAP_OR_STALE, искусственный отказ, APPLIED. В итоге проекция версии 2, только E1/E2 и обе APPLIED. Проверьте это отдельным psql, не по числу сообщений Java.

После reset запустите два первых одинаковых события параллельно. Затем повторите с одинаковым eventId и разными агрегатами: только один договор вправе закрепиться. С разными eventId для одного агрегата и одинаковой версией 1 один вызов применится, другой станет stale. Разные eventId и разные агрегаты должны примениться оба, без общей блокировки таблиц.

Отдельно отправьте версию 2 до версии 1. В этом контракте нет буфера: событие 2 отвергается, затем после успешного 1 может быть повторено. Отказ не создаёт постоянный inbox, запрещающий повтор корректного события.

## Углубление

Добавьте журнал отвергнутых доставок отдельно от квитанции применения. Затем спроектируйте буфер пропущенных версий с пределом и диагностикой. Затем определите миграцию схемы payload с полноценным парсером при явном изменении зависимостей. Текущий эталон не реализует эти расширения и не обещает exactly-once transport; он защищает локальный эффект при повторах.

[Эталон](../../solutions/j06.md) · [J05](../j05-java-change/README.md) · [J07](../j07-java-outbox/README.md).
