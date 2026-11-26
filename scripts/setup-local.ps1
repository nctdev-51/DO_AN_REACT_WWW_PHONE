param([string]$MariaDbBin = 'C:\Program Files\MariaDB 12.2\bin')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$configPath = Join-Path $projectRoot 'application-local.properties'
if (Test-Path -LiteralPath $configPath) {
    Write-Output 'Local database configuration already exists. No credentials changed.'
    exit 0
}
$client = Join-Path $MariaDbBin 'mariadb.exe'
if (!(Test-Path -LiteralPath $client)) { throw 'Set -MariaDbBin to the installed MariaDB bin directory.' }
$securePassword = Read-Host 'MariaDB root password' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$oldMysqlPassword = $env:MYSQL_PWD
$utf8 = New-Object System.Text.UTF8Encoding($false)
$sqlPath = Join-Path $projectRoot 'target/setup-local.sql'
try {
    $env:MYSQL_PWD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    $appPassword = [Guid]::NewGuid().ToString('N') + 'Aa9!'
    $sql = @"
CREATE DATABASE IF NOT EXISTS webshop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'webshop_app'@'localhost' IDENTIFIED BY '$appPassword';
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,INDEX,REFERENCES ON webshop_db.* TO 'webshop_app'@'localhost';
"@
    New-Item -ItemType Directory -Force (Join-Path $projectRoot 'target') | Out-Null
    [IO.File]::WriteAllText($sqlPath, $sql, $utf8)
    $sqlForClient = $sqlPath.Replace('\', '/')
    & $client --skip-ssl -u root "--execute=source $sqlForClient"
    if ($LASTEXITCODE -ne 0) { throw 'Setup failed. If webshop_app already exists, configure its existing credentials in application-local.properties.' }
    [IO.File]::WriteAllText($configPath, "spring.datasource.username=webshop_app`nspring.datasource.password=$appPassword`n", $utf8)
    Write-Output 'MariaDB configured. Start the app with .\mvnw.cmd spring-boot:run.'
} finally {
    $env:MYSQL_PWD = $oldMysqlPassword
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    if (Test-Path -LiteralPath $sqlPath) { Remove-Item -LiteralPath $sqlPath }
}
