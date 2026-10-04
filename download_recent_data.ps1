# download_recent_data.ps1
param (
    [string]$outputDir = "data\raw\yellow_taxi",
    [int[]]$years = @(2023, 2024, 2025)
)

if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir
}

$months = 1..12 | ForEach-Object { "{0:D2}" -f $_ }
$baseUrl = "https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata"

foreach ($year in $years) {
    Write-Host "Fetching data for year $year..."
    foreach ($month in $months) {
        $fileName = "yellow_tripdata_${year}-${month}.parquet"
        $url = "${baseUrl}_${year}-${month}.parquet"
        $outputFile = Join-Path $outputDir $fileName

        if (-not (Test-Path $outputFile)) {
            Write-Host "Downloading $fileName..."
            try {
                Invoke-WebRequest -Uri $url -OutFile $outputFile
            } catch {
                Write-Host "Warning: Could not download $fileName. It might not be available yet." -ForegroundColor Yellow
            }
        } else {
            Write-Host "$fileName already exists. Skipping."
        }
    }
}

Write-Host "Download complete!"
