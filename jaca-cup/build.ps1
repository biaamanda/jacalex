$root = $PSScriptRoot
$build = Join-Path $root 'build'
$gen = Join-Path $build 'generated-sources'
$classes = Join-Path $build 'classes'

$cup = @(
    'C:\Program Files\Apache NetBeans\php\modules\ext\java-cup-11a.jar'
) | Where-Object { Test-Path $_ } | Select-Object -First 1
$jflex = Get-ChildItem "$env:USERPROFILE\.m2\repository\de\jflex\jflex" -Recurse -Filter 'jflex-*.jar' -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
$jflexRuntime = Get-ChildItem "$env:USERPROFILE\.m2\repository\com\github\vbmacher\java-cup-runtime" -Recurse -Filter 'java-cup-runtime-*.jar' -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
$jdk = Get-ChildItem 'C:\Program Files\Java\jdk*\bin\javac.exe' -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty DirectoryName

if (-not $cup)   { throw 'java-cup nao encontrado. Ajuste o caminho em build.ps1.' }
if (-not $jflex) { throw 'jflex nao encontrado em .m2. Ajuste o caminho em build.ps1.' }
if (-not $jflexRuntime) { throw 'java-cup-runtime nao encontrado em .m2. Ajuste o caminho em build.ps1.' }
if (-not $jdk)   { throw 'JDK nao encontrado em C:\Program Files\Java.' }

if (Test-Path $build) { Remove-Item $build -Recurse -Force }
New-Item -ItemType Directory "$gen\parser", $classes | Out-Null

Write-Host '1/4 CUP: gerando Parser.java e sym.java'
Push-Location "$gen\parser"
& "$jdk\java.exe" -jar $cup -parser Parser -symbols sym "$root\src\main\cup\parser.cup"
if ($LASTEXITCODE -ne 0) { Pop-Location; throw 'Falha no CUP.' }
Pop-Location

Write-Host '2/4 JFlex: gerando Scanner.java'
& "$jdk\java.exe" -cp "$jflex;$jflexRuntime" jflex.Main -q -d "$gen\parser" "$root\src\main\jflex\scanner.jflex"
if ($LASTEXITCODE -ne 0) { throw 'Falha no JFlex.' }

Write-Host '3/4 javac: compilando'
$sources = @(Get-ChildItem "$gen\parser\*.java") + @(Get-ChildItem "$root\src\main\java" -Recurse -Filter *.java)
& "$jdk\javac.exe" -encoding UTF-8 -cp $cup -d $classes ($sources | ForEach-Object FullName)
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao.' }

Write-Host '4/4 jar: empacotando build\jacacup.jar'
$tmp = Join-Path $build 'cup-runtime'
New-Item -ItemType Directory $tmp | Out-Null
Push-Location $tmp
& "$jdk\jar.exe" xf $cup java_cup/runtime
Pop-Location
Copy-Item "$tmp\java_cup" $classes -Recurse
& "$jdk\jar.exe" cfe "$build\jacacup.jar" parser.Main -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o jar.' }
Remove-Item $tmp -Recurse -Force

Write-Host ''
Write-Host 'Pronto. Execute:  java -jar build\jacacup.jar teste.input'
