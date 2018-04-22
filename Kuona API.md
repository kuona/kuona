# API

## Requests

### **GET** - /api/status

#### Description
Returns the current status of the API and it's dependencies.

#### CURL

```sh
curl -X GET "http:///api/status"
```

### **GET** - /api

#### CURL

```sh
curl -X GET "http:///api"
```

### **GET** - /api/query/sources

#### CURL

```sh
curl -X GET "http:///api/query/sources"
```

### **GET** - /api/repositories/count

#### CURL

```sh
curl -X GET "http:///api/repositories/count"
```

### **GET** - /api/build/tools

#### CURL

```sh
curl -X GET "http:///api/build/tools"
```

### **GET** - /api/environments

#### CURL

```sh
curl -X GET "http:///api/environments"
```

### **GET** - /api/valuestreams

#### CURL

```sh
curl -X GET "http:///api/valuestreams"
```

### **POST** - /

#### CURL

```sh
curl -X POST "http://localhost:3000/" \
    -H "Content-Type: text/plain; charset=utf-8" \
    --data-raw "$body"
```

#### Header Parameters

- **Content-Type** should respect the following schema:

```
{
  "type": "string",
  "enum": [
    "text/plain; charset=utf-8"
  ],
  "default": "text/plain; charset=utf-8"
}
```

#### Body Parameters

- **body** should respect the following schema:

```
{
  "type": "string",
  "default": "{\n  \"my_object\": {\n    \"my_value\": \"value\"\n  }\n}"
}
```

## References

