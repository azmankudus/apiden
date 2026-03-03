#!/bin/bash
set -e

# API Call Sequence Bash Script

URL=http://localhost:8080/hello
METHOD=POST

#URL=http://localhost:8080/hello/error
#METHOD=POST

echo "=== Client API Call Sequence ==="

# 1. client - capture http headers
# Format must be Map<String, List<String>> to match ApiObject structure
CLIENT_HEADERS='{
  "content-type": ["application/json"],
  "accept": ["application/json"]
}'

# 2. client - create client trace id
if command -v uuidgen >/dev/null 2>&1; then
    TRACE_ID=$(uuidgen)
else
    # fallback if uuidgen is missing
    TRACE_ID=$(cat /proc/sys/kernel/random/uuid)
fi
echo "[Step 1-2] Generated traceid: $TRACE_ID"

# 3. client - capture request timestamp
REQ_TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")
echo "[Step 3] Captured request timestamp: $REQ_TIMESTAMP"

# 4. client - construct request body
BUSINESS_PAYLOAD='{"message": "Hello from Client"}'
echo "[Step 4] Constructed business payload: $BUSINESS_PAYLOAD"

# Construct the ApiEnvelope exactly as the schema requires
PAYLOAD=$(jq -n \
  --argjson headers "$CLIENT_HEADERS" \
  --arg traceid "$TRACE_ID" \
  --arg timestamp "$REQ_TIMESTAMP" \
  --argjson body "$BUSINESS_PAYLOAD" \
  '{
    client: {
      http: { headers: $headers },
      request: {
        traceid: $traceid,
        timestamp: $timestamp,
        body: $body
      }
    }
  }')

echo "[Step 5] Making API call to backend (POST /hello)"

START_TIME=$(date +%s%3N)

# 5. client - make api call to backend
RESPONSE=$(curl -s -X $METHOD $URL \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "$PAYLOAD")

# 12. client - capture timestamp and calculate duration
END_TIME=$(date +%s%3N)
RESP_TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")

# Calculate duration in seconds with 3 decimal places
DIFF_MS=$((END_TIME - START_TIME))
SECONDS=$(echo "scale=3; $DIFF_MS / 1000" | bc | sed 's/^\./0./')
ISO_DURATION="PT${SECONDS}S"

echo "[Step 12] Captured response timestamp ($RESP_TIMESTAMP) and duration ($ISO_DURATION)"

# Update the client-side response representation with the data we just calculated
FINAL_CLIENT_STATE=$(echo "$RESPONSE" | jq --arg rt "$RESP_TIMESTAMP" --arg dur "$ISO_DURATION" '
  .client.response = {
    "timestamp": $rt,
    "duration": $dur
  }
')

echo -e "\n=== Final Representation of API object on Client side ==="
if [ -n "$RESPONSE" ]; then
    echo "$FINAL_CLIENT_STATE" | jq .
else
    echo "No response from server. Is it running on port 8080?"
fi
