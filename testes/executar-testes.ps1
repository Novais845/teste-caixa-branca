$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$saida = Join-Path $PSScriptRoot 'out'
if (Test-Path $saida) { Remove-Item -Recurse -Force $saida }
New-Item -ItemType Directory -Path $saida | Out-Null
javac -d $saida (Get-ChildItem "$raiz\original" -Filter *.java).FullName (Get-ChildItem "$raiz\revisado\src" -Filter *.java).FullName (Get-ChildItem "$raiz\testes\src" -Filter *.java).FullName
java -ea -cp $saida AutenticacaoServiceTest
java -ea -cp $saida AutenticacaoLegadaTest
