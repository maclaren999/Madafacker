# Backend Content Moderation Specification

## Overview

This document specifies the backend implementation requirements for content moderation in the Madafacker application. The system implements
a dual-mode approach with different filtering thresholds for Shine and Shadow modes.

## 1. Architecture Overview

### 1.1 Moderation Flow

```
Client Request → Content Validation → OpenAI Moderation API → Response Processing → Database Storage
```

### 1.2 Mode-Based Filtering

- **Shine Mode (light)**: Comprehensive filtering using all OpenAI moderation categories
- **Shadow Mode (dark)**: Minimal filtering focusing only on illegal content

## 2. OpenAI Moderation API Integration

### 2.1 API Configuration

- **Endpoint**: `https://api.openai.com/v1/moderations`
- **Authentication**: Bearer token with OpenAI API key
- **Rate Limits**: 1,000 requests per minute (free tier)
- **Cost**: Free for moderation API

### 2.2 Request Format

```json
{
  "input": "Text content to moderate",
  "model": "text-moderation-latest"
}
```

### 2.3 Response Format

```json
{
  "id": "modr-XXXXX",
  "model": "text-moderation-005",
  "results": [
    {
      "flagged": false,
      "categories": {
        "sexual": false,
        "hate": false,
        "harassment": false,
        "self-harm": false,
        "sexual/minors": false,
        "hate/threatening": false,
        "violence/graphic": false,
        "self-harm/intent": false,
        "self-harm/instructions": false,
        "harassment/threatening": false,
        "violence": false
      },
      "category_scores": {
        "sexual": 0.0001,
        "hate": 0.0001,
        "harassment": 0.0002,
        "self-harm": 0.0001,
        "sexual/minors": 0.0001,
        "hate/threatening": 0.0001,
        "violence/graphic": 0.0001,
        "self-harm/intent": 0.0001,
        "self-harm/instructions": 0.0001,
        "harassment/threatening": 0.0001,
        "violence": 0.0001
      }
    }
  ]
}
```

## 3. Filtering Thresholds

### 3.1 Shine Mode (Comprehensive Filtering)

Apply strict thresholds for all categories:

| Category               | Threshold | Action |
|------------------------|-----------|--------|
| sexual                 | 0.6       | Reject |
| hate                   | 0.6       | Reject |
| harassment             | 0.6       | Reject |
| self-harm              | 0.7       | Reject |
| sexual/minors          | 0.1       | Reject |
| hate/threatening       | 0.5       | Reject |
| violence/graphic       | 0.7       | Reject |
| self-harm/intent       | 0.6       | Reject |
| self-harm/instructions | 0.5       | Reject |
| harassment/threatening | 0.5       | Reject |
| violence               | 0.6       | Reject |

### 3.2 Shadow Mode (Minimal Filtering)

Apply filtering only for illegal content:

| Category               | Threshold | Action |
|------------------------|-----------|--------|
| sexual/minors          | 0.3       | Reject |
| hate/threatening       | 0.8       | Reject |
| violence/graphic       | 0.9       | Reject |
| self-harm/instructions | 0.8       | Reject |

## 4. API Endpoint Modifications

### 4.1 Enhanced Message Creation Endpoint

**Endpoint**: `POST /api/message`

**Request Body**:

```json
{
  "body": "Message content",
  "mode": "light"
  |
  "dark"
}
```

**Success Response** (201 Created):

```json
{
  "id": "message_id",
  "body": "Message content",
  "mode": "light",
  "public": true,
  "wasSent": false,
  "authorId": "user_id",
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z",
  "parentId": null
}
```

**Moderation Rejection Response** (422 Unprocessable Entity):

```json
{
  "error": "Content violates community guidelines",
  "code": "CONTENT_MODERATION_FAILED",
  "violationType": "harassment",
  "suggestion": "Please keep it positive or switch to Shadow mode for uncensored expression!",
  "details": "Content contains harassment that exceeds acceptable thresholds"
}
```

### 4.2 Optional: Dedicated Moderation Endpoint

**Endpoint**: `POST /api/moderation/check`

**Request Body**:

```json
{
  "content": "Text to moderate",
  "mode": "light"
  |
  "dark",
  "context": "message"
  |
  "reply"
}
```

**Response**:

```json
{
  "allowed": true,
  "violationType": null,
  "confidence": 0.1,
  "reason": null,
  "suggestion": null
}
```

## 5. Error Response Codes

| HTTP Code | Error Code                | Description                 | User Message                                 |
|-----------|---------------------------|-----------------------------|----------------------------------------------|
| 422       | CONTENT_MODERATION_FAILED | Content violates guidelines | Mode-specific suggestion                     |
| 429       | RATE_LIMIT_EXCEEDED       | Too many requests           | "Please wait before sending another message" |
| 500       | MODERATION_SERVICE_ERROR  | OpenAI API error            | "Moderation service temporarily unavailable" |
| 503       | SERVICE_UNAVAILABLE       | System maintenance          | "Service temporarily unavailable"            |

## 6. Implementation Guidelines

### 6.1 Error Handling

1. **Graceful Degradation**: If OpenAI API is unavailable, log the error and allow content through with warning
2. **Retry Logic**: Implement exponential backoff for transient OpenAI API errors
3. **Fallback**: Consider implementing basic keyword filtering as backup

### 6.2 Performance Considerations

1. **Async Processing**: Make moderation calls asynchronous where possible
2. **Caching**: Cache moderation results for identical content (with TTL)
3. **Batching**: Consider batching multiple moderation requests if volume is high

### 6.3 Logging and Monitoring

1. **Audit Trail**: Log all moderation decisions with content hash (not full content)
2. **Metrics**: Track moderation rejection rates by mode and category
3. **Alerts**: Set up alerts for high rejection rates or API failures

### 6.4 Privacy and Security

1. **Data Retention**: Do not store rejected content longer than necessary for debugging
2. **Content Hashing**: Use content hashes for logging instead of full text
3. **API Key Security**: Secure OpenAI API key in environment variables

## 7. Testing Requirements

### 7.1 Unit Tests

- Test moderation logic for both modes
- Test threshold calculations
- Test error handling scenarios

### 7.2 Integration Tests

- Test OpenAI API integration
- Test end-to-end message creation flow
- Test error response formatting

### 7.3 Load Testing

- Test moderation performance under load
- Verify rate limiting behavior
- Test failover scenarios

## 8. Deployment Considerations

### 8.1 Environment Variables

```
OPENAI_API_KEY=your_openai_api_key
MODERATION_ENABLED=true
MODERATION_FALLBACK_ALLOW=false
```

### 8.2 Feature Flags

- `ENABLE_SHINE_MODERATION`: Enable/disable Shine mode filtering
- `ENABLE_SHADOW_MODERATION`: Enable/disable Shadow mode filtering
- `MODERATION_STRICT_MODE`: Use stricter thresholds for testing

This specification provides a complete implementation guide for backend content moderation that aligns with the client-side filtering
already implemented in the Android application.
