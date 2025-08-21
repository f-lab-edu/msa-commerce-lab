# Product Update API Documentation

## Overview

The Product Update API allows partial or complete updates to existing products in the system. It implements robust validation, business rule enforcement, and maintains data integrity through comprehensive checks.

## Endpoint

```
PUT /api/v1/products/{id}
```

## Features

- ✅ **Partial Updates**: Update only the fields you need to change
- ✅ **Validation**: Comprehensive input validation with detailed error messages
- ✅ **Business Rules**: SKU uniqueness, product name uniqueness, status validation
- ✅ **Inventory Management**: Update product inventory information alongside product data
- ✅ **Change Tracking**: Logs all changes for audit purposes
- ✅ **Cache Invalidation**: Prepared for future cache system integration
- ✅ **Error Handling**: Proper HTTP status codes and error responses

## Request Format

### Headers
```
Content-Type: application/json
```

### Path Parameters
- `id` (Long) - The product ID to update

### Request Body

All fields are optional - provide only the fields you want to update:

```json
{
  "categoryId": 1,
  "sku": "NEW-SKU-123",
  "name": "Updated Product Name",
  "description": "Updated product description",
  "shortDescription": "Updated short description",
  "brand": "Updated Brand",
  "model": "Updated Model",
  "price": 25000.00,
  "comparePrice": 30000.00,
  "costPrice": 20000.00,
  "weight": 2.5,
  "productAttributes": "{\"color\": \"red\", \"size\": \"large\"}",
  "visibility": "PUBLIC",
  "taxClass": "STANDARD",
  "metaTitle": "Updated Meta Title",
  "metaDescription": "Updated meta description for SEO",
  "searchKeywords": "updated keywords for search",
  "isFeatured": true,
  
  // Inventory fields
  "initialStock": 100,
  "lowStockThreshold": 10,
  "isTrackingEnabled": true,
  "isBackorderAllowed": false,
  "minOrderQuantity": 1,
  "maxOrderQuantity": 50,
  "reorderPoint": 15,
  "reorderQuantity": 100,
  "locationCode": "WAREHOUSE-A"
}
```

### Field Validation Rules

| Field | Type | Constraints |
|-------|------|-------------|
| `categoryId` | Long | Must be positive |
| `sku` | String | Max 100 characters, must be unique |
| `name` | String | Max 255 characters, must be unique |
| `description` | String | Max 5000 characters |
| `shortDescription` | String | Max 500 characters |
| `brand` | String | Max 100 characters |
| `model` | String | Max 100 characters |
| `price` | BigDecimal | 0.01 - 99,999,999.99, max 2 decimal places |
| `comparePrice` | BigDecimal | 0.01+, max 2 decimal places |
| `costPrice` | BigDecimal | 0.01+, max 2 decimal places |
| `weight` | BigDecimal | 0.01+, max 2 decimal places |
| `visibility` | String | Max 20 characters |
| `taxClass` | String | Max 50 characters |
| `metaTitle` | String | Max 255 characters |
| `metaDescription` | String | Max 500 characters |
| `searchKeywords` | String | Max 1000 characters |
| `initialStock` | Integer | 0+ |
| `lowStockThreshold` | Integer | 0+ |
| `minOrderQuantity` | Integer | 1+ |
| `maxOrderQuantity` | Integer | 1+, must be ≥ minOrderQuantity |
| `reorderPoint` | Integer | 0+ |
| `reorderQuantity` | Integer | 0+ |
| `locationCode` | String | Max 50 characters |

## Response Format

### Success Response (200 OK)

```json
{
  "id": 1,
  "categoryId": 1,
  "sku": "NEW-SKU-123",
  "name": "Updated Product Name",
  "description": "Updated product description",
  "shortDescription": "Updated short description",
  "brand": "Updated Brand",
  "model": "Updated Model",
  "price": 25000.00,
  "comparePrice": 30000.00,
  "costPrice": 20000.00,
  "weight": 2.5,
  "productAttributes": "{\"color\": \"red\", \"size\": \"large\"}",
  "status": "ACTIVE",
  "visibility": "PUBLIC",
  "taxClass": "STANDARD",
  "metaTitle": "Updated Meta Title",
  "metaDescription": "Updated meta description for SEO",
  "searchKeywords": "updated keywords for search",
  "isFeatured": true,
  "createdAt": "2024-08-20T10:30:00",
  "updatedAt": "2024-08-22T03:45:00"
}
```

## Error Responses

### 400 Bad Request
Returned when validation fails or no fields are provided for update.

```json
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2024-08-22T03:45:00",
  "path": "/api/v1/products/1"
}
```

### 404 Not Found
Returned when the product with the specified ID doesn't exist.

```json
{
  "message": "Product not found",
  "status": 404,
  "timestamp": "2024-08-22T03:45:00",
  "path": "/api/v1/products/999"
}
```

### 409 Conflict
Returned when business rules are violated (e.g., duplicate SKU or name).

```json
{
  "message": "SKU already exists: NEW-SKU-123",
  "status": 409,
  "timestamp": "2024-08-22T03:45:00",
  "path": "/api/v1/products/1"
}
```

### 500 Internal Server Error
Returned when an unexpected server error occurs.

```json
{
  "message": "Internal server error",
  "status": 500,
  "timestamp": "2024-08-22T03:45:00",
  "path": "/api/v1/products/1"
}
```

## Business Rules

1. **Product Status**: Only products with status other than `ARCHIVED` can be updated
2. **SKU Uniqueness**: SKU must be unique across all products (when changing SKU)
3. **Name Uniqueness**: Product name must be unique across all products (when changing name)
4. **Partial Updates**: Only provided fields are updated; null fields are ignored
5. **Inventory Integration**: Inventory updates are processed alongside product updates
6. **Change Tracking**: All changes are logged for audit purposes
7. **Cache Invalidation**: Product cache is invalidated after successful updates

## Usage Examples

### Update Product Name and Price Only

```bash
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Product Name",
    "price": 15000.00
  }'
```

### Update Product with Inventory

```bash
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product",
    "price": 20000.00,
    "initialStock": 50,
    "lowStockThreshold": 5,
    "isTrackingEnabled": true
  }'
```

### Update SEO Fields

```bash
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "metaTitle": "New SEO Title",
    "metaDescription": "New SEO description for better search rankings",
    "searchKeywords": "keyword1, keyword2, keyword3"
  }'
```

## Implementation Details

### Architecture
- **Controller Layer**: `ProductController` handles HTTP requests and responses
- **Application Layer**: `ProductUpdateService` implements business logic
- **Domain Layer**: `Product` and `ProductInventory` domain models with update methods
- **Infrastructure Layer**: Repository implementations for data persistence

### Key Components
- `ProductUpdateRequest`: DTO for request validation
- `ProductUpdateCommand`: Command object for application layer
- `ProductUpdateUseCase`: Interface defining update operations
- `ProductUpdateService`: Service implementing update business logic

### Testing
- Comprehensive unit tests for all layers
- Integration tests for web layer
- Domain model tests for update logic
- Command validation tests

## Monitoring and Observability

The API includes comprehensive logging:
- Request/response logging with correlation IDs
- Business operation logging (cache invalidation, inventory updates)
- Error logging with full context
- Performance metrics tracking

## Future Enhancements

- **Cache Integration**: Full cache system with Redis/Hazelcast
- **Event Sourcing**: Domain events for product updates
- **Audit Trail**: Detailed change history table
- **Permissions**: Role-based access control for product updates
- **Versioning**: Product version management
- **Async Processing**: Background processing for complex updates