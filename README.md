# Built Daily

A streak-based workout planner mobile app that generates personalized daily workouts and helps users build consistent fitness habits.

## Overview

Built Daily removes the friction from starting a workout routine. Instead of scrolling through endless YouTube videos, users define their fitness goals and preferences once, and the app generates a personalized daily workout plan with hand-picked YouTube videos that match their routine.

The app tracks workout streaks to motivate consistency and help users build lasting fitness habits.

## Key Features

- **Personalized Workout Generation** - Define your fitness goals and preferences, and get a custom daily workout plan
- **YouTube Integration** - Seamlessly integrated video selection using the YouTube Data API to find relevant workout videos
- **Streak Tracking** - Visual streak counter to motivate consistent workouts and habit building
- **Decision Removal** - No more decision fatigue—the app picks the right videos for you based on your plan
- **Mobile-First Experience** - Native mobile app optimized for quick access and intuitive navigation

## Technical Architecture

### Backend & API Integration
- **YouTube Data API** - Implemented full integration with the YouTube Data API to intelligently search and filter workout videos based on user preferences and workout type
- Handles video metadata retrieval, relevance ranking, and caching for optimal performance
- Manages authentication and rate limiting

### Frontend
- Native mobile UI with features for:
  - Workout plan customization and goal setting
  - Daily workout display with integrated video previews
  - Streak tracking and achievement visualization
  - User preference management

## My Contributions

**API & Backend:**
- Designed and implemented complete YouTube Data API integration
- Built the algorithm that matches user workout preferences to relevant YouTube videos
- Handled authentication, API key management, and request optimization
- Implemented error handling and fallback mechanisms

**Frontend Features:**
- Developed key UI components for workout display and video integration
- Built user preference management interface
- Implemented streak tracking visualization

## Tech Stack

- **Language:** Java
- **APIs:** YouTube Data API v3
- **Platform:** Mobile (Android/Java)

## Getting Started

### Prerequisites
- YouTube Data API key ([Get one here](https://developers.google.com/youtube/registering_an_application))
- Java Development Kit (JDK) 8 or higher
- Android Studio (for building/running the mobile app)

### Setup

1. Clone the repository
```bash
git clone https://github.com/nsnow099/Built-Daily.git
cd Built-Daily
```

2. Configure your YouTube API credentials
   - Add your YouTube Data API key to the project configuration
   - Ensure proper API permissions are enabled

3. Build and run the application
```bash
# Using Android Studio or gradle
./gradlew build
```

## Project Highlights

🎯 **Smart Video Matching** - The core algorithm intelligently filters YouTube videos by workout type, duration, difficulty, and user preferences

🔄 **Streak Motivation** - Leverages gamification principles with streak tracking to encourage habit formation

⚡ **Performance Optimized** - Efficient API usage with intelligent caching to reduce latency and API quota consumption

## Lessons & Learning

This project reinforced the importance of:
- Third-party API integration and rate limiting best practices
- User experience design for habit-forming apps
- Mobile app optimization and performance considerations

## Future Enhancements

- Advanced recommendation algorithm using user workout history
- Social features for sharing workouts and streaks
- Workout performance metrics and progress tracking
- Integration with fitness tracking APIs

## License

This project is open source and available under the MIT License.

---

**Portfolio Contact:** [GitHub Profile](https://github.com/nsnow099)
