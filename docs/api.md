# API Documentation

## Base URL
`http://localhost:8080/api`

## Endpoints

### Health Check

**GET** `/health`

Returns a simple health check response.

**Response:**
- Status: `200 OK`
- Body: `"OK"` (plain text)

**Example:**
```bash
curl http://localhost:8080/api/health
```

---

### Process Words

**POST** `/bakken`

Processes an array of strings and returns an array of strings based on specific word matching rules.

**Request:**
- Content-Type: `application/json`
- Body: Array of strings

**Response:**
- Status: `200 OK`
- Content-Type: `application/json`
- Body: Array of strings

**Business Logic:**
- Searches for the words "aardappel" (9 letters) or "pieper" (6 letters) in the input array
- For each occurrence of these words, outputs the word length times the word "friet"
- Words are matched exactly (case-sensitive)
- Null values in the input array are ignored

**Examples:**

Input: `["aardappel"]`
```json
["friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet"]
```

Input: `["pieper"]`
```json
["friet", "friet", "friet", "friet", "friet", "friet"]
```

Input: `["aardappel", "pieper", "other"]`
```json
["friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet"]
```

Input: `["other", "words"]`
```json
[]
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/bakken \
  -H "Content-Type: application/json" \
  -d '["aardappel", "pieper"]'
```

---

### Process Snacks

**POST** `/frituren`

Accepts an array of snack objects (Frituurbaar) as JSON and generates "friet" strings based on the size of Pataten (aardappels) items.

**Request:**
- Content-Type: `application/json`
- Body: Array of snack objects (Frituurbaar)

**Snack Types:**
- `Pataten`: `{"size": <number>}` - The size determines how many "friet" strings are generated
- `Frikandellen`: `{"count": <number>}`
- `Kroketten`: `{"type": "<Krokettype>", "count": <number>}` where type is one of: KAAS, KALF, GARNALEN, KIP, GROENTE, GEZOND
- `Cervela`: `{"count": <number>}`
- `Bereklauw`: `{"count": <number>}`

**Response:**
- Status: `200 OK`
- Content-Type: `application/json`
- Body: Array of strings (only "friet" strings based on Pataten size)

**Business Logic:**
- Only Pataten (aardappels) items generate output
- For each Pataten item, generates `size` number of "friet" strings
- Other snack types (Frikandellen, Kroketten, Cervela, Bereklauw) are ignored in the output

**Examples:**

Input: `[{"size": 9}]` (Pataten with size 9)
```json
["friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet"]
```

Input: `[{"size": 6}]` (Pataten with size 6)
```json
["friet", "friet", "friet", "friet", "friet", "friet"]
```

Input: `[{"size": 9}, {"count": 3}]` (Pataten + Frikandellen)
```json
["friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet"]
```

Input: `[{"size": 5}, {"size": 4}]` (Multiple Pataten)
```json
["friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet", "friet"]
```

Input: `[{"count": 3}]` (Only Frikandellen, no Pataten)
```json
[]
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/frituren \
  -H "Content-Type: application/json" \
  -d '[{"size": 9}, {"count": 2}]'
```
