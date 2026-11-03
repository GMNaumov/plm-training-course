# Источники и рекомендуемая литература

[К разделу](README.md)

## Как используются источники

Проверка ссылок и заявленной области выполнена 2026-11-03. Ниже перечислены первичные материалы организаций, разработчиков и издателя стандарта. Теоретические модели и условные примеры курса являются учебным обобщением; они не описывают внутреннюю схему коммерческой системы и не устанавливают обязательную отраслевую процедуру.

Ссылки PostgreSQL закреплены на документации ветки 18; практикум использует PostgreSQL 18.6 в отдельном контейнере. Протокол прежнего запуска и закреплённые зависимости приведены в [документации среды](../practice/environment/README.md). Методическая ревизия не обновляла образы и не выполняла новый контейнерный запуск.

## Модель данных, SQL и эксплуатация

| Источник | Использование в курсе |
| --- | --- |
| [PostgreSQL 18: Transaction Isolation](https://www.postgresql.org/docs/18/transaction-iso.html) | Снимки данных, видимость конкурентных изменений, конфликты транзакций; тема 02. |
| [PostgreSQL 18: Constraints](https://www.postgresql.org/docs/18/ddl-constraints.html) | Виды ограничений и границы защиты локальной целостности; тема 02. |
| [PostgreSQL 18: Explicit Locking](https://www.postgresql.org/docs/18/explicit-locking.html) | Блокировки, конфликты и взаимные ожидания при изменениях; тема 02. Выбор полного протокола конкурентного изменения графа остаётся задачей учебной реализации. |
| [PostgreSQL 18: WITH Queries](https://www.postgresql.org/docs/18/queries-with.html) | Рекурсивные CTE, обход и контроль циклов; тема 08. Реализация обхода находится в L05; её область — подготовленный учебный состав. |
| [PostgreSQL 18: Using EXPLAIN](https://www.postgresql.org/docs/18/using-explain.html) | Анализ плана и различие оценки и фактического выполнения; тема 04. |
| [PostgreSQL 18: Indexes](https://www.postgresql.org/docs/18/indexes.html) | Назначение индексов и затраты их поддержки; тема 04. |
| [PostgreSQL 18: Backup and Restore](https://www.postgresql.org/docs/18/backup.html) | Подходы к резервному копированию БД; тема 04. Согласование внешнего файлового хранилища требует дополнительного решения. |

## Доступ, требования и конфигурация

| Источник | Использование в курсе |
| --- | --- |
| [NIST SP 800-162: Guide to Attribute Based Access Control](https://www.nist.gov/publications/guide-attribute-based-access-control-abac-definition-and-considerations-1) | Определение ABAC по атрибутам субъекта, объекта, действия и среды; тема 03. Не устанавливает алгоритм прав конкретной PLM-системы. |
| [NASA Systems Engineering Handbook: 5.0 Product Realization](https://www.nasa.gov/reference/5-0-product-realization/) | Различие верификации и валидации; тема 11. Руководство NASA не объявляется обязательной нормой для выбранного изделия. |
| [NASA Systems Engineering Handbook: 6.0 Crosscutting Technical Management](https://www.nasa.gov/reference/6-0-crosscutting-technical-management/) | Методическое чтение по управлению требованиями, конфигурацией и техническими данными; используется при углублённом объяснении темы 11. |

## Обмен и примеры продуктовых механизмов

| Источник | Что подтверждает и чего не подтверждает |
| --- | --- |
| [ISO 10303-1:2024 — Overview and fundamental principles](https://www.iso.org/standard/83105.html) | Официальная карточка области стандарта представления и обмена данными. Полный нормативный текст не анализировался; соответствие реализации отдельным частям не утверждается. |
| [Siemens: Teamcenter BOM management](https://www.siemens.com/en-us/products/teamcenter/solutions/bom-bill-of-materials-management/) | Официальное описание направлений управления BOM, ревизиями, применяемостью и вариантами. Не является руководством по настройке конкретной установки. |
| [PTC Windchill 13.1.2: Detecting and Resolving Discrepancies in the BOM Transformer](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/mpmlink/ExpMPM_RsolveDiscrepancies.html) | Пример механизма поиска и согласования расхождений; тема 13. Настройки и названия не переносятся на другие продукты. |

## Что пока не выбрано

Конкретные авиационные нормы, юрисдикция, требования к подписи, сертификации аппаратуры и ПО не определены. Целевые коммерческие установки, CAD-адаптеры и отраслевые процедуры не выбраны. Для документальных разборов выбран справочный Windchill 13.1.2.0, отдельно WRS 2.7, а для Teamcenter и ENOVIA — доступные первичные публикации с указанными датами и выпусками там, где они названы. Это не выбор единой совместимой промышленной конфигурации.

При последующем наполнении продуктовых деталей нужно фиксировать версию, модуль, статус документа и дату проверки. Для нормативных требований дополнительно устанавливают применимость к изделию и процессу. Обзорная продуктовая страница не заменяет нормативный текст или справку конкретного выпуска.

[Практические задания и карта покрытия](../practice/exercises/README.md) · [Контейнерная среда](../practice/environment/README.md)


## Контейнерный практикум (проверено 2026-11-03)

Проверенные версии контейнеров приведены в [инструкции среды](../practice/environment/README.md); точные digest записаны в compose.yaml, драйвер защищён SHA-256. Выбор поддерживаемой версии — проектное решение, а не обещание всегда использовать новейший релиз.

- [Официальный образ PostgreSQL](https://hub.docker.com/_/postgres) — тег 18.6, размещение тома /var/lib/postgresql для ветки 18. [Политика поддержки PostgreSQL](https://www.postgresql.org/support/versioning/) — основание выбора поддерживаемой ветки; [backup/restore](https://www.postgresql.org/docs/18/backup-dump.html) для L03.
- [Python 3.15.0: выпуск ветки](https://www.python.org/downloads/release/python-3150/) и [официальный контейнер Python](https://hub.docker.com/_/python) — выбран тег 3.15.2-slim-bookworm. [Decimal](https://docs.python.org/3.15/library/decimal.html) — десятичные значения и конечность чисел L14.
- [Eclipse Temurin 25 LTS](https://adoptium.net/temurin/releases/?version=25) — LTS-ветка; фактически проверена 25.0.5+8. [pgJDBC download](https://jdbc.postgresql.org/download/) — 42.7.14; [Connection: commit, rollback и autocommit](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html) и [PreparedStatement в pgJDBC](https://jdbc.postgresql.org/documentation/query/) — L13.
- [Порядок запуска Compose](https://docs.docker.com/compose/how-tos/startup-order/) — healthcheck и service_healthy. [Compose profiles](https://docs.docker.com/compose/how-tos/profiles/) — одноразовые инструменты. [Docker Desktop WSL 2](https://docs.docker.com/desktop/features/wsl/) — способ запуска на Windows.
- [PostgreSQL RLS](https://www.postgresql.org/docs/18/ddl-rowsecurity.html) — L02; [EXPLAIN](https://www.postgresql.org/docs/18/using-explain.html) — L03; [явные блокировки](https://www.postgresql.org/docs/18/explicit-locking.html) — L09/L13.

## Источники продуктовых разборов методической ревизии

Следующие материалы прочитаны через открытые страницы 2026-11-03. Указанный выпуск относится к источнику, а не к выбранной установке курса. Полные тексты и изображения поставщиков в репозиторий не копируются; разборы даны своими словами с прямыми ссылками. [Области и ограничения каждого примера](vendor-mechanisms.md).

| Источник | Выпуск или дата; статус | Подтверждённая область |
| --- | --- | --- |
| [Siemens: BVR Precision](https://blogs.sw.siemens.com/designcenter/teamcenter-bvr-precision-how-to-deal-with-the-fork-in-the-road/) | 2018-01-29; авторская техническая публикация Siemens, выпуск не указан | Различие precise/imprecise в сценарии миграции CAD; рекомендация не обобщается на все установки |
| [Siemens: LOV updates](https://blogs.sw.siemens.com/teamcenter/How-to-Perform-List-of-Value-LOV-Updates-without-System-Downtime/) | 2014-08-13; технический блог с пояснением product manager BMIDE; batch LOV — 9.1, dynamic LOV — 10.1 | Шаблоны конфигурации и способы организации LOV; не регламент любого обновления |
| [Siemens: product configuration intent](https://blogs.sw.siemens.com/teamcenter/product-configuration-intent/) | 2026-08-04; продуктовый обзор без номера выпуска | Intent и представления общей модели вариантности |
| [Siemens: Easy Plan 2606](https://blogs.sw.siemens.com/teamcenter-manufacturing/2026/07/28/whats-new-in-teamcenter-manufacturing-easy-plan-2606/) | 2606, 2026-07-28; обзор выпуска | MBOM/BOP, область accountability, изменения вхождений |
| [PTC: Versioning Parts, Documents and CAD Documents](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/WCAdminContext/WCAdminContextAdminVersionPartDocCADDoc.html) | Windchill 13.1.2.0; справка | Master, revision, iteration и контекстные ограничения |
| [PTC: Configuration Specifications](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/prodstructure/PMConfigSpecAbout.html) | Windchill 13.1.2.0; справка | Порядок выбора, неразрешённые зависимости, настройки резервного выбора и отображения |
| [PTC: Permissions](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/securitymgmt/SecurityMgmtPermissionsRef.html) | Windchill 13.1.2.0; справка | Разные права, policy и ad hoc; не универсальная формула приоритета |
| [PTC: Access Control and Workflow Templates](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/workflow_chp/WFChp_AccessControl.html) | Windchill 13.1.2.0; справка | Особенность проверки доступа в expressions workflow activity/robot node |
| [PTC: Attribute Constraint Rules](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/typeattr/TypeMgrAttrConstraintRuleRef.html) | Windchill 13.1.2.0; справка | Classification constraint, зависимость PartsLink и внешние перечисления |
| [PTC: CAD and Part Association Overview](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/ManagingCADandPartRelationships/MCPR_AssociationOview.html) и [Usage Examples](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/ManagingCADandPartRelationships/MCPR_AssociationUsageExamples.html) | Windchill 13.1.2.0; справка | Разные функции ассоциаций CAD и part |
| [PTC: BOM Transformer discrepancies](https://support.ptc.com/help/windchill/r13.1.2.0/en/Windchill_Help_Center/mpmlink/ExpMPM_RsolveDiscrepancies.html) | Windchill MPMLink 13.1.2.0; справка | Обнаружение, статусы и разрешение расхождений |
| [PTC: WRS Accessing Domains](https://support.ptc.com/help/windchill_rest_services/r2.7/en/windchill_rest_services/WCCG_RESTAPIsAccess.html) и [OData Services as Domains](https://support.ptc.com/help/windchill_rest_services/r2.7/en/windchill_rest_services/WCCG_RESTAPIsODataAsService.html) | WRS 2.7; отдельная справка API | Обнаружение доменов/метаданных и выбор версии; совместимость с конкретной установкой отдельно не проверена |
| [3DS: Product Release Engineer — SolidPractices](https://3dswym.3dexperience.3ds.com/wiki/solidworks-news-info/getting-started-with-bom-management-using-product-release-engineer-solidpractices_Kf3Z_vYJQga1cSi9-mBNeA) | Документ 2.0, май 2025; основа SOLIDWORKS Design Role 2024 FD04; страница 2026-06-10 | Подготовлен GoEngineer, проверен Dassault Systèmes; описание Engineering Release и соседних ролей. Не исчерпывающая спецификация платформы |
| [3DS: Configuration Engineer Getting Started](https://3dswym.3dexperience.3ds.com/wiki/enovia-user-community/configuration-engineer-cfg-oc-getting-started_V6eisooJRh-CLPm9ZTdfsQ) | 2026-09-30; публичный материал роли, релиз не указан | Модель семейства, эволюция, варианты и конфигурационные приложения |
| [3DS: Change Action Management](https://learn.3ds.com/player?id=30587&lexType=1&lpId=225022&source=eduspace&uuid=null) | R2022x; открытая карточка обучения | Перечень механизмов сценария; полный учебный модуль не анализировался |
| [3DS: Enhanced Change Action Process](https://3dswym.3dexperience.3ds.com/post/enovia-user-community/enhanced-change-action-process-as-of-r2026x-fd02_kqkq0_rlTFu3PJY2oumu3Q) | R2026x FD02, 2026-06-12; продуктовая публикация ENOVIA User Community | Режимы Change Controlled Authoring и Maturity Change; не обобщается на другие выпуски |

При переходе к подробной User Assistance 3DEXPERIENCE сервис потребовал авторизацию либо не вернул доступную страницу. Непрочитанные разделы справки не являются основанием утверждений. Сторонние зеркала, форумы и поисковые фрагменты без первичного подтверждения не используются как спецификации продукта. Для нормативных требований сохраняется прежняя граница: область обязательного применения не выбрана.

## Дополнение по технической корректности учебной модели

[PostgreSQL 18: Numeric Types](https://www.postgresql.org/docs/18/datatype-numeric.html) — специальные значения numeric и их сравнение; используется для объяснения отличия положительности от конечности в теме 05 и предложении P02. [Row Security Policies](https://www.postgresql.org/docs/18/ddl-rowsecurity.html) — область действия RLS и особенности привилегированных контекстов. [Constraints](https://www.postgresql.org/docs/18/ddl-constraints.html) — границы CHECK, NULL и межстрочных ограничений. Эти страницы прочитаны при ревизии; новые исполняемые ограничения в практикум не добавлялись.
