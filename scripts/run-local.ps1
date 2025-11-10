param(
  [string]$JdkHome,
  [string]$Profile = "",
  [switch]$SkipDocker
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
$appDir = Join-Path $repoRoot "app"

function Resolve-Jdk17 {
  param([string]$Preferred)

  if ($Preferred -and (Test-Path (Join-Path $Preferred "bin\java.exe"))) {
    return $Preferred
  }

  $candidates = @()
  $roots = @(
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\Java",
    "C:\Program Files\Microsoft"
  )
  foreach ($r in $roots) {
    if (Test-Path $r) {
      $candidates += Get-ChildItem -Path $r -Directory -Recurse -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match 'jdk-?17' -or $_.FullName -match 'jdk-?17' }
    }
  }
  foreach ($c in $candidates) {
    $java = Join-Path $c.FullName "bin\java.exe"
    if (Test-Path $java) { return $c.FullName }
  }
  throw "Could not find a JDK 17 installation. Install Temurin 17 or pass -JdkHome."
}

$jdkHome = Resolve-Jdk17 -Preferred $JdkHome
$env:JAVA_HOME = $jdkHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using JAVA_HOME: $env:JAVA_HOME"
& "$env:JAVA_HOME\bin\java.exe" -version

if (-not $SkipDocker) {
  try {
    docker version | Out-Null
    Push-Location $repoRoot
    Write-Host "Starting Docker services..."
    docker compose up -d
    Pop-Location
  } catch {
    Write-Warning "Docker not available or failed to start services. Continuing..."
  }
}

Push-Location $appDir

# Build run arguments, only pass profile if provided
$runArgs = @("spring-boot:run")
if ($Profile -and $Profile.Trim().Length -gt 0) {
  $runArgs += "-Dspring-boot.run.profiles=$Profile"
}

# Prefer Maven Wrapper only if wrapper files exist; otherwise fall back to system Maven
$mvnwCmd = Join-Path $appDir "mvnw.cmd"
$wrapperProps = Join-Path $appDir ".mvn\wrapper\maven-wrapper.properties"
$useWrapper = (Test-Path $mvnwCmd) -and (Test-Path $wrapperProps)

if ($useWrapper) {
  & $mvnwCmd -v
  & $mvnwCmd @runArgs
} else {
  $mvn = Get-Command mvn -ErrorAction SilentlyContinue
  if ($mvn) {
    & $mvn.Source -v
    & $mvn.Source @runArgs
  } else {
    Write-Error "Maven wrapper files not found and 'mvn' is not available on PATH. Install Maven (winget install --id Apache.Maven -e) or add the Maven Wrapper (.mvn folder)."
    exit 1
  }
}
Pop-Location
