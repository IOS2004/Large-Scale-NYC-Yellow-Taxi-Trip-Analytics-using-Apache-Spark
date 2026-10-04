# 🚕 Large-Scale NYC Yellow Taxi Trip Analytics 

This project analyzes the massive New York City yellow taxi trip dataset (hundreds of millions of records, 2017–2026) using an enterprise-grade Big Data pipeline. It leverages **Apache Spark**, **Delta Lake**, and **Spark MLlib** to clean data, perform complex spatial-temporal aggregations, and predict passenger tipping behaviors.

---

## 🏗️ Architecture & Technology Stack

The project has been modernized to reflect 2026 data engineering best practices:
- **Processing Engine:** Apache Spark 3.5.0 (Scala 2.13.12)
- **Storage Layer:** Delta Lake 3.0 (ACID compliance & Time Travel)
- **Configuration:** Typesafe Config (`application.conf`)
- **Machine Learning:** Spark MLlib (`RandomForestClassifier`)
- **Infrastructure:** Docker & Docker Compose
- **DevOps:** GitHub Actions CI/CD Pipeline

```mermaid
graph LR
    A[TLC S3 Bucket (Parquet)] --> B(Download Script)
    B --> C[Raw Data Directory]
    C --> D{Spark: Schema Fixer}
    D --> E{Spark: Cleaner & Processor}
    E --> F[(Delta Lake: Clean Data)]
    F --> G{Spark: Advanced Analytics}
    F --> H{Spark MLlib: Tip Prediction}
    G --> I[(Delta Lake: Aggregations)]
    I --> J[Jupyter Notebook: Visualization]
```

---

## 🚀 Getting Started (Docker Environment)

You don't need to install Spark locally. The entire environment is containerized.

1. **Download the Data:**
   Run the included PowerShell script to fetch the latest TLC datasets (2023-2026):
   ```powershell
   ./download_recent_data.ps1
   ```

2. **Spin up the Cluster:**
   ```bash
   docker-compose up -d
   ```
   This will start a Spark Master, a Spark Worker, and a Jupyter Notebook environment (accessible at `http://localhost:8888`).

---

## 🧠 Spark Jobs Overview

The pipeline consists of several distinct Spark jobs driven by `TLCAnalysisApp.scala`. You can run them by passing the `jobName` argument.

### 1. `schema_fixer`
Reconciles schema differences (e.g., `INT64` vs `DOUBLE` across different years) in the raw Parquet files and saves the unified dataset as a Delta table.

### 2. `data_cleaner_processor`
Filters out mathematically impossible trips (e.g., distances > 100 miles, negative passenger counts) and extracts temporal features (hour, day, weekend flags).

### 3. `advanced_analysis`
Utilizes **Window Functions** to compute complex metrics:
- Rolling 7-day average of taxi demand per borough.
- `RANK()` function to determine the top 3 busiest pickup zones per hour.
*Includes benchmarked performance comparisons between Standard Joins and Broadcast Joins.*

### 4. `machine_learning`
Trains a `RandomForestClassifier` using `VectorAssembler` to predict whether a passenger will leave a high tip (>20%) based on trip distance, duration, and time of day. Evaluated using Area Under ROC.

---

## 📊 Visualization
Once the `advanced_analysis` job has run, open the `notebooks/NYC_Taxi_Visualizations.ipynb` file in the provided Jupyter container to view interactive Seaborn/Plotly charts of the aggregated data.

---

## 🛠️ Configuration & CI/CD
All file paths and Spark configurations are centralized in `source/src/main/resources/application.conf`. 

This repository is equipped with a GitHub Actions workflow (`.github/workflows/scala-ci.yml`) that automatically compiles and validates the Scala codebase on every push or pull request to the `main` branch.