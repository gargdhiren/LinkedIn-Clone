# Post It

## Overview

**Post It** is a Spring Boot microservices project built as a social-network-like platform for posting, liking, and managing connections. It uses:

- Spring Cloud Gateway for API routing and authentication
- Kafka for asynchronous event-driven communication
- Eureka service discovery
- PostgreSQL for relational data storage
- Neo4j for connection graph / relationship storage
- Spring Boot services for user, posts, connections, and notifications

## Architecture

The system is composed of these main modules:

- `api-gateway` — route requests and validate JWT tokens
- `discovery-server` — Eureka registry for service discovery
- `user-service` — user registration and login
- `posts-service` — create posts, view posts, like/unlike posts
- `connections-service` — manage connection requests and first-degree connections
- `notification-service` — consume Kafka events and save notifications
- `common-events` — shared Kafka event DTOs used across services
- `common` — shared code and common configuration used by multiple services

## Infrastructure

The project includes `docker-compose.yml` to launch infrastructure services:

- `postgres` at `localhost:5432`
- `neo4j` at `localhost:7474` and `localhost:7687`
- `kafka` at `localhost:8090`
- `kafka-ui` at `localhost:8086`

### Databases

The included `init.sql` creates:

- `postsDB`
- `userDB`
- `notificationDB`

## How Kafka Works

Kafka is the asynchronous event bus connecting services.

### Topics

- `user-created-event`
- `post-created-topic`
- `post-liked-topic`
- `send-connection-request-topic`
- `accept-connection-request-topic`

### Topic configuration

Each topic is created with:

- partitions: `3`
- replication factor: `1`

### Event flow

- `user-service` publishes `user-created-event` after signup
- `connections-service` consumes `user-created-event` and adds the user to the graph
- `posts-service` publishes `post-created-topic` when a post is created
- `notification-service` consumes `post-created-topic` and notifies first-degree connections
- `posts-service` publishes `post-liked-topic` when a post is liked
- `notification-service` consumes `post-liked-topic` and notifies the post creator
- `connections-service` publishes `send-connection-request-topic` when a request is sent
- `connections-service` publishes `accept-connection-request-topic` when a request is accepted
- `notification-service` consumes both connection topics and saves notifications

## Service Details

### API Gateway (`api-gateway`)

The gateway runs at `localhost:8080` and provides routing and authentication.

#### Routes

- `/api/v1/users/**` -> `USER-SERVICE`
- `/api/v1/posts/**` -> `POSTS-SERVICE`
- `/api/v1/connections/**` -> `CONNECTIONS-SERVICE`

#### Behavior

- `StripPrefix=2` removes `/api/v1` before forwarding
- `AuthenticationFilter` validates `Authorization: Bearer <token>` for protected routes
- After validation, it injects `X-User-Id` into forwarded requests

### User Service (`user-service`)

Handles user registration and login.

#### APIs

- `POST /auth/signup`
  - Payload: `name`, `email`, `password`
  - Stores new user in `user-service` database
  - Publishes `user-created-event` to Kafka
  - Returns created `UserDto`

- `POST /auth/login`
  - Payload: `email`, `password`
  - Validates credentials
  - Returns JWT token

#### Tasks

- validate unique emails
- hash passwords before saving
- generate JWT tokens
- publish user creation events to Kafka

### Connections Service (`connections-service`)

Manages connection requests and first-degree connection lists.

#### APIs

- `GET /core/first-degree`
  - Returns first-degree connections for current user

- `POST /core/request/{userId}`
  - Sends a connection request to another user
  - Publishes `send-connection-request-topic`

- `POST /core/accept/{userId}`
  - Accepts a pending connection request from the specified user
  - Publishes `accept-connection-request-topic`

- `POST /core/reject/{userId}`
  - Rejects a pending connection request

#### Tasks

- persist user nodes and relationships in Neo4j
- determine whether requests already exist
- prevent duplicate or self-requests
- publish connection-related Kafka events
- expose first-degree connections

### Posts Service (`posts-service`)

Handles creating and retrieving posts, plus likes.

#### APIs

- `POST /core`
  - Payload: `content`
  - Creates a post for the authenticated user
  - Publishes `post-created-topic`
  - Returns `PostDto`

- `GET /core/{postId}`
  - Returns a single post by ID

- `GET /core/users/{userId}/allPosts`
  - Returns all posts created by a user

- `POST /likes/{postId}`
  - Likes the specified post
  - Publishes `post-liked-topic`

- `DELETE /likes/{postId}`
  - Unlikes the specified post

#### Tasks

- save posts in the post repository
- fetch first-degree connections from `connections-service`
- send post and like events to Kafka
- use `X-User-Id` from gateway for authenticated actions

### Notification Service (`notification-service`)

Event-driven service that creates notifications based on Kafka events.

#### Behavior

No public REST controllers are defined for notifications in this codebase.
It consumes events and saves notifications internally.

#### Kafka consumers

- `@KafkaListener(topics = "post-created-topic")`
  - sends notifications to first-degree connections of the post creator

- `@KafkaListener(topics = "post-liked-topic")`
  - sends a notification to the post creator

- `@KafkaListener(topics = "send-connection-request-topic")`
  - notifies the receiver of a new connection request

- `@KafkaListener(topics = "accept-connection-request-topic")`
  - notifies the sender that their request was accepted

#### Tasks

- query first-degree connections from `connections-service`
- save notification records to the notification database
- keep event handling decoupled from user interaction

### Discovery Server (`discovery-server`)

Runs Eureka service registry at `http://localhost:8761/eureka`.

- registers `USER-SERVICE`, `POSTS-SERVICE`, `CONNECTIONS-SERVICE`, and `API-GATEWAY`
- enables load-balanced routing by service name

## Shared Event Model (`common-events`)

Shared Kafka DTO classes define the contract between services.

- `UserCreatedEvent { userId, name }`
- `PostCreatedEvent { creatorId, content, postId }`
- `PostLikedEvent { postId, creatorId, likedByUserId }`
- `SendConnectionRequestEvent { senderId, receiverId }`
- `AcceptConnectionRequestEvent { senderId, receiverId }`

## Request Flow Examples

### Signup flow

1. Client calls `POST /api/v1/users/auth/signup`
2. `api-gateway` routes to `user-service`
3. `user-service` saves user and publishes `user-created-event`
4. `connections-service` consumes that event and creates the graph node

### Login flow

1. Client calls `POST /api/v1/users/auth/login`
2. `user-service` returns JWT
3. Client includes `Authorization: Bearer <token>` on protected calls

### Create post flow

1. Client calls `POST /api/v1/posts/core` with `content`
2. `api-gateway` validates JWT and injects `X-User-Id`
3. `posts-service` saves the post and publishes `post-created-topic`
4. `notification-service` consumes the event and notifies connections

### Like post flow

1. Client calls `POST /api/v1/posts/likes/{postId}`
2. `posts-service` saves the like and publishes `post-liked-topic`
3. `notification-service` sends a creator notification

### Connection request flow

1. Client calls `POST /api/v1/connections/core/request/{userId}`
2. `connections-service` saves the request and publishes `send-connection-request-topic`

### Accept connection flow

1. Client calls `POST /api/v1/connections/core/accept/{userId}`
2. `connections-service` publishes `accept-connection-request-topic`
3. `notification-service` notifies the original sender

## Important Notes

- `/api/v1/users/auth/**` endpoints do not require gateway JWT auth
- `/api/v1/posts/**` and `/api/v1/connections/**` require `Authorization: Bearer <token>`
- The gateway forwards `X-User-Id` from the validated token to downstream services
- `posts-service` and `connections-service` use interceptors to populate `UserContextHolder`

## Running the Project

1. Start infrastructure:
   ```bash
   docker-compose up -d
   ```

2. Start `discovery-server`:
   ```bash
   cd discovery-server
   ./mvnw spring-boot:run
   ```

3. Start `user-service`:
   ```bash
   cd user-service
   ./mvnw spring-boot:run
   ```

4. Start `connections-service`:
   ```bash
   cd connections-service
   ./mvnw spring-boot:run
   ```

5. Start `posts-service`:
   ```bash
   cd posts-service
   ./mvnw spring-boot:run
   ```

6. Start `notification-service`:
   ```bash
   cd notification-service
   ./mvnw spring-boot:run
   ```

7. Start `api-gateway`:
   ```bash
   cd api-gateway
   ./mvnw spring-boot:run
   ```

## Ports Summary

- API Gateway: `8080`
- Kafka UI: `8086`
- Kafka: `8090`
- PostgreSQL: `5432`
- Neo4j HTTP: `7474`
- Neo4j Bolt: `7687`
- Eureka: `8761`
