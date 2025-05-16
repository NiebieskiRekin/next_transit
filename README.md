# Next Transit
A beginner project Android app that shows the next available transit info as a home-screen widget using Google Maps Directions API.
Built in Kotlin using Android Studio IDE, Gradle and libraries such as: Jetpack Compose, Glance, Ktor, kotlinx-serialization & kotlinx-coroutines

[ Work in progress ]

# Build & Run
Click the button in the IDE and hope for the best? No idea for now...


# Koncept aplikacji
Aplikacja pobiera od użytkownika z kalendarza kiedy użytkownik chce się znaleźć w danym miejscu.
Na tej podstawie wypytując Google Directions API ustala najlepsze opcje podróży transportem publicznym.
Podróże są wpisywane w kalendarz.
W przypadku opóźnienia danego (wspieranego przez API) środka transportu wysyłane jest powiadomienie
na telefon (Firebase cloud messaging) oraz sugerowana jest inna opcja podróży.
Dodatkowo na Home screen wyświetlony jest widget (Jetpack Glance) pokazujący informację o najbliższej podróży.

# TODO
- [ ] sprawdzanie wydarzeń w kalendarzu
- [ ] widoki w aplikacji (nawigacja przez bottom bar):
  - [ ] Widok konfiguracji kalendarza (zezwolenie na dostęp, podgląd, zaznaczanie kiedy-gdzie, 
    ustawienie minimalnego czasu do powrotu)
  - [ ] Widok akceptacji powiadomień
  - [ ] Opcje konfiguracji widgetu
  - [ ] Wyświetlanie opcji podróży
  - [ ] Widok powiadomienia
- [ ] prosty backend (Proponuję Hono w Nodejs, Drizzle, Postgres)
  - [ ] Logowanie (z oauth Google)
  - [ ] Rejestracja
  - [ ] Tworzenie tokenu urządenia do FCM
  - [ ] Wysyłanie powiadomień
  - [ ] Sprawdzanie oczekujących zadań
  - [ ] Tworzenie harmonogramu z kalendarza i dopiswanie podróży
 
# OLD TODO (ignore)
- [x] Collapsable Lazy Column AppSettings View
- [ ] Download AppSettings json file
- [ ] Create tests
- [ ] Add build instructions
- [ ] Add release .apk to git repo
- [ ] Add widget intent in app in MainActivity
- [ ] Widget configuration screen
- [ ] Unlink widget and app data stores
- [ ] Update widget preview