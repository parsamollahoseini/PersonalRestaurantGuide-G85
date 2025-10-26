
# Personal Restaurant Guide 🍽️

## Group G-85

### Team Members
- **Parsa Molahosseini** (Leader) - Data model, ViewModel, validation
- **Mehrad Bayat** - Navigation setup, HomeScreen, DetailsScreen
- **Kevin George Buhain** - AddEditScreen, AboutScreen
- **Jerry-Lee Somera** - MapScreen, Android intents (Maps, Share)

---

## Current Status

### ✅ Completed by Parsa
- [x] Project structure and Gradle configuration
- [x] Restaurant data class
- [x] Sample restaurant data
- [x] RestaurantVm (ViewModel for state management)
- [x] Input validation functions (phone, coordinates)
- [x] Material3 theme setup
- [x] App resources and manifest

### ⏳ Pending - Team Tasks

**Mehrad's Tasks:**
- [ ] Add Navigation Component setup
- [ ] Implement SplashScreen with auto-navigation
- [ ] Create HomeScreen with restaurant list
- [ ] Implement search/filter functionality
- [ ] Build DetailsScreen with restaurant info

**Kevin's Tasks:**
- [ ] Create AddEditScreen with form
- [ ] Implement form validation UI
- [ ] Add rating selection UI
- [ ] Build AboutScreen

**Jerry's Tasks:**
- [ ] Create MapScreen
- [ ] Implement shareRestaurant() intent
- [ ] Implement openMap() intent
- [ ] Add delete confirmation dialog

---

## Technical Details

### Data Model
```kotlin
data class Restaurant(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val description: String,
    val tags: String,
    val rating: Int,
    val lat: Double,
    val lng: Double
)
```

### ViewModel Methods
- `items: List<Restaurant>` - Get all restaurants
- `byId(Long): Restaurant?` - Find restaurant by ID
- `addOrUpdate(Restaurant)` - Create or update restaurant
- `remove(Long)` - Delete restaurant

### Validation Functions
- `isValidPhone(String): Boolean` - 10-11 digits
- `isValidLatitude(Double): Boolean` - -90 to 90
- `isValidLongitude(Double): Boolean` - -180 to 180

---

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Design:** Material Design 3
- **Architecture:** MVVM (ViewModel)
- **Navigation:** Jetpack Navigation Component
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36

---

## Setup Instructions for Team

1. **Clone the repository:**
```bash
git clone https://github.com/YOUR_USERNAME/PersonalRestaurantGuide-G85.git
cd PersonalRestaurantGuide-G85
```

2. **Configure Git with your name:**
```bash
git config user.name "Your Full Name"
git config user.email "your.email@student.gbc.ca"
```

3. **Open in Android Studio:**
   - File → Open → Select project folder
   - Wait for Gradle sync
   - Verify app runs (shows placeholder screen)

4. **Start your assigned tasks in MainActivity.kt**

5. **Commit your work:**
```bash
git add app/src/main/java/ca/gbc/g85/personalrestaurantguide/MainActivity.kt
git commit -m "Add [your feature] - [Your Name]"
git push
```

---

## Running the App

1. Connect Android device or start emulator
2. Click Run (green play button)
3. Currently shows: Placeholder screen confirming data model ready
4. After team adds screens: Full restaurant management app

---

**Last Updated:** October 26, 2025 by Parsa Molahosseini
