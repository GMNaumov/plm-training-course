param(
    [ValidateSet('up','reset','sql','psql','python','java','backup','restore','down','clean')]
    [string]$Action = 'up',
    [Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments
)
$ErrorActionPreference = 'Stop'
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
function Invoke-Compose {
    & docker compose -f (Join-Path $repo 'compose.yaml') @args
    if ($LASTEXITCODE -ne 0) { throw "Docker завершился с кодом: $LASTEXITCODE" }
}
function Require-Path {
    if (!$Arguments -or $Arguments[0] -notmatch '^practice/[a-zA-Z0-9_./-]+$' -or $Arguments[0].Contains('..')) {
        throw 'Ожидается относительный путь practice/... без ..'
    }
}
switch ($Action) {
    up { Invoke-Compose up -d --wait db }
    reset {
        Invoke-Compose up -d --wait db
        Invoke-Compose exec -T db psql -X -U plm_owner -d plm -v ON_ERROR_STOP=1 -f /workspace/practice/postgresql/schema/reset.sql
    }
    sql {
        Require-Path
        Invoke-Compose exec -T db psql -X -U plm_owner -d plm -v ON_ERROR_STOP=1 -f "/workspace/$($Arguments[0])"
    }
    psql { Invoke-Compose exec db psql -X -U plm_owner -d plm }
    python { Require-Path; Invoke-Compose run --rm python @Arguments }
    java {
        Require-Path
        Invoke-Compose build java
        Invoke-Compose run --rm java @Arguments
    }
    backup { Invoke-Compose exec -T db pg_dump -U plm_owner -d plm -Fc -f /artifacts/plm.dump }
    restore {
        # Восстанавливаем в отдельную базу данных, сохраняя plm.
        Invoke-Compose exec -T db dropdb -U plm_owner --if-exists plm_restore
        Invoke-Compose exec -T db createdb -U plm_owner -T template0 plm_restore
        Invoke-Compose exec -T db pg_restore -U plm_owner -d plm_restore --exit-on-error /artifacts/plm.dump
    }
    down { Invoke-Compose down --remove-orphans }
    clean {
        # Удаляем только выбранный проект Compose; глобальную очистку docker system prune не используем.
        Invoke-Compose --profile tools down --volumes --remove-orphans --rmi local
        $output = [IO.Path]::GetFullPath((Join-Path $repo 'practice/output'))
        if ($output -ne (Join-Path $repo 'practice\output')) { throw 'Недопустимый путь очистки' }
        Get-ChildItem -LiteralPath $output -Force | Where-Object Name -ne '.gitkeep' | Remove-Item -Recurse -Force
    }
}
