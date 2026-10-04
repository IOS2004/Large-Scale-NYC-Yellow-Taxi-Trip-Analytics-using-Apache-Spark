import pandas as pd
import json

# Paths
base_dir = '/home/jovyan/work/data/processed/analytics'
rolling_avg_path = f'{base_dir}/rolling_7_day_avg'
top_zones_path = f'{base_dir}/top_3_zones_by_hour'

# Load Parquet
rolling_avg_df = pd.read_parquet(rolling_avg_path)
top_zones_df = pd.read_parquet(top_zones_path)

# 1. Format Rolling Avg Data
# We want: [{ date: '2025-10-01', Manhattan: 45000, Brooklyn: 15000, Queens: 12000 }, ...]
rolling_pivot = rolling_avg_df.pivot(index='date', columns='pickup_borough', values='rolling_7_day_avg').reset_index()
# Fill NaN with 0 and convert date to string
rolling_pivot = rolling_pivot.fillna(0)
rolling_pivot['date'] = rolling_pivot['date'].astype(str)
rolling_avg_json = rolling_pivot.to_dict(orient='records')

# 2. Format Top Zones Data
# We want: [{ hour: '08:00', 'JFK Airport': 1200, 'Midtown': 2500, ...}, ...]
top_zones_df['hour_str'] = top_zones_df['hour'].astype(str).str.zfill(2) + ":00"
# Pivot the top 3 zones into columns
top_zones_pivot = top_zones_df.pivot(index='hour_str', columns='pickup_zone', values='total_trips').reset_index()
top_zones_pivot = top_zones_pivot.fillna(0)
top_zones_json = top_zones_pivot.to_dict(orient='records')

final_data = {
    "rollingAvgData": rolling_avg_json,
    "topZonesData": top_zones_json
}

with open('/home/jovyan/work/notebooks/data.json', 'w') as f:
    json.dump(final_data, f)

print("Data exported to notebooks/data.json successfully!")
