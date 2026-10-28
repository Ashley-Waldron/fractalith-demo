param(
    [string]$Url = "http://localhost:8080/shop/checkout",
    [int]$DelayMs = 100
)

Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$client.Timeout = [TimeSpan]::FromSeconds(5)

$ok = 0
$failed = 0
$byInstance = @{}
$n = 0

Write-Host "Sending checkouts to $Url (Ctrl+C to stop)"
while ($true) {
    $n++
    $body = '{"customerId":"c-' + $n + '","sku":"sku-1","quantity":1}'
    $content = New-Object System.Net.Http.StringContent($body, [Text.Encoding]::UTF8, "application/json")
    $instance = "?"
    $reason = $null
    try {
        $resp = $client.PostAsync($Url, $content).GetAwaiter().GetResult()
        $text = $resp.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $values = $null
        if ($resp.Headers.TryGetValues("X-Instance-Id", [ref]$values)) { $instance = $values | Select-Object -First 1 }
        if ($resp.IsSuccessStatusCode -and $text -match "checked out") {
            $ok++
            $byInstance[$instance] = 1 + [int]$byInstance[$instance]
        } else {
            $failed++
            $reason = "HTTP $([int]$resp.StatusCode): $text"
        }
    } catch {
        $failed++
        $reason = $_.Exception.Message
    }

    if ($reason) {
        Write-Host ("#{0} FAILED ({1}) {2}" -f $n, $instance, $reason) -ForegroundColor Red
    }
    if ($n % 10 -eq 0) {
        $split = ($byInstance.GetEnumerator() | Sort-Object Name | ForEach-Object { "$($_.Name)=$($_.Value)" }) -join " "
        Write-Host ("sent={0} ok={1} failed={2} | {3}" -f $n, $ok, $failed, $split) -ForegroundColor Green
    }
    Start-Sleep -Milliseconds $DelayMs
}
