# Cria a estrutura de pacotes do Maozinha organizada por funcionalidade.
# Executar na raiz do projeto (a pasta onde fica o pom.xml).
# Seguro para rodar mais de uma vez: nunca sobrescreve arquivo existente.

$ErrorActionPreference = 'Stop'

$root = (Get-Location).Path
if (-not (Test-Path (Join-Path $root 'pom.xml'))) {
    Write-Host 'ERRO: execute na raiz do projeto, onde fica o pom.xml.' -ForegroundColor Red
    exit 1
}

$pacoteBase = 'br.com.maozinha'
$pastaBase  = Join-Path $root 'src\main\java\br\com\maozinha'
$semBom     = New-Object System.Text.UTF8Encoding($false)
$script:novos = 0

function CriarPasta([string]$sub) {
    $pasta = $pastaBase
    if ($sub) { $pasta = Join-Path $pastaBase $sub }
    New-Item -ItemType Directory -Force -Path $pasta | Out-Null
    return $pasta
}

function CriarTipo([string]$sub, [string]$tipo, [string]$nome) {
    $pasta = CriarPasta $sub
    $arquivo = Join-Path $pasta ($nome + '.java')
    if (Test-Path $arquivo) { return }
    $pacote = $pacoteBase
    if ($sub) { $pacote = $pacoteBase + '.' + ($sub -replace '\\', '.') }
    $conteudo = "package $pacote;`r`n`r`npublic $tipo $nome {`r`n}`r`n"
    [System.IO.File]::WriteAllText($arquivo, $conteudo, $semBom)
    $script:novos++
}

function CriarGitkeep([string]$sub) {
    $pasta = CriarPasta $sub
    $arquivo = Join-Path $pasta '.gitkeep'
    if (-not (Test-Path $arquivo)) {
        [System.IO.File]::WriteAllText($arquivo, '', $semBom)
    }
}

# Pastas sem classe por enquanto (o .gitkeep as mantem no Git)
$pastasVazias = @(
    'common\config', 'common\exception', 'security',
    'auth\dto', 'user\dto', 'job\dto', 'interest\dto', 'review\dto', 'admin\dto'
)
foreach ($p in $pastasVazias) { CriarGitkeep $p }

# Cada item: pacote, tipo, nome
$tipos = @(
    # auth
    @('auth', 'class',     'AuthController'),
    @('auth', 'class',     'AuthService'),
    @('auth', 'class',     'RefreshToken'),
    @('auth', 'class',     'PasswordResetToken'),
    @('auth', 'interface', 'RefreshTokenRepository'),
    @('auth', 'interface', 'PasswordResetTokenRepository'),

    # user
    @('user', 'class',     'UserController'),
    @('user', 'class',     'UserService'),
    @('user', 'class',     'User'),
    @('user', 'enum',      'Role'),
    @('user', 'enum',      'UserStatus'),
    @('user', 'interface', 'UserRepository'),
    @('user', 'class',     'Skill'),
    @('user', 'interface', 'SkillRepository'),
    @('user', 'class',     'SkillService'),
    @('user', 'class',     'SkillController'),

    # category
    @('category', 'class',     'CategoryController'),
    @('category', 'class',     'CategoryService'),
    @('category', 'class',     'Category'),
    @('category', 'interface', 'CategoryRepository'),

    # job
    @('job', 'class',     'JobController'),
    @('job', 'class',     'JobService'),
    @('job', 'class',     'JobStateMachine'),
    @('job', 'class',     'Job'),
    @('job', 'enum',      'JobStatus'),
    @('job', 'interface', 'JobRepository'),

    # interest
    @('interest', 'class',     'InterestController'),
    @('interest', 'class',     'InterestService'),
    @('interest', 'class',     'SelectionService'),
    @('interest', 'class',     'Interest'),
    @('interest', 'enum',      'InterestStatus'),
    @('interest', 'interface', 'InterestRepository'),

    # review
    @('review', 'class',     'ReviewController'),
    @('review', 'class',     'ReviewService'),
    @('review', 'class',     'Review'),
    @('review', 'interface', 'ReviewRepository'),

    # admin
    @('admin', 'class',     'AdminController'),
    @('admin', 'class',     'AdminService'),
    @('admin', 'class',     'ModerationAction'),
    @('admin', 'interface', 'ModerationActionRepository'),
    @('admin', 'enum',      'ModerationTargetType'),
    @('admin', 'enum',      'ModerationActionType')
)
foreach ($t in $tipos) { CriarTipo ($t[0]) ($t[1]) ($t[2]) }

Write-Host ''
Write-Host ('Concluido. Arquivos Java novos: ' + $script:novos) -ForegroundColor Green
Write-Host ''
tree /F src\main\java