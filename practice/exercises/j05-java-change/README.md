# J05. Команда внедрения изменения, CAS и outbox

[Все задания](../README.md) · [Java-ветка](../../java/advanced/README.md) · [Теория](../../../course/12-change-and-workflow/theory.md) · [Глоссарий](../../../references/glossary.md)

## Задача и предварительные условия

Прочитайте L09 и L13. Нужны транзакции JDBC, UNIQUE, SELECT FOR UPDATE и optimistic locking — оптимистическая проверка ожидаемой версии. CAS (compare-and-set) означает изменение при совпадении прежнего значения. Реализуйте команду `release` для CH-1: approved → implemented только после всех действий и с событием в той же транзакции.

После reset ACT-2 остаётся pending. Первое действие сервиса обязано отказать. Используются `change_order`, `change_action`, отдельные `java_command`, `java_outbox` и `java_delivery`. Исторические outbox/inbox из L13 остаются отдельной моделью.

## Контракт и реализация

В [ChangeRelease.java](../../java/advanced/ChangeRelease.java) реализуйте:

1. Зарезервируйте commandId через INSERT ... ON CONFLICT DO NOTHING. При повторе сравните changeId и expectedVersion, возвратите прежний результат; другой договор под тем же ключом — COMMAND_CONFLICT.
2. Заблокируйте change_order через FOR NO KEY UPDATE. Вставка java_command через FK уже получает KEY SHARE; попытка усилить две такие блокировки до FOR UPDATE способна создать deadlock. Ключ родителя не меняется, поэтому NO KEY UPDATE достаточно. Проверьте ожидаемую версию и approved, затем отсутствие pending. Все писатели действий этого протокола сначала блокируют родительское изменение: иначе проверка не защищена от конкурентного добавления действия.
3. Выполните UPDATE с условием lock_version и проверкой числа обновлённых строк. Измените состояние и увеличьте версию ровно на один.
4. Создайте событие `CHANGE:CH-1:1`, агрегат `CHANGE:CH-1`, версию 1, payload `implemented`, строку доставки и результат команды. Зафиксируйте всё вместе.
5. Параметр fail вызывает исключение перед фиксацией. Не повторяйте произвольные SQL-ошибки без понимания: тайм-аут, stale и неизвестный результат commit имеют разный смысл.

```text
docker compose run --rm java practice/java/advanced/ChangeRelease.java CMD-1 CH-1 0
```

Ожидается PENDING_ACTIONS. Убедитесь, что java_command не содержит CMD-1, CH-1 по-прежнему approved/0 и события нет. Затем подготовьте принятые действия:

```text
docker compose exec -T db psql -X -U plm_owner -d plm -v ON_ERROR_STOP=1 -f /workspace/practice/java/advanced/j05-ready.sql
docker compose run --rm java practice/java/advanced/ChangeRelease.java CMD-1 CH-1 0 fail
docker compose run --rm java practice/java/advanced/ChangeRelease.java CMD-1 CH-1 0
docker compose run --rm java practice/java/advanced/ChangeRelease.java CMD-1 CH-1 0
```

## Самооценка и конкурентность

После fail состояние и версия не изменились, нет команды, события и строки доставки для изменения. Следующий вызов возвращает IMPLEMENTED, повтор — REPLAY:IMPLEMENTED. Новый CMD-2 с expectedVersion 0 получает STALE_VERSION. Повтор CMD-1 с версией 1 получает COMMAND_CONFLICT даже при уже внедрённом изменении.

После отдельного reset и j05-ready.sql запустите два разных commandId одновременно в двух терминалах. Должны существовать одно событие и одно повышение версии; второй клиент получает stale. При одинаковом commandId один вызов применяет, второй воспроизводит. Долгие операции не симулируйте удержанием транзакции после commit.

При потере ответа после commit клиент повторяет тот же commandId. Предметная квитанция позволяет выяснить результат. Если соединение потеряно именно во время commit, сообщение об ошибке не доказывает откат; проверьте команду новым соединением.

## Углубление

Добавьте авторизованную отмену отдельной командой; затем хранение полного исходного договора и результата; затем автомат состояний с разрешёнными переходами. Реализация J05 содержит один переход и не делает согласование реальной авиационной процедурой.

[Эталон](../../solutions/j05.md) · [J04](../j04-java-diff/README.md) · [J06](../j06-java-consumer/README.md).
