# Whiplash Backend - Specific Class Index

## Overview
This document provides a comprehensive index of all classes in the Whiplash backend application, organized by functional domains. Each class is documented with its primary role, key functionality, and relationships.

---

## 1. Application Entry Point

### Core Application
- **`WhiplashApplication`** - Main Spring Boot application class
  - Loads environment configuration from .env file
  - Enables JPA auditing and scheduling
  - Configures application startup

---

## 2. User Domain

### Core User Entities
- **`User`** - Central user entity representing system users
  - Stores user profile information (name, age, email, password)
  - Manages authentication data (kakaoId, socialProvider)
  - Contains user status and role management
  - Links to investor profile for investment-specific data

- **`InvestorProfile`** - Investment-specific user profile
  - Stores investment preferences (goal, risk tolerance, experience level)
  - Manages age range and investment categories of interest
  - One-to-one relationship with User

### User Enums
- **`Role`** - User permission levels (USER, ADMIN)
- **`UserStatus`** - Account status (PENDING, ACTIVE, INACTIVE)
- **`SocialProvider`** - Social login providers (KAKAO)

### Investment Profile Enums
- **`AgeRange`** - Age categorization for investment advice
- **`InvestmentLevel`** - Investment experience levels
- **`InvestmentGoal`** - Investment objectives (RETIREMENT, GROWTH, etc.)
- **`RiskTolerance`** - Risk appetite levels (CONSERVATIVE, MODERATE, AGGRESSIVE)

### User Keyword System
- **`UserKeyword`** - User's interest keywords for content personalization
  - Links users to specific keywords for article filtering

### User Services
- **`UserService`** - Core user business logic
  - User profile management and updates
  - User registration and activation

### User Controllers
- **`UserController`** - REST API endpoints for user operations
  - User profile CRUD operations
  - Keyword management endpoints

### User DTOs
- **`UserCreateDTO`** - User registration request data
- **`UserModifyRequestDTO`** - User profile update request
- **`KakaoUserInfoResponseDTO`** - Kakao API response mapping
- **`KakaoTokenResponseDTO`** - Kakao token response
- **`UserKeywordCreateRequest`** - Keyword creation request

### User Repositories
- **`UserRepository`** - User data access layer
- **`UserKeywordRepository`** - User keyword data access

---

## 3. Authentication & Security

### Authentication Services
- **`AuthService`** - Core authentication business logic
  - User registration, login, logout
  - JWT token generation and validation
  - Social login integration

- **`KakaoAuthService`** - Kakao OAuth integration
  - Kakao API token exchange
  - User information retrieval from Kakao

- **`RefreshTokenService`** - JWT refresh token management
  - Token storage, validation, and cleanup

### Security Configuration
- **`SecurityConfig`** - Spring Security configuration
  - Authentication and authorization setup
  - JWT filter configuration

- **`JwtTokenProvider`** - JWT token operations
  - Token generation, validation, and parsing
  - Claims extraction and verification

- **`JwtAuthenticationFilter`** - JWT authentication filter
  - Request authentication via JWT tokens

- **`CustomUserDetailService`** - Spring Security user details service
  - User authentication data loading

### Authentication Controllers
- **`AuthController`** - Authentication REST endpoints
  - Login, logout, token refresh endpoints

- **`KakaoTestController`** - Kakao integration testing endpoints

### Authentication DTOs
- **`LoginRequestDTO`** - Login request data
- **`TokenResponseDTO`** - Authentication response with tokens
- **`TokenRefreshRequestDTO`** - Token refresh request
- **`AuthResponse`** - General authentication response

---

## 4. Article Management System

### Article Entities
- **`Article`** (MongoDB Document) - News article storage
  - Article metadata (title, press, publishedAt)
  - Category classification for content organization

- **`SummarizedArticleIndex`** (JPA Entity) - Article summarization index
  - Tracks article processing status
  - Links MongoDB articles to relational data

- **`UserArticleAssignment`** - User-article relationship
  - Manages which articles are assigned to which users
  - Supports personalized content delivery

### Article Enums
- **`Category`** - Article categories (ECONOMY, POLITICS, TECHNOLOGY, etc.)

### Article Services
- **`ArticleSummarizationService`** - Article processing service
  - Handles article summarization requests
  - Manages processing job tracking

- **`UserArticleAssignmentService`** - Article assignment logic
  - Assigns relevant articles to users based on preferences

### Article Repositories
- **`ArticleRepository`** - MongoDB article data access
- **`SummarizedArticleIndexRepository`** - JPA article index data access
- **`UserArticleAssignmentRepository`** - User-article assignment data access

---

## 5. Portfolio Management System

### Portfolio Entities
- **`Portfolio`** - User investment portfolio
- **`Asset`** - Individual assets in portfolios
- **`AssetType`** - Asset classification enum

### Portfolio Domain Objects
- **`Goal`** - Investment goals and targets
- **`RebalancePlan`** - Portfolio rebalancing strategies
- **`RebalanceAction`** - Specific rebalancing actions
- **`RiskResponsePlan`** - Risk management strategies
- **`RiskAction`** - Risk mitigation actions
- **`SafetyAnalysisResult`** - Portfolio safety analysis results

### Portfolio Services

#### Rebalancing
- **`RebalanceService`** - Portfolio rebalancing orchestration
- **`RebalanceStrategy`** - Rebalancing strategy interface
- **`RebalanceStrategyV1`** - Specific rebalancing implementation

#### Risk Management
- **`RiskResponseService`** - Risk management orchestration
- **`RiskResponseStrategy`** - Risk response strategy interface
- **`RiskResponseStrategyV1`** - Specific risk response implementation

#### Safety Analysis
- **`SafetyAnalysisService`** - Portfolio safety evaluation
- **`SafetyAnalysisStrategy`** - Safety analysis strategy interface
- **`SafetyAnalysisStrategyV1`** - Specific safety analysis implementation

#### Visualization
- **`VisualizationService`** - Portfolio data visualization
  - Chart generation and data formatting for UI

---

## 6. Trading System

### Trade Entities
- **`MockTradeLog`** - Simulated trading records
- **`TradeType`** - Trading operation types (BUY, SELL)

### Investment Advice
- **`InvestmentAdvice`** - Investment recommendation data structure
- **`Recommendation`** - Specific investment recommendations
- **`InvestmentAdviceService`** - Investment advice orchestration
- **`InvestmentAdviceStrategy`** - Advice generation strategy interface
- **`DefaultAdvice`** - Default investment advice implementation

### Trade Analysis
- **`TradeAnalysisResult`** - Trade performance analysis results
- **`TradeAnalysisService`** - Trade analysis orchestration
- **`TradeAnalysisStrategy`** - Analysis strategy interface
- **`DefaultTradeAnalysis`** - Default analysis implementation

---

## 7. Simulation Engine

### Simulation Entities
- **`SimulationSession`** - Trading simulation sessions
- **`MockInvestmentSetting`** - Simulation configuration parameters
- **`SimulationStatus`** - Simulation state tracking

### Market Data
- **`MarketDataType`** - Market data classification
- **`MarketDataProvider`** - Market data interface
- **`HistoricalMarketDataProvider`** - Historical data implementation
- **`SyntheticMarketDataProvider`** - Synthetic data generation

### Simulation Engines
- **`SimulationEngine`** - Simulation execution interface
- **`SysntheticSimulationEngine`** - Synthetic data simulation implementation

---

## 8. Content Translation & Explanation System

### Input Handling
- **`InputHandlerFactory`** - Input handler creation factory
- **`DragInputHandler`** - Drag-and-drop input processing
- **`KeyboardInputHandler`** - Keyboard input processing
- **`VocaInputHandler`** - Voice input processing

### Content Explanation
- **`ExplanationService`** - Content explanation orchestration
- **`ExplanationGenerator`** - Explanation generation interface
- **`OpenAIExplanationGenerator`** - OpenAI-based explanation generation
- **`OurModelExplanationGenerator`** - Custom model explanation generation

---

## 9. Recommendation System

### Recommendation Services
- **`RecommendService`** - Content recommendation orchestration
- **`RecommendStrategy`** - Recommendation strategy interface
- **`KeywordBasedRecommend`** - Keyword-based recommendation implementation

---

## 10. Delivery & Notification System

### Email System
- **`EmailSender`** - Email sending interface
- **`SmtpEmailSender`** - SMTP email implementation
- **`EmailSendingService`** - Email orchestration service

### Email Entities
- **`EmailSendHistory`** - Email delivery tracking
- **`EmailSendStatus`** - Email delivery status enum
- **`SummaryLevel`** - Content summary levels
- **`UserAlarmInfo`** - User notification preferences

### Dispatch System
- **`DispatchStrategy`** - Content dispatch interface
- **`QueueDispatchStrategy`** - Queue-based dispatch implementation
- **`ScheduledDispatchStrategy`** - Scheduled dispatch implementation

### Orchestration
- **`ArticleDeliveryOrchestrator`** - Article delivery coordination
  - Manages end-to-end article delivery process

---

## 11. History Tracking

### Search History
- **`SearchHistory`** - User search activity tracking

---

## 12. Global Components

### API Response System
- **`ApiResponse`** - Standardized API response wrapper
- **`SuccessStatus`** - Success response status codes
- **`ErrorStatus`** - Error response status codes

### Exception Handling
- **`WhiplashException`** - Custom application exception
- **`WhiplashExceptionHandler`** - Global exception handler

### Base Entities
- **`BaseEntity`** - JPA auditing base class
  - Common fields (createdAt, updatedAt) for all entities

### Data Converters
- **`AuthConverter`** - Authentication data transformation
- **`UserConverter`** - User data transformation

### Configuration
- **`Constants`** - Application constants and configuration values

### Health Check
- **`HealthCheckController`** - Application health monitoring endpoint

---

## 13. Repository Interfaces

### Data Access Layer
- **`InvestorProfileRepository`** - Investor profile data access

---

## Architecture Summary

### Key Patterns Used

1. **Strategy Pattern**: Extensively used for algorithmic flexibility
   - Rebalancing, Risk Response, Safety Analysis strategies
   - Recommendation and Explanation strategies

2. **Service Layer Pattern**: Clear separation of business logic
   - Services orchestrate business operations
   - Controllers handle HTTP concerns only

3. **Repository Pattern**: Data access abstraction
   - JPA repositories for relational data
   - MongoDB repositories for document storage

4. **Factory Pattern**: Dynamic object creation
   - InputHandlerFactory for different input types

5. **DTO Pattern**: Data transfer between layers
   - Clear separation between internal models and API contracts

### Technology Stack

- **Backend Framework**: Spring Boot
- **Security**: Spring Security with JWT
- **Persistence**: JPA (MySQL) + MongoDB
- **Authentication**: OAuth2 (Kakao)
- **Scheduling**: Spring Scheduling
- **Email**: SMTP integration

### Domain Organization

The application is organized into clear functional domains:
- **User Management**: Authentication, profiles, preferences
- **Content Management**: Articles, summarization, assignment
- **Investment Management**: Portfolios, analysis, recommendations
- **Simulation**: Trading simulation and market data
- **Delivery**: Email notifications and content dispatch
- **Translation**: Content explanation and input handling

This architecture supports scalability through clear separation of concerns and extensibility through strategy patterns and interface-based design.