$ErrorActionPreference = "Stop"

$BaseUrl = "http://localhost:8080"
$TempDir = Join-Path ([System.IO.Path]::GetTempPath()) "bug-tracker-smoke-$PID"
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null

function Assert-Status {
    param(
        [string]$Label,
        [string]$Actual,
        [string]$Expected
    )

    if ($Actual -ne $Expected) {
        throw "$Label failed. expected=$Expected actual=$Actual"
    }

    Write-Host "[OK] $Label -> HTTP $Actual"
}

function Get-CsrfFromLoginPage {
    param(
        [string]$CookieFile,
        [string]$HtmlFile
    )

    $status = & curl.exe -s `
        -b $CookieFile `
        -c $CookieFile `
        -o $HtmlFile `
        -w "%{http_code}" `
        "$BaseUrl/login"

    Assert-Status "GET /login" $status "200"

    $html = Get-Content $HtmlFile -Raw
    if ($html -notmatch 'name="_csrf"[^>]*value="([^"]+)"') {
        throw "CSRF token was not found in login page."
    }

    return $Matches[1]
}

function Login-User {
    param(
        [string]$Username,
        [string]$Password,
        [string]$Prefix
    )

    $cookieFile = Join-Path $TempDir "$Prefix-cookies.txt"
    $loginHtml = Join-Path $TempDir "$Prefix-login.html"
    $afterLoginHtml = Join-Path $TempDir "$Prefix-login-authenticated.html"

    New-Item -ItemType File -Path $cookieFile -Force | Out-Null
    $loginCsrf = Get-CsrfFromLoginPage -CookieFile $cookieFile -HtmlFile $loginHtml

    $status = & curl.exe -s `
        -b $cookieFile `
        -c $cookieFile `
        -o (Join-Path $TempDir "$Prefix-login-response.txt") `
        -w "%{http_code}" `
        -X POST "$BaseUrl/login" `
        -H "Content-Type: application/x-www-form-urlencoded" `
        --data-urlencode "username=$Username" `
        --data-urlencode "password=$Password" `
        --data-urlencode "_csrf=$loginCsrf"

    Assert-Status "$Prefix DB login" $status "302"

    $csrf = Get-CsrfFromLoginPage -CookieFile $cookieFile -HtmlFile $afterLoginHtml

    return [PSCustomObject]@{
        CookieFile = $cookieFile
        Csrf = $csrf
    }
}

try {
    Write-Host "Bug Tracker smoke check: $BaseUrl"

    $healthBody = Join-Path $TempDir "health.txt"
    $healthStatus = & curl.exe -s -o $healthBody -w "%{http_code}" "$BaseUrl/health"
    Assert-Status "Public health check" $healthStatus "200"

    if ((Get-Content $healthBody -Raw).Trim() -ne "OK") {
        throw "GET /health body was not OK."
    }

    $unauthHeaders = Join-Path $TempDir "unauth-headers.txt"
    $unauthStatus = & curl.exe -s `
        -D $unauthHeaders `
        -o (Join-Path $TempDir "unauth-body.txt") `
        -w "%{http_code}" `
        "$BaseUrl/api/bugs"
    Assert-Status "Unauthenticated GET /api/bugs" $unauthStatus "302"

    $userSession = Login-User -Username "user" -Password "userpass" -Prefix "user"
    $adminSession = Login-User -Username "admin" -Password "adminpass" -Prefix "admin"

    $userListStatus = & curl.exe -s `
        -b $userSession.CookieFile `
        -o (Join-Path $TempDir "user-list.json") `
        -w "%{http_code}" `
        "$BaseUrl/api/bugs"
    Assert-Status "USER GET /api/bugs" $userListStatus "200"

    $createHeaders = Join-Path $TempDir "create-headers.txt"
    $createBodyFile = Join-Path $TempDir "create-body.json"
    $createBody = '{"title":"smoke verification bug","description":"created by scripts/smoke.ps1","status":"OPEN","priority":"LOW"}'

    $createStatus = & curl.exe -s `
        -D $createHeaders `
        -o $createBodyFile `
        -b $adminSession.CookieFile `
        -w "%{http_code}" `
        -X POST "$BaseUrl/api/bugs" `
        -H "Content-Type: application/json" `
        -H "X-CSRF-TOKEN: $($adminSession.Csrf)" `
        --data-raw $createBody
    Assert-Status "ADMIN POST /api/bugs" $createStatus "201"

    $locationMatch = Select-String -Path $createHeaders -Pattern '^Location:\s*(.+)$'
    if (-not $locationMatch) {
        throw "Location header was not found after POST /api/bugs."
    }

    $bugLocation = $locationMatch.Matches[0].Groups[1].Value.Trim()
    if ($bugLocation -match '^https?://') {
        $bugUrl = $bugLocation
    } else {
        $bugUrl = "$BaseUrl$bugLocation"
    }

    $created = Get-Content $createBodyFile -Raw | ConvertFrom-Json
    if (-not $created.id) {
        throw "Created Bug id was not found in response body."
    }

    $getStatus = & curl.exe -s `
        -b $adminSession.CookieFile `
        -o (Join-Path $TempDir "get-body.json") `
        -w "%{http_code}" `
        $bugUrl
    Assert-Status "ADMIN GET created Bug" $getStatus "200"

    $updateBodyFile = Join-Path $TempDir "update-body.json"
    $updateBody = '{"title":"smoke verification bug updated","description":"updated by scripts/smoke.ps1","status":"IN_PROGRESS","priority":"HIGH"}'
    $updateStatus = & curl.exe -s `
        -b $adminSession.CookieFile `
        -o $updateBodyFile `
        -w "%{http_code}" `
        -X PUT $bugUrl `
        -H "Content-Type: application/json" `
        -H "X-CSRF-TOKEN: $($adminSession.Csrf)" `
        --data-raw $updateBody
    Assert-Status "ADMIN PUT created Bug" $updateStatus "200"

    $updated = Get-Content $updateBodyFile -Raw | ConvertFrom-Json
    if ($updated.status -ne "IN_PROGRESS" -or $updated.priority -ne "HIGH") {
        throw "PUT response did not contain the expected status/priority."
    }

    $searchStatus = & curl.exe -s `
        -b $adminSession.CookieFile `
        -o (Join-Path $TempDir "search-body.json") `
        -w "%{http_code}" `
        "$BaseUrl/api/bugs?status=IN_PROGRESS&priority=HIGH&keyword=smoke&page=0&size=10"
    Assert-Status "Search/filter GET /api/bugs" $searchStatus "200"

    $pageStatus = & curl.exe -s `
        -b $adminSession.CookieFile `
        -o (Join-Path $TempDir "page-body.json") `
        -w "%{http_code}" `
        "$BaseUrl/api/bugs?page=0&size=1"
    Assert-Status "Paging GET /api/bugs" $pageStatus "200"

    $userDeleteStatus = & curl.exe -s `
        -b $userSession.CookieFile `
        -o (Join-Path $TempDir "user-delete-body.txt") `
        -w "%{http_code}" `
        -X DELETE $bugUrl `
        -H "X-CSRF-TOKEN: $($userSession.Csrf)"
    Assert-Status "USER DELETE denied" $userDeleteStatus "403"

    $adminDeleteStatus = & curl.exe -s `
        -b $adminSession.CookieFile `
        -o (Join-Path $TempDir "admin-delete-body.txt") `
        -w "%{http_code}" `
        -X DELETE $bugUrl `
        -H "X-CSRF-TOKEN: $($adminSession.Csrf)"
    Assert-Status "ADMIN DELETE allowed" $adminDeleteStatus "204"

    $deletedGetStatus = & curl.exe -s `
        -b $adminSession.CookieFile `
        -o (Join-Path $TempDir "deleted-get-body.json") `
        -w "%{http_code}" `
        $bugUrl
    Assert-Status "GET deleted Bug" $deletedGetStatus "404"

    Write-Host "[SUCCESS] Smoke check completed. Bug id=$($created.id)"
}
finally {
    Remove-Item $TempDir -Recurse -Force -ErrorAction SilentlyContinue
}
