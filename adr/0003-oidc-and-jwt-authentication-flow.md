# 0003. OIDC and JWT Authentication Flow

- **Date**: 2025-06-20
- **Status**: Accepted

## Context

The application serves as a Backend-for-Frontend (BFF), requiring two distinct security mechanisms:
1.  A user-friendly, browser-based method for users to authenticate themselves.
2.  A stateless, token-based method for a client application (e.g., a Single Page Application) to securely access the REST API.

We need a solution that avoids storing user credentials while leveraging a trusted identity provider. The API must be protected and decoupled from the initial web-based authentication session.

## Decision

We will implement a two-part authentication and authorization flow:

1.  **Initial User Authentication (OIDC/OAuth2):** We will use Spring Security's `oauth2Login` functionality with Google as the OpenID Connect (OIDC) provider. This flow handles the standard browser redirect to Google for login. Upon successful authentication, Spring Security creates a stateful, server-side session (identified by a `JSESSIONID` cookie). This part of the flow is only for establishing the user's identity with the BFF.

2.  **API Authorization (JWT):** After a user has a valid session, the client application must obtain a JSON Web Token (JWT) to interact with the protected API.
    - We will expose a `/auth/token` endpoint, which is protected by the session cookie.
    - A client with a valid session can make a `POST` request to this endpoint.
    - The BFF will then generate a signed, stateless JWT containing user claims (like email) and an expiration time.
    - For all subsequent requests to protected API endpoints (under `/api/**`), the client must include this JWT in the `Authorization: Bearer <token>` header.
    - A custom `JwtAuthenticationFilter` will intercept these requests, validate the token, and establish a security context, allowing access without relying on the `JSESSIONID` cookie.

This hybrid approach leverages the strengths of both stateful (OIDC session) and stateless (JWT) patterns, providing a secure and flexible architecture for a BFF.

## Consequences

### Positive:
- **Separation of Concerns**: The user login flow is cleanly separated from the API authorization mechanism. The API remains stateless and independent of web sessions.
- **Standard-Based Security**: The solution relies on well-known standards (OAuth2, OIDC, JWT), which improves security and interoperability.
- **Improved User Experience**: Delegates user authentication to a trusted external provider (Google), so users don't need to create or manage new credentials for our application.
- **Client Flexibility**: Any client that can handle an OAuth2 redirect flow and manage a JWT can securely interact with the API.

### Negative:
- **Increased Complexity**: The flow is more complex than a single security mechanism. The client application has the added responsibility of requesting the JWT after login and managing its lifecycle (storage, refresh).
- **Token Issuance Dependency**: The ability to get a JWT is dependent on having a valid web session first. This tightly couples the token issuance to the OIDC login flow.
- **Token Management on Client**: The client is responsible for securely storing the JWT and ensuring it is not exposed to cross-site scripting (XSS) attacks.
