# ADR 0001 Modular monolith before service split

## Context

Dự án phục vụ đồ án nhóm, có một codebase Spring MVC/JSP và nhiều feature cần triển khai song song. Tách microservice bây giờ sẽ tăng chi phí deploy, dữ liệu phân tán và thời gian tích hợp.

## Decision

Giữ một deployable Spring Boot nhưng phân ranh giới feature qua package, service API, ownership file và API contract. MariaDB giữ transaction nghiệp vụ. MongoDB/Milvus chỉ được dùng làm read model cho feature cần thiết.

## Consequences

Nhóm có thể phát triển theo nhánh feature, review và merge dần mà không cần hạ tầng phân tán. Một module không được chạm schema/API của module khác nếu chưa có migration và contract được review.
