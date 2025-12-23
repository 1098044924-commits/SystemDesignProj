<#
 PowerShell deployment script for Windows Server
 Usage (run as Administrator):
   .\setup-windows.ps1 -JarUrl "https://example.com/SystemDesignProj.jar"
 Or upload JAR to C:\opt\accounting\ and run the script without JarUrl.
#>

param(
  [string]$JarUrl = "",
  [string]$JarFile = "SystemDesignProj-1.0-SNAPSHOT.jar",
  [string]$TemurinMsiUrl = ""
)

Write-Host "Starting Windows deployment script..."

# create local user 'accounting' if missing
if (-not (Get-LocalUser -Name "accounting" -ErrorAction SilentlyContinue)) {
    Write-Host "Creating local user 'accounting'..."
    $pw = Read-Host -AsSecureString -Prompt "Enter password for local user 'accounting' (will not echo)" 
    New-LocalUser -Name "accounting" -Password $pw -FullName "Accounting Service User" -Description "Run accounting app" -PasswordNeverExpires
} else {
    Write-Host "Local user 'accounting' already exists, skipping."
}

# create directories
$appDir = "C:\opt\accounting"
$configDir = "C:\ProgramData\accounting"
New-Item -Path $appDir -ItemType Directory -Force | Out-Null
New-Item -Path $configDir -ItemType Directory -Force | Out-Null

# set permissions for accounting user
try {
    $acl = Get-Acl $appDir
    $acct = [System.Security.Principal.NTAccount]"NT AUTHORITY\Authenticated Users"
    $rule = New-Object System.Security.AccessControl.FileSystemAccessRule("accounting","Modify","ContainerInherit,ObjectInherit","None","Allow")
    $acl.SetAccessRule($rule)
    Set-Acl $appDir $acl
} catch {
    Write-Warning "Could not set ACL for $appDir: $_"
}

# write env file
$envFile = Join-Path $configDir "accounting.env"
"JDBC_DATABASE_URL=${env:JDBC_DATABASE_URL}" | Out-File -FilePath $envFile -Encoding utf8
"JDBC_DATABASE_USERNAME=${env:JDBC_DATABASE_USERNAME}" | Out-File -FilePath $envFile -Encoding utf8 -Append
"JDBC_DATABASE_PASSWORD=${env:JDBC_DATABASE_PASSWORD}" | Out-File -FilePath $envFile -Encoding utf8 -Append
"JDBC_DATABASE_DRIVER=${env:JDBC_DATABASE_DRIVER}" | Out-File -FilePath $envFile -Encoding utf8 -Append
"JPA_HBM2DDL=${env:JPA_HBM2DDL}" | Out-File -FilePath $envFile -Encoding utf8 -Append
"SPRING_PORT=${env:SPRING_PORT}" | Out-File -FilePath $envFile -Encoding utf8 -Append
"JAVA_OPTS=${env:JAVA_OPTS}" | Out-File -FilePath $envFile -Encoding utf8 -Append
"JAR_FILE=$JarFile" | Out-File -FilePath $envFile -Encoding utf8 -Append

Write-Host "Wrote env to $envFile"

# check java
function Java-Installed {
    try {
        $v = & java -version 2>&1
        return $true
    } catch {
        return $false
    }
}

if (-not (Java-Installed)) {
    Write-Host "Java not found. Attempting to install..."
    if (Get-Command choco -ErrorAction SilentlyContinue) {
        choco install temurin17 -y
    } elseif ($TemurinMsiUrl) {
        $msi = "$env:TEMP\temurin17.msi"
        Invoke-WebRequest -Uri $TemurinMsiUrl -OutFile $msi
        Start-Process msiexec.exe -ArgumentList "/i `"$msi`" /qn" -Wait
        Remove-Item $msi -Force
    } else {
        Write-Warning "No automatic installer available. Please install Java 17 manually or provide TemurinMsiUrl parameter."
    }
} else {
    Write-Host "Java already installed, skipping."
}

# download jar if JarUrl provided and absent
$destJar = Join-Path $appDir $JarFile
if ($JarUrl -and -not (Test-Path $destJar)) {
    Write-Host "Downloading JAR from $JarUrl ..."
    Invoke-WebRequest -Uri $JarUrl -OutFile $destJar
}

if (-not (Test-Path $destJar)) {
    Write-Warning "JAR not found at $destJar. Upload it manually and re-run the script."
}

# find java executable path
$javaCmd = (Get-Command java.exe -ErrorAction SilentlyContinue).Source
if (-not $javaCmd) {
    $javaCmd = (Get-Command java -ErrorAction SilentlyContinue).Source
}

if (-not $javaCmd) {
    Write-Warning "Java executable not found in PATH; service creation may fail."
}

# create Windows service using sc (runs as LocalSystem)
$serviceName = "AccountingApp"
if (!(Get-Service -Name $serviceName -ErrorAction SilentlyContinue)) {
    if ($javaCmd -and (Test-Path $destJar)) {
        $binPath = "`"$javaCmd`" -jar `"$destJar`""
        Write-Host "Creating service $serviceName ..."
        sc.exe create $serviceName binPath= $binPath start= auto
        sc.exe description $serviceName "Accounting application service"
        Start-Service $serviceName
        Write-Host "Service $serviceName started."
    } else {
        Write-Warning "Cannot create service: missing java or jar."
    }
} else {
    Write-Host "Service $serviceName already exists, attempting to restart."
    Restart-Service $serviceName -Force
}

Write-Host "Windows deployment script finished."



