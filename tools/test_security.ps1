# Example (isolated test server, no production services or deployment):
# powershell -NoProfile -ExecutionPolicy Bypass -File tools/test_security.ps1 -JdkHome <JDK> -TomcatHome <Tomcat9>
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$TomcatHome = $env:CATALINA_HOME,
    [string]$TestLibraryDirectory
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    if ([string]::IsNullOrWhiteSpace($JdkHome)) {
        throw 'Specificare -JdkHome con il percorso di un JDK 8 o successivo.'
    }
    $compilerPath = Join-Path $JdkHome 'bin/javac.exe'
    $javaPath = Join-Path $JdkHome 'bin/java.exe'
    if (-not (Test-Path -LiteralPath $compilerPath)) {
        throw 'Il percorso JdkHome deve contenere javac.exe.'
    }
    if ([string]::IsNullOrWhiteSpace($TestLibraryDirectory)) {
        if ([string]::IsNullOrWhiteSpace($TomcatHome)) {
            throw 'Specificare -TomcatHome (Tomcat 9) oppure -TestLibraryDirectory.'
        }
        $testLibraries = (Join-Path $TomcatHome 'lib/*') + ';' + (Join-Path $TomcatHome 'bin/tomcat-juli.jar')
    } else {
        $testLibraries = (Join-Path $TestLibraryDirectory '*')
    }
    $outputDirectory = Join-Path $projectRoot 'tmp/apo-security-test-classes'
    New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
    $classPath = (Join-Path $projectRoot 'lib/*') + ';' + $testLibraries
    $sourceFiles = @(Get-ChildItem -LiteralPath 'src/java' -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName)
    $sourceFiles += (Join-Path $projectRoot 'test/com/example/security/SecurityRegressionTest.java')
    $argumentsFile = Join-Path $projectRoot 'tmp/apo-security-javac.args'
    $quotedSources = @($sourceFiles | ForEach-Object { '"' + $_.Replace('\', '/') + '"' })
    [IO.File]::WriteAllLines($argumentsFile, $quotedSources, (New-Object Text.UTF8Encoding($false)))
    & $compilerPath -source 8 -target 8 -encoding UTF-8 -cp $classPath -d $outputDirectory "@$argumentsFile"
    if ($LASTEXITCODE -ne 0) { throw 'Compilazione dei sorgenti e dei test fallita.' }
    & $javaPath -cp "$outputDirectory;$classPath" com.example.security.SecurityRegressionTest
    if ($LASTEXITCODE -ne 0) { throw 'Test di sicurezza falliti.' }
} finally {
    Pop-Location
}
