$ErrorActionPreference = "Stop"

$url = "http://localhost:8098"

docker compose up -d --build

$deadline = (Get-Date).AddSeconds(90)
do {
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
            Start-Process $url
            Write-Host "Application is running at $url"
            exit 0
        }
    } catch {
        Start-Sleep -Seconds 2
    }
} while ((Get-Date) -lt $deadline)

Write-Error "Application did not become available at $url within 90 seconds. Check logs with: docker compose logs -f"
