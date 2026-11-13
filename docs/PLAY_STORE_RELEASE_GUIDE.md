# Google Play Store Release & Publishing Guide — NOOR LIFE (নূর লাইফ)

> **Status Note:** This repository contains the verified Android codebase, release configuration, Play Store listing copy, Data Safety mapping, and build instructions. Final Play Console upload, privacy URL hosting, AdMob production ID linking, and Google Play review are completed by the publisher in Google Play Console.

---

## 1. App Identity & Store Listing Copy

- **Application ID (Package Name):** `com.noorlife.islamicapp`
- **Version:** `1.0.0` (`versionCode = 1`)
- **Default Language:** Bengali (`bn-BD`)
- **App Name (<= 30 chars):**
  - Bengali: `নূর লাইফ - NOOR LIFE`
  - English: `NOOR LIFE - Islamic Companion`

### Short Description (<= 80 chars)
- **Bengali:** `সম্পূর্ণ বাংলা কুরআন, নামাজের সময়, কিবলা, দোয়া, তাসবিহ ও ইসলামিক জীবনসঙ্গী।`
- **English:** `Complete Bengali Quran, Prayer Times, Qibla, Duas, Tasbih & Islamic Companion.`

### Full Description (Bengali — Store Listing Ready)
নূর লাইফ (NOOR LIFE) — আপনার সম্পূর্ণ ইসলামিক জীবনসঙ্গী। আধুনিক ডিজাইন ও সহজ বাংলা ভাষায় সাজানো একটি নির্ভরযোগ্য অফলাইন-সহায়ক ইসলামিক অ্যাপ।

**প্রধান বৈশিষ্ট্যসমূহ:**
- **সঠিক নামাজের সময়সূচি ও ক্যালেন্ডার:** ইসলামিক ফাউন্ডেশন বাংলাদেশসহ ৬টি স্বীকৃত গণনা পদ্ধতি, হানাফি ও শাফেয়ী আসর সেটিংস, বাংলাদেশের ৬৪টি জেলা ও আন্তর্জাতিক শহর, জিপিএস লোকেশন এবং মাসিক সময়সূচি।
- **ওয়াক্ত ও আজান রিমাইন্ডার:** ফজর, যোহর, আসর, মাগরিব ও এশার আলাদা নোটিফিকেশন নিয়ন্ত্রণ, আজান ধ্বনি ও কম্পন সেটিংস।
- **সম্পূর্ণ আল-কুরআন (১১৪ সূরা ও ৩০ পারা):** বিশুদ্ধ আরবি উসমানি টেক্সট, বাংলা উচ্চারণ, বাংলা অনুবাদ, ইংরেজি অনুবাদ, তাফসির ইবনে কাসীর (ক্যাশকৃত), বুকমার্ক, শেষ পঠিত অবস্থান, ফন্ট সাইজ নিয়ন্ত্রণ ও পটভূমিতে অডিও তিলাওয়াত (অফলাইন ডাউনলোড সুবিধাসহ)।
- **কিবলা কম্পাস:** মক্কা শরীফের সঠিক দিক ও দূরত্ব নির্ণয়, সেন্সর ক্যালিব্রেশন গাইড এবং সেন্সর না থাকলে সঠিক ডিগ্রি নির্দেশনা।
- **ডিজিটাল তাসবিহ:** দৈনন্দিন জিকির, কাস্টম জিকির, লক্ষ্যমাত্রা (৩৩/৯৯/১০০+), হ্যাপটিক ভাইব্রেশন ও দৈনিক ইতিহাস।
- **আল্লাহর ৯৯ নাম, দৈনন্দিন দোয়া ও ৫ কালিমা:** আরবি, বাংলা উচ্চারণ ও অর্থসহ।
- **পূর্ণাঙ্গ নামাজ, অজু ও গোসল শিক্ষা:** ধাপে ধাপে নিয়মাবলি।
- **হিজরি, বাংলা ও ইংরেজি ক্যালেন্ডার:** চাঁদ দেখার ভিত্তিতে ±২ দিন সমন্বয় সুবিধা ও ইসলামিক গুরুত্বপূর্ণ দিবস।
- **রমজান ও যাকাত ক্যালকুলেটর:** সাহরি-ইফতার সময়সূচি, রোজা ট্র্যাকার ও নিসাবভিত্তিক যাকাত হিসাব।
- **হাদিস, ইসলামিক কুইজ, গ্রিটিং কার্ড, ওয়ালপেপার ও ব্যক্তিগত নোট।**

---

## 2. Google Play Data Safety Form Mapping

| Question / Category | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** (only if Google AdMob SDK collects Advertising ID / Device signals & Diagnostics per Google AdMob SDK disclosures; developer collects **zero** user account/PII data). |
| Account creation required? | **No** (100% offline/local usage without login). |
| Location (Approximate / Precise) | Collected only on-device if user grants optional GPS permission for prayer times & Qibla; **not** stored on developer servers, **not** shared for third-party marketing. |
| App Activity / User Content (Bookmarks, Notes, Tasbih) | Stored **only locally** on user's device via Room/DataStore (never uploaded). |
| Device or Other IDs | Used by **Google Mobile Ads (AdMob)** for ad serving and frequency capping (encrypted in transit). |

---

## 3. Permissions & Foreground Service Declarations (Play Console App Content)

- **Location (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`):** Optional runtime permission requested only when user clicks "Detect GPS Location" or opens Qibla Compass. Users can manually select any city without granting location access.
- **Notifications (`POST_NOTIFICATIONS`):** Optional runtime permission (Android 13+) for user-enabled prayer reminders.
- **Exact Alarms (`SCHEDULE_EXACT_ALARM`):** Optional permission (`AlarmManager.canScheduleExactAlarms()`) for prayer-time reminders; degrades gracefully to inexact alarms and displays transparent Android OS battery/exact-alarm notices to the user.
- **Media Playback Foreground Service (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`):** Declared for background Quran recitation playback via AndroidX Media3 `MediaSessionService`, displaying persistent media notification controls.
- **Photo Picker Compliance:** Zero `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES` permissions requested.

---

## 4. Production AdMob Checklist (MANDATORY Before Store Release)

Currently the build ships with Google's official **test AdMob IDs** so testing and development never violate AdMob policies:
1. Open `app/src/main/res/values/strings.xml` and replace `admob_app_id` (`ca-app-pub-3940256099942544~3347511713`) with your real AdMob App ID.
2. Open `app/src/main/java/com/example/ads/NoorAdManager.kt` and replace `PROD_BANNER_UNIT_ID` and `PROD_INTERSTITIAL_UNIT_ID` with your verified AdMob Unit IDs (in release builds `BuildConfig.DEBUG == false`, `AdConfig` automatically selects the production IDs).
3. Verify AdMob **app-ads.txt** is published on your developer website if required by AdMob.

---

## 5. Building Signed Release App Bundle (`.aab`)

Play Store requires a signed Android App Bundle (`.aab`). Provide your upload keystore credentials via environment variables (do **NOT** commit keystores or passwords into Git):

```bash
export KEYSTORE_PATH=/path/to/your-upload-key.jks
export STORE_PASSWORD="your-store-password"
export KEY_PASSWORD="your-key-password"
gradle bundleRelease
```

The signed AAB output will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

### Pre-Submission Testing Checklist
1. Verify all 114 Surahs load offline and audio streams/downloads play in background.
2. Verify all 5 bottom navigation tabs and secondary screens render properly in Light Mode, Dark Mode, Bengali, and English.
3. Verify GPS permission grant/deny and Notification permission grant/deny flows on real Android 13/14/15 devices.
4. Verify ads only show on non-worship screens and never appear inside Quran, Tafsir, Salah, Wudu, Dua, or Tasbih screens.
5. Host `PRIVACY_POLICY.md` at a public HTTPS URL and paste that URL into Google Play Console -> App Content -> Privacy Policy.
