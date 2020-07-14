# WeatherApp

================== Goal of the Exercise =======================

Goal: The goal of this exercise is to build a standard android app that can scale and is maintainable and testable. 

Guidelines: Design your app architecture using best practices prescribed by Google Use Android jetpack components where possible. 
Use your preferred language between Kotlin and Java. 
Feel free to use any third party libraries as needed. 

Exercise: You will be building a Weather app using the metaweather api (https://www.metaweather.com/api/) with the following features:
Search for location
Display location information, current weather and 5/10 day forecast when available 
Display weather from a selectable date in the past year (Optional) 

================== Developer's notes =======================

When the app starts the first time, it will ask for permission to access to the Location Service
and read Latitude and Longitude from the local GPS.
This operation might take a few seconds.



Once read, it queries the backend to retrieve the closest weather location.
If found, it queries the backend for the current and forecast weather.
Th today information are displayed:
temperature, sunrise/sunset time, air pressure, humidity, wind, visibility
The 5/10 forecast is also shown at the bottom.

There's a calendar button of the left side of the Current Date.
When clicked, it opens a calendar and let the user pick a date.
Then, the app retrieve the historical weather for that location and date

On the left of the location name, there's an Edit button.
Use this button to manually change the location.
The user search for a new location or again query the gps for the closest location


Use of:

MVVM design
Retrofit + OkHttp3
DataBinding
LiveData
Coroutines (refresh weather on swipe down)
Navigation Component
RecyclerView
NestedScrollView
ConstraintLayout
Cardview
Animation
Location Service (gps coordinates)
Calendar:
    for the calendar, I took this library from gitHub:
    https://github.com/CleverPumpkin/CrunchyCalendar
    I did not spend too much time looking for the best looking widget.
    The earliest date is 01/01/2014 as suggested in metaweather and the latest is _today_
DI - Dependency Injection for the repositories using Hilt
