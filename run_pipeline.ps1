$ErrorActionPreference = "Stop"

Write-Host "🚕 Starting NYC Yellow Taxi Analytics Pipeline..." -ForegroundColor Cyan

$jarPath = "/home/jovyan/work/target/scala-2.12/nyc-yellow-taxi-analysis_2.12-0.1.0-SNAPSHOT.jar"
$packages = "io.delta:delta-spark_2.12:3.0.0,com.typesafe:config:1.4.3"

Write-Host "`n[1/4] Running Schema Fixer..." -ForegroundColor Yellow
docker exec nyc-yellow-taxi-analysis-master-jupyter-1 spark-submit --driver-memory 4g --conf "spark.sql.files.ignoreCorruptFiles=true" --packages $packages --class jobs.TLCAnalysisApp $jarPath jobName=schema_fixer inputPath=/home/jovyan/work/data/raw outputPath=/home/jovyan/work/data/processed/schema_fixed taxiZonePath=/home/jovyan/work/data/taxi_zones.csv
if ($LASTEXITCODE -ne 0) { Write-Host "Schema Fixer failed!" -ForegroundColor Red; exit $LASTEXITCODE }

Write-Host "`n[2/4] Running Data Cleaner & Processor..." -ForegroundColor Yellow
docker exec nyc-yellow-taxi-analysis-master-jupyter-1 spark-submit --driver-memory 4g --conf "spark.sql.files.ignoreCorruptFiles=true" --packages $packages --class jobs.TLCAnalysisApp $jarPath jobName=data_cleaner_processor inputPath=/home/jovyan/work/data/processed/schema_fixed outputPath=/home/jovyan/work/data/processed/cleaned taxiZonePath=/home/jovyan/work/data/taxi_zones.csv
if ($LASTEXITCODE -ne 0) { Write-Host "Data Cleaner failed!" -ForegroundColor Red; exit $LASTEXITCODE }

Write-Host "`n[3/4] Running Advanced Analytics..." -ForegroundColor Yellow
docker exec nyc-yellow-taxi-analysis-master-jupyter-1 spark-submit --driver-memory 4g --conf "spark.sql.files.ignoreCorruptFiles=true" --packages $packages --class jobs.TLCAnalysisApp $jarPath jobName=advanced_analysis inputPath=/home/jovyan/work/data/processed/cleaned outputPath=/home/jovyan/work/data/processed/analytics taxiZonePath=/home/jovyan/work/data/taxi_zones.csv
if ($LASTEXITCODE -ne 0) { Write-Host "Advanced Analytics failed!" -ForegroundColor Red; exit $LASTEXITCODE }

Write-Host "`n[4/4] Running Machine Learning Tip Predictor..." -ForegroundColor Yellow
docker exec nyc-yellow-taxi-analysis-master-jupyter-1 spark-submit --driver-memory 4g --conf "spark.sql.files.ignoreCorruptFiles=true" --packages $packages --class jobs.TLCAnalysisApp $jarPath jobName=machine_learning inputPath=/home/jovyan/work/data/processed/cleaned outputPath=/home/jovyan/work/data/processed/ml taxiZonePath=/home/jovyan/work/data/taxi_zones.csv
if ($LASTEXITCODE -ne 0) { Write-Host "Machine Learning failed!" -ForegroundColor Red; exit $LASTEXITCODE }

Write-Host "`n✅ Pipeline execution completed successfully!" -ForegroundColor Green
